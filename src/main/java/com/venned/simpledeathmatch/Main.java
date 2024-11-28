package com.venned.simpledeathmatch;

import com.venned.simpledeathmatch.build.Arena;
import com.venned.simpledeathmatch.build.ChunkProfile;
import com.venned.simpledeathmatch.commands.*;
import com.venned.simpledeathmatch.gui.BlockSetMenu;
import com.venned.simpledeathmatch.gui.GlowColorMenu;
import com.venned.simpledeathmatch.gui.JoinGameMenu;
import com.venned.simpledeathmatch.gui.StatsMenu;
import com.venned.simpledeathmatch.listeners.DurabilityProtectionListener;
import com.venned.simpledeathmatch.listeners.PlayerDataListeners;
import com.venned.simpledeathmatch.listeners.PlayerListeners;
import com.venned.simpledeathmatch.manager.ArenaManager;
import com.venned.simpledeathmatch.manager.PlayerDataManager;
import com.venned.simpledeathmatch.messenger.SpigotRedisClient;
import com.venned.simpledeathmatch.placeholders.DeathmatchPlaceholders;
import com.venned.simpledeathmatch.runnable.PlayerTickRunnable;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener  {

    static Main instance;
    boolean available = true;
    SpigotRedisClient spigotRedisClient;
    private ArenaManager arenaManager;
    private PlayerDataManager playerDataManager;
    private GlowColorMenu glowColorMenu;
    private DeathmatchPlaceholders placeholders;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        playerDataManager = new PlayerDataManager(this);

        arenaManager = new ArenaManager(this);


        getServer().getPluginManager().registerEvents(this, this);

        spigotRedisClient = new SpigotRedisClient();
        spigotRedisClient.connectToBungee(getConfig().getString("server"), arenaManager);

        new PlayerTickRunnable(Main.getInstance().getArenaManager()).runTaskTimer(Main.getInstance(), 0L, 1L); // Se ejecuta cada tick

        glowColorMenu = new GlowColorMenu(getConfig().getStringList("glow-colors"), playerDataManager);
        BlockSetMenu blockSetMenu = new BlockSetMenu(getConfig().getStringList("set-blocks"), playerDataManager);
        JoinGameMenu joinGameMenu = new JoinGameMenu(spigotRedisClient);
        StatsMenu statsMenu = new StatsMenu(playerDataManager);

        getCommand("check").setExecutor(new MainCommand(spigotRedisClient));
        getCommand("available").setExecutor(new AvailableCommand());
        getCommand("edit").setExecutor(new EditCommand(arenaManager));
        getCommand("play").setExecutor(new PlayCommand(joinGameMenu));
        getCommand("editkit").setExecutor(new KitEditCommand());
        getServer().getPluginManager().registerEvents(new PlayerListeners(arenaManager, spigotRedisClient, glowColorMenu, blockSetMenu, statsMenu, joinGameMenu), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListeners(this), this);
        getServer().getPluginManager().registerEvents(glowColorMenu, this);
        getServer().getPluginManager().registerEvents(blockSetMenu, this);
        getServer().getPluginManager().registerEvents(joinGameMenu, this);
        getServer().getPluginManager().registerEvents(statsMenu, this);
        getServer().getPluginManager().registerEvents(new DurabilityProtectionListener(), this);

        placeholders = new DeathmatchPlaceholders(this, arenaManager, playerDataManager);


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders.register();
            getLogger().info("Placeholders de Deathmatch registrados con PlaceholderAPI.");
        }

        boolean isLobby = getConfig().getBoolean("isLobby", false);
        if(isLobby) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                placeholders.updateTopWinsCache();
                getLogger().info("Top Wins cache actualizado.");
            }, 0L, 1200L); // 1200 ticks = 60 segundos
        }

    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }



    @Override
    public void onDisable() {

        for(Arena arena : getArenaManager().getArenas().values()){
            for(ChunkProfile chunk_profile : arena.getChunkSave()){
                chunk_profile.regenerate(Main.getInstance());
            }
        }
    }

    public SpigotRedisClient getSpigotRedisClient() {
        return spigotRedisClient;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public static Main getInstance() {
        return instance;
    }
}

