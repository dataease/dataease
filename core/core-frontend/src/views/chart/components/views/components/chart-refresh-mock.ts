/**
 * 真实查询链路的开发测试工具，仅显式开启时修改响应副本，不写入数据源
 */
import { cloneDeep, isEqual } from 'lodash-es'

/**
 * 模拟所需的维度与指标字段，其他响应字段原样保留
 */
interface MockRow {
  field: string
  name?: string
  value: number
  category?: unknown
  [key: string]: unknown
}

/**
 * 真实查询响应中允许模拟的主数据位置
 */
interface MockResponse {
  data?: { data?: MockRow[] }
}

/**
 * 用于限制模拟范围及区分图表实例的最小配置
 */
interface MockView {
  id: unknown
  type: string
  yAxis?: unknown[]
  xAxisExt?: unknown[]
  extStack?: unknown[]
}

/*
 * 开发环境查询响应模拟，每个图表实例独立计数，不修改原始响应
 * mixed 模式每次追加一个维度，并只修改一个原有维度的指标值
 * 默认关闭，sessionStorage 可设为 mixed、append 或 change 开启测试
 */
export function createChartRefreshMock() {
  let previous: { id: unknown; mode: string; rows: MockRow[] } | undefined
  let round = 0
  let simulatedRows: MockRow[] | undefined

  /**
   * 仅为定时刷新生成测试数据，手动查询或后端数据变化时重置模拟进度
   * 返回独立响应副本，避免图表转换污染后续模拟结果
   */
  return <T extends MockResponse>(response: T, view: MockView, autoRefresh: boolean): T => {
    let mode = 'off'
    try {
      mode = sessionStorage.getItem('de-chart-refresh-mock') || 'off'
    } catch {
      // 无法读取测试开关时保持关闭，避免意外修改展示数据
    }
    const rows = response.data?.data
    if (
      !['append', 'change', 'mixed'].includes(mode) ||
      !['bar', 'line'].includes(view.type) ||
      view.yAxis?.length !== 1 ||
      view.xAxisExt?.length ||
      view.extStack?.length ||
      !rows?.length ||
      rows.some(row => typeof row.field !== 'string' || !Number.isFinite(row.value))
    ) {
      previous = undefined
      round = 0
      simulatedRows = undefined
      return response
    }
    if (
      !autoRefresh ||
      previous?.id !== view.id ||
      previous?.mode !== mode ||
      !isEqual(previous?.rows, rows)
    ) {
      round = 0
      simulatedRows = cloneDeep(rows)
    }
    previous = { id: view.id, mode, rows: cloneDeep(rows) }
    if (!autoRefresh) return response

    round++
    const result = cloneDeep(response)
    const nextRows = cloneDeep(simulatedRows ?? rows)
    result.data.data = nextRows
    if ((mode === 'append' || mode === 'mixed') && round <= 20) {
      // 保留此前追加的维度，最多追加 20 个，避免长期刷新无限增长
      const row = cloneDeep(rows[0])
      let field = `模拟新增维度-${round}`
      while (nextRows.some(item => item.field === field)) field = `_${field}`
      row.field = field
      row.name = field
      row.value += Math.max(Math.abs(row.value) * 0.15, 1) * round
      nextRows.push(row)
    }
    // 达到追加上限后继续改变数值，保证每次定时刷新仍能观察更新效果
    if (mode === 'change' || mode === 'mixed' || round > 20) {
      // 保留上一轮模拟结果，避免未选中的维度因恢复后端原值而再次变化
      const index = (round - 1) % rows.length
      const delta = Math.max(Math.abs(rows[index].value) * 0.2, 1)
      const direction = Math.floor((round - 1) / rows.length) % 2 === 0 ? 1 : -1
      nextRows[index].value += direction * delta
    }
    simulatedRows = cloneDeep(nextRows)
    return result
  }
}
