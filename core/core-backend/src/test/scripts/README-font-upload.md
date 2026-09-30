# 字体上传资源限制回归（TAPD 1077001）

仅创建隔离临时目录和 loopback 临时 HTTP 服务，不访问业务数据库或现有字体目录。
使用真实 TTF 文件作为正常上传对照，不将系统字体提交到仓库。

配置默认值：

```yaml
dataease:
  font:
    max-upload-size: 20MB
    max-storage-size: 512MB
```

两项必须大于零。存储配额统计字体目录已有普通文件，包括上传后尚未保存字体信息的文件和历史残留；不会删除历史文件或现有字库。默认每个应用实例只处理一个字体上传，共享目录还通过文件锁互斥。文件系统须支持文件锁和同目录原子移动，失败时拒绝保存。

字体文件受限流式写入临时文件，通过解析后原子移动为正式文件。读取、写入、解析及移动失败均清理临时文件。已有全局 multipart 限额保持不变：字体服务校验发生于 multipart 解析之后，不宣称该变更限制了整个 HTTP 入口的临时磁盘、带宽或所有上传端点并发。上传后的进程强制退出不保证 finally 执行；遗留文件仍计入配额。上传接口原有身份校验保持不变；新增设置接口复用已有菜单查询契约，仅允许具有字体管理菜单权限的登录用户，拒绝未登录、无菜单权限及分享身份。通用系统设置保存接口禁止写入字体限制键。

将更新后的后端类、SDK 和运行依赖设置为 `FONT_TEST_CLASSPATH`，把修改的类放在已打包 jar 之前。Surefire 默认跳过测试，不能以 `mvn test` 成功作为执行证据。

```sh
FONT_TEST_CLASSES=$(mktemp -d)
javac -proc:none -cp "$FONT_TEST_CLASSPATH" -d "$FONT_TEST_CLASSES" \
  core/core-backend/src/test/java/io/dataease/font/manage/FontUploadRegression.java
java -Xmx128m -Djava.awt.headless=true \
  -cp "$FONT_TEST_CLASSES:$FONT_TEST_CLASSPATH" \
  io.dataease.font.manage.FontUploadRegression /path/to/test-font.ttf
```

覆盖：超大声明及空文件拒绝、禁用 getBytes、反复无效字体无残留、输入异常清理、虚报大小下的实际字节限制、真实字体内容一致性、非英语数字区域设置、已有及流式存储配额、并发和多个服务实例互斥、HTTP multipart 正常/无效/21MB 流式上传。

此 HTTP 测试使用真实 Tomcat multipart 解析和生产 FontManage，未启用产品登录/鉴权、业务数据库或完整应用启动，不代表这些层面已完成回归。

## 动态设置

字体管理页的“字体设置”保存到现有 `core_sys_setting` 表的 `font.upload.limits` JSON 行，无需新增表或迁移。每次上传读取已提交的设置，不使用进程缓存。上面的配置文件值仅作为从未保存设置时的默认值。降低配额不会删除已有文件；已经开始的上传使用开始时的配置。

单文件大小和总配额以 MB 正整数填写，单文件不能超过总配额或服务器 multipart 上限（请求预留 64KB），总配额最大 1TiB。网关限制仍独立生效，界面调整不能绕过网关限额。

`FontSettingsRegression` 使用临时 H2 数据库和字体目录，验证真实 JPA 保存、重新建立上下文后读取、第二个服务实例读取、权限拒绝、通用接口绕过拒绝，以及修改限额后真实 FontManage 的拒绝原因变化。需运行依赖包含 H2；此测试不替代所有产品支持数据库的并发兼容性验证。

```sh
javac -proc:none -cp "$FONT_TEST_CLASSPATH" -d "$FONT_TEST_CLASSES" \
  core/core-backend/src/test/java/io/dataease/font/manage/FontSettingsRegression.java
java -Xmx128m -Djava.awt.headless=true \
  -cp "$FONT_TEST_CLASSES:$FONT_TEST_CLASSPATH" \
  io.dataease.font.manage.FontSettingsRegression
```
