package com.venned.simpledeathmatch.manager;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.Arena;
import com.venned.simpledeathmatch.enums.Game;
import com.venned.simpledeathmatch.enums.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ArenaManager {
    private Map<Integer, Arena> arenas = new HashMap<>();
    private Main plugin;

    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getConfig();

        // Load the arenas from the configuration
        if (config.contains("arenas")) {
            for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
                int arenaId = Integer.parseInt(key);
                GameType gameType = GameType.valueOf(config.getString("arenas." + key + ".type"));
                String worldName = config.getString("arenas." + key + ".world");
                World world = Bukkit.getWorld(worldName);

                // Parse the lobby location
                String[] lobbyParts = config.getString("arenas." + key + ".lobby").split(",");
                Location lobby = new Location(world, Double.parseDouble(lobbyParts[0]), Double.parseDouble(lobbyParts[1]), Double.parseDouble(lobbyParts[2]));

                // Parse spawn points
                Location[] spawnPoints = parseLocations(config.getConfigurationSection("arenas." + key + ".spawn-points"), world);

                // Parse supply drops
                Location[] supplyDrops = parseLocations(config.getConfigurationSection("arenas." + key + ".supply-drops"), world);


                int time_supplu = config.getInt("arenas."+ key + ".time_drop");

                int amount_drop = config.getInt("arenas."+ key + ".amount_drop");

                // Create the arena instance and add it to the map
                Arena arena = new Arena(world, spawnPoints, supplyDrops, lobby, gameType, time_supplu, amount_drop);
                arena.setGame(Game.WAITING);
                arenas.put(arenaId, arena);
                System.out.println("Loaded Arena: " + arenaId + " of type " + gameType);
            }
        } else {
            System.err.println("No arenas found in the configuration!");
        }
    }

    public void updateLobbyLocation(int arenaId, Location newLobby) {
        Arena arena = getArena(arenaId);
        if (arena != null) {
            arena.setLobby(newLobby);

            // Update the configuration
            plugin.getConfig().set("arenas." + arenaId + ".lobby", formatLocation(newLobby));
            plugin.saveConfig();
        }
    }

    // Method to update a specific spawn location
    public void updateSpawnLocation(int arenaId, int spawnId, Location newSpawn) {
        Arena arena = getArena(arenaId);
        if (arena != null) {
            Location[] spawns = arena.getSpawnPoints();
            if (spawnId >= 1 && spawnId <= spawns.length) {
                spawns[spawnId - 1] = newSpawn;

                // Update the configuration
                plugin.getConfig().set("arenas." + arenaId + ".spawn-points." + spawnId, formatLocation(newSpawn));
                plugin.saveConfig();
            }
        }
    }

    public void updateSupplyDropLocation(int arenaId, int supplyDropId, Location newSupplyDrop) {
        Arena arena = getArena(arenaId);
        if (arena != null) {
            Location[] supplyDrops = arena.getSupplyDrops();

            // Ensure supplyDrops array has enough space for the new supply drop ID
            if (supplyDropId > supplyDrops.length) {
                Location[] newSupplyDrops = new Location[supplyDropId];
                System.arraycopy(supplyDrops, 0, newSupplyDrops, 0, supplyDrops.length);
                supplyDrops = newSupplyDrops;
                arena.setSupplyDrops(supplyDrops); // Update the arena supply drops array
            }

            // Update or add the new supply drop location
            supplyDrops[supplyDropId - 1] = newSupplyDrop;

            // Update the configuration
            plugin.getConfig().set("arenas." + arenaId + ".supply-drops." + supplyDropId, formatLocation(newSupplyDrop));
            plugin.saveConfig();
        }
    }

    private String formatLocation(Location location) {
        return location.getX() + "," + location.getY() + "," + location.getZ();
    }

    private Location[] parseLocations(ConfigurationSection section, World world) {
        Location[] locations = new Location[section.getKeys(false).size()];
        int index = 0;

        for (String key : section.getKeys(false)) {
            String[] parts = section.getString(key).split(",");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            locations[index++] = new Location(world, x, y, z);
        }

        return locations;
    }

    public Map<Integer, Arena> getArenas() {
        return arenas;
    }

    public Arena getArena(int id) {
        return arenas.get(id);
    }
}
