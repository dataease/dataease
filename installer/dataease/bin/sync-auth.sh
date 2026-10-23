#!/bin/bash

#
# 同步执行器鉴权只在部署阶段初始化，应用启动不得自动更换密钥
#
sync_auth_enabled() {
   [[ "${DE_INSTALL_MODE:-community}" != "community" ]]
}

#
# 只输出错误类型和处理办法，不包含密钥内容
#
sync_auth_error() {
   printf '同步执行器鉴权错误 [%s]：%s\n处理办法：%s\n' "$1" "$2" "$3" >&2
   return 1
}

sync_auth_validate() {
   if ! [[ "${DE_SYNC_AUTH_SECRET:-}" =~ ^[A-Za-z0-9+/]{42}[AEIMQUYcgkosw048]=$ ]]; then
      sync_auth_error CONFIG_INVALID "DE_SYNC_AUTH_SECRET 缺失或格式无效" "检查运行目录 .env，使用32字节随机数的Base64编码；优先恢复原值，重置时统一更新相关节点，再重试安装或 dectl start"
      return 1
   fi
}

#
# 保存单一配置来源，临时文件创建时即限制权限，不输出密钥
#
sync_auth_prepare() (
   sync_auth_enabled || exit 0
   local run_base="$1"
   local installed_auth="$2"
   umask 077
   set -o pipefail
   mkdir -p "$run_base" || {
      sync_auth_error DIRECTORY_FAILED "无法创建运行目录" "检查安装路径、目录权限、磁盘空间和只读挂载后重试"
      exit 1
   }
   if [[ -L "$run_base" || -L "$run_base/.env" || ( -e "$run_base/.env" && ! -f "$run_base/.env" ) ]]; then
      sync_auth_error CONFIG_PATH_INVALID "鉴权配置路径无效" "运行目录和 .env 不能为符号链接，.env 必须是普通文件"
      exit 1
   fi
   local lock_dir="$run_base/.sync-auth-init.lock"
   mkdir "$lock_dir" 2>/dev/null || {
      if [[ -d "$lock_dir" ]]; then
         sync_auth_error INIT_BUSY "初始化锁目录已存在" "等待其他安装进程结束；确认没有安装进程后，移除运行目录中的 .sync-auth-init.lock 空目录再重试"
      else
         sync_auth_error LOCK_FAILED "无法创建初始化锁目录" "检查运行目录的写入权限、磁盘空间和只读挂载后重试"
      fi
      exit 1
   }
   local tmp_file=""
   trap '[[ -z "$tmp_file" ]] || rm -f "$tmp_file"; rmdir "$lock_dir"' EXIT
   if [[ -z "${DE_SYNC_AUTH_SECRET:-}" ]]; then
      if [[ "${DE_SYNC_AUTH_VERSION:-}" == "1" || "$installed_auth" == "true" ]]; then
         sync_auth_error KEY_MISSING "已启用同步执行器鉴权，但密钥丢失" "从备份或相关节点恢复 .env 中的 DE_SYNC_AUTH_SECRET；无法恢复时显式配置新密钥并统一更新相关节点，再重新执行升级"
         exit 1
      fi
      if [[ "${DE_EXTERNAL_SYNC_TASK:-false}" == "true" ]]; then
         sync_auth_error EXTERNAL_KEY_REQUIRED "外部同步执行器未配置共享密钥" "在 .env 中配置与外部同步执行器一致的 DE_SYNC_AUTH_SECRET 后重试，不要在两端分别生成"
         exit 1
      fi
      DE_SYNC_AUTH_SECRET=$(head -c 32 /dev/urandom | base64 | tr -d '\r\n') || {
         sync_auth_error GENERATE_FAILED "无法生成同步执行器鉴权密钥" "检查 /dev/urandom 是否可读及 head、base64、tr 命令是否可用，再重新执行安装"
         exit 1
      }
   fi
   sync_auth_validate || exit 1
   tmp_file=$(mktemp "$run_base/.sync-auth-env.XXXXXX") || {
      sync_auth_error TEMP_FAILED "无法创建密钥配置临时文件" "检查运行目录的权限、磁盘空间和只读挂载后重试，原 .env 未替换"
      exit 1
   }
   if [[ -f "$run_base/.env" ]]; then
      sed '/^DE_SYNC_AUTH_SECRET=/d; /^DE_SYNC_AUTH_VERSION=/d' "$run_base/.env" > "$tmp_file" || {
         sync_auth_error READ_FAILED "无法保留原环境配置" "检查 .env 读取权限及磁盘空间后重试，原 .env 未替换"
         exit 1
      }
   fi
   printf '\nDE_SYNC_AUTH_SECRET=%s\nDE_SYNC_AUTH_VERSION=1\n' "$DE_SYNC_AUTH_SECRET" >> "$tmp_file" || {
      sync_auth_error WRITE_FAILED "无法写入密钥配置临时文件" "检查磁盘空间和写入权限后重试，原 .env 未替换"
      exit 1
   }
   chmod 600 "$tmp_file" || {
      sync_auth_error PERMISSION_FAILED "无法设置密钥配置文件权限" "检查文件系统是否支持权限设置及当前账户权限后重试，原 .env 未替换"
      exit 1
   }
   mv "$tmp_file" "$run_base/.env" || {
      sync_auth_error SAVE_FAILED "无法替换环境配置文件" "检查目录写入权限和只读挂载后重试，不要删除原 .env"
      exit 1
   }
   tmp_file=""
)

