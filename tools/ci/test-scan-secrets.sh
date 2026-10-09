#!/usr/bin/env bash
set -uo pipefail

root="$(git rev-parse --show-toplevel)"
script="$root/tools/ci/scan-secrets.sh"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
failures=0

check() {
  local name="$1"
  shift
  if "$@"; then
    echo "ok   $name"
  else
    echo "FAIL $name"
    failures=$((failures + 1))
  fi
}

cat >"$work/gitleaks" <<'STUB'
#!/usr/bin/env bash
printf '%s\n' "$@" >"$RECORD"
exit "${STUB_EXIT:-0}"
STUB
chmod +x "$work/gitleaks"

repo="$work/repo"
git init -q -b main "$repo"
git -C "$repo" config user.email test@example.test
git -C "$repo" config user.name test
printf 'base-entry\n' >"$repo/.gitleaksignore"
printf '[allowlist]\npaths = [".*"]\n' >"$repo/.gitleaks.toml"
git -C "$repo" add -A
git -C "$repo" commit -q -m base
base_sha="$(git -C "$repo" rev-parse HEAD)"
git -C "$repo" update-ref refs/remotes/origin/main "$base_sha"
printf 'base-entry\npr-added-entry\n' >"$repo/.gitleaksignore"
git -C "$repo" commit -q -am change
head_sha="$(git -C "$repo" rev-parse HEAD)"

run_scan() {
  rm -f "$work/record"
  (
    cd "$repo"
    RECORD="$work/record" RUNNER_TEMP="$work/temp" GITLEAKS="$work/gitleaks" "$@" "$script"
  )
}

mkdir -p "$work/temp"

pull_request() {
  run_scan env GITHUB_EVENT_NAME=pull_request BASE_REF=main BEFORE= AFTER="$head_sha"
}

push_with_before() {
  run_scan env GITHUB_EVENT_NAME=push BASE_REF= BEFORE="$1" AFTER="$head_sha"
}

argument_after() {
  grep -A1 -x -- "$1" "$work/record" | tail -n 1
}

has_log_opts() {
  grep -q -- '^--log-opts' "$work/record"
}

pull_request_uses_a_temp_config_that_extends_the_defaults() {
  pull_request || return 1
  local config
  config="$(argument_after --config)"
  [[ "$config" == "$work/temp/"* && "$config" != "$repo/"* ]] || return 1
  grep -q 'useDefault = true' "$config" && ! grep -q allowlist "$config"
}

pull_request_takes_the_ignore_file_from_the_base_branch() {
  pull_request || return 1
  local ignore
  ignore="$(argument_after --gitleaks-ignore-path)"
  [[ -f "$ignore" && "$ignore" != "$repo/"* ]] || return 1
  grep -qx base-entry "$ignore" && ! grep -q pr-added-entry "$ignore"
}

pull_request_scans_only_the_pull_request_range() {
  pull_request || return 1
  grep -qx -- '--log-opts=origin/main..HEAD' "$work/record"
}

push_with_a_known_before_scans_that_range() {
  push_with_before "$base_sha" || return 1
  grep -qx -- "--log-opts=${base_sha}..${head_sha}" "$work/record"
}

push_with_an_unknown_before_falls_back_to_a_full_scan() {
  push_with_before "1111111111111111111111111111111111111111" || return 1
  ! has_log_opts
}

push_with_a_zero_before_falls_back_to_a_full_scan() {
  push_with_before "0000000000000000000000000000000000000000" || return 1
  ! has_log_opts
}

pull_request_with_an_unknown_base_fails_without_scanning() {
  ! run_scan env GITHUB_EVENT_NAME=pull_request BASE_REF=missing-branch BEFORE= AFTER="$head_sha" 2>/dev/null &&
    [[ ! -e "$work/record" ]]
}

a_finding_fails_the_step() {
  ! run_scan env STUB_EXIT=1 GITHUB_EVENT_NAME=pull_request BASE_REF=main BEFORE= AFTER="$head_sha"
}

policy_job_field() {
  python3 - "$root/.github/workflows/build.yml" "$1" <<'PY'
import sys, yaml
workflow = yaml.safe_load(open(sys.argv[1]))
node = workflow["jobs"]["policy"]
for part in sys.argv[2].split("."):
    node = node[part]
print(node)
PY
}

policy_job_serialises_runs_per_ref() {
  [[ "$(policy_job_field concurrency.group)" == 'policy-${{ github.ref }}' ]] &&
    [[ "$(policy_job_field concurrency.cancel-in-progress)" == "\${{ github.event_name == 'pull_request' }}" ]]
}

policy_job_has_time_for_the_gitleaks_download() {
  [[ "$(policy_job_field timeout-minutes)" == 10 ]]
}

