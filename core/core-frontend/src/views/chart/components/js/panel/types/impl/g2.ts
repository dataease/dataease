import { AxisComponent, G2Spec, type Chart as G2Chart } from '@antv/g2'
import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import { configEmptyDataStyle } from '@/views/chart/components/js/panel/common/common_antv'
import { parseJson, setupSeriesColor } from '../../../util'
import { isEmpty } from 'lodash-es'
import { valueFormatter } from '../../../formatter'
import {
  LEGEND_POPTIP_FOLLOW_DOM_STYLE,
  measureLegendTextWidth,
  prepareLegendPoptip,
  renderLegendPoptipText
} from './g2-legend-poptip'

export const LEGEND_NAV_CONTROLLER_PADDING = 5
export const LEGEND_NAV_CONTROLLER_SPACING = 5
const LEGEND_NAV_BUTTON_MIN_SIZE = 8
const LEGEND_NAV_BUTTON_MAX_SIZE = 12
const LEGEND_NAV_PAGE_FONT_MIN_SIZE = 10
const LEGEND_NAV_PAGE_FONT_MAX_SIZE = 12
const HORIZONTAL_LEGEND_LABEL_MIN_WIDTH = 120
const HORIZONTAL_LEGEND_LABEL_MAX_WIDTH = 240
const HORIZONTAL_LEGEND_LABEL_EM = 12
const HORIZONTAL_LEGEND_NAV_CONTROLLER_SPACING = 8

// 统一阈值按估算出的数据 mark 工作量判断，而不是只看后端返回的记录数
// 这样可以把多指标展开、多个 mark 复用同一份数据以及数据标签带来的额外开销都计算在内
// 阈值以下继续保留动画和标签体验，超过阈值后优先保证首次渲染、刷新和容器缩放的响应速度
const LARGE_DATA_RENDER_COUNT = 1000

// 大数据图表最多创建的普通标签图元数量
// 超出后白名单图表按维度均匀抽取标签载体，其它图表保留各自原有标签策略
// 2000 可覆盖约 280 个维度值乘 7 个指标的常见展开规模
const LARGE_DATA_LABEL_RENDER_COUNT = 2000

// 采样标签使用独立的不可见数据 mark 承载，通过固定 key 前缀与真实业务图元区分
export const LARGE_DATA_LABEL_MARK_KEY_PREFIX = '__de_large_data_label__'

// 仅为定位方式已确认的图表创建采样载体，根 point 会先包装为共享尺度的 view
// 其它根图元和极坐标图表保留各自原有标签策略，避免改变比例尺或几何布局
const LARGE_DATA_LABEL_MARK_TYPES = new Map([
  ['bar', new Set(['interval'])],
  ['bar-horizontal', new Set(['interval'])],
  ['bar-stack', new Set(['interval', 'point'])],
  ['bar-stack-horizontal', new Set(['interval', 'point'])],
  ['bar-group', new Set(['interval'])],
  ['bar-group-stack', new Set(['interval'])],
  ['percentage-bar-stack', new Set(['interval'])],
  ['percentage-bar-stack-horizontal', new Set(['interval'])],
  ['progress-bar', new Set(['interval'])],
  ['bar-range', new Set(['interval'])],
  ['waterfall', new Set(['interval'])],
  ['bidirectional-bar', new Set(['interval'])],
  ['bullet-graph', new Set(['interval'])],
  ['line', new Set(['point'])],
  ['area', new Set(['point'])],
  ['area-stack', new Set(['point'])],
  ['chart-mix', new Set(['interval', 'point'])],
  ['chart-mix-group', new Set(['interval', 'point'])],
  ['chart-mix-stack', new Set(['interval', 'point'])],
  ['chart-mix-dual-line', new Set(['point'])],
  ['radar', new Set(['line'])],
  ['scatter', new Set(['point'])],
  ['multi-scatter', new Set(['point'])],
  ['quadrant', new Set(['point'])]
])

// 只统计图元数量会随数据量线性增长的基础 mark
// 辅助线和液体图、仪表盘、词云、桑基图等结构型图表不在此处统一降级，避免改变其动画语义
const LARGE_DATA_MARK_TYPES = new Set(['interval', 'point', 'line', 'area', 'cell'])

// elementHighlight 会为大量柱体建立高亮命中区域，基础柱状图在多指标大数据下开销最明显
// 目前只对白名单中的基础柱状图关闭该交互，其它图表继续保留原有高亮行为
const LARGE_DATA_DISABLE_HIGHLIGHT_CHARTS = new Set(['bar'])

// 用于遍历最终 G2 Spec 的内部结构类型
// children 兼容静态子 mark 数组和 G2 支持的函数形式，data 兼容不同的内联数据写法
type LargeDataSpec = G2Spec & {
  children?: LargeDataSpec[] | ((...args: any[]) => LargeDataSpec)
  data?: unknown
  labels?: Record<string, any>[]
  interaction?: Record<string, any>
  animate?: unknown
}

