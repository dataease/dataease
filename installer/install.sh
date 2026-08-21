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
   [ -f "$1" ] && grep -P "^\s*[^#]?${2}=.*$" "$1" | cut -d'=' -f2
}

# ==================== 数据库类型相关 ====================
# 根据数据库类型设置默认端口、默认 JDBC 参数、驱动 jar
function db_defaults() {
   case "$1" in
      mysql)
         DB_DEFAULT_PORT=3306
         DB_DEFAULT_PARAMS="autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true"
         DB_DRIVER_JAR="mariadb-java-client-3.5.3.jar"
         ;;
      pg)
         DB_DEFAULT_PORT=5432
         DB_DEFAULT_PARAMS="connectTimeout=10"
         DB_DRIVER_JAR="postgresql-42.7.11.jar"
         ;;
      oracle)
         DB_DEFAULT_PORT=1521
         DB_DEFAULT_PARAMS="useUnicode=true&characterEncoding=UTF-8"
         DB_DRIVER_JAR="ojdbc10-19.19.0.0.jar"
         ;;
      dm)
         DB_DEFAULT_PORT=5236
         DB_DEFAULT_PARAMS="charset=UTF-8&loginTimeout=10&connectTimeout=10000"
         DB_DRIVER_JAR="DmJdbcDriver18.jar"
         ;;
      kingbase)
         DB_DEFAULT_PORT=54321
         DB_DEFAULT_PARAMS="connectTimeout=10"
         DB_DRIVER_JAR="kingbase8-9.0.1.jar"
         ;;
      sqlserver)
         DB_DEFAULT_PORT=1433
         DB_DEFAULT_PARAMS="encrypt=false"
         DB_DRIVER_JAR="mssql-jdbc-13.4.0.jre11.jar"
         ;;
      greatsql)
         DB_DEFAULT_PORT=3306
         DB_DEFAULT_PARAMS="autoReconnect=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true"
         DB_DRIVER_JAR="mariadb-java-client-3.5.3.jar"
         ;;
      *)
         DB_DEFAULT_PORT=3306
         DB_DEFAULT_PARAMS=""
         DB_DRIVER_JAR=""
         ;;
   esac
}

# 询问外部数据库连接信息
function prompt_db_connection() {
   local answer
   read -r -p "请输入数据库地址 [localhost]: " answer; DE_DB_HOST="${answer:-localhost}"
   read -r -p "请输入数据库端口 [${DB_DEFAULT_PORT}]: " answer; DE_DB_PORT="${answer:-$DB_DEFAULT_PORT}"
   read -r -p "请输入数据库用户名 [root]: " answer; DE_DB_USER="${answer:-root}"
   read -r -s -p "请输入数据库密码: " answer; echo; DE_DB_PASSWORD="$answer"
   read -r -p "请输入数据库名 [dataease]: " answer; DE_DB_DATABASE="${answer:-dataease}"
   read -r -p "请输入数据库 schema（MySQL/GreatSQL 可留空）: " answer; DE_DB_SCHEMA="$answer"
   read -r -p "请输入 JDBC 连接参数 [${DB_DEFAULT_PARAMS}]: " answer; DE_DB_PARAMS="${answer:-$DB_DEFAULT_PARAMS}"
}

