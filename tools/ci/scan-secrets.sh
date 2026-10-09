#!/usr/bin/env bash
set -euo pipefail

config="$RUNNER_TEMP/gitleaks.toml"
ignore_dir="$RUNNER_TEMP/gitleaks-ignore"
mkdir -p "$ignore_dir"
config_path=tools/ci/gitleaks.toml

if [[ "$GITHUB_EVENT_NAME" == "pull_request" ]]; then
  git show "origin/${BASE_REF}:${config_path}" >"$config" 2>/dev/null || cp "$config_path" "$config"
  git show "origin/${BASE_REF}:.gitleaksignore" >"$ignore_dir/.gitleaksignore" 2>/dev/null || : >"$ignore_dir/.gitleaksignore"
  if git cat-file -e "origin/${BASE_REF}:.gitleaksignore" 2>/dev/null; then
    cp "$ignore_dir/.gitleaksignore" .gitleaksignore
  else
    rm -f .gitleaksignore
  fi
else
  cp "$config_path" "$config"
  if [[ -f .gitleaksignore ]]; then
    cp .gitleaksignore "$ignore_dir/.gitleaksignore"
  else
    : >"$ignore_dir/.gitleaksignore"
  fi
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

text_dir="$(mktemp -d "$RUNNER_TEMP/gitleaks-text.XXXXXX")"
python3 - "$text_dir" $merge_scope <<'PY'
import subprocess, sys

out_dir, *scope = sys.argv[1:]
BOMS = ((b"\xff\xfe\x00\x00", "utf-32-le"), (b"\x00\x00\xfe\xff", "utf-32-be"),
        (b"\xff\xfe", "utf-16-le"), (b"\xfe\xff", "utf-16-be"))

raw = subprocess.run(
    ["git", "log", "--raw", "-z", "--no-abbrev", "--no-renames", "--diff-merges=first-parent", "--format=", *scope],
    capture_output=True, check=True).stdout.split(b"\0")
blobs = {}
i = 0
while i < len(raw):
    if raw[i].startswith(b":"):
        _, mode, _, sha, status = raw[i].split()[:5]
        if status[:1] != b"D" and mode != b"160000":
            blobs.setdefault(sha.decode(), raw[i + 1])
        i += 2
    else:
        i += 1

cat = subprocess.Popen(["git", "cat-file", "--batch"], stdin=subprocess.PIPE, stdout=subprocess.PIPE)
for sha, path in blobs.items():
    cat.stdin.write(sha.encode() + b"\n")
    cat.stdin.flush()
    header = cat.stdout.readline().split()
    if len(header) != 3 or header[1] != b"blob":
        raise SystemExit(f"cannot read blob {sha}: {b' '.join(header)!r}")
    data = cat.stdout.read(int(header[2]))
    cat.stdout.read(1)
    if b"\0" not in data:
        continue
    print(f"text copy of NUL-containing blob {sha} ({path.decode(errors='replace')})", file=sys.stderr)
    with open(f"{out_dir}/{sha}.nulstripped.txt", "wb") as f:
        f.write(data.replace(b"\0", b""))
    for bom, codec in BOMS:
        if data.startswith(bom):
            with open(f"{out_dir}/{sha}.{codec}.txt", "w", encoding="utf-8") as f:
                f.write(data[len(bom):].decode(codec))
            break
cat.stdin.close()
if cat.wait() != 0:
    raise SystemExit("git cat-file failed")
PY
if [[ -n "$(ls -A "$text_dir")" ]]; then
  text_output="$("$GITLEAKS" dir --redact --exit-code 1 --ignore-gitleaks-allow --config "$config" "$text_dir" 2>&1)" && text_status=0 || text_status=$?
  printf '%s\n' "$text_output"
  if ((text_status != 0)) || grep -Eq '(^| )ERR( |$)|partial scan' <<<"$text_output"; then
    exit 1
  fi
fi

args=("${base_args[@]}")
if [[ -n "$range" ]]; then
  args+=(--log-opts="$range")
fi
"$GITLEAKS" "${args[@]}" .
