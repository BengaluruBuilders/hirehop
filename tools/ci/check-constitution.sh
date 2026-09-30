#!/usr/bin/env bash
set -uo pipefail
cd "$(git rev-parse --show-toplevel)"

failures=0

forbid() {
  local article="$1" message="$2" pattern="$3"
  shift 3
  local matches rc
  matches="$(git grep -nE "$pattern" -- "$@")"
  rc=$?
  if ((rc > 1)); then
    echo "::error title=Constitution ${article}::The check itself failed (git grep exit ${rc})."
    failures=$((failures + 1))
  elif [[ -n "$matches" ]]; then
    echo "::error title=Constitution ${article}::${message}"
    echo "$matches"
    echo
    failures=$((failures + 1))
  fi
}

forbid_ungrounded_comments() {
  local grounds='https?://|(^|[^[:alpha:]])([Ii]ssue|[Bb]ug) ?#?[0-9]+|b/[0-9]+|CVE-[0-9]+|KT-[0-9]+|[Ll]ine [0-9]+'
  local matches
  matches="$(git grep -nE '^[[:space:]]*(//|/\*|\*)|[[:space:]]//[[:space:]]' -- '*.kt' '*.kts' |
    grep -vE "^[^:]+:[0-9]+:.*(${grounds})")"
  if [[ -n "$matches" ]]; then
    echo "::error title=Constitution III.2::Comment without an external ground. Rename the code or cite the ground."
    echo "$matches"
    echo
    failures=$((failures + 1))
  fi
}

base_ref() {
  if [[ -n "${BASE_REF:-}" ]]; then
    echo "$BASE_REF"
  elif git rev-parse --verify -q main >/dev/null; then
    echo main
  fi
}

forbid_new_violations() {
  local article="$1" message="$2" pattern="$3" base
  base="$(base_ref)"
  [[ -z "$base" ]] && return
  git rev-parse --verify -q "$base" >/dev/null || return
  local matches
  matches="$(git diff -U0 "$base" -- '*.kt' '*.kts' '*.xml' |
    grep -E '^\+' | grep -vE '^\+\+\+' | sed -E 's/^\+//' | grep -E "$pattern")"
  if [[ -n "$matches" ]]; then
    echo "::error title=Constitution ${article}::${message}"
    echo "$matches"
    echo
    failures=$((failures + 1))
  fi
}

forbid II.3 "GlobalScope is forbidden. Inject an application CoroutineScope." \
  'GlobalScope' '*.kt'
forbid II.3 "The !! operator is forbidden. Model the null case." \
  '!!' '*.kt'
production_kotlin=('*/src/*/*.kt' ':!*/src/test/*' ':!*/src/androidTest/*' ':!*/src/testFixtures/*' ':!core/testing/*')
forbid II.3 "runBlocking is forbidden in production code." \
  'runBlocking' "${production_kotlin[@]}"
forbid II.4 "Hard-coded dispatcher. Inject it with @Dispatcher." \
  '(^|[^[:alnum:]_])Dispatchers\.(IO|Default|Unconfined)' "${production_kotlin[@]}" ':!core/common/*'
forbid II.1 "A feature module depends on another feature's impl module." \
  'projects\.feature\.[A-Za-z0-9_]+\.impl|":feature:[A-Za-z0-9_-]+:impl"' 'feature/*/build.gradle.kts'
forbid II.1 "A core module depends on a feature module." \
  'projects\.feature|":feature:' 'core/*/build.gradle.kts'
forbid II.1 "core:model must stay a pure JVM module." \
  'plugins\.(hirehop\.)?android|com\.android' 'core/model/build.gradle.kts'
forbid IV.2 "Mocking libraries are forbidden. Use fakes from core:testing." \
  'io\.mockk|org\.mockito' '*.kt' '*.kts' 'gradle/libs.versions.toml'
forbid V.1 "Dependency version outside the version catalog." \
  '"[[:alnum:]._-]+:[[:alnum:]._-]+:[0-9]' '*.gradle.kts'
forbid I.4 "Possible API key or secret in source." \
  '(^|[^[:alnum:]-])sk-[[:alnum:]_-]{20,}|AIza[[:alnum:]_-]{35}|-----BEGIN [A-Z ]*PRIVATE KEY' \
  ':!tools/ci/check-constitution.sh'