# 询问运行数据库类型（仅全新安装）
function prompt_database_config() {
   # 升级安装不允许切换数据库
   [[ $INSTALL_TYPE == 'upgrade' ]] && return
   # 非交互模式（无 TTY）直接使用 install.conf 配置
   [[ ! -t 0 ]] && return

   log_title "选择运行数据库"
   echo -e "请选择 DataEase 运行数据库类型："
   echo -e "  1) MySQL"
   echo -e "  2) PostgreSQL"
   echo -e "  3) Oracle"
   echo -e "  4) DM (达梦)"
   echo -e "  5) Kingbase (人大金仓)"
   echo -e "  6) SQLServer"
   echo -e "  7) GreatSQL"
   local answer
   read -r -p "请输入序号 [1-7]，默认 1 (MySQL): " answer
   case "${answer:-1}" in
      2) DE_DB_TYPE=pg ;;
      3) DE_DB_TYPE=oracle ;;
      4) DE_DB_TYPE=dm ;;
      5) DE_DB_TYPE=kingbase ;;
      6) DE_DB_TYPE=sqlserver ;;
      7) DE_DB_TYPE=greatsql ;;
      *) DE_DB_TYPE=mysql ;;
   esac

   if [[ "$DE_DB_TYPE" == "mysql" ]]; then
      read -r -p "是否使用 DataEase 内置 MySQL？[Y/n]（默认内置）: " answer
      case "${answer:-Y}" in
         [nN]|[nN][oO]) DE_EXTERNAL_DB=true ;;
         *) DE_EXTERNAL_DB=false ;;
      esac
   else
      DE_EXTERNAL_DB=true
   fi

   db_defaults "$DE_DB_TYPE"

   if [[ "$DE_EXTERNAL_DB" == "true" ]]; then
      log_content "配置外部数据库连接信息"
      prompt_db_connection
   fi

   # 设置 spring profile，用于加载对应数据库的 jpa 配置
   DE_SPRING_PROFILE="$DE_DB_TYPE"

   export DE_DB_TYPE DE_EXTERNAL_DB DE_DB_HOST DE_DB_PORT DE_DB_DATABASE DE_DB_SCHEMA \
          DE_DB_USER DE_DB_PASSWORD DE_DB_PARAMS DE_SPRING_PROFILE

   log_content "数据库类型: ${DE_DB_TYPE}, 使用外置数据库: ${DE_EXTERNAL_DB}"
}

# 复制数据库驱动到 data/driver
function copy_db_driver() {
   db_defaults "$DE_DB_TYPE"
   if [[ -z "$DB_DRIVER_JAR" ]]; then
      log_content "[警告] ${DE_DB_TYPE} 未配置驱动 jar，请确认 drivers 目录包含对应驱动"
      return
   fi
   local drivers_folder="${CURRENT_DIR}/drivers"
   [[ -d "$drivers_folder" ]] || drivers_folder="${CURRENT_DIR}/../drivers"
   local driver_source="${drivers_folder}/${DB_DRIVER_JAR}"
   if [[ -f "$driver_source" ]]; then
      mkdir -p ${DE_RUN_BASE}/data/driver
      cp "$driver_source" ${DE_RUN_BASE}/data/driver/
      log_content "已复制数据库驱动: ${DB_DRIVER_JAR}"
   else
      log_content "[警告] 未找到驱动 ${driver_source}，请检查安装包是否包含该驱动"
   fi
}

function check_and_prepare_env_params() {
   log "当前时间 : $(date)"
   log_title "检查安装环境并初始化环境变量"
   DE_APISIX_PORT=9080

   cd ${CURRENT_DIR}
   if [ -f /usr/bin/dectl ]; then
      v3_version=$(dectl version | head -n 2 | grep "v3.")
      if [[ -z $v3_version ]];then
         echo "系统当前版本不是 DataEase v3 版本系列，不支持升级到 v3，请检查离线包版本。"
         exit 1;
      fi
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
   fi

   set -a
   source ${CURRENT_DIR}/install.conf
   if [[ $DE_BASE_OLD ]];then
      DE_BASE=$DE_BASE_OLD
      export DE_BASE=$DE_BASE_OLD
   fi
   if [[ -d $DE_BASE ]] && [[ -f $DE_BASE/dataease3.0/.env ]]; then
      source $DE_BASE/dataease3.0/.env
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

   prompt_database_config

   read available_disk <<< $(df -H --output=avail "${DE_BASE}" | tail -1)
   disk_num=${available_disk%[KMGTP]}
   disk_unit=${available_disk##*[0-9.]}
   case $disk_unit in
     K) disk_gb=$(awk -v i="$disk_num" 'BEGIN{printf "%.0f\n", i / 1024 / 1024}') ;;
     M) disk_gb=$(awk -v i="$disk_num" 'BEGIN{printf "%.0f\n", i / 1024}') ;;
     G) disk_gb=${disk_num%.*} ;;
     T) disk_gb=$(awk -v i="$disk_num" 'BEGIN{printf "%.0f\n", 1024 * i}') ;;
     *) disk_gb=${disk_num%.*} ;;
   esac
   [[ $disk_gb -lt 20 ]] && log_content "\033[31m[警告] DataEase 运行目录所在磁盘剩余空间不足 20G 可能无法正常启动!\033[0m"
   }

