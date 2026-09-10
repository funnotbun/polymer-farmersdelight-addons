package dev.fdunified.patch.mixin;

import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Forces kettle recipes to sync display-less (same end state as the FD
 * patch, which nulls its own recipe displays at registration). The FR
 * display type is rsm-hidden from vanilla sync, so a synced recipe carrying
 * it disconnects vanilla clients with a {@code recipe_book_add} decode
 * error. Empty displays decode cleanly; brewing itself is unaffected.
 * String target avoids a compile dependency on Respite.
 */
@Mixin(targets = "com.chefsdelights.farmersrespite.common.crafting.KettleRecipe")
public class KettleRecipeDisplayMixin {
    @Inject(method = "display", at = @At("HEAD"), cancellable = true)
    private void fdUnified$emptyDisplay(CallbackInfoReturnable<List<RecipeDisplay>> cir) {
        cir.setReturnValue(List.of());
    }
}
