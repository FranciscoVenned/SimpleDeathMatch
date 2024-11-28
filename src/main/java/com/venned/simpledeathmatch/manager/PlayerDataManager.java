package com.venned.simpledeathmatch.manager;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.PlayerData;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;

public class PlayerDataManager {

    private final Main plugin;
    private final HashMap<UUID, PlayerData> playerDataCache = new HashMap<>();
    private Connection connection;

    public PlayerDataManager(Main plugin) {
        this.plugin = plugin;
        connectToDatabase();
    }

    private void connectToDatabase() {
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String database = plugin.getConfig().getString("database.database");
        String username = plugin.getConfig().getString("database.username");
        String password = plugin.getConfig().getString("database.password");

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            plugin.getLogger().info("Conectado a la base de datos MySQL.");
            createTableIfNotExists(); // Llamada para crear la tabla si no existe


        } catch (SQLException e) {
            plugin.getLogger().severe("No se pudo conectar a la base de datos MySQL: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "xp INT DEFAULT 0, " +
                "stars INT DEFAULT 0, " +
                "kills INT DEFAULT 0, " +
                "deaths INT DEFAULT 0, " +
                "winStreak INT DEFAULT 0, " +
                "bestWinStreak INT DEFAULT 0, " +
                "gaps INT DEFAULT 0, " +
                "colors_buy TEXT, " + // Use TEXT for storing serialized list
                "color_select VARCHAR(16) DEFAULT ''," + // Length depends on your needs
                "materials_buy TEXT, " +
                "materials_select VARCHAR(16) DEFAULT ''," +
                "wins INT DEFAULT 0," +
                "kit_editor LONGTEXT" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            plugin.getLogger().info("Tabla 'player_data' verificada o creada con éxito.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al crear/verificar la tabla 'player_data': " + e.getMessage());
        }
    }

    public void loadPlayerData(UUID uuid) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_data WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                List<String> colorsBuy = deserializeColors(resultSet.getString("colors_buy"));
                List<String> materialsBuy = deserializeColors(resultSet.getString("materials_buy"));
                String kitSerialized = resultSet.getString("kit_editor");


                PlayerData playerData = new PlayerData(
                        uuid,
                        resultSet.getInt("xp"),
                        resultSet.getInt("stars"),
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths"),
                        resultSet.getInt("winStreak"),
                        resultSet.getInt("bestWinStreak"),
                        resultSet.getInt("gaps"),
                        colorsBuy,
                        resultSet.getString("color_select"),
                        resultSet.getString("materials_select"),
                        materialsBuy,
                        resultSet.getInt("wins")
                );

                Inventory kitInventory = plugin.getServer().createInventory(null, 45); // Ejemplo de inventario de tamaño 36
                deserializeInventory(kitSerialized, kitInventory);
                playerData.setKitInventory(kitInventory);


