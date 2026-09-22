# Publishing CatCraft Companion

## Release identity

- Mod: CatCraft Companion
- Current version: `1.1.0+26.2`
- Git tag: `v1.1.0`
- Minecraft: `26.2`
- Loaders: `Fabric`, `NeoForge`, and `Quilt` through the Fabric artifact
- Environment: client-side only
- Fabric required: Fabric API and Cloth Config
- Quilt required: the Fabric builds of Fabric API 0.160.0+26.2 and Cloth Config 26.2.155, manually placed in the profile's `mods` folder if Modrinth hides Fabric-tagged dependencies
- NeoForge required: NeoForge 26.2.0.88+ and Cloth Config
- Optional on Fabric/Quilt: Mod Menu

## Release process

1. Build against the real Minecraft 26.2 Fabric and NeoForge APIs using Java 25.
2. Complete static verification without treating it as a runtime pass.
3. Runtime-test each exact loader artifact in Minecraft.
4. Only an explicit in-game PASS establishes a loader artifact as a supported baseline.
5. Tag the exact tested source commit and attach clearly loader-suffixed production JARs.
6. Create separate Modrinth version records for Fabric/Quilt and NeoForge so launchers receive the correct artifact. The Quilt version description must include the manual dependency-installation note because the Modrinth App may hide Fabric-tagged dependency releases in Quilt profiles.
7. Never replace the passed Fabric JAR merely because the shared source was reorganized.

Do not publish a candidate that has not received an explicit runtime PASS. If a candidate fails, return to the last confirmed baseline rather than stacking changes on it.
