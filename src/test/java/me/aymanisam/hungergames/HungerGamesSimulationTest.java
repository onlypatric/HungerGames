package me.aymanisam.hungergames;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("MockBukkit 1.21 registry bootstrap issue; real Paper server smoke test runs in CI instead")
class HungerGamesSimulationTest {

    private ServerMock server;
    private HungerGames plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        // Create simple lobby and arena worlds before loading the plugin
        World lobbyWorld = server.addSimpleWorld("hub");
        World arenaWorld = server.addSimpleWorld("arena1");
        assertNotNull(lobbyWorld);
        assertNotNull(arenaWorld);

        plugin = MockBukkit.load(HungerGames.class);
        ConfigHandler configHandler = plugin.getConfigHandler();

        // Ensure we are simulating the "no database" production setup
        configHandler.getPluginSettings().set("database.enabled", false);

        // Use a simple lobby / arena configuration
        configHandler.getPluginSettings().set("lobby-world", "hub");

        // Only allow one arena world for this simulation
        HungerGames.hgWorldNames.clear();
        HungerGames.hgWorldNames.add("arena1");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void simulateFiftyPlayersJoiningWithNoDatabase() {
        List<PlayerMock> players = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            PlayerMock player = server.addPlayer("Player" + i);
            players.add(player);

            // Basic sanity checks: player is online and can execute a simple command
            server.dispatchCommand(player, "hg stats");
        }

        assertEquals(50, server.getOnlinePlayers().size(), "All mock players should be online");

        // Verify that database is not enabled in this simulation
        assertFalse(plugin.getConfigHandler().getPluginSettings().getBoolean("database.enabled"),
                "Database should be disabled for this simulation");
    }
}
