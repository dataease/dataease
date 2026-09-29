"""Launch Playwright MCP with project-local browser storage."""
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True
base = Path(__file__).resolve().parent
sys.path.insert(0, str(base.parent))
from mcp_runtime import execute, load_config, resolve_command

try:
    local = load_config(base, {'PLAYWRIGHT_MCP_ENTRY', 'TEST_BASE_URL', 'TEST_USERNAME', 'TEST_PASSWORD'},
                        {'PLAYWRIGHT_MCP_ENTRY'})
    _, command = resolve_command(local, 'PLAYWRIGHT_MCP_ENTRY')
    profile = base / 'browser-profile'
    output = base / 'output'
    profile.mkdir(exist_ok=True, mode=0o700)
    output.mkdir(exist_ok=True, mode=0o700)
    command.extend(['--browser', 'chrome', '--user-data-dir', str(profile), '--output-dir', str(output)])
    execute(command, os.environ.copy())
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('Playwright MCP 启动失败；请检查 .codex/playwright_mcp/.env.local 的字段、入口及解释器')
