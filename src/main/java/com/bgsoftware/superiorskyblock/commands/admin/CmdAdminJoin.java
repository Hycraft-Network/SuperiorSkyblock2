package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdAdminJoin implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("join");
    }

    @Override
    public String getPermission() {
        return "superior.admin.join";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin join <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_JOIN.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if (superiorPlayer.getIsland() != null) {
            Message.ALREADY_IN_ISLAND.send(superiorPlayer);
            return;
        }

        IslandUtils.sendMessage(island, Message.JOIN_ADMIN_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

        island.addMember(superiorPlayer, SPlayerRole.defaultRole());

        if (targetPlayer == null)
            Message.JOINED_ISLAND_NAME.send(superiorPlayer, island.getName());
        else
            Message.JOINED_ISLAND.send(superiorPlayer, targetPlayer.getName());

        if (plugin.getSettings().isTeleportOnJoin())
            superiorPlayer.teleport(island);
    }

}
