#!/usr/bin/env bash
set -euo pipefail

fail=0
check_absent() {
  local pattern="$1"
  if grep -RIn --exclude-dir=.git --exclude='static-audit.sh' -E "$pattern" common/src fabric/src neoforge/src >/dev/null 2>&1; then
    echo "FAIL: forbidden legacy pattern found: $pattern"
    grep -RIn --exclude-dir=.git --exclude='static-audit.sh' -E "$pattern" common/src fabric/src neoforge/src || true
    fail=1
  fi
}

check_absent 'net\.catcraft\.ccmc\.internal'
check_absent 'TextUtils|MessageUtils|McUtils|LoggerUtils|RegExUtils|MessageStacker|ChatComponentMixin|ChatListenerMixin'

python - <<'PY'
import json
from pathlib import Path

fabric = json.loads(Path('fabric/src/main/resources/fabric.mod.json').read_text())
assert fabric['id'] == 'catcraft_moderation_companion'
assert fabric['license'] == 'MIT'
assert fabric['environment'] == 'client'
assert fabric['depends']['java'] == '>=25'

neo = Path('neoforge/src/main/resources/META-INF/neoforge.mods.toml').read_text()
for required in (
    'modId="catcraft_moderation_companion"',
    'displayName="CatCraft Companion"',
    'config="ccmc-core.mixins.json"',
    'config="ccmc.mixins.json"',
    'modId="cloth_config"',
    'side="CLIENT"',
):
    assert required in neo, required

print('PASS: Fabric and NeoForge metadata sanity')
PY

if [ "$fail" -ne 0 ]; then exit 1; fi
echo "PASS: static provenance sanity"
