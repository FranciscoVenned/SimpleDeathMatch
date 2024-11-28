package com.venned.simpledeathmatch.utils;

import com.venned.simpledeathmatch.build.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class TeamUtils {

    //private static final Scoreboard customScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    public static void setPlayerGlowColor(Player player, ChatColor color) {
        // Get or create a team specifically for this playe
        //
        List<Scoreboard> scoreboards = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            scoreboards.add(p.getScoreboard());
        }

        for(Scoreboard scoreboard : scoreboards) {
                Team team = scoreboard.getTeam(player.getName() + "color");
                if(team == null) {
                 team = scoreboard.registerNewTeam(player.getName() + "color");
                }
                team.setColor(color);
                if(!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }

        }
        player.setGlowing(true);

    }

    public static void setPlayerTeam(Player player, String teamNumber, Arena arena) {
        List<Scoreboard> scoreboards = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            scoreboards.add(p.getScoreboard());
        }

        for(Scoreboard scoreboard : scoreboards) {

            for(Player players : arena.getPlayers()) {

                String team_N = String.valueOf(arena.getPlayerTeamIndex(players));

                Team team = scoreboard.getTeam(player.getName() + team_N);
                if (team == null) {
                    team = scoreboard.registerNewTeam(player.getName() + team_N);
                }
                team.setPrefix("Team " + (team_N) + " | ");

                if (!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }
            }

        }

    }

    public static void removePlayerTeam(Player player, String teamNumber) {
        List<Scoreboard> scoreboards = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            scoreboards.add(p.getScoreboard());
        }

        for(Scoreboard scoreboard : scoreboards) {
            Team team = scoreboard.getTeam(player.getName() + teamNumber);
            if (team != null) {
                team.removeEntry(player.getName());
            }
        }
    }




    public static void unsetPlayerGlowColor(Player player) {
        List<Scoreboard> scoreboards = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            scoreboards.add(p.getScoreboard());
        }

        for(Scoreboard scoreboard : scoreboards) {
            Team team = scoreboard.getTeam(player.getName() + "color");
            if (team != null) {
                team.removeEntry(player.getName());
            }
        }
        player.setGlowing(false);
    }

}
