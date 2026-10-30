# 国际化热更新回归

在 `core/core-frontend` 目录使用已安装的 Playwright Test 和独立 CLI，不连接后端，不更改业务数据，不修改工作区源码。

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test node /path/to/playwright/cli.js test --config tests/i18n-hmr/playwright.config.cjs
```

测试复制生产国际化入口、实例模块、语言加载器、翻译 hook 到临时目录，以真实 Vue、Vue I18n 和 Vite（127.0.0.1:7095）运行。仅缓存、语言列表及语言包内容使用本地夹具。

覆盖初始化前取得的翻译函数在初始化后恢复翻译、命名空间和插值；模块带新时间戳重复求值仍共享已安装实例；Vue 模板真实 HMR 后翻译正常且保留未保存输入；简中/英文/繁中切换保持 hook 与全局翻译一致。

设置 `I18N_HMR_VARIANT=before` 并通过 `I18N_BEFORE_REF` 指定可访问的修复前提交，读取该提交的原始入口和 hook，用同一测试复现问题。不要使用已包含本次修复的提交：

```sh
I18N_HMR_VARIANT=before I18N_BEFORE_REF=<修复前提交> PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test node /path/to/playwright/cli.js test --config tests/i18n-hmr/playwright.config.cjs --grep '初始化前|国际化入口'
```

临时文件和服务在结束时清理；失败截图和 trace 位于 `/tmp/dataease-i18n-hmr-results`。未覆盖真实自定义语言包服务及所有多页面入口。
