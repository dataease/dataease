# 查询候选加载器回归

第二批把 Select 与 Tree 的请求生命周期收敛到 `candidate-loader.ts`。继续使用已有请求作用域，同一组件中输入相同的等待中/进行中任务只执行一次；完成后再次触发仍重新请求，不缓存结果，不跨组件或身份共享。切换输入立即失效旧工作，延迟任务和网络结束均核对当前代次，卸载时取消。

默认值、字段显示映射、自定义排序、选项过滤、级联传播及查询按钮处理继续由原组件负责，未修改模板和样式。

在前端目录执行：

```sh
node --test tests/query-candidate-loader/regression.cjs tests/query-options-race/regression.cjs tests/query-cascade-runtime/regression.cjs tests/scoped-query/regression.cjs
```

加载器用例使用真实生产模块与 QueryScope，覆盖相同输入复用、输入快照、延迟期间取消、乱序响应、同步/异步失败、失败后重试、不同实例隔离及卸载清理。已有组件用例加载真实处理函数和加载器，验证实际分发路径、loading 和级联行为。

完整弹窗浏览器用例见 `../query-config-bindings/README.md`，其中新增真实 Select/Tree 的重复级联触发、字段切换乱序响应、手动来源切换和关闭取消。接口为合成数据，可控制响应顺序，未覆盖已登录产品的业务后端保存、分享或移动端入口。
