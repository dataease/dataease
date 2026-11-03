"""Offline launcher regression tests; no real credentials or services."""
import json
import os
from pathlib import Path
import runpy
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from mcp_runtime import load_config, resolve_command


class RuntimeTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)

    def entry(self, name, content=''):
        p = self.base / name
        p.write_text(content)
        return str(p)

    def test_runtime_detection(self):
        for name, content, expected in [('index.js', '', 'node'), ('index.mjs', '', 'node'),
                                        ('index.cjs', '', 'node'), ('main.py', '', 'python'),
                                        ('mcp-server', '#!/venv/bin/python\n', 'python')]:
            with self.subTest(name=name):
                cfg = {'ENTRY': self.entry(name, content), 'NODE_BIN': sys.executable, 'PYTHON_BIN': sys.executable}
                runtime, command = resolve_command(cfg, 'ENTRY')
                self.assertEqual(runtime, expected)
                self.assertEqual(command[0], sys.executable)

    def test_explicit_runtime_and_missing_binary(self):
        cfg = {'ENTRY': self.entry('custom'), 'MCP_RUNTIME': 'python', 'PYTHON_BIN': sys.executable}
        self.assertEqual(resolve_command(cfg, 'ENTRY')[0], 'python')
        cfg['PYTHON_BIN'] = '/does-not-exist/python'
        with self.assertRaises(ValueError):
            resolve_command(cfg, 'ENTRY')

    def test_bad_configs_do_not_echo_secrets(self):
        for value in ['TOKEN="secret"\nTOKEN="secret"', 'TOKEN=secret', 'TOKEN=123', 'UNKNOWN="secret"']:
            (self.base / '.env.local').write_text(value)
            with self.assertRaises(ValueError) as error:
                load_config(self.base, {'TOKEN'}, {'TOKEN'})
            self.assertNotIn('secret', str(error.exception))

    def test_launcher_contracts(self):
        for service, runtime in [('tapd', 'node'), ('tapd', 'python'), ('playwright', 'node'), ('dbhub', 'node')]:
            with self.subTest(service=service, runtime=runtime):
                config = {'TAPD_WORKSPACE_ID': '123', 'TAPD_CURRENT_USER': 'tester',
                          'TAPD_API_TOKEN': 'test-token', 'DBHUB_DSN': 'test-dsn'}
                command = [sys.executable, '/fake/entry']
                with patch('mcp_runtime.load_config', return_value=config), \
                     patch('mcp_runtime.resolve_command', return_value=(runtime, command)), \
                     patch('mcp_runtime.execute') as execute, \
                     patch.object(Path, 'mkdir'), \
                     patch.dict(os.environ, {'TAPD_API_USER': 'old', 'TAPD_API_PASSWORD': 'old', 'TAPD_ACCESS_TOKEN': 'old'}):
                    runpy.run_path(str(ROOT / (service + '_mcp') / 'launch.py'), run_name='__main__')
                args, env = execute.call_args.args
                self.assertNotIn('test-token', args)
                self.assertNotIn('test-dsn', args)
                if service == 'tapd':
                    self.assertNotIn('TAPD_API_USER', env)
                    self.assertNotIn('TAPD_API_PASSWORD', env)
                    self.assertEqual(env['TAPD_API_TOKEN'], 'test-token')
                    if runtime == 'python':
                        self.assertEqual(env['TAPD_ACCESS_TOKEN'], 'test-token')
                        self.assertEqual(env['CURRENT_USER_NICK'], 'tester')
                        self.assertEqual(args[-2:], ['--mode', 'stdio'])
                    else:
                        self.assertNotIn('TAPD_ACCESS_TOKEN', env)
                elif service == 'dbhub':
                    self.assertEqual(env['DSN'], 'test-dsn')
                    self.assertIn('--config', args)
                else:
                    self.assertIn('--user-data-dir', args)
                    self.assertIn('--output-dir', args)

    def test_tapd_selects_exactly_one_configured_implementation(self):
        select = runpy.run_path(str(ROOT / 'launch_tapd.py'))['select_launcher']
        with self.assertRaises(FileNotFoundError):
            select(self.base)
        legacy = self.base / 'tapd_mcp'
        official = self.base / 'tapd_official_mcp'
        legacy.mkdir()
        official.mkdir()
        (legacy / '.env.local').touch()
        self.assertEqual(select(self.base), legacy / 'launch.py')
        (official / '.env.local').touch()
        self.assertEqual(select(self.base), official / 'launch.py')
        (legacy / '.env.local').unlink()
        self.assertEqual(select(self.base), official / 'launch.py')

    def test_tapd_does_not_fallback_after_selected_launcher_failure(self):
        main = runpy.run_path(str(ROOT / 'launch_tapd.py'))['main']
        selected = self.base / 'tapd_official_mcp' / 'launch.py'
        with patch.dict(main.__globals__, {'select_launcher': lambda base: selected}), \
             patch('runpy.run_path', side_effect=SystemExit('invalid config')) as run, \
             self.assertRaises(SystemExit):
            main()
        run.assert_called_once_with(str(selected), run_name='__main__')

    def test_official_tapd_uses_only_its_local_token(self):
        (self.base / '.env.local').write_text(
            'TAPD_ACCESS_TOKEN="official-token"\nTAPD_MCP_ENTRY="/fake/entry"\n')

        def local_config(base, allowed, required):
            self.assertEqual(base, ROOT / 'tapd_official_mcp')
            return load_config(self.base, allowed, required)

        inherited = {key: 'unrelated' for key in (
            'TAPD_API_USER', 'TAPD_API_PASSWORD', 'TAPD_API_TOKEN',
            'TAPD_ACCESS_TOKEN', 'TAPD_WORKSPACE_ID', 'TAPD_CURRENT_USER',
            'CURRENT_USER_NICK', 'TAPD_API_BASE_URL', 'TAPD_BASE_URL', 'BOT_URL')}
        with patch('mcp_runtime.load_config', side_effect=local_config), \
             patch('mcp_runtime.resolve_command', return_value=('python', [sys.executable, '/fake/entry'])), \
             patch('mcp_runtime.execute') as execute, patch.dict(os.environ, inherited):
            runpy.run_path(str(ROOT / 'tapd_official_mcp' / 'launch.py'), run_name='__main__')
        args, env = execute.call_args.args
        self.assertNotIn('official-token', args)
        self.assertEqual(env['TAPD_ACCESS_TOKEN'], 'official-token')
        self.assertEqual(env['TAPD_API_BASE_URL'], 'https://api.tapd.cn')
        self.assertEqual(env['TAPD_BASE_URL'], 'https://www.tapd.cn')
        self.assertEqual(env['PYTHON_DOTENV_DISABLED'], '1')
        self.assertEqual(args[-2:], ['--mode', 'stdio'])
        for key in inherited.keys() - {'TAPD_ACCESS_TOKEN', 'TAPD_API_BASE_URL', 'TAPD_BASE_URL'}:
            self.assertNotIn(key, env)

    def test_official_tapd_rejects_missing_token_and_non_python_entry(self):
        (self.base / '.env.local').write_text('TAPD_MCP_ENTRY="/fake/entry"\n')

        def local_config(base, allowed, required):
            return load_config(self.base, allowed, required)

        with patch('mcp_runtime.load_config', side_effect=local_config), \
             patch('mcp_runtime.execute') as execute, self.assertRaises(SystemExit):
            runpy.run_path(str(ROOT / 'tapd_official_mcp' / 'launch.py'), run_name='__main__')
        execute.assert_not_called()
        with patch('mcp_runtime.load_config', return_value={'TAPD_ACCESS_TOKEN': 'test-token'}), \
             patch('mcp_runtime.resolve_command', return_value=('node', ['node', '/fake/entry'])), \
             patch('mcp_runtime.execute') as execute, self.assertRaises(SystemExit):
            runpy.run_path(str(ROOT / 'tapd_official_mcp' / 'launch.py'), run_name='__main__')
        execute.assert_not_called()

    def test_official_tapd_errors_hide_secrets(self):
        with patch('mcp_runtime.load_config', side_effect=ValueError('secret-token')), \
             patch('mcp_runtime.execute') as execute, \
             self.assertRaises(SystemExit) as error:
            runpy.run_path(str(ROOT / 'tapd_official_mcp' / 'launch.py'), run_name='__main__')
        self.assertNotIn('secret-token', str(error.exception))
        execute.assert_not_called()


if __name__ == '__main__':
    unittest.main()
