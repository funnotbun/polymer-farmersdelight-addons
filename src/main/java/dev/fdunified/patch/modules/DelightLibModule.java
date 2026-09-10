package dev.fdunified.patch.modules;

import dev.fdunified.patch.common.PatchModule;
import dev.fdunified.patch.common.PatchOverlays;
import dev.fdunified.patch.FdUnifiedPatch;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic engine for DelightLib-backed addons: overlays every item in an
 * explicitly WHITELISTED namespace registered through DelightLib, registers
 * the addon creative tab, and adds mod assets. Unknown namespaces are a
 * no-op with a clear log line (fail closed, no auto-detection).
 */
public class DelightLibModule implements PatchModule {
    protected static final Logger LOGGER = LoggerFactory.getLogger("fd-unified-patch");

    private static final Map<String, DelightLibModule> ACTIVE = new ConcurrentHashMap<>();
    private static final Set<String> WARNED_NAMESPACES = Collections.synchronizedSet(new HashSet<>());

    private final Set<String> namespaces;

    protected DelightLibModule(Set<String> namespaces) {
        this.namespaces = Set.copyOf(namespaces);
    }

    @Override
    public String modId() {
        return "delightlib";
    }

    @Override
    public String versionRange() {
        return "26.06.23-26.2-fabric";
    }

    /** Namespaces this module whitelists. Only these are ever touched. */
    public Set<String> namespaces() {
        return namespaces;
    }

    @Override
    public void apply() {
        ACTIVE.put(modId(), this);
        for (String namespace : namespaces) {
            PolymerResourcePackUtils.addModAssets(namespace);
            int early = PatchOverlays.overlayAllItems(namespace);
            if (early > 0) {
                LOGGER.info("[fd-unified-patch] {}: {} items already registered, overlaid at init", namespace, early);
            }
        }
        PolymerResourcePackUtils.addModAssets(FdUnifiedPatch.MOD_ID);
        // Registries are frozen by server start, so this is verification only.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            for (String namespace : namespaces) {
                int count = PatchOverlays.countItems(namespace);
                if (count == 0) {
                    LOGGER.warn("[fd-unified-patch] {}: no items found at server start", namespace);
                } else {
                    LOGGER.info("[fd-unified-patch] {}: {} items present at server start", namespace, count);
                }
            }
        });
        LOGGER.info("[fd-unified-patch] DelightLib module active for {}", namespaces);
    }

    /** Replaces vanilla tab registration only for an active whitelisted addon. */
    public static boolean onDelightCreativeTabRegistered(Identifier id, CreativeModeTab tab) {
        String namespace = id.getNamespace();
        for (DelightLibModule module : ACTIVE.values()) {
            if (module.namespaces.contains(namespace)) {
                PatchOverlays.registerTab(id, tab);
                LOGGER.info("[fd-unified-patch] {}: creative tab overlaid on addon build", namespace);
                return true;
            }
        }
        if (WARNED_NAMESPACES.add(namespace)) {
            LOGGER.info("[fd-unified-patch] ignoring unwhitelisted DelightLib namespace '{}' (fail closed; add a module to support it)", namespace);
        }
        return false;
    }

    /** Called by the DelightAddon mixin for every DelightLib item registration. */
    public static void onDelightItemRegistered(String namespace, Item item) {
        for (DelightLibModule module : ACTIVE.values()) {
            if (module.namespaces.contains(namespace)) {
                PatchOverlays.overlayItem(item);
                return;
            }
        }
        if (WARNED_NAMESPACES.add(namespace)) {
            LOGGER.info("[fd-unified-patch] ignoring unwhitelisted DelightLib namespace '{}' (fail closed; add a module to support it)", namespace);
        }
    }
}
