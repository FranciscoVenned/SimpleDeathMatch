package com.venned.simpledeathmatch.build;

import com.venned.simpledeathmatch.Main;
import com.venned.simpledeathmatch.enums.Game;
import com.venned.simpledeathmatch.enums.GameType;
import com.venned.simpledeathmatch.utils.EquipGame;
import com.venned.simpledeathmatch.utils.TeamUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Arena {

    Game game;
    GameType gameType;
    World world;
    Location[] spawn_points;
    Location[] supply_drops;
    Location lobby;
    List<Player> players;
    Set<ChunkProfile> chunk_save;
    int amount_drop;

    int time_supply;
    long last_supply;

    List<BukkitTask> task_supply;

    private List<Player[]> teams; // Lista de equipos
    private static final int TEAM_SIZE = 2;

    List<Location> chest_Location = new ArrayList<>();

    Set<Player> playersDeath = new HashSet<>();

    public Arena(World world, Location[] spawn_points, Location[] supply_drops, Location lobby, GameType gameType, int time_supply, int amount_drop) {
        this.world = world;
        this.spawn_points = spawn_points;
        this.supply_drops = supply_drops;
        this.lobby = lobby;
        this.players = new ArrayList<Player>();
        this.gameType = gameType;
        this.chunk_save = new HashSet<>(); // Inicializar aquí
        this.time_supply = time_supply;

        this.task_supply = new ArrayList<>();
        this.amount_drop = amount_drop;

        if (gameType == GameType.DUO) {
            teams = new ArrayList<>();
            initializeTeams();
        }
    }

    public void setSupplyDrops(Location[] supply_drops) {
        this.supply_drops = supply_drops;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<ChunkProfile> getChunkSave() {
        return chunk_save;
    }

    public Game getGame() {
        return game;
    }

    public World getWorld() {
        return world;
    }

    public GameType getGameType() {
        return gameType;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String gameType(){
        return gameType.name();
    }

    public Location getLobby() {
        return lobby;
    }

    public Location[] getSpawnPoints() {
        return spawn_points;
    }

    public Location[] getSupplyDrops() {
        return supply_drops;
    }

    private void initializeTeams() {
        for (int i = 0; i < 4; i++) {
            teams.add(new Player[TEAM_SIZE]);
        }
    }

    public List<Player[]> getTeams() {
        return teams;
    }

    public int getPlayerTeamIndex(Player player) {
        for (int i = 0; i < teams.size(); i++) {
            Player[] team = teams.get(i);
            if (team[0] == player || team[1] == player) {
                return i + 1;
            }
        }
        return -1;
    }

    public String getTeammateName(Player player) {
        if(teams != null) {
            for (Player[] team : teams) {
                if (team[0] == player) {
                    return (team[1] != null) ? team[1].getName() : ""; // Devuelve el nombre del compañero
                } else if (team[1] == player) {
                    return (team[0] != null) ? team[0].getName() : ""; // Devuelve el nombre del compañero
                }
            }
            return ""; // Retorna null si el jugador no está en ningún equipo
        }
        return ""; // Retorna null si el jugador no está en ningún equipo
    }

    public boolean addPlayersToTeam(List<Player> players) {
        if (players.size() != 2) {
            throw new IllegalArgumentException("La lista de jugadores debe contener exactamente dos jugadores.");
        }

        if (gameType == GameType.DUO) {
            for (Player[] team : teams) {
                if (team[0] == null && team[1] == null) {
                    // Agregar ambos jugadores al equipo
                    team[0] = players.get(0);
                    team[1] = players.get(1);

                    // Mensajes y teletransportación
                    int teamNumber = teams.indexOf(team) + 1;
                    for (Player player : players) {
                        player.sendMessage("§aTe has unido al equipo " + teamNumber);
                        teleportLobby(player);
                    }
                    return true;
                }
            }
            for (Player player : players) {
                player.sendMessage("§cTodos los equipos están llenos.");
            }
            return false;
        }
        return false;
    }

    public boolean addPlayerToTeam(Player player) {
        if (gameType == GameType.DUO) {
            for (Player[] team : teams) {
                if (team[0] == null) {
                    team[0] = player;
                   // TeamUtils.setPlayerTeam(player, String.valueOf((teams.indexOf(team) + 1)), this);
                    player.sendMessage("§aYou have joined the team " + (teams.indexOf(team) + 1));
                    addPlayer(player);
                    return true;
                } else if (team[1] == null) {
                    team[1] = player;
                   // TeamUtils.setPlayerTeam(player, String.valueOf((teams.indexOf(team) + 1)), this);
                    player.sendMessage("§aYou have joined the team " + (teams.indexOf(team) + 1));
                    addPlayer(player);
                    return true;
                }
            }
            player.sendMessage("§cTodos los equipos están llenos.");
            return false;
        }
        return false;
    }


    public void removePlayerFromTeam(Player player) {
        for (Player[] team : teams) {
            for (int i = 0; i < team.length; i++) {
                if (team[i] != null && team[i].equals(player)) {
                    team[i] = null;

                    TeamUtils.removePlayerTeam(player, String.valueOf((teams.indexOf(team) + 1)));

                    player.sendMessage("§cHas salido de tu equipo.");
                    return;
                }
            }
        }
    }

    public void clearTeams() {
        for (Player[] team : teams) {
            Arrays.fill(team, null);
        }
    }

    public boolean hasSpaceForOnePlayer() {
        for (Player[] team : teams) {
            if (team[0] == null || team[1] == null) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEmptyTeamForTwoPlayers() {
        for (Player[] team : teams) {
            if (team[0] == null && team[1] == null) {
                return true;
            }
        }
        return false;
    }

    public void teleportLobby(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(lobby);
                if ((getGameType() == GameType.SOLO && players.size() == 2) ||
                        (getGameType() == GameType.DUO && players.size() == 4)) {
                    startCountdown();
                }
            }
        }.runTask(Main.getInstance());
    }

    public void addPlayer(Player player) {
        player.getInventory().clear();

        player.setHealth(20);
        player.setFoodLevel(20);

        player.sendMessage("§c§l(!) §7You have entered the Game");
        players.add(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(lobby);
                if (getGameType() == GameType.SOLO && players.size() == 4) {
                    startCountdown();
                } else if (getGameType() == GameType.DUO && countFullTeams() >= 4) {
                    startCountdown();
                }
            }
        }.runTask(Main.getInstance());
    }

    private void startCountdown() {
        new BukkitRunnable() {
            int countdown = 10; // Contador de 10 segundos

            @Override
            public void run() {
                // Verificar que se mantenga la cantidad mínima de jugadores
                if ((getGameType() == GameType.SOLO && players.size() < 4) ||
                        (getGameType() == GameType.DUO && players.size() < 8)) {
                    cancel(); // Detener el conteo si no hay suficientes jugadores
                    for (Player player : players) {
                        player.sendMessage("§c§l(!) §cNot enough players, game start canceled.");
                    }
                    return;
                }

                // Notificar a los jugadores el tiempo restante
                for (Player player : players) {
                    player.sendMessage("§e§lStarting in: " + countdown + " seconds!");
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.5f, 1.5f);
                }

                // Si el conteo llega a 0, iniciar el juego
                if (countdown == 0) {
                    cancel();
                    startGame(); // Llamar a la función para iniciar el juego
                }

                countdown--;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L); // Ejecutar cada segundo
    }

    public void startGame() {
        setGame(Game.RUNNING);


        if (getGameType() == GameType.SOLO) {
            for (int i = 0; i < players.size(); i++) {
                players.get(i).teleport(spawn_points[i]);
            }
        } else if (getGameType() == GameType.DUO) {
            for (int i = 0; i < teams.size(); i++) {
                Player[] team = teams.get(i);
                Location teamSpawn = spawn_points[i];
                for (Player player : team) {
                    if(player != null) {

                        player.teleport(teamSpawn);
                    }
                }
            }
        }

        for (Player player : players) {

            player.setGameMode(GameMode.SURVIVAL);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()); // Set current health to max
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3 * 20, 5));

            PlayerData playerData = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId());
            if(playerData != null){
                playerData.setLastHit(System.currentTimeMillis());
            }

            player.sendMessage("§c§l(!) §cThe game has started!");

            // Dar el kit de juego al jugador
            Inventory kitItems = EquipGame.kitGame(player, false);
            int maxSlots = Math.min(kitItems.getSize(), 41); // Limitar a 41 o el tamaño real del inventario
            for (int i = 0; i < maxSlots; i++) {
                player.getInventory().setItem(i, kitItems.getItem(i));
            }

            // Equipar al jugador con armadura de diamante
            player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getGame() != Game.RUNNING) {
                    cancel();
                    return;
                }

                if(getGameType() == GameType.SOLO) {
                    if (players.size() <= 1) {
                        Player winner = players.size() == 1 ? players.get(0) : null;
                        if (winner != null) {
                            winner.sendMessage("§a§l(!) §7You are the last player standing! You win!");
                        }
                        endGame();
                        cancel();
                    }
                } else if (getGameType() == GameType.DUO) {
                    // Comprobar si queda solo un equipo con jugadores vivos
                    int activeTeams = 0;
                    Player lastPlayerInTeam = null;
                    for (Player[] team : teams) {
                        int playersInTeam = 0;
                        for (Player player : team) {
                            if (players.contains(player)) { // El jugador sigue en la partida
                                playersInTeam++;
                                lastPlayerInTeam = player;
                            }
                        }
                        if (playersInTeam > 0) {
                            activeTeams++;
                        }
                    }
                    if (activeTeams == 1 && lastPlayerInTeam != null) {
                        for (Player member : players) {
                            if (players.contains(member)) {
                                member.sendMessage("§a§l(!) §7Your team is the last one standing! You win!");
                            }
                        }
                        endGame();
                        cancel();
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getGame() != Game.RUNNING) {
                    cancel();
                    return;
                }

                spawnSupplyDrop();
            }
        }.runTaskTimer(Main.getInstance(), 0L, time_supply * 20L);
    }

    private void spawnSupplyDrop() {
        // Selecciona una ubicación aleatoria de supply_drops



        Location targetLocation = supply_drops[(int) (Math.random() * supply_drops.length)];
        Location dropLocation = targetLocation.clone().add(0, 90, 0); // Crear la ubicación inicial del drop con +50 de altura

        chest_Location.add(targetLocation);

        Set<Chunk> chunksToCheck = new HashSet<>();
        Chunk chunk = dropLocation.getChunk();
        if (getChunkSave().stream().noneMatch(c -> c.getChunk().equals(chunk))) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Chunk adjacentChunk = chunk.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
                    chunksToCheck.add(adjacentChunk);
                }
            }
        }

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            for (Chunk chunks : chunksToCheck) {
                ChunkProfile chunkProfile = new ChunkProfile(chunks);
                getChunkSave().add(chunkProfile);
            }
        });


        for (Player player : players) {
            player.sendMessage("§e§l(!) Throwing drop in " + targetLocation.getBlockX() + ", " + targetLocation.getBlockY() + ", " + targetLocation.getBlockZ());
        }

        // Crear un FallingBlock que simula el suministro como un cofre
        FallingBlock supplyDrop = world.spawnFallingBlock(dropLocation, Material.CHEST.createBlockData());
        supplyDrop.setDropItem(false); // Eliminar cuando llegue a la ubicación destino

        BukkitTask dropTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Verificar si el supply drop ha alcanzado la ubicación objetivo
                if (supplyDrop.getLocation().getY() <= targetLocation.getY()) {
                    // Fijar el cofre en la ubicación objetivo
                    supplyDrop.remove(); // Eliminar el FallingBlock
                    world.getBlockAt(targetLocation).setType(Material.CHEST); // Crear cofre en destino

                    // Avisar a los jugadores
                    for (Player player : players) {
                        player.sendMessage("§e§l(!) ¡A supply drop has landed on " + targetLocation.getBlockX() + ", " + targetLocation.getBlockY() + ", " + targetLocation.getBlockZ() + "!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.5f);
                    }

                    Inventory chestInventory = ((Chest) world.getBlockAt(targetLocation).getState()).getInventory();
                    fillChestWithRandomLoot(chestInventory);


                    cancel(); // Detener el Runnable
                    return;
                }

                // Agregar partículas y sonido para dar efecto visual
                supplyDrop.teleport(supplyDrop.getLocation().add(0, -1, 0)); // Desciende un bloque
                world.spawnParticle(Particle.CLOUD, supplyDrop.getLocation(), 5); // Partículas alrededor del cofre
                world.playSound(supplyDrop.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f); // Sonido para el descenso

            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L); // Ejecutar cada segundo (20 ticks)

        task_supply.add(dropTask); // Añadir la tarea a la lista para control
    }


    public void endGame(){
        setGame(Game.END);
        cancelTasks();
        String lobby_TP = Main.getInstance().getConfig().getString("server-LOBBY");

        for(Player player : players){
            player.sendTitle(ChatColor.GOLD + "¡Victory!", ChatColor.GREEN + "¡Congratulations for winning!", 10, 60, 10);
        }

        for(Player player : getPlayersDeath()){
            player.sendTitle(ChatColor.GOLD + "¡DEFEAT!", ChatColor.GREEN + "¡You Lose!", 10, 60, 10);
        }




        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {

            for(Player player : getPlayersDeath()){
                Main.getInstance().getSpigotRedisClient().sendMessageToBungee("TELEPORT:" + lobby_TP + ":" + player.getName());
            }
            playersDeath.clear();

            int xpWin = Main.getInstance().getConfig().getInt("xp.WIN");

            for (Player player : players) {

                Inventory inventory = player.getInventory();
                int amountApple = 0;

                for (ItemStack item : inventory) {
                    if(item!= null) {
                        if (item.getType() == Material.GOLDEN_APPLE) {
                            amountApple += item.getAmount();
                        }
                    }
                }


                int currentAmount = Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId()).getGaps();
                Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId()).setGaps(amountApple + currentAmount);



                player.getInventory().clear();
                Main.getInstance().getSpigotRedisClient().sendMessageToBungee("TELEPORT:" + lobby_TP + ":" + player.getName());
                Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId()).incrementWinStreak();
                Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId()).incrementWins();
                Main.getInstance().getPlayerDataManager().getPlayerData(player.getUniqueId()).agregarXP(xpWin);


            }


            players.clear();
            if(getGameType() == GameType.DUO){
                clearTeams();
            }

        }, 80L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {

            for(Location location : chest_Location){
                if(location != null) {

                    int radius = 10;


                    if(location.getWorld() != null) {
                        BlockState state = location.getWorld().getBlockAt(location).getState();
                        state.setType(Material.AIR);
                    }

                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -radius; y <= radius; y++) {
                            for (int z = -radius; z <= radius; z++) {
                                // Calculate the position of each block within the radius
                                Location checkLocation = location.clone().add(x, y, z);
                                BlockState state = checkLocation.getBlock().getState();

                                // Check if the block is a chest and remove it if so
                                if (state.getType() == Material.CHEST) {
                                    state.setType(Material.AIR);
                                    state.update(true, false); // Update the block in the world
                                }
                            }
                        }
                    }
                }
            }


            World world = this.world;
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                }
            }

            for(ChunkProfile chunk_profile : chunk_save){
                chunk_profile.regenerate(Main.getInstance());
            }

            chest_Location.clear();
            chunk_save.clear();
        }, 160L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            setGame(Game.WAITING);
        }, 220L);



    }


    private void fillChestWithRandomLoot(Inventory chestInventory) {
        FileConfiguration config = Main.getInstance().getConfig();
        List<String> lootItems = config.getStringList("supply-drops-loot");
        Random random = new Random();

        int itemsAdded = 0;
        while (itemsAdded < amount_drop && !lootItems.isEmpty()) {
            String lootData = lootItems.get(random.nextInt(lootItems.size()));
            String[] parts = lootData.split(":");
            Material material = Material.getMaterial(parts[0]);
            int quantity = Integer.parseInt(parts[1]);

            if (material != null) {
                ItemStack itemStack = new ItemStack(material, quantity);

                int slot;
                do {
                    slot = random.nextInt(chestInventory.getSize());
                } while (chestInventory.getItem(slot) != null);

                chestInventory.setItem(slot, itemStack);
                itemsAdded++;
            }
        }
    }


    private int countFullTeams() {
        int fullTeams = 0;
        for (Player[] team : teams) {
            if (team[0] != null && team[1] != null) {
                fullTeams++;
            }
        }
        return fullTeams;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void cancelTasks(){
        for(BukkitTask runnable : task_supply){
            runnable.cancel();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
        if(getGameType() == GameType.DUO){
            removePlayerFromTeam(player);
        }

        if(getGame() != Game.RUNNING) {
            player.getInventory().clear();
        }
    }

    public Set<Player> getPlayersDeath() {
        return playersDeath;
    }
}
