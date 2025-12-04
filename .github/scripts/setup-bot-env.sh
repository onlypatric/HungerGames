#!/usr/bin/env bash
set -euo pipefail

# Install Node dependencies for full-protocol bots
if [ ! -f package.json ]; then
  npm init -y >/dev/null 2>&1
fi

npm install mineflayer@^4 --save-dev >/dev/null 2>&1

