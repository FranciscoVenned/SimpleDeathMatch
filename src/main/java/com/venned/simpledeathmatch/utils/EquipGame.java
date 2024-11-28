package com.venned.simpledeathmatch.utils;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EquipGame {

    public static Inventory kitGame(Player player, boolean edit) {
        Inventory inventory = Bukkit.createInventory(null, 45);

        PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());

        if(!edit) {

            int nonNullItemCount = 0;
            for (ItemStack item : playerData.getKitInventory().getContents()) {
                if (item != null) {
                    nonNullItemCount++;
                }
            }

            if (nonNullItemCount >= 3) {

                for (int i = 0; i < playerData.getKitInventory().getSize(); i++) {
                    inventory.setItem(i, playerData.getKitInventory().getItem(i));
                }

                if (!playerData.getMaterialSelect().isEmpty()) {
                    String material = playerData.getMaterialSelect();
                    Material mat = Material.getMaterial(material);
                    assert mat != null;

                    for (ItemStack item : inventory.getContents()) {
                        if (item != null && item.getType() == Material.OAK_PLANKS) {
                            item.setType(mat);
                        }
                    }
                }

                return inventory;
            }
        }




        // Diamond Sword
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        inventory.addItem(sword);

        ItemStack pickAxe = new ItemStack(Material.DIAMOND_PICKAXE);
        inventory.addItem(pickAxe);

        // Cobwebs (8)
        ItemStack cobweb = new ItemStack(Material.COBWEB, 8);
        inventory.addItem(cobweb);

        // Bow
        ItemStack bow = new ItemStack(Material.BOW);
        inventory.addItem(bow);

        // Golden Apples (3)
        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 3);
        inventory.addItem(goldenApple);

        // Water Bucket
        ItemStack waterBucket = new ItemStack(Material.WATER_BUCKET);
        inventory.addItem(waterBucket);

        // Oak Planks (64)
        if(playerData.getMaterialSelect().isEmpty()) {
            ItemStack oakPlanks = new ItemStack(Material.OAK_PLANKS, 64);
            inventory.addItem(oakPlanks);
        } else {
            String material = playerData.getMaterialSelect();
            Material mat = Material.getMaterial(material);
            assert mat != null;
            ItemStack materialItem = new ItemStack(mat, 64);
            inventory.addItem(materialItem);
        }

        // Lava Bucket
        ItemStack lavaBucket = new ItemStack(Material.LAVA_BUCKET);
        inventory.addItem(lavaBucket);

        // Diamond Axe
        ItemStack diamondAxe = new ItemStack(Material.DIAMOND_AXE);
        inventory.addItem(diamondAxe);

        // Golden Head (2) - Custom Item (Assuming you want this to be named "Golden Head")
        ItemStack goldenHead = new ItemStack(Material.GOLDEN_APPLE, 2);
        ItemMeta goldenHeadMeta = goldenHead.getItemMeta();
        goldenHeadMeta.setDisplayName("Golden Head");
        goldenHead.setItemMeta(goldenHeadMeta);
        inventory.addItem(goldenHead);

        // Another Water Bucket
        ItemStack secondWaterBucket = new ItemStack(Material.WATER_BUCKET);
        inventory.addItem(secondWaterBucket);

        // Another Oak Planks (64)
        if(playerData.getMaterialSelect().isEmpty()) {
            ItemStack oakPlanks = new ItemStack(Material.OAK_PLANKS, 64);
            inventory.addItem(oakPlanks);
        } else {
            String material = playerData.getMaterialSelect();
            Material mat = Material.getMaterial(material);
            assert mat != null;
            ItemStack materialItem = new ItemStack(mat, 64);
            inventory.addItem(materialItem);
        }

        // Another Lava Bucket
        ItemStack secondLavaBucket = new ItemStack(Material.LAVA_BUCKET);
        inventory.addItem(secondLavaBucket);

        // Steak (16)
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 16);
        inventory.addItem(steak);

        // Arrows (16)
        ItemStack arrow = new ItemStack(Material.ARROW, 16);
        inventory.addItem(arrow);


        return inventory; // Return the complete kit
    }
}