function set_run_base_path() {
   log_title "设置运行目录"
   DE_RUN_BASE=$DE_BASE/dataease3.0
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
   mkdir -p ${DE_RUN_BASE}/data/{mysql,static-resource,map,etcd_data,geo,appearance,exportData,plugin,font,i18n,report,driver}
   mkdir -p ${DE_RUN_BASE}/apisix/logs
   mkdir -p ${DE_RUN_BASE}/task/logs
   chmod 777 ${DE_RUN_BASE}/apisix/logs ${DE_RUN_BASE}/data/etcd_data ${DE_RUN_BASE}/task/logs

   if [ "${DE_EXTERNAL_DB}" = "false" ]; then
      sed -i -e "s/^      DE_MYSQL_HOST/      ${DE_DB_HOST}/g" docker-compose.yml
      sed -i -e "s/^. DE_MYSQL_HOST/  ${DE_DB_HOST}/g" docker-compose-mysql.yml
      export DE_DB_PORT=3306
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

   # 生成数据库 jpa 配置（application-<profile>.yml）
   if [[ -n "$DE_SPRING_PROFILE" && -f "jpa/application-${DE_SPRING_PROFILE}.yml" ]]; then
      envsubst < "jpa/application-${DE_SPRING_PROFILE}.yml" > "$CONF_FOLDER/application-${DE_SPRING_PROFILE}.yml"
      log_content "已启用数据库配置 profile: ${DE_SPRING_PROFILE}"
   else
      log_content "[警告] 未找到数据库 jpa 模板 jpa/application-${DE_SPRING_PROFILE}.yml"
   fi

   # 复制数据库驱动到 data/driver
   copy_db_driver

   # 内置地图由镜像 map-origin 提供，持久化 map 仅保存用户覆盖文件
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
      else
         log_content "在线安装 docker"
         curl -fsSL https://resource.fit2cloud.com/get-docker-linux.sh -o get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
         if [[ ! -f get-docker.sh ]];then
            log_content "docker 在线安装脚本下载失败，请稍候重试"
            exit 1
         fi
         sudo sh get-docker.sh 2>&1 | tee -a ${CURRENT_DIR}/install.log
      fi

      docker_config_folder="/etc/docker"
      if [ ! -d "$docker_config_folder" ];then
         mkdir -p "$docker_config_folder"
         cat <<EOF> $docker_config_folder/daemon.json
         {
            "log-driver": "json-file",
            "log-opts": {
               "max-file": "3",
               "max-size": "10m"
            }
         }
EOF
      fi

      log_content "启动 docker"
      systemctl enable docker >/dev/null 2>&1; systemctl daemon-reload; systemctl start docker 2>&1 | tee -a ${CURRENT_DIR}/install.log

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
            curl -L https://resource.fit2cloud.com/docker/compose/releases/download/v2.29.2/docker-compose-$(uname -s | tr A-Z a-z)-$(uname -m) -o /usr/local/bin/docker-compose 2>&1 | tee -a ${CURRENT_DIR}/install.log
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

   # 判断是否为wsl
   local is_wsl= false
   if grep -qE "(Microsoft|microsoft|WLS)" /proc/version; then
      is_wsl=true
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

   if [[ ! -f /etc/systemd/system/dataease.service ]];then
      log_content "配置 dataease Service"
      cp ${DE_RUN_BASE}/bin/dataease/dataease.service /etc/systemd/system/
      #--- 如果是 WSL，则移除 service 文件中对 docker 的依赖 ---
      if [ "$is_wsl" = true ]; then
         log_content "检测到 WSL 环境，移除 dataease.service 中的 Docker 依赖配置"
         sed -i '/docker.service/d' /etc/systemd/system/dataease.service
      fi
      #------------------------------------------------------
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
