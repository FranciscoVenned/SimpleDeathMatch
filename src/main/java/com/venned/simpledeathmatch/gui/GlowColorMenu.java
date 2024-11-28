package com.venned.simpledeathmatch.gui;

import com.venned.simpledeathmatch.manager.PlayerDataManager;
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

import java.util.List;

public class GlowColorMenu implements Listener {

    private final List<String> glowColors; // Glow colors from config
    private final PlayerDataManager playerDataManager;

    public GlowColorMenu(List<String> glowColors, PlayerDataManager playerDataManager) {
        this.glowColors = glowColors;
        this.playerDataManager = playerDataManager;
    }

    // Opens the glow color selection menu for the player
    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, "Select Glow Color");

        // Populate the menu with paper items for each glow color
        for (String colorName : glowColors) {
            ChatColor color = ChatColor.valueOf(colorName);
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(color + colorName); // Set paper color name with ChatColor
                paper.setItemMeta(meta);
            }

            menu.addItem(paper);
        }

        player.openInventory(menu);
    }

    // Event handler for menu clicks
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select Glow Color")) {
            event.setCancelled(true); // Prevent taking items from the menu

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.PAPER) return;

            // Get the clicked color name from the item display name
            Player player = (Player) event.getWhoClicked();
            String colorName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if(player.hasPermission("deathmatch.glow." + colorName)) {

                try {
                    ChatColor color = ChatColor.valueOf(colorName);
                    // Apply the glow color to the player (this depends on your implementation)
                    applyGlowColor(player, colorName);

                    player.sendMessage("§aYou have selected the glow color: " + color + colorName);
                    player.closeInventory();
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cThis color is not available.");
                }
            }
        }
    }

    private void applyGlowColor(Player player, String color) {
        playerDataManager.getPlayerData(player.getUniqueId()).setColorSelect(color);
    }
}
