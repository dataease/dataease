#!/bin/bash

DE_BASE=/opt

echo "如需备份 DataEase 数据，请执行 dectl backup 进行备份。如您的 dectl 命令不支持 backup 命令，请升级版本。"
read -r -p "即将卸载 DataEase 服务，包括删除运行目录、数据及相关镜像，是否继续? [Y/n] " input

case $input in
   [yY][eE][sS]|[yY])
      echo "Yes"
      ;;
   [nN][oO]|[nN])
      echo "No"
      exit 1
      ;;
   *)
      echo "无效输入..."
      exit 1
      ;;
esac

echo "停止 DataEase 服务"
service dataease stop >/dev/null 2>&1

echo "移除 DataEase 服务"
if which chkconfig >/dev/null 2>&1;then
   chkconfig dataease >/dev/null
   if [ $? -eq 0 ]; then
      chkconfig --del dataease >/dev/null 2>&1
   fi
fi

if [ -f /etc/systemd/system/dataease.service ];then
   systemctl disable dataease >/dev/null 2>&1
   rm -f /etc/systemd/system/dataease.service
   systemctl daemon-reload
elif [[ -f /etc/init.d/dataease ]];then
   rm -f /etc/init.d/dataease
fi

if [ -f /usr/bin/dectl ]; then
   # 获取已安装的 DataEase 的运行目录
   DE_BASE=$(grep "^DE_BASE=" /usr/bin/dectl | cut -d'=' -f2)
fi

cleanup_images() {
   local image_group=$1
   local repository_prefix=$2
   local image_list image_ref
   local -a image_refs=()
   shift 2

   if ! image_list=$(docker image ls "$@"); then
      echo "获取 ${image_group}列表失败，请检查 Docker 错误。" >&2
      return 1
   fi

   while IFS= read -r image_ref; do
      if [[ -n "$image_ref" && "$image_ref" == "$repository_prefix"* && "$image_ref" != *':<none>' ]]; then
         image_refs+=("$image_ref")
      fi
   done <<< "$image_list"
   if (( ${#image_refs[@]} == 0 )); then
      return 0
   fi

   echo "清理 ${image_group}"
   if ! docker rmi -- "${image_refs[@]}"; then
      echo "清理 ${image_group}失败，请检查 Docker 错误。" >&2
      return 1
   fi
}

image_cleanup_failed=0
cleanup_images "虚悬镜像" "" --quiet --filter dangling=true || image_cleanup_failed=1
cleanup_images "DataEase 镜像" "registry.cn-qingdao.aliyuncs.com/dataease/" \
   --format '{{.Repository}}:{{.Tag}}' || image_cleanup_failed=1

# 清理 DataEase 运行目录及命令行工具 dectl
rm -rf ${DE_BASE}/dataease2.0 /usr/bin/dectl

if (( image_cleanup_failed )); then
   echo "DataEase 卸载流程已结束，镜像清理失败。" >&2
   exit 1
fi

echo "DataEase 服务卸载完成"
