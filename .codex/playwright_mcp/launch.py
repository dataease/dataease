"""Launch Playwright MCP with project-local browser storage."""
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
        if not sep or key.strip() not in {'PLAYWRIGHT_MCP_ENTRY', 'TEST_BASE_URL', 'TEST_USERNAME', 'TEST_PASSWORD', 'NODE_BIN'}:
            raise ValueError()
        local[key.strip()] = json.loads(value.strip())
    entry = Path(local['PLAYWRIGHT_MCP_ENTRY']).expanduser()
    node = local.get('NODE_BIN') or shutil.which('node')
    if not entry.is_file() or not node:
        raise ValueError()
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('Playwright MCP 配置无效；请检查 .codex/playwright_mcp/.env.local')

profile = base / 'browser-profile'
output = base / 'output'
profile.mkdir(exist_ok=True, mode=0o700)
output.mkdir(exist_ok=True, mode=0o700)
os.execvpe(node, [node, str(entry), '--browser', 'chrome', '--user-data-dir', str(profile), '--output-dir', str(output)], os.environ.copy())
