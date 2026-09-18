# CatCraft Moderation Companion — Server Plugin 1.0.0

Server-side companion implementation of the validated CCMC 1.0.0 moderation workflow.

## Target

- Paper 26.2
- Java 25
- Vanilla Java clients: supported
- Bedrock clients connecting through CatCraft's Java/Bedrock bridge: inventory GUI is designed to remain usable through normal inventory translation
- Fabric CCMC client: optional; not required for this plugin

## Entry points

- `/ccmc` — opens the staff menu
- `/ccmc <online-player>` — opens the player action menu for that target

## Permission model

- `ccmc.use` — Helper/base access
- `ccmc.moderator` — Moderator actions
- `ccmc.seniormod` — Senior Moderator actions such as Fly/God
- `ccmc.admin` — Administrator umbrella permission

The plugin dispatches CatCraft moderation commands **as the staff player**, so the server's existing command permissions remain authoritative.

## 1.0.0 scope

Ported server-side:
- Player selection and player action GUI
- Message/Mail command preparation
- Teleports
- Punish, Warn, Temp Mute, Kick, Jail, Unjail, Unmute, Temp Ban
- Player Info
- Inventory/Ender Chest/Trade Logs
- Vulcan
- CoreProtect
- Vanish, game modes, Ignore Claims, TradeShop Admin, TPS, Report Lag
- Staff chat/channel actions
- Moderator/Senior Moderator permission gating

Client-only and intentionally omitted:
- F8 keybind
- Clicking a username directly in rendered client chat
- CCMC timestamps/chat retention/copy menu
- Client settings screen
- Native OS clipboard/screenshot capture

Custom command fields use clickable Java chat suggestions after the GUI closes. Fixed actions are fully GUI-driven.
