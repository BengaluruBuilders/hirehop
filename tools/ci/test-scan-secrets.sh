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
