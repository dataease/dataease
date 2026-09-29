"""Shared local MCP configuration and runtime resolution (Python 3.9+)."""
import json
import os
import shutil
from pathlib import Path

RUNTIME_KEYS = {'MCP_RUNTIME', 'NODE_BIN', 'PYTHON_BIN'}


def load_config(base, allowed, required):
    local = {}
    for number, raw in enumerate((base / '.env.local').read_text().splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith('#'):
            continue
        key, sep, value = line.partition('=')
        key = key.strip()
        if not sep or key not in allowed | RUNTIME_KEYS or key in local:
            raise ValueError(f'配置第 {number} 行存在未知或重复字段')
        try:
            parsed = json.loads(value.strip())
        except ValueError:
            raise ValueError(f'配置第 {number} 行必须使用 JSON 字符串') from None
        if not isinstance(parsed, str):
            raise ValueError(f'配置第 {number} 行必须使用字符串')
        local[key] = parsed
    for key in required:
        if not local.get(key):
            raise ValueError(f'缺少配置字段 {key}')
    return local


def resolve_command(local, entry_key):
    entry = Path(local[entry_key]).expanduser()
    if not entry.is_absolute() or not entry.is_file():
        raise ValueError(f'{entry_key} 必须是存在的文件绝对路径')
    runtime = local.get('MCP_RUNTIME') or 'auto'
    if runtime == 'auto':
        if entry.suffix in {'.js', '.cjs', '.mjs'}:
            runtime = 'node'
        elif entry.suffix == '.py':
            runtime = 'python'
        else:
            with entry.open('rb') as stream:
                shebang = stream.readline(512)
            if shebang.startswith(b'#!') and b'python' in shebang:
                runtime = 'python'
            elif shebang.startswith(b'#!') and b'node' in shebang:
                runtime = 'node'
            else:
                raise ValueError('无法判断入口类型，请明确设置 MCP_RUNTIME')
    if runtime not in {'node', 'python'}:
        raise ValueError('MCP_RUNTIME 仅支持 auto、node、python')
    key, fallback = ('NODE_BIN', 'node') if runtime == 'node' else ('PYTHON_BIN', 'python3')
    binary = shutil.which(str(Path(local.get(key) or fallback).expanduser()))
    if not binary:
        raise ValueError(f'{key} 不是可执行文件，请指定实际解释器路径')
    return runtime, [binary, str(entry)]


def execute(command, env):
    os.execvpe(command[0], command, env)
