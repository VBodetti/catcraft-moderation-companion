package net.catcraft.ccmc.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class CcmcCommand implements CommandExecutor, TabCompleter {
    private final MenuManager menus;

    CcmcCommand(MenuManager menus) {
        this.menus = menus;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("CCMC is an in-game staff GUI and must be opened by a player.");
            return true;
        }

        if (!player.hasPermission("ccmc.use")) {
            player.sendMessage(Component.text("You do not have permission to use CCMC.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            menus.openStaffRoot(player);
            return true;
        }

        if (args.length == 1) {
            Player target = findExactOnline(args[0]);
            if (target == null) {
                player.sendMessage(Component.text("Player is not online: " + args[0], NamedTextColor.RED));
                return true;
            }
            menus.openPlayerRoot(player, target);
            return true;
        }

        player.sendMessage(Component.text("Usage: /ccmc [online-player]", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player) || !player.hasPermission("ccmc.use") || args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(online.getName());
            }
        }
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }

    private static Player findExactOnline(String name) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null) return exact;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) return player;
        }
        return null;
    }
}
