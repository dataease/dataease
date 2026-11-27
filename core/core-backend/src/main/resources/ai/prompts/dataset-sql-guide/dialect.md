# 方言参考

## calcite

DataEase 的 Calcite 使用 JAVA 词法规则。需要引用物理标识符时使用反引号。

## dm

需要引用物理标识符时使用双引号。

## quoted_column

按当前方言引用标识符，例如 {{quoted_column}}。

## plugin

使用简单物理标识符。采用插件扩展语法前，先确认该连接器的能力。

## unknown_month

当前插件尚未登记按月汇总的示例。请先确认插件支持的日期函数。
