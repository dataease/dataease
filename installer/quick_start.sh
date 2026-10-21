#!/bin/bash

if [[ -x "$(command -v python)" ]]; then
  py_cmd='python'
elif [[ -x "$(command -v python3)" ]]; then
  py_cmd='python3'
else
  echo "未找到 Python，请先安装 Python 后重试。" >&2
  exit 1
fi

server_url="github.com"

echo -ne "检测 ${server_url} ... "
curl --fail -m 5 -Is "https://${server_url}" >/dev/null

if [ $? != 0 ];then
	echo "failed"
	echo "没有找到稳定的下载服务器，请稍候重试"
	exit 1
else
	echo "ok"
fi

if ! latest_version=$("$py_cmd" - <<'EOF'
# -*- coding: UTF-8 -*-
import json
import re
import subprocess
import sys

# 只接受完整的 ASCII v3 版本号，不能把远端元数据拼入 Shell。
release_pattern = r"\Av3\.[0-9]+\.[0-9]+\Z"
try:
    for page in range(1, 4):
        response = subprocess.check_output([
            "curl", "--fail", "--silent", "--show-error", "--connect-timeout", "10",
            "--max-time", "30",
            "https://api.github.com/repos/dataease/dataease/releases?page=%d" % page
        ])
        for release in json.loads(response):
            if release.get("prerelease") or release.get("draft"):
                continue
            version = release.get("tag_name", "")
            if isinstance(version, (type(""), type(u""))) and re.match(release_pattern, version):
                sys.stdout.write(version)
                sys.exit(0)
except Exception:
    sys.stderr.write("Failed to obtain Release information, please check the network.\n")
    sys.exit(1)
sys.stderr.write("Failed to obtain latest version, please try again.\n")
sys.exit(1)
EOF
); then
  echo "获取最新版本失败，请检查网络连接是否正常" >&2
  exit 1
fi

echo "开始下载 DataEase ${latest_version} 版本在线安装包"

installer_file="dataease-online-installer-${latest_version}-ce.tar.gz"
download_url="https://${server_url}/dataease/dataease/releases/download/${latest_version}/$installer_file"
echo "下载地址： ${download_url}"
curl --fail -L -m 60 -o "$installer_file" "$download_url" || exit 1

if [ ! -f "$installer_file" ];then
	echo "下载在线安装包失败，请试试重新执行一次安装命令。"
	exit 1
fi

tar zxvf "$installer_file"
if [ $? != 0 ];then
	echo "下载在线安装包失败，请试试重新执行一次安装命令。"
	rm -f -- "$installer_file"
	exit 1
fi

cd "${installer_file%.tar.gz}" || exit 1
/bin/bash install.sh
