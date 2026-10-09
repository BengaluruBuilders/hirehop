#!/usr/bin/env bash
set -euo pipefail

config="$RUNNER_TEMP/gitleaks.toml"
ignore_dir="$RUNNER_TEMP/gitleaks-ignore"
mkdir -p "$ignore_dir"
printf '[extend]\nuseDefault = true\n' >"$config"

if [[ "$GITHUB_EVENT_NAME" == "pull_request" ]]; then
  git show "origin/${BASE_REF}:.gitleaksignore" >"$ignore_dir/.gitleaksignore" 2>/dev/null || : >"$ignore_dir/.gitleaksignore"
  if git cat-file -e "origin/${BASE_REF}:.gitleaksignore" 2>/dev/null; then
    cp "$ignore_dir/.gitleaksignore" .gitleaksignore
  else
    rm -f .gitleaksignore
  fi
elif [[ -f .gitleaksignore ]]; then
  cp .gitleaksignore "$ignore_dir/.gitleaksignore"
else
  : >"$ignore_dir/.gitleaksignore"
fi

range=""
case "$GITHUB_EVENT_NAME" in
  pull_request)
    git cat-file -e "origin/${BASE_REF}^{commit}"
    range="origin/${BASE_REF}..HEAD"
    ;;
  push)
    if [[ -n "$BEFORE" && ! "$BEFORE" =~ ^0+$ ]] && git cat-file -e "${BEFORE}^{commit}" 2>/dev/null; then
      range="${BEFORE}..${AFTER}"
    fi
    ;;
esac

args=(git --redact --exit-code 1 --ignore-gitleaks-allow --config "$config" --gitleaks-ignore-path "$ignore_dir/.gitleaksignore")
if [[ -n "$range" ]]; then
  git rev-list "$range" >/dev/null
  args+=(--log-opts="$range")
fi
"$GITLEAKS" "${args[@]}" .
