package com.venned.simpledeathmatch.listeners;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.manager.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class PlayerDataListeners implements Listener {

    private final PlayerDataManager playerDataManager;
    private final int xpKill;


    public PlayerDataListeners(Main plugin) {
        this.playerDataManager = plugin.getPlayerDataManager();

        this.xpKill = plugin.getConfig().getInt("xp.KILL");

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerDataManager.loadPlayerData(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            PlayerData playerData = playerDataManager.getPlayerData(event.getPlayer().getUniqueId());
            if(playerData == null){
                playerDataManager.getPlayerDataCache().put(event.getPlayer().getUniqueId(), new PlayerData(event.getPlayer().getUniqueId(), 0, 1, 0, 0, 0, 0, 0, new ArrayList<>(), "", "", new ArrayList<>(), 0));
            }
        }, 40L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerDataManager.savePlayerData(event.getPlayer().getUniqueId());
        playerDataManager.removePlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            if (event.getEntity() instanceof Player) {
                playerDataManager.getPlayerData(killer.getUniqueId()).incrementKills();
                playerDataManager.getPlayerData(event.getEntity().getUniqueId()).incrementDeath();
                playerDataManager.getPlayerData(killer.getUniqueId()).incrementCurrentKills();
                playerDataManager.getPlayerData(killer.getUniqueId()).agregarXP(xpKill);

                int regenLevel = Main.getInstance().getConfig().getInt("regeneration-per-kill.level", 3);
                int regenDuration = Main.getInstance().getConfig().getInt("regeneration-per-kill.duration", 5);

                // Apply regeneration effect to the killer
                killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration * 20, regenLevel - 1));

            }
        }
    }

}
