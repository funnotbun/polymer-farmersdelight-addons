# FD Unified Polymer Patch (v0.3)

Unified Polymer patch jar for Farmer's Delight addons

Currently supports:

> DelightLib + MoreDelight
> Rustic Delight
> Farmer's Respite (incl. kettle SGUI)

## Build (fresh clone, no sibling checkouts needed)

Requires Temurin JDK 25 (e.g. `$HOME/.local/jdks/jdk-25.0.4.1+1`):

```bash
export JAVA_HOME="$HOME/.local/jdks/jdk-25.0.4.1+1" PATH="$JAVA_HOME/bin:$PATH"
./gradlew --no-daemon build
```

All dependencies resolve from Maven coordinates pinned in
`gradle.properties` (Modrinth, Nucleoid, JitPack). Output:
`build/libs/fd-unified-patch-0.3.0+26.2.jar`.

## Agents

Never add git submodules here. Upstream sources are not vendored;
see `docs/addons/` for pins and coordinates.
