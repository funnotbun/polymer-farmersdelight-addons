package dev.fdunified.patch.common;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.other.PolymerPotion;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.util.function.Function;

/**
 * Small static helpers over the farmers-delight-patch patterns.
 * v0.1 (MoreDelight, items-only) used overlayItem/overlayAllItems/registerTab;
 * block-adding modules additionally use overlayAllBlocks with a preset from
 * {@link BlockPresets}, overlayAllPotions for vanilla-effect potions, and
 * bridgeBlockModels (without it, overlaid blocks are missing-texture checkers).
 */
public final class PatchOverlays {
    private PatchOverlays() {
    }

    /** Overlay one item; knives get the interaction flag (see PolyItem). */
    public static void overlayItem(Item item) {
        PolymerItem.registerOverlay(item, new PolyItem(item, item instanceof KnifeItem));
    }

    /**
     * Overlay every item currently in a WHITELISTED namespace.
     * Must run during mod init (registries freeze before server start, so
     * Polymer overlays cannot be added later). The DelightAddon mixin is the
     * primary path; this sweep covers items registered before our entrypoint
     * ran. Never call with an unknown namespace.
     */
    public static int overlayAllItems(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                overlayItem(entry.getValue());
                count++;
            }
        }
        return count;
    }

    /** Read-only count of items in a namespace (safe any time, even frozen). */
    public static int countItems(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                count++;
            }
        }
        return count;
    }

    /** Overlay a block with a FactoryBlock preset (future block-adding modules). */
    public static void overlayBlock(Identifier id, Block block, FactoryBlock preset) {
        PolymerBlock.registerOverlay(block, preset);
        BlockWithElementHolder.registerOverlay(block, preset);
        BlockStateModelManager.addBlock(id, block);
    }

    /**
     * Bridge a WHITELISTED namespace's block-model folder so BSMM display
     * entities resolve (writes the {@code items/-/block} stubs at pack
     * build). Without this, overlaid blocks render as missing-texture
     * checkers even though raw models/textures ship in the pack. Same call
     * the farmers-delight-patch makes for {@code farmersdelight:block}.
     */
    public static void bridgeBlockModels(String namespace) {
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.fromNamespaceAndPath(namespace, "block"));
    }

    /**
     * Overlay every block currently in a WHITELISTED namespace, classifying
     * each with the module's preset function. Same fail-closed rule as
     * {@link #overlayAllItems}: never call with an unknown namespace.
     */
    public static int overlayAllBlocks(String namespace, Function<Block, FactoryBlock> preset) {
        int count = 0;
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                overlayBlock(entry.getKey().identifier(), entry.getValue(), preset.apply(entry.getValue()));
                count++;
            }
        }
        return count;
    }

    /** Read-only count of blocks in a namespace (safe any time, even frozen). */
    public static int countBlocks(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Hide one potion from vanilla registry sync (null replacement, same as
     * effects). Held/brewed stacks degrade to empty contents client-side via
     * Polymer's PotionContents handling; server behavior is unchanged.
     */
    public static void overlayPotion(Potion potion) {
        PolymerPotion.registerOverlay(potion, HIDDEN_POTION);
    }

    private static final PolymerPotion HIDDEN_POTION = new PolymerPotion() {
        @Override
        public Potion getPolymerReplacement(Potion serverPotion, net.fabricmc.fabric.api.networking.v1.context.PacketContext context) {
            return null;
        }
    };

    /** Hide every potion in a WHITELISTED namespace from vanilla sync. */
    public static int overlayAllPotions(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.POTION.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                overlayPotion(entry.getValue());
                count++;
            }
        }
        return count;
    }

    /** Register a source creative tab as a Polymer tab; null-safe no-op. */
    public static void registerTab(Identifier id, CreativeModeTab tab) {
        if (tab != null) {
            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(id, tab);
        }
    }

    /** Look a tab up by id and register it; returns whether it existed. */
    public static boolean registerTabById(String namespace, String path) {
        var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(Identifier.fromNamespaceAndPath(namespace, path));
        if (tab == null) {
            return false;
        }
        registerTab(Identifier.fromNamespaceAndPath(namespace, path), tab);
        return true;
    }

    public static void registerBlockEntity(BlockEntityType<?> type) {
        PolymerBlockUtils.registerBlockEntity(type);
    }

    public static void registerSound(SoundEvent sound) {
        PolymerSoundEvent.registerOverlay(sound);
    }

    public static void registerEffect(MobEffect effect) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.MOB_EFFECT, effect, (server, context) -> null);
    }
}
