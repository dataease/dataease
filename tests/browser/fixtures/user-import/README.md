# 用户导入空组织提示回归

从仓库根目录执行：

```sh
npm --prefix tests/browser test -- --config=playwright.user-import.config.mjs
```

夹具仅监听 `127.0.0.1:4198`，加载真实用户导入组件、Element Plus 和三种语言资源。替换图标及 API 边界；上传内容为合成数据，API 只记录文件名，不连接业务后端、不创建用户。

覆盖简体中文、繁体中文和英文，在 1440px、640px 视口检查空组织规则提示、未选文件时禁止导入、选择文件后的提交交互。该测试不证明 Excel 内容解析、数据库事务或实际鉴权通过。

CLI 只读检查可先启动 `node tests/browser/fixtures/user-import/server.mjs`，再使用独立会话 `npm --prefix tests/browser run cli -- -s=tapd-1078405 open http://127.0.0.1:4198`。复用手动启动的本夹具运行 Test 时设置 `USER_IMPORT_SERVER=1`。完成后仅关闭本任务的浏览器和夹具进程。
