package dev.fdunified.patch.modules;

import java.util.Set;

/**
 * v0.1 proof of the engine: explicitly whitelists {@code moredelight:*}.
 * Copy this class plus docs/addons/moredelight.md (adjusted) to support a
 * future addon; never extend the whitelist in place for another mod.
 */
public final class MoreDelightModule extends DelightLibModule {
    public MoreDelightModule() {
        super(Set.of("moredelight"));
    }

    @Override
    public String modId() {
        return "moredelight";
    }

    @Override
    public String versionRange() {
        return "26.06.23-26.2-fabric";
    }
}
