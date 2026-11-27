按自然月汇总时，在 SELECT 和 GROUP BY 中使用同一个月份表达式。将示例中的日期列、金额列和表名替换为已核对的物理名称。

```sql
SELECT {{month_bucket}} AS month_bucket, SUM(amount) AS total
FROM table_name
GROUP BY {{month_bucket}}
```
