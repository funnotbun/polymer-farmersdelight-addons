package dev.fdunified.patch.common;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.other.PolymerMenuUtils;
import eu.pb4.polymer.core.api.other.PolymerPotion;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.networking.impl.ExtConnection;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import vectorwing.farmersdelight.common.item.KnifeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

/**
 * Small static helpers over the farmers-delight-patch patterns.
 * v0.1 (MoreDelight, items-only) used overlayItem/overlayAllItems/registerTab;
 * block-adding modules additionally use overlayAllBlocks with a preset from
 * {@link BlockPresets}, overlayAllPotions for vanilla-effect potions, and
 * bridgeBlockModels (without it, overlaid blocks are missing-texture checkers).
 */
public final class PatchOverlays {
    private static final Logger LOGGER = LoggerFactory.getLogger("fd-unified-patch");

    private PatchOverlays() {
    }

    /**
     * Creative tabs diverted to Polymer before vanilla registration (FD-patch
     * pattern). Post-hoc lookup cannot work: Polymer refuses ids already in
     * the vanilla registry. A new addon is one line here plus its redirect.
     */
    public static final Set<String> POLYMER_TAB_IDS = Set.of(
            "moredelight:tab",
            "rusticdelight:item_group",
            "farmersrespite:group");

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

    /**
     * Divert a whitelisted tab into Polymer, skipping vanilla registration.
     * Called from redirect handlers, which must not touch mixin-class state
     * (Mixin remaps such accesses onto the target class). Returns true when
     * the caller should use {@code tab} as the registration result.
     */
    public static boolean divertTabToPolymer(Identifier id, CreativeModeTab tab) {
        if (tab != null && POLYMER_TAB_IDS.contains(id.toString())) {
            PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(id, tab);
            LOGGER.info("[fd-unified-patch] {}: creative tab diverted to Polymer", id);
            return true;
        }
        return false;
    }

    public static void registerBlockEntity(BlockEntityType<?> type) {
        PolymerBlockUtils.registerBlockEntity(type);
    }

    /** Register every block-entity type in a WHITELISTED namespace. */
    public static int overlayAllBlockEntities(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                registerBlockEntity(entry.getValue());
                count++;
            }
        }
        return count;
    }

    public static void registerSound(SoundEvent sound) {
        PolymerSoundEvent.registerOverlay(sound);
    }

    /** Register every sound event in a WHITELISTED namespace. */
    public static int overlayAllSounds(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.SOUND_EVENT.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                registerSound(entry.getValue());
                count++;
            }
        }
        return count;
    }

    public static void registerEffect(MobEffect effect) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.MOB_EFFECT, effect, (server, context) -> null);
    }

    /** Hide every mob effect in a WHITELISTED namespace from vanilla sync. */
    public static int overlayAllEffects(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.MOB_EFFECT.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                registerEffect(entry.getValue());
                count++;
            }
        }
        return count;
    }

    /** Mark every menu type in a WHITELISTED namespace as server-only. */
    public static int overlayAllMenus(String namespace) {
        int count = 0;
        for (var entry : BuiltInRegistries.MENU.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(namespace)) {
                PolymerMenuUtils.registerType(entry.getValue());
                count++;
            }
        }
        return count;
    }

    /**
     * Hide one registry entry from vanilla sync (last resort for entries
     * with no Polymer overlay API, e.g. recipe book categories/displays).
     * The server keeps full behavior; vanilla clients simply never see it.
     */
    public static <T> void hideFromSync(net.minecraft.core.Registry<T> registry, Identifier id) {
        RegistrySyncUtils.setServerEntry(registry, id);
    }

    /**
     * Drops every non-vanilla entry from an advancement update for players
     * without a Polymer handshake. Modded display icons encode as polymer
     * stacks carrying tags a vanilla client lacks, which kicks with a
     * DecoderException on grant. Server-side grants, recipe unlocks, and
     * progress are unaffected; modded tabs simply stay hidden client-side
     * (same degradation class as the hidden kettle book category).
     */
    public static ClientboundUpdateAdvancementsPacket filterAdvancementsForVanilla(
            ClientboundUpdateAdvancementsPacket packet, ServerPlayer player) {
        // NOTE: getSupportedVersion() is NOT a vanilla check (it reports the
        // server's own versions when no handshake happened); hasPolymer() is
        // only true after a real client hello. Both are internal Polymer API,
        // safe under the pinned version.
        boolean polymer = ExtConnection.of(player.connection).polymerNet$hasPolymer();
        // TEMP-DEBUG: name every advancement packet until the kick source is found.
        LOGGER.info("[fd-unified-patch] adv-debug player={} polymer={} reset={} added={} progress={} removed={}",
                player.getScoreboardName(), polymer, packet.shouldReset(),
                packet.added().stream().map(p -> p.advancement().id().toString()).toList(),
                packet.progress().keySet().stream().map(Object::toString).toList(),
                packet.removed().stream().map(Object::toString).toList());
        if (polymer) {
            return packet;
        }
        var added = packet.added().stream()
                .filter(p -> p.advancement().id().getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
                .toList();
        var progress = new HashMap<>(packet.progress());
        progress.keySet().removeIf(id -> !id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE));
        if (added.size() == packet.added().size() && progress.size() == packet.progress().size()) {
            return packet;
        }
        LOGGER.info("[fd-unified-patch] hid {} advancement(s) from vanilla client {}",
                packet.added().size() - added.size(), player.getScoreboardName());
        return new ClientboundUpdateAdvancementsPacket(
                packet.shouldReset(), added, packet.removed(), progress, packet.showAdvancements());
    }
}
