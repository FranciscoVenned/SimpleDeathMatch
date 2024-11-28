package com.venned.simpledeathmatch.commands;

import com.venned.simpledeathmatch.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AvailableCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Main.getInstance().setAvailable(false);
        return false;
    }
}
