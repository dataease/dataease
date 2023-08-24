#!/bin/bash

DE_BASE=/opt

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

if [ -f /usr/bin/dectl ]; then
   # 获取已安装的 DataEase 的运行目录
   DE_BASE=$(grep "^DE_BASE=" /usr/bin/dectl | cut -d'=' -f2)
fi

echo "停止 DataEase 服务"
dectl uninstall

# 清理 DataEase 相关镜像
if test ! -z "$(docker images -f dangling=true -q)"; then
   echo "清理虚悬镜像"
   docker rmi $(docker images -f dangling=true -q)
fi

if test -n "$(docker images | grep 'registry.cn-qingdao.aliyuncs.com/dataease')"; then
   echo "清理 DataEase 镜像"
   docker rmi $(docker images | grep "registry.cn-qingdao.aliyuncs.com/dataease" | awk -F' ' '{print $1":"$2}')
fi

# 清理 DataEase 运行目录及命令行工具 dectl
rm -rf ${DE_BASE}/dataease /usr/local/bin/dectl /usr/bin/dectl