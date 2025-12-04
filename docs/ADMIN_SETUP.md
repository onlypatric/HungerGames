## HungerGames Admin Setup Guide

This guide is for server admins who just want to run the plugin, not develop it.

### Requirements

- Java 21 (JDK, not just JRE)
- Paper server `1.21.4`
- Maven (optional, only needed if you build from source)

### Option 1: Use a Released Jar (Recommended)

1. Download a release jar from Modrinth:  
   https://modrinth.com/plugin/hungergames
2. Place the jar into your server `plugins/` folder.
3. Start the server once to let it generate default configs and data folders.

### Option 2: Build from Source

1. Install Java 21 and Maven.
2. Clone the repository and open a terminal in the project root.
3. Run:

   ```bash
   mvn -DskipTests package
   ```

4. The plugin jar will be generated at:

   `target/Hungergames-1.9.0-beta.jar`

5. Copy that jar into your server `plugins/` folder and restart the server.

### Basic Server Configuration

After first start, a folder `HungerGames/` will be created inside your `plugins/` directory. The most important files:

- `plugins/HungerGames/settings.yml` – global settings (lobby world, database, tips, etc.)
- `plugins/HungerGames/<world>/settings.yml` – per-world game settings (time limits, border size, chest refill, etc.)
- `plugins/HungerGames/lang/*.yml` – language files per locale (e.g. `en_US.yml`).

Key settings to check in `settings.yml`:

- `lobby-world`: name of your lobby world (must match the actual world folder).
- `whitelist-worlds` / `ignored-worlds`: control which worlds HungerGames should use.
- `database.enabled`: turn on SQL stats storage if you have configured a database.

### First-Time Game Setup (High-Level)

1. **Lobby and worlds**
   - Make sure your lobby world exists and is set correctly in `settings.yml`.
   - Any world with a `level.dat` file and not ignored may be used as a game world.

2. **Arena selection**
   - Use `/hg arena select` (or the configured command) to get the arena selection wand.
   - Left and right click to mark corners of your arena region.

3. **Spawn points**
   - Use `/hg setspawn` to get the spawn wand.
   - Left-click in the arena where you want spawn points; these are stored per world.

4. **Signs to join**
   - Place a wall sign in your lobby.
   - Stand facing it and run `/hg signset` to have the plugin configure a row of join signs.

5. **Chest scanning**
   - Use `/hg scanarena` to let the plugin find chests and barrels inside the arena region.
   - Adjust loot in `items.yml` for each world as needed.

6. **Test a game**
   - Join with a few players.
   - Run `/hg start` in the relevant world.
   - Verify countdown, teleportation, border, chests, and end-of-game behavior.

### Player-Facing Features to Be Aware Of

- `/hg` main command with subcommands for joining, starting, spectating, etc.
- Automatic team selection and voting for game modes if enabled.
- Scoreboard shows alive players, time left, border info, and optionally team members.
- Optional database-backed stats (kills, deaths, time played, etc.) and PlaceholderAPI expansion.

