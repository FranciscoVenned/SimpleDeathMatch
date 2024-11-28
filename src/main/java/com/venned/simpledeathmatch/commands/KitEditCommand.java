package com.venned.simpledeathmatch.commands;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.utils.EquipGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class KitEditCommand implements CommandExecutor {

    public static Set<Player> players = new HashSet<Player>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            boolean isLobby = Main.getInstance().getConfig().getBoolean("isLobby", false);
            if(args.length != 0) {
                if(isLobby){
                if(args[0].equalsIgnoreCase("edit")) {
                    if(!players.contains(player)) {
                        Inventory kitItems = EquipGame.kitGame(player, true);
                        int maxSlots = Math.min(kitItems.getSize(), 41);
                        for (int i = 0; i < maxSlots; i++) {
                            player.getInventory().setItem(i, kitItems.getItem(i));
                        }
                        players.add(player);
                        player.sendMessage("§a§l(!) §7Once you finish editing use /editkit save");
                        return true;
                    }
                } else if(args[0].equalsIgnoreCase("save")) {
                    PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
                    if(players.contains(player)) {
                        if (playerData != null) {
                            Inventory inventory = Bukkit.createInventory(null, 45);
                            Inventory playerInventory = player.getInventory();

                            for (int i = 0; i < playerInventory.getSize(); i++) {
                                if (playerInventory.getItem(i) != null) {
                                    inventory.setItem(i, playerInventory.getItem(i));
                                }
                            }

                            playerData.setKitInventory(inventory);
                        }

                        player.getInventory().clear();
                        ItemStack itemStack = new ItemStack(Material.PAPER);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName("Set Glow Color");
                        itemStack.setItemMeta(itemMeta);

                        ItemStack itemStack1 = new ItemStack(Material.BOOK);
                        ItemMeta itemMeta1 = itemStack1.getItemMeta();
                        itemMeta1.setDisplayName("Set Block Type");
                        itemStack1.setItemMeta(itemMeta1);

                        ItemStack itemStack2 = new ItemStack(Material.EMERALD);
                        ItemMeta itemMeta2 = itemStack2.getItemMeta();
                        itemMeta2.setDisplayName("Stats");
                        itemStack2.setItemMeta(itemMeta2);

                        ItemStack itemStack3 = new ItemStack(Material.REDSTONE);
                        ItemMeta itemMeta3 = itemStack3.getItemMeta();
                        itemMeta3.setDisplayName("Play");
                        itemStack3.setItemMeta(itemMeta3);

                        ItemStack itemStack4 = new ItemStack(Material.NETHER_STAR);
                        ItemMeta itemMeta4 = itemStack4.getItemMeta();
                        itemMeta4.setDisplayName("Kit Editor");
                        itemStack4.setItemMeta(itemMeta4);

                        player.getInventory().setItem(6, itemStack);
                        player.getInventory().setItem(8, itemStack1);
                        player.getInventory().setItem(0, itemStack2);
                        player.getInventory().setItem(4, itemStack3);
                        player.getInventory().setItem(2, itemStack4);

                        player.sendMessage("§a§l(!) §7Kit saved successfully");

                        players.remove(player);

                        return true;
                    }
                 }
                }
            }
        }

        return false;
    }
}
