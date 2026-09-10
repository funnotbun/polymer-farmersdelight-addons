package dev.fdunified.patch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.fdunified.patch.modules.KettleUi;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

/**
 * Replaces the kettle screen with {@link KettleUi} (SGUI) so vanilla clients
 * never see the modded menu (same role as the FD patch's cooking-pot hook).
 * Matched by class name so no compile dependency on Respite is needed; the
 * provider is the BE itself, giving pos/level/title through vanilla types.
 */
@Mixin(ServerPlayer.class)
public class KettleMenuMixin {
    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void fdUnified$openKettleUi(MenuProvider factory, CallbackInfoReturnable<OptionalInt> cir,
                                        @Local AbstractContainerMenu menu) {
        if (menu.getClass().getName().equals("com.chefsdelights.farmersrespite.common.block.entity.container.KettleContainer")
                && factory instanceof BlockEntity be) {
            Component title;
            try {
                title = factory.getDisplayName();
            } catch (RuntimeException e) {
                title = Component.literal("Kettle");
            }
            new KettleUi((ServerPlayer) (Object) this, menu, be, title);
            cir.setReturnValue(OptionalInt.empty());
        }
    }
}