#
# 仅备份鉴权迁移涉及的现有配置，备份与原目录使用相同的访问边界
#
sync_auth_backup() (
   local run_base="$1"
   [[ -d "$run_base" ]] || exit 0
   umask 077
   local backup_dir
   backup_dir=$(mktemp -d "$run_base/.sync-auth-backup.XXXXXXXX") || {
      sync_auth_error BACKUP_FAILED "无法创建升级配置备份" "检查运行目录权限和磁盘空间，旧服务尚未停止，修复后重试升级"
      exit 1
   }
   local file
   for file in .env docker-compose.yml docker-compose-task.yml; do
      if [[ -f "$run_base/$file" ]]; then
         cp "$run_base/$file" "$backup_dir/$file" && chmod 600 "$backup_dir/$file" || {
            sync_auth_error BACKUP_FAILED "无法备份升级配置" "检查原配置读取权限及磁盘空间，旧服务尚未停止，修复后重试升级"
            exit 1
         }
      fi
   done
   printf '同步执行器鉴权升级配置已备份至：%s\n' "$backup_dir"
)

#
# 同步鉴权两端必须使用配套镜像，同标签升级也不复用未经确认的旧镜像
#
sync_auth_prepare_images() (
   sync_auth_enabled || exit 0
   local package_dir="$1"
   local compose_file image archive candidate
   local files=(docker-compose.yml)
   if [[ "${DE_EXTERNAL_SYNC_TASK:-false}" != "true" ]]; then
      files+=(docker-compose-task.yml)
   fi
   for compose_file in "${files[@]}"; do
      image=$(awk '$1 == "image:" { print $2; exit }' "$package_dir/dataease/$compose_file")
      if [[ -z "$image" || "$image" == *DE_TAG* ]]; then
         sync_auth_error IMAGE_CONFIG_INVALID "安装包中的同步鉴权镜像配置无效" "使用已完成版本替换的配套安装包，不要直接运行源码目录中的安装脚本"
         exit 1
      fi
      if [[ -d "$package_dir/images" ]]; then
         archive=""
         for candidate in "$package_dir/images/${image##*/}.tar.gz" "$package_dir/images/${image##*/}.tar"; do
            [[ -f "$candidate" ]] && archive="$candidate" && break
         done
         if [[ -z "$archive" ]]; then
            sync_auth_error IMAGE_ARCHIVE_MISSING "离线包缺少主服务或同步执行器的配套镜像" "使用包含两端镜像的完整离线包；不要以本地同标签旧镜像代替"
            exit 1
         fi
         docker load -i "$archive" || {
            sync_auth_error IMAGE_LOAD_FAILED "同步鉴权配套镜像加载失败" "检查离线镜像文件完整性、Docker 状态及磁盘空间后重新升级"
            exit 1
         }
      else
         docker pull "$image" || {
            sync_auth_error IMAGE_PULL_FAILED "同步鉴权配套镜像拉取失败" "检查镜像仓库访问、网络和磁盘空间后重新升级，勿只更新主服务"
            exit 1
         }
      fi
   done
)
