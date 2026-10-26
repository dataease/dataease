# 定时报告明细前端回归

报告前端实现统一位于 `core/core-frontend/src/views/menu/system/report`。`LogDetails.vue` 异步加载同目录的 `ReportDeliveryDetails.vue`，失败时回退 `LogDetailsLegacy.vue`。构建不依赖 XPack 前端目录或报告虚拟模块。

以下命令从仓库根目录执行，复用主前端依赖及根目录 `tests/browser` 的 Playwright，不新增依赖：

```sh
node core/core-frontend/node_modules/vite/bin/vite.js --config core/core-frontend/tests/report-delivery/vite.config.ts
npm --prefix tests/browser run cli -- -s=v3-report-delivery open http://127.0.0.1:7096
node tests/browser/node_modules/@playwright/test/cli.js test --config core/core-frontend/tests/report-delivery/playwright.config.mjs
node core/core-frontend/node_modules/vue-tsc/bin/vue-tsc.js -p core/core-frontend/tests/report-delivery/tsconfig.json --noEmit
node core/core-frontend/node_modules/vite/bin/vite.js build --config core/core-frontend/tests/report-delivery/vite.config.ts --outDir /tmp/v3-report-ui-build
npm --prefix tests/browser run cli -- -s=v3-report-delivery close
```

夹具加载真实报告组件，替换 API 和国际化边界，使用合成接收人数据，不连接业务数据库或发送消息。11 个 Playwright 场景覆盖首次结果与补发记录、历史日志、重复投递风险确认、副本清理、筛选恢复、全部成功、未发送目标、无有效接收人、异步组件加载失败回退和窄屏布局。

定向类型检查与隔离 UI 生产构建不代表整个产品构建或后端权限、真实渠道验收。运行输出保存在 `/tmp/v3-report-*`，不纳入仓库。

目录迁移后验证：Playwright CLI 抽屉与历史禁用补发检查通过，Playwright Test 11 项通过，定向类型检查和隔离生产构建通过。

新增 SMTP 连接失败回归：显示连接发送服务阶段，两条确认失败目标可通过批量按钮提交；结果未确认项仍需要单独确认风险。