policy_job_runs_the_script_and_its_test() {
  grep -q 'tools/ci/scan-secrets.sh' "$root/.github/workflows/build.yml" &&
    grep -q 'tools/ci/test-scan-secrets.sh' "$root/.github/workflows/build.yml"
}

find_real_gitleaks() {
  local candidate="${REAL_GITLEAKS:-$(command -v gitleaks)}"
  if [[ -n "$candidate" && -x "$candidate" ]]; then
    echo "$candidate"
    return 0
  fi
  if [[ "${CI:-}" == "true" ]]; then
    echo "real gitleaks not found in CI" >&2
    return 1
  fi
  echo "SKIP real gitleaks not found" >&2
  return 2
}

new_real_repo() {
  local r="$1"
  git init -q -b main "$r"
  git -C "$r" config user.email test@example.test
  git -C "$r" config user.name test
  printf 'base\n' >"$r/README"
  git -C "$r" add -A
  git -C "$r" commit -q -m base
  git -C "$r" update-ref refs/remotes/origin/main "$(git -C "$r" rev-parse HEAD)"
}

fake_token() {
  echo "ghp_$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 36)"
}

real_scan() {
  local r="$1" real="$2"
  mkdir -p "$work/real-temp"
  (cd "$r" && RUNNER_TEMP="$work/real-temp" GITHUB_EVENT_NAME=pull_request BASE_REF=main BEFORE= AFTER=HEAD GITLEAKS="$real" "$script" 2>&1)
}

real_gitleaks_still_fails_when_a_pull_request_adds_its_own_ignore_entry() {
  local real status=0
  real="$(find_real_gitleaks)" || status=$?
  [[ $status -eq 2 ]] && return 0
  [[ $status -ne 0 ]] && return 1
  local r="$work/real"
  new_real_repo "$r"
  printf 'token = "%s"\n' "$(fake_token)" >"$r/Leak.kt"
  git -C "$r" add -A
  git -C "$r" commit -q -m leak
  local leak_sha out
  leak_sha="$(git -C "$r" rev-parse HEAD)"
  out="$(real_scan "$r" "$real")" && return 1
  grep -q 'leaks found' <<<"$out" || return 1
  printf '%s:Leak.kt:github-pat:1\n' "$leak_sha" >"$r/.gitleaksignore"
  git -C "$r" add -A
  git -C "$r" commit -q -m "ignore the leak"
  out="$(real_scan "$r" "$real")" && return 1
  grep -q 'leaks found' <<<"$out"
}

real_gitleaks_still_fails_when_a_pull_request_hides_the_diff_with_gitattributes() {
  local real status=0
  real="$(find_real_gitleaks)" || status=$?
  [[ $status -eq 2 ]] && return 0
  [[ $status -ne 0 ]] && return 1
  local r="$work/real-attributes"
  new_real_repo "$r"
  printf 'Leak.kt -diff\n' >"$r/.gitattributes"
  git -C "$r" add -A
  git -C "$r" commit -q -m attributes
  printf 'token = "%s"\n' "$(fake_token)" >"$r/Leak.kt"
  git -C "$r" add -A
  git -C "$r" commit -q -m leak
  local out
  out="$(real_scan "$r" "$real")" && return 1
  grep -q 'leaks found' <<<"$out"
}

check "real gitleaks still fails when a pull request adds its own ignore entry" real_gitleaks_still_fails_when_a_pull_request_adds_its_own_ignore_entry
check "real gitleaks still fails when a pull request hides the diff with gitattributes" real_gitleaks_still_fails_when_a_pull_request_hides_the_diff_with_gitattributes
check "pull request uses a temp config that extends the defaults" pull_request_uses_a_temp_config_that_extends_the_defaults
check "pull request takes the ignore file from the base branch" pull_request_takes_the_ignore_file_from_the_base_branch
check "pull request scans only the pull request range" pull_request_scans_only_the_pull_request_range
check "push with a known before scans that range" push_with_a_known_before_scans_that_range
check "push with an unknown before falls back to a full scan" push_with_an_unknown_before_falls_back_to_a_full_scan
check "push with a zero before falls back to a full scan" push_with_a_zero_before_falls_back_to_a_full_scan
check "pull request with an unknown base fails without scanning" pull_request_with_an_unknown_base_fails_without_scanning
check "a finding fails the step" a_finding_fails_the_step
check "policy job serialises runs per ref" policy_job_serialises_runs_per_ref
check "policy job has time for the gitleaks download" policy_job_has_time_for_the_gitleaks_download
check "policy job runs the script and its test" policy_job_runs_the_script_and_its_test

if ((failures > 0)); then
  echo "${failures} check(s) failed."
  exit 1
fi
echo "All secret scan checks passed."
