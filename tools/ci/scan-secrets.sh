#!/usr/bin/env bash
set -euo pipefail

args=(git --redact --exit-code 1 --ignore-gitleaks-allow)
case "$GITHUB_EVENT_NAME" in
  pull_request)
    args+=(--log-opts="origin/${BASE_REF}..HEAD")
    ;;
  push)
    if [[ -n "$BEFORE" && ! "$BEFORE" =~ ^0+$ ]]; then
      args+=(--log-opts="${BEFORE}..${AFTER}")
    else
      args+=(--log-opts="-1")
    fi
    ;;
esac
"$GITLEAKS" "${args[@]}" .
