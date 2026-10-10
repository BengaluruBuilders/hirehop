#!/usr/bin/env bash
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

tools/ci/check-constitution.sh
tools/ci/test-scan-secrets.sh
tools/ci/test-release-config.sh
./gradlew \
  :build-logic:convention:check \
  spotlessCheck \
  testDebugUnitTest \
  :app:testDemoDebugUnitTest \
  :app:testProdDebugUnitTest \
  :core:domain:koverVerify \
  :core:data:koverVerify \
  verifyRoborazziDebug \
  lintRelease \
  :app:lintDemoRelease \
  :app:lintProdRelease \
  dependencyGuard \
  assembleDebug \
  assembleRelease \
  -PwarningsAsErrors=true \
  --continue
