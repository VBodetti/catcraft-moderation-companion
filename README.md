# CatCraft Companion

CatCraft Companion (CCC) is a client-side Fabric mod for CatCraft, authored by **B0_Deadly**. It gives players a searchable command companion and gives staff capability-filtered investigation and moderation tools without changing server behavior.

## Version

**1.1.0** for Minecraft Java **26.2**.

## Requirements

- Minecraft Java 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API compatible with 26.2
- Java 25
- Cloth Config 26.2.155 or newer
- Mod Menu is optional

## Features

- F8 opens the global CatCraft Companion; `/ccc menu` opens it from chat
- Focused live search from the top of the F8 menu for commands and actions
- Task-oriented navigation for travel, homes, players, claims, pets, trading, clans, perks, and staff tools
- Live completion-backed Homes and Kits selectors plus a compact CatCraft Warps selector
- Search for online or offline players and open their player profile
- Player profiles with skin, username, formatted nickname, online/last-seen state, playtime, and joined date when CatCraft exposes those values
- Player actions including TPA, TPA Here, Trade, Message, Mail, Ignore, GivePet, Meow, and Purr
- Staff profile tabs for history, CoreProtect, anti-cheat, trade logs, investigation, and moderation
- Separate Player Rank and Staff Role settings with capability-based filtering
- Correct Moderator commands for Unmute (`/lunmute`), Jail (`/togglejail PLAYER 1`), and Unjail (`/unjail PLAYER`)
- Discord moderation-report capture for fixed punishment actions
- Optional timestamps, timestamp-only copy options, retained chat history, and conflict-aware chat integration

## Configuration

Use `/ccc settings` or Mod Menu. Existing `catcraft_moderation_companion.json` settings are retained automatically; the internal mod ID also remains unchanged for upgrade compatibility.

Chat Integration Mode supports:

- `auto` — yield overlapping chat processing when a supported external chat processor is detected
- `ccc` — use CatCraft Companion timestamps and history (`ccmc` remains accepted for compatibility)
- `external` — disable CatCraft Companion chat processing only

## Build

This project targets Java 25 and Minecraft 26.2. Build locally with Gradle 9.7.1 or a compatible Gradle 9 release:

```bash
gradle build
```

The production JAR is generated under `build/libs/`.

## CatCraft

- Website: https://www.catcraft.net/
- Wiki: https://wiki.catcraft.net/?ref=catcraft.net

## License

MIT License. See [LICENSE](LICENSE).
