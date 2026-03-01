#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

./gradlew :app:ktlintCheck :app:detekt :app:lintDebug :app:lintRelease
