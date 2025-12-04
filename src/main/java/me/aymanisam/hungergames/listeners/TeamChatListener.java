package me.aymanisam.hungergames.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.aymanisam.hungergames.handlers.TeamsHandler;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.aymanisam.hungergames.HungerGames.isGameStartingOrStarted;
import static me.aymanisam.hungergames.handlers.TeamsHandler.teams;

public class TeamChatListener implements Listener {
    private final TeamsHandler teamsHandler;

    public TeamChatListener(TeamsHandler teamsHandler) {
        this.teamsHandler = teamsHandler;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        if ((isGameStartingOrStarted(sender.getWorld().getName())) && isPlayerInAnyTeam(sender, sender.getWorld()) && teamsHandler.isChatModeEnabled(sender)) {
            List<Player> teammates = teamsHandler.getTeammates(sender, sender.getWorld());

            teammates.add(sender);

            Set<org.bukkit.entity.Player> viewers = new HashSet<>();
            viewers.addAll(teammates);
            event.viewers().clear();
            event.viewers().addAll(viewers);
        }
    }

    private boolean isPlayerInAnyTeam(Player player, World world) {
        List<List<Player>> worldTeams = teams.computeIfAbsent(world.getName(), k -> new ArrayList<>());

        for (List<Player> team : worldTeams) {
            if (team.contains(player)) {
                return true;
            }
        }
        return false;
    }
}
