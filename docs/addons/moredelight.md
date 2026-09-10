# MoreDelight addon record (v0.1)

- Source: https://github.com/axperty/moredelight
- Branch: `26.2-fabric`
- Pinned commit: `3e2db335dedc3a2019659d596aceae7d629d9615` ("See changelog.md for the latest changes.")
- Mod version: `26.06.23-26.2-fabric` (gradle.properties)
- Jar (test rig, local build): `moredelight-26.06.23-26.2-fabric.jar`
- Build coordinates: Farmer's Delight
  `curse.maven:farmers-delight-refabricated-993166:8016410`; DelightLib
  `com.github.axperty:delightlib:50a7936638`.
- Fabric dependencies (src/main/resources/fabric.mod.json): `fabricloader >=0.19.3`,
  `minecraft ~26.2`, `java >=25`, `fabric-api *`, `farmersdelight *`, `delightlib *`
- Patch metadata: `suggests: { moredelight: 26.06.23-26.2-fabric }` (tested pin).
  No `breaks`: no incompatibility known; do not invent one.

## Registry inventory (derived from MoreDelight.java @ pinned commit)

- Items (33, all under `moredelight:*` via DelightLib builders):
  knives (2): `wooden_knife`, `stone_knife`
  foods (31): `diced_potatoes`, `chocolate_popsicle`, `omelette`,
  `cooked_rice_with_chicken_cuts`, `cooked_rice_with_beef`,
  `cooked_rice_with_porkchop`, `creamy_pasta_with_ham`,
  `creamy_pasta_with_chicken_cuts`, `mashed_potatoes`,
  `diced_potatoes_with_chicken_cuts`, `diced_potatoes_with_beef`,
  `diced_potatoes_with_porkchop`, `diced_potatoes_with_egg_and_tomato`,
  `potato_salad`, `chicken_salad`, `carrot_soup`, `simple_hamburger`,
  `hamburger_with_egg`, `loaded_hamburger`,
  `chicken_sandwich_with_egg_and_tomato`, `steak_sandwich`,
  `porkchop_sandwich`, `egg_with_bacon_sandwich`, `tomato_sandwich`,
  `bread_slice`, `toast`, `toast_with_egg`, `toast_with_honey`,
  `toast_with_sweet_berries`, `toast_with_glow_berries`, `toast_with_chocolate`
  (3 commented-out foods — hamburger_with_cheese, toast_with_cheese,
  toast_with_peanut_butter — are NOT registered on Fabric.)
- Blocks: none. Block entities: none. Menus/GUIs: none.
- Recipes (29): 12 cooking-pot (`cooking/*`), 3 shaped, 14 shapeless.
- Creative tab: `moredelight:tab` ("More Delight").
- Sounds / particles / effects / components: none new. Foods reuse FD
  `NOURISHMENT` and vanilla `REGENERATION` (already Polymer-synced by the
  farmers-delight-patch).
- Knife tags (generated): both knives in `farmersdelight:tools/knives` and
  `c:tools/knife`, so the FD cutting board accepts them server-side.

## Module coverage

- `MoreDelightModule` whitelists `moredelight:*` only.
- Items: overlay at DelightLib registration time (DelightAddonMixin) +
  `overlayAllItems` sweep at init (registration hook handles later items);
  knives (FD `KnifeItem`)
  get the interaction flag.
- Creative tab `moredelight:tab` is redirected from vanilla registration to
  Polymer registration while the addon finishes `build()`.
- Assets: `addModAssets("moredelight")` + patch id.
- Blocks / block entities / sounds / effects helpers exist in common for
  future modules but are unused here (MoreDelight has none).
- Tested version: `26.06.23-26.2-fabric`.

## Extending support

Copy `MoreDelightModule` and this record for the new addon, adjusted
explicitly (new namespace, pins, inventory).
