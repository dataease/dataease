# TAPD 1077734 Ticket 刷新回归

约定：刷新只轮换凭据，保留数据库中的 UUID、参数和有效期；首次访问时间重置。草稿只在点击保存时提交。

## 浏览器组件回归

使用真实 `TicketEdit.vue` 和 Element Plus；仅替换翻译及 HTTP 适配器。所有 Ticket 响应由 Playwright 拦截，不连接业务数据库。这不代替登录、网关、列表和数据库事务的整链路验证。

依赖使用已安装的前端与 `tests/browser` 依赖，不单独安装另一份 Vue。

```sh
node tests/browser/fixtures/ticket/server.mjs
npm --prefix tests/browser run cli -- -s=tapd-1077734 open http://127.0.0.1:4194
npm --prefix tests/browser test -- --config=ticket.config.mjs
```

CLI 复现时先拦截 `**/ticket/saveTicket`，记录 `route.request().postDataJSON()`，模拟缺少 UUID 的失败响应，再打开“编辑已有 Ticket”并点击刷新。修复前只有 ticket/generateNew；修复后包含 uuid，且不携带编辑草稿。CLI 结束时仅关闭自己的会话。

## 真实应用回归

按 `.vscode/launch.json` 启动前后端，保留原 APISIX 代理。使用专用空白测试仪表板并开启分享，不能传入业务资源 ID。通过进程环境提供 `TICKET_TEST_RESOURCE_ID`、`TICKET_TEST_USER`、`TICKET_TEST_PASSWORD`，不将凭据写入测试文件或保存浏览器登录状态。

```sh
npm --prefix tests/browser test -- --config=ticket-live.config.mjs
```

真实用例会新增测试 Ticket，验证刷新请求、分页返回的持久化值、匿名访问旧 Ticket 拒绝/新 Ticket 有效、取消重开、显式保存、无限期和列表刷新。等待临时 Ticket 非空且唯一定位目标行，避免异步生成期间选中其他记录。不会自动删除测试数据或关闭分享；运行结束后关闭专用测试仪表板的分享。关闭 trace 避免记录登录凭据，失败截图仍须按敏感测试产物处理。

## 后端独立回归

`Regression.java` 调用真实 `ShareTicketManage`，使用受控仓库和权限检查替身，不启动 Spring 或数据库。覆盖参数/有效期保留（含 0、null）、旧 Ticket 与绑定旧 ID 的 Token 失效、首次访问重置、UUID 不匹配、缺失 Ticket、权限拒绝以及普通保存。

先取得当前模块运行 classpath（含已编译 SDK/backend classes 和 Maven 依赖），保存为临时文件。已有构建产物必须对应当前 SDK 契约；不使用被 Surefire 跳过的测试作为结果。

```sh
ticket_test_cp="$(cat /tmp/de-share-ticket-classpath)"
mkdir -p /tmp/tapd-1077734-classes
javac -proc:none -cp "$ticket_test_cp" -d /tmp/tapd-1077734-classes \
  core/core-backend/src/main/java/io/dataease/share/manage/ShareTicketManage.java \
  tests/ticket-refresh/Regression.java
java -cp "/tmp/tapd-1077734-classes:$ticket_test_cp" io.dataease.share.manage.Regression
```

预期 38 项断言通过。该测试不证明 Spring 事务回滚、真实权限服务或并发刷新正确；这些需要隔离业务环境补充验证。
