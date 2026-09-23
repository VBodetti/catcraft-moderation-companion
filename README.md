# CatCraft Companion

CatCraft Companion (CCC) is a client-side Fabric and NeoForge mod for CatCraft, authored by **B0_Deadly**. It gives players a searchable command companion and gives staff capability-filtered investigation and moderation tools without changing server behavior. The Fabric artifact is also distributed for Quilt after compatibility testing; there is no separate Quilt JAR.

## Version

**1.1.1** for Minecraft Java **26.2**.

## Loader support

- **Fabric:** Fabric Loader 0.19.5+, Fabric API 0.160.0+26.2, and Cloth Config 26.2.155+
- **NeoForge:** NeoForge 26.2.0.88+ and Cloth Config 26.2.155+
- **Quilt:** the Fabric artifact with a compatible Quilt Loader, plus the **Fabric** builds of Fabric API 0.160.0+26.2 and Cloth Config 26.2.155

The Modrinth App may hide Fabric API and Cloth Config when adding content to a Quilt profile because those dependency releases are tagged Fabric. Quilt users must download the two Fabric dependency JARs and place them directly in the profile's `mods` folder. This exact setup has passed in-game testing on Minecraft 26.2.

All distributions require Minecraft Java 26.2 and Java 25. Mod Menu is optional on Fabric and Quilt; NeoForge exposes the settings screen through its native mod list.

## Features

- F8 opens the global CatCraft Companion; `/ccc menu` opens it from chat
- Focused live search from the top of the F8 menu for commands and actions
- Task-oriented navigation for travel, homes, players, claims, pets, trading, clans, perks, and staff tools
- Live completion-backed Homes and Kits selectors plus a compact CatCraft Warps selector
- Search for online or offline players and open their player profile
- Player profiles with skin, username, formatted nickname, online/last-seen state, playtime, and joined date when CatCraft exposes those values
- Player actions directly on the profile, including Message, Mail, TPA, TPA Here, Trade, Itembox Held Item, Ignore, GivePet, and Meow
- Player and Staff command tabs on the same profile; staff can view paged history, CoreProtect, anti-cheat, and trade query results beside their actions
- Separate Player Rank and Staff Role settings with capability-based filtering
- Cat is the lowest player rank; fresh installations default to Lion player rank and no staff role
- Correct Moderator commands for Unmute (`/lunmute`), Jail (`/togglejail PLAYER 1`), and Unjail (`/unjail PLAYER`)
- Discord moderation-report capture for fixed punishment actions
- Optional timestamps, timestamp-only copy options, retained chat history, and conflict-aware chat integration

## Configuration

Use `/ccc settings`, Mod Menu on Fabric/Quilt, or NeoForge's mod-list configuration button. Existing `catcraft_moderation_companion.json` settings are retained automatically across loaders; the internal mod ID also remains unchanged for upgrade compatibility.

Chat Integration Mode supports:

- `auto` — yield overlapping chat processing when a supported external chat processor is detected
- `ccc` — use CatCraft Companion timestamps and history (`ccmc` remains accepted for compatibility)
- `external` — disable CatCraft Companion chat processing only

## Build

This project targets Java 25 and Minecraft 26.2. Shared code lives under `common/`; the loader adapters live under `fabric/` and `neoforge/`. Build both distributions with Gradle 9.7.1 or a compatible Gradle 9 release:

```bash
gradle build
```

The loader-specific JARs are generated under `fabric/build/libs/` and `neoforge/build/libs/`.

## CatCraft

- Website: https://www.catcraft.net/
- Wiki: https://wiki.catcraft.net/?ref=catcraft.net

## License

MIT License. See [LICENSE](LICENSE).
