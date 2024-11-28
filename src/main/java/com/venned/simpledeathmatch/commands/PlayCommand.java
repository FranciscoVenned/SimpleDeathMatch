package com.venned.simpledeathmatch.commands;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.gui.JoinGameMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayCommand implements CommandExecutor {

    JoinGameMenu joinGameMenu;

    public PlayCommand(JoinGameMenu joinGameMenu) {
        this.joinGameMenu = joinGameMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player) {
            boolean isLobby = Main.getInstance().getConfig().getBoolean("isLobby", false);
            if(isLobby) {
                Player player = (Player) commandSender;
                joinGameMenu.openMenu(player);
            }
        }
        return false;
    }
}
