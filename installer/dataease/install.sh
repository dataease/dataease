#!/bin/bash

CURRENT_DIR=$(
   cd "$(dirname "$0")"
   pwd
)

echo "$(date)" | tee -a ${CURRENT_DIR}/install.log

function log() {
   message="[DATAEASE Log]: $1 "
   echo -e "${message}" 2>&1 | tee -a ${CURRENT_DIR}/install.log
}

args=$@
os=$(uname -a)
docker_config_folder="/etc/docker"
compose_files="-f docker-compose.yml"

INSTALL_TYPE='install'
if [ -f /usr/bin/dectl ]; then
   # 获取已安装的 DataEase 的运行目录
   DE_BASE=$(grep "^DE_BASE=" /usr/bin/dectl | cut -d'=' -f2)
   echo "停止 DataEase 服务"
   service dataease stop
   INSTALL_TYPE='upgrade'
fi

set -a
if [[ -d $DE_BASE ]] && [[ -f $DE_BASE/dataease/.env ]]; then
   source $DE_BASE/dataease/.env
   INSTALL_TYPE='upgrade'
else
   source ${CURRENT_DIR}/install.conf
   INSTALL_TYPE='install'
fi
set +a

DE_RUN_BASE=$DE_BASE/dataease
conf_folder=${DE_RUN_BASE}/conf
templates_folder=${DE_RUN_BASE}/templates
mysql_container_name="mysql"
if [ -f ${DE_RUN_BASE}/docker-compose-mysql.yml ]; then
   mysql_container_name=$(grep "container_name" ${DE_RUN_BASE}/docker-compose-mysql.yml | awk -F': ' '{print $2}')
fi

dataease_conf=${conf_folder}/dataease.properties

function prop {
   [ -f "$1" ] | grep -P "^\s*[^#]?${2}=.*$" $1 | cut -d'=' -f2
}

if [ "x${DE_ENGINE_MODE}" = "x" ]; then
   export DE_ENGINE_MODE="local"
fi

if [ "x${DE_DOCKER_SUBNET}" = "x" ]; then
   export DE_DOCKER_SUBNET=$(grep "^[[:blank:]]*- subnet" ${DE_RUN_BASE}/docker-compose.yml | awk -F': ' '{print $2}')
fi

if [ "x${DE_DOCKER_GATEWAY}" = "x" ]; then
   export DE_DOCKER_GATEWAY=$(grep "^[[:blank:]]*gateway" ${DE_RUN_BASE}/docker-compose.yml | awk -F': ' '{print $2}')
fi

if [ "x${DE_DORIS_FE_IP}" = "x" ]; then
   DE_DORIS_FE_IP=$(grep "^[[:blank:]]*ipv4_address" ${DE_RUN_BASE}/docker-compose-doris.yml | awk -F': ' '{print $2}' | head -n 1)
   export DE_DORIS_FE_IP
fi

if [ "x${DE_DORIS_BE_IP}" = "x" ]; then
   DE_DORIS_BE_IP=$(grep "^[[:blank:]]*ipv4_address" ${DE_RUN_BASE}/docker-compose-doris.yml | awk -F': ' '{print $2}' | tail -n 1)
   export DE_DORIS_BE_IP
fi

echo -e "*******************************************************\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log
echo -e " 当前部署模式为 ${DE_ENGINE_MODE}，如需切换模式，\n 请修改 $DE_BASE/dataease/.env 中的 DE_ENGINE_MODE 变量后，\n 重新执行 bash install.sh 即可\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log
echo -e "*******************************************************\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log

if [[ -f $dataease_conf ]]; then
   DE_LOGIN_TIMEOUT=$(prop $dataease_conf dataease.login_timeout)
   DE_INIT_PASSWORD=$(prop $dataease_conf dataease.init_password)
   DE_MYSQL_PARAMS=$(grep -P "^\s*[^#]?spring.datasource.url=.*$" $dataease_conf | cut -d'=' --complement -f1 | awk -F'?' '{print $2}')
fi
export DE_MYSQL_PARAMS
export DE_LOGIN_TIMEOUT=$([[ -z $DE_LOGIN_TIMEOUT ]] && echo -n 480 || echo -n $DE_LOGIN_TIMEOUT)
export DE_INIT_PASSWORD=$([[ -z $DE_INIT_PASSWORD ]] && echo -n DataEase123456 || echo -n $DE_INIT_PASSWORD)

