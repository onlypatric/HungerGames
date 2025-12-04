#!/usr/bin/env node

const mineflayer = require('mineflayer');

const host = process.env.MC_HOST || '127.0.0.1';
const port = Number(process.env.MC_PORT || 25565);
const botCount = Number(process.env.MC_BOT_COUNT || 50);

async function createBot(index) {
  return new Promise((resolve, reject) => {
    const username = `CI_Bot_${index}`;

    const bot = mineflayer.createBot({
      host,
      port,
      username,
      version: false, // let mineflayer detect
      // Disable internal plugins (including chat parsing) to avoid
      // prismarine-chat incompatibilities with modern Paper chat formats.
      loadInternalPlugins: false,
    });

    const timeout = setTimeout(() => {
      bot.end();
      reject(new Error(`Bot ${username} timed out before spawn`));
    }, 30000);

    bot.once('spawn', () => {
      clearTimeout(timeout);
      // Join the configured arena
      bot.chat('/hg join arena1');

      // After joining, exercise a few HungerGames-related commands and some movement
      setTimeout(() => {
        bot.chat('/hg stats');
      }, 3000);

      setTimeout(() => {
        bot.chat('/hg teamchat');
      }, 6000);

      setTimeout(() => {
        bot.chat('CI bot ready for HungerGames!');
      }, 9000);

      // Simple movement pattern inside the arena
      setTimeout(() => {
        bot.setControlState('forward', true);
        bot.setControlState('jump', true);
      }, 4000);

      setTimeout(() => {
        bot.setControlState('jump', false);
        bot.setControlState('left', true);
      }, 9000);

      setTimeout(() => {
        bot.setControlState('left', false);
        bot.setControlState('right', true);
      }, 14000);

      setTimeout(() => {
        bot.setControlState('forward', false);
        bot.setControlState('right', false);
      }, 20000);

      // Stay connected long enough for countdown + early game, then disconnect
      setTimeout(() => {
        bot.end();
        resolve();
      }, 30000);
    });

    bot.on('kicked', (reason) => {
      clearTimeout(timeout);
      reject(new Error(`Bot ${username} was kicked: ${reason}`));
    });

    bot.on('error', (err) => {
      // Log but don't immediately fail all bots on transient errors
      console.error(`Bot ${username} error:`, err.message || err);
    });
  });
}

async function main() {
  console.log(`Starting ${botCount} protocol bots against ${host}:${port}...`);

  const results = [];

  for (let i = 0; i < botCount; i += 1) {
    // Stagger connections slightly to avoid spikes
    /* eslint-disable no-await-in-loop */
    await new Promise((r) => setTimeout(r, 300));
    results.push(
      createBot(i).then(
        () => ({ ok: true }),
        (err) => {
          console.error(`Bot ${i} failed:`, err.message || err);
          return { ok: false };
        },
      ),
    );
  }

  const settled = await Promise.all(results);
  const successCount = settled.filter((r) => r.ok).length;

  console.log(`Bot simulation complete: ${successCount}/${botCount} bots connected and ran /hg.`);

  if (successCount === 0) {
    throw new Error('All bots failed to connect; failing CI.');
  }
}

main().catch((err) => {
  console.error('Bot simulation failed:', err.message || err);
  process.exit(1);
});
