#!/bin/bash

CURRENT_DIR=$(
   cd "$(dirname "$0")"
   pwd
)

function log() {
   message="[DATAEASE Log]: $1 "
   echo -e "${message}" 2>&1 | tee -a ${CURRENT_DIR}/install.log
}

docker_config_folder="/etc/docker"
compose_files="-f docker-compose.yml"

INSTALL_TYPE='install'
if [ -f /usr/bin/dectl ]; then
   # 获取已安装的 DataEase 的运行目录
   DE_BASE=$(grep "^DE_BASE=" /usr/bin/dectl | cut -d'=' -f2)
   dectl stop
   INSTALL_TYPE='upgrade'

   v2_version=$(dectl version | head -n 2 | grep "v2.")
   if [[ -z $v2_version ]];then
      echo "系统当前版本不是 DataEase v2 版本系列，不支持升级到 v2，请检查离线包版本。"
      exit 1;
   fi
fi

set -a
if [[ $DE_BASE ]] && [[ -f $DE_BASE/dataease2.0/.env ]]; then
   source $DE_BASE/dataease2.0/.env
   INSTALL_TYPE='upgrade'
else
   source ${CURRENT_DIR}/install.conf
   INSTALL_TYPE='install'
fi
set +a

read available_disk <<< $(df -H --output=avail ${DE_BASE} | tail -1)
available_disk=${available_disk%?}
available_disk=${available_disk%.*}
if [[ $available_disk -lt 20 ]];then
   log "\033[31m[警告] DataEase 运行目录所在磁盘剩余空间不足 20G 可能无法正常启动!\033[0m"
fi

DE_RUN_BASE=$DE_BASE/dataease2.0
conf_folder=${DE_RUN_BASE}/conf
templates_folder=${DE_RUN_BASE}/templates

