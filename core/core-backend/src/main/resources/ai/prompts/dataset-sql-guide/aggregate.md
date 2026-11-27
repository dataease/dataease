作者要求按维度汇总时，按该维度分组，在每组内计算指标。以下示例返回每个维度值对应的总额。

```sql
SELECT dimension, SUM(amount) AS total
FROM table_name
GROUP BY dimension
```