/** G2 公共字体处理需要访问的可选 Spec 字段 */
type G2FontSpec = LargeDataSpec & {
  layout?: Record<string, any>
  style?: Record<string, any>
  theme?: string | Record<string, any>
}

/** 主题字段可能是字符串或数组，只合并普通对象以避免破坏原配置 */
const isRecord = (value: unknown): value is Record<string, any> =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

/** 补齐 G2 内置组件的字体主题，同时保留图表已有的主题属性 */
const applyG2FontTheme = (theme: G2FontSpec['theme'], fontFamily: string): Record<string, any> => {
  const currentTheme = typeof theme === 'string' ? { type: theme } : isRecord(theme) ? theme : {}
  const tooltip = isRecord(currentTheme.tooltip) ? currentTheme.tooltip : {}
  const tooltipCss = isRecord(tooltip.css) ? tooltip.css : {}
  const tooltipStyle = isRecord(tooltipCss['.g2-tooltip']) ? tooltipCss['.g2-tooltip'] : {}
  return {
    ...currentTheme,
    axis: {
      ...(isRecord(currentTheme.axis) ? currentTheme.axis : {}),
      labelFontFamily: fontFamily,
      titleFontFamily: fontFamily
    },
    legendCategory: {
      ...(isRecord(currentTheme.legendCategory) ? currentTheme.legendCategory : {}),
      itemLabelFontFamily: fontFamily,
      itemValueFontFamily: fontFamily,
      navPageNumFontFamily: fontFamily,
      titleFontFamily: fontFamily
    },
    legendContinuous: {
      ...(isRecord(currentTheme.legendContinuous) ? currentTheme.legendContinuous : {}),
      handleLabelFontFamily: fontFamily,
      labelFontFamily: fontFamily,
      titleFontFamily: fontFamily
    },
    label: {
      ...(isRecord(currentTheme.label) ? currentTheme.label : {}),
      fontFamily
    },
    innerLabel: {
      ...(isRecord(currentTheme.innerLabel) ? currentTheme.innerLabel : {}),
      fontFamily
    },
    htmlLabel: {
      ...(isRecord(currentTheme.htmlLabel) ? currentTheme.htmlLabel : {}),
      fontFamily
    },
    slider: {
      ...(isRecord(currentTheme.slider) ? currentTheme.slider : {}),
      handleLabelFontFamily: fontFamily
    },
    title: {
      ...(isRecord(currentTheme.title) ? currentTheme.title : {}),
      titleFontFamily: fontFamily,
      subtitleFontFamily: fontFamily
    },
    tooltip: {
      ...tooltip,
      css: {
        ...tooltipCss,
        '.g2-tooltip': {
          ...tooltipStyle,
          // G2 默认在 tooltip 节点写入 sans-serif，需要显式覆盖而不能只依赖父容器继承
          'font-family': fontFamily
        }
      }
    }
  }
}

/** 递归处理组合图表的 Spec，并适配不同图形使用的字体字段 */
const applyG2FontFamily = (spec: G2FontSpec, fontFamily: string): G2FontSpec => {
  const style = isRecord(spec.style) ? { ...spec.style } : undefined
  if (style) {
    // text、层级图、仪表盘和水波图在 G2 中分别使用不同的字体属性
    if (spec.type === 'text') {
      style.fontFamily = fontFamily
    }
    if ('labelText' in style || 'labelFontSize' in style) {
      style.labelFontFamily = fontFamily
    }
    if ('textContent' in style || 'textFontSize' in style) {
      style.textFontFamily = fontFamily
    }
    if ('contentText' in style || 'contentFontSize' in style) {
      style.contentFontFamily = fontFamily
    }
  }
  // 同时覆盖标签根属性与 style，兼容各图表当前采用的两种标签写法
  const labels = Array.isArray(spec.labels)
    ? spec.labels.map(label => ({
        ...label,
        fontFamily,
        ...(isRecord(label.style) && { style: { ...label.style, fontFamily } })
      }))
    : spec.labels
  // mix、spaceFlex 等组合图表会把实际图形放在 children 中
  const children = Array.isArray(spec.children)
    ? spec.children.map(child => applyG2FontFamily(child, fontFamily))
    : spec.children
  return {
    ...spec,
    // 最终 Spec 统一只注入字体属性，保留各图表已有的颜色、字号和布局配置
    theme: applyG2FontTheme(spec.theme, fontFamily),
    ...(style && { style }),
    ...(labels && { labels }),
    // 词云在布局阶段就需要字体信息，否则测量结果会与最终绘制不一致
    ...(spec.type === 'wordCloud' && {
      layout: { ...(isRecord(spec.layout) ? spec.layout : {}), font: fontFamily }
    }),
    ...(children && { children })
  }
}

/**
 * 提取当前 Spec 节点直接持有的内联数据
 *
 * DataEase 的 G2 数据既可能直接传入数组，也可能包装在 `{ value }` 中
 * 无法识别时返回 undefined，让子 mark 可以继续继承父 View 的数据
 */
const getInlineData = (data: unknown): unknown[] | undefined => {
  if (Array.isArray(data)) {
    return data
  }
  if (data && typeof data === 'object') {
    const value = (data as { value?: unknown }).value
    if (Array.isArray(value)) {
      return value
    }
  }
}

