package dev.fdunified.patch.modules;

import dev.fdunified.patch.FdUnifiedPatch;
import dev.fdunified.patch.common.BlockPresets;
import dev.fdunified.patch.common.PatchModule;
import dev.fdunified.patch.common.PatchOverlays;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.block.WildCropBlock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * v0.2 proof for a full content mod: explicitly whitelists
 * {@code rusticdelight:*}. Triggered by {@code RusticInitMixin} at the end of
 * Rustic init (entrypoint-order safe); the entrypoint only arms a fail-closed
 * guard. Copy this class plus docs/addons/rusticdelight.md (adjusted) to
 * support a future addon; never extend the whitelist in place for another mod.
 */
public final class RusticModule implements PatchModule {
    public static final String NAMESPACE = "rusticdelight";

    private static final Logger LOGGER = LoggerFactory.getLogger("fd-unified-patch");
    private static final AtomicBoolean APPLIED = new AtomicBoolean(false);

    @Override
    public String modId() {
        return NAMESPACE;
    }

    @Override
    public String versionRange() {
        return "1.7.0";
    }

    /** First call wins; later calls are a no-op (mixin + entrypoint races). */
    public static void tryApply() {
        if (APPLIED.compareAndSet(false, true)) {
            new RusticModule().apply();
        } else {
            LOGGER.info("[fd-unified-patch] rusticdelight: already applied, skipping");
        }
    }

    public static boolean applied() {
        return APPLIED.get();
    }

    @Override
    public void apply() {
        PolymerResourcePackUtils.addModAssets(NAMESPACE);
        PatchOverlays.bridgeBlockModels(NAMESPACE);
        int items = PatchOverlays.overlayAllItems(NAMESPACE);
        int blocks = PatchOverlays.overlayAllBlocks(NAMESPACE, RusticModule::presetFor);
        int potions = PatchOverlays.overlayAllPotions(NAMESPACE);
        PolymerResourcePackUtils.addModAssets(FdUnifiedPatch.MOD_ID);
        boolean tab = PatchOverlays.registerTabById(NAMESPACE, "item_group");
        LOGGER.info("[fd-unified-patch] rusticdelight: {} items, {} blocks, {} potions overlaid, tab={}",
                items, blocks, potions, tab);
        // Registries are frozen by server start, so this is verification only.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            int i = PatchOverlays.countItems(NAMESPACE);
            int b = PatchOverlays.countBlocks(NAMESPACE);
            if (i == 0 || b == 0 || !tab) {
                LOGGER.warn("[fd-unified-patch] rusticdelight: missing content at server start (items={}, blocks={}, tab={})",
                        i, b, tab);
            } else {
                LOGGER.info("[fd-unified-patch] rusticdelight: {} items, {} blocks present at server start", i, b);
            }
        });
    }

    /**
     * Block -&gt; preset table, mirroring the farmers-delight-patch mapping.
     * Rustic customs reuse FD base classes except PancakeBlock (plain Block,
     * matched by path; all seven end in "pancakes"), so no compile dependency
     * on Rustic itself is needed.
     */
    static FactoryBlock presetFor(Block block) {
        if (block instanceof PieBlock || block instanceof FeastBlock) {
            return BlockPresets.CAMPFIRE;
        }
        if (BuiltInRegistries.BLOCK.getKey(block).getPath().endsWith("pancakes")) {
            return BlockPresets.CAMPFIRE;
        }
        if (block instanceof WildCropBlock) {
            return BlockPresets.PLANT;
        }
        if (block instanceof CropBlock) {
            return BlockPresets.KELP;
        }
        return BlockPresets.BARRIER;
    }
}
