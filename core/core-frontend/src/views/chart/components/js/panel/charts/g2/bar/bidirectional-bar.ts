import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  getLineConditions,
  getLineLabelColorByCondition,
  hexColorToRGBA,
  hexToRgba,
  parseJson
} from '@/views/chart/components/js/util'
import { defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import {
  configXAxisLengthLimit,
  formatAxisLabelWithLengthLimit,
  handleChartDashboardHidden,
  setGradientColor,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'

const { t } = useI18n()

/**
 * 对称柱状图
 */
export class BidirectionalHorizontalBar extends G2ChartView {
  /**
   * 保存每个图表实例首次渲染后的布局校正任务
   *
   * 对称条形图的布局校正依赖 G2 首次 render 后生成的 view.layout，不能在 options 阶段提前计算
   * 图表视图对象会被多个组件复用，因此任务必须按 G2Chart 实例隔离，避免并发渲染时串用其他图表的配置
   * WeakMap 不会阻止已销毁的图表实例被回收，并且任务执行后会立即删除
   */
  private readonly afterRenderHandlers = new WeakMap<G2Chart, () => Promise<void>>()

  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_axis_ext')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'yAxisExt', 'filter', 'drill', 'extLabel', 'extTooltip']
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'x-axis-selector',
    'dual-y-axis-selector',
    'title-selector',
    'legend-selector',
    'label-selector',
    'tooltip-selector',
    'function-cfg',
    'jump-set',
    'linkage',
    'threshold'
  ]
  propertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient', 'layout', 'radiusColumnBar'],
    'x-axis-selector': ['position', 'axisLabel', 'axisLine', 'splitLine'],
    'dual-y-axis-selector': [
      'name',
      'position',
      'color',
      'fontSize',
      'axisLabel',
      'showLengthLimit',
      'axisLine',
      'splitLine',
      'axisValue',
      'axisLabelFormatter'
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
    'legend-selector': ['icon', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'function-cfg': ['emptyDataStrategy'],
    'label-selector': ['hPosition', 'vPosition', 'seriesLabelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    threshold: ['lineThreshold']
  }

  selectorSpec: EditorSelectorSpec = {
    ...this['selectorSpec'],
    'dual-y-axis-selector': {
      title: `${t('chart.xAxis')}`
    },
    'x-axis-selector': {
      title: `${t('chart.yAxis')}`
    }
  }

  private getChartOptions(options: G2Spec) {
    // 图例会插入外层 children，按 key 定位真实图表层
    return options.children?.find(child => child.key === 'chart')
  }

  private getChartMarks(options: G2Spec) {
    return this.getChartOptions(options)?.children || []
  }

  private getValueAxis(axis: DeepPartial<ChartAxisStyle>) {
    const axisOption = this.getAxis(axis)
    const originLabelFormatter = axisOption.labelFormatter
    // 两个数值轴都在数值格式化后按各自配置截断刻度文本
    axisOption.labelFormatter = value => {
      const label = typeof originLabelFormatter === 'function' ? originLabelFormatter(value) : value
      return formatAxisLabelWithLengthLimit(label, axis.axisLabel.lengthLimit)
    }
    // 数值先按轴格式化配置生成文本，再由 G2 测量边界并分配轴空间
    delete axisOption.transform
    return {
      ...axisOption,
      labelAutoHide: true,
      labelAutoRotate: false
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    chart.container = container
    const [firstData, secondData] = chart.data.data
    const initOptions: G2Spec = {
      autoFit: true,
      type: 'spaceFlex',
      direction: 'col',
      ratio: [1],
      children: [
        {
          type: 'spaceFlex',
          key: 'chart',
          direction: 'row',
          ratio: [1, 1],
          children: [
            {
              key: 'first',
              type: 'interval',
              data: {
                value: firstData.data
              },
              encode: {
                x: 'field',
                y: 'value',
                color: {
                  type: 'constant',
                  value: firstData.name
                }
              },
              legend: false,
              coordinate: {
                transform: [{ type: 'transpose' }]
              },
              axis: {
                x: {
                  title: false
                }
              }
            },
            {
              key: 'second',
              type: 'interval',
              data: {
                value: secondData.data
              },
              encode: {
                x: 'field',
                y: 'value',
                color: {
                  type: 'constant',
                  value: secondData.name
                }
              },
              legend: false,
              coordinate: {
                transform: [{ type: 'transpose' }]
              },
              axis: {
                x: {
                  title: false
                }
              }
            }
          ]
        }
      ]
    }
    const newChart = new G2Chart({ container })
    const options = this.setupOptions(chart, initOptions)
    const { basicStyle } = parseJson(chart.customAttr)
    const { xAxis } = parseJson(chart.customStyle)
    const [firstMark, secondMark] = this.getChartMarks(options)
    // 将依赖首次布局结果的修正注册为可等待任务，不在 AFTER_RENDER 事件中启动无法被外层感知的异步重绘
    this.afterRenderHandlers.set(newChart, async () => {
      let reRenderMark = false
      if (
        basicStyle.layout === 'vertical' &&
        firstMark.axis?.y?.position === secondMark.axis?.y?.position &&
        ((firstMark.axis?.y?.title === false && secondMark.axis?.y?.title === false) ||
          (firstMark.axis?.y?.title && secondMark.axis?.y?.title))
      ) {
        // 垂直布局上下指标尺度不一致时柱子没对齐，手动处理 padding
        const [first, second] = newChart.getContext().views.filter(c => c.key !== 'legends')
        const paddingAttr = firstMark.axis?.y?.position === 'left' ? 'paddingLeft' : 'paddingRight'
        const { [paddingAttr]: firstPadding } = first.layout
        const { [paddingAttr]: secondPadding } = second.layout
        const offsetPadding = Math.abs(firstPadding - secondPadding)
        if (offsetPadding > 1) {
          const [minView] = [first, second].sort((a, b) => {
            return a.layout[paddingAttr] - b.layout[paddingAttr]
          })
          const minMark = newChart.children
            .find(c => c.value.key === 'chart')
            .children.find(c => c.value.key === minView.key)
          minMark.attr(paddingAttr, minView.layout[paddingAttr] + offsetPadding)
          reRenderMark = true
        }
      }
      if (xAxis.show && xAxis.axisLabel?.show && xAxis.position === 'bottom') {
        // 处理维度轴标签居中
        const [first, second] = newChart.getContext().views.filter(c => c.key !== 'legends')
        if (basicStyle.layout === 'horizontal') {
          const firstEmptySpace =
            first.layout.paddingRight + first.layout.marginRight + first.layout.insetRight
          const secondEmptySpace = second.layout.paddingLeft + second.layout.insetLeft
          const emptySpace = firstEmptySpace + secondEmptySpace
          const labelDx = emptySpace / 2
          const firstMark = newChart.children
            .find(c => c.value.key === 'chart')
            .children.find(c => c.value.key === 'first')
          const xAxisAttr = firstMark.value.axis?.x
          firstMark.axis('x', merge({}, xAxisAttr, { labelDx, labelTextAlign: 'center' }))
          reRenderMark = true
        }
        if (basicStyle.layout === 'vertical') {
          const firstEmptySpace =
            first.layout.paddingBottom + first.layout.marginBottom + first.layout.insetBottom
          const secondEmptySpace = second.layout.paddingTop + second.layout.insetTop
          const emptySpace = firstEmptySpace + secondEmptySpace
          const labelDy = emptySpace / 2
          const firstMark = newChart.children
            .find(c => c.value.key === 'chart')
            .children.find(c => c.value.key === 'first')
          const xAxisAttr = firstMark.value.axis?.x
          firstMark.axis('x', merge({}, xAxisAttr, { labelDy, labelTextBaseline: 'middle' }))
          reRenderMark = true
        }
      }
      if (reRenderMark) {
        handleChartDashboardHidden(chart, newChart)
        // 必须等待校正后的图形元素生成完成，联动 selected/unselected 状态才能应用到最终元素
        await newChart.render()
      }
    })
    newChart.on('interval:click', action)
    configXAxisLengthLimit(chart, newChart)
    // 开始渲染
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    return newChart
  }

  public async afterRender(chart: G2Chart): Promise<void> {
    // 只获取当前实例对应的任务，其他对称条形图实例及其他图表不会受本次布局校正影响
    const handler = this.afterRenderHandlers.get(chart)
    if (!handler) {
      return
    }
    // 布局任务只应在首次渲染后执行一次，先删除可避免后续流程意外重复触发二次 render
    this.afterRenderHandlers.delete(chart)
    // 将二次 render 的完成时机传递给公共渲染组件，确保事件状态恢复发生在稳定画布上
    await handler()
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const chartOptions = this.getChartOptions(options)
    const [firstMark, secondMark] = this.getChartMarks(options)
    const basicStyle = parseJson(chart.customAttr).basicStyle
    let [firstColor, secondColor] = basicStyle.colors
    firstColor = hexColorToRGBA(firstColor, basicStyle.alpha)
    secondColor = hexColorToRGBA(secondColor, basicStyle.alpha)
    defaultsDeep(firstMark, {
      scale: {
        color: {
          type: 'ordinal',
          domain: [firstMark.encode.color.value],
          range: [firstColor]
        },
        y: {
          nice: true,
          range: [0, 1]
        }
      }
    })
    defaultsDeep(secondMark, {
      scale: {
        color: {
          type: 'ordinal',
          domain: [secondMark.encode.color.value],
          range: [secondColor]
        },
        y: {
          nice: true
        }
      }
    })
    if (basicStyle.layout === 'vertical') {
      chartOptions.direction = 'col'
      delete firstMark.scale.y.range
      delete firstMark.coordinate
      delete secondMark.coordinate
      defaultsDeep(secondMark, {
        scale: {
          y: {
            nice: true,
            range: [0, 1]
          }
        }
      })
    }
    if (basicStyle.gradient) {
      let firstAngle = 180
      if (basicStyle.layout === 'vertical') {
        firstAngle = 270
      }
      firstColor = setGradientColor(firstColor, true, firstAngle)
      secondColor = setGradientColor(secondColor, true, firstAngle - 180)
    }
    if (basicStyle.radiusColumnBar === 'roundAngle') {
      defaultsDeep(firstMark, {
        style: {
          radius: 20
        }
      })
      defaultsDeep(secondMark, {
        style: {
          radius: 20
        }
      })
    }
    if (basicStyle.radiusColumnBar === 'topRoundAngle') {
      if (basicStyle.layout === 'vertical') {
        defaultsDeep(firstMark, {
          style: {
            radiusTopLeft: 20,
            radiusTopRight: 20
          }
        })
        defaultsDeep(secondMark, {
          style: {
            radiusBottomLeft: 20,
            radiusBottomRight: 20
          }
        })
      } else {
        defaultsDeep(firstMark, {
          style: {
            radiusBottomLeft: 20,
            radiusBottomRight: 20
          }
        })
        defaultsDeep(secondMark, {
          style: {
            radiusTopLeft: 20,
            radiusTopRight: 20
          }
        })
      }
    }
    defaultsDeep(firstMark, {
      style: {
        fill: d => {
          if (d.value === null || d.value === undefined) {
            return 'transparent'
          }
          return firstColor
        }
      }
    })
    defaultsDeep(secondMark, {
      style: {
        fill: d => {
          if (d.value === null || d.value === undefined) {
            return 'transparent'
          }
          return secondColor
        }
      }
    })
    return options
  }

  protected configXAxis(chart: Chart, options: G2Spec): G2Spec {
    const [firstMark, secondMark] = this.getChartMarks(options)
    const { xAxis } = parseJson(chart.customStyle)
    const { basicStyle } = parseJson(chart.customAttr)
    if (!xAxis.show) {
      firstMark.axis.x = false
      secondMark.axis.x = false
      return options
    }
    let lineLineDash = undefined
    if (xAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (xAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (xAxis.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (xAxis.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    let position = 'right'
    if (basicStyle.layout === 'vertical') {
      if (xAxis.position === 'top') {
        position = 'top'
      } else {
        position = 'bottom'
      }
    } else {
      if (xAxis.position === 'top') {
        position = 'left'
      }
    }
    // G2 默认轴组件会按完整标签宽度预留空间，横向对称条形图只需要按文本宽度估算中间轴占位
    const labelFontSize = xAxis.axisLabel.fontSize ?? 12
    const formatXAxisLabelText = value => {
      const label = `${value ?? ''}`
      const lengthLimit = xAxis.axisLabel.lengthLimit
      return lengthLimit && label.length > lengthLimit
        ? label.substring(0, lengthLimit) + '...'
        : label
    }
    const formatXAxisLabel = value => {
      const originLabel = `${value ?? ''}`
      return formatAxisLabelWithLengthLimit(originLabel, xAxis.axisLabel.lengthLimit)
    }
    const getLabelTextWidth = text => {
      return Array.from(`${text ?? ''}`).reduce((width, char) => {
        return width + (char.charCodeAt(0) > 255 ? labelFontSize : labelFontSize * 0.6)
      }, 0)
    }
    let centerAxisSize: number
    if (basicStyle.layout === 'horizontal' && position === 'right' && xAxis.axisLabel.show) {
      const fields = (firstMark.data?.value || []).map(item => item.field)
      const maxLabelWidth = fields.reduce((maxWidth, field) => {
        return Math.max(maxWidth, getLabelTextWidth(formatXAxisLabelText(field)))
      }, 0)
      // 中间维度轴只需要左右各预留半个标签宽度和少量间距，避免 G2 默认轴宽把两侧空白撑大
      centerAxisSize = Math.ceil(Math.max(labelFontSize + 8, maxLabelWidth / 2 + 8))
    }
    const axisStyle = {
      axis: {
        x: {
          zIndex: 1,
          position: position,
          size: centerAxisSize,
          crossPadding: centerAxisSize ? 2 : undefined,
          padding: centerAxisSize ? 0 : undefined,
          line: xAxis.axisLine.show,
          lineStroke: xAxis.axisLine.lineStyle.color,
          lineStrokeOpacity: 1,
          lineLineWidth: xAxis.axisLine.lineStyle.width,
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelFontSize: xAxis.axisLabel.fontSize,
          tick: xAxis.axisLabel.show,
          tickLineWidth: xAxis.axisLine.lineStyle.width,
          tickStroke: xAxis.axisLine.lineStyle.color,
          tickOpacity: 1,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...this.getAxisLabelStyle({ ...xAxis, position }),
          transform: [
            {
              type: 'hide',
              keepHeader: true,
              keepTail: true
            }
          ],
          labelFormatter: formatXAxisLabel
        }
      }
    }
    defaultsDeep(firstMark, axisStyle)
    const POSITION_MAP = {
      left: 'left',
      right: 'left',
      top: 'top',
      bottom: 'top'
    }
    const reserveHiddenCenterLabel =
      basicStyle.layout === 'horizontal' && position === 'right' && xAxis.axisLabel.show
    // 根因是维度轴标签实际挂在左侧子图上，右侧如果完全隐藏该轴，左右绘图区宽度会不一致
    const secondXAxis = {
      label: false,
      tick: xAxis.axisLabel.show && ['right', 'bottom'].includes(position),
      position: POSITION_MAP[position],
      line: xAxis.axisLine.show && ['right', 'bottom'].includes(position)
    }
    if (reserveHiddenCenterLabel) {
      // 横向布局的维度轴标签显示在左右图中间，右侧子图也保留一份不可见标签空间，避免左侧因承载标签而绘图区变窄
      merge(secondXAxis, {
        label: true,
        labelOpacity: 0,
        labelFillOpacity: 0
      })
    }
    merge(secondMark, axisStyle, {
      axis: {
        x: secondXAxis
      }
    })
    if (position === 'left') {
      defaultsDeep(firstMark, {
        insetRight: 0,
        paddingRight: 0,
        marginRight: 0
      })
      defaultsDeep(secondMark, {
        insetLeft: 0,
        paddingLeft: 0,
        marginLeft: 0
      })
    }
    if (position === 'top') {
      defaultsDeep(firstMark, {
        insetBottom: 0,
        paddingBottom: 0,
        marginBottom: 0
      })
      defaultsDeep(secondMark, {
        insetTop: 0,
        paddingTop: 0,
        marginTop: 0
      })
    }
    return options
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const [firstMark, secondMark] = this.getChartMarks(options)
    const { yAxis, yAxisExt } = parseJson(chart.customStyle)
    const { basicStyle } = parseJson(chart.customAttr)
    if (!yAxis.show) {
      firstMark.axis.y = false
      secondMark.axis.y = false
      return options
    }
    const yAxisOption = this.getValueAxis(yAxis)
    const yAxisExtOption = this.getValueAxis(yAxisExt)
    if (
      yAxisOption.label &&
      yAxisExtOption.label &&
      yAxisOption.position === yAxisExtOption.position &&
      ['left', 'top'].includes(firstMark.axis?.x?.position)
    ) {
      defaultsDeep(yAxisExtOption, {
        tickFilter: (_, i) => i !== 0
      })
    }
    if (basicStyle.layout === 'horizontal') {
      const POSITION_MAP = {
        left: 'bottom',
        right: 'top'
      }
      merge(yAxisOption, { position: POSITION_MAP[yAxis.position] })
      merge(yAxisExtOption, { position: POSITION_MAP[yAxisExt.position] })
      merge(yAxisOption, this.getAxisLabelStyle({ ...yAxis, position: yAxisOption.position }))
      merge(
        yAxisExtOption,
        this.getAxisLabelStyle({ ...yAxisExt, position: yAxisExtOption.position })
      )
    }
    if (yAxis.axisValue.auto === false) {
      merge(firstMark, {
        scale: {
          y: {
            nice: false,
            domain: [yAxis.axisValue.min, yAxis.axisValue.max]
          }
        },
        encode: {
          y1: {
            type: 'constant',
            value: yAxis.axisValue.min
          }
        },
        axis: {
          y: {
            tickCount: yAxis.axisValue.splitCount < 2 ? 2 : yAxis.axisValue.splitCount,
            tickMethod: (min, max, count) => {
              const step = (max - min) / (count - 1)
              const ticks = []
              for (let i = 0; i < count; i++) {
                ticks.push(min + i * step)
              }
              return ticks
            }
          }
        }
      })
    }
    if (yAxisExt.axisValue.auto === false) {
      merge(secondMark, {
        scale: {
          y: {
            nice: false,
            domain: [yAxisExt.axisValue.min, yAxisExt.axisValue.max]
          }
        },
        encode: {
          y1: {
            type: 'constant',
            value: yAxis.axisValue.min
          }
        },
        axis: {
          y: {
            tickCount: yAxisExt.axisValue.splitCount < 2 ? 2 : yAxisExt.axisValue.splitCount,
            tickMethod: (min, max, count) => {
              const step = (max - min) / (count - 1)
              const ticks = []
              for (let i = 0; i < count; i++) {
                ticks.push(min + i * step)
              }
              return ticks
            }
          }
        }
      })
    }
    defaultsDeep(firstMark, { axis: { y: yAxisOption } })
    defaultsDeep(secondMark, { axis: { y: yAxisExtOption } })
    return options
  }

  protected configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const [firstMark, secondMark] = this.getChartMarks(options)
    const firstData = firstMark?.data?.value
    const secondData = secondMark?.data?.value
    if (!firstData?.length || !secondData?.length) {
      return options
    }

    const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
    const isNullValue = value => value === null || value === undefined

    if (strategy === 'ignoreData') {
      const emptyFields = new Set<string>()
      firstData.forEach(obj => {
        if (isNullValue(obj?.value)) {
          emptyFields.add(obj?.field)
        }
      })
      secondData.forEach(obj => {
        if (isNullValue(obj?.value)) {
          emptyFields.add(obj?.field)
        }
      })
      firstMark.data.value = firstData.filter(obj => !emptyFields.has(obj?.field))
      secondMark.data.value = secondData.filter(obj => !emptyFields.has(obj?.field))
      return options
    }

    const updateValues = (strategy: 'breakLine' | 'setZero', data: any[]) => {
      const emptyValue = strategy === 'setZero' ? 0 : null
      data.forEach(obj => {
        if (isNullValue(obj?.value)) {
          obj.value = emptyValue
        }
      })
    }
    if (strategy === 'breakLine' || strategy === 'setZero') {
      updateValues(strategy, firstData)
      updateValues(strategy, secondData)
    }
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const { tooltip: tooltipAttr, basicStyle } = parseJson(chart.customAttr)
    const { yAxis, yAxisExt } = chart
    const [firstMark, secondMark] = this.getChartMarks(options)
    if (!tooltipAttr.show) {
      merge(firstMark, { tooltip: false })
      merge(secondMark, { tooltip: false })
      return options
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    let g2TooltipWrapper = document.getElementById('G2-TOOLTIP-WRAPPER')
    if (!g2TooltipWrapper) {
      g2TooltipWrapper = document.createElement('div')
      g2TooltipWrapper.id = 'G2-TOOLTIP-WRAPPER'
      g2TooltipWrapper.style.position = 'absolute'
      g2TooltipWrapper.style.pointerEvents = 'none'
      g2TooltipWrapper.style.zIndex = '9999'
      document.body.appendChild(g2TooltipWrapper)
    }
    const tooltipOptions: G2Spec = {
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          mount: g2TooltipWrapper,
          css: {
            '.g2-tooltip': {
              background: tooltipAttr.backgroundColor
            },
            '.g2-tooltip-title': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            },
            '.g2-tooltip-list-item-name-label': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            },
            '.g2-tooltip-list-item-value': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            }
          },
          render: (_, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            let hideLeft = false
            let hideRight = false
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              hideLeft = formatterMap[yAxis[0].id] ? false : true
              hideRight = formatterMap[yAxisExt[0].id] ? false : true
            }
            const result = []
            const [item] = items
            if ((item.left && !hideLeft) || (item.right && !hideRight)) {
              const formatter =
                formatterMap[item.quotaList[0].id] ?? (item.left ? yAxis[0] : yAxisExt[0])
              const value = valueFormatter(item.value, formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value })
            }
            const anotherHide = item.left ? hideRight : hideLeft
            if (!anotherHide) {
              const anotherSeries = item.left ? secondMark.data.value : firstMark.data.value
              const anotherItem = anotherSeries.find(d => d.field === item.field)
              if (anotherItem?.value !== undefined && anotherItem?.value !== null) {
                const formatter =
                  formatterMap[anotherItem.quotaList[0].id] ?? (item.left ? yAxis[0] : yAxisExt[0])
                const value = valueFormatter(anotherItem.value, formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                const color = hexColorToRGBA(basicStyle.colors[item.left ? 1 : 0], basicStyle.alpha)
                item.left
                  ? result.push({ color, name, value })
                  : result.unshift({ color, name, value })
              }
            }
            item.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
              }
            })
            const itemsHtml = result
              .map(item => {
                const marker = item.color
                const label = item.name
                const value = item.value
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
    defaultsDeep(firstMark, {
      ...tooltipOptions,
      tooltip: { items: [d => ({ ...d, left: true })] }
    })
    defaultsDeep(secondMark, {
      ...tooltipOptions,
      tooltip: { items: [d => ({ ...d, right: true })] }
    })
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label, basicStyle } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const { yAxis, yAxisExt } = chart
    const [firstMark, secondMark] = this.getChartMarks(options)
    const conditions = getLineConditions(chart)
    const formatterMap = label.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    if (label.seriesLabelFormatter?.every(item => !item.show)) {
      return options
    }
    const labelOpt = {
      text: d => {
        if (d.value === null) {
          return ''
        }
        if (!label.seriesLabelFormatter?.length) {
          return d.value
        }
        const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return d.value
        }
        if (!labelCfg.show) {
          return ''
        }
        return valueFormatter(d.value, labelCfg.formatterCfg)
      },
      style: {
        opacity: 1,
        // 标签不参与命中测试，避免遮挡柱体鼠标事件
        pointerEvents: 'none',
        fontSize: d => {
          if (!label.seriesLabelFormatter?.length) {
            return 12
          }
          const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
          if (!labelCfg) {
            return 12
          }
          if (!labelCfg.show) {
            return 0
          }
          return labelCfg.fontSize
        },
        fill: d => {
          if (!label.seriesLabelFormatter?.length) {
            return 'black'
          }
          const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
          if (!labelCfg?.show) {
            return 'black'
          }
          const color =
            getLineLabelColorByCondition(conditions, d.value, d.quotaList[0].id) || labelCfg.color
          return color
        },
        position: d => {
          if (!label.seriesLabelFormatter?.length) {
            return 'top'
          }
          const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
          if (!labelCfg?.show) {
            return 'top'
          }
          return labelCfg.position
        }
      },
      transform: label.fullDisplay
        ? [{ type: 'exceedAdjust' }]
        : [{ type: 'exceedAdjust' }, { type: 'overlapHide' }],
      fontFamily: chart.fontFamily
    }
    if (formatterMap[yAxis[0].id]?.show !== false) {
      let position = label.position === 'middle' ? 'inside' : label.position
      if (basicStyle.layout === 'horizontal') {
        position = {
          right: 'left',
          left: 'right',
          inside: 'inside'
        }[position]
      }
      const textAlign = {
        right: 'end',
        left: 'end',
        inside: 'center',
        top: 'center',
        bottom: 'center'
      }[position]
      const textBaseline = {
        right: 'middle',
        left: 'middle',
        inside: 'middle',
        top: 'bottom',
        bottom: 'bottom'
      }[position]
      const firstLabelOpt = merge({}, labelOpt, {
        style: {
          position,
          textAlign,
          textBaseline
        }
      })
      merge(firstMark, { labels: [firstLabelOpt] })
    }
    if (formatterMap[yAxisExt[0].id]?.show !== false) {
      let position = label.position === 'middle' ? 'inside' : label.position
      if (basicStyle.layout === 'vertical') {
        position = {
          top: 'bottom',
          bottom: 'top',
          inside: 'inside'
        }[position]
      }
      const textAlign = {
        right: 'start',
        left: 'start',
        inside: 'center',
        top: 'center',
        bottom: 'center'
      }[position]
      const textBaseline = {
        right: 'middle',
        left: 'middle',
        inside: 'middle',
        top: 'top',
        bottom: 'top'
      }[position]
      const secondLabelOpt = merge({}, labelOpt, {
        style: {
          position,
          textAlign,
          textBaseline
        }
      })
      merge(secondMark, { labels: [secondLabelOpt] })
    }
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return options
    }
    const { basicStyle } = parseJson(chart.customAttr)
    const [firstData, secondData] = chart.data.data
    const flexOptions = options as any
    // 旧图表配置可能没有 legend.size/fontSize，先兜底，避免图例空间计算出现 NaN
    const legendFontSize = legend.fontSize ?? 12
    const legendMarkerSize = legend.size ?? 8
    const topLegend = legend.vPosition === 'top'
    const getLegendRatio = (direction: 'col' | 'row', legendFirst = false) => {
      // spaceFlex 的 ratio 是纯比例切分，小容器下固定 [20, 1] 会把图例层压到不可见；这里按实际容器给图例预留最小像素空间
      const containerRect =
        typeof document === 'undefined' || !chart.container
          ? undefined
          : document.getElementById(chart.container)?.getBoundingClientRect()
      const mainSize = direction === 'col' ? containerRect?.height : containerRect?.width
      const getTextWidth = text => {
        return Array.from(`${text ?? ''}`).reduce((width, char) => {
          return width + (char.charCodeAt(0) > 255 ? legendFontSize : legendFontSize * 0.6)
        }, 0)
      }
      // 图例字体或图形放大后，图例层也要随之增高；否则图例会从独立 legends 子层溢出到图表边界外
      const legendGap = topLegend && direction === 'col' ? 8 : 14
      const legendLineSize = Math.ceil(Math.max(legendFontSize * 1.3, legendMarkerSize) + legendGap)
      const legendMainSize =
        direction === 'col'
          ? Math.max(topLegend ? 28 : 32, legendLineSize)
          : Math.max(
              80,
              getTextWidth(firstData.name) + legendMarkerSize + 40,
              getTextWidth(secondData.name) + legendMarkerSize + 40
            )
      if (!mainSize || mainSize <= 0) {
        return legendFirst ? [1, 20] : [20, 1]
      }
      const safeLegendSize = Math.max(1, Math.min(legendMainSize, mainSize - 1))
      const chartMainSize = Math.max(mainSize - safeLegendSize, 1)
      return legendFirst ? [safeLegendSize, chartMainSize] : [chartMainSize, safeLegendSize]
    }
    const keepTopLegendPlotInset = () => {
      if (!topLegend || basicStyle.layout !== 'horizontal') {
        return
      }
      const chartOptions = this.getChartOptions(options)
      if (!chartOptions) {
        return
      }
      merge(chartOptions, {
        margin: 0,
        padding: 0,
        inset: 0
      })
      chartOptions.children?.forEach(mark => {
        const topPadding = typeof mark.paddingTop === 'number' ? Math.min(mark.paddingTop, 2) : 2
        merge(mark, {
          marginTop: 0,
          paddingTop: topPadding,
          insetTop: 0,
          insetBottom: Math.max(mark.insetBottom ?? 0, 12)
        })
      })
    }
    const legendOpt: any = {
      key: 'legends',
      type: 'legends',
      scale: {
        color: {
          type: 'ordinal',
          domain: [firstData.name, secondData.name],
          range: [
            hexColorToRGBA(basicStyle.colors[0], basicStyle.alpha),
            hexColorToRGBA(basicStyle.colors[1], basicStyle.alpha)
          ]
        }
      },
      position: 'top',
      layout: {},
      itemMarker: legend.icon,
      itemMarkerSize: legendMarkerSize,
      itemLabelFontSize: legendFontSize,
      itemLabelFill: legend.color,
      ...(topLegend
        ? {
            margin: 0,
            rowPadding: 2,
            colPadding: 6,
            crossPadding: 2,
            itemSpacing: [4, 4, 2],
            maxRows: 1
          }
        : {
            margin: 8
          })
    }
    if (legend.hPosition === 'center') {
      legendOpt.layout.justifyContent = 'center'
      legendOpt.layout.flexDirection = 'row'
      if (legend.vPosition === 'top') {
        flexOptions.ratio = getLegendRatio('col', true)
        flexOptions.children.unshift(legendOpt)
        keepTopLegendPlotInset()
      }
      if (legend.vPosition === 'bottom') {
        flexOptions.ratio = getLegendRatio('col')
        flexOptions.children.push(legendOpt)
      }
    } else {
      if (legend.vPosition === 'center') {
        flexOptions.direction = 'row'
        legendOpt.position = 'left'
        legendOpt.layout.justifyContent = 'center'
        legendOpt.layout.flexDirection = 'col'
        if (legend.hPosition === 'left') {
          flexOptions.ratio = getLegendRatio('row', true)
          flexOptions.children.unshift(legendOpt)
        }
        if (legend.hPosition === 'right') {
          flexOptions.ratio = getLegendRatio('row')
          flexOptions.children.push(legendOpt)
        }
      } else {
        flexOptions.direction = 'col'
        if (legend.hPosition === 'left') {
          legendOpt.layout.justifyContent = 'flex-start'
        }
        if (legend.hPosition === 'right') {
          legendOpt.layout.justifyContent = 'flex-end'
        }
        if (legend.vPosition === 'top') {
          flexOptions.ratio = getLegendRatio('col', true)
          flexOptions.children.unshift(legendOpt)
          keepTopLegendPlotInset()
        }
        if (legend.vPosition === 'bottom') {
          flexOptions.ratio = getLegendRatio('col')
          flexOptions.children.push(legendOpt)
        }
      }
    }
    return options
  }

  protected configConditions(chart: Chart, options: G2Spec): G2Spec {
    const { threshold } = parseJson(chart.senior)
    if (!threshold?.enable || !threshold?.lineThreshold?.length) {
      return options
    }
    const leftAxis = chart.yAxis?.[0]
    const rightAxis = chart.yAxisExt?.[0]
    let leftCondition: TableThreshold, rightCondition: TableThreshold
    threshold.lineThreshold.forEach(item => {
      if (item.fieldId === leftAxis.id) {
        leftCondition = item as TableThreshold
      }
      if (item.fieldId === rightAxis.id) {
        rightCondition = item as TableThreshold
      }
    })
    const { basicStyle } = parseJson(chart.customAttr)
    const isNullValue = value => value === null || value === undefined
    const [firstMark, secondMark] = this.getChartMarks(options)
    if (leftCondition?.conditions?.length) {
      firstMark.data.value.forEach(d => {
        // 空值不参与条件样式比较
        if (isNullValue(d.value)) {
          return
        }
        leftCondition.conditions.forEach(c => {
          if (
            (c.term === 'between' && d.value >= c.min && d.value <= c.max) ||
            (c.term === 'lt' && d.value < c.value) ||
            (c.term === 'le' && d.value <= c.value) ||
            (c.term === 'gt' && d.value > c.value) ||
            (c.term === 'ge' && d.value >= c.value)
          ) {
            let tmpColor = hexToRgba(c.color, basicStyle.alpha)
            if (basicStyle.gradient) {
              const angle = basicStyle.layout === 'vertical' ? 270 : 180
              tmpColor = setGradientColor(tmpColor, true, angle)
            }
            d.conditionColor = tmpColor
          }
        })
      })
      const originColor = firstMark.style?.fill
      firstMark.style = {
        ...firstMark.style,
        fill: d => {
          if (isNullValue(d.value)) {
            return typeof originColor === 'function' ? originColor(d) : originColor
          }
          if (d.conditionColor) {
            return d.conditionColor
          }
          return typeof originColor === 'function' ? originColor(d) : originColor
        }
      }
    }
    if (rightCondition?.conditions?.length) {
      secondMark.data.value.forEach(d => {
        if (isNullValue(d.value)) {
          return
        }
        rightCondition.conditions.forEach(c => {
          if (
            (c.term === 'between' && d.value >= c.min && d.value <= c.max) ||
            (c.term === 'lt' && d.value < c.value) ||
            (c.term === 'le' && d.value <= c.value) ||
            (c.term === 'gt' && d.value > c.value) ||
            (c.term === 'ge' && d.value >= c.value)
          ) {
            let tmpColor = hexToRgba(c.color, basicStyle.alpha)
            if (basicStyle.gradient) {
              const angle = basicStyle.layout === 'vertical' ? 90 : 0
              tmpColor = setGradientColor(tmpColor, true, angle)
            }
            d.conditionColor = tmpColor
          }
        })
      })
      const originColor = secondMark.style?.fill
      secondMark.style = {
        ...secondMark.style,
        fill: d => {
          if (isNullValue(d.value)) {
            return typeof originColor === 'function' ? originColor(d) : originColor
          }
          if (d.conditionColor) {
            return d.conditionColor
          }
          return typeof originColor === 'function' ? originColor(d) : originColor
        }
      }
    }
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis = {
      ...chart.customStyle.yAxis,
      position: 'left'
    }
    chart.customStyle.yAxisExt = {
      ...chart.customStyle.yAxisExt,
      position: 'left',
      splitLine: chart.customStyle.yAxis.splitLine
    }
    chart.customAttr.label = {
      ...chart.customAttr.label,
      position: 'right'
    }
    chart.customAttr.basicStyle.layout = 'horizontal'
    return chart
  }

  protected setupOptions(chart: Chart, options: G2Spec) {
    return flow(
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configXAxis,
      this.configYAxis,
      this.configTooltip,
      this.configLabel,
      this.configLegend,
      this.configConditions
    )(chart, options, {}, this)
  }

  constructor() {
    super('bidirectional-bar', [])
  }
}
