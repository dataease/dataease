# 分布式模块预加载回归

在前端目录执行：

```sh
node --test tests/distributed-preload/regression.mjs
```

测试加载生产 `config/distributed.ts`，使用当前安装的 Vite 构建临时双页面工程，保留分布式配置的输出命名和外部扩展规则，将手工分块替换为本地的小型依赖；不启用压缩插件、不执行完整应用构建或 `build:flush`。构建结果保留在内存，临时源码执行后清理。

覆盖：

- 相对 base `./` 和子路径 `/dataease/` 的首页/嵌套页核心依赖 modulepreload 链接及资源路径。
- 懒加载路由的脚本依赖和 CSS 进入预加载映射，懒加载依赖不会提前进入首页 HTML。
- `extensions` 外部模块保留为动态导入，不生成 HTML 预加载链接。
- 恢复旧的空依赖回调时，HTML 脚本预加载和懒加载脚本依赖消失。当前 Vite 仍保留动态 CSS 依赖，不将其误判为 CSS 预加载一同被关闭。

该测试验证构建产物，不代表真实首屏性能测量；完整应用、部署路径、XPack 联调和 lib 嵌入构建未覆盖。本次仅修改 distributed 模式，base/lib 配置保持原样。
