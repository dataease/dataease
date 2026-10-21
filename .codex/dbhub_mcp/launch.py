"""Launch DBHub using the existing read-only configuration."""
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True
base = Path(__file__).resolve().parent
sys.path.insert(0, str(base.parent))
from mcp_runtime import execute, load_config, resolve_command

try:
    local = load_config(base, {'DBHUB_DSN', 'DBHUB_MCP_ENTRY'}, {'DBHUB_DSN', 'DBHUB_MCP_ENTRY'})
    _, command = resolve_command(local, 'DBHUB_MCP_ENTRY')
    env = os.environ.copy()
    env['DSN'] = local['DBHUB_DSN']
    command.extend(['--config', str(base / 'dbhub.toml')])
    execute(command, env)
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('DBHub MCP 启动失败；请检查 .codex/dbhub_mcp/.env.local 的字段、入口及解释器')
