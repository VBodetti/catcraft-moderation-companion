package net.catcraft.ccmc.server;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CcmcServerPlugin extends JavaPlugin {
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        menuManager = new MenuManager(this);
        getServer().getPluginManager().registerEvents(menuManager, this);

        PluginCommand command = getCommand("ccmc");
        if (command == null) {
            throw new IllegalStateException("plugin.yml did not register /ccmc");
        }

        CcmcCommand handler = new CcmcCommand(menuManager);
        command.setExecutor(handler);
        command.setTabCompleter(handler);

        getLogger().info("CCMC Server 1.0.0 enabled for Paper 26.2.");
    }

    MenuManager menus() {
        return menuManager;
    }
}
