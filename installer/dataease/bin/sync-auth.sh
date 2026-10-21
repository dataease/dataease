#!/bin/bash

# 同步执行器鉴权只在部署阶段初始化，应用启动不得自动更换密钥
sync_auth_enabled() {
   [[ "${DE_INSTALL_MODE:-community}" != "community" ]] && {
      [[ "${DE_EXTERNAL_SYNC_TASK:-false}" != "true" ]] ||
      [[ "${DE_SYNC_AUTH_REQUIRED:-false}" == "true" ]] ||
      [[ -n "${DE_SYNC_AUTH_SECRET:-}" ]]
   }
}

# 只输出错误类型和处理办法，不包含密钥内容
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

# 保存单一配置来源，临时文件创建时即限制权限，不输出密钥
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
