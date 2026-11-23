#!/bin/bash

# DE_TIMEZONE=host 时每次启动重新识别，不把上次检测结果作为固定配置。
de_timezone_valid() {
   local zone="$1"
   local zoneinfo="$2"
   [[ "$zone" =~ ^[A-Za-z0-9_+-]+(/[A-Za-z0-9_+-]+)*$ ]] || return 1
   [[ -f "$zoneinfo/$zone" && "$(head -c 4 "$zoneinfo/$zone")" == "TZif" ]]
}

de_timezone_resolve() {
   local zoneinfo="${1:-/usr/share/zoneinfo}"
   local localtime="${2:-/etc/localtime}"
   local timezone_file="${3:-/etc/timezone}"
   local zone="${DE_TIMEZONE:-host}"
   local candidate=""
   if [[ "$zone" == "host" ]]; then
      [[ -r "$localtime" ]] || {
         echo "无法读取宿主机时区文件 $localtime，请修复文件或显式配置 DE_TIMEZONE" >&2
         return 1
      }
      candidate=$(timedatectl show --property=Timezone --value 2>/dev/null) || candidate=""
      if de_timezone_valid "$candidate" "$zoneinfo" && cmp -s "$localtime" "$zoneinfo/$candidate"; then
         zone="$candidate"
      else
         candidate=$(readlink "$localtime") || candidate=""
         candidate="${candidate#*/zoneinfo/}"
         if de_timezone_valid "$candidate" "$zoneinfo" && cmp -s "$localtime" "$zoneinfo/$candidate"; then
            zone="$candidate"
         else
            candidate=""
            [[ ! -r "$timezone_file" ]] || read -r candidate < "$timezone_file"
            if de_timezone_valid "$candidate" "$zoneinfo" && cmp -s "$localtime" "$zoneinfo/$candidate"; then
               zone="$candidate"
            else
               echo "无法可靠识别宿主机时区，请检查 timedatectl、/etc/localtime，或配置 DE_TIMEZONE=地域/城市" >&2
               return 1
            fi
         fi
      fi
   fi
   de_timezone_valid "$zone" "$zoneinfo" || {
      echo "DE_TIMEZONE 无效或宿主机缺少对应时区数据：$zone" >&2
      return 1
   }
   export DE_TIMEZONE="${DE_TIMEZONE:-host}"
   export DE_RESOLVED_TIMEZONE="$zone"
}

# 只更新时区配置，不重写其他环境变量或密钥；新文件仅所有者可读写。
de_timezone_save() (
   local env_file="$1"
   local tmp_file=""
   umask 077
   tmp_file=$(mktemp "${env_file}.timezone.XXXXXX") || exit 1
   trap 'rm -f "$tmp_file"' EXIT
   if [[ -f "$env_file" ]]; then
      awk '!/^(DE_TIMEZONE|DE_RESOLVED_TIMEZONE)=/' "$env_file" > "$tmp_file" || exit 1
   fi
   printf 'DE_TIMEZONE=%s\nDE_RESOLVED_TIMEZONE=%s\n' "$DE_TIMEZONE" "$DE_RESOLVED_TIMEZONE" >> "$tmp_file" || exit 1
   mv -f "$tmp_file" "$env_file"
)
