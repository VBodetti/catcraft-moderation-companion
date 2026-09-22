# Publishing CatCraft Companion

## Release identity

- Mod: CatCraft Companion
- Current version: `1.1.0+26.2`
- Git tag: `v1.1.0`
- Minecraft: `26.2`
- Loader: `Fabric`
- Environment: client-side only
- Required: Fabric API and Cloth Config
- Optional: Mod Menu

## Release process

1. Build against the real Minecraft 26.2 and Fabric APIs using Java 25.
2. Complete static verification without treating it as a runtime pass.
3. Runtime-test the exact candidate JAR in Minecraft.
4. Only an explicit in-game PASS establishes a new baseline.
5. Tag the exact tested source commit and attach the corresponding production JAR.
6. Upload that same JAR to Modrinth with matching metadata and release notes.

Do not publish a candidate that has not received an explicit runtime PASS. If a candidate fails, return to the last confirmed baseline rather than stacking changes on it.
