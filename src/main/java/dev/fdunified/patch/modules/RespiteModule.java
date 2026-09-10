package dev.fdunified.patch.modules;

import dev.fdunified.patch.FdUnifiedPatch;
import dev.fdunified.patch.common.BlockPresets;
import dev.fdunified.patch.common.PatchModule;
import dev.fdunified.patch.common.PatchOverlays;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vectorwing.farmersdelight.common.block.PieBlock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * v0.3 proof for a block-entity + menu content mod: explicitly whitelists
 * {@code farmersrespite:*}. Triggered by {@code RespiteInitMixin} at the end
 * of Respite init (entrypoint-order safe); the entrypoint only arms a
 * fail-closed guard. The kettle GUI is replaced with {@link KettleUi} (SGUI)
 * by {@code KettleMenuMixin}. Copy this class plus
 * docs/addons/farmersrespite.md (adjusted) to support a future addon; never
 * extend the whitelist in place for another mod.
 */
public final class RespiteModule implements PatchModule {
    public static final String NAMESPACE = "farmersrespite";

    private static final Logger LOGGER = LoggerFactory.getLogger("fd-unified-patch");
    private static final AtomicBoolean APPLIED = new AtomicBoolean(false);

    @Override
    public String modId() {
        return NAMESPACE;
    }

    @Override
    public String versionRange() {
        return "2.3.1";
    }

    /** First call wins; later calls are a no-op (mixin + entrypoint races). */
    public static void tryApply() {
        if (APPLIED.compareAndSet(false, true)) {
            new RespiteModule().apply();
        } else {
            LOGGER.info("[fd-unified-patch] farmersrespite: already applied, skipping");
        }
    }

    public static boolean applied() {
        return APPLIED.get();
    }

    @Override
    public void apply() {
        PolymerResourcePackUtils.addModAssets(NAMESPACE);
        PatchOverlays.bridgeBlockModels(NAMESPACE);
        RespiteGui.setup();
        int items = PatchOverlays.overlayAllItems(NAMESPACE);
        int blocks = PatchOverlays.overlayAllBlocks(NAMESPACE, RespiteModule::presetFor);
        int effects = PatchOverlays.overlayAllEffects(NAMESPACE);
        int sounds = PatchOverlays.overlayAllSounds(NAMESPACE);
        int blockEntities = PatchOverlays.overlayAllBlockEntities(NAMESPACE);
        int menus = PatchOverlays.overlayAllMenus(NAMESPACE);
        // No Polymer API covers these two; hidden so vanilla sync passes.
        // Kettle recipes still brew server-side; they just show no dedicated
        // book category/display client-side (same degradation class as FD's
        // nulled recipe displays).
        PatchOverlays.hideFromSync(BuiltInRegistries.RECIPE_BOOK_CATEGORY,
                Identifier.fromNamespaceAndPath(NAMESPACE, "kettle_drinks"));
        PatchOverlays.hideFromSync(BuiltInRegistries.RECIPE_DISPLAY,
                Identifier.fromNamespaceAndPath(NAMESPACE, "brewing"));
        PolymerResourcePackUtils.addModAssets(FdUnifiedPatch.MOD_ID);
        boolean tab = PatchOverlays.registerTabById(NAMESPACE, "group");
        LOGGER.info("[fd-unified-patch] farmersrespite: {} items, {} blocks, {} effects, {} sounds, {} block entities, {} menus overlaid, tab={}",
                items, blocks, effects, sounds, blockEntities, menus, tab);
        // Registries are frozen by server start, so this is verification only.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            int i = PatchOverlays.countItems(NAMESPACE);
            int b = PatchOverlays.countBlocks(NAMESPACE);
            if (i == 0 || b == 0 || !tab) {
                LOGGER.warn("[fd-unified-patch] farmersrespite: missing content at server start (items={}, blocks={}, tab={})",
                        i, b, tab);
            } else {
                LOGGER.info("[fd-unified-patch] farmersrespite: {} items, {} blocks present at server start", i, b);
            }
        });
    }

    /**
     * Block -&gt; preset table, mirroring the farmers-delight-patch mapping.
     * Respite customs reuse FD base classes only for the rose_hip_pie
     * (FD PieBlock); everything else is matched by path, so no compile
     * dependency on Respite itself is needed. Kettle is waterloggable.
     */
    static FactoryBlock presetFor(Block block) {
        if (block instanceof PieBlock) {
            return BlockPresets.CAMPFIRE;
        }
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (path.equals("kettle")) {
            return BlockPresets.Waterlogged.BARRIER;
        }
        if (path.endsWith("cake") || path.endsWith("pie")) {
            return BlockPresets.CAMPFIRE;
        }
        if (block instanceof FlowerPotBlock) {
            return BlockPresets.BARRIER;
        }
        return BlockPresets.PLANT;
    }
}
