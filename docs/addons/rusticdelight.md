# RusticDelight addon record (v0.2)

- Source: https://github.com/PhantomWing/RusticDelight
- Branch: `fabric/26.2`
- Pinned commit: `9483734c138f832695164c20bf0329ee65bea6eb` ("Add sulfur cube support for giant bell pepper blocks")
- Mod version: `1.7.0` (gradle.properties `mod_version`; jar `rusticdelight-fabric-26.2-1.7.0.jar`)
- Jar (test rig, local build): `rusticdelight-fabric-26.2-1.7.0.jar`
- Build coordinates: Farmer's Delight Greenhouse
  `vectorwing:FarmersDelight:26.2-3.6.6+refabricated`; Cloth Config
  `me.shedaniel.cloth:cloth-config-fabric:26.2.155` (see build.gradle).
- Fabric dependencies (fabric.mod.json, expanded): `fabricloader >=0.19.3`,
  `minecraft >=26.2 <=26.2`, `java >=25`, `fabric-api >=0.152.2`,
  `farmersdelight >=26.2-3.6.6`, `cloth-config >=26.2.155`
- Patch metadata: `suggests: { rusticdelight: 1.7.0 }` (tested pin). No
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
- Creative tab `rusticdelight:item_group` via `registerTabById`.
- Assets: `addModAssets("rusticdelight")` + patch id, PLUS
  `bridgeBlockModels("rusticdelight")` — without bridging the whole
  `rusticdelight:block` model folder, overlaid blocks render as
  missing-texture checkers (the `items/-/block` display-model stubs are never
  written; same call the farmers-delight-patch makes for `farmersdelight:block`).
- Trigger: `RusticInitMixin` at `ModItemGroups.registerModItemGroups` RETURN
  (Rustic registers its tab last, so all content exists; string target, no
  compile dep on Rustic). Entrypoint only arms a fail-closed SERVER_STARTING
  guard that ERRORs if the trigger never fired.
- Tested version: `1.7.0`.
