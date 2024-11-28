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

import java.util.ArrayList;
import java.util.List;

public class BlockSetMenu implements Listener {

    private final List<Material> blocksList; // Glow colors from config
    private final PlayerDataManager playerDataManager;

    public BlockSetMenu(List<String> blocksList, PlayerDataManager playerDataManager) {
        List<Material> blocks = new ArrayList<>();
        for(String materials : blocksList) {
            Material material = Material.getMaterial(materials);
            if (material != null) {
                blocks.add(material);
            }
        }
        this.blocksList = blocks;
        this.playerDataManager = playerDataManager;
    }

    // Opens the glow color selection menu for the player
    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 18, "Select Block Type");

        // Populate the menu with paper items for each glow color
        for (Material material : blocksList) {
            ItemStack paper = new ItemStack(material, 1);
            ItemMeta meta = paper.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + material.name()); // Set paper color name with ChatColor
                paper.setItemMeta(meta);
            }

            menu.addItem(paper);
        }

        player.openInventory(menu);
    }

    // Event handler for menu clicks
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select Block Type")) {
            event.setCancelled(true); // Prevent taking items from the menu

            if (event.getCurrentItem() == null) return;

            // Get the clicked color name from the item display name
            Player player = (Player) event.getWhoClicked();
            if(blocksList.contains(event.getCurrentItem().getType())) {


                String blockType = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

                if(player.hasPermission("deathmatch.blockset." + blockType)) {
                    try {
                        // Apply the glow color to the player (this depends on your implementation)
                        applySetBlock(player, blockType);
                        player.sendMessage("§aYou have selected block type: " + ChatColor.YELLOW + blockType);
                        player.closeInventory();
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§cThis color is not available.");
                    }
                }
            }
        }
    }

    private void applySetBlock(Player player, String block) {
        playerDataManager.getPlayerData(player.getUniqueId()).setMaterialSelect(block);
    }
}