                playerDataCache.put(uuid, playerData);

            } else {
                playerDataCache.put(uuid, new PlayerData(uuid, 0, 1, 0, 0, 0, 0, 0, new ArrayList<>(), "", "", new ArrayList<>(), 0));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al cargar los datos del jugador: " + e.getMessage());
        }
    }

    public HashMap<UUID, PlayerData> getPlayerDataCache() {
        return playerDataCache;
    }

    public void savePlayerData(UUID uuid) {
        PlayerData playerData = playerDataCache.get(uuid);
        if (playerData == null) return;

        try {
            String serializedKit = serializeInventory(playerData.getKitInventory());


            PreparedStatement statement = connection.prepareStatement(
                    "REPLACE INTO player_data (uuid, xp, stars, kills, deaths, winStreak, bestWinStreak, gaps, colors_buy, color_select, materials_buy, materials_select, wins, kit_editor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?)"
            );
            statement.setString(1, playerData.getUuid().toString());
            statement.setInt(2, playerData.getXp());
            statement.setInt(3, playerData.getStars());
            statement.setInt(4, playerData.getKills());
            statement.setInt(5, playerData.getDeaths());
            statement.setInt(6, playerData.getWinStreak());
            statement.setInt(7, playerData.getBestWinStreak());
            statement.setInt(8, playerData.getGaps());
            statement.setString(9, serializeColors(playerData.getColorsBuy()));
            statement.setString(10, playerData.getColorSelect());
            statement.setString(11, serializeColors(playerData.getMaterialsBuy()));
            statement.setString(12, playerData.getMaterialSelect());
            statement.setInt(13, playerData.getWins());
            statement.setString(14, serializedKit);

            statement.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al guardar los datos del jugador: " + e.getMessage());
        }
    }

    public List<PlayerData> getTopKills(int limit) {
        return getTopByStat("kills", limit);
    }

    public List<PlayerData> getTopStars(int limit) {
        return getTopByStat("stars", limit);
    }

    public List<PlayerData> getTopBestWinstreak(int limit) {
        return getTopByStat("bestWinStreak", limit);
    }

    private List<PlayerData> getTopByStat(String stat, int limit) {
        List<PlayerData> topPlayers = new ArrayList<>();
        String query = "SELECT * FROM player_data ORDER BY " + stat + " DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                PlayerData playerData = new PlayerData(
                        uuid,
                        resultSet.getInt("xp"),
                        resultSet.getInt("stars"),
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths"),
                        resultSet.getInt("winStreak"),
                        resultSet.getInt("bestWinStreak"),
                        resultSet.getInt("gaps"),
                        deserializeColors(resultSet.getString("colors_buy")),
                        resultSet.getString("color_select"),
                        resultSet.getString("materials_select"),
                        deserializeColors(resultSet.getString("materials_buy")),
                        resultSet.getInt("wins")
                );
                topPlayers.add(playerData);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error retrieving top players by " + stat + ": " + e.getMessage());
        }

        return topPlayers;
    }




        public List<PlayerData> getTopWins(int limit) {
        List<PlayerData> topWins = new ArrayList<>();
        String query = "SELECT * FROM player_data ORDER BY wins DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                List<String> colorsBuy = deserializeColors(resultSet.getString("colors_buy"));
                List<String> materialsBuy = deserializeColors(resultSet.getString("materials_buy"));
                String kitSerialized = resultSet.getString("kit_editor");

                PlayerData playerData = new PlayerData(
                        uuid,
                        resultSet.getInt("xp"),
                        resultSet.getInt("stars"),
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths"),
                        resultSet.getInt("winStreak"),
                        resultSet.getInt("bestWinStreak"),
                        resultSet.getInt("gaps"),
                        colorsBuy,
                        resultSet.getString("color_select"),
                        resultSet.getString("materials_select"),
                        materialsBuy,
                        resultSet.getInt("wins")
                );

                Inventory kitInventory = plugin.getServer().createInventory(null, 45);
                deserializeInventory(kitSerialized, kitInventory);
                playerData.setKitInventory(kitInventory);

                topWins.add(playerData);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener el top de wins: " + e.getMessage());
        }

        return topWins;
    }

    public String serializeInventory(Inventory inventory) {
        StringJoiner serialized = new StringJoiner(";");

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                // Formato: material:data:cantidad:slot
                serialized.add(item.getType() + ":" + item.getAmount() + ":" + i);
            }
        }
        return serialized.toString();
    }

    public void deserializeInventory(String serializedInventory, Inventory inventory) {
        if (serializedInventory == null || serializedInventory.isEmpty()) return;

        String[] items = serializedInventory.split(";");
        for (String itemData : items) {
            String[] parts = itemData.split(":");

            Material material = Material.valueOf(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            int slot = Integer.parseInt(parts[2]);

            ItemStack item = new ItemStack(material, amount);
            inventory.setItem(slot, item);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public void removePlayerData(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    private String serializeColors(List<String> colors) {
        return String.join(",", colors); // Serialize as a comma-separated string
    }

    private List<String> deserializeColors(String colors) {
        if (colors == null || colors.isEmpty()) return new ArrayList<>();
        return List.of(colors.split(","));
    }
}
