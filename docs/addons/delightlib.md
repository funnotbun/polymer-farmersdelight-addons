# DelightLib record (v0.1)

DelightLib is the registration library MoreDelight builds on. The patch
hooks it; it is not content itself.

- Source: https://github.com/axperty/delightlib
- Branch: `26.2-fabric` (checked-out `master` is NeoForge-only and NOT used)
- Pinned commit: `50a7936638b20c588a11f1f8d8dec1f96855e983` ("Version change.")
- Mod version: `26.06.23-26.2-fabric` (gradle.properties on that branch)
- Dependency coordinate (pinned, JitPack):
  `com.github.axperty:delightlib:50a7936638` (see gradle.properties;
  also the exact coordinate MoreDelight itself compiles against)
- Jar (test rig, local build of that commit): `delightlib-26.06.23-26.2-fabric.jar`
- Fabric dependencies (fabric.mod.json on that branch): `fabricloader >=0.19.3`,
  `minecraft ~26.2`, `java >=25`, `fabric-api *`, `farmersdelight *`
- Patch metadata: hard `depends` on `delightlib *` (the mixin targets
  `DelightAddon`, so it must be present for runtime correctness).

## Namespace evidence (why `moredelight:*` is the right whitelist)

DelightApi.create javadoc: "The mod ID you provide will be used as a
namespace for all registry entries created through this API."
`MoreDelight.java` calls `DelightAddon.create("moredelight")`, and
`DelightAddon.registerItem` (26.2-fabric branch) registers with
`Identifier.fromNamespaceAndPath(modId, name)` — full evidence in `~/mc-test/TEST.md`. Need to collapse into this repo at some point

Note: the `26.2-fabric` branch registers via vanilla `Registry.register`
(NeoForge `master` uses DeferredRegister). The namespace conclusion is
unaffected.

## Module coverage

- Mixin target: `DelightAddon.registerItem(String, Supplier)` @RETURN
  (event-driven overlay; entrypoint-order safe).
- DelightLib's cabinet chest menu is out of scope: MoreDelight registers
  no cabinets, so no menu/blocks/BE handling is implemented.
