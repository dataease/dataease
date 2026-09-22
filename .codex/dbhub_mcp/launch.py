"""Launch a locally installed DBHub in read-only mode."""
import json
import os
import shutil
import sys
from pathlib import Path

base = Path(__file__).resolve().parent
try:
    local = {}
    for raw in (base / '.env.local').read_text().splitlines():
        line = raw.strip()
        if not line or line.startswith('#'):
            continue
        key, sep, value = line.partition('=')
        if not sep or key.strip() not in {'DBHUB_DSN', 'DBHUB_MCP_ENTRY', 'NODE_BIN'}:
            raise ValueError()
        local[key.strip()] = json.loads(value.strip())
    if not all(isinstance(local.get(k), str) and local[k] for k in ('DBHUB_DSN', 'DBHUB_MCP_ENTRY')):
        raise ValueError()
    entry = Path(local['DBHUB_MCP_ENTRY']).expanduser()
    node = local.get('NODE_BIN') or shutil.which('node')
    if not entry.is_file() or not node:
        raise ValueError()
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('DBHub MCP 配置无效；请检查 .codex/dbhub_mcp/.env.local')

env = os.environ.copy()
env['DSN'] = local['DBHUB_DSN']
os.execvpe(node, [node, str(entry), '--config', str(base / 'dbhub.toml')], env)
