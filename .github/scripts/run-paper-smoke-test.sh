#!/usr/bin/env bash
set -euo pipefail

if [ ! -f paper.jar ]; then
  echo "paper.jar not found in workspace root"
  exit 1
fi

if ! ls target/Hungergames-*.jar >/dev/null 2>&1; then
  echo "HungerGames plugin jar not found in target/"
  exit 1
fi

mkdir -p server/plugins
cp target/Hungergames-*.jar server/plugins/
cd server

cat > server.properties << 'EOF'
online-mode=false
server-port=25565
enable-rcon=false
motd=HungerGames CI
max-players=80
level-name=arena1
EOF

mkdir -p plugins/HungerGames/arena1

# Global plugin settings: use default, but ensure database is disabled and lobby-world
# does not match the arena world so arena1 stays available as a game world.
cp ../src/main/resources/settings.yml plugins/HungerGames/settings.yml
sed -i 's/^lobby-world:.*/lobby-world: "world"/' plugins/HungerGames/settings.yml
sed -i 's/^  enabled:.*/  enabled: false/' plugins/HungerGames/settings.yml || true

# Per-world config for arena1: start from default config.yml
cp ../src/main/resources/config.yml plugins/HungerGames/arena1/config.yml

# Make the game shorter and enable auto-start so CI actually runs a full match
sed -i 's/^game-time:.*/game-time: 120/' plugins/HungerGames/arena1/config.yml
sed -i 's/^countdown:.*/countdown: 10/' plugins/HungerGames/arena1/config.yml
sed -i 's/^grace-period:.*/grace-period: 5/' plugins/HungerGames/arena1/config.yml
sed -i 's/^min-players:.*/min-players: 10/' plugins/HungerGames/arena1/config.yml
sed -i 's/^auto-start:$/auto-start:/' plugins/HungerGames/arena1/config.yml || true
sed -i 's/^  enabled:.*/  enabled: true/' plugins/HungerGames/arena1/config.yml
sed -i 's/^  players:.*/  players: 10/' plugins/HungerGames/arena1/config.yml
sed -i 's/^  delay:.*/  delay: 5/' plugins/HungerGames/arena1/config.yml

# Minimal arena region around spawn
cat > plugins/HungerGames/arena1/arena.yml << 'EOF'
region:
  world: arena1
  pos1:
    x: -30
    y: 0
    z: -30
  pos2:
    x: 30
    y: 255
    z: 30
EOF

# Predefined spawnpoints for up to 50 players in arena1
{
  echo "spawnpoints:"
  for i in $(seq 0 49); do
    echo "  - \"arena1,$i.0,65.0,0.0\""
  done
} > plugins/HungerGames/arena1/setspawn.yml

echo "eula=true" > eula.txt

java -Xmx2G -jar ../paper.jar --nogui &
SERVER_PID=$!

echo "Waiting for Paper server to start..."

for i in {1..24}; do
  if [ -f logs/latest.log ] && grep -q "Done (" logs/latest.log; then
    echo "Server started successfully"
    break
  fi
  if ! kill -0 "$SERVER_PID" 2>/dev/null; then
    echo "Server process exited before startup completed"
    exit 1
  fi
  sleep 5
done

if ! kill -0 "$SERVER_PID" 2>/dev/null; then
  echo "Server did not reach running state in time"
  exit 1
fi

if grep -q "Error occurred while enabling HungerGames" logs/latest.log; then
  echo "HungerGames plugin failed to enable:"
  grep "HungerGames" logs/latest.log || true
  kill "$SERVER_PID" || true
  exit 1
fi
echo "HungerGames plugin appears to have enabled successfully."

echo "Running full-protocol bot simulation (50 bots) and starting a game..."
MC_HOST=127.0.0.1 MC_PORT=25565 MC_BOT_COUNT=50 node ../.github/scripts/run-bot-simulation.cjs &
BOT_PID=$!

# Wait for bots to finish their run (they stay ~30 seconds each)
wait "$BOT_PID"

echo "Bot simulation finished; stopping server."

kill "$SERVER_PID" || true
