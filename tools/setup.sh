#!/usr/bin/env bash
set -euo pipefail
root="$(git rev-parse --show-toplevel)"

hooks_dir="$(git -C "$root" rev-parse --path-format=absolute --git-path hooks)"
mkdir -p "$hooks_dir"
ln -sf "$root/tools/pre-push" "$hooks_dir/pre-push"
echo "Linked the pre-push hook. It runs tools/ci/check-constitution.sh."
