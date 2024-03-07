
if [[ -x "$(command -v python)" ]];then
  py_cmd='python'
elif [[ -x "$(command -v python3)" ]]; then
  py_cmd='python3'
fi

server_url="github.com"

echo -ne "检测 ${server_url} ... "
curl -m 5 -kIs https://${server_url} >/dev/null

if [ $? != 0 ];then
	echo "failed"
	echo "没有找到稳定的下载服务器，请稍候重试"
	exit 1
else
	echo "ok"
fi

rm -f /tmp/de_latest_release

$py_cmd - <<EOF
# -*- coding: UTF-8 -*-
import os
import json
import re

latest_release=""
release_pattern="v2\.\d+\.\d+$"

def get_releases(page):
   try:
      releases=os.popen("curl -s https://api.github.com/repos/dataease/dataease/releases?page=%d" % (page)).read()
      releases=[ x["name"] for x in json.loads(releases) if x["prerelease"] == False ]
   except Exception as e:
      print(str(e))
      print("Failed to obtain Release information, please check the network.")
      exit(1)
   else:
      for release in releases:
         if re.search(release_pattern,release) != None:
            return release

page = 1
while (page <= 3):
   latest_release = get_releases(page)
   if (latest_release != "" and latest_release != None):
      break
   page += 1

if latest_release == None or latest_release == "":
   print("Failed to obtain latest version, please try again.")
   exit(1)

# 记录最新版本号
os.popen("echo "+latest_release+" > /tmp/de_latest_release")
EOF

if [ ! -f /tmp/de_latest_release ]; then
  echo "获取最新版本失败，请检查网络连接是否正常"
  exit 1
fi
latest_version=$(cat /tmp/de_latest_release)

echo "开始下载 DataEase ${latest_version} 版本在线安装包"

installer_file="dataease-online-installer-${latest_version}-ce.tar.gz"
download_url="https://${server_url}/dataease/dataease/releases/download/${latest_version}/$installer_file"
echo "下载地址： ${download_url}"
curl -LOk -m 60 -o $installer_file $download_url

if [ ! -f ${installer_file} ];then
	echo "下载在线安装包失败，请试试重新执行一次安装命令。"
	exit 1
fi

tar zxvf ${installer_file}
if [ $? != 0 ];then
	echo "下载在线安装包失败，请试试重新执行一次安装命令。"
	rm -f ${installer_file}
	exit 1
fi

cd ${installer_file%.tar.gz}
/bin/bash install.sh