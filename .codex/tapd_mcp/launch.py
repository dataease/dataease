"""Launch TAPD MCP with project-local credentials."""
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
        if not sep or key.strip() not in {'TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER', 'TAPD_API_TOKEN', 'TAPD_MCP_ENTRY', 'NODE_BIN'}:
            raise ValueError()
        local[key.strip()] = json.loads(value.strip())
    if not all(isinstance(local.get(k), str) and local[k] for k in ('TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER', 'TAPD_API_TOKEN', 'TAPD_MCP_ENTRY')):
        raise ValueError()
    entry = Path(local['TAPD_MCP_ENTRY']).expanduser()
    node = local.get('NODE_BIN') or shutil.which('node')
    if not entry.is_file() or not node:
        raise ValueError()
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('TAPD MCP 配置无效；请检查 .codex/tapd_mcp/.env.local')

env = os.environ.copy()
env.pop('TAPD_API_USER', None)
env.pop('TAPD_API_PASSWORD', None)
env.update({key: local[key] for key in ('TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER', 'TAPD_API_TOKEN')})
os.execvpe(node, [node, str(entry)], env)
