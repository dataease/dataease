# 字体设置弹窗回归

运行本地前端开发服务后执行 `python3 tests/font-settings/browser-test.py`（需 Python Playwright 和 Chromium）。可通过 `FONT_SETTINGS_TEST_URL` 指定测试页。

挂载真实 FontSettings 组件、i18n 和请求客户端，仅拦截字体设置接口返回隔离数据；不修改业务数据库。覆盖显示、数值关系约束、保存、重新打开、低配额提示和取消。后端持久化与权限验证见 FontSettingsRegression，本测试不替代完整登录环境联调。
