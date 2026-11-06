# 用户系统变量值检索回归

在主前端目录使用可用的 Playwright Test 执行：

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test node /path/to/playwright/cli.js test --config tests/user-variable-search/playwright.config.cjs
```

独立夹具仅监听 `127.0.0.1:7116`，加载真实用户管理 `UserForm.vue`、Vue 和 Element Plus；API、Store、国际化初始化、剪贴板及图标为合成边界，不访问真实用户或保存业务数据。

分别覆盖新增和编辑用户：2005 个变量候选按值忽略大小写检索、按已有值描述检索、无结果后继续检索、清空搜索、选择多个匹配值、保留编辑前已选项，以及最终新增/编辑请求中的 `variableValueIds`。检索不增加候选接口请求，不允许自由创建候选。

修改前两个用例均在搜索输入框只读的断言失败。测试未覆盖真实组织/角色权限、变量同步、数据库写入或生产构建。
