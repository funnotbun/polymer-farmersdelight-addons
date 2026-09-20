package dev.fdunified.patch.mixin;

import dev.fdunified.patch.common.PatchOverlays;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Diverts {@code farmersrespite:group} into Polymer before vanilla
 * registration (same pattern as DelightAddonMixin / the FD regTab mixin).
 * Respite registers its tab via the {@code ResourceKey} overload of
 * {@code Registry.register} at the top of {@code onInitialize}. The handler
 * keeps all logic in {@link PatchOverlays#divertTabToPolymer} and touches no
 * mixin-class state (Mixin would remap that onto the target class). The
 * mixin plugin skips this entirely when Respite is absent.
 */
@Mixin(targets = "com.chefsdelights.farmersrespite.core.FarmersRespite")
public class RespiteTabMixin {
    @Redirect(method = "onInitialize",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object fdUnified$divertTab(Registry registry, ResourceKey key, Object value) {
        if (value instanceof CreativeModeTab tab && PatchOverlays.divertTabToPolymer(key.identifier(), tab)) {
            return tab;
        }
        return Registry.register(registry, key, value);
    }
}
