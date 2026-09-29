#!/usr/bin/env python3
"""Offline source regression; requires existing module classes and Maven dependencies.
Run after compiling the sibling dataease-license-sdk project. Does not start an application.
"""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[5]
license_classes = root.parent / 'dataease-license-sdk/target/classes'
outputs = [license_classes, root / 'core/core-backend/target/classes',
           root / 'de-xpack/xpack-permissions/target/classes']
for output in outputs:
    if not output.is_dir():
        raise SystemExit(f'Missing compiled dependency directory: {output}')
classpath = ':'.join(str(path) for path in outputs + sorted((Path.home() / '.m2/repository').rglob('*.jar')))
core = root / 'core/core-backend/src/main/java/io/dataease'
xpack = root / 'de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions'
sources = [core / name for name in [
    'share/util/LinkTokenUtil.java', 'share/manage/LinkTokenValidationManage.java',
    'share/interceptor/ShareQueryScopeAop.java', 'share/manage/ShareSecretManage.java',
    'share/manage/ShareVisitorPermissionManage.java', 'share/manage/XpackShareManage.java',
    'share/interceptor/LinkInterceptor.java', 'share/interceptor/DeLinkAop.java']]
sources += [xpack / name for name in [
    'utils/PerTokenUtils.java', 'utils/TokenCacheUtils.java', 'apisix/manage/ApisixManage.java', 'apisix/manage/ApisixTokenManage.java', 'apisix/server/ApisixServer.java']]
sources += [root / 'de-xpack/xpack-permissions/src/test/java/io/dataease/xpack/permissions/apisix/LinkAuthenticationRegression.java',
            root / 'de-xpack/xpack-permissions/src/test/java/io/dataease/xpack/permissions/apisix/LoginAuthenticationRegression.java',
            root / 'sdk/common/src/main/java/io/dataease/utils/RsaUtils.java',
            root / 'sdk/common/src/main/java/io/dataease/result/ResultCode.java',
            root / 'core/core-backend/src/test/java/io/dataease/share/LinkTokenSecurityRegression.java']
with tempfile.TemporaryDirectory(prefix='de-link-regression-') as output:
    subprocess.run(['javac', '-encoding', 'UTF-8', '-processor',
                    'lombok.launch.AnnotationProcessorHider$AnnotationProcessor',
                    '-cp', classpath, '-d', output, *map(str, sources)], check=True)
    subprocess.run(['java', '-cp', output + ':' + classpath,
                    'io.dataease.share.LinkTokenSecurityRegression'], check=True)
    subprocess.run(['java', '-cp', output + ':' + classpath,
                    'io.dataease.xpack.permissions.apisix.LinkAuthenticationRegression'], check=True)
    subprocess.run(['java', '-cp', output + ':' + classpath,
                    'io.dataease.xpack.permissions.apisix.LoginAuthenticationRegression'], check=True)
