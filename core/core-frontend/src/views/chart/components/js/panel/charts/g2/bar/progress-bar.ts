import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  listenerTooltipShow,
  createTooltipWrapper,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { cloneDeep, defaultTo } from 'lodash-es'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { Chart as G2Column } from '@antv/g2'
import { useI18n } from '@/hooks/web/useI18n'
import {
  configAxisLengthLimit,
  getG2Renderer,
  handleChartDashboardHidden,
  setGradientColor,
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { HorizontalStackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-horizontal-bar'

const { t } = useI18n()

/**
 * 进度条
 */
export class ProgressBar extends HorizontalStackBar {
  axisConfig = {
    xAxis: {
      name: `${t('chart.form_type')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.progress_target')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    yAxisExt: {
      name: `${t('chart.progress_current')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'y-axis-selector',
    'title-selector',
    'function-cfg',
    'jump-set',
    'linkage',
    'threshold'
  ]
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'legend-selector': null,
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient', 'radiusColumnBar', 'columnWidthRatio'],
    'label-selector': ['hPosition', 'color', 'fontSize', 'showQuota', 'showProportion'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'axisForm',
      'axisLabel',
      // 'position',
      'showLengthLimit'
    ],
    'function-cfg': ['emptyDataStrategy'],
    threshold: ['lineThreshold']
  }

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const { chart, container, action } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      return
    }
    const getCompletionRate = (target: number, current: number) => {
      if (target === 0) {
        return 100
      }
      // 目标为正 当前为负
      if (target > 0 && current < 0) {
        return 0
      }
      // 目标为负 当前为正 正向
      if ((target < 0 && current > 0) || (target < 0 && current === 0)) {
        return (2 - current / target) * 100
      }
      // 目标与当前都为正
      if (target > 0 && current > 0) {
        return (current / target) * 100
      }
      // 目标与当前都为负 负向小于0为0
      if (target < 0 && current < 0) {
        const completionRate = (2 - current / target) * 100
        return Number(Math.max(completionRate, 0).toFixed(2))
      }
      return 0
    }
    // data
    const sourceData: Array<any> = cloneDeep(chart.data.data)
    const data1 = defaultTo(sourceData[0]?.data, [])
    const data2 = defaultTo(sourceData[1]?.data, [])
    const currentData = data2.map(item => {
      const progress = getCompletionRate(data1.find(i => i.field === item.field)?.value, item.value)
      return {
        ...item,
        type: 'current',
        title: item.field,
        id: item.quotaList[0].id,
        originalValue: item.value,
        progress: progress >= 100 ? 100 : progress
      }
    })
    const targetData = data1.map(item => {
      return {
        ...item,
        type: 'target',
        title: item.field,
        id: item.quotaList[0].id
      }
    })

    const initOptions: ViewSpec = {
      type: 'view',
      paddingTop: -15,
      paddingBottom: -10,
      data: currentData.concat(targetData).flat(),
      children: [
        {
          type: 'interval',
          style: {
            radius: 0
          },
          encode: {
            x: 'title',
            y: 'progress',
            color: ''
          },
          coordinate: {
            transform: [{ type: 'transpose' }]
          },
          axis: {
            y: false,
            x: {
              title: '',
              // 对齐条形图的左轴标题布局，避免负间距让标题挤入分类标签区域
              dataeaseAxisTitleSafeMargin: true,
              labelSpacing: 15,
              tick: false,
              line: false
            }
          },
          scale: {
            color: { range: [] },
            y: { nice: true }
          },
          tooltip: false
        },
        {
          key: 'progress-background',
          type: 'interval',
          style: {
            radius: 0
          },
          encode: {
            x: 'title',
            y: 100,
            color: ''
          },
          scale: {
            color: { type: 'identity' }
          },
          animate: false,
          tooltip: false
        }
      ]
    }
    const options: ViewSpec = this.setupOptions(chart, initOptions)
    // 注入公共渲染器配置以响应 SVG 渲染开关
    const newChart = new G2Column({ container, autoFit: true, ...getG2Renderer() })
    const newOptions = cloneDeep(options)
    newOptions.children = [options.children[1], options.children[0]]
    handleChartDashboardHidden(chart, newOptions)
    newChart.options(newOptions)
    newChart.on('interval:click', param => {
      if (param.data.data.type === 'target') {
        // 点击背景柱时统一使用同维度的实际值数据
        param.data.data = currentData.find(item => item.field === param.data.data.field)
      }
      action(param)
    })
    listenerTooltipShow(newChart, chart)
    configAxisLengthLimit(chart, newChart, 'yAxis')
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: ViewSpec): ViewSpec {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    let color1 = basicStyle.colors?.map((ele, index) => {
      if (index === 1) {
        return hexColorToRGBA(ele, basicStyle.alpha / 10)
      } else {
        return hexColorToRGBA(ele, basicStyle.alpha)
      }
    })
    if (basicStyle.gradient) {
      color1 = color1.map((ele, _index) => {
        return setGradientColor(ele, true, 0)
      })
    }
    const superOptions = super.configBasicStyle(chart, options)
    const { children } = superOptions
    children[0].encode.color = color1[0]
    children[1].encode.color = color1[1]
    const columnWidthRatio = this.getColumnWidthRatio(basicStyle)
    const columnPadding = this.getColumnPadding(columnWidthRatio)
    const styleColumnWidthRatio = this.getStyleColumnWidthRatio(columnPadding)
    if (styleColumnWidthRatio) {
      children[0].style = { ...children[0].style, columnWidthRatio: styleColumnWidthRatio }
      children[1].scale = {
        ...children[1].scale,
        x: {
          ...(children[1].scale?.x || {}),
          padding: columnPadding,
          paddingInner: columnPadding
        }
      }
      children[1].style = { ...children[1].style, columnWidthRatio: styleColumnWidthRatio }
    }
    return superOptions
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const tmpOptions = super.configLabel(chart, options)
    const { children } = tmpOptions
    const { label: labelAttr } = parseJson(chart.customAttr) || {}
    if (!labelAttr?.show || !children?.[0]) {
      return tmpOptions
    }

    const baseLabel = children[0].labels?.[0] || {}
    const label = {
      ...baseLabel,
      text: 'progress',
      fillOpacity: 1,
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: 'start',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: 0,
      transform: baseLabel.transform ?? [
        { type: 'exceedAdjust' },
        ...(labelAttr.fullDisplay ? [] : [{ type: 'overlapHide' }])
      ],
      formatter: (progress, data) => {
        if (data.type === 'target') return ''
        const showQuota = labelAttr.showQuota === true
        const showProportion = labelAttr.showProportion ?? true
        if (!showQuota && !showProportion) return ''

        const quotaText = showQuota
          ? valueFormatter(
              data.value,
              labelAttr.quotaLabelFormatter ?? labelAttr.labelFormatter ?? formatterItem
            )
          : ''
        const progressValue = Number(progress)
        const progressText = Number.isFinite(progressValue)
          ? progressValue.toFixed(labelAttr.reserveDecimalCount ?? 2)
          : ''
        const proportionText =
          showProportion && progressText
            ? `${showQuota ? ' (' : ''}${progressText}%${showQuota ? ')' : ''}`
            : ''
        return `${quotaText}${proportionText}`
      }
    }
    return {
      ...tmpOptions,
      children: [
        {
          ...children[0],
          labels: [label]
        },
        ...children.slice(1)
      ]
    }
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return options
    }
    children[0].tooltip = {
      items: [
        (datum, index, data, _column) => {
          if (datum.type === 'target') {
            return {
              value: datum.value,
              original_data: data[index]
            }
          }
        }
      ]
    }
    children[1].tooltip = {
      items: [
        (datum, index, data, _column) => ({
          value: datum.value,
          original_data: data[index]
        })
      ]
    }
    const yAxis = cloneDeep(chart.yAxis)[0]
    const yAxisExt = cloneDeep(chart.yAxisExt)[0]
    const tooltipOptions: ViewSpec = {
      tooltip: a => a,
      interaction: {
        ...children[0].interaction,
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: {
            ...tooltipCss(tooltip),
            'g2-tooltip-list-item-marker': {
              display: 'none !important',
              width: '0px !important',
              backgroundColor: 'red !important'
            }
          },
          enterable: true,
          shared: true,
          marker: false,
          bounding: {
            x: 0,
            y: 0
          },
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const tooltipItems = originalItems?.filter(item => item.original_data)
            const result = []
            tooltipItems.forEach(item => {
              const value = valueFormatter(item.value, tooltip.tooltipFormatter)
              let name = item.original_data.field
              if (item.original_data.id === yAxis.id) {
                name = yAxis.chartShowName ? yAxis.chartShowName : yAxis.name
              }
              if (item.original_data.id === yAxisExt.id) {
                name = yAxisExt.chartShowName ? yAxisExt.chartShowName : yAxisExt.name
              }
              result.push({ ...item, name, value })
            })
            const itemsHtml = result
              .map(item => {
                const marker = toLinearGradient(item.color)
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace(
                  /<span class="g2-tooltip-list-item-marker"[^>]*>.*?<\/span>/g,
                  ''
                )
                  .replace('{marker}', marker)
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
    return {
      ...options,
      children: [{ ...children[0], ...tooltipOptions }, ...children.slice(1)]
    }
  }

  protected configXAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const superOptions = super.configXAxis(chart, options)
    const { children } = superOptions
    children[0].axis.x.grid = false
    return superOptions
  }

  protected configEmptyDataStrategy(chart: Chart, options: ViewSpec): ViewSpec {
    const { data } = options
    if (!data?.length) {
      return options
    }
    const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
    if (strategy === 'ignoreData') {
      const emptyFields = data.filter(obj => obj['value'] === null).map(obj => obj['field'])
      return {
        ...options,
        data: data.filter(obj => {
          if (emptyFields.includes(obj['field'])) {
            return false
          }
          return true
        })
      }
    }
    if (strategy === 'breakLine') {
      data.forEach(obj => {
        if (obj['value'] === null) {
          obj['value'] = null
        }
      })
    }
    if (strategy === 'setZero') {
      data.forEach(obj => {
        if (obj['value'] === null) {
          obj['value'] = 0
        }
      })
    }
    return options
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configEmptyDataStrategy,
      this.configBarConditions
    )(chart, options, {}, this)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis = {
      ...chart.customStyle.yAxis,
      position: 'left',
      axisLine: {
        show: false,
        // 切换为进度条时保留用户自定义或主题跟随状态
        colorMode: chart.customStyle.yAxis.axisLine.colorMode,
        lineStyle: chart.customStyle.yAxis.axisLine.lineStyle
      },
      splitLine: {
        show: false,
        lineStyle: chart.customStyle.yAxis.axisLine.lineStyle
      }
    }
    chart.customStyle.legend.show = false
    chart.customAttr.label.show = true
    chart.customAttr.label.position = 'right'
    chart.customAttr.label.showQuota = false
    chart.customAttr.label.showProportion = true
    return super.setupDefaultOptions(chart)
  }

  constructor(name = 'progress-bar') {
    super(name)
    this.axis = [...BAR_AXIS_TYPE, 'yAxisExt']
  }
}
