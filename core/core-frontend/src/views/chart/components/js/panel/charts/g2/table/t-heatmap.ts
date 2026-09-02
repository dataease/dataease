import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { getG2Renderer, TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, toString } from 'lodash-es'
import { ChartEvent, Chart as G2Chart, extend, G2Spec, Runtime, stdlib } from '@antv/g2'
import { Text } from '@antv/g'
import { valueFormatter } from '../../../../formatter'
import { createTooltipWrapper } from '../bar/barUtil'

const { t } = useI18n()

const DEFAULT_DATA = []
// 标记已修正的图例实例，避免 G2 重绘复用对象时重复覆盖内部换算方法
const CONTINUOUS_LEGEND_RANGE_FIXED = Symbol('continuousLegendRangeFixed')

// 从当前图例实例读取实时数据域，避免数据刷新后继续使用旧的最大最小值
const getContinuousLegendDomain = legend => {
  const domain = legend?.attributes?.domain
  if (!Array.isArray(domain) || domain.length < 2) {
    return
  }
  const [min, max] = domain
  if (!Number.isFinite(min) || !Number.isFinite(max) || min > max) {
    return
  }
  return [min, max] as [number, number]
}

// 按数据量级计算浮点容差，用于识别经过运算后仍代表同一端点的数值
const isSameLegendValue = (current: number, target: number, min: number, max: number) => {
  if (!Number.isFinite(current) || !Number.isFinite(target)) {
    return false
  }
  const tolerance = Math.max(1, Math.abs(min), Math.abs(max)) * Number.EPSILON * 8
  return Math.abs(current - target) <= tolerance
}

// 将接近端点的结果吸附回原始值，同时阻止换算结果越过真实数据域
const snapLegendValue = (value: number, min: number, max: number) => {
  if (isSameLegendValue(value, min, min, max)) {
    return min
  }
  if (isSameLegendValue(value, max, min, max)) {
    return max
  }
  return Math.max(min, Math.min(max, value))
}

// 用全量区间探测 AntV 是否仍返回 [0, max - min]，防止上游修复后被二次补偿
const hasBrokenVerticalRange = (legend, getRealSelection) => {
  const domain = getContinuousLegendDomain(legend)
  if (!domain) {
    return false
  }
  const [min, max] = domain
  const fullRange = getRealSelection.call(legend, domain)
  if (!Array.isArray(fullRange) || fullRange.length < 2) {
    return false
  }
  const usesZeroBasedRange =
    isSameLegendValue(fullRange[0], 0, min, max) &&
    isSameLegendValue(fullRange[1], max - min, min, max)
  const alreadyMatchesDomain =
    isSameLegendValue(fullRange[0], min, min, max) && isSameLegendValue(fullRange[1], max, min, max)
  return usesZeroBasedRange && !alreadyMatchesDomain
}

// 同时修正图例筛选范围和悬浮指示值，使交互数据与图例标签保持同一数据域
const fixVerticalContinuousLegendRange = legend => {
  if (!legend || legend[CONTINUOUS_LEGEND_RANGE_FIXED]) {
    return
  }
  const getRealSelection = legend.getRealSelection
  const getRealValue = legend.getRealValue
  if (typeof getRealSelection !== 'function' || typeof getRealValue !== 'function') {
    return
  }
  legend.getRealSelection = function (range) {
    const selection = getRealSelection.call(this, range)
    const domain = getContinuousLegendDomain(this)
    if (!domain || !hasBrokenVerticalRange(this, getRealSelection)) {
      return selection
    }
    const [min, max] = domain
    // 旧实现缺少 min 偏移，将零基区间平移回真实数据域
    return [
      snapLegendValue(selection[0] + min, min, max),
      snapLegendValue(selection[1] + min, min, max)
    ]
  }
  legend.getRealValue = function (value) {
    const realValue = getRealValue.call(this, value)
    const domain = getContinuousLegendDomain(this)
    if (!domain || !hasBrokenVerticalRange(this, getRealSelection)) {
      return realValue
    }
    const [min, max] = domain
    // indicator 使用相同偏移规则，避免筛选正确但悬浮值仍少一个 min
    return snapLegendValue(realValue + min, min, max)
  }
  legend[CONTINUOUS_LEGEND_RANGE_FIXED] = true
}

