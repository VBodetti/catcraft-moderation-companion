package net.catcraft.ccmc;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class OpenStaffMenuCommand implements Command<FabricClientCommandSource> {
    @Override public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        StaffMenuKeyHandler.requestOpen();
        return 1;
    }
}