/**
 * 提取当前 Spec 节点直接持有的内联数据长度
 *
 * 统计阶段只读取数组 length，不遍历业务字段，避免公共性能判断本身随数据量明显变慢
 */
const getInlineDataLength = (data: unknown): number | undefined => getInlineData(data)?.length

/**
 * 从有序集合中均匀抽取指定数量的元素
 *
 * 首尾元素始终保留，中间元素按等距索引选择，让采样标签覆盖完整的维度范围
 *
 * @param values 原始有序集合
 * @param limit 最大保留数量
 * @returns 不超过 limit 且保持原顺序的采样结果
 */
const sampleEvenly = <T>(values: T[], limit: number): T[] => {
  if (values.length <= limit) {
    return values
  }
  if (limit <= 1) {
    return values.slice(0, 1)
  }
  const step = (values.length - 1) / (limit - 1)
  return Array.from({ length: limit }, (_, index) => values[Math.round(index * step)])
}

/**
 * 合并父 View 与当前 mark 的编码字段
 *
 * 折线图的 point mark 会从父 View 继承 x、y 和 color，柱状图则直接在 interval 上声明编码
 * 在生成标签载体前统一合并，可以同时取得分类维度和系列字段
 *
 * @param inheritedEncode 父 View 已生效的编码
 * @param encode 当前 mark 自己声明的编码
 * @returns 当前 mark 实际使用的编码集合
 */
const mergeEncode = (
  inheritedEncode?: Record<string, any>,
  encode?: Record<string, any>
): Record<string, any> => ({ ...inheritedEncode, ...encode })

/**
 * 判断单条数据经过现有标签文本规则后是否会产生可见内容
 *
 * 不同图表分别使用 text 字段或 text 函数，两种写法都在这里复用
 * 先过滤未勾选的指标可以避免 G2 为返回空文本的数据仍然创建标签图元
 * 标签函数出现异常时保守保留数据，让正式渲染流程继续暴露原有问题而不是静默丢标签
 *
 * @param labels 当前 mark 的标签配置
 * @param datum 待判断的数据记录
 * @returns 至少一个标签配置是否会输出非空文本
 */
const hasVisibleLabelText = (labels: Record<string, any>[], datum: unknown): boolean => {
  return labels.some(label => {
    try {
      const record = datum as Record<string, unknown>
      const text =
        typeof label.text === 'function'
          ? label.text(record)
          : typeof label.text === 'string'
          ? record?.[label.text]
          : label.text
      const formattedText =
        typeof label.formatter === 'function' ? label.formatter(text, record) : text
      return formattedText !== '' && formattedText !== null && formattedText !== undefined
    } catch {
      return true
    }
  })
}

/**
 * 计算当前标签模式允许创建的数据载体数量
 *
 * 每条载体数据会为每个 label 配置创建一个文本图元，因此需要按配置数量均分总预算
 * 普通和全量显示共用统一预算，避免不同模式下出现容量差异
 *
 * @param labels 当前 mark 的标签配置
 * @returns 当前 mark 最多保留的数据记录数
 */
const getLabelDataRenderLimit = (labels: Record<string, any>[]): number => {
  return Math.max(1, Math.floor(LARGE_DATA_LABEL_RENDER_COUNT / Math.max(1, labels.length)))
}

/**
 * 按字段提取保持原始顺序的唯一值域
 *
 * 柱状图过滤未显示标签的指标后仍需保留完整系列域，否则 dodgeX 会把标签移动到组内错误位置
 *
 * @param data 完整的 mark 数据
 * @param field 系列编码字段
 * @returns 去重且保持首次出现顺序的字段值
 */
const getFieldDomain = (data: unknown[], field: unknown): unknown[] => {
  if (typeof field !== 'string') {
    return []
  }
  return Array.from(
    new Map(
      data.map(item => {
        const value = (item as Record<string, unknown>)?.[field]
        return [`${value instanceof Date ? value.getTime() : value}`, value]
      })
    ).values()
  )
}

/**
 * 按分类维度均匀抽取标签载体数据
 *
 * 柱状图的同一维度通常包含多个指标，采样时整组保留可以维持 dodgeX 后的柱体位置
 * 无法识别分类字段时退化为按数据行均匀抽取，仍确保标签数量不会失控
 *
 * @param data 当前数据 mark 使用的数据
 * @param dimensionField 分类维度对应的字段名
 * @param limit 标签载体允许保留的最大数据行数
 * @returns 用于承载有限标签的数据子集
 */
const sampleLabelData = (data: unknown[], dimensionField: unknown, limit: number): unknown[] => {
  if (data.length <= limit) {
    return data
  }
  if (typeof dimensionField !== 'string') {
    return sampleEvenly(data, limit)
  }
  const groups = new Map<unknown, unknown[]>()
  data.forEach(item => {
    const dimension = (item as Record<string, unknown>)?.[dimensionField]
    const group = groups.get(dimension)
    if (group) {
      group.push(item)
    } else {
      groups.set(dimension, [item])
    }
  })
  const dimensionGroups = Array.from(groups.values())
  const maxGroupSize = Math.max(...dimensionGroups.map(group => group.length))
  const groupLimit = Math.max(1, Math.floor(limit / maxGroupSize))
  const sampledData = sampleEvenly(dimensionGroups, groupLimit).flat()
  return sampledData.length <= limit ? sampledData : sampleEvenly(data, limit)
}

