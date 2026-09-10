package dev.fdunified.patch;

import dev.fdunified.patch.modules.MoreDelightModule;
import dev.fdunified.patch.modules.RespiteModule;
import dev.fdunified.patch.modules.RusticModule;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * v0.3: DelightLib-backed MoreDelight, RusticDelight, plus Farmer's Respite.
 * Modules gate on FabricLoader.isModLoaded; anything else is a no-op (fail
 * closed, no scanning of unknown namespaces). Rustic/Respite overlays
 * trigger from their init mixins (entrypoint-order safe); the entrypoint
 * only arms a guard.
 */
public class FdUnifiedPatch implements ModInitializer {
    public static final String MOD_ID = "fd-unified-patch";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        var loader = FabricLoader.getInstance();
        if (loader.isModLoaded("moredelight") && loader.isModLoaded("delightlib")) {
            var module = new MoreDelightModule();
            logAddonVersion("moredelight", module.versionRange());
            module.apply();
        } else {
            LOGGER.info("[fd-unified-patch] MoreDelight not present; patch idle (fail closed)");
        }
        if (loader.isModLoaded("rusticdelight")) {
            logAddonVersion("rusticdelight", new RusticModule().versionRange());
            ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                if (!RusticModule.applied()) {
                    LOGGER.error("[fd-unified-patch] rusticdelight present but overlays never applied; vanilla clients will be rejected (RemapException)");
                }
            });
        }
        if (loader.isModLoaded("farmersrespite")) {
            logAddonVersion("farmersrespite", new RespiteModule().versionRange());
            ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                if (!RespiteModule.applied()) {
                    LOGGER.error("[fd-unified-patch] farmersrespite present but overlays never applied; vanilla clients will be rejected (RemapException)");
                }
            });
        }
    }

    private static void logAddonVersion(String modId, String testedRange) {
        var version = FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
        LOGGER.info("[fd-unified-patch] {} version {}, tested {}", modId, version, testedRange);
    }
}
