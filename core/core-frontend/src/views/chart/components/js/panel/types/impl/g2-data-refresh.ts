/**
 * 定时刷新扩展入口：登记图表与样式能力，并为数据元素维护稳定身份
 * 未注册或不兼容的组合沿用原有完整绘制流程
 */
import type { G2Spec } from '@antv/g2'
import type { ChildSpec, ViewSpec } from '../../charts/g2/bar/barUtil'
import { cloneDeep, isEmpty } from 'lodash-es'

const ITEM_KEY = '__deRefreshItemKey'

/**
 * 参与身份匹配的维度、系列和指标信息
 */
interface RefreshRow {
  field?: unknown
  category?: unknown
  value: number
  quotaList?: { id: unknown }[]
  [key: string]: unknown
}

/**
 * 保存上一轮元素身份，供下一轮匹配未变化的数据
 */
export interface RefreshDataState {
  items: { row: RefreshRow; id: number }[]
  nextId: number
}

/**
 * 图表声明主图元、数值轴和依赖数据的样式生成方法
 */
export interface ChartRefreshAdapter {
  valueChannel: 'x' | 'y'
  marks: { type: string; key: 'item' | 'series' }[]
  /**
   * 根据本轮查询结果生成独立辅助线图元，不复用主图元数据
   */
  assistLines: (chart: ChartObj, options: ViewSpec) => ChildSpec[]
  /**
   * 重建条件样式的数据转换，未提供时启用条件样式的组合回退完整绘制
   */
  conditions?: (chart: ChartObj, options: ViewSpec) => ViewSpec
}

/**
 * 样式声明启用条件、兼容能力及附加图元更新方式
 */
export interface StyleRefreshAdapter {
  markId?: string
  /**
   * 判断当前图表是否启用了该样式
   */
  enabled: (chart: ChartObj) => boolean
  /**
   * 判断图表适配器能否保留该样式的更新语义
   */
  supports: (adapter: ChartRefreshAdapter, chart: ChartObj) => boolean
  /**
   * 在绘制前更新样式或附加图元，返回包含本轮变化的配置
   */
  update?: (options: ViewSpec, chart: ChartObj, adapter: ChartRefreshAdapter) => ViewSpec
}

const chartRefreshAdapters = new Map<string, ChartRefreshAdapter>()
const refreshInstances = new WeakSet<object>()

/**
 * 仅在首次 Spec 校验通过后登记实例，配套样式行为据此隔离未适配图表
 */
export function enableInstanceDataRefresh(instance: object) {
  refreshInstances.add(instance)
}

/**
 * 查询实例是否已经完成局部更新适配，不能仅凭图表名称判断
 */
export function isInstanceDataRefreshEnabled(instance: object): boolean {
  return refreshInstances.has(instance)
}

/**
 * 只有明确注册的图表进入定时增量刷新，继承的其他图表不自动启用
 */
export function registerChartRefreshAdapter(type: string, adapter: ChartRefreshAdapter) {
  chartRefreshAdapters.set(type, adapter)
}

/**
 * 按精确图表类型查询适配器，不自动匹配派生类型
 */
export const getChartRefreshAdapter = (type: string) => chartRefreshAdapters.get(type)

/**
 * 开关开启但配置为空时没有条件样式需要处理
 */
export function hasConfiguredThreshold(threshold?: ChartThreshold): boolean {
  return (
    !!threshold?.enable &&
    Object.entries(threshold).some(([key, value]) => key !== 'enable' && !isEmpty(value))
  )
}

/**
 * 样式注册表同时列出已支持和待适配功能，避免遗漏启用中的功能
 */
export const styleRefreshAdapters: Record<string, StyleRefreshAdapter> = {
  assistLine: {
    markId: 'assist-line',
    enabled: chart => !!chart.senior?.assistLineCfg?.enable,
    supports: adapter => !!adapter.assistLines,
    /**
     * 追加本轮辅助线并标记归属，供后续刷新区分主图元和附加图元
     */
    update: (options, chart, adapter) => {
      options.children.push(
        ...tagRefreshComponentMarks('assist-line', adapter.assistLines(chart, options))
      )
      return options
    }
  },
  conditions: {
    enabled: chart => hasConfiguredThreshold(chart.senior?.threshold),
    supports: adapter => !!adapter.conditions,
    /**
     * 委托图表自己的条件计算，避免重复实现颜色规则
     */
    update: (options, chart, adapter) => adapter.conditions(chart, options)
  },
  // 标签和最值沿用主图元的转换与标签回调，公共更新替换它们的输入数据
  labels: {
    enabled: chart => !!chart.customAttr?.label?.show,
    supports: () => true
  },
  // 轮播进度由公共更新入口暂停和恢复，tooltip 回调读取最新数据
  carousel: {
    enabled: chart => !!chart.customAttr?.tooltip?.carousel?.enable,
    supports: () => true
  },
  slider: {
    enabled: chart => !!chart.senior?.functionCfg?.sliderShow,
    supports: () => false
  },
  tiledLegend: {
    enabled: chart => chart.customStyle?.legend?.displayMode === 'tile',
    supports: () => false
  }
}

/**
 * 返回不支持的原因标识，空字符串表示通过公共能力检查
 * 支持范围由图表数据结构和已启用样式共同决定
 */
