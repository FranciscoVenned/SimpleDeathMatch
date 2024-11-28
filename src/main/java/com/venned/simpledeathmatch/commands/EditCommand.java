package com.venned.simpledeathmatch.commands;

import com.venned.simpledeathmatch.manager.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditCommand implements CommandExecutor {

    ArenaManager manager;

    public EditCommand(ArenaManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can execute this command.");
            return true;
        }

        Player player = (Player) commandSender;

        if (args.length < 2) {
            player.sendMessage("Usage: /edit <setlobby/setspawn/setsupplydrop> <id_arena> [id_spawn]");
            return true;
        }

        String action = args[0];
        int arenaId;

        try {
            arenaId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid arena ID.");
            return true;
        }

        if (manager.getArena(arenaId) == null) {
            player.sendMessage("Arena not found.");
            return true;
        }

        switch (action.toLowerCase()) {
            case "setlobby":
                // Set the lobby location
                manager.updateLobbyLocation(arenaId, player.getLocation());
                player.sendMessage("Lobby location updated for arena ID: " + arenaId);
                break;

            case "setspawn":
                if (args.length < 3) {
                    player.sendMessage("Usage: /edit setspawn <id_spawn> <id_arena>");
                    return true;
                }

                int spawnId;
                try {
                    spawnId = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid spawn ID.");
                    return true;
                }

                manager.updateSpawnLocation(arenaId, spawnId, player.getLocation());
                player.sendMessage("Spawn point " + spawnId + " updated for arena ID: " + arenaId);
                break;

            case "setsupplydrop":
                if (args.length < 3) {
                    player.sendMessage("Usage: /edit setsupplydrop <id_supply> <id_arena>");
                    return true;
                }

                int supplyDropId;
                try {
                    supplyDropId = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid supply drop ID.");
                    return true;
                }

                manager.updateSupplyDropLocation(arenaId, supplyDropId, player.getLocation());
                player.sendMessage("Supply drop " + supplyDropId + " updated for arena ID: " + arenaId);
                break;

            default:
                player.sendMessage("Unknown action. Use setlobby or setspawn.");
                break;
        }

        return true;
    }
}
