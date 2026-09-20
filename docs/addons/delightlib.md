# DelightLib record (v0.1)

DelightLib is the registration library MoreDelight builds on. The patch
hooks it; it is not content itself.

- Source: https://github.com/axperty/delightlib
- Branch: `26.3-fabric` (checked-out `master` is NeoForge-only and NOT used)
- Pinned commit: `f0017bf3f4d1b541fb20e3cf3399c1a87c7061e2` ("A few 26.3 changes. Cabinets might be a bit broken.")
- Mod version: `26.09.16-26.3-fabric` (gradle.properties on that branch)
- Dependency coordinate (pinned, JitPack):
  `com.github.axperty:delightlib:f0017bf3f4` (see gradle.properties;
  the exact coordinate MoreDelight itself compiles against. Newer tip
  `a86481a89c` ("Port generated loot tables to 26.3 format") has no JitPack
  build yet, so it does not resolve — re-pin when it does)
- Jar (test rig, local build of that commit): `delightlib-26.09.16-26.3-fabric.jar`
- Fabric dependencies (fabric.mod.json on that branch): `fabricloader >=0.19.2`,
  `minecraft ~26.3`, `java >=25`, `fabric-api *`, `farmersdelight *`
- Patch metadata: hard `depends` on `delightlib *` (the mixin targets
  `DelightAddon`, so it must be present for runtime correctness).

## Namespace evidence (why `moredelight:*` is the right whitelist)

DelightApi.create javadoc: "The mod ID you provide will be used as a
namespace for all registry entries created through this API."
`MoreDelight.java` calls `DelightAddon.create("moredelight")`, and
`DelightAddon.registerItem` (26.3-fabric branch) registers with
`Identifier.fromNamespaceAndPath(modId, name)` — full evidence in `~/mc-test/TEST.md`. Need to collapse into this repo at some point

Note: the `26.3-fabric` branch registers via vanilla `Registry.register`
(NeoForge `master` uses DeferredRegister). The namespace conclusion is
unaffected.

## Module coverage

- Mixin target: `DelightAddon.registerItem(String, Supplier)` @RETURN
  (event-driven overlay; entrypoint-order safe).
- DelightLib's cabinet chest menu is out of scope: MoreDelight registers
  no cabinets, so no menu/blocks/BE handling is implemented.
