package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static me.aymanisam.hungergames.HungerGames.hgWorldNames;

public class BlockBreakListener implements Listener {
    private final HungerGames plugin;
    private final ConfigHandler configHandler;

    public BlockBreakListener(HungerGames plugin) {
        this.plugin = plugin;
        this.configHandler = plugin.getConfigHandler();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        String worldName = player.getWorld().getName();

        // Only enforce HG block rules in HG-managed worlds
        if (!hgWorldNames.contains(worldName)) {
            return;
        }

        FileConfiguration worldConfig = configHandler.getWorldConfig(player.getWorld());

        // If block breaking is not enabled in this world, leave it alone
        if (!worldConfig.getBoolean("break-blocks.enabled")) {
            return;
        }

        List<String> allowedStrings = worldConfig.getStringList("break-blocks.allowed-blocks");

        for (String string: allowedStrings) {
            if (string.equalsIgnoreCase("ALL")) {
                return;
            }
        }

        List<Material> allowedMaterials = new ArrayList<>();

        for (String string: allowedStrings) {
            try {
                allowedMaterials.add(Material.valueOf(string));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "Invalid material: " + string);
            }
        }

        if (!allowedMaterials.contains(event.getBlock().getType())) {
            event.setCancelled(true);
        }

        if (!worldConfig.getBoolean("break-blocks.drop")) {
            event.setDropItems(false);
        }
    }
}
