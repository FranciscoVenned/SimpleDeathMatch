package com.venned.simpledeathmatch.build;

import com.venned.simpledeathmatch.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class PlayerData {

    UUID uuid;
    int xp;
    int stars;
    int kills;
    int deaths;
    int winStreak;
    int bestWinStreak;
    int gaps;
    List<String> colors_buy;
    String color_select;
    List<String> materials_buy;
    String material_select;
    int wins;
    Inventory kit_inventory;

    long last_hit;
    int current_kills;

    public PlayerData(UUID uuid, int xp, int stars, int kills, int deaths, int winStreak, int bestWinStreak, int gaps, List<String> colors_buy, String color_select, String material_select, List<String> materials_buy, int wins) {
        this.uuid = uuid;
        this.xp = xp;
        this.stars = stars;
        this.kills = kills;
        this.deaths = deaths;
        this.winStreak = winStreak;
        this.bestWinStreak = bestWinStreak;
        this.gaps = gaps;
        this.colors_buy = colors_buy;
        this.color_select = color_select;
        this.materials_buy = materials_buy;
        this.material_select = material_select;
        this.wins = wins;
        this.kit_inventory = Bukkit.createInventory(null, 45);

        this.current_kills = 0;

    }

    public int getTotalGames(){
        return this.deaths + this.wins;
    }

    public int getCurrentKills() {
        return current_kills;
    }

    public void incrementCurrentKills(){
        this.current_kills++;
    }

    public Inventory getKitInventory() {
        return kit_inventory;
    }

    public void setKitInventory(Inventory kit_inventory) {
        this.kit_inventory = kit_inventory;
    }

    public void setMaterialSelect(String material_select) {
        this.material_select = material_select;
    }

    public void setLastHit(long last_hit) {
        this.last_hit = last_hit;
    }

    public long getLastHit() {
        return last_hit;
    }

    public void incrementWins(){
        this.wins++;
    }

    public int getWins() {
        return wins;
    }

    public List<String> getMaterialsBuy() {
        return materials_buy;
    }

    public String getMaterialSelect() {
        return material_select;
    }

    public List<String> getColorsBuy() {
        return colors_buy;
    }

    public String getColorSelect() {
        return color_select;
    }

    public int getXp() {
        return xp;
    }

    public void incrementWinStreak(){
        this.winStreak++;
    }

    public void incrementKills(){
        this.kills += 1;
    }

    public void incrementDeath(){
        this.deaths += 1;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public int getBestWinStreak() {
        return bestWinStreak;
    }

    public void setColorSelect(String color_select) {
        this.color_select = color_select;
    }

    public void setBestWinStreak(int bestWinStreak) {
        this.bestWinStreak = bestWinStreak;
    }

    public int getGaps() {
        return gaps;
    }

    public void setGaps(int gaps) {
        this.gaps = gaps;
    }

    public int calcularXPParaSiguienteEstrella(int estrellasActuales) {
        int xpParaProximaEstrella;

        if (estrellasActuales == 1) {
            // Requiere menos XP para alcanzar la segunda estrella
            xpParaProximaEstrella = Main.getInstance().getConfig().getInt("levelSystem.forSecondStar");
        } else {
            // Incremento de XP base para estrellas adicionales
            xpParaProximaEstrella = Main.getInstance().getConfig().getInt("levelSystem.forNextStar") * (estrellasActuales - 1);

            // Incremento adicional cada cinco estrellas
            int dificultadExtra = (estrellasActuales / 5) * Main.getInstance().getConfig().getInt("levelSystem.everyFiveStarsDifficultyAdder");

            // Límite de dificultad para no hacer el incremento infinito
            int limiteDificultad = Main.getInstance().getConfig().getInt("levelSystem.starAmountDifficultyLimit");
            if (estrellasActuales <= limiteDificultad) {
                xpParaProximaEstrella += dificultadExtra;
            } else {
                // Si se supera el límite, no se agrega dificultad extra
                xpParaProximaEstrella += Main.getInstance().getConfig().getInt("levelSystem.everyFiveStarsDifficultyAdder") * limiteDificultad;
            }
        }

        return xpParaProximaEstrella;
    }


    public void agregarXP(int xp_gained) {
        this.xp += xp_gained;
        int xpNecesario = calcularXPParaSiguienteEstrella(getStars());
        while (getXp() >= xpNecesario) {
            // Subir de nivel: Aumentar estrellas y restar el XP consumido
            this.xp -= xpNecesario;
            this.stars++;
            // Recalcular XP necesario para la próxima estrella
            xpNecesario = calcularXPParaSiguienteEstrella(getStars());
        }
    }

    public String obtenerFormatoEstrella() {
        String formato = "&7[%stars%★]"; // Valor por defecto

        for (String key : Main.getInstance().getConfig().getConfigurationSection("starPrestiges").getKeys(false)) {
            int limite = Integer.parseInt(key);
            if (getStars() >= limite) {
                formato = Main.getInstance().getConfig().getString("starPrestiges." + key);
            } else {
                break;
            }
        }

        // Reemplazar el marcador de estrellas y traducir colores
        formato = formato.replace("%stars%", String.valueOf(getStars()));
        return ChatColor.translateAlternateColorCodes('&', formato);
    }

    public int getXpFaltante() {
        int xpNecesario = calcularXPParaSiguienteEstrella(getStars());
        return xpNecesario + getXp();
    }
}
