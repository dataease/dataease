"""Launch the official Python TAPD MCP with its own local configuration."""
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True
base = Path(__file__).resolve().parent
sys.path.insert(0, str(base.parent))
from mcp_runtime import execute, load_config, resolve_command

try:
    keys = {'TAPD_ACCESS_TOKEN', 'TAPD_MCP_ENTRY', 'TAPD_API_BASE_URL', 'TAPD_BASE_URL'}
    local = load_config(base, keys, {'TAPD_ACCESS_TOKEN', 'TAPD_MCP_ENTRY'})
    runtime, command = resolve_command(local, 'TAPD_MCP_ENTRY')
    if runtime != 'python':
        raise ValueError('官方 TAPD MCP 需要 Python 解释器')
    env = os.environ.copy()
    for key in ('TAPD_API_USER', 'TAPD_API_PASSWORD', 'TAPD_API_TOKEN',
                'TAPD_ACCESS_TOKEN', 'TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER',
                'CURRENT_USER_NICK', 'TAPD_API_BASE_URL', 'TAPD_BASE_URL', 'BOT_URL'):
        env.pop(key, None)
    env['TAPD_ACCESS_TOKEN'] = local['TAPD_ACCESS_TOKEN']
    env['TAPD_API_BASE_URL'] = local.get('TAPD_API_BASE_URL') or 'https://api.tapd.cn'
    env['TAPD_BASE_URL'] = local.get('TAPD_BASE_URL') or 'https://www.tapd.cn'
    env['PYTHON_DOTENV_DISABLED'] = '1'
    command.extend(['--mode', 'stdio'])
    execute(command, env)
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('官方 TAPD MCP 启动失败；请检查 .codex/tapd_official_mcp/.env.local 的字段、入口及解释器')
