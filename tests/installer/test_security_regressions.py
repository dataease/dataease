"""Installer security regressions. Run in a disposable Linux container, never on a host install.

python3 -m unittest discover -s tests/installer -v
Requires bash, gettext's envsubst and GNU coreutils. External service/Docker/network calls are stubbed.
"""
import io
import json
import os
from pathlib import Path
import re
import shlex
import shutil
import subprocess
import tarfile
import tempfile
import unittest

ROOT = Path(os.environ.get('INSTALLER_ROOT', Path(__file__).resolve().parents[2] / 'installer'))


class InstallerSecurity(unittest.TestCase):
    def setUp(self):
        self.scratch = tempfile.TemporaryDirectory(prefix='de-security-')
        self.root = Path(self.scratch.name)
        self.base = self.root / 'base'
        self.runtime = self.base / 'dataease3.0'
        self.runtime.mkdir(parents=True)
        self.package = self.root / 'package'
        self.package.mkdir()
        shutil.copytree(ROOT / 'dataease', self.package / 'dataease')
        self.bin = self.root / 'bin'
        self.bin.mkdir()
        self.calls = self.root / 'calls.jsonl'
        self.tool = self.root / 'dectl'
        self.tool.write_text('DE_BASE=' + str(self.base) + '\n')
        self.env = {k: v for k, v in os.environ.items() if not k.startswith('DE_')}
        self.env.update(PATH=str(self.bin) + ':' + os.environ['PATH'], TEST_CALLS=str(self.calls),
                        TEST_ROOT=str(self.root), TMPDIR=str(self.root), LC_ALL='C.UTF-8',
                        DE_BASE=str(self.base), DE_INSTALL_MODE='community', DE_EXTERNAL_DB='false',
                        DE_DB_TYPE='mysql', DE_SPRING_PROFILE='mysql', DE_DB_HOST='mysql-de',
                        DE_DB_PORT='3306', DE_DB_USER='root', DE_DB_PASSWORD='fixture-secret',
                        DE_DB_DATABASE='dataease')
        self.mock('service', 'sys.exit(0)')
        self.mock('systemctl', 'sys.exit(0)')
        self.mock('docker', 'sys.exit(0)')
        self.mock('chkconfig', 'sys.exit(1)')
        self.mock('rm', '''
for argument in sys.argv[1:]:
    if argument.startswith('/') and not argument.startswith(os.environ['TEST_ROOT'] + '/'):
        sys.exit(99)
os.execv('/bin/rm', ['/bin/rm'] + sys.argv[1:])
''')
        (self.runtime / '.env').write_text('DE_DB_PASSWORD=fixture-secret\n')
        (self.runtime / 'docker-compose.yml').write_text('services: {}\n')

    def tearDown(self):
        self.scratch.cleanup()

    def mock(self, name, body):
        script = self.bin / name
        script.write_text('#!/usr/bin/env python3\nimport json,os,sys,shutil\n'
                          'with open(os.environ["TEST_CALLS"], "a") as log:\n'
                          '    log.write(json.dumps([os.path.basename(sys.argv[0])] + sys.argv[1:]) + "\\n")\n'
                          + body + '\n')
        script.chmod(0o755)

    def execute(self, source, suffix='', input=None, env=None):
        return subprocess.run(['bash', '-c', 'umask 022\n' + source + '\n' + suffix],
                              cwd=self.package, env={**self.env, **(env or {})}, input=input,
                              text=True, capture_output=True, timeout=20)

    def functions(self, name):
        source = (ROOT / name).read_text()
        source = re.sub(r'\nmain\s*$', '\n', source)
        if name == 'dectl':
            source = source[source.index('function usage()'):]
        return source

    def installer(self, suffix='prepare_de_run_base', env=None):
        prefix = ('CURRENT_DIR=' + shlex.quote(str(self.package)) + '\n'
                  'DE_RUN_BASE=' + shlex.quote(str(self.runtime)) + '\n'
                  'CONF_FOLDER="$DE_RUN_BASE/conf"\nTEMPLATES_FOLDER="$DE_RUN_BASE/templates"\n'
                  'copy_db_driver() { return 0; }\n')
        return self.execute(self.functions('install.sh'), prefix + suffix, env=env)

    def call_list(self):
        return [json.loads(line) for line in self.calls.read_text().splitlines()] if self.calls.exists() else []

    def uninstall(self, assignment):
        self.tool.write_text(assignment)
        source = (ROOT / 'uninstall.sh').read_text()
        source = source.replace('/usr/bin/dectl', str(self.tool))
        source = source.replace('/usr/local/bin/dectl', str(self.root / 'fallback-dectl'))
        source = source.replace('/etc/systemd/system/dataease.service', str(self.root / 'service'))
        source = source.replace('/etc/init.d/dataease', str(self.root / 'init'))
        return self.execute(source, input='y\n')

    def test_syntax(self):
        for name in ['install.sh', 'dectl', 'uninstall.sh', 'quick_start.sh']:
            with self.subTest(script=name):
                result = subprocess.run(['bash', '-n', str(ROOT / name)], capture_output=True)
                self.assertEqual(result.returncode, 0, result.stderr)

    def test_empty_internal_password_fails_before_runtime_write(self):
        before = (self.runtime / '.env').read_bytes()
        result = self.installer(env={'DE_DB_PASSWORD': ''})
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual((self.runtime / '.env').read_bytes(), before)
        self.assertFalse((self.runtime / 'conf').exists())

    def test_unset_internal_password_fails(self):
        result = self.installer('unset DE_DB_PASSWORD; prepare_de_run_base')
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse((self.runtime / 'conf').exists())

    def test_upgrade_empty_password_fails_before_stopping_service(self):
        (self.package / 'install.conf').write_text('DE_BASE=' + str(self.base) +
                                                  '\nDE_EXTERNAL_DB=false\nDE_DB_PASSWORD=\n')
        (self.runtime / '.env').write_text('DE_EXTERNAL_DB=false\nDE_DB_PASSWORD=\n')
        (self.package / 'dectl').write_text('DE_BASE=/opt\n')
        local_bin = self.root / 'local-bin'
        local_bin.mkdir()
        self.mock('dectl', "print('v3.3.0')")
        source = self.functions('install.sh').replace('/usr/bin/dectl', str(self.tool))
        source = source.replace('/usr/local/bin', str(local_bin))
        source = source.replace('/etc/systemd/system/dataease.service', str(self.root / 'service'))
        result = self.execute(source, 'CURRENT_DIR=' + shlex.quote(str(self.package)) +
                              '\ncheck_and_prepare_env_params')
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(any(call[0] in ['service', 'systemctl'] or call == ['dectl', 'stop']
                             for call in self.call_list()))
        self.assertEqual((self.runtime / '.env').read_text(), 'DE_EXTERNAL_DB=false\nDE_DB_PASSWORD=\n')

    def test_external_empty_password_is_still_supported(self):
        result = self.installer(env={'DE_EXTERNAL_DB': 'true', 'DE_DB_PASSWORD': ''})
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('MYSQL_ROOT_PASSWORD=', (self.runtime / 'conf/mysql.env').read_text())

    def assert_private(self, path):
        self.assertEqual(path.stat().st_mode & 0o777, 0o600, path)
        # A real unprivileged process cannot read the file; directory permissions are open.
        self.root.chmod(0o755)
        result = subprocess.run(['runuser', '-u', 'nobody', '--', 'cat', str(path)], capture_output=True)
        self.assertNotEqual(result.returncode, 0, path)

    def test_new_and_old_config_copies_are_private(self):
        conf = self.runtime / 'conf'
        conf.mkdir()
        old = conf / 'application-pg.yml'
        old.write_text('password: old-fixture-secret\n')
        old.chmod(0o644)
        (self.runtime / '.env').chmod(0o644)
        result = self.installer()
        self.assertEqual(result.returncode, 0, result.stderr)
        for path in [self.runtime / '.env', conf / 'mysql.env', conf / 'application.yml',
                     conf / 'application-mysql.yml', old]:
            self.assert_private(path)
        self.assertIn('fixture-secret', (conf / 'application-mysql.yml').read_text())
        self.assertIn('fixture-secret', (conf / 'mysql.env').read_text())
        self.assertEqual(old.read_text(), 'password: old-fixture-secret\n')

    def test_all_supported_profiles_keep_credentials_private(self):
        for profile in ['mysql', 'pg', 'oracle', 'dm', 'kingbase', 'sqlserver', 'greatsql']:
            with self.subTest(profile=profile):
                result = self.installer(env={'DE_EXTERNAL_DB': 'true', 'DE_DB_TYPE': profile,
                                             'DE_SPRING_PROFILE': profile,
                                             'DE_DB_PASSWORD': 'fixture=a&$\\"b'})
                self.assertEqual(result.returncode, 0, result.stderr)
                path = self.runtime / ('conf/application-' + profile + '.yml')
                self.assert_private(path)
                self.assertIn('fixture=a&$\\"b', path.read_text())

    def test_render_destination_is_private_before_secret_is_written(self):
        self.mock('envsubst', '''
if os.fstat(1).st_mode & 0o077:
    sys.exit(65)
os.execv('/usr/bin/envsubst', ['/usr/bin/envsubst'])
''')
        result = self.installer()
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_render_failure_preserves_previous_file(self):
        target = self.runtime / 'saved.yml'
        target.write_text('previous\n')
        result = self.installer('write_private_config "$DE_RUN_BASE/saved.yml" bash -c '\
                                + shlex.quote('printf partial; exit 9'))
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(target.read_text(), 'previous\n')
        self.assertEqual(list(self.runtime.glob('.de-config.*')), [])

    def test_symlink_config_is_rejected_without_touching_target(self):
        target = self.root / 'outside'
        target.write_text('sentinel')
        (self.runtime / '.env').unlink()
        (self.runtime / '.env').symlink_to(target)
        result = self.installer()
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(target.read_text(), 'sentinel')

    def test_apisix_rewrite_keeps_database_password_private(self):
        conf = self.runtime / 'conf'
        conf.mkdir()
        (conf / 'application.yml').write_text('key: DE_APISIX_KEY\n')
        (conf / 'application-pg.yml').write_text('password: fixture-secret\n')
        apisix = self.runtime / 'apisix/apisix_conf'
        apisix.mkdir(parents=True)
        (apisix / 'config.yaml').write_text('key: DE_APISIX_KEY\n')
        result = self.execute(self.functions('dectl'),
                              'DE_RUNNING_BASE=' + shlex.quote(str(self.runtime)) + '\n_prepare_apisix')
        self.assertEqual(result.returncode, 0, result.stderr)
        for path in [self.runtime / '.env', conf / 'application.yml', conf / 'application-pg.yml']:
            self.assert_private(path)
        self.assertIn('DE_DB_PASSWORD=fixture-secret', (self.runtime / '.env').read_text())
        self.assertNotIn('DE_APISIX_KEY', (apisix / 'config.yaml').read_text())

    def test_uninstall_rejects_bad_paths_before_stopping_services(self):
        cases = ['', '/', 'relative', str(self.base) + '/../base', str(self.base) + '/.',
                 '\"\"', str(self.base) + '\nDE_BASE=' + str(self.base)]
        for value in cases:
            with self.subTest(value=value):
                self.calls.unlink(missing_ok=True)
                result = self.uninstall('DE_BASE=' + value + '\n')
                self.assertNotEqual(result.returncode, 0)
                self.assertEqual(self.call_list(), [])
                self.assertTrue(self.runtime.is_dir())

    def test_uninstall_requires_an_instance_and_rejects_links(self):
        alias = self.root / 'alias'
        alias.symlink_to(self.base, target_is_directory=True)
        for value in [str(self.root), str(alias)]:
            with self.subTest(value=value):
                result = self.uninstall('DE_BASE=' + value + '\n')
                self.assertNotEqual(result.returncode, 0)
                self.assertEqual(self.call_list(), [])
        (self.runtime / '.env').unlink()
        (self.runtime / '.env').symlink_to(self.tool)
        self.assertNotEqual(self.uninstall('DE_BASE=' + str(self.base) + '\n').returncode, 0)
        self.assertEqual(self.call_list(), [])

    def test_uninstall_spaces_glob_equals_and_quotes_are_one_argument(self):
        for quoting in ['', "'", '"']:
            with self.subTest(quoting=quoting):
                base = self.root / ('space [*] = ' + str(len(quoting)))
                runtime = base / 'dataease3.0'
                runtime.mkdir(parents=True, exist_ok=True)
                (runtime / '.env').touch()
                (runtime / 'docker-compose.yml').touch()
                sentinel = self.root / 'space'
                sentinel.write_text('keep')
                self.calls.unlink(missing_ok=True)
                result = self.uninstall('DE_BASE=' + quoting + str(base) + quoting + '\n')
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertFalse(runtime.exists())
                self.assertEqual(sentinel.read_text(), 'keep')
                removals = [call for call in self.call_list() if call[0] == 'rm']
                self.assertEqual(removals, [['rm', '-rf', '--', str(runtime), str(self.tool)]])

    def test_uninstall_normalizes_legacy_slashes(self):
        value = str(self.base).replace('/base', '//base//')
        result = self.uninstall('DE_BASE=' + value + '\n')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertFalse(self.runtime.exists())

    def releases(self, entries):
        self.env['TEST_RELEASE_JSON'] = json.dumps(entries)
        self.mock('curl', '''
if any('api.github.com' in arg for arg in sys.argv):
    print(os.environ['TEST_RELEASE_JSON'])
elif '-o' in sys.argv:
    shutil.copyfile(os.environ['TEST_ARCHIVE'], sys.argv[sys.argv.index('-o') + 1])
sys.exit(int(os.environ.get('TEST_CURL_EXIT', '0')))
''')

    def release_entry(self, version):
        return {'tag_name': version, 'name': version, 'prerelease': False, 'draft': False}

    def quick(self):
        archive = self.root / 'installer.tar.gz'
        with tarfile.open(archive, 'w:gz') as output:
            body = b'printf installed > "$TEST_ROOT/installed"\n'
            info = tarfile.TarInfo('dataease-online-installer-v3.3.0-ce/install.sh')
            info.size = len(body)
            output.addfile(info, io.BytesIO(body))
        return self.execute((ROOT / 'quick_start.sh').read_text(), env={'TEST_ARCHIVE': str(archive)})

    def dectl_version(self):
        return self.execute(self.functions('dectl'),
                            '_get_available_server() { server_url=github.com; }\n'
                            '_get_latest_version || exit $?\nprintf "RESULT:%s" "$latest_version"')

    def test_both_release_entries_reject_command_and_encoding_variants(self):
        values = ['echo bad;v3.3.0', '$(touch ' + str(self.root / 'injected') + ')v3.3.0',
                  'v3.3.0\n', 'v3.٣.٠', 'v3.3.0-rc1', 'v3.3.0 trailing', None]
        for value in values:
            for entry in ['quick', 'dectl']:
                with self.subTest(value=value, entry=entry):
                    (self.root / 'injected').unlink(missing_ok=True)
                    (self.root / 'installed').unlink(missing_ok=True)
                    self.releases([self.release_entry(value)])
                    result = self.quick() if entry == 'quick' else self.dectl_version()
                    self.assertNotEqual(result.returncode, 0, result.stderr)
                    self.assertFalse((self.root / 'injected').exists())
                    self.assertFalse((self.root / 'installed').exists())

    def test_valid_tag_display_name_pagination_and_prerelease(self):
        entries = [{**self.release_entry('v3.4.0'), 'prerelease': True},
                   {**self.release_entry('v3.5.0'), 'draft': True},
                   {**self.release_entry('v3.3.0'), 'name': 'DataEase Release 3.3'}]
        self.releases(entries)
        for entry in ['quick', 'dectl']:
            with self.subTest(entry=entry):
                result = self.quick() if entry == 'quick' else self.dectl_version()
                self.assertEqual(result.returncode, 0, result.stderr)
                if entry == 'quick':
                    self.assertTrue((self.root / 'installed').exists())
                else:
                    self.assertIn('RESULT:v3.3.0', result.stdout)
        (self.root / 'installed').unlink()
        self.mock('curl', '''
if any('api.github.com' in arg for arg in sys.argv):
    print(os.environ['TEST_RELEASE_JSON'] if any('page=2' in arg for arg in sys.argv) else '[]')
sys.exit(0)
''')
        result = self.dectl_version()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('RESULT:v3.3.0', result.stdout)

    def test_release_network_failure_and_version_failure_block_upgrade(self):
        self.releases([self.release_entry('v3.3.0')])
        self.env['TEST_CURL_EXIT'] = '7'
        self.assertNotEqual(self.quick().returncode, 0)
        self.assertNotEqual(self.dectl_version().returncode, 0)
        result = self.execute(self.functions('dectl'),
                              '_get_current_version() { current_version=v3.2.0; }\n'
                              '_get_available_server() { server_url=github.com; }\nupgrade')
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(any('-o' in call for call in self.call_list()))


if __name__ == '__main__':
    if os.uname().sysname != 'Linux' or os.geteuid() != 0:
        raise SystemExit('Run in a disposable Linux container as root; external calls are stubbed.')
    unittest.main()
