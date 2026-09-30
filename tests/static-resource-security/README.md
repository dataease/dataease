# 静态资源安全回归（TAPD 1076986）

覆盖上传、模板 staticResource、模板快照的统一图片校验，以及静态资源 HTTP 防护。

允许 PNG/JPEG/GIF 和白名单内的静态 SVG；扩展名须与可解码的实际图片类型一致。保持原文件名契约，拒绝伪装 HTML，而不是改名后让前端引用失效。GIF 不重编码，保留动画字节。单文件最大 15 MB（与现有上传 UI 一致），位图首帧最大 4000 万像素以限制解码内存。

SVG 采用命名空间及元素允许列表：拒绝脚本、foreignObject、事件属性、处理指令、DTD、外部引用及动态修改属性的元素。支持基本形状、文本、渐变、遮罩及常用滤镜。含动画元素、嵌入外部图片、非 SVG 命名空间元数据等文件可能需要导出为静态 SVG 或 PNG。不能仅根据扩展名判断文件安全。

模板校验失败会向调用方报错，不再记录日志后假装导入成功；原文件已存在也先校验传入内容。原有合法资源不批量改写。历史非图片扩展名通过静态资源映射访问返回 404；历史 SVG 通过 CSP sandbox/script-src none 禁用脚本，正常应用页面不受该策略影响。

## 独立测试

Java 21 编译 core-backend 后执行 Regression.java。测试 classpath 包含 core-backend/target/classes、SDK classes、模块 Maven 依赖及 spring-test。可通过 `mvn -f core/core-backend/pom.xml dependency:build-classpath -Dmdep.outputFile=/tmp/static-resource-classpath` 获取依赖；用 javac 编译、java 运行 Regression。测试仅在临时目录写合成文件并在结束时删除。38 项断言覆盖类型、SVG 绕过、实际写入入口、失败未落盘及响应拦截器，不以被跳过的 Surefire 作为通过依据。

## 本地实际 HTTP/浏览器验收

- 本地合法 PNG/SVG 上传成功，魔数图片 .html 及带前缀 SVG script 上传失败。
- 经 8102 网关和 7070 前端代理直接访问模拟历史 SVG，CSP 生效，测试脚本未执行；历史 HTML 返回 404。
- 合法 PNG/SVG 请求返回 200 且 Content-Type 为 image/*。所有模拟历史文件和上传测试文件均清理。
- 尚未覆盖全部历史模板库、GIF 多帧动画视觉效果、Desktop 模式和所有浏览器。若部署自行绕过后端直接托管静态文件，需要在该托管层执行同等策略。
