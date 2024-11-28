package com.venned.simpledeathmatch.listeners;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.build.Arena;
import com.venned.simpledeathmatch.build.ChunkProfile;
import com.venned.simpledeathmatch.build.PlayerData;
import com.venned.simpledeathmatch.commands.KitEditCommand;
import com.venned.simpledeathmatch.enums.Game;
import com.venned.simpledeathmatch.gui.BlockSetMenu;
import com.venned.simpledeathmatch.gui.GlowColorMenu;
import com.venned.simpledeathmatch.gui.JoinGameMenu;
import com.venned.simpledeathmatch.gui.StatsMenu;
import com.venned.simpledeathmatch.manager.ArenaManager;
import com.venned.simpledeathmatch.messenger.SpigotRedisClient;
import com.venned.simpledeathmatch.utils.TeamUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class PlayerListeners implements Listener {

    ArenaManager arenaManager;
    SpigotRedisClient spigotRedisClient;
    GlowColorMenu glowColorMenu;
    BlockSetMenu blockSetMenu;
    StatsMenu statsMenu;
    JoinGameMenu joinGameMenu;
    private final Map<UUID, BukkitRunnable> glowTimers = new HashMap<>();

    public PlayerListeners(ArenaManager arenaManager, SpigotRedisClient spigotRedisClient, GlowColorMenu glowColorMenu, BlockSetMenu blockSetMenu, StatsMenu statsMenu, JoinGameMenu joinGameMenu) {
        this.arenaManager = arenaManager;
        this.spigotRedisClient = spigotRedisClient;
        this.glowColorMenu = glowColorMenu;
        this.blockSetMenu = blockSetMenu;
        this.statsMenu = statsMenu;
        this.joinGameMenu = joinGameMenu;
    }

    @EventHandler
    public void onLeaveServer(PlayerQuitEvent event){
        Collection<Arena> arenas = arenaManager.getArenas().values();
        if(arenas.isEmpty()) return;


        for(Arena arena : arenas) {
            arena.getPlayersDeath().remove(event.getPlayer());

            arena.removePlayer(event.getPlayer());
            TeamUtils.unsetPlayerGlowColor(event.getPlayer());

        }

        for (PotionEffect potions : event.getPlayer().getActivePotionEffects()) {
            event.getPlayer().removePotionEffect(potions.getType());
        }

        if (event.getPlayer().hasPotionEffect(PotionEffectType.ABSORPTION)) {
            event.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
        }

        KitEditCommand.players.remove(event.getPlayer());

    }

    @EventHandler
    public void damageTake(EntityDamageEvent event){
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Collection<Arena> arenas = arenaManager.getArenas().values();
            if (arenas.isEmpty()) return;
            for (Arena arena : arenas) {
                if (arena.getPlayers().contains(player)) {
                    if (arena.getGame() == Game.WAITING) {
                        event.setCancelled(true);
                    } else {
                        PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
                        if(playerData != null){
                            if(event.getDamageSource().getCausingEntity() instanceof Player attakcer){
                                PlayerData playerData1 = Main.getInstance().getPlayerDataManager().getPlayerData(attakcer.getUniqueId());
                                playerData1.setLastHit(System.currentTimeMillis());
                            }
                        }
                    }

                }
            }
        }
    }


    @EventHandler
    public void damageTake(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            Collection<Arena> arenas = arenaManager.getArenas().values();
            if (arenas.isEmpty()) return;

            for (Arena arena : arenas) {
                if (arena.getPlayers().contains(player)) {

                    if (event.getDamager() instanceof Player attacker) {
                        PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
                        PlayerData attackerData = Main.getInstance().getPlayerDataManager().getPlayerData(attacker.getUniqueId());

                        if (playerData != null && attackerData != null) {
                            if (arePlayersOnSameTeam(player, attacker, arena)) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean arePlayersOnSameTeam(Player player, Player attacker, Arena arena) {

        if(arena.getTeams() != null) {
            for (Player[] team : arena.getTeams()) {
                if (Arrays.asList(team).contains(player) && Arrays.asList(team).contains(attacker)) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Collection<Arena> arenas = arenaManager.getArenas().values();
        if(arenas.isEmpty()) return;
        for(Arena arena : arenas) {
            if(arena.getPlayers().contains(player)) {
                if(arena.getGame() == Game.WAITING){
                    player.setFoodLevel(20);
                }
            }
        }
    }


    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeathServer(PlayerDeathEvent event){
        Collection<Arena> arenas = arenaManager.getArenas().values();
        if(arenas.isEmpty()) return;
        for(Arena arena : arenas) {
            if(arena.getPlayers().contains(event.getEntity())) {
                Inventory inventory = event.getEntity().getInventory();
                int amountApple = 0;

                for (ItemStack item : inventory) {
                    if(item!= null) {
                        if (item.getType() == Material.GOLDEN_APPLE) {
                            amountApple += item.getAmount();
                        }
                    }
                }


                int currentAmount = Main.getInstance().getPlayerDataManager().getPlayerData(event.getEntity().getUniqueId()).getGaps();
                Main.getInstance().getPlayerDataManager().getPlayerData(event.getEntity().getUniqueId()).setGaps(amountApple + currentAmount);




                arena.getPlayersDeath().add(event.getEntity());
            }
        }

        for(Arena arena : arenas) {
            arena.removePlayer(event.getEntity());

            String lobby = Main.getInstance().getConfig().getString("server-LOBBY");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(event.getEntity().isDead()){
                        event.getEntity().spigot().respawn();
                        event.getEntity().setGameMode(GameMode.SPECTATOR);
                      //  spigotRedisClient.sendMessageToBungee("TELEPORT:" + lobby + ":" + event.getEntity().getName());
                    } else {
                        event.getEntity().setGameMode(GameMode.SPECTATOR);
                      //  spigotRedisClient.sendMessageToBungee("TELEPORT:" + lobby + ":" + event.getEntity().getName());
                    }
                }
            }.runTaskLater(Main.getInstance(), 40);


        }
        PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(event.getEntity().getUniqueId());
        if (playerData != null) {
            int currentWins = playerData.getWinStreak();
            int bestWinStreak = playerData.getBestWinStreak();

            if (currentWins > bestWinStreak) {
                playerData.setBestWinStreak(currentWins);
            }

            playerData.setWinStreak(0);
        }


        TeamUtils.unsetPlayerGlowColor(event.getEntity());
    }

    @EventHandler
    public void onBlockBrea(BlockBreakEvent event){
        Arena arena = arenaManager.getArenas().values().stream()
                .filter(f->f.getPlayers().contains(event.getPlayer()))
                .findFirst().orElse(null);
        if(arena != null){
            if(arena.getGame() == Game.WAITING){
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event){
        Arena arena = arenaManager.getArenas().values().stream()
                .filter(f->f.getPlayers().contains(event.getPlayer()))
                .findFirst().orElse(null);
        if(arena != null){
            Chunk chunk = event.getBlock().getChunk();
            if(arena.getGame() == Game.RUNNING) {
                if (arena.getChunkSave().stream().anyMatch(c -> c.getChunk().equals(chunk))) {
                    ChunkProfile chunkProfile = arena.getChunkSave().stream()
                            .filter(c -> c.getChunk().equals(chunk))
                            .findFirst()
                            .orElse(null);
                    assert chunkProfile != null;
                    chunkProfile.addPlacedBlock(event.getBlock());

                }
            }

        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        boolean isLobby = Main.getInstance().getConfig().getBoolean("isLobby", false);
        if(isLobby){
            event.getPlayer().getInventory().clear();

            Player player = event.getPlayer();
            ItemStack itemStack = new ItemStack(Material.PAPER);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("Set Glow Color");
            itemStack.setItemMeta(itemMeta);

            ItemStack itemStack1 = new ItemStack(Material.BOOK);
            ItemMeta itemMeta1 = itemStack1.getItemMeta();
            itemMeta1.setDisplayName("Set Block Type");
            itemStack1.setItemMeta(itemMeta1);

            ItemStack itemStack2 = new ItemStack(Material.EMERALD);
            ItemMeta itemMeta2 = itemStack2.getItemMeta();
            itemMeta2.setDisplayName("Stats");
            itemStack2.setItemMeta(itemMeta2);

            ItemStack itemStack3 = new ItemStack(Material.REDSTONE);
            ItemMeta itemMeta3 = itemStack3.getItemMeta();
            itemMeta3.setDisplayName("Play");
            itemStack3.setItemMeta(itemMeta3);

            ItemStack itemStack4 = new ItemStack(Material.NETHER_STAR);
            ItemMeta itemMeta4 = itemStack4.getItemMeta();
            itemMeta4.setDisplayName("Kit Editor");
            itemStack4.setItemMeta(itemMeta4);

            player.getInventory().setItem(6, itemStack);
            player.getInventory().setItem(8, itemStack1);
            player.getInventory().setItem(0, itemStack2);
            player.getInventory().setItem(4, itemStack3);
            player.getInventory().setItem(2, itemStack4);
        }
    }



    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        ItemStack itemStack = event.getItem();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
            if(itemStack != null){
                ItemMeta itemMeta = itemStack.getItemMeta();
                if(itemMeta != null) {
                    String display = itemMeta.getDisplayName();

                    if (display.equalsIgnoreCase("Set Glow Color")) {
                        event.getPlayer().swingOffHand();
                        glowColorMenu.openMenu(event.getPlayer());
                    } else if (display.equalsIgnoreCase("Set Block Type")) {
                        blockSetMenu.openMenu(event.getPlayer());
                    } else if (display.equalsIgnoreCase("Stats")) {
                        statsMenu.openMenu(event.getPlayer());
                    } else if (display.equalsIgnoreCase("Play")) {
                        joinGameMenu.openMenu(event.getPlayer());
                    } else if(display.equalsIgnoreCase("Kit Editor")){
                        event.getPlayer().performCommand("editkit edit");
                    }
                }
            }
        }

        if(KitEditCommand.players.contains(event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractInv(InventoryClickEvent event){
        ItemStack itemStack = event.getCurrentItem();
        if(itemStack != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(itemMeta != null) {
                String display = itemMeta.getDisplayName();
                if (display.equalsIgnoreCase("Set Glow Color")) {
                    event.setCancelled(true);
                } else if (display.equalsIgnoreCase("Set Block Type")) {
                    event.setCancelled(true);
                } else if (display.equalsIgnoreCase("Stats")) {
                    event.setCancelled(true);
                } else if (display.equalsIgnoreCase("Play")) {
                    event.setCancelled(true);
                } else if(display.equalsIgnoreCase("Kit Editor")){
                    event.setCancelled(true);
                }
            }
        }
    }




    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        ItemStack itemStack = event.getItemDrop().getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null) {
            String display = itemMeta.getDisplayName();
            if (display.equalsIgnoreCase("Set Glow Color")) {
                event.setCancelled(true);
            } else if (display.equalsIgnoreCase("Set Block Type")) {
                event.setCancelled(true);
            } else if (display.equalsIgnoreCase("Stats")) {
                event.setCancelled(true);
            } else if (display.equalsIgnoreCase("Play")) {
                event.setCancelled(true);
            } else if(display.equalsIgnoreCase("Kit Editor")){
                event.setCancelled(true);
            }
        }

        if(KitEditCommand.players.contains(event.getPlayer())){
            event.setCancelled(true);
        }
    }


    /*

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        String message = event.getMessage();


        new BukkitRunnable() {
            @Override
            public void run() {
                glowColorMenu.openMenu(event.getPlayer());

            }
        }.runTask(Main.getInstance());


    }

     */



}
