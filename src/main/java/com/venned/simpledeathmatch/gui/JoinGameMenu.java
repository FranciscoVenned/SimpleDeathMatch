package com.venned.simpledeathmatch.gui;

import com.venned.simpledeathmatch.messenger.SpigotRedisClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JoinGameMenu implements Listener {

    SpigotRedisClient spigotRedisClient;

    public JoinGameMenu(SpigotRedisClient spigotRedisClient) {
        this.spigotRedisClient = spigotRedisClient;
    }

    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, "Select Game Mode");

        // Populate the menu with paper items for each glow color
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Solo Mode");
        item.setItemMeta(meta);

        ItemStack item2 = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta2 = item2.getItemMeta();
        meta2.setDisplayName(ChatColor.GREEN + "Duo Mode");
        item2.setItemMeta(meta2);

        menu.setItem(2, item);
        menu.setItem(6, item2);


        player.openInventory(menu);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select Game Mode")) {
            event.setCancelled(true); // Prevent taking items from the menu

            if (event.getCurrentItem() == null) return;

            // Get the clicked color name from the item display name
            Player player = (Player) event.getWhoClicked();
            String GameMode = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            try {
                if(GameMode.equalsIgnoreCase("Duo Mode")) {
                    spigotRedisClient.sendMessageToBungee("CHECK_AVAILABLE:" + player.getDisplayName() + ":DUO");
                    player.sendMessage("§aYou have selected : §7" + GameMode);
                } else if(GameMode.equalsIgnoreCase("Solo Mode")) {
                    spigotRedisClient.sendMessageToBungee("CHECK_AVAILABLE:"+player.getDisplayName() + ":SOLO");
                    player.sendMessage("§aYou have selected : §7" + GameMode);
                }
                player.closeInventory();
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cThis color is not available.");
            }
        }
    }

}
