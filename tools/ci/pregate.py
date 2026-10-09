#!/usr/bin/env python3
import argparse
import fnmatch
import json
import os
import re
import subprocess
import sys
import tempfile

HUNK_RE = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@")
NAME_STATUS_RE = re.compile(r"^([AMDRCT])\d*\t(.*)$")
ASSERT_RE = re.compile(r"assert|expect\(|XCTAssert|require\.|should\.")

SKIP_TOKENS = ("pytest.mark.skip", "pytest.skip(", "unittest.skip", "xfail",
               "it.skip(", "describe.skip(", "test.skip(", ".only(", "xit(",
               "xdescribe(", "@Ignore", "@Disabled", "t.Skip(", "#[ignore]")
DEBUG_TOKENS = ("console.log(", "debugger;", "breakpoint(", "pdb.set_trace",
                "binding.pry", "System.out.println", "dbg!(")
TEST_DIRS = frozenset(("test", "tests", "__tests__", "spec", "androidTest", "testDebug"))
TEST_NAMES = ("test_*.py", "*_test.py", "*_test.go", "*.test.*", "*.spec.*",
              "*Test.kt", "*Test.java", "*Tests.swift")
DEP_FILES = ("package.json", "pyproject.toml", "go.mod", "Cargo.toml", "build.gradle",
             "build.gradle.kts", "libs.versions.toml", "Gemfile", "Podfile")
SNAP_TOKENS = ("__snapshots__", ".snap", "/snapshots/", "/screenshots/")


def is_test(path):
    if TEST_DIRS.intersection(path.split("/")):
        return True
    base = os.path.basename(path)
    return any(fnmatch.fnmatch(base, pat) for pat in TEST_NAMES)


def is_dependency(path):
    base = os.path.basename(path)
    return base in DEP_FILES or fnmatch.fnmatch(base, "requirements*.txt")


def run_git(args, cwd):
    return subprocess.run(("git", "-c", "core.quotepath=off") + tuple(args), cwd=cwd, stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE, encoding="utf-8", errors="replace")


def per_file_added_removed(cwd, base, head):
    proc = run_git(("diff", "-U0", "--no-color", "--text", "--no-ext-diff", "--no-textconv", base + "..." + head), cwd)
    if proc.returncode != 0:
        raise IOError(proc.stderr.strip())
    files = {}
    path = None
    lineno = 0
    in_header = False
    for line in proc.stdout.splitlines():
        if line.startswith("diff --git "):
            in_header = True
            continue
        if in_header and line.startswith("+++ "):
            path = line[4:].strip()
            if path.startswith("b/"):
                path = path[2:]
            files.setdefault(path, ([], []))
            continue
        if in_header and not line.startswith("@@"):
            continue
        m = HUNK_RE.match(line)
        if m:
            in_header = False
            lineno = int(m.group(1))
            continue
        if path is None:
            continue
        added, removed = files[path]
        if line.startswith("+"):
            added.append((lineno, line[1:]))
            lineno += 1
        elif line.startswith("-"):
            removed.append((lineno, line[1:]))
        elif line.startswith(" ") or line == "":
            lineno += 1
    return files


def scan(base, head, plan_globs, cwd):
    files = per_file_added_removed(cwd, base, head)
    findings = []

    status_paths = set()
    proc = run_git(("diff", "--name-status", "-M", "--no-ext-diff", base + "..." + head), cwd)
    if proc.returncode != 0:
        raise IOError(proc.stderr.strip())
    for line in proc.stdout.splitlines():
        m = NAME_STATUS_RE.match(line)
        if not m:
            continue
        status, rest = m.group(1), m.group(2)
        parts = rest.split("\t")
        old = parts[0]
        new = parts[-1]
        status_paths.add(new)
        if old != new:
            status_paths.add(old)
        if status == "D" and is_test(old):
            findings.append(("TEST_DELETED", old, "test file deleted"))
        elif status == "R" and is_test(old) and not is_test(new):
            findings.append(("TEST_DELETED", old, "test moved to " + new))

    for path, (added, removed) in files.items():
        test = is_test(path)
        for lineno, text in added:
            if any(tok in text for tok in SKIP_TOKENS):
                findings.append(("SKIP_ADDED", "%s:%d" % (path, lineno), text.strip()[:80]))
            if not test and any(tok in text for tok in DEBUG_TOKENS):
                findings.append(("DEBUG_LEFTOVER", "%s:%d" % (path, lineno), text.strip()[:80]))
        if test:
            rem = sum(1 for _, t in removed if ASSERT_RE.search(t))
            add = sum(1 for _, t in added if ASSERT_RE.search(t))
            if rem > add:
                findings.append(("ASSERT_REMOVED", path, "net -%d assertions" % (rem - add)))

    for path in status_paths:
        if any(tok in path for tok in SNAP_TOKENS) or (path.endswith(".png") and is_test(path)):
            findings.append(("BASELINE_CHANGED", path, "snapshot or image baseline"))
        if plan_globs is not None and not any(fnmatch.fnmatch(path, g) for g in plan_globs):
            findings.append(("OUT_OF_PLAN", path, "no plan glob matches"))
        if is_dependency(path):
            added_lines = [t.strip()[:80] for _, t in files.get(path, ([], []))[0] if t.strip()]
            detail = " | ".join(added_lines[:5]) or "dependency file changed"
            findings.append(("NEW_DEPENDENCY", path, detail))

    return sorted(set(findings))


def read_plan(path):
    globs = []
    with open(path) as handle:
        for line in handle:
            line = line.strip()
            if line and not line.startswith("#"):
                globs.append(line)
    return globs


