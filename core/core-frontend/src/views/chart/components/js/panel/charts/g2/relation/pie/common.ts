import { useI18n } from '@/hooks/web/useI18n'
import { parseJson } from '@/views/chart/components/js/util'
import type { G2Spec } from '@antv/g2'
import { LARGE_DATA_LABEL_MARK_KEY_PREFIX } from '../../../../types/impl/g2'

const { t } = useI18n()

export const PIE_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'border-style',
  'basic-style-selector',
  'title-selector',
  'legend-selector',
  'label-selector',
  'tooltip-selector',
  'jump-set',
  'linkage'
]
export const PIE_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'border-style': ['all'],
  'label-selector': [
    'fontSize',
    'color',
    'rPosition',
    'showDimension',
    'showQuota',
    'showProportion'
  ],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
  'basic-style-selector': ['colors', 'alpha', 'radius', 'seriesColor'],
  'title-selector': [
    'title',
    'fontSize',
    'color',
    'hPosition',
    'isItalic',
    'isBolder',
    'remarkShow',
    'fontFamily',
    'letterSpace',
    'fontShadow'
  ],
  'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition']
}

export const PIE_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'drill',
  'filter',
  'extLabel',
  'extTooltip'
]

export const PIE_AXIS_CONFIG: AxisConfig = {
  xAxis: {
    name: `${t('chart.drag_block_pie_label')} / ${t('chart.dimension')}`,
    type: 'd'
  },
  yAxis: {
    name: `${t('chart.drag_block_pie_angle')} / ${t('chart.quota')}`,
    type: 'q',
    limit: 1
  }
}

export const configSingleSectorScale = (options: G2Spec, data: any[]): G2Spec => {
  if (data.length !== 1) {
    return options
  }
  options.scale = {
    ...options.scale,
    x: {
      ...options.scale?.x,
      padding: 0,
      paddingInner: 0,
      paddingOuter: 0
    }
  }
  return options
}

// 外部标签布局常见保留约二十组左右的左右标签
const CIRCULAR_LABEL_RENDER_COUNT = 40
const CIRCULAR_LABEL_FILTER_FIELD = '__de_circular_label_visible__'

const sampleEvenlyByIndex = (dataLength: number, limit: number): Set<number> => {
  if (dataLength <= limit) {
    return new Set(Array.from({ length: dataLength }, (_, index) => index))
  }
  const step = (dataLength - 1) / (limit - 1)
  return new Set(Array.from({ length: limit }, (_, index) => Math.round(index * step)))
}

const sampleEvenlyByValue = (data: Record<string, any>[], limit: number): Set<number> => {
  const values = data.map(item => Math.max(0, Number(item.value) || 0))
  const total = values.reduce((sum, value) => sum + value, 0)
  if (total <= 0) {
    return sampleEvenlyByIndex(data.length, limit)
  }
  const midpoints: number[] = []
  let cumulative = 0
  values.forEach(value => {
    midpoints.push(cumulative + value / 2)
    cumulative += value
  })
  const indexes = new Set<number>()
  let currentIndex = 0
  for (let sampleIndex = 0; sampleIndex < limit; sampleIndex++) {
    const target = ((sampleIndex + 0.5) / limit) * total
    while (
      currentIndex < midpoints.length - 1 &&
      Math.abs(midpoints[currentIndex + 1] - target) < Math.abs(midpoints[currentIndex] - target)
    ) {
      currentIndex++
    }
    indexes.add(currentIndex)
  }
  return indexes
}

const getFieldDomain = (data: Record<string, any>[], field: unknown): unknown[] => {
  if (typeof field !== 'string') {
    return []
  }
  return Array.from(new Set(data.map(item => item[field])))
}

/**
 * 为高密度圆形图创建保留原扇区角度的有限标签载体
 *
 * 标签载体先在完整数据上执行 stackY，再按标记字段过滤，因此采样标签仍指向原扇区
 * 主 interval 继续绘制全部数据，点击、联动、Tooltip 和颜色配置均保持原样
 */
export const limitCircularChartLabels = (
  options: G2Spec,
  data: Record<string, any>[],
  angleByValue: boolean
): G2Spec => {
  if (
    options.type !== 'interval' ||
    !Array.isArray(options.labels) ||
    !options.labels.length ||
    data.length <= CIRCULAR_LABEL_RENDER_COUNT
  ) {
    return options
  }
  const sampledIndexes = angleByValue
    ? sampleEvenlyByValue(data, CIRCULAR_LABEL_RENDER_COUNT)
    : sampleEvenlyByIndex(data.length, CIRCULAR_LABEL_RENDER_COUNT)
  const labelData = data.map((item, index) => ({
    ...item,
    [CIRCULAR_LABEL_FILTER_FIELD]: sampledIndexes.has(index) ? 1 : 0
  }))
  const encode = options.encode as Record<string, any>
  const colorDomain = getFieldDomain(data, encode?.color)
  const xDomain = getFieldDomain(data, encode?.x)
  const labels = angleByValue
    ? options.labels
    : options.labels.map(label => ({
        ...label,
        transform: [
          ...(Array.isArray(label.transform) ? label.transform : []),
          { type: 'overlapHide' }
        ]
      }))
  const labelMark = {
    ...options,
    key: `${LARGE_DATA_LABEL_MARK_KEY_PREFIX}circular`,
    data:
      options.data && !Array.isArray(options.data) && typeof options.data === 'object'
        ? { ...options.data, value: labelData }
        : labelData,
    transform: [
      ...(Array.isArray(options.transform) ? options.transform : []),
      // 在 stackY 之后过滤，避免采样数据重新计算扇区角度
      { type: 'filter', opacity: value => value === 1 }
    ],
    encode: {
      ...encode,
      opacity: CIRCULAR_LABEL_FILTER_FIELD
    },
    labels,
    scale: {
      ...options.scale,
      ...(colorDomain.length && {
        color: { ...options.scale?.color, domain: colorDomain }
      }),
      ...(xDomain.length && {
        x: { ...options.scale?.x, domain: xDomain }
      })
    },
    // 仅关闭内部透明度通道图例，保留主图原有颜色图例
    legend: options.legend === false ? false : { ...options.legend, opacity: false },
    tooltip: false,
    animate: false,
    style: {
      ...options.style,
      fillOpacity: 0,
      strokeOpacity: 0,
      pointerEvents: 'none'
    }
  } as G2Spec
  delete labelMark.slider
  delete labelMark.interaction
  delete labelMark.state
  return {
    type: 'view',
    autoFit: options.autoFit,
    theme: options.theme,
    children: [{ ...options, labels: [] }, labelMark]
  }
}

// 饼图与玫瑰图共用同一套高密度外部标签降载逻辑，仅采样方式不同
export const createCircularLabelLayout =
  (angleByValue: boolean) =>
  (chart: Chart, options: G2Spec): G2Spec => {
    const { label } = parseJson(chart.customAttr)
    if (label.fullDisplay || label.position === 'inner') {
      return options
    }
    const data = options.data?.value
    return Array.isArray(data) ? limitCircularChartLabels(options, data, angleByValue) : options
  }
