git_urls=('gitee.com' 'github.com')

if [[ -x "$(command -v python)" ]];then
  py_cmd='python'
elif [[ -x "$(command -v python3)" ]]; then
  py_cmd='python3'
else
  git_urls=('github.com')
fi

for git_url in ${git_urls[*]}
do
	success="true"
	for i in {1..3}
	do
		echo -ne "检测 ${git_url} ... ${i} "
	    curl -m 5 -kIs https://${git_url} >/dev/null
		if [ $? != 0 ];then
			echo "failed"
			success="false"
			break
		else
			echo "ok"
		fi
	done
	if [[ ${success} == "true" ]];then
		server_url=${git_url}
		break
	fi
done

if [[ "x${server_url}" == "x" ]];then
    echo "没有找到稳定的下载服务器，请稍候重试"
    exit 1
fi

echo "使用下载服务器 ${server_url}"

if [[ "${server_url}" == "gitee.com" ]];then
    owner='fit2cloud-feizhiyun'
    repo='DataEase'
    gitee_release_content=$(curl -s https://gitee.com/api/v5/repos/${owner}/${repo}/releases/latest)
    # export LC_ALL="en_US.utf8"
    DEVERSION=$($py_cmd -c "import json; obj=json.loads('$gitee_release_content', strict=False); print(obj['tag_name']);")
else
	owner='dataease'
	repo='dataease'
	DEVERSION=$(curl -s https://api.github.com/repos/${owner}/${repo}/releases/latest | grep -e "\"tag_name\"" | sed -r 's/.*: "(.*)",/\1/')	
fi

if [[ "x${DEVERSION}" == "x" ]];then
    echo "获取最新版本失败，请稍候重试"
    exit 1
fi

echo "开始下载 DataEase ${DEVERSION} 版本在线安装包"
dataease_online_file_name="dataease-${DEVERSION}-online.tar.gz"
download_url="https://${server_url}/${owner}/${repo}/releases/download/${DEVERSION}/${dataease_online_file_name}"
echo "下载地址： ${download_url}"

curl -LOk -m 60 -o ${dataease_online_file_name} ${download_url}

if [ ! -f ${dataease_online_file_name} ];then
	echo "下载在线安装包失败，请试试重新执行一次安装命令。"
	exit 1
fi

tar zxvf ${dataease_online_file_name}
if [ $? != 0 ];then
	echo "下载在线安装包失败，请试试重新执行一次安装命令。"
	rm -f ${dataease_online_file_name}
	exit 1
fi
cd dataease-${DEVERSION}-online

/bin/bash install.sh