#!/usr/bin/env bash
set -euo pipefail

fail=0
check_absent() {
  local pattern="$1"
  if grep -RIn --exclude-dir=.git --exclude='static-audit.sh' -E "$pattern" src/main/java src/main/resources >/dev/null 2>&1; then
    echo "FAIL: forbidden legacy pattern found: $pattern"
    grep -RIn --exclude-dir=.git --exclude='static-audit.sh' -E "$pattern" src/main/java src/main/resources || true
    fail=1
  fi
}

check_absent 'net\.catcraft\.ccmc\.internal'
check_absent 'TextUtils|MessageUtils|McUtils|LoggerUtils|RegExUtils|MessageStacker|ChatComponentMixin|ChatListenerMixin'

python - <<'PY'
import json
from pathlib import Path
p=Path('src/main/resources/fabric.mod.json')
d=json.loads(p.read_text())
assert d['id']=='catcraft_moderation_companion'
assert d['license']=='MIT'
assert d['environment']=='client'
assert d['depends']['java']=='>=25'
print('PASS: fabric.mod.json sanity')
PY

if [ "$fail" -ne 0 ]; then exit 1; fi
echo "PASS: static provenance sanity"
