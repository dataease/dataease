"""Run isolated Java language regressions using existing local build dependencies.

Usage: python3 tests/backend/run-language-switch.py [--xpack]
Requires an existing core/core-backend/target/CoreApplication.jar and local Maven jars.
Compiles only the changed sources into a temporary directory. No Maven lifecycle,
application startup, database, Redis connection or dependency installation is involved.
The XPack regression uses its own recording manage and remains in the private submodule.
"""

import argparse
import os
from pathlib import Path
import subprocess
import tempfile
from zipfile import ZipFile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--xpack", action="store_true")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    repository = Path.home() / ".m2/repository"
    application = root / "core/core-backend/target/CoreApplication.jar"
    if not application.is_file():
        parser.error("Existing CoreApplication.jar is required; this script does not build it")
    by_name = {file.name: file for file in repository.rglob("*.jar")}
    with ZipFile(application) as archive:
        names = [Path(name).name for name in archive.namelist()
                 if name.startswith("BOOT-INF/lib/") and name.endswith(".jar")
                 and not Path(name).name.startswith("spring-boot-jarmode-")]
    missing = [name for name in names if name not in by_name]
    if missing:
        parser.error("Missing local Maven dependencies: " + ", ".join(missing))
    libraries = [by_name[name] for name in names]
    for artifact in ["org/projectlombok/lombok/*/lombok-*.jar",
                     "org/springframework/spring-test/*/spring-test-*.jar"]:
        matches = sorted(repository.glob(artifact))
        if not matches:
            parser.error("Missing test dependency: " + artifact)
        libraries.append(matches[-1])
    classes = [root / "core/core-backend/target/classes"]
    for directory in ["sdk", "de-xpack"]:
        classes.extend((root / directory).glob("**/target/classes"))
    sources = [
        "sdk/common/src/main/java/io/dataease/i18n/LanguageUtils.java",
        "sdk/api/api-permissions/src/main/java/io/dataease/api/permissions/user/dto/LangSwitchRequest.java",
        "sdk/api/api-permissions/src/main/java/io/dataease/api/permissions/user/api/UserApi.java",
        "core/core-backend/src/main/java/io/dataease/substitute/permissions/user/SubstituteUserServer.java",
        "core/core-backend/src/main/java/io/dataease/system/server/SysParameterServer.java",
        "tests/backend/LanguageSwitchRegression.java",
    ]
    tests = ["io.dataease.i18n.LanguageSwitchRegression"]
    if args.xpack:
        sources.extend([
            "de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions/user/manage/LanguageSwitchValidator.java",
            "de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions/user/server/UserServer.java",
            "de-xpack/xpack-permissions/src/test/java/io/dataease/xpack/permissions/user/UserLanguageValidationRegression.java",
        ])
        tests.append("io.dataease.xpack.permissions.user.UserLanguageValidationRegression")
    with tempfile.TemporaryDirectory(prefix="de-language-regression-") as output:
        classpath = os.pathsep.join(str(path) for path in [output, *classes, *libraries])
        compile_result = subprocess.run([
            "javac", "-encoding", "UTF-8", "-processor",
            "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
            "-cp", classpath, "-d", output, *[str(root / source) for source in sources],
        ], check=False)
        if compile_result.returncode:
            return compile_result.returncode
        for test in tests:
            result = subprocess.run(["java", "-cp", classpath, test], check=False)
            if result.returncode:
                return result.returncode
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
