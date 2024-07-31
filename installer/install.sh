#!/bin/bash

INSTALL_TYPE='install'
title_count=1

CURRENT_DIR=$(
   cd "$(dirname "$0")"
   pwd
)

function log() {
   echo -e "${1}" 2>&1 | tee -a ${CURRENT_DIR}/install.log
}

function log_title () {
   log "${title_count}. ${1}"
   let title_count++
}

function log_content () {
   log "\t${1}"
}

function prop {
   [ -f "$1" ] | grep -P "^\s*[^#]?${2}=.*$" $1 | cut -d'=' -f2
}

function check_and_prepare_env_params() {
   log "当前时间 : $(date)"
   log_title "检查安装环境并初始化环境变量"
   DE_APISIX_PORT=9080

   cd ${CURRENT_DIR}
   if [ -f /usr/bin/dectl ]; then
      # 获取已安装的 DataEase 的运行目录
      DE_BASE=$(grep "^DE_BASE=" /usr/bin/dectl | cut -d'=' -f2)
      DE_BASE_OLD=$DE_BASE
      sed -i -e "s#DE_BASE=.*#DE_BASE=${DE_BASE}#g" dectl
      \cp dectl /usr/local/bin && chmod +x /usr/local/bin/dectl

      log_content "停止 DataEase 服务"
      if [[ -f /etc/systemd/system/dataease.service ]];then
         systemctl stop dataease
      else
         dectl stop
      fi

      INSTALL_TYPE='upgrade'

      v2_version=$(dectl version | head -n 2 | grep "v2.")
      if [[ -z $v2_version ]];then
         echo "系统当前版本不是 DataEase v2 版本系列，不支持升级到 v2，请检查离线包版本。"
         exit 1;
      fi
   fi

   set -a
   source ${CURRENT_DIR}/install.conf
   if [[ $DE_BASE_OLD ]];then
      DE_BASE=$DE_BASE_OLD
      export DE_BASE=$DE_BASE_OLD
   fi
   if [[ -d $DE_BASE ]] && [[ -f $DE_BASE/dataease2.0/.env ]]; then
      source $DE_BASE/dataease2.0/.env
      INSTALL_TYPE='upgrade'

      conf_install_mode=$(prop $CURRENT_DIR/install.conf DE_INSTALL_MODE)
      if [[ $DE_INSTALL_MODE == 'community' ]] && [[ $conf_install_mode == 'enterprise' ]];then
         DE_INSTALL_MODE=$conf_install_mode
         export DE_INSTALL_MODE=$conf_install_mode
      fi
      log_content "升级安装"
   else
      INSTALL_TYPE='install'
      mkdir -p ${DE_BASE}
      log_content "全新安装"
   fi
   set +a

   read available_disk <<< $(df -H --output=avail ${DE_BASE} | tail -1)
   available_disk=${available_disk%?}
   available_disk=${available_disk%.*}
   if [[ $available_disk -lt 20 ]];then
      log_content "\033[31m[警告] DataEase 运行目录所在磁盘剩余空间不足 20G 可能无法正常启动!\033[0m"
   fi
}

function set_run_base_path() {
   log_title "设置运行目录"
   DE_RUN_BASE=$DE_BASE/dataease2.0
   CONF_FOLDER=${DE_RUN_BASE}/conf
   TEMPLATES_FOLDER=${DE_RUN_BASE}/templates
   log_content "运行目录 $DE_RUN_BASE"
   log_content "配置文件目录 $CONF_FOLDER"
}

