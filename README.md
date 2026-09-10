# Unified FD Polymer Patch (v0.3 for MC 26.2)

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

[ ] - fix putting a candle atop farmer's respite cakes and lighting it (candle placing works)

[ ] - fix JEI/REI plugin for farmer's respite

[ ] - add polydex-compatible recipe book into farmer's respite kettle

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