/**
 * 用采样数据替换 mark 的内联数据，同时保留原有数据转换配置
 *
 * 条件色等逻辑会把数据包装为 `{ value, transform }`，只替换 value 可以继续复用原转换链
 *
 * @param originalData mark 原始 data 配置
 * @param sampledData 已完成采样的数据子集
 * @returns 可直接写回 G2 Spec 的数据配置
 */
const replaceInlineData = (originalData: unknown, sampledData: unknown[]): unknown => {
  if (originalData && !Array.isArray(originalData) && typeof originalData === 'object') {
    return { ...originalData, value: sampledData }
  }
  return sampledData
}

/**
 * 将单个标签数据 mark 拆分为完整主图元与有限标签载体
 *
 * @param mark 当前标签所属的数据 mark
 * @param labelMarkTypes 当前图表允许使用标签载体的 mark 类型
 * @param data 当前 mark 实际使用的数据
 * @param encode 当前 mark 实际使用的编码
 * @param path 当前节点路径
 * @returns 无需处理时返回 undefined，否则返回主图元和标签载体
 */
const createSampledLabelMarks = (
  mark: LargeDataSpec,
  labelMarkTypes: Set<string>,
  data: unknown[] | undefined,
  encode: Record<string, any>,
  path: string
): LargeDataSpec[] | undefined => {
  const labels = Array.isArray(mark.labels) ? mark.labels : []
  if (
    !labelMarkTypes.has(mark.type) ||
    !data ||
    labels.length === 0 ||
    data.length * labels.length <= LARGE_DATA_LABEL_RENDER_COUNT
  ) {
    return
  }
  const visibleLabelData = data.filter(datum => hasVisibleLabelText(labels, datum))
  if (!visibleLabelData.length) {
    return [{ ...mark, labels: [] }]
  }
  const sampledData = sampleLabelData(visibleLabelData, encode.x, getLabelDataRenderLimit(labels))
  const seriesField = encode.series ?? encode.color
  const seriesDomain = mark.type === 'interval' ? getFieldDomain(data, seriesField) : []
  const labelMark = {
    ...mark,
    key: `${LARGE_DATA_LABEL_MARK_KEY_PREFIX}${path}`,
    data: replaceInlineData(mark.data, sampledData),
    ...(seriesDomain.length && {
      scale: {
        ...mark.scale,
        series: { ...mark.scale?.series, domain: seriesDomain }
      }
    }),
    tooltip: false,
    animate: false,
    style: {
      ...mark.style,
      fillOpacity: 0,
      strokeOpacity: 0,
      pointerEvents: 'none'
    }
  } as LargeDataSpec
  // G2 会把 axis、legend 和 slider 写入比例尺后再合并多个 mark
  // 对共享组件赋值 false 会生成空 guide 并覆盖主图，因此这里只移除载体独有的交互字段
  // 不声明 slider 才表示载体不创建缩略轴，同时不会破坏主 mark 已有的可交互缩略轴
  delete labelMark.slider
  delete labelMark.interaction
  delete labelMark.state
  return [{ ...mark, labels: [] }, labelMark]
}

/**
 * 为指定图表生成有限数量的标签载体 mark
 *
 * 真实数据 mark 不再配置超量 labels，避免 G2 为全部数据创建文本图元
 * 标签载体先执行现有文本规则过滤未启用的指标，再按分类维度均匀采样
 * interval 通过完整 series 域保持分组位置，主图元继续使用完整数据绘制
 * 标签载体保留主 mark 的轴和图例配置，防止共享比例尺合并时把公共 guide 覆盖为空
 * 缩略轴字段在构造完成后彻底移除，避免重复创建控件或覆盖主 mark 的缩略轴配置
 *
 * @param spec 当前需要处理的 Spec 节点
 * @param labelMarkTypes 当前图表允许使用标签载体的 mark 类型
 * @param inheritedData 从父 View 继承的内联数据
 * @param inheritedEncode 从父 View 继承的编码字段
 * @param path 当前节点路径，用于生成稳定且唯一的 mark key
 * @returns 已将超量标签替换为受控标签载体的新 Spec
 */
