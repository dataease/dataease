# 地理信息搜索回归

在前端目录执行，使用已有依赖，不安装或连接业务后端：

```sh
node --test tests/geometry-search/regression.cjs
```

测试从生产 `Geometry.vue` 提取真实搜索、点击详情函数与地名模板，使用实际 Vue 响应式、已安装的 `element-plus-secondary` TreeStore 和 Vue 服务端渲染。

覆盖 500/1000 节点反复搜索“北京”、大小写、无结果及清空；验证命中祖先保持可见、过滤不写节点且不触发全树深监听。验证真实模板的高亮和 HTML/实体转义，以及 20000 点 GeoJSON 不写回树节点、快速切换后旧响应不覆盖当前详情。

接口响应使用替身；性能回归以全树深监听次数为断言，不依赖不稳定的运行耗时阈值。

## 独立 Chrome 回归

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
node /path/to/playwright/cli.js test --config tests/geometry-search/playwright.config.cjs
```

挂载生产 `Geometry.vue`、真实 Vue 和 Element Plus，使用 7134 端口及约 1000 个区域节点。覆盖选中太原、显示长坐标 JSON 后搜索北京并回车，以及清空、无命中、反复搜索、大小写和 HTML/实体文本展示。

API、缓存、地图绘图库和子弹窗使用替身，不连接业务后端或写入业务数据。该测试不等于 QA 环境的完整端到端验收。
