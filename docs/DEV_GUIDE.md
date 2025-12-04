## HungerGames Developer Guide

This document is for developers working on the HungerGames source.

### Tech Stack

- Java 21
- Paper API `1.21.4`
- Maven build (`pom.xml` in project root)
- Adventure (Kyori) for chat/titles/actionbars

### Building

From the project root:

```bash
mvn -DskipTests package
```

This produces:

- Shaded plugin jar at `target/HungerGames-1.9.0-beta-shaded.jar` (renamed to `Hungergames-1.9.0-beta.jar` by shade).

The project targets Java 21 via `maven-compiler-plugin` with `<release>21</release>`.

### Project Layout

- `src/main/java/me/aymanisam/hungergames/HungerGames.java`  
  Main plugin class, wiring handlers, listeners, commands, database setup, and Adventure (`BukkitAudiences`).

- `handlers/`  
  Core logic:
  - `GameSequenceHandler` – game lifecycle, timers, win conditions.
  - `CountDownHandler` – pre-game countdown, team assignments.
  - `TeamsHandler` – team logic, compass items, glow, placements.
  - `SetSpawnHandler` – per-world spawn configuration and auto-start.
  - `WorldBorderHandler`, `WorldResetHandler` – border and world resets.
  - `ChestRefillHandler` – loot loading and chest/barrel/shulker refill.
  - `ScoreBoardHandler` – FastBoard scoreboard.
  - `LangHandler` – language configuration and message components.
  - `TipsHandler`, `SupplyDropHandler`, `SpectatePlayerHandler`, etc.

- `listeners/`  
  Event listeners for players, signs, teams, compass, GUIs, etc.

- `commands/`  
  `/hg` dispatcher plus individual command executors (join, start, setspawn, arenas, stats, etc.).

- `stats/`  
  Database integration, stats model, and PlaceholderAPI expansion.

- `src/main/resources/plugin.yml`  
  Plugin metadata (name, version, main class, api-version) with `${version}` filtered from Maven.

### Localization (`LangHandler`)

- `LangHandler` loads YAML files from `plugins/HungerGames/lang/*.yml` into a map keyed by locale (`en_us`, `fr_fr`, ...).
- Preferred API:
  - `String getMessage(Player player, String key, Object... args)` – for APIs that require plain strings (scoreboard lines, bossbar titles).
  - `Component getMessageComponent(Player player, String key, Object... args)` – for Adventure-based messaging (chat, titles, item display names).
- Templates in language files can include `{0}`, `{1}`, ... which are replaced with provided arguments.
- Color codes use `&` in YAML and are converted to Adventure components via `LegacyComponentSerializer.legacyAmpersand()`.

### Adventure Usage Patterns

- Titles:

  ```java
  Component subtitle = langHandler.getMessageComponent(player, "game.start");
  Title.Times times = Title.Times.times(Duration.ofMillis(5L * 50L), Duration.ofMillis(20L * 50L), Duration.ofMillis(10L * 50L));
  plugin.adventure().player(player).showTitle(Title.title(Component.empty(), subtitle, times));
  ```

- Action bars:

  ```java
  plugin.adventure().player(player).sendActionBar(
      Component.text(langHandler.getMessage(player, "tips.some-key"))
  );
  ```

- Item meta:

  ```java
  ItemMeta meta = item.getItemMeta();
  meta.displayName(langHandler.getMessageComponent(player, "item.name"));
  meta.lore(List.of(
      langHandler.getMessageComponent(player, "item.lore.line1"),
      langHandler.getMessageComponent(player, "item.lore.line2")
  ));
  item.setItemMeta(meta);
  ```

### Paper 1.21.4 Migration Notes

- **Titles:** Legacy `Player#sendTitle(String, ...)` is replaced by Adventure titles everywhere.
- **Chat:** Team chat uses Paper’s `AsyncChatEvent` and manipulates viewers instead of legacy `AsyncPlayerChatEvent`.
- **Locales:** Use `Player#locale()` (returns `java.util.Locale`) rather than `getLocale()` where possible.
- **Registries:** Avoid deprecated static registries. For potion effects and enchants, the code currently uses:
  - `PotionEffectType.values()` plus `type.getKey().getKey()` string comparison.
  - `Enchantment.values()` plus `e.getKey().getKey()` matching.
- **Potions:** `PotionMeta#setBasePotionType(PotionType)` is used instead of the older `PotionData` + `setBasePotionData`.

### Coding Conventions

- Java 21 language level.
- Use `var` sparingly; existing code prefers explicit types.
- Use Adventure `Component` for any new user-facing text when possible.
- Prefer null-safe patterns (`Objects.requireNonNull`, early returns).
- Keep behavior backward-compatible with existing configuration where feasible.

### Running and Debugging

- Build the jar and drop it into a Paper 1.21.4 test server.
- Use a separate test world for arenas; the plugin auto-discovers worlds with a `level.dat`.
- Logs:
  - Language key issues are reported at `WARNING` level with details.
  - Database issues log full stack traces to help diagnose SQL problems.

