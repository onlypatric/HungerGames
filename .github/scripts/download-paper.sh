#!/usr/bin/env bash
set -euo pipefail

PAPER_VERSION="${1:-1.21.4}"

API_URL="https://api.papermc.io/v2/projects/paper/versions/${PAPER_VERSION}"

BUILD=$(python3 - <<PY
import json, urllib.request
url = "${API_URL}"
with urllib.request.urlopen(url) as resp:
    data = json.load(resp)
print(data["builds"][-1])
PY
)

echo "Using Paper ${PAPER_VERSION} build ${BUILD}"

curl -s -L "https://api.papermc.io/v2/projects/paper/versions/${PAPER_VERSION}/builds/${BUILD}/downloads/paper-${PAPER_VERSION}-${BUILD}.jar" -o paper.jar
ls -lh paper.jar