export function getRefreshUnsupportedReason(
  chart: ChartObj,
  adapter?: ChartRefreshAdapter
): string {
  if (!adapter) return 'chart-not-registered'
  if (chart.yAxis?.length !== 1 || chart.xAxisExt?.length || chart.extStack?.length) {
    return 'multiple-series'
  }
  if (
    !Array.isArray(chart.data?.data) ||
    chart.data.data.some(row => !Number.isFinite(row.value))
  ) {
    return 'non-finite-data'
  }
  return (
    Object.entries(styleRefreshAdapters).find(
      ([, style]) => style.enabled(chart) && !style.supports(adapter, chart)
    )?.[0] || ''
  )
}

/**
 * 用维度、系列和指标标识数据身份，不包含会变化的指标值
 */
const identity = (row: RefreshRow) =>
  JSON.stringify([row.field, row.category, row.quotaList?.map(item => item.id)])

/**
 * 先匹配同一维度中数值未变的行，再匹配改值行，避免重复维度增删误伤未变元素
 * 数值加入图元 key，使改值和新增元素进入入场分支，重排不改变身份
 */
export function createRefreshData(rows: RefreshRow[], previous?: RefreshDataState) {
  const groups = new Map<string, Set<RefreshDataState['items'][number]>>()
  for (const item of previous?.items || []) {
    const key = identity(item.row)
    if (!groups.has(key)) groups.set(key, new Set())
    groups.get(key).add(item)
  }
  const matches = rows.map(row => {
    const available = groups.get(identity(row))
    const match = available && [...available].find(item => item.row.value === row.value)
    if (match) available.delete(match)
    return match
  })
  let nextId = previous?.nextId || 0
  const items = rows.map((row, index) => {
    const available = groups.get(identity(row))
    const match = matches[index] || available?.values().next().value
    if (match) available.delete(match)
    return { row: cloneDeep(row), id: match?.id ?? nextId++ }
  })
  return {
    state: { items, nextId },
    data: items.map(({ row, id }) => ({
      ...cloneDeep(row),
      [ITEM_KEY]: JSON.stringify([id, row.value])
    }))
  }
}

/**
 * 附加图元与主图元分开标记，辅助线使用自己的数据
 */
export function tagRefreshComponentMarks(id: string, marks: ChildSpec[]): ChildSpec[] {
  return marks.map((mark, index) => ({
    ...mark,
    key: `de-refresh:${id}:${index}`,
    encode: {
      ...mark.encode,
      key: (datum: RefreshRow, row: number) =>
        JSON.stringify([datum.id, datum.field, datum.fieldId, datum.summary, datum.yAxisType, row])
    }
  }))
}

/**
 * 校验主图元顺序及附加图元归属，未知结构交回完整绘制流程
 */
export function matchesRefreshMarks(options: G2Spec, adapter: ChartRefreshAdapter): boolean {
  const view = options as ViewSpec
  return (
    view.type === 'view' &&
    view.children?.length >= adapter.marks.length &&
    view.children.every((mark, index) =>
      index < adapter.marks.length
        ? mark.type === adapter.marks[index].type
        : Object.values(styleRefreshAdapters).some(
            style =>
              style.markId && String(mark.key || '').startsWith(`de-refresh:${style.markId}:`)
          )
    )
  )
}

/**
 * 首次绘制保留既有数据转换，后续样式适配器会重建条件转换而非层层叠加
 */
export function configureRefreshKeys(
  options: G2Spec,
  adapter: ChartRefreshAdapter,
  data: RefreshRow[]
) {
  const view = options as ViewSpec
  view.data = replaceData(view.data, data)
  // 空数据时不能让数值轴被推断为分类轴，否则恢复数据后位置会错误
  const channel = adapter.valueChannel
  view.scale = {
    ...view.scale,
    [channel]: { type: 'linear', ...view.scale?.[channel] }
  }
  view.children.slice(0, adapter.marks.length).forEach((mark, index) => {
    const { key } = adapter.marks[index]
    mark.key = `de-refresh:main:${index}`
    mark.data = replaceData(mark.data, data)
    mark.encode = {
      ...mark.encode,
      key:
        key === 'item'
          ? ITEM_KEY
          : (row: RefreshRow) => JSON.stringify([row.category, row.quotaList?.map(item => item.id)])
    }
  })
}

/**
 * 替换数据值并保留已有数据源上的转换配置
 */
const replaceData = (source: G2Spec['data'], data: RefreshRow[]) =>
  source && !Array.isArray(source) && typeof source === 'object' ? { ...source, value: data } : data

/**
 * 更新当前实例的主数据，条件颜色与辅助线在本轮 render 前一起生成
 */
export function createRefreshOptions(
  options: G2Spec,
  chart: ChartObj,
  adapter: ChartRefreshAdapter,
  data: RefreshRow[]
): ViewSpec {
  const view = options as ViewSpec
  let next: ViewSpec = {
    ...view,
    type: 'view',
    data: replaceData(view.data, data),
    children: view.children.slice(0, adapter.marks.length).map(mark => ({
      ...mark,
      data,
      // 改值通过新 key 入场，旧元素直接移除，避免出入场重叠
      animate: { ...(typeof mark.animate === 'object' ? mark.animate : {}), exit: { type: null } }
    }))
  }
  for (const style of Object.values(styleRefreshAdapters)) {
    if (style.enabled(chart) && style.update) next = style.update(next, chart, adapter)
  }
  return next
}
