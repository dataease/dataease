"""Compile current sources and run against isolated H2; no product startup or database access."""
from pathlib import Path
import os
import subprocess
import tempfile
import zipfile

root = Path(__file__).resolve().parents[2]
with tempfile.TemporaryDirectory(prefix="dataset-semantics-") as temp:
    temp = Path(temp)
    with zipfile.ZipFile(root / "core/core-backend/target/CoreApplication.jar") as archive:
        for name in archive.namelist():
            if name.startswith(("BOOT-INF/classes/", "BOOT-INF/lib/")):
                archive.extract(name, temp)
    repository = Path.home() / ".m2/repository"
    libraries = list((temp / "BOOT-INF/lib").glob("*.jar"))
    libraries += list(repository.glob("org/projectlombok/lombok/*/*.jar"))
    libraries += [p for p in repository.glob("com/h2database/h2/*/*.jar") if not p.name.endswith("-sources.jar")]
    classpath = os.pathsep.join(map(str, [temp / "classes", temp / "BOOT-INF/classes", *libraries]))
    names = {"CoreDatasetTable.java", "CoreDatasetTableField.java", "DatasetTableDTO.java", "DatasetTableFieldDTO.java", "CoreDatasetTableFieldVO.java", "DatasetTableManage.java", "DatasetTableFieldManage.java", "DatasetSemanticValidation.java", "DatasetSemanticsInit.java", "DatasourceSemanticSnapshot.java", "DatasourceServer.java", "DatasetDataManage.java", "CoreDatasourceTableSemantic.java", "CoreDatasourceFieldSemantic.java", "CoreDatasourceTableSemanticRepository.java", "CoreDatasourceFieldSemanticRepository.java"}
    sources = [p for directory in ("sdk/common", "sdk/extensions/extensions-datasource", "sdk/api/api-base", "core/core-backend") for p in (root / directory / "src/main/java").rglob("*.java") if p.name in names]
    subprocess.run(["javac", "-parameters", "-encoding", "UTF-8", "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor", "-cp", classpath, "-d", str(temp / "classes"), *map(str, sources), str(root / "tests/dataset-semantics/Regression.java")], check=True)
    subprocess.run(["java", "-cp", classpath, "io.dataease.dataset.manage.Regression"], check=True)