const sampleLargeDataLabels = (
  spec: LargeDataSpec,
  labelMarkTypes: Set<string>,
  inheritedData?: unknown[],
  inheritedEncode?: Record<string, any>,
  path = '0'
): LargeDataSpec => {
  const data = getInlineData(spec.data) ?? inheritedData
  const encode = mergeEncode(inheritedEncode, spec.encode as Record<string, any>)
  if (!Array.isArray(spec.children)) {
    const rootMarks =
      path === '0' ? createSampledLabelMarks(spec, labelMarkTypes, data, encode, path) : undefined
    if (rootMarks) {
      // 根 point 无法直接添加兄弟标签 mark，包装为 view 后继续复用原比例尺和事件图元
      return {
        type: 'view',
        autoFit: spec.autoFit,
        theme: spec.theme,
        children: rootMarks
      } as LargeDataSpec
    }
    return spec
  }
  let changed = false
  const children = spec.children.flatMap((child, index) => {
    const childPath = `${path}-${index}`
    const preparedChild = sampleLargeDataLabels(child, labelMarkTypes, data, encode, childPath)
    const childData = getInlineData(preparedChild.data) ?? data
    const childEncode = mergeEncode(encode, preparedChild.encode as Record<string, any>)
    const sampledMarks = createSampledLabelMarks(
      preparedChild,
      labelMarkTypes,
      childData,
      childEncode,
      childPath
    )
    if (!sampledMarks) {
      changed ||= preparedChild !== child
      return [preparedChild]
    }
    changed = true
    return sampledMarks
  })
  return changed ? ({ ...spec, children } as LargeDataSpec) : spec
}

/**
 * 判断 mark key 是否属于大数据采样标签载体
 *
 * 渲染组件使用该判断跳过联动样式回放，避免不可见载体被选中描边重新显示
 *
 * @param markKey G2 图元关联的 mark key
 * @returns 是否为大数据采样标签载体
 */
export const isLargeDataLabelMark = (markKey: unknown): boolean =>
  typeof markKey === 'string' && markKey.startsWith(LARGE_DATA_LABEL_MARK_KEY_PREFIX)

/**
 * 递归估算最终 G2 Spec 需要处理的数据 mark 工作量
 *
 * 每个基础数据 mark 按其有效数据长度计数，同一份数据被 line 和 point 复用时会分别累计
 * 每组 labels 还会为每个图元生成标签，因此按额外的数据 mark 倍数计入
 * 使用 setupOptions 完成后的最终 Spec，可以覆盖图表按样式动态追加的 mark、data 和 label
 *
 * @param spec 当前需要统计的 Spec 或子 mark
 * @param inheritedDataLength 从父 View 继承的数据长度
 * @returns 当前节点及其全部静态子 mark 的估算工作量
 */
const getLargeDataRenderCount = (spec: LargeDataSpec, inheritedDataLength = 0): number => {
  const dataLength = getInlineDataLength(spec.data) ?? inheritedDataLength
  const labelCount = Array.isArray(spec.labels) ? spec.labels.length : 0
  const markCount = LARGE_DATA_MARK_TYPES.has(spec.type) ? dataLength * (1 + labelCount) : 0
  const children = Array.isArray(spec.children) ? spec.children : []
  return children.reduce(
    (count, child) => count + getLargeDataRenderCount(child, dataLength),
    markCount
  )
}

/**
 * 在不修改原 Spec 对象的前提下，递归生成大数据优化版本
 *
 * 关闭基础数据 mark 的 enter、update 和 exit 动画，保留辅助元素及结构型图表的动画语义
 * 标签数量仅由白名单图表的采样载体控制，其它几何结构保留各自原有标签策略
 * 单独绘制的极值、参考线等 text mark 不受影响，tooltip 配置也保持原样
 * 只有显式列入策略的图表才关闭已经存在的 elementHighlight，tooltip 和选择交互保持原样
 * 返回新对象可以避免污染图表类复用的默认配置，也能让 G2 options 正确感知配置变化
 *
 * @param spec 当前需要优化的 Spec 或子 mark
 * @param disableElementHighlight 是否关闭当前 Spec 树中的元素高亮交互
 * @returns 保留原业务配置并应用大数据降级策略的新 Spec
 */
const optimizeLargeDataSpec = (
  spec: LargeDataSpec,
  disableElementHighlight: boolean,
  inheritedData?: unknown[]
): LargeDataSpec => {
  const data = getInlineData(spec.data) ?? inheritedData
  const children = Array.isArray(spec.children)
    ? spec.children.map(child => optimizeLargeDataSpec(child, disableElementHighlight, data))
    : spec.children
  const interaction =
    disableElementHighlight && spec.interaction && spec.interaction.elementHighlight !== undefined
      ? { ...spec.interaction, elementHighlight: false }
      : spec.interaction
  const isLargeDataMark = LARGE_DATA_MARK_TYPES.has(spec.type)
  const disableAnimation = isLargeDataMark && spec.animate !== false
  if (children === spec.children && interaction === spec.interaction && !disableAnimation) {
    return spec
  }
  return {
    ...spec,
    ...(disableAnimation ? { animate: false } : {}),
    ...(interaction !== spec.interaction ? { interaction } : {}),
    ...(children !== spec.children ? { children } : {})
  } as LargeDataSpec
}

export const getLegendNavButtonPath = (size: number) => [
  ['M', -size / 2, -size / 2],
  ['L', size / 2, 0],
  ['L', -size / 2, size / 2],
  ['Z']
]

