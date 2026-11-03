"""Select one TAPD implementation by the presence of its local configuration."""
from pathlib import Path
import runpy
import sys

sys.dont_write_bytecode = True


def select_launcher(base):
    for directory in ('tapd_official_mcp', 'tapd_mcp'):
        if (base / directory / '.env.local').is_file():
            return base / directory / 'launch.py'
    raise FileNotFoundError('没有 TAPD 本地配置')


def main():
    try:
        launcher = select_launcher(Path(__file__).resolve().parent)
    except FileNotFoundError:
        sys.exit('请先配置 .codex/tapd_official_mcp/.env.local 或 .codex/tapd_mcp/.env.local')
    # A configured but broken service must report its error, never use another identity.
    runpy.run_path(str(launcher), run_name='__main__')


if __name__ == '__main__':
    main()
