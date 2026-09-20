package dev.fdunified.patch.mixin;

import dev.fdunified.patch.common.PatchOverlays;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Diverts {@code rusticdelight:item_group} into Polymer before vanilla
 * registration (same pattern as DelightAddonMixin / the FD regTab mixin).
 * The tab is registered in a static field initializer, so the redirect
 * targets {@code <clinit>}; the existing init mixin trigger is too late for
 * tabs (vanilla registration has already happened by then). The handler
 * keeps all logic in {@link PatchOverlays#divertTabToPolymer} and touches no
 * mixin-class state (Mixin would remap that onto the target class). The
 * mixin plugin skips this entirely when Rustic is absent.
 */
@Mixin(targets = "com.phantomwing.rusticdelight.itemGroup.ModItemGroups")
public class RusticTabMixin {
    @Redirect(method = "<clinit>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object fdUnified$divertTab(Registry<Object> registry, Identifier id, Object value) {
        if (value instanceof CreativeModeTab tab && PatchOverlays.divertTabToPolymer(id, tab)) {
            return tab;
        }
        return Registry.register(registry, id, value);
    }
}
