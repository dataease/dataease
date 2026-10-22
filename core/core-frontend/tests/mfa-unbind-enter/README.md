# MFA 取消绑定弹窗回车回归

复用仓库 `tests/browser` 的 Playwright Test 和本机 Chrome。在该工具的依赖已安装后，从 `core/core-frontend` 执行：

```sh
NODE_PATH=../../tests/browser/node_modules ../../tests/browser/node_modules/.bin/playwright test --config tests/mfa-unbind-enter/playwright.config.cjs
```

夹具挂载真实 `UserForm.vue` 和 Element Plus，仅替换用户 API、Store、国际化与未使用的绑定页面；全部数据和取消绑定响应均为合成数据，不连接真实后端或修改真实 MFA。临时服务仅监听 `127.0.0.1:7098`，测试后自动停止，失败截图写入系统临时目录。

覆盖空值、格式错误、六位验证码输入后按回车时 URL 不变、不发生页面导航、弹窗仍在且不发送取消绑定请求；确认按钮仍校验验证码并发送一次请求，取消后可重新打开。

修复前六位验证码回车用例复现 `/#/user-center/index?tab=1` 变为 `/?#/user-center/index?tab=1`；修复后五项均应通过。此验证不覆盖真实 TOTP、全局 MFA 策略和完整登录路由。
