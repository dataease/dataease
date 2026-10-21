# 日期原型移除后的表格回归

运行逻辑及组件编译验证：

```sh
node --test tests/grid-table-alignment/regression.mjs
```

测试提取六个生产组件的 timestampFormatDate 并执行，确认 Date.prototype.format 不存在时，正常时间及空值仍可处理，显式 formatDate 导入有效，脚本与模板均可编译。

浏览器验证：

```sh
./node_modules/.bin/vite --config tests/grid-table-alignment/vite.config.ts
```

打开 `http://127.0.0.1:7091/`，点击“检查对齐”，再“切换宽度”并再次检查。页面挂载真实 GridTable/TableBody 与 Element Plus 表格，创建时间列执行从生产用户管理页面提取的格式化函数。检查日期显示、空值占位，以及十列表头和首行表体的横坐标/宽度一致；六个生产格式化函数均应显示 PASS。

问题根因是遗留 `new Date(value)['format']()` 在单元格渲染时抛错，导致行内容不完整，并非固定列 CSS。这里的页面用本地虚构数据验证渲染，不连接线上后端；完整系统路由、真实分页和筛选仍需联调。
