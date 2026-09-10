package dev.fdunified.patch.mixin;

import com.axperty.delightlib.api.DelightAddon;
import dev.fdunified.patch.modules.DelightLibModule;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

/**
 * Event-driven hook into DelightLib registration (same pattern as the
 * farmers-delight-patch RegUtilsMixin): overlays each item at registration
 * time so entrypoint order between the patch and content mods does not
 * matter. The build redirect replaces whitelisted vanilla tab registration
 * with Polymer registration, matching the Farmer's Delight patch pattern.
 * Whitelist enforcement lives in DelightLibModule.
 */
@Mixin(DelightAddon.class)
public class DelightAddonMixin {
    @Inject(method = "registerItem", at = @At("RETURN"))
    private void fdUnified$onRegisterItem(String name, Supplier<Item> supplier, CallbackInfoReturnable<Supplier<Item>> cir) {
        DelightLibModule.onDelightItemRegistered(((DelightAddon) (Object) this).getModId(), cir.getReturnValue().get());
    }

    @Redirect(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private Object fdUnified$registerCreativeTab(Registry<Object> registry, Identifier id, Object value) {
        var tab = (CreativeModeTab) value;
        return DelightLibModule.onDelightCreativeTabRegistered(id, tab) ? tab : Registry.register(registry, id, value);
    }
}
