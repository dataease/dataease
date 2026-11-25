# 部署启动回归

部署统一使用 `Asia/Shanghai`，不再检测或持久化宿主机时区。基础镜像仍需保留 `tzdata` 并重新构建应用镜像。旧 `.env` 中的 `DE_TIMEZONE`、`DE_RESOLVED_TIMEZONE` 不再使用，无需手动清理。

执行 `python3 -B -m unittest discover -s installer/tests -p 'test_deployment_start.py' -v`。测试使用临时目录和命令桩，覆盖 Compose 更新失败但旧容器健康、正常启动、主服务不健康/等待超时，以及工作目录不可访问；不启动实际服务。

检查 Shell 语法：`bash -n installer/install.sh installer/dectl`。实际镜像日期、JVM 时区、日志及告警调度仍需隔离 Docker 环境验收，离线测试不代表产品回归通过。