if [[ -f $dataease_conf ]] && [[ ! ${DE_EXTERNAL_DORIS} ]]; then
   export DE_DORIS_DB=$(prop $dataease_conf doris.db)
   export DE_DORIS_USER=$(prop $dataease_conf doris.user)
   export DE_DORIS_PASSWORD=$(prop $dataease_conf doris.password)
   export DE_DORIS_HOST=$(prop $dataease_conf doris.host)
   export DE_DORIS_PORT=$(prop $dataease_conf doris.port)
   export DE_DORIS_HTTPPORT=$(prop $dataease_conf doris.httpPort)

   if [ ${DE_DORIS_HOST} = "doris-fe" ]; then
      export DE_EXTERNAL_DORIS="false"
   else
      export DE_EXTERNAL_DORIS="true"
   fi
fi

if [ ${DE_EXTERNAL_DORIS} = "false" ] && [ ${DE_ENGINE_MODE} = "local" ]; then
   compose_files="${compose_files} -f docker-compose-doris.yml"
fi

if [[ -f $dataease_conf ]] && [[ ! ${DE_EXTERNAL_KETTLE} ]]; then
   export DE_CARTE_HOST=$(prop $dataease_conf carte.host)
   export DE_CARTE_PORT=$(prop $dataease_conf carte.port)
   export DE_CARTE_USER=$(prop $dataease_conf carte.user)
   export DE_CARTE_PASSWORD=$(prop $dataease_conf carte.passwd)

   if [ ${DE_CARTE_HOST} = "kettle" ]; then
      export DE_EXTERNAL_KETTLE="false"
   else
      export DE_EXTERNAL_KETTLE="true"
   fi
fi

if [ ${DE_EXTERNAL_KETTLE} = "false" ] && [ ${DE_ENGINE_MODE} = "local" ]; then
   compose_files="${compose_files} -f docker-compose-kettle.yml"
fi


echo -e "======================= 开始安装 =======================" 2>&1 | tee -a ${CURRENT_DIR}/install.log

keep_doris="false"
if [[ -f ${DE_RUN_BASE}/docker-compose-doris.yml ]]; then
   current_doris_version=$(grep '^    image:' ${DE_RUN_BASE}/docker-compose-doris.yml | head -1 | cut -d ':' -f3)
   if [[ ! $current_doris_version =~ "v1.2.4" ]]; then
      echo "不升级doris，备份 docker-compose-doris.yml 文件" | tee -a ${CURRENT_DIR}/install.log
      keep_doris="true"
      \cp ${DE_RUN_BASE}/docker-compose-doris.yml ${DE_RUN_BASE}/docker-compose-doris.yml.bak
   fi
fi

keep_mysql="false"
if [[ -f ${DE_RUN_BASE}/docker-compose-mysql.yml ]]; then
   current_mysql_version=$(grep '^    image:' ${DE_RUN_BASE}/docker-compose-mysql.yml | head -1 | cut -d ':' -f3)
   if [[ ! $current_mysql_version =~ "8." ]]; then
      echo "不升级MySQL，备份 docker-compose-mysql.yml 文件" | tee -a ${CURRENT_DIR}/install.log
      keep_mysql="true"
      \cp ${DE_RUN_BASE}/docker-compose-mysql.yml ${DE_RUN_BASE}/docker-compose-mysql.yml.bak
   fi
fi

mkdir -p ${DE_RUN_BASE}


read available_disk <<< $(df -m --output=avail ${DE_RUN_BASE} | tail -1)
if [[ $available_disk -lt 20480 ]];then
   log "\033[31m[警告] DataEase 运行目录所在磁盘剩余空间不足 20G 可能无法正常启动!\033[0m"
fi

