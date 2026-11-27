# 编辑器工具

## find_historical_step

按编号或线索查找本会话的历史步骤。只读取作者所问的原有证据。

## find_historical_step.action

原工具名称。按确切步骤编号查询且动作未知时填 `*`。

## find_historical_step.subject

确切表名、建议的字段名或公式表达式。未指定时填 `*`。

## find_historical_step.fields

用英文逗号分隔确切物理列名，最多 32 个。查时间字段用 temporal，查已记录的列数用 #count，查列名用 #names，只查状态用 `*`。列名含逗号或与保留词重名时，传入 JSON 字符串数组的序列化文本。

## find_historical_step.selection

最近一次回答的第 N 步用 last.N，第 A 次回答的第 N 步用 A.N。按其他线索查找时用 any；取最近一次匹配用 latest；按数据源查找用 `source:<确切数据源名>`；指定回答时填回答编号。

## revalidate_current_formula

作者明确要求重新校验当前公式时，预览发问时固定的原公式。

## propose_calc_field

提交一个明细或聚合计算字段建议。在当前数据集的私有副本中预览该表达式。

## propose_calc_field.expression

计算字段表达式。字段和计算参数均使用已提供的完整 `[引用名]`。

## propose_calc_field.name

建议的字段名称。

## propose_calc_field.groupType

维度填 d，度量填 q。

## propose_calc_field.deType

文本填 0，日期填 1，整数填 2，小数填 3，地理填 5，URL 填 7。
