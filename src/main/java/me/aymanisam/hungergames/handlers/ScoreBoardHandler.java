package me.aymanisam.hungergames.handlers;

import fr.mrmicky.fastboard.FastBoard;
import me.aymanisam.hungergames.HungerGames;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import static me.aymanisam.hungergames.handlers.CountDownHandler.playersPerTeam;
import static me.aymanisam.hungergames.handlers.GameSequenceHandler.*;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;
import static me.aymanisam.hungergames.listeners.PlayerListener.playerKills;

public class ScoreBoardHandler {
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;

    public static final Map<UUID, FastBoard> boards = new HashMap<>();

    public ScoreBoardHandler(HungerGames plugin, LangHandler langHandler) {
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
    }

    private String getColorCode(int interval, int countdown) {
        if (countdown <= interval / 3) {
            return "§c";
        } else if (countdown <= 2 * interval / 3) {
            return "§e";
        } else {
            return "§a";
        }
    }

    private String formatScore(Player player, String messageKey, int countdown, int interval) {
        int minutes = countdown / 60;
        int seconds = countdown % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        return langHandler.getMessage(player, messageKey, getColorCode(interval, countdown) + timeFormatted);
    }

    public void createBoard(Player player) {
        if (!configHandler.getWorldConfig(player.getWorld()).getBoolean("display-scoreboard")) {
            return;
        }
        FastBoard board = new FastBoard(player);
        if (playersPerTeam == 1) {
            board.updateTitle(langHandler.getMessage(player, "score.name-solo"));
        } else {
            board.updateTitle(langHandler.getMessage(player, "score.name-team"));
        }

        boards.put(player.getUniqueId(), board);
    }

    public void updateBoard(FastBoard board, World world) {
        if (!configHandler.getWorldConfig(world).getBoolean("display-scoreboard")) {
            return;
        }

        FileConfiguration worldConfig = configHandler.getWorldConfig(world);
        int gameTimeConfig = worldConfig.getInt("game-time");
        int borderShrinkTimeConfig = worldConfig.getInt("border.start-time");
        int pvpTimeConfig = worldConfig.getInt("grace-period");
        int chestRefillInterval = worldConfig.getInt("chestrefill.interval");
        int supplyDropInterval = worldConfig.getInt("supplydrop.interval");
        int borderStartSize = worldConfig.getInt("border.size");
        int borderEndSize = worldConfig.getInt("border.final-size");

        int worldTimeLeft = timeLeft.get(world.getName());
        int worldPlayersAliveSize = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>()).size();
        int worldStartingPlayers = startingPlayers.get(world.getName()).size();
        int worldBorderSize = (int) world.getWorldBorder().getSize();
        int borderShrinkTimeLeft = (worldTimeLeft - gameTimeConfig) + borderShrinkTimeConfig;
        int pvpTimeLeft = (worldTimeLeft - gameTimeConfig) + pvpTimeConfig;
        int chestRefillTimeLeft = worldTimeLeft % chestRefillInterval;
        int supplyDropTimeLeft = worldTimeLeft % supplyDropInterval;
        String borderColorCode;

        if (borderStartSize == worldBorderSize) {
            borderColorCode = "§a";
        } else if (borderEndSize == worldBorderSize) {
            borderColorCode = "§c";
        } else {
            borderColorCode = "§e";
        }

        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add(langHandler.getMessage(board.getPlayer(), "score.alive", getColorCode(worldStartingPlayers, worldPlayersAliveSize) + worldPlayersAliveSize));
        Map<Player, Integer> worldPlayerKills = playerKills.computeIfAbsent(world.getName(), k -> new HashMap<>());
        lines.add(langHandler.getMessage(board.getPlayer(), "score.kills", "§c" + worldPlayerKills.computeIfAbsent(board.getPlayer(), k -> 0)));
        lines.add(langHandler.getMessage(board.getPlayer(), "score.border", borderColorCode + worldBorderSize));
        lines.add("");
        lines.add(formatScore(board.getPlayer(), "score.time", worldTimeLeft, gameTimeConfig));

        if (borderShrinkTimeLeft >= 0) {
            lines.add(formatScore(board.getPlayer(), "score.borderShrink", borderShrinkTimeLeft, borderShrinkTimeConfig));
        }

        if (pvpTimeLeft >= 0) {
            lines.add(formatScore(board.getPlayer(), "score.pvp", pvpTimeLeft, pvpTimeConfig));
        }

        lines.add("");
        lines.add(formatScore(board.getPlayer(), "score.chestrefill", chestRefillTimeLeft, chestRefillInterval));
        lines.add(formatScore(board.getPlayer(), "score.supplydrop", supplyDropTimeLeft, supplyDropInterval));

        String teamScoreBoard = getScoreBoardTeam(board.getPlayer(), world);

        if (teamScoreBoard != null) {
            lines.add("");
            lines.add(teamScoreBoard);
        }

        board.updateLines(lines);
    }

    private String getScoreBoardTeam(Player player, World world) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());
        List<Player> worldPlayersAlive = playersAlive.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        if (playersPerTeam > 1) {
            for (List<Player> team : worldTeams) {
                if (team.contains(player)) {
                    for (Player teamMember : team) {
                        if (!teamMember.equals(player)) {
                            String teammateName = teamMember.getName();
                            String colorCode = worldPlayersAlive.contains(teamMember) ? "§a" : "§c";
                            return langHandler.getMessage(player, "score.teammate", colorCode + teammateName);
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    public void removeScoreboard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }
}
