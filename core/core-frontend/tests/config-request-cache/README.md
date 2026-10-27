# 路由配置请求缓存

从前端目录执行：

```sh
node --test tests/config-request-cache/regression.cjs tests/route-config-parallel/regression.cjs
```

仅字体 Store 的 `setFontList()` 与路由守卫的默认设置读取复用当前页面内的成功响应和进行中的请求。原始 `font.list()`、`getDefaultSettings()` 保持实时读取契约。页面刷新重新初始化缓存，不在 localStorage/sessionStorage 保存响应或新增凭据副本。

缓存按当前账号、组织、管理员代理、语言、运行模式及嵌入/分享身份隔离。失败不缓存；退出和清理登录状态时统一清理；失效或身份变化后的迟到响应转而等待当前请求，避免旧值回写。

字体新增、编辑、删除及上传成功后失效；字体管理页及上传完成主动调用 `setFontList(true)`。基本设置保存成功后调用 `invalidateDefaultSettings()`，下一次路由初始化重新获取并应用。需要主动读取时可调用 `getCachedDefaultSettings(true)` 或 `setFontList(true)`。其他浏览器/会话修改配置后，可刷新页面获取最新配置。

回归使用合成数据，不修改真实字体、基本设置或用户身份。实页验证：刷新登录后的工作台，依次切换仪表板、数据集、工作台；Performance Resource Timing 中 `/typeface/listFont` 与 `/sysParameter/defaultSettings` 的累计计数应保持各一次。不要输出请求头或登录缓存。
