"""Offline control-script regression; no real containers or services are used."""

import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


INSTALLER = Path(__file__).resolve().parents[1]


class DeploymentStartTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.bin = self.root / "bin"
        self.bin.mkdir()
        self.run_base = self.root / "dataease2.0"
        (self.run_base / "bin").mkdir(parents=True)
        shutil.copyfile(INSTALLER / "dataease/bin/sync-auth.sh", self.run_base / "bin/sync-auth.sh")
        shutil.copyfile(INSTALLER / "dataease/docker-compose.yml", self.run_base / "docker-compose.yml")
        (self.run_base / ".env").write_text("DE_INSTALL_MODE=community\nDE_EXTERNAL_MYSQL=true\n")
        self.dectl = self.root / "dectl"
        self.dectl.write_text((INSTALLER / "dectl").read_text().replace("DE_BASE=/opt", f'DE_BASE="{self.root}"'))
        self.log = self.root / "commands"
        self.stub("docker-compose", f'echo "$*" >> "{self.log}"; '
                  'case "$*" in *"up -d"*) exit "${TEST_COMPOSE_STATUS:-0}";; esac')
        self.stub("docker", f'echo "docker $*" >> "{self.log}"; '
                  'case "$1" in ps) echo fake-container;; inspect) '
                  'echo "running ${TEST_HEALTH:-healthy}";; esac')
        self.stub("sleep", ":")

    def stub(self, name, body):
        path = self.bin / name
        path.write_text("#!/bin/bash\n" + body + "\n")
        path.chmod(0o755)

    def run_dectl(self, action, **options):
        env = {key: value for key, value in os.environ.items() if not key.startswith(("DE_", "TEST_", "BASH_ENV"))}
        env.update(options)
        env["PATH"] = str(self.bin) + os.pathsep + env["PATH"]
        return subprocess.run(["bash", str(self.dectl), action], env=env,
                              capture_output=True, text=True, timeout=10)

    def test_failed_update_does_not_accept_healthy_old_container(self):
        for action in ("start", "reload"):
            result = self.run_dectl(action, TEST_COMPOSE_STATUS="42")
            self.assertEqual(result.returncode, 42, result.stderr)
            self.assertIn("容器更新失败", result.stderr)
        self.assertNotIn("docker inspect", self.log.read_text())
        self.assertNotIn("docker ps", self.log.read_text())

    def test_successful_update_and_healthy_container(self):
        for action in ("start", "reload"):
            result = self.run_dectl(action)
            self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("docker inspect", self.log.read_text())

    def test_unhealthy_or_timed_out_container_fails(self):
        for health in ("unhealthy", "starting"):
            result = self.run_dectl("reload", TEST_HEALTH=health)
            self.assertNotEqual(result.returncode, 0)

    def test_inaccessible_directory_does_not_update(self):
        (self.root / "shell-env").write_text("cd() { return 1; }\n")
        result = self.run_dectl("start", BASH_ENV=str(self.root / "shell-env"))
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn("up -d", self.log.read_text())

    def test_failed_stop_does_not_continue_restart(self):
        self.stub("docker-compose", f'echo "$*" >> "{self.log}"; '
                  'case "$*" in *down*) exit 42;; esac')
        result = self.run_dectl("restart")
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn("up -d", self.log.read_text())


if __name__ == "__main__":
    unittest.main()