// 复制 G2 标准组件注册表，仅在热力图 Runtime 内覆盖图例实现
const heatmapLibrary = stdlib() as Record<string, any>
// 固定图例显示方向，避免 G2 根据停靠位置自动切换水平或垂直布局
const withLegendOrientation = (component, fixContinuousRange = false) => {
  const customComponent = options => {
    // 私有方向配置不继续透传给原始 G2 图例组件
    const { dataeaseOrientation, ...rest } = options
    const positionVertical = rest.position === 'left' || rest.position === 'right'
    const directionMismatch = positionVertical !== (dataeaseOrientation === 'vertical')
    // 方向与停靠边交叉时恢复原组件短边，避免图例挤入绘图区
    const legendOptions = directionMismatch
      ? { ...rest, length: rest.length ?? component.props.defaultSize }
      : rest
    const renderComponent = component({
      ...legendOptions,
      style: {
        orientation: dataeaseOrientation,
        // 居中图例首帧先隐藏，避免布局完成前出现在错误位置
        opacity: rest.layout?.justifyContent === 'center' ? 0 : 1
      }
    })
    if (!fixContinuousRange || dataeaseOrientation !== 'vertical') {
      return renderComponent
    }
    // 在图例组件绑定交互前修正实例方法，不改变 G2 全局组件库
    return context => {
      const layout = renderComponent(context)
      const continuousLegend = layout?.children?.find(
        child => typeof child?.getRealSelection === 'function'
      )
      fixVerticalContinuousLegendRange(continuousLegend)
      return layout
    }
  }
  // 保留默认尺寸等静态配置，保证包装后仍参与 G2 布局计算
  customComponent.props = component.props
  return customComponent
}
// 分类图例和连续色带图例使用相同的方向修正规则
heatmapLibrary['component.legendCategory'] = withLegendOrientation(
  heatmapLibrary['component.legendCategory']
)
// 仅热力图的连续图例启用非零最小值兼容逻辑，分类图例保持原行为
heatmapLibrary['component.legendContinuous'] = withLegendOrientation(
  heatmapLibrary['component.legendContinuous'],
  true
)
// 创建热力图专用 G2 构造器，避免修改全局组件库影响其他图表
const HeatmapG2Chart = extend(Runtime, heatmapLibrary) as typeof G2Chart
/**
 * 热力图
 */
