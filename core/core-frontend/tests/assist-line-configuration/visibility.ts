import { createAssistLineVisibility } from '@/views/chart/components/js/panel/types/impl/g2-assist-line'
import { getMixSeriesKey } from '@/views/chart/components/js/panel/charts/g2/mix/mix-series'

export const checkVisibility = () => {
  let checks = 0
  const expect = (actual: boolean, expected: boolean, name: string) => {
    if (actual !== expected) throw new Error(name)
    checks++
  }
  const field = { id: 'salary', name: '薪资' }
  const other = { id: 'commission', name: '提成' }
  const line = {
    name: '阈值',
    field: '0',
    boundFieldId: field.id,
    followField: true,
    yAxisType: 'left',
    fieldId: field.id,
    color: '#f00',
    lineType: 'solid',
    summary: 'avg'
  }
  const group = {
    type: 'bar-group',
    yAxis: [field],
    yAxisExt: [],
    data: { data: [{ category: '公司 A' }, { category: '公司 B' }] }
  }
  const state = createAssistLineVisibility(group as Chart)
  const unbound = { ...line, boundFieldId: '', followField: false } as AssistLine
  state.select(['公司 A'])
  expect(state.visible(unbound), true, '单指标整体参考线保留')
  expect(state.visible(line as AssistLine), true, '历史绑定在部分分组隐藏后保留')
  state.select([])
  expect(state.visible(unbound), true, '整体参考线不跟随图例')
  expect(state.visible(line as AssistLine), false, '历史绑定在全部分组隐藏后隐藏')
  expect(state.visible({ ...line, boundFieldId: 'removed' } as AssistLine), false, '失效绑定不转移')

  for (const type of ['chart-mix', 'chart-mix-group', 'chart-mix-stack', 'chart-mix-dual-line']) {
    // 同一字段、同名指标同时放在左右轴，仍须按轴隔离。
    const left = { ...line } as AssistLine
    const right = { ...line, yAxisType: 'right' } as AssistLine
    const dynamic = { ...right, name: '均值', field: '1' } as AssistLine
    const chart = {
      type,
      yAxis: [field],
      yAxisExt: [field],
      senior: { assistLineCfg: { assistLine: [left, right, dynamic] } },
      data: {
        left: {
          data: [
            { category: field.name, quotaList: [field] },
            { category: '公司 B', quotaList: [field] }
          ]
        },
        right: {
          data: [
            { category: field.name, quotaList: [field] },
            { category: '公司 B', quotaList: [field] }
          ]
        }
      }
    }
    const visibility = createAssistLineVisibility(chart as Chart)
    visibility.select([getMixSeriesKey('left', field.name)])
    expect(visibility.visible(left), true, `${type}: 左轴保留`)
    expect(visibility.visible(right), false, `${type}: 右轴隐藏`)
    expect(
      visibility.visible({ ...dynamic, followField: undefined }),
      false,
      `${type}: 动态线读取持久化配置`
    )
    visibility.select([getMixSeriesKey('right', field.name)])
    expect(visibility.visible(left), false, `${type}: 左轴隐藏`)
    expect(visibility.visible(right), true, `${type}: 右轴保留`)
    expect(visibility.visible({ ...left, followField: false }), true, `${type}: 关闭跟随`)
    visibility.select()
    expect(visibility.visible(left), true, `${type}: 图例重置`)
    expect(visibility.visible(right), true, `${type}: 图例重置右轴`)
    if (type !== 'chart-mix') {
      visibility.select([getMixSeriesKey('right', '公司 B')])
      expect(visibility.visible(right), true, `${type}: 只隐藏部分分组保留辅助线`)
      expect(visibility.visible(left), false, `${type}: 隐藏另一侧全部分组`)
    }
    const replaced = createAssistLineVisibility({ ...chart, yAxis: [other] } as Chart)
    expect(replaced.visible(left), false, `${type}: 替换指标不沿用旧阈值`)
    expect(replaced.visible(right), true, `${type}: 替换左轴不影响右轴`)
  }
  return checks
}
