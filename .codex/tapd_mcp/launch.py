"""Launch Node or Python TAPD MCP using only project-local credentials."""
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True
base = Path(__file__).resolve().parent
sys.path.insert(0, str(base.parent))
from mcp_runtime import execute, load_config, resolve_command

try:
    keys = {'TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER', 'TAPD_API_TOKEN',
            'TAPD_MCP_ENTRY', 'TAPD_API_BASE_URL', 'TAPD_BASE_URL'}
    local = load_config(base, keys, {'TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER',
                                    'TAPD_API_TOKEN', 'TAPD_MCP_ENTRY'})
    runtime, command = resolve_command(local, 'TAPD_MCP_ENTRY')
    env = os.environ.copy()
    for key in ('TAPD_API_USER', 'TAPD_API_PASSWORD', 'TAPD_ACCESS_TOKEN',
                'TAPD_API_BASE_URL', 'TAPD_BASE_URL', 'CURRENT_USER_NICK'):
        env.pop(key, None)
    env.update({key: local[key] for key in ('TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER', 'TAPD_API_TOKEN')})
    if runtime == 'python':
        env['TAPD_ACCESS_TOKEN'] = local['TAPD_API_TOKEN']
        env['CURRENT_USER_NICK'] = local['TAPD_CURRENT_USER']
        env['TAPD_API_BASE_URL'] = local.get('TAPD_API_BASE_URL') or 'https://api.tapd.cn'
        env['TAPD_BASE_URL'] = local.get('TAPD_BASE_URL') or 'https://www.tapd.cn'
        command.extend(['--mode', 'stdio'])
    else:
        for key in ('TAPD_API_BASE_URL', 'TAPD_BASE_URL'):
            if local.get(key):
                env[key] = local[key]
    execute(command, env)
except (OSError, ValueError, KeyError, TypeError):
    sys.exit('TAPD MCP 启动失败；请检查 .codex/tapd_mcp/.env.local 的字段、入口及解释器')
