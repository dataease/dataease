按已确认的表关联连接两端。复合键的每一对字段都要用 AND 连接。需要保留起始表的所有行时使用 LEFT JOIN。

图表使用明细时，按所需明细粒度返回维度和度量。作者要求分组汇总时，先核对关联后每行代表什么。下面的明细表按租户和事实 ID 先计数，再关联到事实表，使每笔事实的金额只计一次。

```sql
SELECT f.group_key, SUM(f.amount) AS total, SUM(COALESCE(x.line_count, 0)) AS line_count
FROM fact_table f
LEFT JOIN (
    SELECT tenant_key, fact_id, COUNT(*) AS line_count
    FROM line_table
    GROUP BY tenant_key, fact_id
) x ON f.tenant_key = x.tenant_key AND f.id = x.fact_id
GROUP BY f.group_key
```

如果明细只用于判断是否存在匹配行，使用 EXISTS。不同事实可以有相同金额，用事实键区分它们。多对多关联需要先确认统计粒度或度量的分摊方式。
