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

kill "$SERVER_PID" || true

