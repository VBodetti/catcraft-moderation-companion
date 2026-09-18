package net.catcraft.ccmc.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

final class MenuManager implements Listener {
    private static final int PLAYER_PAGE_SIZE = 45;

    private final CcmcServerPlugin plugin;
    private final NamespacedKey actionKey;
    private final NamespacedKey valueKey;

    MenuManager(CcmcServerPlugin plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "action");
        this.valueKey = new NamespacedKey(plugin, "value");
    }

    void openStaffRoot(Player staff) {
        MenuHolder holder = holder(MenuHolder.Type.STAFF_ROOT, null, null, 0, 27, "CCMC • Staff Menu");
        Inventory inv = holder.getInventory();

        inv.setItem(11, item(Material.PLAYER_HEAD, "Player Actions", "player-select", "", NamedTextColor.AQUA,
                "Choose an online player."));
        if (isModerator(staff)) {
            inv.setItem(13, item(Material.COMPARATOR, "Server Tools", "server-tools", "", NamedTextColor.GOLD,
                    "Moderator server utilities."));
        }
        inv.setItem(15, item(Material.WRITABLE_BOOK, "Chat Channels", "chat-channels", "", NamedTextColor.GREEN,
                "Staff and global chat controls."));

        staff.openInventory(inv);
    }

    void openPlayerRoot(Player staff, Player target) {
        openPlayerRoot(staff, target.getUniqueId());
    }

    private void openPlayerRoot(Player staff, UUID targetId) {
        String target = targetName(targetId);
        if (target == null) {
            unavailable(staff, "That player is no longer available.");
            return;
        }

        MenuHolder holder = holder(MenuHolder.Type.PLAYER_ROOT, targetId, null, 0, 27, "CCMC • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(10, item(Material.PAPER, "Message", "suggest", "/msg " + target + " ", NamedTextColor.AQUA,
                "Prepare a private message."));
        inv.setItem(11, item(Material.BOOK, "Mail", "suggest", "/mail send " + target + " ", NamedTextColor.AQUA,
                "Prepare server mail."));
        if (isModerator(staff)) {
            inv.setItem(12, item(Material.ENDER_PEARL, "Teleport", "teleport", "", NamedTextColor.LIGHT_PURPLE));
        }
        inv.setItem(13, item(Material.ANVIL, "Moderate", "moderate", "", NamedTextColor.RED));
        inv.setItem(14, item(Material.SPYGLASS, "Investigate", "investigate", "", NamedTextColor.YELLOW));
        if (isModerator(staff)) {
            inv.setItem(15, item(Material.REDSTONE_TORCH, "Tools", "player-tools", "", NamedTextColor.GOLD));
        }
        inv.setItem(16, item(Material.NAME_TAG, "Show Username", "show-username", target, NamedTextColor.WHITE,
                "Displays the exact username in chat."));
        inv.setItem(22, back("staff-root"));

        staff.openInventory(inv);
    }

    private void openPlayerSelect(Player staff, int requestedPage) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int maxPage = Math.max(0, (players.size() - 1) / PLAYER_PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, maxPage));

        MenuHolder holder = holder(MenuHolder.Type.PLAYER_SELECT, null, null, page, 54,
                "CCMC • Select Player " + (page + 1) + "/" + (maxPage + 1));
        Inventory inv = holder.getInventory();

        int start = page * PLAYER_PAGE_SIZE;
        int end = Math.min(players.size(), start + PLAYER_PAGE_SIZE);
        for (int i = start; i < end; i++) {
            Player target = players.get(i);
            inv.setItem(i - start, playerHead(target));
        }

        if (page > 0) {
            inv.setItem(45, item(Material.ARROW, "Previous Page", "player-page", String.valueOf(page - 1), NamedTextColor.YELLOW));
        }
        inv.setItem(49, back("staff-root"));
        if (page < maxPage) {
            inv.setItem(53, item(Material.ARROW, "Next Page", "player-page", String.valueOf(page + 1), NamedTextColor.YELLOW));
        }

        staff.openInventory(inv);
    }

    private void openTeleport(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.TELEPORT, targetId, null, 0, 27, "CCMC • Teleport • " + target);
        Inventory inv = holder.getInventory();
        inv.setItem(10, commandItem(Material.ENDER_PEARL, "TPO to Player", "tpo " + target));
        inv.setItem(12, commandItem(Material.RECOVERY_COMPASS, "TPO Here", "tphere " + target));
        inv.setItem(14, commandItem(Material.COMPASS, "Offline TP", "offlinetp " + target));
        inv.setItem(16, commandItem(Material.LODESTONE, "Here Offline", "tphereoffline " + target));
        inv.setItem(22, back("player-root"));
        staff.openInventory(inv);
    }

    private void openModerate(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.MODERATE, targetId, null, 0, 27, "CCMC • Moderate • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(9, commandItem(Material.BOOK, "Punish", "punish " + target));
        inv.setItem(10, item(Material.CLOCK, "Temp Mute", "temp-mute-duration", "", NamedTextColor.YELLOW));
        inv.setItem(11, item(Material.PAPER, "Warn", "warn", "", NamedTextColor.YELLOW));
        inv.setItem(12, commandItem(Material.MILK_BUCKET, "Unmute", "lunmute " + target));
        inv.setItem(13, item(Material.IRON_BOOTS, "Kick", "kick", "", NamedTextColor.RED));
        inv.setItem(14, commandItem(Material.IRON_BARS, "Jail", "togglejail " + target + " 1"));
        inv.setItem(15, commandItem(Material.TRIPWIRE_HOOK, "Unjail", "unjail " + target));

        if (isModerator(staff)) {
            inv.setItem(16, item(Material.BARRIER, "Temp Ban", "temp-ban-duration", "", NamedTextColor.DARK_RED));
        }

        inv.setItem(22, back("player-root"));
        staff.openInventory(inv);
    }

    private void openWarn(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.WARN, targetId, null, 0, 27, "CCMC • Warn • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(9, commandItem(Material.PAPER, "Causing Drama", "warn " + target + " Causing Drama"));
        inv.setItem(10, commandItem(Material.PAPER, "Spamming", "warn " + target + " Spamming"));
        inv.setItem(11, commandItem(Material.PAPER, "Begging", "warn " + target + " Begging"));
        inv.setItem(12, commandItem(Material.PAPER, "Mini-Modding", "warn " + target + " Please refrain from mini-modding players"));
        inv.setItem(13, commandItem(Material.PAPER, "Admin Demands", "warn " + target + " Continued demands for an admin despite assistance"));
        inv.setItem(14, commandItem(Material.PAPER, "Threatening Players", "warn " + target + " Threatening other players is against the server rules"));
        inv.setItem(16, suggestItem(Material.WRITABLE_BOOK, "Custom Reason", "/warn " + target + " "));
        inv.setItem(22, back("moderate"));

        staff.openInventory(inv);
    }

    private void openKick(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.KICK, targetId, null, 0, 27, "CCMC • Kick • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(11, commandItem(Material.IRON_BOOTS, "Inappropriate Name/Skin",
                "kick " + target + " Inappropriate name/skin. Please change before re-joining or it will result in a ban!"));
        inv.setItem(15, suggestItem(Material.WRITABLE_BOOK, "Custom Reason", "/kick " + target + " "));
        inv.setItem(22, back("moderate"));

        staff.openInventory(inv);
    }

    private void openTempMuteDuration(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.TEMP_MUTE_DURATION, targetId, null, 0, 27,
                "CCMC • Temp Mute • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(11, item(Material.CLOCK, "5 min", "temp-mute-reason", "5m", NamedTextColor.YELLOW));
        inv.setItem(13, item(Material.CLOCK, "20 min", "temp-mute-reason", "20m", NamedTextColor.YELLOW));
        inv.setItem(15, suggestItem(Material.WRITABLE_BOOK, "Custom Duration", "/ltempmute " + target + " "));
        inv.setItem(22, back("moderate"));

        staff.openInventory(inv);
    }

    private void openTempMuteReason(Player staff, UUID targetId, String duration) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.TEMP_MUTE_REASON, targetId, duration, 0, 27,
                "CCMC • Mute " + duration + " • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(10, commandItem(Material.PAPER, "Causing Drama", "ltempmute " + target + " " + duration + " Causing Drama"));
        inv.setItem(12, commandItem(Material.PAPER, "Spamming", "ltempmute " + target + " " + duration + " Spamming"));
        inv.setItem(14, commandItem(Material.PAPER, "Begging", "ltempmute " + target + " " + duration + " Begging"));
        inv.setItem(16, suggestItem(Material.WRITABLE_BOOK, "Custom Reason", "/ltempmute " + target + " " + duration + " "));
        inv.setItem(22, back("temp-mute-duration"));

        staff.openInventory(inv);
    }

    private void openTempBanDuration(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.TEMP_BAN_DURATION, targetId, null, 0, 27,
                "CCMC • Temp Ban • " + target);
        Inventory inv = holder.getInventory();

        String[] durations = {"3d", "1w", "2w", "3w", "4w"};
        for (int i = 0; i < durations.length; i++) {
            String duration = durations[i];
            inv.setItem(9 + i, item(Material.CLOCK, duration, "temp-ban-reason", duration, NamedTextColor.RED));
        }
        inv.setItem(15, suggestItem(Material.WRITABLE_BOOK, "Custom Duration", "/tempban " + target + " "));
        inv.setItem(22, back("moderate"));

        staff.openInventory(inv);
    }

    private void openTempBanReason(Player staff, UUID targetId, String duration) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.TEMP_BAN_REASON, targetId, duration, 0, 36,
                "CCMC • Ban " + duration + " • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(9, banReason(target, duration, "Minor Grief/Stealing", "Minor Grief/Stealing"));
        inv.setItem(10, banReason(target, duration, "Medium Grief/Stealing", "Medium Grief/Stealing"));
        inv.setItem(11, banReason(target, duration, "TP-Killing", "TP-Killing"));
        inv.setItem(12, banReason(target, duration, "Map Art Theft/Cloning", "Stealing/Cloning Map Art"));
        inv.setItem(13, banReason(target, duration, "Base Raiding", "Base Raiding"));
        inv.setItem(14, banReason(target, duration, "Hacked Client", "Hacked Client"));
        inv.setItem(15, banReason(target, duration, "Inappropriate Name/Skin", "Inappropriate name/skin"));

        if ("3w".equals(duration)) {
            inv.setItem(19, banReason(target, duration, "Admitting to Xray",
                    "Admitting to xray. Reduced ban for being honest. Next time, its a permanent ban."));
        }
        if ("4w".equals(duration)) {
            inv.setItem(19, banReason(target, duration, "Xraying",
                    "Xraying. Next time, its a permanent ban."));
        }

        inv.setItem(25, suggestItem(Material.WRITABLE_BOOK, "Custom Reason", "/tempban " + target + " " + duration + " "));
        inv.setItem(31, back("temp-ban-duration"));

        staff.openInventory(inv);
    }

    private void openInvestigate(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.INVESTIGATE, targetId, null, 0, 27,
                "CCMC • Investigate • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(10, item(Material.BOOK, "Player Info", "player-info", "", NamedTextColor.AQUA));
        if (isModerator(staff)) {
            inv.setItem(12, item(Material.CHEST, "Inventories", "inventories", "", NamedTextColor.GOLD));
            inv.setItem(14, item(Material.IRON_SWORD, "Anti-Cheat", "anticheat", "", NamedTextColor.RED));
            inv.setItem(16, item(Material.GOLDEN_SHOVEL, "CoreProtect", "coreprotect", "", NamedTextColor.YELLOW));
        }
        inv.setItem(22, back("player-root"));

        staff.openInventory(inv);
    }

    private void openPlayerInfo(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.PLAYER_INFO, targetId, null, 0, 27,
                "CCMC • Player Info • " + target);
        Inventory inv = holder.getInventory();
        inv.setItem(11, commandItem(Material.BOOK, "History", "history " + target));
        inv.setItem(15, commandItem(Material.CLOCK, "Playtime", "eplaytime " + target));
        inv.setItem(22, back("investigate"));
        staff.openInventory(inv);
    }

    private void openInventories(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.INVENTORIES, targetId, null, 0, 27,
                "CCMC • Inventories • " + target);
        Inventory inv = holder.getInventory();
        inv.setItem(10, commandItem(Material.CHEST, "Inventory", "open " + target));
        inv.setItem(13, commandItem(Material.ENDER_CHEST, "Ender Chest", "openender " + target));
        inv.setItem(16, commandItem(Material.EMERALD, "Trade Logs", "trade logs " + target));
        inv.setItem(22, back("investigate"));
        staff.openInventory(inv);
    }

    private void openAntiCheat(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.ANTICHEAT, targetId, null, 0, 27,
                "CCMC • Anti-Cheat • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(9, commandItem(Material.PLAYER_HEAD, "Vulcan Profile", "vulcan profile " + target));
        inv.setItem(10, commandItem(Material.REDSTONE, "Violations", "vulcan violations " + target));
        inv.setItem(11, commandItem(Material.CLOCK, "CPS", "vulcan cps " + target));
        inv.setItem(12, commandItem(Material.SHIELD, "Knockback Test", "vulcan knockback " + target));
        inv.setItem(13, commandItem(Material.PACKED_ICE, "Freeze", "vulcan freeze " + target));
        inv.setItem(22, back("investigate"));

        staff.openInventory(inv);
    }

    private void openCoreProtect(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.COREPROTECT, targetId, null, 0, 27,
                "CCMC • CoreProtect • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(10, commandItem(Material.GOLDEN_SHOVEL, "All Actions (2d)", "co lookup user:" + target + " time:2d"));
        inv.setItem(12, commandItem(Material.CHEST, "Container (2d)", "co lookup user:" + target + " time:2d action:container"));
        inv.setItem(14, commandItem(Material.HOPPER, "Pickup (2d)", "co lookup user:" + target + " time:2d action:pickup"));
        inv.setItem(16, suggestItem(Material.WRITABLE_BOOK, "Custom Lookup", "/co lookup user:" + target + " "));
        inv.setItem(22, back("investigate"));

        staff.openInventory(inv);
    }

    private void openPlayerTools(Player staff, UUID targetId) {
        String target = requireTarget(staff, targetId);
        if (target == null) return;

        MenuHolder holder = holder(MenuHolder.Type.PLAYER_TOOLS, targetId, null, 0, 36,
                "CCMC • Tools • " + target);
        Inventory inv = holder.getInventory();

        inv.setItem(9, commandItem(Material.GLASS, "Vanish", "vanish"));
        inv.setItem(10, commandItem(Material.SPYGLASS, "Spectator Mode", "gamemode spectator"));
        inv.setItem(11, commandItem(Material.GRASS_BLOCK, "Survival Mode", "gamemode survival"));
        inv.setItem(12, item(Material.WRITABLE_BOOK, "Chat Channels", "chat-channels", "", NamedTextColor.GREEN));
        inv.setItem(13, commandItem(Material.GOLDEN_SHOVEL, "Ignore Claims", "ignoreclaims"));
        inv.setItem(14, commandItem(Material.EMERALD, "TradeShop Admin", "ts toggleadmin"));
        inv.setItem(15, commandItem(Material.REDSTONE, "Report Lag", "reportlag"));

        if (isSenior(staff)) {
            inv.setItem(18, commandItem(Material.FEATHER, "Fly", "fly"));
            inv.setItem(20, commandItem(Material.TOTEM_OF_UNDYING, "God", "god"));
        }

        inv.setItem(31, back("player-root"));
        staff.openInventory(inv);
    }

    private void openServerTools(Player staff) {
        MenuHolder holder = holder(MenuHolder.Type.SERVER_TOOLS, null, null, 0, 36, "CCMC • Server Tools");
        Inventory inv = holder.getInventory();

        inv.setItem(9, commandItem(Material.GLASS, "Vanish", "vanish"));
        inv.setItem(10, commandItem(Material.SPYGLASS, "Spectator Mode", "gamemode spectator"));
        inv.setItem(11, commandItem(Material.GRASS_BLOCK, "Survival Mode", "gamemode survival"));
        inv.setItem(12, commandItem(Material.GOLDEN_SHOVEL, "Ignore Claims", "ignoreclaims"));
        inv.setItem(13, commandItem(Material.EMERALD, "TradeShop Admin", "ts toggleadmin"));
        inv.setItem(14, commandItem(Material.CLOCK, "TPS", "tps"));
        inv.setItem(15, commandItem(Material.REDSTONE, "Report Lag", "reportlag"));

        if (isSenior(staff)) {
            inv.setItem(18, commandItem(Material.FEATHER, "Fly", "fly"));
            inv.setItem(20, commandItem(Material.TOTEM_OF_UNDYING, "God", "god"));
        }

        inv.setItem(31, back("staff-root"));
        staff.openInventory(inv);
    }

    private void openChatChannels(Player staff, UUID returnTarget) {
        MenuHolder holder = holder(MenuHolder.Type.CHAT_CHANNELS, returnTarget, null, 0, 27, "CCMC • Chat Channels");
        Inventory inv = holder.getInventory();

        inv.setItem(9, commandItem(Material.WRITABLE_BOOK, "SCC Staff Chat", "scc"));
        inv.setItem(11, suggestItem(Material.PAPER, "VSC Message", "/vsc "));
        inv.setItem(13, commandItem(Material.REDSTONE_TORCH, "Mod Chat", "channel mod"));
        inv.setItem(15, commandItem(Material.GRASS_BLOCK, "Global", "g"));
        inv.setItem(17, commandItem(Material.LEVER, "Chat On/Off", "togglechat"));
        inv.setItem(22, back(returnTarget == null ? "staff-root" : "player-tools"));

        staff.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player staff)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        String value = meta.getPersistentDataContainer().get(valueKey, PersistentDataType.STRING);
        if (action == null) return;

        handle(staff, holder, action, value == null ? "" : value);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    private void handle(Player staff, MenuHolder holder, String action, String value) {
        UUID targetId = holder.targetId();

        switch (action) {
            case "player-select" -> openPlayerSelect(staff, 0);
            case "player-page" -> openPlayerSelect(staff, parseInt(value, holder.page()));
            case "select-target" -> {
                try {
                    UUID selected = UUID.fromString(value);
                    Player target = Bukkit.getPlayer(selected);
                    if (target == null) unavailable(staff, "That player went offline.");
                    else openPlayerRoot(staff, target);
                } catch (IllegalArgumentException ignored) {
                    unavailable(staff, "Invalid player selection.");
                }
            }
            case "staff-root" -> openStaffRoot(staff);
            case "player-root" -> openPlayerRoot(staff, targetId);
            case "teleport" -> requireModerator(staff, () -> openTeleport(staff, targetId));
            case "moderate" -> openModerate(staff, targetId);
            case "warn" -> openWarn(staff, targetId);
            case "kick" -> openKick(staff, targetId);
            case "temp-mute-duration" -> openTempMuteDuration(staff, targetId);
            case "temp-mute-reason" -> openTempMuteReason(staff, targetId, value);
            case "temp-ban-duration" -> requireModerator(staff, () -> openTempBanDuration(staff, targetId));
            case "temp-ban-reason" -> requireModerator(staff, () -> openTempBanReason(staff, targetId, value));
            case "investigate" -> openInvestigate(staff, targetId);
            case "player-info" -> openPlayerInfo(staff, targetId);
            case "inventories" -> requireModerator(staff, () -> openInventories(staff, targetId));
            case "anticheat" -> requireModerator(staff, () -> openAntiCheat(staff, targetId));
            case "coreprotect" -> requireModerator(staff, () -> openCoreProtect(staff, targetId));
            case "player-tools" -> requireModerator(staff, () -> openPlayerTools(staff, targetId));
            case "server-tools" -> requireModerator(staff, () -> openServerTools(staff));
            case "chat-channels" -> openChatChannels(staff, targetId);
            case "command" -> runCommand(staff, value);
            case "suggest" -> suggestCommand(staff, value);
            case "show-username" -> showUsername(staff, value);
            default -> unavailable(staff, "Unknown CCMC menu action: " + action);
        }
    }

    private void runCommand(Player staff, String command) {
        staff.closeInventory();
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean accepted = Bukkit.dispatchCommand(staff, command);
            if (!accepted) {
                staff.sendMessage(Component.text("The server did not accept /" + command, NamedTextColor.RED));
            }
        });
    }

    private void suggestCommand(Player staff, String command) {
        staff.closeInventory();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Component prepared = Component.text("Click to prepare: ", NamedTextColor.YELLOW)
                    .append(Component.text(command, NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.suggestCommand(command))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to put this command in chat"))));
            staff.sendMessage(prepared);
            staff.sendMessage(Component.text("Bedrock clients may need to type the shown command manually.", NamedTextColor.GRAY));
        });
    }

    private void showUsername(Player staff, String target) {
        staff.closeInventory();
        staff.sendMessage(Component.text("Username: ", NamedTextColor.YELLOW)
                .append(Component.text(target, NamedTextColor.AQUA)));
    }

    private ItemStack playerHead(Player target) {
        ItemStack stack = item(Material.PLAYER_HEAD, target.getName(), "select-target",
                target.getUniqueId().toString(), NamedTextColor.AQUA);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(target);
            stack.setItemMeta(skull);
        }
        return stack;
    }

    private ItemStack banReason(String target, String duration, String label, String reason) {
        return commandItem(Material.PAPER, label, "tempban " + target + " " + duration + " " + reason);
    }

    private ItemStack commandItem(Material material, String label, String command) {
        return item(material, label, "command", command, NamedTextColor.WHITE);
    }

    private ItemStack suggestItem(Material material, String label, String command) {
        return item(material, label, "suggest", command, NamedTextColor.YELLOW,
                "Closes the GUI and prepares a command in chat.");
    }

    private ItemStack back(String destination) {
        return item(Material.ARROW, "Back", destination, "", NamedTextColor.GRAY);
    }

    private ItemStack item(Material material, String label, String action, String value, NamedTextColor color, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text(label, color).decoration(TextDecoration.ITALIC, false));
        if (lore.length > 0) {
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(Component.text(line, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lines);
        }

        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        meta.getPersistentDataContainer().set(valueKey, PersistentDataType.STRING, value == null ? "" : value);
        stack.setItemMeta(meta);
        return stack;
    }

    private MenuHolder holder(
            MenuHolder.Type type,
            UUID targetId,
            String data,
            int page,
            int size,
            String title
    ) {
        MenuHolder holder = new MenuHolder(type, targetId, data, page);
        Inventory inventory = Bukkit.createInventory(holder, size, Component.text(title, NamedTextColor.DARK_AQUA));
        holder.attach(inventory);
        return holder;
    }

    private String requireTarget(Player staff, UUID targetId) {
        String target = targetName(targetId);
        if (target == null) {
            unavailable(staff, "Target player information is no longer available.");
            return null;
        }
        return target;
    }

    private static String targetName(UUID targetId) {
        if (targetId == null) return null;
        Player online = Bukkit.getPlayer(targetId);
        if (online != null) return online.getName();

        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetId);
        return offline.getName();
    }

    private void requireModerator(Player staff, Runnable action) {
        if (!isModerator(staff)) {
            unavailable(staff, "This action requires the CCMC Moderator permission.");
            return;
        }
        action.run();
    }

    private static boolean isModerator(Player player) {
        return player.hasPermission("ccmc.moderator");
    }

    private static boolean isSenior(Player player) {
        return player.hasPermission("ccmc.seniormod");
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static void unavailable(Player player, String message) {
        player.closeInventory();
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    }
}
