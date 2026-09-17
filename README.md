# CatCraft Moderation Companion

CatCraft Moderation Companion (CCMC) is a client-side Fabric utility for CatCraft staff, authored by **B0_Deadly**.

## Version

**1.0.0** for Minecraft Java **26.2**.

## Requirements

- Minecraft Java 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API compatible with 26.2
- Java 25
- Cloth Config 26.2.155 or newer
- Mod Menu is optional

## Core features

- Player-name moderation popup from chat
- Direct Message and Mail actions with editable command prefills
- Moderator teleport, punishment, investigation, inventory, anti-cheat, and CoreProtect actions
- Global Staff Menu via `/ccmc menu` and a rebindable F8 key
- Rank-aware command visibility so Senior Moderator/Admin-only actions are not exposed to lower ranks
- Optional timestamps, two-action message copy menu, and configurable retained chat history
- Conflict-aware chat integration: in `auto` mode CCMC yields overlapping chat processing when a supported external chat processor is installed while moderation features remain active

## Chat Integration Mode

- `auto` — CCMC automatically yields overlapping timestamp/history processing when a supported external chat processor is detected
- `ccmc` — CCMC handles its own chat enhancements
- `external` — CCMC chat processing is disabled explicitly

This setting affects only overlapping chat enhancements. Moderation and staff tools remain active in every mode.

## Build

This project targets Java 25 and Minecraft 26.2. The GitHub Actions workflow builds with a pinned Gradle version and Java 25.

For a local build with Gradle 9.5.1 installed:

```bash
gradle build
```

The production JAR is generated under `build/libs/`.

## CatCraft

- Website: https://www.catcraft.net/
- Wiki: https://wiki.catcraft.net/?ref=catcraft.net

## License

MIT License. See [LICENSE](LICENSE).
