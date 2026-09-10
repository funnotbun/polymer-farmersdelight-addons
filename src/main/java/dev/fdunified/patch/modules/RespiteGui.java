package dev.fdunified.patch.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Minimum port of the farmers-delight-patch UiResourceCreator system:
 * title-carried backgrounds via a private-use font, icon/progress builders
 * backed by generated pack models, and pack-build asset generation.
 * All art lives under our own assets (see textures/sgui/). Touching this
 * class (e.g. via {@link #setup()}) triggers static registration, which
 * must happen during mod init, before pack build.
 */
public final class RespiteGui {
    private static final String BASE_MODEL = "minecraft:item/generated";
    private static final FontDescription GUI_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("fd-unified-patch", "gui"));
    private static final Style STYLE = Style.EMPTY.withColor(0xFFFFFF).withFont(GUI_FONT);

    private static final String ITEM_TEMPLATE = """
            {"parent":"|BASE|","textures":{"layer0":"|ID|"}|DISPLAY|}""";

    private static final List<SimpleModel> SIMPLE_MODEL = new ArrayList<>();
    private static final Char2IntMap SPACES = new Char2IntOpenHashMap();
    private static final List<FontTexture> FONT_TEXTURES = new ArrayList<>();
    private static char character = 'a';

    private static final char CHEST_SPACE0 = character++;
    private static final char CHEST_SPACE1 = character++;

    private RespiteGui() {
    }

    public static final Function<Component, Component> KETTLE_BACKGROUND = background("kettle");
    public static final Supplier<GuiElementBuilder> EMPTY_ICON = icon16("empty");
    public static final Supplier<GuiElementBuilder> HEATED_ICON = icon16("heated");
    private static final ItemStackTemplate[] ARROW_PROGRESS = generatedIcons("arrow", 16, "");
    private static final ItemStackTemplate[] WATER_LEVELS = generatedIcons("water", 3,
            ",\"display\":{\"gui\":{\"translation\":[0,-1,0]}}");
    // Art is +2/+2 off in its canvas; screen (-2 left, -2 up) is [-2, +2] here (display Y is up).
    public static final Supplier<GuiElementBuilder> BOTTLE_ICON = icon16offset("bottle", -2, 2);

    public static Supplier<GuiElementBuilder> icon16(String path) {
        return iconModel(path, "");
    }

    public static Supplier<GuiElementBuilder> icon16offset(String path, int x, int y) {
        return iconModel(path, ",\"display\":{\"gui\":{\"translation\":[" + x + "," + y + ",0]}}");
    }

    public static GuiElementBuilder waterLevel(int level) {
        return progressLevel(WATER_LEVELS, level);
    }

    public static GuiElementBuilder arrowProgress(int pixels) {
        return progressLevel(ARROW_PROGRESS, pixels);
    }

    private static GuiElementBuilder progressLevel(ItemStackTemplate[] levels, int level) {
        if (level <= 0) {
            return EMPTY_ICON.get();
        }
        return new GuiElementBuilder(levels[Math.min(level, levels.length) - 1])
                .setName(Component.empty()).hideDefaultTooltip();
    }

    private static ItemStackTemplate[] generatedIcons(String path, int count, String display) {
        var models = new ItemStackTemplate[count];
        for (int i = 0; i < count; i++) {
            models[i] = genericIconRaw(Items.ALLIUM, "gen/" + path + "_" + (i + 1), BASE_MODEL, display);
        }
        return models;
    }

    private static Supplier<GuiElementBuilder> iconModel(String path, String display) {
        var model = genericIconRaw(Items.ALLIUM, path, BASE_MODEL, display);
        return () -> new GuiElementBuilder(model).setName(Component.empty()).hideDefaultTooltip();
    }

    private static ItemStackTemplate genericIconRaw(Item item, String path, String base, String display) {
        var texturePath = elementPath(path);
        var modelPath = elementPath(path);
        SIMPLE_MODEL.add(new SimpleModel(texturePath, modelPath, base, display));
        return new ItemStackTemplate(Items.TRIAL_KEY, DataComponentPatch.builder()
                .set(DataComponents.ITEM_MODEL, ResourcePackExtras.bridgeModel(texturePath)).build());
    }

    private static Identifier elementPath(String path) {
        return Identifier.fromNamespaceAndPath("fd-unified-patch", "sgui/elements/" + path);
    }

    public static Function<Component, Component> background(String path) {
        var builder = new StringBuilder().append(CHEST_SPACE0);
        var c = (character++);
        builder.append(c);
        builder.append(CHEST_SPACE1);
        FONT_TEXTURES.add(new FontTexture(
                Identifier.fromNamespaceAndPath("fd-unified-patch", "sgui/" + path), 13, 256,
                new char[][]{new char[]{c}}));
        var base = Component.literal(builder.toString()).setStyle(STYLE);
        return (text) -> Component.empty().append(base).append(text);
    }

    public static void setup() {
        SPACES.put(CHEST_SPACE0, -8);
        SPACES.put(CHEST_SPACE1, -168);
        // Bridges every model under our sgui folder (icons + sliced frames),
        // same as the FD patch does for its own sgui folder.
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                Identifier.fromNamespaceAndPath("fd-unified-patch", "sgui"));
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((b) -> generateAssets(b::addData));
    }

    private static void generateAssets(BiConsumer<String, byte[]> assetWriter) {
        for (var texture : SIMPLE_MODEL) {
            assetWriter.accept("assets/" + texture.modelPath.getNamespace() + "/models/" + texture.modelPath.getPath() + ".json",
                    ITEM_TEMPLATE.replace("|ID|", texture.texturePath.toString()).replace("|BASE|", texture.base).replace("|DISPLAY|", texture.display).getBytes(StandardCharsets.UTF_8));
        }
        generateProgressTextures(assetWriter);
        var fontBase = new JsonObject();
        var providers = new JsonArray();
        var spaces = new JsonObject();
        spaces.addProperty("type", "space");
        var advances = new JsonObject();
        SPACES.char2IntEntrySet().stream().sorted(Comparator.comparing(Char2IntMap.Entry::getCharKey))
                .forEach((c) -> advances.addProperty(Character.toString(c.getCharKey()), c.getIntValue()));
        spaces.add("advances", advances);
        providers.add(spaces);

        FONT_TEXTURES.forEach((entry) -> {
            var bitmap = new JsonObject();
            bitmap.addProperty("type", "bitmap");
            bitmap.addProperty("file", entry.path + ".png");
            bitmap.addProperty("ascent", entry.ascent);
            bitmap.addProperty("height", entry.height);
            var chars = new JsonArray();
            for (var a : entry.chars) {
                var builder = new StringBuilder();
                for (var b : a) {
                    builder.append(b);
                }
                chars.add(builder.toString());
            }
            bitmap.add("chars", chars);
            providers.add(bitmap);
        });
        fontBase.add("providers", providers);
        assetWriter.accept("assets/fd-unified-patch/font/gui.json", fontBase.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void generateProgressTextures(BiConsumer<String, byte[]> assetWriter) {
        try {
            var water = readTexture("water");
            int[] starts = {11, 6, 1};
            for (int level = 1; level <= starts.length; level++) {
                int start = starts[level - 1];
                var image = new BufferedImage(water.getWidth(), water.getHeight(), BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, start, water.getWidth(), water.getHeight() - start,
                        water.getRGB(0, start, water.getWidth(), water.getHeight() - start,
                                null, 0, water.getWidth()), 0, water.getWidth());
                writeTexture(assetWriter, "water_" + level, image);
            }

            var arrow = readTexture("arrow");
            for (int width = 1; width <= arrow.getWidth(); width++) {
                var image = new BufferedImage(arrow.getWidth(), arrow.getHeight(), BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, 0, width, arrow.getHeight(),
                        arrow.getRGB(0, 0, width, arrow.getHeight(), null, 0, width), 0, width);
                writeTexture(assetWriter, "arrow_" + width, image);
            }
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Failed to generate GUI progress textures", e);
        }
    }

    private static BufferedImage readTexture(String path) throws IOException {
        try (var stream = Objects.requireNonNull(RespiteGui.class.getResourceAsStream(
                "/assets/fd-unified-patch/textures/sgui/elements/" + path + ".png"))) {
            return ImageIO.read(stream);
        }
    }

    private static void writeTexture(BiConsumer<String, byte[]> assetWriter, String path, BufferedImage image)
            throws IOException {
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        assetWriter.accept("assets/fd-unified-patch/textures/sgui/elements/gen/" + path + ".png",
                output.toByteArray());
    }

    private record FontTexture(Identifier path, int ascent, int height, char[][] chars) {
    }

    private record SimpleModel(Identifier texturePath, Identifier modelPath, String base, String display) {
    }
}
