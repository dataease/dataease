# 方言参考

## mysql.identifiers

需要引用表名或列名时，使用反引号，例如 `` `column_name` ``。

## mysql.month

DATE_FORMAT(date_column, '%Y-%m')

## calcite.identifiers

DataEase 的 Calcite 使用 JAVA 词法规则。需要引用表名或列名时，使用反引号。

## calcite.month

FLOOR(date_column TO MONTH)

## unsupported

向作者说明当前执行方言尚未适配 SQL 生成。可以解释已有查询，或协助核对可访问的表和字段。