export class TableG2Chart extends G2ChartView {
  legendCapabilities: LegendCapabilities = {
    orient: true,
    type: 'dynamic',
    source: 'native'
  }
  properties: EditorProperty[] = [
    'basic-style-selector',
    'background-overall-component',
    'label-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'tooltip-selector',
    'jump-set',
    'linkage',
    'border-style'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors'],
    'label-selector': ['fontSize', 'color', 'labelFormatter'],
    'x-axis-selector': ['name', 'color', 'fontSize', 'position', 'axisLabel', 'axisLine'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'position',
      'axisLabel',
      'axisLine',
      'showLengthLimit'
    ],
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
    'legend-selector': ['orient', 'color', 'fontSize', 'hPosition', 'vPosition'],
    'tooltip-selector': ['show', 'color', 'fontSize', 'backgroundColor', 'tooltipFormatter'],
    'border-style': ['all']
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'extColor', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.x_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    xAxisExt: {
      name: `${t('chart.y_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    extColor: {
      name: `${t('chart.color')} / ${t('chart.dimension_or_quota')}`,
      limit: 1
    }
  }
  protected getDefaultLength = (chart, l) => {
    const containerDom = document.getElementById(chart.container)
    const containerHeight = containerDom?.clientHeight || 100
    const containerWidth = containerDom?.clientWidth || 100
    let defaultLength = containerHeight - containerHeight * 0.5
    if (l.orient !== 'vertical') {
      defaultLength = containerWidth - containerWidth * 0.5
    }
    return defaultLength
  }
  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    const { xAxis, xAxisExt, extColor } = chart
    if (!xAxis?.length || !xAxisExt?.length || !extColor?.length) {
      return
    }
    const xField = xAxis[0].dataeaseName
    const xFieldExt = xAxisExt[0].dataeaseName
    const extColorField = extColor[0].dataeaseName
    // data
    const tableRow = chart.data.tableRow
    // G2 编码要求数组，中间态数据未就绪时跳过本轮渲染
    if (!Array.isArray(tableRow)) {
      return
    }
    // 空维度或空颜色值不绘制，数值 0 仍作为有效数据保留
    const data = tableRow
      .filter(item =>
        [xField, xFieldExt, extColorField].every(
          field => item[field] !== null && item[field] !== undefined && item[field] !== ''
        )
      )
      .map(item => ({
        ...item,
        name: item[xField],
        category: item[xFieldExt]
      }))
    // options
    const initOptions: G2Spec = {
      type: 'cell',
      autoFit: true,
      data: {
        value: data
      },
      encode: {
        x: xField,
        y: xFieldExt,
        color: extColorField
      }
    }
    const axisMap = {
      [chart.xAxis[0].dataeaseName]: chart.xAxis[0].chartShowName ?? chart.xAxis[0].name,
      [chart.xAxisExt[0].dataeaseName]: chart.xAxisExt[0].chartShowName ?? chart.xAxisExt[0].name,
      [chart.extColor[0].dataeaseName]: chart.extColor[0].chartShowName ?? chart.extColor[0].name
    }
    chart.container = container
    const options = this.setupOptions(chart, initOptions, { axisMap, container })
    const newChart = new HeatmapG2Chart({ container, ...getG2Renderer() })
    newChart.options(options)
    newChart.on('plot:click', param => {
      if (!param?.target?.__data__?.data) {
        return
      }
      const pointData = param.target.__data__.data
      const dimensionList = []
      chart.data.fields.forEach(item => {
        Object.keys(pointData).forEach(key => {
          if (key.startsWith('f_') && item.dataeaseName === key) {
            dimensionList.push({
              id: item.id,
              dataeaseName: item.dataeaseName,
              value: pointData[key]
            })
          }
        })
      })
      if (dimensionList.length === 0) {
        return
      }
      action({
        x: param.x,
        y: param.y,
        data: {
          data: {
            ...pointData,
            value: dimensionList[1]?.value,
            dimensionList: dimensionList,
            quotaList: [dimensionList[1]]
          }
        }
      })
    })
    this.configYAxisLabelLimit(newChart, chart)
    return newChart
  }

  protected configYAxisLabelLimit(chartObj: G2Chart, chart: Chart) {
    const { yAxis } = parseJson(chart.customStyle)
    if (!yAxis.show || !yAxis.axisLabel.show || !yAxis.axisLabel.lengthLimit) {
      return
    }
    const { tooltip } = parseJson(chart.customAttr)
    const labelTipId = `AXIS_LABEL_TIP-${chart.id}`
    chartObj.on(`axis-label-item:${ChartEvent.POINTER_OVER}`, e => {
      const target = e.target
      const { text, originValue } = target.attributes
      if (!originValue) {
        return
      }
      if (text !== originValue) {
        let parentDom = document.getElementById('G2-TOOLTIP-WRAPPER')
        if (!parentDom) {
          parentDom = document.createElement('div')
          parentDom.id = 'G2-TOOLTIP-WRAPPER'
          parentDom.style.position = 'absolute'
          parentDom.style.pointerEvents = 'none'
          parentDom.style.zIndex = '9999'
          document.body.appendChild(parentDom)
        }
        let labelTipDom = document.getElementById(labelTipId)
        if (!labelTipDom) {
          labelTipDom = document.createElement('div')
          labelTipDom.id = labelTipId
          labelTipDom.style.position = 'fixed'
          labelTipDom.style.color = tooltip.color
          labelTipDom.style.backgroundColor = tooltip.backgroundColor
          labelTipDom.style.fontSize = `${tooltip.fontSize}px`
          labelTipDom.style.padding = '5px 20px'
          labelTipDom.style.boxShadow = 'rgba(0, 0, 0, 0.12) 0px 6px 12px 0px'
          labelTipDom.style.borderRadius = '4px'
          labelTipDom.style.transition =
            'visibility 0.2s cubic-bezier(0.23, 1, 0.32, 1), left 0.4s cubic-bezier(0.23, 1, 0.32, 1), top 0.4s cubic-bezier(0.23, 1, 0.32, 1)'
          parentDom.appendChild(labelTipDom)
        }
        labelTipDom.innerText = originValue
        labelTipDom.style.visibility = 'visible'
        labelTipDom.style.left = `${e.client.x + 30}px`
        labelTipDom.style.top = `${e.client.y + 20}px`
      }
    })
    chartObj.on(`axis-label-item:${ChartEvent.POINTER_OUT}`, e => {
      const target = e.target
      const { originValue } = target.attributes
      if (!originValue) {
        return
      }
      const labelTipDom = document.getElementById(labelTipId)
      if (labelTipDom) {
        labelTipDom.style.visibility = 'hidden'
      }
    })
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    return { ...options, theme }
  }

  protected configLegend(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    const colorField = chart.extColor[0]
    const colors = options.theme.category10
    if (colorField.groupType === 'q') {
      const colorQuotaScale = {
        scale: {
          color: {
            type: 'linear',
            // 不扩展数据范围，图例刻度直接使用实际最大最小值
            nice: false,
            tickMethod: (min, max) => [min, max],
            interpolate() {
              return c => {
                if (isNaN(c)) return colors[0]
                const t = Math.max(0, Math.min(1, c))
                return colors[Math.floor(t * (colors.length - 1))]
              }
            }
          }
        }
      }
      defaultsDeep(options, colorQuotaScale)
    }
    if (!legend.show) {
      return { ...options, legend: false }
    }
    // 按 V2 语义分别计算位置和方向，位置变化不触发 G2 重新推断方向
    const verticalLegend = legend.orient === 'vertical'
    const centerHorizontal = legend.hPosition === 'center'
    const centerVertical = legend.vPosition === 'center'
    const position = centerHorizontal
      ? centerVertical
        ? 'top'
        : legend.vPosition
      : centerVertical || verticalLegend
      ? legend.hPosition
      : legend.vPosition
    const alignPosition = position === legend.hPosition ? legend.vPosition : legend.hPosition
    const positionVertical = position === 'left' || position === 'right'
    const directionMismatch = positionVertical !== verticalLegend
    const { container } = context
    const containerDom = document.getElementById(container)
    if (!containerDom) return
    const baseLegend = {
      ...this.getLegend(chart, colorField.groupType === 'q' ? 1 : 2),
      position,
      dataeaseOrientation: legend.orient,
      // 显式写入两个方向，避免同一图表切换配置后复用旧的水平网格标记
      ...(positionVertical
        ? { dataeaseLegendOrientLayout: verticalLegend ? 'vertical' : 'horizontal' }
        : {}),
      layout: {
        justifyContent:
          alignPosition === 'left' || alignPosition === 'top'
            ? 'flex-start'
            : alignPosition === 'right' || alignPosition === 'bottom'
            ? 'flex-end'
            : 'center'
      },
      maxCols: verticalLegend ? 1 : undefined,
      maxRows: verticalLegend ? undefined : 1,
      ...(directionMismatch
        ? {
            size: this.getDefaultLength(chart, legend)
          }
        : {})
    }
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend,
          title: false
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    if (colorField.groupType === 'q') {
      const quotaLegendOption = {
        legend: {
          color: {
            color: colors,
            // 连续图例显示比例尺生成的最大最小值
            label: true,
            labelFill: legend.color,
            labelFillOpacity: 1,
            labelFontSize: legend.fontSize
          }
        }
      }
      if (verticalLegend) {
        quotaLegendOption.legend.color.height = containerDom?.offsetHeight / 2
      } else {
        quotaLegendOption.legend.color.width = containerDom?.offsetWidth / 2
      }
      defaultsDeep(options, quotaLegendOption)
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const colorField = chart.extColor[0].dataeaseName
    const labelStyle = {
      labels: [
        {
          text: d => toString(valueFormatter(d[colorField], label.labelFormatter)),
          position: 'inside',
          style: {
            fill: label.color,
            fillOpacity: 1,
            fontSize: label.fontSize,
            // 标签不参与命中测试，鼠标事件继续落到热力单元
            pointerEvents: 'none'
          },
          transform: label.fullDisplay ? [] : [{ type: 'overflowHide' }]
        }
      ]
    }
    return defaultsDeep(options, labelStyle)
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const { axisMap } = context
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: {
            '.g2-tooltip': {
              background: tooltip.backgroundColor
            },
            '.g2-tooltip-title': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            },
            '.g2-tooltip-list-item-name-label': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            },
            '.g2-tooltip-list-item-value': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            }
          },
          render: (_, { items }) => {
            const xField = chart.xAxis[0].dataeaseName
            const yField = chart.xAxisExt[0].dataeaseName
            const colorField = chart.extColor[0].dataeaseName
            const head = items[0]
            const title = head[xField]
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const result = [
              { marker: head.color, label: axisMap[yField], value: head[yField] },
              {
                marker: head.color,
                label: axisMap[colorField],
                value: valueFormatter(head[colorField], tooltip.tooltipFormatter)
              }
            ]
            const itemsHtml = result
              .map(({ marker, label, value }) => {
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return defaultsDeep(options, tooltipOptions)
  }

  protected configXAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis } = parseJson(chart.customStyle)
    if (!xAxis.show) {
      const axisHide = {
        axis: {
          x: false
        }
      }
      return defaultsDeep(options, axisHide)
    }
    let lineLineDash = undefined
    if (xAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (xAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    const axisStyle = {
      axis: {
        x: {
          position: xAxis.position,
          title: xAxis.nameShow === false ? false : xAxis.name,
          titleFontSize: xAxis.fontSize,
          titleFill: xAxis.color,
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelFontSize: xAxis.axisLabel.fontSize,
          ...this.getAxisLabelStyle(xAxis)
        }
      }
    }
    return defaultsDeep(options, axisStyle)
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const { yAxis } = parseJson(chart.customStyle)
    if (!yAxis.show) {
      const axisHide = {
        axis: {
          y: false
        }
      }
      return defaultsDeep(options, axisHide)
    }
    let lineLineDash = undefined
    if (yAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (yAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    const axisStyle = {
      axis: {
        y: {
          position: yAxis.position,
          title: yAxis.nameShow === false ? false : yAxis.name,
          titleFontSize: yAxis.fontSize,
          titleFill: yAxis.color,
          ...this.getAxisLineStyle(chart, yAxis),
          lineLineDash,
          label: yAxis.axisLabel.show,
          labelFill: yAxis.axisLabel.color,
          labelFontSize: yAxis.axisLabel.fontSize,
          ...this.getAxisLabelStyle(yAxis),
          labelFormatter: d => {
            const str = toString(d)
            if (!str) {
              return ''
            }
            const lengthLimit = yAxis.axisLabel.lengthLimit
            if (lengthLimit) {
              const strLength = str.length
              if (strLength > lengthLimit) {
                const ellipsisText = str.substring(0, lengthLimit) + '...'
                return new Text({
                  style: {
                    text: ellipsisText,
                    originValue: `${d}`
                  }
                })
              }
            }
            return d
          }
        }
      }
    }
    return defaultsDeep(options, axisStyle)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.legend.orient = 'vertical'
    chart.customStyle.legend.vPosition = 'center'
    chart.customStyle.legend.hPosition = 'right'
    chart.customStyle.legend['rail'] = { defaultLength: 100 }
    return chart
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configLegend,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis
    )(chart, options, context, this)
  }

  constructor() {
    super('t-heatmap', DEFAULT_DATA)
  }
}
