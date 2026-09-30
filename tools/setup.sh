#!/usr/bin/env bash
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

hooks_dir="$(git rev-parse --git-path hooks)"
mkdir -p "$hooks_dir"
cp tools/pre-push "$hooks_dir/pre-push"
chmod +x "$hooks_dir/pre-push"
echo "Installed the pre-push hook. It runs the constitution check and Spotless."