// 统一分类图例的 marker、label 与分页器样式，避免各图表配置漂移
export const getCategoryLegendStyle = (markerSize: number, fontSize: number, color: string) => {
  // 分页器使用 AntV 55px 固定区域，不跟随超大图标和文本继续放大
  const navButtonSize = Math.min(
    LEGEND_NAV_BUTTON_MAX_SIZE,
    Math.max(LEGEND_NAV_BUTTON_MIN_SIZE, markerSize)
  )
  const navPageNumFontSize = Math.min(
    LEGEND_NAV_PAGE_FONT_MAX_SIZE,
    Math.max(LEGEND_NAV_PAGE_FONT_MIN_SIZE, fontSize)
  )
  return {
    itemMarkerSize: markerSize,
    itemLabelFontSize: fontSize,
    itemLabelFill: color,
    itemLabelFillOpacity: 1,
    itemLabelOpacity: 1,
    itemMarkerLineWidth: 0,
    navPageNumFontSize,
    navPageNumFill: color,
    navPageNumFillOpacity: 1,
    // 直接提供目标像素大小的路径，避免 Navigator 重绘时基于旧 transform 交替缩放按钮
    navButtonD: getLegendNavButtonPath(navButtonSize),
    navButtonSize,
    navButtonFill: color,
    navButtonFillOpacity: 1,
    navOrientation: 'horizontal',
    navControllerPadding: LEGEND_NAV_CONTROLLER_PADDING,
    navControllerSpacing: LEGEND_NAV_CONTROLLER_SPACING
  }
}

const measureAxisLabelWidth = (value: unknown, fontSize: number) => {
  const label =
    typeof value === 'object' && value !== null
      ? `${value['label'] ?? value['value'] ?? ''}`
      : `${value ?? ''}`
  return measureLegendTextWidth(label, fontSize)
}

export const getHorizontalLegendLabelMaxWidth = (fontSize: number) => {
  const safeFontSize = Number.isFinite(fontSize) && fontSize > 0 ? fontSize : 12
  return Math.min(
    HORIZONTAL_LEGEND_LABEL_MAX_WIDTH,
    Math.max(HORIZONTAL_LEGEND_LABEL_MIN_WIDTH, safeFontSize * HORIZONTAL_LEGEND_LABEL_EM)
  )
}

const truncateHorizontalLegendLabel = (value: unknown, fontSize: number, maxWidth: number) => {
  const label =
    typeof value === 'object' && value !== null
      ? `${value['label'] ?? value['value'] ?? ''}`
      : `${value ?? ''}`
  if (measureAxisLabelWidth(label, fontSize) <= maxWidth) {
    return label
  }
  const chars = Array.from(label)
  const suffix = '...'
  const suffixWidth = measureAxisLabelWidth(suffix, fontSize)
  let start = 0
  let end = chars.length
  while (start < end) {
    const middle = Math.ceil((start + end) / 2)
    const text = chars.slice(0, middle).join('')
    if (measureAxisLabelWidth(text, fontSize) + suffixWidth <= maxWidth) {
      start = middle
    } else {
      end = middle - 1
    }
  }
  return `${chars.slice(0, start).join('')}${suffix}`
}

/**
 * 上下图例必须在 G2 首次测量前限制单项文本宽度
 * 否则任意分页中的超长项都会把所有页面和分页器之间的距离一起撑大
 */
export const getHorizontalLegendTextStyle = (fontSize: number) => {
  prepareLegendPoptip()
  const safeFontSize = Number.isFinite(fontSize) && fontSize > 0 ? fontSize : 12
  const maxWidth = getHorizontalLegendLabelMaxWidth(safeFontSize)
  const truncatedItemPoptip = {
    // id 始终保留原始图例值，省略后悬停展示完整内容
    render: ({ id, label }) => renderLegendPoptipText(id ?? label),
    domStyles: {
      '.component-poptip': {
        ...LEGEND_POPTIP_FOLLOW_DOM_STYLE,
        maxWidth: '320px',
        whiteSpace: 'normal',
        wordBreak: 'break-all'
      }
    }
  }
  return {
    labelFormatter: value => truncateHorizontalLegendLabel(value, safeFontSize, maxWidth),
    itemLabelWordWrap: true,
    itemLabelWordWrapWidth: maxWidth,
    itemLabelMaxLines: 1,
    itemLabelTextOverflow: '...',
    // 上下图例项与分页器保持一个 8px 基础间距，分页器内部间距继续沿用公共配置
    navControllerSpacing: HORIZONTAL_LEGEND_NAV_CONTROLLER_SPACING,
    // AntV 会按图例项解析 itemPoptip，未省略的文字不创建提示绑定
    itemPoptip: ({ id, label }) =>
      `${id ?? label ?? ''}` !== `${label ?? ''}` ? truncatedItemPoptip : undefined
  }
}

export interface G2DrawOptions<O> extends AntVDrawOptions<O> {
  /**
   * 缩放比例
   */
  scale?: number
  /**
   * 特殊处理，象限图设置分割线的默认值
   * @param args
   */
  quadrantDefaultBaseline?: (...args: any) => void
}

