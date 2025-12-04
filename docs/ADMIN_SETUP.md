## HungerGames Admin Setup Guide

This guide is written for server admins who want to install, configure, and run HungerGames without needing to read the source code.

---

### Index

1. [Requirements](#1-requirements)
2. [Installing the Plugin](#2-installing-the-plugin)
   - 2.1 [Using a release jar (recommended)](#21-using-a-release-jar-recommended)
   - 2.2 [Building from source](#22-building-from-source)
3. [Files and Folders Created](#3-files-and-folders-created)
4. [Global Settings (`settings.yml`)](#4-global-settings-settingsyml)
5. [Per-World Game Settings](#5-per-world-game-settings)
6. [Languages and Messages](#6-languages-and-messages)
7. [Basic Gameplay Setup](#7-basic-gameplay-setup)
   - 7.1 [Lobby world](#71-lobby-world)
   - 7.2 [Arena region](#72-arena-region)
   - 7.3 [Spawn points](#73-spawn-points)
   - 7.4 [Join signs](#74-join-signs)
   - 7.5 [Chest scanning and loot](#75-chest-scanning-and-loot)
8. [Database & Statistics (Optional)](#8-database--statistics-optional)
9. [Commands & Permissions Overview](#9-commands--permissions-overview)
10. [Common Admin Workflows](#10-common-admin-workflows)
11. [Troubleshooting & FAQ](#11-troubleshooting--faq)

---

### 1. Requirements

- Java 21 (JDK, not just JRE).
  - You can check with:
    - `java -version` (should show 21).
- Paper server `1.21.4`.
  - Download from https://papermc.io
- Maven (only needed if you build from source).

If your server host lets you pick versions, choose:

- Server type: **Paper**
- Minecraft version: **1.21.4**
- Java version: **21**

---

### 2. Installing the Plugin

#### 2.1 Using a Release Jar (Recommended)

1. Download the latest jar from Modrinth:  
   https://modrinth.com/plugin/hungergames
2. Stop your server.
3. Copy the jar into your server’s `plugins/` folder.
4. Start the server.
5. After startup, confirm the folder `plugins/HungerGames/` has been created and no errors are shown in the console.

#### 2.2 Building from Source

Only needed if you want the latest dev version or have modified the source.

1. Install Java 21 and Maven.
2. Clone or download the HungerGames source.
3. Open a terminal in the project root (where `pom.xml` is).
4. Run:

   ```bash
   mvn -DskipTests package
   ```

5. When it completes successfully, you’ll get a jar at:

   - `target/Hungergames-1.9.0-beta.jar`

6. Stop your server and copy that jar into `plugins/` on your server.
7. Start the server and check the console for any errors.

---

### 3. Files and Folders Created

After first run, HungerGames creates a folder:

- `plugins/HungerGames/`

Inside you’ll typically see:

- `settings.yml` – global plugin settings (applies to all worlds).
- `<world>/settings.yml` – per-world settings for each world (e.g. `world`, `world_nether`, etc.).
- `lang/` – folder containing language files, for example:
  - `en_US.yml`, `fr_FR.yml`, etc.
- `setspawn.yml` and `chest-locations.yml` per world – created when you configure spawns and scan arenas.

Example structure:

```text
plugins/
  HungerGames/
    settings.yml
    lang/
      en_US.yml
      fr_FR.yml
    world/
      settings.yml
      setspawn.yml
      chest-locations.yml
    world_hg1/
      settings.yml
      setspawn.yml
      chest-locations.yml
```

---

### 4. Global Settings (`settings.yml`)

Open `plugins/HungerGames/settings.yml`. Key options:

- `lobby-world`  
  - Name of the world players are sent to when not in a game.
  - Must match a folder name in your server root (e.g. `world`, `hub`).

- `whitelist-worlds` (true/false)  
  - If `false` (default), all worlds **except** those in `ignored-worlds` can be used by HungerGames.
  - If `true`, only worlds listed in `ignored-worlds` (used here as a whitelist) are allowed.  
    The naming is inherited from older versions; read comments in the file if present.

- `ignored-worlds`  
  - A list of world names that are either ignored or whitelisted depending on `whitelist-worlds`.

- `tips` (true/false)  
  - If true, players occasionally see helpful tips in their action bar.

- `database.enabled` (true/false)  
  - If true, HungerGames will use the database section to store detailed player stats.

Other settings may control logging, update checks, and integration with PlaceholderAPI.

After editing `settings.yml`, either restart the server or run:

```txt
/hg reloadconfig
```

to reload configuration.

---

### 5. Per-World Game Settings

Each world that HungerGames uses has its own `settings.yml` inside:

- `plugins/HungerGames/<world>/settings.yml`

Important options (names may differ slightly, see comments in the file):

- `game-time` – total duration of the game in seconds.
- `border.size` – initial world border size.
- `border.final-size` – border size at the end of shrink.
- `border.start-time` – when (in seconds) to start shrinking the border.
- `grace-period` – how long players have PVP disabled at the start.
- `players-per-team` – number of players per team (1 = solo).
- `display-scoreboard` – enable/disable the scoreboard.
- `display-bossbar` – show remaining time as a boss bar.
- `chestrefill.interval` – how often chests refill (seconds).
- `supplydrop.interval` – how often supply drops occur (seconds).
- `voting` – whether players can vote on game mode (solo/duo/trio/versus).
- `auto-start.enabled`, `auto-start.players`, `auto-start.delay` – auto-start behavior once enough players have joined.

Always restart or reload after editing:

```txt
/hg reloadconfig
```

---

### 6. Languages and Messages

Language files live in:

- `plugins/HungerGames/lang/`

Examples:

- `en_US.yml` – American English
- `fr_FR.yml` – French

Each file contains keys and text used by the plugin, such as:

```yaml
game:
  start: "&aThe game has started!"
  grace-start: "&eGrace period has begun."
  grace-end: "&cGrace period is over!"
```

#### Changing the Default Language

1. Open `settings.yml` and check if there is a default locale option (if present).
2. Ensure the corresponding file exists in `lang/` (e.g. `de_DE.yml`).
3. Restart or reload the plugin.

If a translation key is missing in a language file, HungerGames will:

- Log a warning in the console.
- Attempt to fill missing keys based on `lang/en_US.yml` when `validateLanguageKeys` runs.

Players’ language is typically chosen based on their client locale where supported.

---

### 7. Basic Gameplay Setup

This section walks through setting up one playable map from scratch.

#### 7.1 Lobby World

1. Choose or create a lobby world (e.g. `hub`).
2. Set `lobby-world: hub` in `plugins/HungerGames/settings.yml`.
3. Restart or reload the server so the plugin knows your lobby world.

#### 7.2 Arena Region

1. Teleport to the world you want to use as an arena (for example `world_hg1`).
2. Run the command that gives the arena selection wand (commonly via `/hg arena select` if mapped in commands).
3. Use the selection wand:
   - Left-click a block to set the first corner of the region.
   - Right-click another block to set the opposite corner.
4. The plugin stores this region and uses it to scan for chests and manage the game area.

#### 7.3 Spawn Points

1. In the same arena world, run:

   ```txt
   /hg setspawn
   ```

   This gives you a special stick for configuring spawns.

2. With the stick in hand:
   - **Left-click** on blocks where players should spawn.
   - Each click adds a spawn point to `setspawn.yml` for that world.
3. To remove a spawn, right-click an existing spawn point location with the stick (see in-game message hints).
4. After configuring spawns, save them and confirm the number of spawn points shown in chat.

#### 7.4 Join Signs

1. Go to your lobby world.
2. Place a row of wall signs where players should be able to join games.
3. Stand facing the first sign in the row.
4. Run:

   ```txt
   /hg signset
   ```

5. The plugin will:
   - Check you are targeting a valid sign.
   - Configure a row of signs with one entry per configured game world.
   - Update sign text automatically to show world names and status (Waiting / In Progress).

Players will then be able to right-click these signs to join games.

#### 7.5 Chest Scanning and Loot

1. Make sure your arena region is set and contains chests/barrels/shulker boxes with loot.
2. Run the command to scan the arena (commonly `/hg scanarena`).
3. The plugin will:
   - Find containers inside the arena region.
   - Save their positions to `chest-locations.yml` under the world’s folder.
4. Edit loot tables:
   - Open `plugins/HungerGames/<world>/items.yml` (or the central items config if your version uses a shared file).
   - Adjust items, weights, and quantities for:
     - `chest-items`
     - `barrel-items`
     - `trapped-chest-items`
5. Use the chest refill command (e.g. `/hg chestrefill`) or wait for the automatic interval to see your new loot in action.

---

### 8. Database & Statistics (Optional)

HungerGames can store detailed player statistics in a SQL database and expose them to PlaceholderAPI.

#### 8.1 Enabling the Database

1. In `plugins/HungerGames/settings.yml`, set:

   ```yaml
   database:
     enabled: true
     # plus connection details below
   ```

2. Fill in the database connection settings as documented in your `settings.yml` (host, port, database name, username, password).
3. Ensure the database exists and the user has permission to create/alter tables.
4. Restart the server:
   - On first run with `database.enabled: true`, HungerGames will create necessary tables and columns.

#### 8.2 What Gets Tracked

Depending on your version, stats may include:

- Kills, deaths, kill assists.
- Solo/team games played and won.
- Chests opened, supply drops opened.
- Damage dealt and taken.
- Border, environment, and player deaths.
- Time played overall and per month.

These stats are exposed to PlaceholderAPI with identifiers like:

- `%hungergames_kills%`
- `%hungergames_deaths%`
- `%hungergames_solo_games_played%`
- and many more (see `HungerGamesExpansion` or your wiki).

---

### 9. Commands & Permissions Overview

The exact command names and permissions are listed in `plugin.yml` and the project wiki, but here is a rough overview of key ones:

- **Main command:** `/hg`
  - Subcommands typically include join, start, stop, setspawn, arena selection, chest refill, stats display.

- **Admin-related permissions** (examples):
  - `hungergames.start` – start a game manually.
  - `hungergames.end` – end/cancel a running game.
  - `hungergames.setspawn` – configure spawn points.
  - `hungergames.create` / `hungergames.scanarena` – setup arenas and scan chests.
  - `hungergames.chestrefill` / `hungergames.supplydrop` – trigger chest refill and supply drops.

Check `src/main/resources/plugin.yml` or online documentation for the exact list and defaults.

---

### 10. Common Admin Workflows

#### 10.1 Creating a New Map from Scratch

1. Create or import a world (for example `world_hg2`).
2. Start the server so the world is recognized.
3. Join `world_hg2` and set up:
   - Arena region (selection wand).
   - Spawn points (`/hg setspawn`).
   - Chest scanning (`/hg scanarena`).
4. Edit `plugins/HungerGames/world_hg2/settings.yml` to tune:
   - `game-time`, border behavior, grace period, etc.
5. Add or adjust loot in the items config.
6. Run `/hg signset` again in the lobby if you want signs to include the new world.

#### 10.2 Resetting a World Between Games

HungerGames integrates with a reset handler to restore the arena between games (depending on your configuration).

General advice:

- Keep a clean copy of your arena world.
- Use a dedicated world per map, not your main survival world.
- If you use external world reset plugins, make sure their timing does not conflict with HungerGames’ own reset logic.

#### 10.3 Quickly Testing Changes

1. Change configuration or loot.
2. Run `/hg reloadconfig`.
3. Use `/hg chestrefill` or start a game with a few test players (or yourself and friends) to verify:
   - Teleportation and spawn points.
   - Scoreboard and bossbar.
   - Border shrink and timing.
   - Loot distribution.

---

### 11. Troubleshooting & FAQ

**Q: The plugin doesn’t create any folders or configs.**

- Make sure:
  - The jar is in `plugins/`.
  - You are running a compatible Paper version (1.21.4).
  - The server console log does not show any startup errors from HungerGames.

**Q: Players can’t join games / “no worlds available”.**

- Check:
  - `plugins/HungerGames/settings.yml` – lobby world and whitelist/ignore configuration.
  - That your arena worlds have `settings.yml` and spawn points defined (`setspawn.yml`).
  - That join signs were set up correctly with `/hg signset`.

**Q: Loot seems empty or wrong.**

- Ensure `chest-locations.yml` exists for that world (run `/hg scanarena` if needed).
- Check items config (e.g. `items.yml`) for:
  - Correct `type` names (matching Minecraft item IDs).
  - Reasonable `weight` and `amount` values.

**Q: I changed a language file but messages didn’t update.**

- Run `/hg reloadconfig` or restart the server.
- Check the console for missing key warnings.
- Make sure you edited the correct locale file and that your client’s language matches or defaults to `en_US`.

If you continue having issues, check the console logs for stack traces and consult the project’s wiki or issue tracker for further guidance.

