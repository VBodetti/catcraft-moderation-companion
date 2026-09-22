# Changelog

## 1.1.0

- Added a dedicated NeoForge 26.2 distribution while preserving the existing Fabric release
- Added a shared-code project structure for maintaining Fabric and NeoForge together
- Added Quilt distribution through the runtime-verified Fabric artifact rather than a duplicate Quilt build; Quilt profiles require the Fabric builds of Fabric API and Cloth Config to be added manually when the launcher hides them
- Rebranded the mod as CatCraft Companion with the `/ccc` client command and a new icon
- Added a focused, live command/action search to the F8 menu
- Added searchable player profiles for online and offline players
- Added full-player skin rendering, real username and formatted nickname display, online/last-seen state, playtime, and joined date when available from CatCraft
- Added Player and Staff profile tabs with target-player actions and capability-filtered staff tools
- Added live completion-backed Homes and Kits selectors without duplicating server data
- Replaced the nonfunctional `/warps` shortcut with a compact CatCraft warp selector
- Separated Player Rank from Staff Role and expanded capability filtering
- Expanded the task-oriented player command tree and preserved predictable Back, Main Menu, Close, and Escape navigation
- Preserved timestamp-only copy behavior, chat compatibility, moderation-report capture, and corrected Jail, Unjail, and Unmute routing

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
