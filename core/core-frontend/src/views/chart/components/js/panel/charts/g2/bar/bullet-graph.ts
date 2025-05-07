import { Chart as G2Bullet } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { RuntimeOptions } from '@antv/g2/lib/api/runtime'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { getLineDash } from '@/views/chart/components/js/panel/common/common_antv'

const { t } = useI18n()

/**
 * 子弹图
 */
export class BulletGraph extends G2ChartView<RuntimeOptions, G2Bullet> {
  constructor() {
    super('bullet-graph', [])
  }

  axis: AxisType[] = [...BAR_AXIS_TYPE, 'yAxisExt', 'extBubble']
  axisConfig = {
    ...this['axisConfig'],
    xAxis: { name: `${t('chart.form_type')} / ${t('chart.dimension')}`, type: 'd', limit: 1 },
    yAxis: { name: `${t('chart.progress_current')} / ${t('chart.quota')}`, type: 'q', limit: 1 },
    yAxisExt: { name: `${t('chart.progress_target')} / ${t('chart.quota')}`, type: 'q', limit: 1 },
    extBubble: {
      name: `${t('chart.range_bg')} / ${t('chart.quota')}`,
      type: 'q',
      allowEmpty: true,
      limit: 1
    }
  }
  properties: EditorProperty[] = [
    ...BAR_EDITOR_PROPERTY.filter(
      item => !['function-cfg', 'assist-line', 'threshold'].includes(item)
    ),
    'bullet-graph-selector'
  ]
  propertyInner = {
    'basic-style-selector': ['radiusColumnBar', 'layout'],
    'label-selector': ['hPosition', 'fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'].filter(item => item != 'position'),
      'showLengthLimit'
    ],
    'y-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'].filter(
        item => item !== 'axisValue' && item !== 'position'
      ),
      'axisLabelFormatter'
    ],
    'legend-selector': ['showRange', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition']
  }

  async drawChart(drawOption: G2DrawOptions<G2Bullet>): Promise<G2Bullet> {
    const { chart, container, action } = drawOption
    if (!chart.data?.data?.length) return
    const result = mergeBulletData(chart)
    // 处理自定义区间
    const { bullet } = parseJson(chart.customAttr).misc
    if (bullet.bar.ranges.showType === 'fixed') {
      const customRange = bullet.bar.ranges.fixedRange?.map(item => item.fixedRangeValue) || [0]
      result.forEach(item => (item.ranges = customRange))
    } else {
      result.forEach(item => (item.ranges = item.originalRanges))
    }
    // 处理自定义目标值
    if (bullet.bar.target.showType === 'fixed') {
      const customTarget = bullet.bar.target.value || 0
      result.forEach(item => (item.target = customTarget))
    } else {
      result.forEach(item => (item.target = item.originalTarget))
    }
    const initOptions = {
      container,
      autoFit: true,
      data: result
    }
    const options = this.setupOptions(chart, initOptions)
    let newChart = null
    const { Chart: BulletClass } = await import('@antv/g2')
    newChart = new BulletClass(options)
    newChart.on('element:click', ev => {
      const pointData = ev?.data?.data
      const dimensionList = options.data.find(item => item.title === pointData.title)?.dimensionList
      const actionParams = {
        x: ev.x,
        y: ev.y,
        data: {
          data: {
            ...pointData,
            dimensionList
          }
        }
      }
      action(actionParams)
    })
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { radiusColumnBar, columnBarRightAngleRadius, layout } = basicStyle
    // 获取所有 interval 类型的子元素
    const intervals = options.children?.filter(item => item.type === 'interval') || []
    const intervalCount = intervals.length
    // 如果需要设置圆角，计算圆角值
    const radiusValue =
      radiusColumnBar === 'roundAngle' || radiusColumnBar === 'topRoundAngle'
        ? columnBarRightAngleRadius
        : 0
    // 更新每个 interval 的样式
    intervals.forEach((item, index) => {
      const updateStyle = (styleUpdates: Record<string, number>) => {
        item.style = { ...item.style, ...styleUpdates }
      }
      // 根据 radiusColumnBar 的值和索引位置，设置不同的圆角样式
      if (radiusColumnBar === 'topRoundAngle') {
        // 顶部圆角
        if (index === intervalCount - 2) {
          // 倒数第二个柱子（固定区间背景的最后一个）
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
        if (index === intervalCount - 1) {
          // 最后一个柱子（实际值）
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
      } else if (radiusColumnBar === 'roundAngle') {
        // 四角圆角
        if (index === 0) {
          // 第一个柱子
          updateStyle({ radiusBottomLeft: radiusValue, radiusBottomRight: radiusValue })
        }
        if (index === intervalCount - 2) {
          // 倒数第二个柱子（固定区间背景的最后一个）
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
        if (index === intervalCount - 1) {
          // 最后一个柱子
          updateStyle({ radiusBottomLeft: radiusValue, radiusBottomRight: radiusValue })
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
      }
    })
    // 如果布局是水平的，转置坐标系
    if (layout === 'horizontal') {
      options.coordinate = { transform: [{ type: 'transpose' }] }
    }
    return options
  }

  protected configMisc(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const { basicStyle } = parseJson(chart.customAttr)
    const { bullet } = parseJson(chart.customAttr).misc
    const isDynamic = bullet.bar.ranges.showType === 'dynamic'
    // 背景颜色，固定区间背景时，按大小降序
    const rangeColor = isDynamic
      ? chart.extBubble?.length
        ? bullet.bar.ranges.fill
        : []
      : bullet.bar.ranges.fixedRange
          ?.sort((a, b) => (a.fixedRangeValue ?? 0) - (b.fixedRangeValue ?? 0))
          .map(item => item.fill) || []
    const childrens = []
    // 固定区间背景
    const ranges = bullet.bar.ranges.fixedRange || []
    ranges.sort((a, b) => (a.fixedRangeValue ?? 0) - (b.fixedRangeValue ?? 0))
    ranges.forEach((item, index) => {
      // 用于配置区间边界， 存储当前区间的上一个区间
      const prev = ranges[index - 1]
      const range = {
        type: 'interval',
        encode: {
          x: 'title',
          y: [prev ? prev.fixedRangeValue : 0, item.fixedRangeValue],
          color: () => item.name
        },
        interaction: {
          legendFilter: false
        },
        style: {
          maxWidth: bullet.bar.ranges.size
        },
        tooltip: false
      }
      childrens.push(range)
    })
    if (isDynamic) {
      childrens.length = 0
      if (chart.extBubble?.length) {
        const rangeName = chart.extBubble[0]?.chartShowName || chart.extBubble[0]?.name
        childrens.push({
          type: 'interval',
          encode: {
            x: 'title',
            y: 'ranges',
            color: () => rangeName
          },
          interaction: {
            legendFilter: false
          },
          style: {
            maxWidth: bullet.bar.ranges.size
          },
          tooltip: false
        })
      }
    }
    // 实际值与目标值
    const measureName =
      chart.yAxis[0]?.chartShowName || bullet.bar.measures.name || chart.yAxis[0]?.name
    const measures = {
      type: 'interval',
      encode: {
        x: 'title',
        y: 'measures',
        color: () => measureName,
        shape: 'rect'
      },
      interaction: {
        legendFilter: false
      },
      style: {
        maxWidth: bullet.bar.measures.size
      },
      tooltip: {
        title: d => d.title,
        items: [{ channel: 'y' }]
      }
    }
    const targetName =
      chart.yAxisExt[0]?.chartShowName || bullet.bar.target.name || chart.yAxisExt[0]?.name
    const target = {
      type: 'point',
      encode: {
        x: 'title',
        y: 'target',
        color: () => targetName,
        shape: basicStyle.layout === 'horizontal' ? 'line' : 'hyphen',
        size: bullet.bar.target.size
      },
      interaction: {
        legendFilter: false
      },
      tooltip: {
        title: false,
        items: [{ channel: 'y' }]
      }
    }
    childrens.push(target)
    childrens.push(measures)
    options = {
      ...options,
      scale: {
        color: {
          range: [
            ...[].concat(rangeColor),
            ...[].concat(bullet.bar.target.fill),
            ...[].concat(bullet.bar.measures.fill)
          ]
        }
      },
      children: childrens
    }
    return options
  }

  protected configAxis(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    return { ...options, axis: { x: this.configXAxis(chart), y: this.configYAxis(chart) } }
  }

  protected configXAxis(chart: Chart): any {
    return this.getAxisConfig(chart, 'xAxis')
  }

  protected configYAxis(chart: Chart): any {
    return this.getAxisConfig(chart, 'yAxis')
  }

  protected getAxisConfig(chart: Chart, axisType: string) {
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle[axisType]))
    if (customStyle[axisType] && axis.show) {
      // 轴线
      const line = {
        line: axis.axisLine.show,
        lineLineWidth: axis.axisLine.lineStyle.width,
        lineStroke: axis.axisLine.lineStyle.color,
        lineLineDash: getLineDash(axis.axisLine.lineStyle.style)
      }
      // 刻度
      const tick = {
        tick: axis.axisLine.show,
        tickLineWidth: axis.axisLine.lineStyle.width,
        tickStroke: axis.axisLine.lineStyle.color
      }
      // 网格线
      const grid = {
        grid: axis.splitLine.show,
        gridLineWidth: axis.splitLine.lineStyle.width,
        gridStroke: axis.splitLine.lineStyle.color,
        gridStrokeOpacity: 1,
        gridLineDash: getLineDash(axis.splitLine.lineStyle.style)
      }
      // 标签（刻度值）
      const label = {
        label: axis.axisLabel.show,
        labelFontSize: axis.axisLabel.fontSize,
        labelFill: axis.axisLabel.color,
        labelFormatter: value => {
          if (axisType === 'yAxis') {
            return valueFormatter(value, axis.axisLabelFormatter)
          }
          return value
        }
      }
      // 刻度值旋转角度
      const labelTransform = {
        type: 'rotate',
        optionalAngles: [axis.axisLabel.rotate]
      }
      const x = {
        // 标题
        title: axis.nameShow && axis.name ? axis.name : false,
        titleFontSize: axis.fontSize,
        titleFill: axis.color,
        // 轴线
        ...line,
        // 刻度线
        ...tick,
        // 网格线
        ...grid,
        // 刻度值
        ...label,
        transform: [labelTransform]
      }
      return x
    }
    return false
  }

  protected configLegend(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const tmpOptions = { legend: this.getLegend(chart) }
    if (!tmpOptions.legend) {
      return { ...options, legend: false }
    }
    const { bullet } = parseJson(chart.customAttr).misc
    const { ranges } = bullet.bar
    const targetName =
      chart.yAxisExt[0]?.chartShowName || bullet.bar.target.name || chart.yAxisExt[0]?.name
    const baseLegend = tmpOptions.legend ? (tmpOptions.legend as any) : {}
    const tmpLegend = {
      color: {
        ...baseLegend,
        itemMarkerSize: ranges.symbolSize,
        itemMarker: d => {
          if (d === targetName) {
            return 'line'
          }
          return ranges.symbol
        }
      }
    }
    return { ...options, legend: tmpLegend }
  }

  protected configLabel(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const customAttr = parseJson(chart.customAttr)
    const { label: l } = customAttr
    // 如果没有启用标签，直接返回
    if (!l || !l.show) {
      return options
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { layout } = basicStyle
    const position = {
      position: l.position === 'middle' ? 'inside' : l.position,
      textAlign: 'left',
      dy: 0,
      dx: 0
    }
    if (layout !== 'horizontal') {
      ;(position.position =
        l.position === 'middle' ? 'inside' : l.position === 'left' ? 'bottom' : 'top'),
        (position.textAlign = 'center')
      position.dy = position.position === 'top' ? -10 : 0
    }
    // contrastReverse 标签颜色在图形背景上对比度低的情况下，从指定色板选择一个对比度最优的颜色
    // overlapDodgeY 对位置碰撞的标签在 y 方向上进行调整，防止标签重叠
    // exceedAdjust 自动对标签做溢出检测和矫正，即当标签超出视图区域时，会对标签自动做反方向的位移
    // overlapHide 对位置碰撞的标签进行隐藏，默认保留前一个，隐藏后一个
    const transform = {
      transform: [{ type: 'contrastReverse' }, { type: 'exceedAdjust' }, { type: 'overlapHide' }]
    }
    // 配置标签样式
    const label = {
      text: 'measures',
      fill: l.color,
      fontSize: l.fontSize,
      ...position,
      formatter: d => valueFormatter(d, l.labelFormatter),
      ...(l.fullDisplay ? {} : transform)
    }
    // 将标签配置应用到最后一个子元素（实际值）
    options.children?.forEach((item, index) => {
      if (index === options.children.length - 1) {
        item.labels = [label]
      }
    })
    return options
  }

  protected configTooltip(_chart: Chart, options: RuntimeOptions): RuntimeOptions {
    return {
      ...options,
      interaction: {
        tooltip: {
          shared: true,
          enterable: true
        }
      }
    }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label.position = 'middle'
    chart.customStyle.yAxis.splitLine.show = false
    chart.customAttr.misc.bullet.bar.ranges.symbolSize = 8
    chart.customAttr.misc.bullet.bar.target.symbolSize = 8
    chart.customAttr.misc.bullet.bar.measures.symbolSize = 8
    chart.customAttr.misc.bullet.bar.target.size = 8
    chart.customAttr.misc.bullet.bar.measures.symbol = 'square'
    chart.customAttr.misc.bullet.bar.ranges.symbol = 'square'
    return super.setupDefaultOptions(chart)
  }
  protected setupOptions(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    return flow(
      this.configMisc,
      this.configBasicStyle,
      this.configAxis,
      this.configLabel,
      this.configLegend,
      this.configTooltip
    )(chart, options, {}, this)
  }
}

/**
 * 组装子弹图数据
 * @param chart
 */
function mergeBulletData(chart): any[] {
  // 先根据维度分组，再根据指标字段组装成子弹图的格式
  const groupedData = chart.data.data.reduce((acc, item) => {
    const field = item.field
    if (!acc[field]) {
      acc[field] = []
    }
    acc[field].push(item)
    return acc
  }, {})
  const result = []
  // 组装子弹图数据，每个维度对应一个子弹图
  Object.keys(groupedData).forEach(field => {
    const items = groupedData[field]
    // 初始化子弹图条目结构
    const entry = {
      title: field,
      ranges: [],
      measures: [],
      target: [],
      dimensionList: items[0].dimensionList,
      quotaList: []
    }

    // 防止指标相同时无数据有可能会导致数据不一致
    items.forEach(item => {
      const quotaId = item.quotaList[0]?.id
      const v = item.value || 0
      if (quotaId === chart.yAxis[0]?.id) {
        entry.measures.push(v)
      }
      if (quotaId === chart.yAxisExt[0]?.id) {
        entry.target.push(v)
      }
      if (quotaId === chart.extBubble[0]?.id) {
        entry.ranges.push(v)
      }
      entry.quotaList.push(item.quotaList[0])
    })
    // 对数据进行累加
    const ranges = chart.extBubble[0]?.id
      ? [].concat(entry.ranges?.reduce((acc, curr) => acc + curr, 0))
      : []
    const target = [].concat(entry.target?.reduce((acc, curr) => acc + curr, 0))
    const measures = [].concat(entry.measures?.reduce((acc, curr) => acc + curr, 0))
    const bulletData = {
      ...entry,
      measures: measures,
      target: target,
      ranges: ranges,
      quotaList: [...entry.quotaList],
      minRanges: ranges,
      originalRanges: ranges,
      originalTarget: target
    }
    result.push(bulletData)
  })
  return result
}
