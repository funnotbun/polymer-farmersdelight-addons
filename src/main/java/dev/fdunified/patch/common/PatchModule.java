package dev.fdunified.patch.common;

/**
 * One module per supported addon mod.
 * v0.2 supports DelightLib-backed MoreDelight plus RusticDelight; extending
 * support means copying a module class and an addon record (see docs/addons/).
 */
public interface PatchModule {
    /** Addon mod id this module handles (e.g. "moredelight"). */
    String modId();

    /** Tested addon version range, informational (see suggests in fabric.mod.json). */
    String versionRange();

    /** Register overlays/assets. Called once from the entrypoint when gated in. */
    void apply();
}
