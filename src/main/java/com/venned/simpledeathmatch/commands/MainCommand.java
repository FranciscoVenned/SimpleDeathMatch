package com.venned.simpledeathmatch.commands;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.messenger.SpigotRedisClient;
import com.venned.simpledeathmatch.utils.EquipGame;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MainCommand implements CommandExecutor {

    SpigotRedisClient spigotRedisClient;

    public MainCommand(SpigotRedisClient spigotRedisClient) {
        this.spigotRedisClient = spigotRedisClient;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if(args.length != 0){
                if(args[0].equalsIgnoreCase("kit")){
                    Inventory kitItems = EquipGame.kitGame(player, true);
                    for(int i = 0; i < kitItems.getSize(); i++) {
                        player.getInventory().setItem(i , kitItems.getItem(i));
                    }
                    return true;
                } else if(args[0].equalsIgnoreCase("save")) {
                    PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
                    if(playerData != null){
                        Inventory inventory = Bukkit.createInventory(null, 45);
                        Inventory playerInventory = player.getInventory();

                        for(int i = 0; i < playerInventory.getSize(); i++){
                            if(playerInventory.getItem(i) != null) {
                                inventory.setItem(i, playerInventory.getItem(i));
                            }
                        }

                        playerData.setKitInventory(inventory);
                    }

                    return true;
                } else if(args[0].equalsIgnoreCase("give")) {
                    PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
                    if(playerData != null){
                        Inventory inventory = playerData.getKitInventory();
                        player.getInventory().clear();
                        for(int i = 0; i < inventory.getSize(); i++){
                            if(inventory.getItem(i) != null){
                                player.getInventory().setItem(i, inventory.getItem(i));
                            }
                        }

                        return true;

                    }
                }
            }

            spigotRedisClient.sendMessageToBungee("CHECK_AVAILABLE:"+player.getDisplayName() + ":SOLO");

        }
        return false;
    }
}
