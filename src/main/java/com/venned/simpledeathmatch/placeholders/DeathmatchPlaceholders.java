package com.venned.simpledeathmatch.placeholders;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.Arena;
import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.manager.ArenaManager;
import com.venned.simpledeathmatch.manager.PlayerDataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DeathmatchPlaceholders extends PlaceholderExpansion {

    private final Main plugin;
    private final ArenaManager arenaManager;
    private final PlayerDataManager playerDataManager;

    private List<PlayerData> topWinsCache;
    private List<PlayerData> topKillsCache;
    private List<PlayerData> topStarsCache;
    private List<PlayerData> topBestWinstreakCache;


    public DeathmatchPlaceholders(Main plugin, ArenaManager arenaManager,  PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.playerDataManager = playerDataManager;

        boolean isLobby = Main.getInstance().getConfig().getBoolean("isLobby", false);
        if(isLobby) {
            this.topWinsCache = playerDataManager.getTopWins(10); // Cargar top 10 inicialmente
            this.topKillsCache = playerDataManager.getTopKills(10);
            this.topStarsCache = playerDataManager.getTopStars(10);
            this.topBestWinstreakCache = playerDataManager.getTopBestWinstreak(10);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "deathmatch";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Fran";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.hasPlayedBefore()) return "";

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) return "";

        switch (params.toLowerCase()) {
            case "kills":
                return String.valueOf(playerData.getKills());
            case "deaths":
                return String.valueOf(playerData.getDeaths());
            case "xp":
                return String.valueOf(playerData.getXp());
            case "xp_next":
                return String.valueOf(playerData.getXpFaltante());
            case "winstreak":
                return String.valueOf(playerData.getWinStreak());
            case "bestwinstreak":
                return String.valueOf(playerData.getBestWinStreak());
            case "gaps":
                double gapsAverage = (double)  playerData.getTotalGames() / playerData.getGaps() ;
                return String.valueOf(Math.round(gapsAverage));
            case "totalgaps":
                int totalGaps = playerData.getGaps();
                return String.valueOf(totalGaps);
            case "color_select":
                return playerData.getColorSelect();
            case "material_select":
                return playerData.getMaterialSelect();
            case "stars":
                return playerData.obtenerFormatoEstrella();
            case "team":
                Arena arena2 = arenaManager.getArenas().values().stream()
                        .filter(f->f.getPlayers().contains(player.getPlayer()))
                        .findFirst().orElse(null);
                if(arena2 == null) return "";
                return  (arena2.getTeammateName(player.getPlayer()) != null) ?  String.valueOf(arena2.getTeammateName(player.getPlayer())) : " ";
            case "currentkills":
                return String.valueOf(playerData.getCurrentKills());
            case "playerleft":
                Arena arena = arenaManager.getArenas().values().stream()
                        .filter(f->f.getPlayers().contains(player.getPlayer()))
                        .findFirst().orElse(null);
                if(arena == null) return "";
                return String.valueOf(arena.getPlayers().size());

            default:
                break; // Retorna null si el placeholder no coincide
        }

        if (params.startsWith("win_")) {
            try {
                String rankStr = params.substring(4); // Extraer el número después de "win_"
                int rank = Integer.parseInt(rankStr) - 1; // Convertir a índice (0-based)
                if (rank >= 0 && rank < topWinsCache.size()) {
                    PlayerData topPlayer = topWinsCache.get(rank);
                    UUID topUUID = topPlayer.getUuid();
                    OfflinePlayer topOfflinePlayer = Bukkit.getOfflinePlayer(topUUID);
                    return topOfflinePlayer.getName(); // Mostrar el nombre del jugador
                } else {
                    return "-"; // Retornar "-" si el índice está fuera de los límites
                }
            } catch (NumberFormatException e) {
                return "-"; // Retornar "-" si el formato no es correcto
            }
        }

        if (params.startsWith("winamount_")) {
            try {
                String rankStr = params.substring(10); // Extraer el número después de "winamount_"
                int rank = Integer.parseInt(rankStr) - 1; // Convertir a índice (0-based)
                if (rank >= 0 && rank < topWinsCache.size()) {
                    PlayerData topPlayer = topWinsCache.get(rank);
                    return String.valueOf(topPlayer.getWins()); // Mostrar la cantidad de victorias del jugador
                } else {
                    return "-"; // Retornar "-" si el índice está fuera de los límites
                }
            } catch (NumberFormatException e) {
                return "-"; // Retornar "-" si el formato no es correcto
            }
        }

        if(!handleLeaderboardPlaceholders(params).isEmpty()){
            return handleLeaderboardPlaceholders(params);
        }

        return null;

    }

    private String handleLeaderboardPlaceholders(String params) {
        // Extract leaderboard type and rank
        String[] parts = params.split("_");

        String stat = parts[0];
        int rank;
        try {
            if(parts.length == 2) {
                rank = Integer.parseInt(parts[1]) - 1;
            } else {
                rank = Integer.parseInt(parts[2]) - 1;
            }
        } catch (NumberFormatException e) {
            return "-";
        }


        // Determine leaderboard cache and stat based on placeholder prefix
        List<PlayerData> leaderboard;
        Function<PlayerData, Integer> statGetter;
        switch (stat) {
            case "win": leaderboard = topWinsCache; statGetter = PlayerData::getWins; break;
            case "kill": leaderboard = topKillsCache; statGetter = PlayerData::getKills; break;
            case "star": leaderboard = topStarsCache; statGetter = PlayerData::getStars; break;
            case "winstreak": leaderboard = topBestWinstreakCache; statGetter = PlayerData::getBestWinStreak; break;
            default: return "-";
        }

        if (rank < 0 || rank >= leaderboard.size()) return "-";
        PlayerData topPlayer = leaderboard.get(rank);

        return params.startsWith(stat + "_amount_")
                ? String.valueOf(statGetter.apply(topPlayer)) // e.g., "killamount_1" shows kills
                : Bukkit.getOfflinePlayer(topPlayer.getUuid()).getName(); // e.g., "kill_1" shows player name
    }

    public void updateTopWinsCache() {
        this.topWinsCache = playerDataManager.getTopWins(10); // Actualizar con el top 10
        this.topKillsCache = playerDataManager.getTopKills(10);
        this.topStarsCache = playerDataManager.getTopStars(10);
        this.topBestWinstreakCache = playerDataManager.getTopBestWinstreak(10);
    }
}