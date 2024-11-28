package com.venned.simpledeathmatch.runnable;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.Arena;
import com.venned.simpledeathmatch.build.ChunkProfile;
import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.enums.Game;
import com.venned.simpledeathmatch.manager.ArenaManager;
import com.venned.simpledeathmatch.utils.TeamUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;

import java.util.HashSet;
import java.util.Set;

public class PlayerTickRunnable extends BukkitRunnable {

    private final ArenaManager arenaManager;
    private final Main plugin = Main.getInstance();

    public PlayerTickRunnable(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Verificar si el jugador está en alguna arena
            if (arenaManager.getArenas().values().stream().anyMatch(arena -> arena.getPlayers().contains(player))) {
                Arena arena = arenaManager.getArenas().values().stream()
                        .filter(c -> c.getPlayers().contains(player)).findFirst()
                        .orElse(null);

                if (arena != null) {

                    if(arena.getGame() == Game.RUNNING) {
                        if (!player.isDead()) {
                            PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
                            if (playerData != null) {
                                long lastHit = playerData.getLastHit();
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastHit >= 30000) {
                                    ChatColor chatColor = ChatColor.WHITE;
                                    if (!playerData.getColorSelect().isEmpty()) {
                                        chatColor = ChatColor.valueOf(playerData.getColorSelect());
                                    }
                                    TeamUtils.setPlayerGlowColor(player, chatColor);
                                } else {
                                    TeamUtils.unsetPlayerGlowColor(player);
                                }
                            }
                        }
                    }

                    if(arena.getGame() == Game.RUNNING) {
                        Set<Chunk> chunksToCheck = new HashSet<>();

                        Chunk chunk = player.getLocation().getChunk();
                        if (arena.getChunkSave().stream().noneMatch(c -> c.getChunk().equals(chunk))) {
                            for (int dx = -10; dx <= 10; dx++) {
                                for (int dz = -10; dz <= 10; dz++) {
                                    Chunk adjacentChunk = chunk.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
                                    chunksToCheck.add(adjacentChunk);
                                }
                            }
                        }

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            for (Chunk chunks : chunksToCheck) {
                                ChunkProfile chunkProfile = new ChunkProfile(chunks);
                                arena.getChunkSave().add(chunkProfile);
                            }
                        });
                    }
                }
            }
        }
    }
}
