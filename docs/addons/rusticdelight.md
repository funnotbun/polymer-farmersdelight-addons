# RusticDelight addon record (v0.2)

- Source: https://github.com/PhantomWing/RusticDelight
- Branch: `fabric/26.3`
- Pinned commit: `548fced77eaa3308738c127acedab83b440364a5` ("Release plumbing for 1.7.1-jf")
- Mod version: `1.7.1-jf` (gradle.properties `mod_version`; jar `rusticdelight-fabric-26.3-1.7.1-jf.jar`)
- Jar (test rig, local build): `rusticdelight-fabric-26.3-1.7.1-jf.jar`
- Build coordinates: Farmer's Delight Greenhouse
  `maven.modrinth:farmers-delight-refabricated:26.3-3.6.25`; Cloth Config
  `me.shedaniel.cloth:cloth-config-fabric:26.2.155` (see build.gradle).
- Fabric dependencies (fabric.mod.json, expanded): `fabricloader >=0.19.5`,
  `minecraft >=26.3 <=26.3`, `java >=25`, `fabric-api >=0.160.6`,
  `farmersdelight >=26.3-3.6.25`, `cloth-config >=26.2.155`
- Patch metadata: `suggests: { rusticdelight: 1.7.1-jf }` (tested pin). No
  `breaks`. `delightlib` moved from hard `depends` to `suggests`: the mixin
  plugin gates each mixin on its target mod, so profiles without DelightLib
  (or without Rustic) boot instead of failing on a missing target class.

## Registry inventory (at pinned commit; 55 + 154 + 3 = 212 = the exact pre-port rejection count)

- Blocks (55, all under `rusticdelight:*` via vanilla `Registry.register`):
  seed bags (6): `cotton_seeds_bag`, `bell_pepper_seeds_bag`,
  `pale_bell_pepper_seeds_bag`, `dark_bell_pepper_seeds_bag`,
  `coffee_beans_bag`, `roasted_coffee_beans_bag`
  crates (11): `cotton_boll_crate`, `bell_pepper_{green,yellow,red,orange,white,pink,blue,purple,black}_crate`,
  `calamari_crate`
  melon-like pepper blocks (9): `bell_pepper_{green,yellow,red,orange,white,pink,blue,purple,black}_block`
  pies (3, FD `PieBlock`): `syrup_cheesecake`, `cherry_blossom_cheesecake`, `coffee_cheesecake`
  pancakes (7, Rustic `PancakeBlock`): `pancakes`, `honey_pancakes`, `chocolate_pancakes`,
  `cherry_blossom_pancakes`, `vegetable_pancakes`, `pumpkin_pancakes`, `coffee_pancakes`
  feasts (4, FD `FeastBlock`): `rice_roll_royale`, `bell_pepper_medley`,
  `pale_bell_pepper_medley`, `dark_bell_pepper_medley`
  wild crops (5, FD `WildCropBlock`): `wild_cotton`, `wild_bell_peppers`,
  `wild_pale_bell_peppers`, `wild_dark_bell_peppers`, `wild_coffee`
  crops, unobtainable as items (5, vanilla `CropBlock`): `cotton`, `bell_peppers`,
  `pale_bell_peppers`, `dark_bell_peppers`, `coffee` (placed via seed items)
  potted wild crops, no item at all (5, `FlowerPotBlock`): `potted_wild_cotton`,
  `potted_wild_bell_peppers`, `potted_wild_pale_bell_peppers`,
  `potted_wild_dark_bell_peppers`, `potted_wild_coffee`
- Items (154): 45 block items (every block above except the 5 crops and 5
  potted), 5 seed items (`cotton_seeds`, `bell_pepper_seeds`,
  `pale_bell_pepper_seeds`, `dark_bell_pepper_seeds`, `coffee_beans`), and 104
  standalone items: 9 raw + 9 roasted bell peppers, 9 + 9 pepper slices
  (raw/roasted), 9 stuffed peppers, 9 pepper rolls, `cotton_boll`, `calamari`,
  `cooked_calamari` (+ slices), `roasted_coffee_beans`, `golden_coffee_beans`,
  8 bottled coffees, `cooking_oil`, `syrup`, `batter`, `potato_slices`,
  `baked_potato_slices`, 3 cheesecakes + 3 slices, 3 cookies, `syrup_sandwich`,
  `fruit_beignet`, 7 pancake servings, `potato_salad`, `sweet_salad`,
  `fried_dough`, `fried_dumplings`, `spring_rolls`, `fried_fish`,
  `calamari_roll`, `cherry_blossom_roll`, `bell_pepper_soup`, `calamari_soup`,
  `bell_pepper_pasta`, `fried_calamari`, `fried_chicken`, `fried_mushrooms`,
  `coffee_braised_beef`. No knives; no custom consume-effect types (all foods
  use vanilla `FoodProperties`/`Consumable` with vanilla effects and sounds).
- Creative tab: `rusticdelight:item_group` ("Rustic Delight").
- Potions (3, vanilla Haste effects only): `haste`, `long_haste`,
  `strong_haste` (+ brewing recipes off golden coffee beans, config-gated).
- Worldgen: wild-crop configured/placed features, village crops, chest/villager
  loot — needs a FRESH world when first enabled (see rig `rd` profile).
- None new: block entities, menus/screens, sounds, particles, effects,
  components, enchantments. Server-only mechanics (villager food points,
  pancake UseBlockCallback, compost/fuel/loot helpers) need no sync.

## Module coverage

- `RusticModule` whitelists `rusticdelight:*` only.
- Items: `overlayAllItems` sweep (vanilla `PolyItem`; `BlockItem`s incl.
  `PlaceableItem`/seed items get the interaction flag automatically).
- Blocks: `overlayAllBlocks` with the FD-patch preset table — pies/feasts +
  `*pancakes` to CAMPFIRE, wild crops to PLANT, crops to KELP, everything else
  (bags, crates, pepper blocks, pots) to BARRIER.
- Potions: `overlayAllPotions` hides the 3 entries from vanilla sync (null
  replacement, same as FD effects); held/brewed stacks degrade to empty
  contents client-side, server brewing unchanged.
- Creative tab `rusticdelight:item_group` diverted to Polymer by
  `VanillaRegistryTabMixin` before vanilla registration (FD-patch pattern;
  post-hoc lookup cannot work, Polymer refuses vanilla-registered ids).
- Assets: `addModAssets("rusticdelight")` + patch id, PLUS
  `bridgeBlockModels("rusticdelight")` — without bridging the whole
  `rusticdelight:block` model folder, overlaid blocks render as
  missing-texture checkers (the `items/-/block` display-model stubs are never
  written; same call the farmers-delight-patch makes for `farmersdelight:block`).
- Trigger: `RusticInitMixin` at `ModItemGroups.registerModItemGroups` RETURN
  (Rustic registers its tab last, so all content exists; string target, no
  compile dep on Rustic). Entrypoint only arms a fail-closed SERVER_STARTING
  guard that ERRORs if the trigger never fired.
- Advancements: non-vanilla entries are dropped from
  `ClientboundUpdateAdvancementsPacket` for players without a Polymer
  handshake (`AdvancementSyncMixin` + `PatchOverlays`, applies to every
  modded namespace, not just rustic) — modded display icons encode with
  tags vanilla clients lack and kick with a DecoderException on first
  grant (any pickup grants main/root). Server grants, recipe unlocks, and
  progress are unaffected; modded tabs stay hidden client-side.
- Tested version: `1.7.1-jf`.
