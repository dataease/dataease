# 部署时区回归

默认 `DE_TIMEZONE=host`，安装及每次 `dectl start` / `reload` / `restart` 都会重新读取宿主机时区。非 systemd 主机依次尝试 `/etc/localtime` 符号链接、与实际规则匹配的 `/etc/timezone`；无法可靠识别时，在 `install.conf`（首次安装）或运行目录 `.env`（已安装）设置完整时区名，例如 `DE_TIMEZONE=Europe/Berlin`。这时部署固定使用该时区，不再跟随宿主机。

`DE_RESOLVED_TIMEZONE` 是自动生成的结果，供 Compose 的 `TZ` 使用，不应手工维护。脚本只更新 `.env` 的两个时区键，保留其他配置与密钥。部署时区同时用于主服务、内置同步执行器、APISIX 和 Playwright；不改变 MySQL 的数据库/会话时区，也不管理外部执行器。

必须先发布保留 `tzdata` 的基础镜像，再基于它重新构建 DataEase 应用镜像。同步执行器等其他镜像也应确认安装了对应系统时区数据。仅修改 Compose、继续使用缺少时区数据的旧镜像，不能视为完整修复。自定义 JVM 参数、应用代码、日志配置如有独立时区覆盖，需一并检查。

宿主机更改时区后执行 `dectl reload`，重新读取时区并由 Compose 根据环境变量变化重建容器。不要仅执行 `docker restart`。即使时区名称未变，规则文件已更新时也应重建相关镜像并重建容器，使 JVM 加载新的规则。直接执行 Compose 只使用 `.env` 上次保存的结果，不会自动检测宿主机。

## 离线检查

从主仓库根目录执行：

```bash
python3 -B -m unittest discover -s installer/tests -p 'test_timezone.py' -v
bash -n installer/install.sh installer/dectl installer/dataease/bin/timezone.sh
```

测试使用临时目录和命令桩，不连接 Docker、数据库或业务服务，不发送通知。覆盖多个地域时区、非 systemd 回退、配置冲突/无效值、旧检测值刷新、环境文件幂等更新与密钥保留，以及安装/重启预检失败时不停止服务。不是产品或镜像运行回归。

## 实际镜像与部署验收

- 在基础镜像仓库运行 `tests/timezone-smoke.sh <本地构建的镜像标签>`，分别验证上海、UTC、柏林和纽约的系统日期与 JVM 日期，并验证没有 `TZ` 时默认上海时区。需要 Docker 和 Java 21 的 `javac`。
- 在隔离 Linux 环境覆盖首次安装、旧版升级、systemd 启动、`dectl reload` 和手动指定时区。检查 `/usr/share/zoneinfo`、容器 `date`、实际 JVM 默认时区和新日志时间，核对所有受管理容器。
- 验证每小时、每日、每周告警的触发时间，检查已有 Quartz 触发器的时区。已有持久化触发器和夏令时跳变不能仅凭日志修正就认定正确，不要用真实接收者做通知测试。
- 时区配置失败应在升级/重启停止旧服务之前报错；修复后重试。回滚时应同时恢复匹配的应用镜像、Compose、控制脚本及 `.env` 备份，不改写历史日志和业务时间数据。
