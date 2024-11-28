package com.venned.simpledeathmatch.messenger;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.Arena;
import com.venned.simpledeathmatch.enums.Game;
import com.venned.simpledeathmatch.enums.GameType;
import com.venned.simpledeathmatch.manager.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Optional;

public class SpigotRedisClient{
    private Jedis jedisPub; // Publisher
    private Jedis jedisSub; // Subscriber
    private String serverName;
    ArenaManager arenaManager;

    public void connectToBungee(String serverName, ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
        this.serverName = serverName;
        try {
            jedisPub = new Jedis("p-bfus-1.plas.host", 5072);
            jedisSub = new Jedis("p-bfus-1.plas.host", 5072);

            String redisPassword = "382ferterg90rei9gj95j4regi9jerw9ig";
            jedisSub.auth(redisPassword);
            jedisPub.auth(redisPassword);

            if ("PONG".equals(jedisSub.ping())) {
                System.out.println("Redis connection established successfully. (Spigot)");
            } else {
                System.err.println("Error connecting to Redis. (Spigot)");
            }


            new Thread(() -> {
                jedisSub.subscribe(new MessageListener(), "bungee_channel");
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToBungee(String message) {
        try {
            if (jedisPub != null) {
                jedisPub.publish("bungee_channel", message);

            } else {
                System.err.println("Error: Jedis no está conectado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getServerName() {
        return serverName;
    }

    private class MessageListener extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {
            if(message.startsWith("CHECk_AVAILABLE_SERVER")) {
                String[] parts = message.split(":");
                String type = parts[2];
                Arena arena = arenaManager.getArenas().values().stream()
                        .filter(a -> a.gameType().equalsIgnoreCase(type) && a.getGame() == Game.WAITING)
                        .findFirst()
                        .orElse(null);
                if(arena != null){
                    if(arena.getGameType() == GameType.DUO) {
                        if(arena.hasSpaceForOnePlayer()) {
                            if (arena.getGame() == Game.WAITING) {
                                sendMessageToBungee("SERVER_ONLINE:" + getServerName() + ":" + parts[1] + ":" + type);
                            }
                        }
                    } else {
                        if(arena.getPlayers().size() != 4) {
                            if (arena.getGame() == Game.WAITING) {
                                sendMessageToBungee("SERVER_ONLINE:" + getServerName() + ":" + parts[1] + ":" + type);
                            }
                        }
                    }
                }
            }

            if(message.startsWith(getServerName() + ":JOIN_ARENA")){
                String[] parts = message.split(":");
                String playerName = parts[2];
                String type = parts[3];
                Player player = Bukkit.getPlayer(playerName);
                if(player != null){
                    Arena arena = arenaManager.getArenas().values().stream()
                            .filter(a -> a.gameType().equalsIgnoreCase(type) && a.getGame() == Game.WAITING)
                            .findFirst()
                            .orElse(null);
                    if(arena != null){
                        if(arena.getGameType() == GameType.SOLO) {
                            arena.addPlayer(player);
                        } else {
                            arena.addPlayerToTeam(player);
                        }
                    }
                }
            }
        }
    }
}