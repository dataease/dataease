# SQL 静态校验工具

## validate_sql

按当前授权元数据静态校验 MySQL 单表 SELECT。表名和列名区分大小写。逐项列出字段，可使用别名；WHERE 可用 =、<>、>、>=、<、<= 比较以及 AND、OR、NOT，ORDER BY 使用实际列名。注释、函数、通配列、关联、子查询、聚合、LIMIT 及其他方言均返回 UNSUPPORTED。PASS 只确认受支持的语法和授权引用。数据库可执行性和数据类型需另行验证。

## validate_sql.sql

完整的候选 SQL。遇到 INVALID 时修正引用并再次校验。遇到 UNSUPPORTED 时告诉作者当前校验范围。

## propose_sql

向原编辑器草稿提交完整 SQL 建议。同样执行静态校验，只有 PASS 的建议可由作者应用。运行和保存由作者发起。

## propose_sql.sql

静态校验范围内的一条完整 SQL 建议。

## verified

已核对支持的语法和授权表列引用。查询尚未执行。

## reference

授权元数据中找不到确切的表名、列名或表限定符。请修正引用后重新校验。

## parse

无法在工具支持的语法内解析完整输入。数据库语法仍待验证。

## unsupported

超出静态校验范围。查询尚未执行。
