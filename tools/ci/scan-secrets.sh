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

attributes_file="$(git rev-parse --git-path info/attributes)"
mkdir -p "$(dirname "$attributes_file")"
printf '* diff\n' >>"$attributes_file"

base_args=(git --redact --exit-code 1 --ignore-gitleaks-allow --config "$config" --gitleaks-ignore-path "$ignore_dir/.gitleaksignore")
if [[ -n "$range" ]]; then
  git rev-list "$range" >/dev/null
  merge_scope="$range"
else
  merge_scope="--all"
fi
# gitleaks 8.30.1 runs git log -p, which prints no diff for merge commits, so an evil merge needs its own pass
# gitleaks 8.30.1 exits 0 when its git log subprocess fails, so validate the scope and scan the output for errors
git log --merges --diff-merges=first-parent --format=%H $merge_scope >/dev/null
merge_output="$("$GITLEAKS" "${base_args[@]}" "--log-opts=--merges --diff-merges=first-parent $merge_scope" . 2>&1)" && merge_status=0 || merge_status=$?
printf '%s\n' "$merge_output"
if ((merge_status != 0)) || grep -Eq '(^| )ERR( |$)|partial scan' <<<"$merge_output"; then
  exit 1
fi

args=("${base_args[@]}")
if [[ -n "$range" ]]; then
  args+=(--log-opts="$range")
fi
"$GITLEAKS" "${args[@]}" .