def self_test():
    def git(*args):
        proc = subprocess.run(("git",) + args, cwd=root, check=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                              encoding="utf-8", errors="replace")
        return proc

    with tempfile.TemporaryDirectory() as root:
        git("init", "-q")
        git("config", "user.name", "pregate")
        git("config", "user.email", "pregate@example.com")
        git("config", "commit.gpgsign", "false")
        write = lambda rel, body: _write(root, rel, body)
        write("src/app.py", "print(1)\n")
        write("src/app.js", "var a = 1;\n")
        write("tests/test_app.py", "def test_run():\n    assert 1\n    assert 2\n")
        write("package.json", '{\n  "dependencies": {}\n}\n')
        git("add", "-A")
        git("commit", "-q", "-m", "base")
        git("tag", "base")

        git("checkout", "-q", "-b", "bad")
        write("tests/test_app.py",
              "def test_run():\n    @pytest.mark.skip\n    assert 1\n")
        write("src/app.js", "var a = 1;\nconsole.log(1)\n")
        write("package.json", '{\n  "dependencies": {\n    "left-pad": "1.0.0"\n  }\n}\n')
        write("docs/notes.md", "notes\n")
        git("add", "-A")
        git("commit", "-q", "-m", "bad")
        globs = ["src/*", "tests/*", "package.json"]
        kinds = set(k for k, _, _ in scan("base", "bad", globs, root))
        want = {"SKIP_ADDED", "ASSERT_REMOVED", "DEBUG_LEFTOVER", "NEW_DEPENDENCY", "OUT_OF_PLAN"}
        assert kinds == want, (kinds, want)

        git("checkout", "-q", "-b", "good", "base")
        write("tests/test_app.py", "def test_run():\n    assert 1\n    assert 2\n    assert 3\n")
        git("add", "-A")
        git("commit", "-q", "-m", "good")
        assert scan("base", "good", globs, root) == []
        bad = scan("base", "bad", None, root)
        assert ci_exit_code(bad, "") == 1
        skip_path = [loc for kind, loc, _ in bad if kind == "SKIP_ADDED"][0].split(":")[0]
        body = "## Pre-review gate\n- %s: obsolete case\n" % skip_path
        assert ci_exit_code(bad, body) == 0
        assert ci_exit_code([("NEW_DEPENDENCY", "package.json", "x")], "") == 0
        assert ci_exit_code(bad, "## Pre-review gate\n- tests: everything\n") == 1
        git("checkout", "-q", "bad")
        write(".gitattributes", "*.py -diff\n")
        git("add", "-A")
        git("commit", "-q", "-m", "blind")
        assert "SKIP_ADDED" in set(k for k, _, _ in scan("base", "bad", None, root))

        git("checkout", "-q", "-b", "binary", "base")
        with open(os.path.join(root, "tests", "shot.png"), "wb") as handle:
            handle.write(b"\x89PNG\r\n\x1a\n\x8e\xff\x00\x8e")
        write("tests/test_app.py", "def test_run():\n    @pytest.mark.skip\n    assert 1\n    assert 2\n")
        git("add", "-A")
        git("commit", "-q", "-m", "binary")
        binary_kinds = set(k for k, _, _ in scan("base", "binary", None, root))
        assert {"SKIP_ADDED", "BASELINE_CHANGED"} <= binary_kinds, binary_kinds
    print("self-test ok")


HARD_KINDS = ("TEST_DELETED", "SKIP_ADDED", "ASSERT_REMOVED")


def gate_section(body):
    match = re.search(r"^## Pre-review gate\s*$(.*?)(?=^## |\Z)", body or "", re.M | re.S)
    return match.group(1) if match else ""


def is_justified(path, section):
    return re.search(r"^\s*[-*]\s*`?%s`?\s*:" % re.escape(path), section, re.M) is not None


def ci_exit_code(findings, body):
    justified = gate_section(body)
    unjustified = [f for f in findings
                   if f[0] in HARD_KINDS and not is_justified(f[1].split(":")[0], justified)]
    for kind, location, detail in findings:
        level = "error" if (kind, location, detail) in unjustified else "warning"
        print("::%s::%s %s %s" % (level, kind, location, detail))
    return 1 if unjustified else 0


def ci_context():
    with open(os.environ["GITHUB_EVENT_PATH"]) as handle:
        event = json.load(handle)
    pr = event["pull_request"]
    return pr["base"]["sha"], pr["head"]["sha"], pr.get("body") or ""


def _write(root, rel, body):
    full = os.path.join(root, rel)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as handle:
        handle.write(body)


def main(argv=None):
    parser = argparse.ArgumentParser(description="Scan a git diff for weakened tests and scope drift.")
    parser.add_argument("--base")
    parser.add_argument("--head", default="HEAD")
    parser.add_argument("--plan")
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--ci", action="store_true")
    args = parser.parse_args(argv)
    if args.self_test:
        self_test()
        return 0
    if args.ci:
        base, head, body = ci_context()
        try:
            findings = scan(base, head, None, os.getcwd())
        except IOError as exc:
            print(exc, file=sys.stderr)
            return 2
        return ci_exit_code(findings, body)
    if not args.base:
        parser.error("--base is required")
    plan_globs = read_plan(args.plan) if args.plan else None
    try:
        findings = scan(args.base, args.head, plan_globs, os.getcwd())
    except IOError as exc:
        print(exc, file=sys.stderr)
        return 2
    for kind, location, detail in findings:
        print("%s %s %s" % (kind, location, detail))
    print("pregate: %d findings" % len(findings))
    return 1 if findings else 0


if __name__ == "__main__":
    sys.exit(main())
