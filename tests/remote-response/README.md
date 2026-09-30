# 远程响应限制回归

独立 Java 21 回归入口，不依赖数据库，不修改现有数据源。测试使用本机随机端口 HTTP 服务、临时目录以及生成的 XLS/XLSX 数据。

先编译 SDK 和后端；将后端运行依赖目录作为参数传给下列命令（例如解包 CoreApplication.jar 后的 BOOT-INF/lib）。不能以 Maven test 的成功代替本回归执行，因为项目 Surefire 跳过测试。

```sh
mkdir -p /tmp/remote-response-test
CP="sdk/common/target/classes:sdk/extensions/extensions-datasource/target/classes:sdk/api/api-base/target/classes:core/core-backend/target/classes:$DE_TEST_LIB/*"
javac -proc:none -cp "$CP" -d /tmp/remote-response-test tests/remote-response/Regression.java
java -Xmx512m -cp "/tmp/remote-response-test:$CP" Regression
```

覆盖正常响应、声明长度超限、无长度分块超限、gzip 解压超限、持续慢速响应和响应头等待、文件下载和超限清理、配置默认值/上下界/实际覆盖、API Provider 配置传递、CSV 引号与预览提前停止、多工作表 XLS/XLSX 的预览和全量读取、ZIP 解压比异常。

## 配置与影响

- 每个 API 接口的高级设置：`maxResponseSizeMb`，默认 16 MB，1–64 MB；`transferTimeoutSeconds`，默认 120 秒，1–1800 秒。分页逐次请求限制，不是整次同步的累计额度。
- 远程文件配置：`maxFileSizeMb`，默认 100 MB，1–1024 MB；同名传输总时限。HTTP、FTP、SMB 共用配置。
- 字段缺失/null 采用默认值；零、负数和越界值拒绝。配置存于原有配置 JSON，无新增数据库列。
- 原有连接/读取超时继续生效；增大总时限不会取消它们。
- 公共 HTTP 工具的其他调用使用默认响应限制。限制按实际接收的解压后响应计数，不能依赖 Content-Length 绕过。
- 远程 XLSX 还受独立解析安全预算保护：10000 ZIP 条目，单条目 32 MB、总展开 128 MB，超过 1 MB 的条目压缩比不超过 100，校验最长 120 秒。增大下载上限不会取消解析安全预算。
- CSV/XLS/XLSX 预览最多收集 100 行/工作表；字段推断基于这些预览数据。全量同步不截断至 100 行。
- 此回归未覆盖实际 FTP/SMB 服务、生产网络、所有第三方 HTTP 调用及高并发资源预算；每请求限制不等于全局并发配额。
