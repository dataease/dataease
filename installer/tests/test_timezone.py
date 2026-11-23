"""Offline deployment tests. No Docker daemon or application service is started."""

import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


INSTALLER = Path(__file__).resolve().parents[1]
HELPER = INSTALLER / "dataease/bin/timezone.sh"
ZONEINFO = Path("/usr/share/zoneinfo")


class TimezoneTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.bin = self.root / "bin"
        self.bin.mkdir()
        self.localtime = self.root / "localtime"
        self.timezone = self.root / "timezone"
        self.env = dict(os.environ)
        for key in list(self.env):
            if key.startswith("DE_"):
                del self.env[key]
        self.env["PATH"] = str(self.bin) + os.pathsep + self.env["PATH"]
        self.stub("timedatectl", "exit 1")

    def stub(self, name, body):
        path = self.bin / name
        path.write_text("#!/bin/bash\n" + body + "\n")
        path.chmod(0o755)

    def run_shell(self, body, **env):
        return subprocess.run(
            ["bash", "-c", body],
            env={**self.env, **env},
            capture_output=True,
            text=True,
            timeout=10,
        )

    def resolve(self, prefix="", **env):
        return self.run_shell(
            f'source "{HELPER}"\n{prefix}\n'
            f'de_timezone_resolve "{ZONEINFO}" "{self.localtime}" "{self.timezone}" '
            '|| exit $?\nprintf "%s" "$DE_RESOLVED_TIMEZONE"',
            **env,
        )

    def test_explicit_regions_and_utc(self):
        for zone in ("Asia/Shanghai", "Etc/UTC", "Europe/Berlin", "America/New_York"):
            with self.subTest(zone=zone):
                result = self.resolve(DE_TIMEZONE=zone)
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertEqual(result.stdout, zone)

    def test_invalid_and_path_traversal_rejected(self):
        for zone in ("CST-unknown", "../etc/passwd", "Asia/Shanghai\nDE_OTHER=value"):
            with self.subTest(zone=zone):
                self.assertNotEqual(self.resolve(DE_TIMEZONE=zone).returncode, 0)

    def test_timedatectl_and_stale_resolved_value(self):
        shutil.copyfile(ZONEINFO / "Europe/Berlin", self.localtime)
        self.stub("timedatectl", "echo Europe/Berlin")
        result = self.resolve(DE_TIMEZONE="host", DE_RESOLVED_TIMEZONE="Asia/Shanghai")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, "Europe/Berlin")

    def test_symlink_without_systemd(self):
        self.localtime.symlink_to(ZONEINFO / "America/New_York")
        result = self.resolve()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, "America/New_York")

    def test_timezone_file_fallback_checks_actual_rules(self):
        shutil.copyfile(ZONEINFO / "Asia/Shanghai", self.localtime)
        self.timezone.write_text("Asia/Shanghai\n")
        result = self.resolve()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, "Asia/Shanghai")
        self.timezone.write_text("Etc/UTC\n")
        self.assertNotEqual(self.resolve().returncode, 0)

    def test_missing_host_configuration_fails(self):
        self.assertNotEqual(self.resolve().returncode, 0)

    def test_persist_preserves_secrets_and_is_idempotent(self):
        env_file = self.root / ".env"
        original = "# preserved\nDE_SYNC_AUTH_SECRET='literal$secret'\nOTHER=value\n"
        env_file.write_text(original + "DE_TIMEZONE=host\nDE_RESOLVED_TIMEZONE=Etc/UTC\n")
        result = self.run_shell(
            f'source "{HELPER}"\n'
            'export DE_TIMEZONE=host DE_RESOLVED_TIMEZONE=Europe/Berlin\n'
            f'de_timezone_save "{env_file}" && de_timezone_save "{env_file}"'
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        content = env_file.read_text()
        self.assertTrue(content.startswith(original))
        self.assertEqual(content.count("DE_TIMEZONE="), 1)
        self.assertEqual(content.count("DE_RESOLVED_TIMEZONE="), 1)
        self.assertIn("DE_RESOLVED_TIMEZONE=Europe/Berlin\n", content)
        self.assertEqual(env_file.stat().st_mode & 0o777, 0o600)
        result = self.run_shell(
            f'source "{HELPER}"\n'
            'export DE_TIMEZONE=host DE_RESOLVED_TIMEZONE=Europe/Berlin\n'
            f'de_timezone_save "{env_file}"'
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(content, env_file.read_text())

    def test_install_invalid_timezone_exits_before_stopping_service(self):
        package = self.root / "package"
        (package / "dataease/bin").mkdir(parents=True)
        for name in ("sync-auth.sh", "timezone.sh"):
            shutil.copyfile(INSTALLER / "dataease/bin" / name, package / "dataease/bin" / name)
        shutil.copyfile(INSTALLER / "install.sh", package / "install.sh")
        (package / "install.conf").write_text(
            f'DE_BASE="{self.root}/deployment"\nDE_TIMEZONE=Invalid/Timezone\n'
        )
        log = self.root / "commands"
        self.stub("systemctl", f'echo "$*" >> "{log}"; exit 99')
        self.stub("docker-compose", f'echo "$*" >> "{log}"; exit 99')
        result = self.run_shell(f'bash "{package}/install.sh"')
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("DE_TIMEZONE", result.stderr)
        self.assertFalse(log.exists())

    def prepare_dectl(self, zone):
        run_base = self.root / "dataease2.0"
        (run_base / "bin").mkdir(parents=True)
        shutil.copyfile(HELPER, run_base / "bin/timezone.sh")
        shutil.copyfile(INSTALLER / "dataease/bin/sync-auth.sh", run_base / "bin/sync-auth.sh")
        shutil.copyfile(INSTALLER / "dataease/docker-compose.yml", run_base / "docker-compose.yml")
        (run_base / ".env").write_text(
            f"DE_TIMEZONE={zone}\nDE_INSTALL_MODE=community\nDE_EXTERNAL_MYSQL=true\n"
        )
        dectl = self.root / "dectl"
        dectl.write_text((INSTALLER / "dectl").read_text().replace("DE_BASE=/opt", f'DE_BASE="{self.root}"'))
        log = self.root / "commands"
        self.stub("docker-compose", f'printf "%s\\n" "$*" >> "{log}"')
        self.stub("docker", 'case "$1" in ps) echo fake-container;; inspect) echo "running healthy";; esac')
        self.stub("sleep", ":")
        return dectl, run_base, log

    def test_dectl_start_reload_persist_timezone(self):
        dectl, run_base, log = self.prepare_dectl("Europe/Berlin")
        for action in ("start", "reload"):
            result = self.run_shell(f'bash "{dectl}" {action}')
            self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("DE_RESOLVED_TIMEZONE=Europe/Berlin", (run_base / ".env").read_text())
        self.assertEqual(log.read_text().count("up -d"), 2)

    def test_invalid_restart_does_not_stop_existing_service(self):
        dectl, _, log = self.prepare_dectl("Invalid/Timezone")
        result = self.run_shell(f'bash "{dectl}" restart')
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn("down", log.read_text())
        self.assertNotIn("up -d", log.read_text())


if __name__ == "__main__":
    unittest.main()
