package dev.fdunified.patch.mixin;

import dev.fdunified.patch.modules.RusticModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Order-safe trigger for {@link RusticModule}: Rustic registers its item group
 * last in onInitialize, so at this RETURN every rusticdelight block, item,
 * potion, and the creative tab exists. String target avoids a compile
 * dependency on Rustic; the mixin plugin skips this entirely when Rustic is
 * absent (fail closed).
 */
@Mixin(targets = "com.phantomwing.rusticdelight.itemGroup.ModItemGroups")
public class RusticInitMixin {
    @Inject(method = "registerModItemGroups", at = @At("RETURN"))
    private static void fdUnified$onRusticReady(CallbackInfo ci) {
        RusticModule.tryApply();
    }
}