cp -r ${CURRENT_DIR}/dataease/* ${DE_RUN_BASE}/

cd $DE_RUN_BASE
env | grep DE_ >.env

mkdir -p $conf_folder
mkdir -p ${DE_RUN_BASE}/data/kettle
mkdir -p ${DE_RUN_BASE}/data/fe
mkdir -p ${DE_RUN_BASE}/data/be
mkdir -p ${DE_RUN_BASE}/data/mysql
mkdir -p ${DE_RUN_BASE}/data/exportData
mkdir -p ${DE_RUN_BASE}/data/static-resource
mkdir -p ${DE_RUN_BASE}/custom-drivers
mkdir -p ${DE_RUN_BASE}/custom-drivers
mkdir -p ${DE_RUN_BASE}/data/business


if [ ${keep_doris} = "true" ]; then
   \mv ${DE_RUN_BASE}/docker-compose-doris.yml.bak ${DE_RUN_BASE}/docker-compose-doris.yml
fi

if [ ${keep_mysql} = "true" ]; then
   \mv ${DE_RUN_BASE}/docker-compose-mysql.yml.bak ${DE_RUN_BASE}/docker-compose-mysql.yml
fi

DE_MYSQL_HOST_ORIGIN=$DE_MYSQL_HOST
DE_MYSQL_PORT_ORIGIN=$DE_MYSQL_PORT

if [ ${DE_EXTERNAL_MYSQL} = "false" ]; then
   compose_files="${compose_files} -f docker-compose-mysql.yml"
   export DE_MYSQL_HOST=$mysql_container_name
   export DE_MYSQL_PORT=3306
   sed -i "s/^    container_name: mysql/    container_name: ${DE_MYSQL_HOST}/g" docker-compose-mysql.yml
else
   sed -i -e "/^    depends_on/,+2d" docker-compose.yml
fi

log "拷贝配置文件模板文件  -> $conf_folder"
if [[ -f $dataease_conf ]]; then
   grep "redis" $dataease_conf >> $templates_folder/dataease.properties
fi
cd $DE_RUN_BASE
cp -r $templates_folder/* $conf_folder
cp -r $templates_folder/.kettle $conf_folder

log "根据安装配置参数调整配置文件"
cd ${templates_folder}
templates_files=( dataease.properties mysql.env )
for i in ${templates_files[@]}; do
   if [ -f $i ]; then
      envsubst < $i > $conf_folder/$i
   fi
done

export DE_MYSQL_HOST=$DE_MYSQL_HOST_ORIGIN
export DE_MYSQL_PORT=$DE_MYSQL_PORT_ORIGIN

cd ${CURRENT_DIR}
sed -i -e "s#DE_BASE=.*#DE_BASE=${DE_BASE}#g" dectl
\cp dectl /usr/local/bin && chmod +x /usr/local/bin/dectl
if [ ! -f /usr/bin/dectl ]; then
   ln -s /usr/local/bin/dectl /usr/bin/dectl 2>/dev/null
fi

if which getenforce >/dev/null 2>&1 && [ $(getenforce) == "Enforcing" ];then
   log  "关闭 SELINUX"
   setenforce 0
   sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
fi

#Install docker & docker-compose
##Install Latest Stable Docker Release
if which docker >/dev/null 2>&1; then
   log "检测到 Docker 已安装，跳过安装步骤"
   log "启动 Docker "
   service docker start >/dev/null 2>&1 | tee -a ${CURRENT_DIR}/install.log
else
   if [[ -d docker ]]; then
      log "离线安装 docker"
      cp docker/bin/* /usr/bin/
      cp docker/service/docker.service /etc/systemd/system/
      chmod +x /usr/bin/docker*
      chmod 644 /etc/systemd/system/docker.service
      log "启动 docker"
      systemctl enable docker >/dev/null 2>&1; systemctl daemon-reload; service docker start >/dev/null 2>&1 | tee -a ${CURRENT_DIR}/install.log
   else
      log "在线安装 docker"
      curl -fsSL https://resource.fit2cloud.com/get-docker-linux.sh -o get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
      if [[ ! -f get-docker.sh ]];then
         log "docker 在线安装脚本下载失败，请稍候重试"
         exit 1
      fi
      sudo sh get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
      log "启动 docker"
      systemctl enable docker >/dev/null 2>&1; systemctl daemon-reload; service docker start >/dev/null 2>&1 | tee -a ${CURRENT_DIR}/install.log
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
         log "离线安装 docker-compose"
         cp docker/bin/docker-compose /usr/bin/
         chmod +x /usr/bin/docker-compose
      else
         log "在线安装 docker-compose"
         curl -L https://resource.fit2cloud.com/docker/compose/releases/download/v2.24.5/docker-compose-$(uname -s | tr A-Z a-z)-$(uname -m) -o /usr/local/bin/docker-compose 2>&1 | tee -a ${CURRENT_DIR}/install.log
         if [[ ! -f /usr/local/bin/docker-compose ]];then
            log "docker-compose 下载失败，请稍候重试"
            exit 1
         fi
         chmod +x /usr/local/bin/docker-compose
         ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
      fi
   fi

   docker-compose version >/dev/null 2>&1
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

for i in $(docker images --format '{{.Repository}}:{{.Tag}}' | grep dataease); do
   current_images[${#current_images[@]}]=${i##*/}
