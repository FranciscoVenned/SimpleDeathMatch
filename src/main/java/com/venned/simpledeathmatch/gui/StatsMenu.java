package com.venned.simpledeathmatch.gui;

import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.manager.PlayerDataManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class StatsMenu implements Listener {

    private final PlayerDataManager playerDataManager;

    public StatsMenu(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void openMenu(Player player) {
        PlayerData stats = playerDataManager.getPlayerData(player.getUniqueId()); // Obtener estadísticas del jugador
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Player Stats");

        // Ítem en el slot 0: Estrellas (Stars)
        ItemStack starsItem = createMenuItem(Material.NETHER_STAR, ChatColor.GOLD + "Stars",
                Collections.singletonList(ChatColor.YELLOW + "Stars: " + ChatColor.WHITE + stats.getStars()));
        inventory.setItem(0, starsItem);

        // Ítem en el slot 1: Victorias (Wins)
        ItemStack winsItem = createMenuItem(Material.GREEN_CONCRETE, ChatColor.GREEN + "Wins",
                Collections.singletonList(ChatColor.YELLOW + "Wins: " + ChatColor.WHITE + stats.getWins()));
        inventory.setItem(1, winsItem);

        // Ítem en el slot 2: Asesinatos (Kills)
        ItemStack killsItem = createMenuItem(Material.DIAMOND_SWORD, ChatColor.RED + "Kills",
                Collections.singletonList(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + stats.getKills()));
        inventory.setItem(2, killsItem);

        // Ítem en el slot 3: Muertes (Deaths)
        ItemStack deathsItem = createMenuItem(Material.SKELETON_SKULL, ChatColor.DARK_GRAY + "Deaths",
                Collections.singletonList(ChatColor.YELLOW + "Deaths: " + ChatColor.WHITE + stats.getDeaths()));
        inventory.setItem(3, deathsItem);

        // Ítem en el slot 4: Racha actual (Current Win Streak)
        ItemStack currentWinStreakItem = createMenuItem(Material.EMERALD, ChatColor.AQUA + "Current Win Streak",
                Collections.singletonList(ChatColor.YELLOW + "Current Streak: " + ChatColor.WHITE + stats.getWinStreak()));
        inventory.setItem(4, currentWinStreakItem);

        // Ítem en el slot 5: Mejor racha (Best Win Streak)
        ItemStack bestWinStreakItem = createMenuItem(Material.DIAMOND, ChatColor.LIGHT_PURPLE + "Best Win Streak",
                Collections.singletonList(ChatColor.YELLOW + "Best Streak: " + ChatColor.WHITE + stats.getBestWinStreak()));
        inventory.setItem(5, bestWinStreakItem);


        double gapsAverage = (double)   stats.getGaps()  / stats.getTotalGames() ;
        String gaps_text = String.valueOf(Math.round(gapsAverage));

        // Ítem en el slot 6: Gaps por partida (Gaps/Game)
        ItemStack gapsPerGameItem = createMenuItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Gaps/Game",
                Collections.singletonList(ChatColor.YELLOW + "Gaps per Game: " + ChatColor.WHITE + gaps_text));
        inventory.setItem(6, gapsPerGameItem);

        // Abrir el inventario para el jugador
        player.openInventory(inventory);
    }

    private ItemStack createMenuItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = org.bukkit.ChatColor.stripColor(event.getView().getTitle());
        if (title.equals("Player Stats")) {
            event.setCancelled(true); // Prevent taking items from the menu

        }
    }
}