package dev.fdunified.patch.mixin;

import dev.fdunified.patch.common.PatchOverlays;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Filters non-vanilla advancement entries out of the update packet for
 * players without a Polymer handshake (see
 * {@link PatchOverlays#filterAdvancementsForVanilla}). Always applied:
 * with no modded entries the filter is a no-op.
 */
@Mixin(PlayerAdvancements.class)
public class AdvancementSyncMixin {
    @Redirect(method = "flushDirty",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void fdUnified$sendFilteredAdvancements(ServerGamePacketListenerImpl connection, Packet<?> packet,
                                                    ServerPlayer player, boolean firstPacket) {
        connection.send(PatchOverlays.filterAdvancementsForVanilla((ClientboundUpdateAdvancementsPacket) packet, player));
    }
}
