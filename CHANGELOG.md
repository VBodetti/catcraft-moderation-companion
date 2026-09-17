# Changelog

## 1.1.0

- Added Discord moderation report capture for fixed CCMC punishment actions
- Automatically records player, offense, punishment, and evidence timestamp
- Captures and preserves a raw evidence screenshot after the moderation popup closes
- Builds a Discord-ready report PNG with the moderation details above the evidence screenshot
- Copies the report image to the system clipboard for direct paste into Discord
- Falls back to a text report if image clipboard access is unavailable
- Stores report assets under `screenshots/ccmc-reports`

## 1.0.0

Initial public release of CatCraft Moderation Companion.

- CatCraft staff moderation popup and Staff Menu
- Moderator-level defaults with rank-aware Senior Moderator/Admin actions
- Editable Message, Mail, and variable-command prefills
- Correct CatCraft Jail, Unjail, and Unmute command routing
- Investigation groups for Player Info, Inventories, Anti-Cheat, and CoreProtect
- Optional timestamps and two-action copy menu
- Configurable retained chat lines
- Automatic conflict avoidance with supported external chat-processing mods
- Java 25 / Minecraft 26.2 / Fabric client-side implementation
