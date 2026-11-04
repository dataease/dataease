# 搜索名称高亮回归

在主仓库根目录、浏览器工具依赖就绪后运行：

```sh
npm --prefix tests/browser test -- --config=playwright.search-name-highlight.config.cjs
```

夹具在临时目录启动独立 Vite 服务，使用前端已安装的 Vue、Lodash、DOMPurify 和项目实际注册的 `vue-dompurify-html`。提取真实 `setColorName`、系统变量过滤逻辑及名称渲染模板，候选名称均为合成数据，不连接业务后端，也不创建或修改系统变量。

覆盖名称中包含字面 HTML 标签、字符实体、重复转义实体和危险标签时，搜索前、搜索后、清空搜索的可见文本一致；只高亮匹配片段，不创建原名中的 HTML 元素。另验证公共函数的自定义字段、忽略大小写、无匹配和清空语义，以及系统变量列表大小写不同的搜索命中、未命中隐藏与清空恢复。

环境没有单独安装 `@playwright/test` 时，可通过 `PLAYWRIGHT_TEST_MODULE` 指向已有的兼容 Playwright Test 模块，并使用该安装提供的 CLI 执行此配置；不需要为本回归修改业务依赖。

该回归验证高亮函数及真实过滤/渲染片段，不替代完整系统页面和后端查询回归。
