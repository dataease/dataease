"""Run synthetic XLSX regression using existing backend classes and packaged dependencies.

Usage: python3 tests/report-watermark-columns/run.py
No application startup, database, dependency download or external delivery.
"""

from pathlib import Path
import subprocess
import tempfile
import zipfile


ROOT = Path(__file__).resolve().parents[2]
BACKEND = ROOT / "core/core-backend"


def main():
    archive = BACKEND / "target/CoreApplication.jar"
    classes = BACKEND / "target/classes"
    if not archive.is_file() or not classes.is_dir():
        raise SystemExit("Existing backend classes and CoreApplication.jar are required")
    with tempfile.TemporaryDirectory(prefix="report-watermark-regression-") as temp:
        work = Path(temp)
        libraries = work / "lib"
        libraries.mkdir()
        with zipfile.ZipFile(archive) as package:
            for name in package.namelist():
                if name.startswith("BOOT-INF/lib/") and name.endswith(".jar"):
                    (libraries / Path(name).name).write_bytes(package.read(name))
        output = work / "classes"
        classpath = f"{classes}:{libraries}/*"
        sources = [
            BACKEND / "src/main/java/io/dataease/visualization/manage/CoreVisualizationExportManage.java",
            BACKEND / "src/main/java/io/dataease/visualization/utils/VisualizationExcelUtils.java",
            BACKEND / "src/main/java/io/dataease/commons/utils/ExcelWatermarkUtils.java",
            BACKEND / "src/main/java/io/dataease/exportCenter/manage/ExportCenterDownLoadManage.java",
            Path(__file__).with_name("ReportExcelWatermarkTest.java"),
            ROOT / "tests/report-hidden-columns/CoreVisualizationExportManageTest.java",
        ]
        subprocess.run(
            ["javac", "-proc:none", "-cp", classpath, "-d", str(output), *map(str, sources)],
            check=True,
        )
        subprocess.run(
            ["java", "-Djava.awt.headless=true", "-cp", f"{output}:{classpath}",
             "ReportExcelWatermarkTest"],
            check=True,
        )
        subprocess.run(
            ["java", "-Djava.awt.headless=true", "-cp", f"{output}:{classpath}",
             "io.dataease.visualization.manage.CoreVisualizationExportManageTest"],
            check=True,
        )



if __name__ == "__main__":
    main()