forbid I.3 "UI copy claims an ATS result or a guarantee." \
  '[Yy]our ATS score|ATS score:|[Bb]eats? (the )?ATS|ATS[- ](proof|approved)|[Gg]uaranteed (job|interview|selection|placement|shortlist)' \
  '*/res/values*/*.xml'
forbid I.5 "Network access needs a constitution amendment first." \
  'android\.permission\.INTERNET' '*AndroidManifest.xml'
forbid I.5 "Backups would copy candidate data off the device." \
  'allowBackup="true"' '*AndroidManifest.xml'
forbid I.5 "Network, analytics, or crash SDKs need a constitution amendment first." \
  'okhttp|retrofit|ktor|firebase|crashlytics|analytics|sentry|amplitude|mixpanel' \
  'gradle/libs.versions.toml'
forbid IV.3 "Thread.sleep makes tests slow and flaky. Use runTest and virtual time." \
  'Thread\.sleep' '*.kt'
forbid III.3 "Production code must not reference a test double or a fake." \
  '^import .*\.Fake[A-Za-z0-9_]+' '*/src/*/*.kt' ':!*/src/test/*' ':!*/src/androidTest/*' ':!core/testing/*'
forbid_new_violations II.5 "A feature must build its UI from the shared Hh* components, not from raw Material components." \
  '^import androidx\.compose\.material3\.(Button|OutlinedButton|TextButton|FilledTonalButton|ElevatedButton|IconButton|FilledIconButton|TextField|OutlinedTextField|Card|ElevatedCard|OutlinedCard|Surface|Scaffold|Snackbar|SnackbarHost|AlertDialog|BasicAlertDialog|TopAppBar|CenterAlignedTopAppBar|LargeTopAppBar|MediumTopAppBar|TopAppBarDefaults|ListItem|Checkbox|TriStateCheckbox|RadioButton|Switch|ModalBottomSheet|BottomSheetScaffold|Chip|AssistChip|FilterChip|InputChip|SuggestionChip|Badge|Divider|HorizontalDivider|VerticalDivider|LinearProgressIndicator|CircularProgressIndicator|MaterialTheme)'
forbid_new_violations II.5 "Read design-system tokens through HhTheme, not through MaterialTheme." \
  'MaterialTheme\.(colorScheme|typography|shapes|dimens)'
forbid_ungrounded_comments

forbid_lint_baseline_growth() {
  [[ -z "${BASE_REF:-}" ]] && return
  local diff added removed
  diff="$(git diff --diff-filter=M "${BASE_REF}...HEAD" -- '*lint-baseline.xml')"
  added="$(grep -cE '^\+[[:space:]]*<issue$' <<<"$diff")"
  removed="$(grep -cE '^-[[:space:]]*<issue$' <<<"$diff")"
  if ((added > removed)); then
    echo "::error title=Constitution III.3::Lint baselines grew by $((added - removed)) issue(s). Fix the new warnings."
    failures=$((failures + 1))
  fi
}
forbid_lint_baseline_growth

forbid_unrecorded_screenshot_baselines() {
  local base
  base="$(base_ref)"
  [[ -z "$base" ]] && return
  git rev-parse --verify -q "$base" >/dev/null || return
  local image_changes kt_changes
  image_changes="$(git diff --name-only "$base" -- '*.png' | wc -l | tr -d ' ')"
  [[ "$image_changes" == "0" ]] && return
  kt_changes="$(git diff --name-only "$base" -- '*.kt' '*.kts' | wc -l | tr -d ' ')"
  if [[ "$kt_changes" == "0" ]]; then
    echo "::error title=Constitution II.2::Screenshot baselines changed with no Kotlin change. Re-record deliberately, not reflexively."
    failures=$((failures + 1))
  fi
}
forbid_unrecorded_screenshot_baselines

require_gradle_property() {
  local key="$1" value="$2"
  if ! grep -qxF "${key}=${value}" gradle.properties; then
    echo "::error title=Constitution V.4::gradle.properties must set ${key}=${value}"
    failures=$((failures + 1))
  fi
}
require_gradle_property org.gradle.workers.max 3
require_gradle_property org.gradle.caching true
require_gradle_property org.gradle.configuration-cache true
require_gradle_property org.gradle.configuration-cache.problems fail
require_gradle_property org.gradle.isolated-projects true

if ((failures > 0)); then
  echo "Constitution check failed: ${failures} rule(s) broken. See docs/CONSTITUTION.md."
  exit 1
fi
echo "Constitution check passed."
