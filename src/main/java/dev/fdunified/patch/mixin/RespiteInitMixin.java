package dev.fdunified.patch.mixin;

import dev.fdunified.patch.modules.RespiteModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Order-safe trigger for {@link RespiteModule}: Respite registers its
 * creative tab inside onInitialize after all content, so at this RETURN
 * every farmersrespite block, item, effect, sound, block entity, menu, and
 * the tab exists. String target avoids a compile dependency on Respite; the
 * mixin plugin skips this entirely when Respite is absent (fail closed).
 */
@Mixin(targets = "com.chefsdelights.farmersrespite.core.FarmersRespite")
public class RespiteInitMixin {
    @Inject(method = "onInitialize", at = @At("RETURN"))
    private void fdUnified$onRespiteReady(CallbackInfo ci) {
        RespiteModule.tryApply();
    }
}