function prepare_de_run_base() {
   log_title "初始化运行目录"
   cd ${CURRENT_DIR}
   mkdir -p ${DE_RUN_BASE}
   log_content "复制安装文件到运行目录"
   cp -r ./dataease/* ${DE_RUN_BASE}/

   cd $DE_RUN_BASE
   env | grep DE_ >.env

   mkdir -p ${DE_RUN_BASE}/{cache,logs,conf}
   mkdir -p ${DE_RUN_BASE}/data/{mysql,static-resource,map,etcd_data,geo,appearance,exportData,plugin}
   mkdir -p ${DE_RUN_BASE}/apisix/logs
   mkdir -p ${DE_RUN_BASE}/task/logs
   chmod 777 ${DE_RUN_BASE}/apisix/logs ${DE_RUN_BASE}/data/etcd_data ${DE_RUN_BASE}/task/logs

   if [ "${DE_EXTERNAL_MYSQL}" = "false" ]; then
      sed -i -e "s/^      DE_MYSQL_HOST/      ${DE_MYSQL_HOST}/g" docker-compose.yml
      sed -i -e "s/^. DE_MYSQL_HOST/  ${DE_MYSQL_HOST}/g" docker-compose-mysql.yml
   else
      sed -i -e "/^    depends_on/,+2d" docker-compose.yml
   fi

   log_content "调整配置文件参数"
   cd $DE_RUN_BASE
   cp -r $TEMPLATES_FOLDER/* $CONF_FOLDER

   cd ${TEMPLATES_FOLDER}
   templates_files=( application.yml mysql.env )
   for i in ${templates_files[@]}; do
      if [ -f $i ]; then
         envsubst < $i > $CONF_FOLDER/$i
      fi
   done
}

function update_dectl() {
   log_title "安装 dectl 命令行工具"
   log_content "安装至 /usr/local/bin/dectl & /usr/bin/dectl"
   cd ${CURRENT_DIR}
   sed -i -e "s#DE_BASE=.*#DE_BASE=${DE_BASE}#g" dectl
   \cp dectl /usr/local/bin && chmod +x /usr/local/bin/dectl
   if [ ! -f /usr/bin/dectl ]; then
      ln -s /usr/local/bin/dectl /usr/bin/dectl 2>/dev/null
   fi
}

function prepare_system_settings() {
   log_title "修改操作系统相关设置"
   if which getenforce >/dev/null 2>&1 && [ $(getenforce) == "Enforcing" ];then
      log_content  "关闭 SELINUX"
      setenforce 0
      sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
   fi

   if which firewall-cmd >/dev/null 2>&1; then
      if systemctl is-active firewalld &>/dev/null ;then
         log_content "开启防火墙端口 ${DE_PORT}"
         firewall-cmd --zone=public --add-port=${DE_PORT}/tcp --permanent
         firewall-cmd --reload
      else
         log_content "防火墙未开启，忽略端口开放"
      fi
   fi
}

function install_docker() {
   log_title "安装 docker"
   #Install docker
   ##Install Latest Stable Docker Release
   cd ${CURRENT_DIR}

   if which docker >/dev/null 2>&1; then
      log_content "检测到 Docker 已安装，跳过安装步骤"
      log_content "启动 Docker "
      service docker start >/dev/null 2>&1 | tee -a ${CURRENT_DIR}/install.log
   else
      if [[ -d docker ]]; then
         log_content "离线安装 docker"
         cp docker/bin/* /usr/bin/
         cp docker/service/docker.service /etc/systemd/system/
         chmod +x /usr/bin/docker*
         chmod 644 /etc/systemd/system/docker.service
         log_content "启动 docker"
         systemctl enable docker >/dev/null 2>&1; systemctl daemon-reload; systemctl start docker 2>&1 | tee -a ${CURRENT_DIR}/install.log
      else
         log_content "在线安装 docker"
         curl -fsSL https://resource.fit2cloud.com/get-docker-linux.sh -o get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
         if [[ ! -f get-docker.sh ]];then
            log_content "docker 在线安装脚本下载失败，请稍候重试"
            exit 1
         fi
         sudo sh get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
         log_content "启动 docker"
         systemctl enable docker >/dev/null 2>&1; systemctl daemon-reload; systemctl start docker 2>&1 | tee -a ${CURRENT_DIR}/install.log
      fi

      docker_config_folder="/etc/docker"
      if [ ! -d "$docker_config_folder" ];then
         mkdir -p "$docker_config_folder"
      fi

      docker version >/dev/null 2>&1
      if [ $? -ne 0 ]; then
         log_content "docker 安装失败"
         exit 1
      else
         log_content "docker 安装成功"
      fi
   fi
}

function install_docker_compose() {
   log_title "安装 docker-compose"
   #Install docker-compose
   cd ${CURRENT_DIR}
   ##Install Latest Stable Docker Compose Release
   docker-compose version >/dev/null 2>&1
   if [ $? -ne 0 ]; then
      docker compose version >/dev/null 2>&1
      if [ $? -eq 0 ]; then
         echo 'docker compose "$@"' > /usr/bin/docker-compose
         chmod +x /usr/bin/docker-compose
      else
         if [[ -d docker ]]; then
            log_content "离线安装 docker-compose"
            cp docker/bin/docker-compose /usr/bin/
            chmod +x /usr/bin/docker-compose
         else
            log_content "在线安装 docker-compose"
            curl -L https://resource.fit2cloud.com/docker/compose/releases/download/v2.16.0/docker-compose-$(uname -s | tr A-Z a-z)-$(uname -m) -o /usr/local/bin/docker-compose 2>&1 | tee -a ${CURRENT_DIR}/install.log
            if [[ ! -f /usr/local/bin/docker-compose ]];then
               log_content "docker-compose 下载失败，请稍候重试"
               exit 1
            fi
            chmod +x /usr/local/bin/docker-compose
            ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
         fi
      fi

      docker-compose version >/dev/null
      if [ $? -ne 0 ]; then
         log_content "docker-compose 安装失败"
         exit 1
      else
         log_content "docker-compose 安装成功"
      fi
   else
      log_content "检测到 Docker Compose 已安装，跳过安装步骤"
   fi
   export COMPOSE_HTTP_TIMEOUT=180
}

function load_de_images() {
   log_title "加载 DataEase 镜像"
   cd ${CURRENT_DIR}

   for i in $(docker images --format '{{.Repository}}:{{.Tag}}' | grep dataease); do
      current_images[${#current_images[@]}]=${i##*/}
   done

   # 加载镜像
   if [[ -d images ]]; then
      for i in $(ls images); do
         if [[ "${current_images[@]}"  =~ "${i%.tar.gz}" ]]; then
            log_content "已存在镜像 ${i%.tar.gz}"
         else
            log_content "加载镜像 ${i%.tar.gz}"
            docker load -i images/$i >/dev/null 2>&1 | tee -a ${CURRENT_DIR}/install.log
         fi
      done
   else
      DEVERSION=$(cat ${CURRENT_DIR}/dataease/templates/version)
      curl -sfL https://resource.fit2cloud.com/installation-log.sh | sh -s de ${INSTALL_TYPE} ${DEVERSION}
   fi
}

