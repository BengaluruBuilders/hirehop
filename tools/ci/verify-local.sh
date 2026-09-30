#!/usr/bin/env bash
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

tools/ci/check-constitution.sh
./gradlew \
  :build-logic:convention:check \
  spotlessCheck \
  testDebugUnitTest \
  lintRelease \
  assembleDebug \
  -PwarningsAsErrors=true \
  --continue
