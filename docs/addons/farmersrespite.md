# Farmer's Respite addon record (v0.3)

- Source: https://github.com/funnotbun/farmersrespite-fabric-junkfork
  (Fabric port of the Forge addon by Umpaz/HoboJoe/SoyTutta)
- Branch: `fabric/26.3`
- Pinned commit: `8d40216db39440eddb545961e200d563806236bf` ("ci: build on linux only")
- Mod version: `2.3.1` (gradle.properties `mod_version`; jar `farmersrespite-fabric-2.3.1.jar`)
- Jar (test rig, local build): `farmersrespite-fabric-2.3.1.jar`
- Build coordinates: Farmer's Delight Greenhouse
  `maven.modrinth:farmers-delight-refabricated:26.3-3.6.25`; Cloth Config
  `me.shedaniel.cloth:cloth-config-fabric:26.2.155`; JEI
  `maven.modrinth:jei:31.0.0.5` (compile-only integration; JEI itself
  stays a user-installed optional dep).
- Fabric dependencies: `fabricloader >=0.19.5`, `minecraft >=26.3 <=26.3`,
  `java >=25`, `fabric-api >=0.160.6`, `farmersdelight >=26.3-3.6.25`,
  `cloth-config >=26.2.155` (see fabric.mod.json after expansion).
- Patch metadata: `suggests: { farmersrespite: 2.3.1 }` (tested pin). No
  `breaks`.

## Registry inventory (at pinned commit)

- Blocks (33, all under `farmersrespite:*` via vanilla `Registry.register`):
  `kettle` (custom, block entity, waterloggable),
  tea bushes (3: `tea_bush`, `small_tea_bush`, `wild_tea_bush`),
  coffee plant parts (5: `coffee_bush`, `coffee_stem`, `coffee_bush_top`,
  `coffee_stem_double`, `coffee_stem_middle`),
  wither roots (2: `wither_roots`, `wither_roots_plant`),
  `coffee_cake` (custom cake), `rose_hip_pie` (FD `PieBlock`),
  candle coffee cakes (17: plain + 16 dyed),
  potted bushes (3, `FlowerPotBlock` — no items).
- Items (39): block items for kettle, wild tea bush, coffee cake,
  rose_hip_pie (4); seed/block-aliased items (`tea_seeds`,
  `coffee_beans`); leaves (3), berries/hips (2); bottled drinks
  (`DrinkableItem` + glass-bottle remainder); long/strong variants;
  cookie, sourdough, cod, curry, chili; cake/pie slices.
- Creative tab: `farmersrespite:group` ("Farmer's Respite", vanilla
  registration inside onInitialize).
- Effect (1): `caffeinated` (custom MobEffect; hidden from vanilla sync,
  same as FD effects).
- Sound (1): `block.kettle.whistle` (Polymer sound overlay).
- Block entity (1): `kettle` (registered for vanilla BE packets).
- Menu (1): `kettle` (`ExtendedMenuType` over BlockPos; replaced with
  `KettleUi` SGUI so vanilla clients never see the modded menu).
- Recipe plumbing: serializer `brewing` passes unmapped (FD precedent);
  menu `kettle` is marked server-only (`PolymerMenuUtils`, safe because the
  SGUI never sends it); book category `kettle_drinks` + display `brewing`
  are rsm-hidden (first boot proved all three remap-block). Kettle recipes
  still brew server-side; they show no dedicated book category client-side
  (same degradation class as FD's nulled recipe displays).
- Worldgen: wild tea/coffee configured/placed features + village crops —
  needs a FRESH world when first enabled (see rig `fr` profile).
- None new: potions, particles, enchantments, components.

## Module coverage

- `RespiteModule` whitelists `farmersrespite:*` only.
- Items: `overlayAllItems` sweep (vanilla `PolyItem`; `BlockItem`s incl.
  the `KettleBlockItem`/seed aliases get the interaction flag).
- Blocks: `overlayAllBlocks` — kettle to waterlogged BARRIER (new
  `BlockPresets.Waterlogged` counterpart, mirrors FD's waterloggable
  preset); pies/cakes (FD `PieBlock` + `*cake`/`*pie` paths) to CAMPFIRE;
  potted to BARRIER; bushes/stems/roots to PLANT.
- Effects/sounds/block entities: `overlayAllEffects` /
  `overlayAllSounds` / `overlayAllBlockEntities` sweeps (new reusable
  `common/` helpers).
- Creative tab `farmersrespite:group` diverted to Polymer by
  `VanillaRegistryTabMixin` before vanilla registration (FD-patch pattern).
- Assets: `addModAssets("farmersrespite")` + `bridgeBlockModels` + patch id.
- Kettle GUI: `KettleMenuMixin` (vanilla `ServerPlayer` target, FD's
  injection point) swaps the modded menu for `KettleUi` (SGUI generic-9x3:
  live input/meal/bottle/output slots, title from the MenuProvider, water
  level from the `water` blockstate property, heat from FD's
  `HeatableBlockEntity`, brew % from BE NBT `CookTime`/`CookTimeTotal`).
  Skin follows the FD patch system (`RespiteGui`: title-carried background
  font, generated icon/progress models, `addBridgedModelsFolder` for our
  `sgui` folder) over user-supplied art in `textures/sgui/` (`kettle.png`
  background, `arrow`/`water` progress strips sliced at pack build,
  `heated`/`empty`/`bottle` icons). No compile dependency on Respite
  anywhere: menu matched by class name, slots/title/heat/progress all read
  through vanilla/FD types.
- Trigger: `RespiteInitMixin` at `FarmersRespite.onInitialize` RETURN
  (tab registers last, so all content exists; string target, no dep).
  Entrypoint only arms a fail-closed SERVER_STARTING guard.
- Tested version: `2.3.1`.