function set_de_service() {
   log_title "配置 DataEase 服务"
   if [[ -f /etc/init.d/dataease ]];then
      if which chkconfig >/dev/null 2>&1;then
         chkconfig dataease >/dev/null
         if [ $? -eq 0 ]; then
            chkconfig --del dataease
         fi
      fi
      rm -f /etc/init.d/dataease
   fi

   if [[ ! -f /etc/systemd/system/dataease.service ]];then
      log_content "配置 dataease Service"
      cp ${DE_RUN_BASE}/bin/dataease/dataease.service /etc/systemd/system/
      chmod 644 /etc/systemd/system/dataease.service
      log_content "配置开机自启动"
      systemctl enable dataease >/dev/null 2>&1; systemctl daemon-reload | tee -a ${CURRENT_DIR}/install.log
   fi
}

function start_de_service() {
   log_title "启动 DataEase 服务"
   systemctl start dataease 2>&1 | tee -a ${CURRENT_DIR}/install.log

   access_port=$DE_PORT
   if [[ $DE_INSTALL_MODE != "community" ]];then
      access_port=$DE_APISIX_PORT
   fi

   echo
   if [[ $INSTALL_TYPE != "upgrade" ]];then
      echo -e "======================= 安装完成 =======================\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log
      echo -e "系统登录信息如下:\n\t访问地址: http://服务器IP:$access_port\n\t用户名: admin\n\t初始密码: DataEase@123456" 2>&1 | tee -a ${CURRENT_DIR}/install.log
   else
      echo -e "======================= 升级完成 =======================\n" 2>&1 | tee -a ${CURRENT_DIR}/install.log
   fi
}

function main() {
   check_and_prepare_env_params
   set_run_base_path
   prepare_de_run_base
   update_dectl
   prepare_system_settings
   install_docker
   install_docker_compose
   load_de_images
   set_de_service
   start_de_service
}

main
