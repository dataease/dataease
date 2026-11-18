# 查询请求实例隔离回归

在 `core/core-frontend` 执行，使用已安装依赖：

```sh
node --test tests/scoped-query/regression.cjs tests/query-options-race/regression.cjs tests/chart-viewport-load/regression.cjs
```

实现范围：

- CanvasCore 提供随画布实例销毁的请求作用域；独立图表、弹窗没有祖先作用域时使用局部作用域。
- 每个组件按用途创建查询控制器，代次在新查询/筛选失效时递增，成功、失败、取消分别释放自身的请求登记；网络结束后仍保留绘制阶段的代次校验。
- G2/S2、指标卡、富文本和图片组显式纳管图表读请求。S2 分页与筛选共用同一组件的查询代次。
- Select 沿用现有候选值取消、默认值与级联逻辑，改用作用域管理生命周期。
- 查询配置不再调用全局取消。新请求不进入旧 cancelMap；旧表只登记仍有旧调用方的明确查询接口，保存等写操作不登记。
- loading 的路径在请求开始时捕获，避免切换路由后减少另一个页面的计数。

测试使用真实 Axios 实例、请求封装和拦截器，模拟适配器返回，不连接产品后端。Vue 生命周期测试使用真实 Vue renderer；图表用例从真实 SFC 抽取查询方法，使用真实查询管理器，替换取数/绘制边界以控制响应顺序。它们验证数据、缓存写入、钻取通知和完成回调，不代替真实图表浏览器回归。

覆盖同资源双实例、同页面不同用途、跨页面隔离、连续十次筛选、旧成功/失败响应、筛选防抖等待期失效、S2 分页与异步绘制完成交叉、旧 loading 回调、写请求并发、签名和语言透传、成功/失败/取消句柄清理、页面卸载与候选值原有回归。同步准备失败另覆盖五类图表的序列化异常、S2 缺失配置、图片组非法 JSON：登记立即释放，其他用途请求不受影响，原异常继续抛出，修正配置后可重试。

连续十次筛选的用例已对照修改前 G2 源码：旧代码产生十次详情缓存写入，本批代码只提交最新一次。对照时只替换测试中抽取的组件源码，不切换或修改工作区：

```sh
# 先在临时目录中按原相对路径放置基线 ChartComponentG2Plot.vue。
SCOPED_QUERY_BASELINE_DIR=/tmp/dataease-scoped-query-baseline \
  node --test --test-name-pattern 'ten rapid' tests/scoped-query/regression.cjs
```

补充迁移和隔离：

- 数据集节点/表详情及整体预览、数据源 API 调试、资源树和画布元数据加载按实例管理请求；快速切换和关闭后，旧响应不能覆盖当前状态。
- 原生图表的详情、请求参数、S2 实例和导出数据使用实例缓存；详情弹窗接收打开时的独立快照。原生渲染容器 ID 同时区分实例。
- 查询通知携带画布作用域，嵌套页签继承主画布的查询上下文；Select/Tree 的防抖等待期立即使旧查询失效。
- 主编排链的关联和跳转配置请求共用当前画布代次，子请求分别登记、释放。

浏览器回归：

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
  node /path/to/playwright/cli.js test --config tests/scoped-query/playwright.config.cjs
```

使用临时 Vite 服务（7127 端口），挂载真实 Vue 生命周期和实例缓存，抽取真实 G2/S2 查询、详情导出方法；接口、绘制和下载边界使用可控桩。覆盖同资源双实例、快速筛选、详情快照和导出参数、保存请求并发、S2 分页与筛选、关闭重开。它不连接业务后端，也不代表真实图表引擎已自动回归。

人工产品验证使用本地已登录开发环境，实际仪表板 G2/S2 绘制、S2 下一页和汇总表详情弹窗；未发起真实导出或保存。

边界：

- 按用户要求，插件内部暂不改造，保留既有调用和缓存兼容路径；不能保证插件内部请求和状态已经隔离。
- 未完整实测移动端、分享、嵌入和全部图表类型，未执行生产构建。
- 既有 `query-cascade-default` 中 “multiple and automatic conditions also clear their disabled defaults” 用例缺少 `validateDynamicDefault` 桩；使用 HEAD 源码对照也失败，属于已有测试基线问题。
