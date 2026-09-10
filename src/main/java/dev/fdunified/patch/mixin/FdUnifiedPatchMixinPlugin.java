package dev.fdunified.patch.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Each mixin targets an optional content mod; apply it only when that mod is
 * present so profiles without it (e.g. md without Rustic, rd without
 * DelightLib) boot instead of failing on a missing target class.
 */
public class FdUnifiedPatchMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var loader = FabricLoader.getInstance();
        if (mixinClassName.endsWith("DelightAddonMixin")) {
            return loader.isModLoaded("delightlib");
        }
        if (mixinClassName.endsWith("RusticInitMixin")) {
            return loader.isModLoaded("rusticdelight");
        }
        if (mixinClassName.endsWith("RespiteInitMixin")
                || mixinClassName.endsWith("KettleMenuMixin")
                || mixinClassName.endsWith("KettleRecipeDisplayMixin")) {
            return loader.isModLoaded("farmersrespite");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
    }
}