done

# 加载镜像
if [[ -d images ]]; then
   log "加载镜像"
   for i in $(ls images); do
      if [ ${DE_ENGINE_MODE} != "local" ]; then
         if [[ $i =~ "doris" ]] || [[ $i =~ "kettle" ]]; then
            continue
         fi
      fi
      if [[ "${current_images[@]}"  =~ "${i%.tar.gz}" ]]; then
         echo "本地已存在镜像 ${i%.tar.gz}，略过加载"
      else
         docker load -i images/$i 2>&1 | tee -a ${CURRENT_DIR}/install.log
      fi
   done
else
   log "拉取镜像"
   cd ${DE_RUN_BASE} && docker-compose $compose_files pull 2>&1

   DEVERSION=$(cat ${CURRENT_DIR}/dataease/templates/version)
   curl -sfL https://resource.fit2cloud.com/installation-log.sh | sh -s de ${INSTALL_TYPE} ${DEVERSION}
   cd -
fi

if [[ -f /etc/init.d/dataease ]];then
   if which chkconfig >/dev/null 2>&1;then
      chkconfig dataease >/dev/null
      if [ $? -eq 0 ]; then
         chkconfig --del dataease
      fi
   fi
   rm -f /etc/init.d/dataease
fi

log "配置 dataease Service"
cp ${DE_RUN_BASE}/bin/dataease/dataease.service /etc/systemd/system/
chmod 644 /etc/systemd/system/dataease.service
log "配置开机自启动"
systemctl enable dataease >/dev/null 2>&1; systemctl daemon-reload | tee -a ${CURRENT_DIR}/install.log

if [[ $(grep "vm.max_map_count" /etc/sysctl.conf | wc -l) -eq 0 ]];then
   sysctl -w vm.max_map_count=2000000 >/dev/null 2>&1
   echo "vm.max_map_count=2000000" >> /etc/sysctl.conf
elif (( $(grep "vm.max_map_count" /etc/sysctl.conf | awk -F'=' '{print $2}') < 2000000 ));then
   sysctl -w vm.max_map_count=2000000 >/dev/null 2>&1
   sed -i 's/^vm\.max_map_count.*/vm\.max_map_count=2000000/' /etc/sysctl.conf
fi

if [ $(grep "net.ipv4.ip_forward" /etc/sysctl.conf | wc -l) -eq 0 ];then
   sysctl -w net.ipv4.ip_forward=1 >/dev/null 2>&1
   echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
else
   sed -i 's/^net\.ipv4\.ip_forward.*/net\.ipv4\.ip_forward=1/' /etc/sysctl.conf
fi

if which firewall-cmd >/dev/null 2>&1; then
   if systemctl is-active firewalld &>/dev/null ;then
      log "防火墙端口开放"
      firewall-cmd --zone=public --add-port=${DE_PORT}/tcp --permanent
      firewall-cmd --reload
   else
      log "防火墙未开启，忽略端口开放"
   fi
fi

log "启动服务"
systemctl start dataease 2>&1 | tee -a ${CURRENT_DIR}/install.log

echo -e "======================= 安装完成 =======================\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log
echo -e "请通过以下方式访问:\n\tURL: http://\$LOCAL_IP:$DE_PORT\n\t用户名: admin\n\t初始密码: dataease" 2>&1 | tee -a ${CURRENT_DIR}/install.log
