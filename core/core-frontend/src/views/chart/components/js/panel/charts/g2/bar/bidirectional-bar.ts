import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  getLineConditions,
  getLineLabelColorByCondition,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { AxisComponent, ChartEvent, Chart as G2Chart, G2Spec } from '@antv/g2'
import { setGradientColor, TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'

const { t } = useI18n()

/**
 * 对称柱状图
 */
export class BidirectionalHorizontalBar extends G2ChartView {
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

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
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
    const [firstMark, secondMark] = options.children.find(c => c.key === 'chart').children
    newChart.once(ChartEvent.AFTER_RENDER, () => {
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
        newChart.render()
      }
    })
    newChart.on('interval:click', action)
    // 开始渲染
    newChart.options(options)
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const [firstMark, secondMark] = options.children[0].children
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
        }
      }
    })
    if (basicStyle.layout === 'vertical') {
      options.children[0].direction = 'col'
      delete firstMark.scale.y.range
      delete firstMark.coordinate
      delete secondMark.coordinate
      defaultsDeep(secondMark, {
        scale: {
          y: {
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
        fill: firstColor
      }
    })
    defaultsDeep(secondMark, {
      style: {
        fill: secondColor
      }
    })
    return options
  }

  protected configXAxis(chart: Chart, options: G2Spec): G2Spec {
    const [firstMark, secondMark] = options.children[0].children
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
    let gridLineDash = undefined
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
    const axisStyle = {
      axis: {
        x: {
          position: position,
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
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          labelTransform: `rotate(${xAxis.axisLabel.rotate || 0})`,
          transform: [
            {
              type: 'hide',
              keepHeader: true,
              keepTail: true
            }
          ]
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
    merge(secondMark, axisStyle, {
      axis: {
        x: {
          label: false,
          tick: xAxis.axisLabel.show && ['right', 'bottom'].includes(position),
          position: POSITION_MAP[position],
          line: xAxis.axisLine.show && ['right', 'bottom'].includes(position)
        }
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
    const [firstMark, secondMark] = options.children[0].children
    const { xAxis, yAxis, yAxisExt } = parseJson(chart.customStyle)
    const { basicStyle } = parseJson(chart.customAttr)
    if (!yAxis.show) {
      firstMark.axis.y = false
      secondMark.axis.y = false
      return options
    }
    const yAxisOption = this.getAxis(yAxis)
    const yAxisExtOption = this.getAxis(yAxisExt)
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
    }
    if (yAxis.axisValue.auto === false) {
      merge(firstMark, {
        scale: {
          y: {
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
            tickCount: yAxis.axisValue.splitCount,
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
            tickCount: yAxisExt.axisValue.splitCount,
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

  private getAxis(axis: DeepPartial<ChartAxisStyle>): AxisComponent {
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
      line: axis.axisLine.show,
      lineStroke: axis.axisLine.lineStyle.color,
      lineStrokeOpacity: 1,
      lineLineWidth: axis.axisLine.lineStyle.width,
      lineLineDash,
      label: axis.axisLabel.show,
      labelFill: axis.axisLabel.color,
      labelFillOpacity: 1,
      labelFontSize: axis.axisLabel.fontSize,
      grid: axis.splitLine.show,
      gridStroke: axis.splitLine.lineStyle.color,
      gridStrokeOpacity: 1,
      gridLineWidth: axis.splitLine.lineStyle.width,
      gridLineDash,
      labelTransform: `rotate(${axis.axisLabel.rotate || 0})`,
      transform: [
        {
          type: 'hide',
          keepHeader: true,
          keepTail: true
        }
      ],
      labelFormatter: d => {
        return valueFormatter(d, axis.axisLabelFormatter)
      }
    }
    return axisOption
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const { tooltip: tooltipAttr, basicStyle } = parseJson(chart.customAttr)
    const { yAxis, yAxisExt } = chart
    const [firstMark, secondMark] = options.children[0].children
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
    const [firstMark, secondMark] = options.children[0].children
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
        ? []
        : [{ type: 'overlapDodgeY' }, { type: 'exceedAdjust' }, { type: 'overlapHide' }],
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
    const legendOpt = {
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
      itemMarkerSize: legend.size,
      itemLabelFontSize: legend.fontSize,
      itemLabelFill: legend.color
    }
    if (legend.hPosition === 'center') {
      legendOpt.layout.justifyContent = 'center'
      legendOpt.layout.flexDirection = 'row'
      if (legend.vPosition === 'top') {
        options.ratio = [1, 20]
        options.children.unshift(legendOpt)
      }
      if (legend.vPosition === 'bottom') {
        options.ratio = [20, 1]
        options.children.push(legendOpt)
      }
    } else {
      if (legend.vPosition === 'center') {
        options.direction = 'row'
        legendOpt.position = 'left'
        legendOpt.layout.justifyContent = 'center'
        legendOpt.layout.flexDirection = 'col'
        if (legend.hPosition === 'left') {
          options.ratio = [1, 20]
          options.children.unshift(legendOpt)
        }
        if (legend.hPosition === 'right') {
          options.ratio = [20, 1]
          options.children.push(legendOpt)
        }
      } else {
        options.direction = 'col'
        if (legend.hPosition === 'left') {
          legendOpt.layout.justifyContent = 'flex-start'
        }
        if (legend.hPosition === 'right') {
          legendOpt.layout.justifyContent = 'flex-end'
        }
        if (legend.vPosition === 'top') {
          options.ratio = [1, 20]
          options.children.unshift(legendOpt)
        }
        if (legend.vPosition === 'bottom') {
          options.ratio = [20, 1]
          options.children.push(legendOpt)
        }
      }
    }
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec) {
    return flow(
      this.configBasicStyle,
      this.configXAxis,
      this.configYAxis,
      this.configTooltip,
      this.configLabel,
      this.configLegend
    )(chart, options, {}, this)
  }

  constructor() {
    super('bidirectional-bar', [])
  }
}
