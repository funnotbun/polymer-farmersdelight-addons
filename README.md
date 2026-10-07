# Unified FD Polymer Patch (v0.4 for MC 26.3)

Unified Polymer patch jar for Farmer's Delight addons

Currently supports:

> [DelightLib](https://modrinth.com/mod/delight-lib) (whitelisted addons only)
>
> [MoreDelight](https://modrinth.com/mod/more-delight)
> 
> [Rustic Delight](https://modrinth.com/mod/rustic-delight)
> 
> [Farmer's Respite](https://github.com/funnotbun/farmersrespite-fabric-junkfork) (incl. kettle SGUI)
> 

## TODO

[ ] - add polydex-compatible recipe book button into farmer's respite (patch) kettle

[ ] - replace kettle progress bar with identical behavior to farmer's delight polymer patch (no tooltip, crossing 2 tiles)

[ ] - fix strings in farmer's delight (farmersdelight.container.cooking_pot.heated, not_heated)

[ ] - "Who Needs Cotton?" achievement (turn cotton into string) triggers when regularly picking up cotton 

[ ] - planted peppers are auto waterlogged

## Build (fresh clone, no sibling checkouts needed)

Requires Temurin JDK 25 (e.g. `$HOME/.local/jdks/jdk-25.0.4.1+1`):

```bash
export JAVA_HOME="$HOME/.local/jdks/jdk-25.0.4.1+1" PATH="$JAVA_HOME/bin:$PATH"
./gradlew --no-daemon build
```

Resulting output will be in `build/libs/fd-unified-patch-*.jar`.

## Agents

Never add git submodules here. Upstream sources are not vendored;
see `docs/addons/` for pins and coordinates.