if [[ $DE_RUN_BASE ]];then
   for image in $(grep  "image: " $DE_RUN_BASE/docker*.yml | awk -F 'image:' '{print $2}'); do
      image_path=$(eval echo $image)
      image_name=$(echo $image_path | awk -F "[/]" '{print $3}')
      current_images[${#current_images[@]}]=$image_name
   done
fi

function prop {
   [ -f "$1" ] | grep -P "^\s*[^#]?${2}=.*$" $1 | cut -d'=' -f2
}

echo -e "======================= 开始安装 =======================" 2>&1 | tee -a ${CURRENT_DIR}/install.log

mkdir -p ${DE_RUN_BASE}
cp -r ./dataease/* ${DE_RUN_BASE}/

cd $DE_RUN_BASE
env | grep DE_ >.env

mkdir -p ${DE_RUN_BASE}/{cache,logs,conf}
mkdir -p ${DE_RUN_BASE}/data/{mysql,static-resource,map,etcd_data,geo}
mkdir -p ${DE_RUN_BASE}/apisix/logs
mkdir -p ${DE_RUN_BASE}/task/logs
chmod 777 ${DE_RUN_BASE}/apisix/logs ${DE_RUN_BASE}/data/etcd_data ${DE_RUN_BASE}/task/logs

if [ "${DE_EXTERNAL_MYSQL}" = "false" ]; then
   compose_files="${compose_files} -f docker-compose-mysql.yml"
   sed -i -e "s/^      DE_MYSQL_HOST/      ${DE_MYSQL_HOST}/g" docker-compose.yml
   sed -i -e "s/^. DE_MYSQL_HOST/  ${DE_MYSQL_HOST}/g" docker-compose-mysql.yml
else
   sed -i -e "/^    depends_on/,+2d" docker-compose.yml
fi

log "拷贝配置文件模板文件  -> $conf_folder"
cd $DE_RUN_BASE
cp -r $templates_folder/* $conf_folder

log "根据安装配置参数调整配置文件"
cd ${templates_folder}
templates_files=( application.yml mysql.env )
for i in ${templates_files[@]}; do
   if [ -f $i ]; then
      envsubst < $i > $conf_folder/$i
   fi
done

cd ${CURRENT_DIR}
sed -i -e "s#DE_BASE=.*#DE_BASE=${DE_BASE}#g" dectl
\cp dectl /usr/local/bin && chmod +x /usr/local/bin/dectl
if [ ! -f /usr/bin/dectl ]; then
   ln -s /usr/local/bin/dectl /usr/bin/dectl 2>/dev/null
fi

echo "time: $(date)"

if which getenforce && [ $(getenforce) == "Enforcing" ];then
   log  "... 关闭 SELINUX"
   setenforce 0
   sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
fi

#Install docker & docker-compose
##Install Latest Stable Docker Release
if which docker >/dev/null 2>&1; then
   log "检测到 Docker 已安装，跳过安装步骤"
   log "启动 Docker "
   service docker start 2>&1 | tee -a ${CURRENT_DIR}/install.log
else
   if [[ -d docker ]]; then
      log "... 离线安装 docker"
      cp docker/bin/* /usr/bin/
      cp docker/service/docker.service /etc/systemd/system/
      chmod +x /usr/bin/docker*
      chmod 754 /etc/systemd/system/docker.service
      log "... 启动 docker"
      systemctl enable docker; systemctl daemon-reload; service docker start 2>&1 | tee -a ${CURRENT_DIR}/install.log
   else
      log "... 在线安装 docker"
      curl -fsSL https://resource.fit2cloud.com/get-docker-linux.sh -o get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
      if [[ ! -f get-docker.sh ]];then
         log "docker 在线安装脚本下载失败，请稍候重试"
         exit 1
      fi
      sudo sh get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
      log "... 启动 docker"
      systemctl enable docker; systemctl daemon-reload; service docker start 2>&1 | tee -a ${CURRENT_DIR}/install.log
   fi

   if [ ! -d "$docker_config_folder" ];then
      mkdir -p "$docker_config_folder"
   fi

   docker version >/dev/null 2>&1
   if [ $? -ne 0 ]; then
      log "docker 安装失败"
      exit 1
   else
      log "docker 安装成功"
   fi
fi

##Install Latest Stable Docker Compose Release
docker-compose version >/dev/null 2>&1
if [ $? -ne 0 ]; then
   docker compose version >/dev/null 2>&1
   if [ $? -eq 0 ]; then
      echo 'docker compose "$@"' > /usr/bin/docker-compose
      chmod +x /usr/bin/docker-compose
   else
      if [[ -d docker ]]; then
         log "... 离线安装 docker-compose"
         cp docker/bin/docker-compose /usr/bin/
         chmod +x /usr/bin/docker-compose
      else
         log "... 在线安装 docker-compose"
         curl -L https://resource.fit2cloud.com/docker/compose/releases/download/v2.16.0/docker-compose-$(uname -s | tr A-Z a-z)-$(uname -m) -o /usr/local/bin/docker-compose 2>&1 | tee -a ${CURRENT_DIR}/install.log
         if [[ ! -f /usr/local/bin/docker-compose ]];then
            log "docker-compose 下载失败，请稍候重试"
            exit 1
         fi
         chmod +x /usr/local/bin/docker-compose
         ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
      fi
   fi

   docker-compose version >/dev/null
   if [ $? -ne 0 ]; then
      log "docker-compose 安装失败"
      exit 1
   else
      log "docker-compose 安装成功"
   fi
else
   log "检测到 Docker Compose 已安装，跳过安装步骤"
fi

export COMPOSE_HTTP_TIMEOUT=180
cd ${CURRENT_DIR}
# 加载镜像
if [[ -d images ]]; then
   log "加载镜像"
   for i in $(ls images); do
      if [[ "${current_images[@]}"  =~ "${i%.tar.gz}" ]]; then
         echo "ignore image $i"
      else
         docker load -i images/$i 2>&1 | tee -a ${CURRENT_DIR}/install.log
      fi
   done
else
   DEVERSION=$(cat ${CURRENT_DIR}/dataease/templates/version)
   curl -sfL https://resource.fit2cloud.com/installation-log.sh | sh -s de ${INSTALL_TYPE} ${DEVERSION}
   cd -
fi

log "配置 dataease Service"
cp ${DE_RUN_BASE}/bin/dataease/dataease.service /etc/init.d/dataease
chmod a+x /etc/init.d/dataease
if which chkconfig;then
   chkconfig --add dataease
fi

if [ -f /etc/rc.d/rc.local ];then
   dataeaseService=$(grep "service dataease start" /etc/rc.d/rc.local | wc -l)
   if [ "$dataeaseService" -eq 0 ]; then
      echo "sleep 10" >> /etc/rc.d/rc.local
      echo "service dataease start" >> /etc/rc.d/rc.local
   fi
   chmod +x /etc/rc.d/rc.local
fi

if [[ $(grep "vm.max_map_count" /etc/sysctl.conf | wc -l) -eq 0 ]];then
   sysctl -w vm.max_map_count=2000000
   echo "vm.max_map_count=2000000" >> /etc/sysctl.conf
elif (( $(grep "vm.max_map_count" /etc/sysctl.conf | awk -F'=' '{print $2}') < 2000000 ));then
   sysctl -w vm.max_map_count=2000000
   sed -i 's/^vm\.max_map_count.*/vm\.max_map_count=2000000/' /etc/sysctl.conf
fi

if [ $(grep "net.ipv4.ip_forward" /etc/sysctl.conf | wc -l) -eq 0 ];then
   sysctl -w net.ipv4.ip_forward=1
   echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
else
   sed -i 's/^net\.ipv4\.ip_forward.*/net\.ipv4\.ip_forward=1/' /etc/sysctl.conf
fi

if which firewall-cmd >/dev/null; then
   if systemctl is-active firewalld &>/dev/null ;then
      log "防火墙端口开放"
      firewall-cmd --zone=public --add-port=${DE_PORT}/tcp --permanent
      firewall-cmd --reload
   else
      log "防火墙未开启，忽略端口开放"
   fi
fi

http_code=$(curl -sILw "%{http_code}\n" http://localhost:${DE_PORT} -o /dev/null)
if [[ $http_code == 200 ]];then
   log "停止服务进行升级..."
   dectl stop
fi

log "启动服务"
dectl start | tee -a ${CURRENT_DIR}/install.log
dectl status 2>&1 | tee -a ${CURRENT_DIR}/install.log

access_port=$DE_PORT
if [[ $DE_INSTALL_MODE != "community" ]];then
   access_port=9080
fi
echo -e "======================= 安装完成 =======================\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log
echo -e "系统登录信息如下:\n 访问地址: http://服务器IP:$access_port\n 用户名: admin\n 初始密码: DataEase@123456" 2>&1 | tee -a ${CURRENT_DIR}/install.log