export abstract class G2ChartView<
  O extends G2Spec = G2Spec,
  P extends G2Chart = G2Chart
> extends AntVAbstractChartView {
  public abstract drawChart(drawOptions: G2DrawOptions<P>): P | Promise<P>

  /** 在首次渲染前统一应用视图主题字体 */
  public applyThemeFont(chart?: P, fontFamily?: string): void {
    if (!chart || !fontFamily || fontFamily === 'inherit') {
      return
    }
    chart.options(applyG2FontFamily(chart.options() as G2FontSpec, fontFamily))
  }

  /**
   * 在 drawChart 完成最终 options 装配且首次 render 尚未开始时应用公共性能策略
   *
   * 小于等于阈值时保持原动画、标签和交互，不改变常规数据量下的视觉体验
   * 超过阈值时关闭数据 mark 动画，并对支持的图表限制普通标签数量
   * 优化后的 Spec 会写回图表实例，让首次 render、刷新和后续 forceFit 复用相同行为
   * chart 参数允许为空，用于兼容空数据或图表实例尚未创建成功的场景
   *
   * @param chart drawChart 创建的 G2 图表实例
   */
  public optimizeLargeData(chart?: P): void {
    if (!chart) {
      return
    }
    const options = chart.options() as LargeDataSpec
    if (getLargeDataRenderCount(options) <= LARGE_DATA_RENDER_COUNT) {
      return
    }
    // 仅基础柱状图关闭全量区域高亮索引，其它图表保留原有交互
    // 图表类型在这里统一判断，避免各图表实现重复维护阈值和降级规则
    // 白名单图表使用受控标签载体，其它图表保留各自原有标签策略
    const labelMarkTypes = LARGE_DATA_LABEL_MARK_TYPES.get(this.name)
    const preparedOptions = labelMarkTypes
      ? sampleLargeDataLabels(options, labelMarkTypes)
      : options
    const optimizedOptions = optimizeLargeDataSpec(
      preparedOptions,
      LARGE_DATA_DISABLE_HIGHLIGHT_CHARTS.has(this.name)
    )
    chart.options(optimizedOptions)
  }

  /**
   * 图表首次 render 完成后的可选异步处理钩子
   *
   * 某些图表必须读取首次渲染生成的布局信息，修正配置后再执行一次 render
   * 公共渲染组件会等待该钩子结束，再恢复联动等依赖最终图形元素的事件状态
   * 公共轴标签边界校正已在 G2 布局阶段完成，不经过该钩子
   */
  public afterRender?(chart: P): void | Promise<void>

  /**
   * 统一坐标轴标签的旋转锚点与轴线间距
   */
  protected getAxisLabelStyle(axis: DeepPartial<ChartAxisStyle>): Partial<AxisComponent> {
    const position = axis.position
    const rotate = Number(axis.axisLabel.rotate) || 0
    const rotateRadian = (rotate * Math.PI) / 180
    const rotateRatio = Math.sin(Math.abs(rotateRadian))
    const fontSize = axis.axisLabel.fontSize || 12
    // 主题文本色按配置原值渲染，避免叠加 G2 默认透明度
    const opacityStyle = {
      labelOpacity: 1,
      labelFillOpacity: 1
    }
    if (position === 'top' || position === 'bottom') {
      const direction = position === 'top' ? -1 : 1
      return {
        ...opacityStyle,
        labelSpacing: 4,
        labelTextAlign: 'center',
        labelTextBaseline: position === 'top' ? 'bottom' : 'top',
        labelTransform: value => {
          const offset = (measureAxisLabelWidth(value, fontSize) * rotateRatio) / 2
          return `translate(0, ${(direction * offset).toFixed(2)}px) rotate(${rotate})`
        }
      }
    }
    if (position === 'left' || position === 'right') {
      const direction = position === 'left' ? 1 : -1
      return {
        ...opacityStyle,
        labelSpacing: 4 + (fontSize * rotateRatio) / 2,
        labelTextAlign: position === 'left' ? 'right' : 'left',
        labelTextBaseline: 'middle',
        labelTransform: value => {
          // 保留靠近轴线的端点锚点，并沿刻度方向补偿旋转后的半个文本投影
          const offset =
            (direction * measureAxisLabelWidth(value, fontSize) * Math.sin(rotateRadian)) / 2
          return `translate(0, ${offset.toFixed(2)}px) rotate(${rotate})`
        }
      }
    }
    return { ...opacityStyle, labelTransform: `rotate(${rotate})` }
  }

  protected getLegend = (chart: Chart, markerSizeScale = 1) => {
    let legend = {}
    let customStyle: CustomStyle
    if (chart.customStyle) {
      customStyle = parseJson(chart.customStyle)
      // legend
      if (customStyle.legend) {
        const l = JSON.parse(JSON.stringify(customStyle.legend))
        if (l.show) {
          let position
          let layoutJustifyContent = 'center'
          const legendSymbol = l.icon
          const legendSize = l.size * markerSizeScale
          const legendFontSize = l.fontSize
          const legendColor = l.color
          // position 图例布局
          // layoutJustifyContent 图例实例布局
          // 根据图例方向和位置设置布局和位置
          if (l.vPosition === 'top' || l.vPosition === 'bottom') {
            position = l.vPosition
            layoutJustifyContent =
              l.hPosition === 'left'
                ? 'flex-start'
                : l.hPosition === 'right'
                ? 'flex-end'
                : 'center'
          } else {
            position = l.hPosition
          }
          const verticalLegend = position === 'left' || position === 'right'
          legend = {
            position,
            layout: {
              justifyContent: layoutJustifyContent
            },
            itemMarker: legendSymbol,
            ...getCategoryLegendStyle(legendSize, legendFontSize, legendColor),
            ...(verticalLegend
              ? {
                  // 侧边图例由布局阶段按实际分页和文本宽度自适应
                  dataeaseSideLegendAutoLayout: true,
                  navOrientation: 'vertical',
                  maxCols: 1
                }
              : {
                  ...getHorizontalLegendTextStyle(legendFontSize),
                  maxRows: 1
                })
          }
        } else {
          legend = false
        }
      }
    }
    return legend
  }

  /**
   * 解析轴线最终颜色，主题模式使用现有反差色，自定义及历史配置使用保存值
   */
  protected getAxisLineColor(chart: Chart, axis: DeepPartial<ChartAxisStyle>): string {
    if (axis.axisLine.colorMode === 'theme') {
      const customAttr = parseJson(chart.customAttr)
      return customAttr?.basicStyle?.themeContrastColor ?? customAttr?.label?.color ?? '#000000'
    }
    // 兼容历史图表：缺失颜色模式时继续使用已保存的轴线颜色
    return axis.axisLine.lineStyle.color
  }

  /**
   * 统一输出轴线和刻度样式，普通轴跟随轴线显隐，双轴可保留独立刻度开关
   */
  protected getAxisLineStyle(
    chart: Chart,
    axis: DeepPartial<ChartAxisStyle>,
    independentTick = false
  ): Partial<AxisComponent> {
    const axisLineColor = this.getAxisLineColor(chart, axis)
    return {
      line: axis.axisLine.show,
      lineStroke: axisLineColor,
      lineStrokeOpacity: 1,
      lineLineWidth: axis.axisLine.lineStyle.width,
      tick: independentTick
        ? axis.axisLabel.show && axis.axisLabel.showTick !== false
        : axis.axisLine.show,
      tickLineWidth: axis.axisLine.lineStyle.width,
      tickStroke: axisLineColor,
      tickOpacity: 1,
      tickStrokeOpacity: 1
    }
  }

  protected getAxis(chart: Chart, axis: DeepPartial<ChartAxisStyle>): AxisComponent {
    let lineLineDash = undefined
    if (axis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (axis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (axis.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (axis.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    const axisOption = {
      position: axis.position,
      title: axis.nameShow === false ? false : isEmpty(axis.name) ? false : axis.name,
      titleFontSize: axis.fontSize,
      titleFill: axis.color,
      ...this.getAxisLineStyle(chart, axis, true),
      lineLineDash,
      label: axis.axisLabel.show,
      labelOpacity: 1,
      labelFill: axis.axisLabel.color,
      labelFillOpacity: 1,
      labelFontSize: axis.axisLabel.fontSize,
      grid: axis.splitLine.show,
      gridStroke: axis.splitLine.lineStyle.color,
      gridStrokeOpacity: 1,
      gridLineWidth: axis.splitLine.lineStyle.width,
      gridLineDash,
      ...this.getAxisLabelStyle(axis),
      // 交由公共布局按真实标签尺寸等距抽稀，避免强保尾标签后挤出画布
      labelAutoHide: true,
      labelAutoRotate: false,
      labelFormatter: d => {
        return valueFormatter(d, axis.axisLabelFormatter)
      }
    }
    return axisOption
  }

  protected getOverlapGridFilter(axis: DeepPartial<ChartAxisStyle>): Partial<AxisComponent> {
    if (!axis?.show || !axis.axisLine?.show) {
      return {}
    }
    return {
      // 仅过滤与维度轴重合的数值轴边界网格线
      gridFilter: (_, index, values) =>
        axis.position === 'top' ? index < values.length - 1 : index > 0
    }
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setupSeriesColor(chart, data)
  }

  public setupSubSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    void chart
    void data
    return undefined
  }

  protected configEmptyDataStyle(newData, container, newChart?, content?) {
    configEmptyDataStyle(newData, container, newChart, content)
  }

  /**
   * 流式配置公共参数，处理常用的配置，后续如果有其他通用配置也可以放进来，需要单独配置的属性在各个图表自行实现。
   * @param chart 数据库图表对象。
   * @param options 各个图表的参数，泛化的 Options，可以自行扩展，比如加个扩展 X 轴或者扩展 Y 轴字段。
   */
  protected abstract setupOptions(chart: Chart, options: O, context?: Record<string, any>): O
  protected constructor(name: string, defaultData: any[]) {
    super(ChartLibraryType.G2, name, defaultData)
  }
}
