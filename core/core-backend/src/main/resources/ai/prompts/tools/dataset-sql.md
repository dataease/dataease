# 数据集 SQL 工具

## read_sql_guide

读取当前节点的执行方言、通用规则和指定主题的写法。方言由当前数据源和查询模式决定。

## read_sql_guide.topic

普通取列用 projection，按月汇总用 monthly，其他聚合用 aggregate，同源关联用 join，筛选参数或系统变量用 parameters。

## search_registered_tables

按物理名、备注或已确认的业务说明，在当前数据源已登记的表中寻找候选。选中候选后用 get_table_fields 核对字段。

## search_registered_tables.terms

本轮问题涉及的确切表名和有区分度的业务词，用英文逗号分隔。

## search_physical_tables

查过已登记的表仍找不到业务对象时，按业务词和可能的物理名检索物理表目录。再核对候选表的字段以确认用途。此步只读表名与备注。

## search_physical_tables.terms

用英文逗号分隔业务词或物理名，最多 12 项，合计最多 512 个字符。

## get_table_fields

读取指定表的实时字段。业务口径依据已确认的业务名称、说明或明确的取值与含义。物理备注和别名可用来挑选候选字段。

## get_table_fields.table

问题、已有 SQL 或搜索结果中出现的确切物理表名。

## get_named_table_fields

刷新已知物理列的元数据。需要根据拼写或业务含义辨认字段时，使用 get_table_fields 查看整表结构。

## get_named_table_fields.table

当前数据源中的确切物理表名。

## get_named_table_fields.fields

用英文逗号分隔确切物理列名，最多 32 个。

## read_relations

读取同一数据源中两张物理表已确认的关联，方向从第一张表指向第二张表。

## read_relations.fromTable

起始表的确切物理名，通常是需要保留记录的一侧。

## read_relations.toTable

另一张表的确切物理名。

## propose_sql

提交一条完整的只读查询作为编辑器建议，由作者决定是否应用、运行或保存。

## propose_sql.sql

要提交给作者的完整 SQL。

## propose_sql.invalid

请提交一条以 SELECT 或 WITH 开头的完整只读查询。
