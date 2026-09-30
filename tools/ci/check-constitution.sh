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
  local grounds='https?://|(^|[^[:alpha:]])([Ii]ssue|[Bb]ug) ?#?[0-9]+|b/[0-9]+|CVE-[0-9]+|[A-Z]{2,}-[0-9]+|[Ll]ine [0-9]+'
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

forbid II.3 "GlobalScope is forbidden. Inject an application CoroutineScope." \
  'GlobalScope' '*.kt'
forbid II.3 "The !! operator is forbidden. Model the null case." \
  '!!' '*.kt'
forbid II.3 "runBlocking is forbidden in production code." \
  'runBlocking' '*/src/*/*.kt' ':!*/src/test/*' ':!*/src/androidTest/*'
forbid II.4 "Hard-coded dispatcher. Inject it with @Dispatcher." \
  '(^|[^[:alnum:]_])Dispatchers\.(IO|Default|Unconfined)' '*/src/*/*.kt' ':!*/src/test/*' ':!*/src/androidTest/*' ':!core/common/*'
forbid II.1 "A feature module depends on another feature's impl module." \
  'projects\.feature\.[A-Za-z0-9_]+\.impl|":feature:[A-Za-z0-9_-]+:impl"' 'feature/*/build.gradle.kts'
forbid II.1 "A core module depends on a feature module." \
  'projects\.feature|":feature:' 'core/*/build.gradle.kts'
forbid II.1 "core:model must stay a pure JVM module." \
  'android' 'core/model/build.gradle.kts'
forbid IV.2 "Mocking libraries are forbidden. Use fakes from core:testing." \
  'io\.mockk|org\.mockito' '*.kt' '*.kts' 'gradle/libs.versions.toml'
forbid V.1 "Dependency version outside the version catalog." \
  '"[[:alnum:]._-]+:[[:alnum:]._-]+:[0-9]' '*.gradle.kts'
forbid I.4 "Possible API key or secret in source." \
  '(^|[^[:alnum:]-])sk-[[:alnum:]_-]{20,}|AIza[[:alnum:]_-]{35}|-----BEGIN [A-Z ]*PRIVATE KEY' \
  ':!tools/ci/check-constitution.sh'
forbid I.3 "UI copy claims an ATS result or a guarantee." \
  '[Yy]our ATS score|ATS score:|[Bb]eats? (the )?ATS|ATS[- ](proof|approved)|[Gg]uaranteed (job|interview|selection|placement|shortlist)' \
  '*/res/values*/strings.xml'
forbid I.5 "Network access needs a constitution amendment first." \
  'android\.permission\.INTERNET' '*AndroidManifest.xml'
forbid I.5 "Network, analytics, or crash SDKs need a constitution amendment first." \
  'okhttp|retrofit|ktor|firebase|crashlytics|analytics|sentry|amplitude|mixpanel' \
  'gradle/libs.versions.toml'
forbid IV.3 "Thread.sleep makes tests slow and flaky. Use runTest and virtual time." \
  'Thread\.sleep' '*.kt'
forbid_ungrounded_comments
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
