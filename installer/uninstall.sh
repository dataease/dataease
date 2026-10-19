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

# 先确认唯一且真实的实例目录，再停止服务或删除任何文件；不执行 dectl 中的配置。
dectl_path=/usr/bin/dectl
[[ -f "$dectl_path" ]] || dectl_path=/usr/local/bin/dectl
if [[ ! -f "$dectl_path" ]]; then
   echo "未找到 dectl，无法确认安装目录，请核实实例后手工卸载。" >&2
   exit 1
fi
base_assignment=$(grep '^DE_BASE=' "$dectl_path") || exit 1
if [[ "$base_assignment" == *$'\n'* ]]; then
   echo "dectl 中的 DE_BASE 配置不唯一，卸载停止。" >&2
   exit 1
fi
DE_BASE=${base_assignment#DE_BASE=}
# 兼容普通路径及完整引号包裹的路径，不做命令替换或变量展开。
if [[ "$DE_BASE" == \"*\" || "$DE_BASE" == \'*\' ]]; then
   DE_BASE=${DE_BASE:1:${#DE_BASE}-2}
fi
# 允许历史配置的重复/末尾斜杠，但拒绝 .、.. 路径分量。
while [[ "$DE_BASE" == *//* ]]; do DE_BASE=${DE_BASE//\/\//\/}; done
DE_BASE=${DE_BASE%/}
if [[ "$DE_BASE" != /* || "$DE_BASE" == *$'\r'* || "$DE_BASE" == *$'\n'* ||
      "/${DE_BASE#/}/" == */../* || "/${DE_BASE#/}/" == */./* || ! -d "$DE_BASE" ]]; then
   echo "DE_BASE 不是有效的绝对安装目录，卸载停止。" >&2
   exit 1
fi
resolved_base=$(cd -- "$DE_BASE" && pwd -P) || exit 1
runtime_path="$resolved_base/dataease3.0"
if [[ "$resolved_base" != "$DE_BASE" || -L "$runtime_path" || ! -d "$runtime_path" ||
      ! -f "$runtime_path/.env" || -L "$runtime_path/.env" ||
      ! -f "$runtime_path/docker-compose.yml" || -L "$runtime_path/docker-compose.yml" ]]; then
   echo "安装目录包含符号链接或缺少实例标记，卸载停止；请核实实例后手工处理。" >&2
   exit 1
fi

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
rm -rf -- "$runtime_path" /usr/bin/dectl || exit 1

echo "DataEase 服务卸载完成"
