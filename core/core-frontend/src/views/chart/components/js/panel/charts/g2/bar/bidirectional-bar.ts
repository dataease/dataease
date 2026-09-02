import {
  G2ChartView,
  G2DrawOptions,
  getCategoryLegendStyle,
  getHorizontalLegendTextStyle
} from '../../../types/impl/g2'
import { flow, hexColorToRGBA, hexToRgba, parseJson } from '@/views/chart/components/js/util'
import { defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Chart, extend, G2Spec, Runtime, stdlib } from '@antv/g2'
import {
  configXAxisLengthLimit,
  formatAxisLabelWithLengthLimit,
  getG2Renderer,
  handleChartDashboardHidden,
  setGradientColor,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import {
  bindPlotBackgroundClick,
  createTooltipWrapper,
  getSeriesTooltipFormatter,
  getSeriesTooltipFormatterMap,
  isSeriesTooltipFormatterShown
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import {
  getSideLegendMaxWidth,
  SIDE_LEGEND_DEFAULT_COL_PADDING,
  SIDE_LEGEND_MIN_LABEL_WIDTH
} from '../../../types/impl/g2-legend'
import { measureLegendTextWidth } from '../../../types/impl/g2-legend-poptip'

interface BidirectionalLegendFlexLayout {
  direction: 'col' | 'row'
  legendFirst: boolean
  legendContentSize: number
}

const createResponsiveBidirectionalSpaceFlex = baseSpaceFlex => {
  const responsiveSpaceFlex = (...args) => {
    const layout = baseSpaceFlex(...args)
    return options => {
      const legendLayout = options.dataeaseBidirectionalLegendFlex as BidirectionalLegendFlexLayout
      if (!legendLayout) {
        return layout(options)
      }
      const mainSize = Number(legendLayout.direction === 'col' ? options.height : options.width)
      if (!Number.isFinite(mainSize) || mainSize <= 0) {
        return layout(options)
      }
      const padding = Math.max(0, Number(options.padding) || 0)
      const childCount = Array.isArray(options.children) ? options.children.length : 2
      const availableMainSize = Math.max(1, mainSize - padding * Math.max(0, childCount - 1))
      const sideLegendMaxWidth = getSideLegendMaxWidth(Number(options.width))
      const legendMainSize = Math.max(
        1,
        Math.min(
          legendLayout.direction === 'row'
            ? Math.min(legendLayout.legendContentSize, sideLegendMaxWidth)
            : legendLayout.legendContentSize,
          availableMainSize - 1
        )
      )
      const chartMainSize = Math.max(1, availableMainSize - legendMainSize)
      const ratio = legendLayout.legendFirst
        ? [legendMainSize, chartMainSize]
        : [chartMainSize, legendMainSize]
      // 每轮布局都按当前容器尺寸重算分栏，避免 resize 沿用旧比例放大图例留白
      return layout({ ...options, ratio })
    }
  }
  responsiveSpaceFlex.props = baseSpaceFlex.props
  return responsiveSpaceFlex
}

const bidirectionalLibrary = stdlib() as Record<string, any>
bidirectionalLibrary['composition.spaceFlex'] = createResponsiveBidirectionalSpaceFlex(
  bidirectionalLibrary['composition.spaceFlex']
)
const BidirectionalG2Chart = extend(Runtime, bidirectionalLibrary) as typeof G2Chart

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

  private getValueAxis(
    chart: Chart,
    axis: DeepPartial<ChartAxisStyle>,
    hideInnerBaselineLabel = false
  ) {
    const axisOption = this.getAxis(chart, axis)
    const originLabelFormatter = axisOption.labelFormatter
    const configuredBaseline = Number(axis.axisValue.min)
    const baseline =
      axis.axisValue.auto === false && Number.isFinite(configuredBaseline) ? configuredBaseline : 0
    // 两个数值轴都在数值格式化后按各自配置截断刻度文本
    axisOption.labelFormatter = value => {
      // 共享中线的基线文字直接隐藏，刻度线、网格线和手动范围保持原配置
      if (hideInnerBaselineLabel && Number(value) === baseline) {
        return ''
      }
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

  private getCenteredCategoryAxisLabelStyle(axis: DeepPartial<ChartAxisStyle>) {
    const rotate = Number(axis.axisLabel.rotate) || 0
    const rotateRatio = Math.sin(Math.abs((rotate * Math.PI) / 180))
    const fontSize = axis.axisLabel.fontSize || 12
    return {
      labelOpacity: 1,
      labelFillOpacity: 1,
      labelSpacing: 4 + (fontSize * rotateRatio) / 2,
      labelTextAlign: 'center',
      labelTextBaseline: 'middle',
      // 中轴始终以文字中心为旋转原点，使文字中心落在对应刻度上
      labelTransform: `rotate(${rotate})`
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
    const newChart = new BidirectionalG2Chart({ container, ...getG2Renderer() })
    const options = this.setupOptions(chart, initOptions)
    const { basicStyle } = parseJson(chart.customAttr)
    const { xAxis, yAxis, yAxisExt } = parseJson(chart.customStyle)
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
      if (
        !chart.dashboardHidden &&
        basicStyle.layout === 'vertical' &&
        xAxis.show &&
        xAxis.axisLabel?.show
      ) {
        // 上下对称布局的类目文字沿水平方向排列，首尾修正后同步左右边距以保持两张子图刻度对齐
        const [first, second] = newChart.getContext().views.filter(c => c.key !== 'legends')
        const paddingAttrs = ['paddingLeft', 'paddingRight']
        const chartMark = newChart.children.find(c => c.value.key === 'chart')
        paddingAttrs.forEach(paddingAttr => {
          const firstPadding = Number(first.layout[paddingAttr]) || 0
          const secondPadding = Number(second.layout[paddingAttr]) || 0
          if (Math.abs(firstPadding - secondPadding) <= 1) {
            return
          }
          if (firstPadding < secondPadding) {
            chartMark.children.find(c => c.value.key === 'first').attr(paddingAttr, secondPadding)
          } else {
            chartMark.children.find(c => c.value.key === 'second').attr(paddingAttr, firstPadding)
          }
          reRenderMark = true
        })
      }
      if (
        !chart.dashboardHidden &&
        xAxis.show &&
        xAxis.axisLabel?.show &&
        xAxis.position === 'top'
      ) {
        const [first, second] = newChart.getContext().views.filter(c => c.key !== 'legends')
        const mainSizeAttr = basicStyle.layout === 'horizontal' ? 'width' : 'height'
        const innerSizeAttr = basicStyle.layout === 'horizontal' ? 'innerWidth' : 'innerHeight'
        const firstSize = Number(first.layout[mainSizeAttr]) || 0
        const secondSize = Number(second.layout[mainSizeAttr]) || 0
        const firstNonPlotSize = Math.max(0, firstSize - Number(first.layout[innerSizeAttr]))
        const secondNonPlotSize = Math.max(0, secondSize - Number(second.layout[innerSizeAttr]))
        const sharedPlotSize = (firstSize + secondSize - firstNonPlotSize - secondNonPlotSize) / 2
        if (
          sharedPlotSize > 1 &&
          Math.abs(first.layout[innerSizeAttr] - second.layout[innerSizeAttr]) > 1
        ) {
          // 外置类目轴只增加承载它的 View 比例，让两边 Plot 等大，不再向另一端复制空白
          newChart.children
            .find(c => c.value.key === 'chart')
            .attr('ratio', [sharedPlotSize + firstNonPlotSize, sharedPlotSize + secondNonPlotSize])
          reRenderMark = true
        }
      }
      if (
        !chart.dashboardHidden &&
        yAxis.show &&
        (yAxis.axisLabel?.show || yAxisExt.axisLabel?.show) &&
        // 上方类目轴会占用第一个 View 的外侧总 padding，不能把这部分误复制到第二个 View
        !(xAxis.show && xAxis.axisLabel?.show && xAxis.position === 'top')
      ) {
        // 两张子图的外边界取较大留白，既容纳首尾刻度文字，也保持柱长和零点对称
        const [first, second] = newChart.getContext().views.filter(c => c.key !== 'legends')
        const firstPaddingAttr = basicStyle.layout === 'horizontal' ? 'paddingLeft' : 'paddingTop'
        const secondPaddingAttr =
          basicStyle.layout === 'horizontal' ? 'paddingRight' : 'paddingBottom'
        const firstPadding = Number(first.layout[firstPaddingAttr]) || 0
        const secondPadding = Number(second.layout[secondPaddingAttr]) || 0
        if (Math.abs(firstPadding - secondPadding) > 1) {
          const chartMark = newChart.children.find(c => c.value.key === 'chart')
          if (firstPadding < secondPadding) {
            chartMark.children
              .find(c => c.value.key === 'first')
              .attr(firstPaddingAttr, secondPadding)
          } else {
            chartMark.children
              .find(c => c.value.key === 'second')
              .attr(secondPaddingAttr, firstPadding)
          }
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
    bindPlotBackgroundClick(newChart, { axis: basicStyle.layout === 'vertical' ? 'x' : 'y' })
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
    const formatXAxisLabel = value => {
      const originLabel = `${value ?? ''}`
      return formatAxisLabelWithLengthLimit(originLabel, xAxis.axisLabel.lengthLimit)
    }
    const visibleCategoryAxisLabel = !chart.dashboardHidden && xAxis.axisLabel.show
    const centerAxisLabel = visibleCategoryAxisLabel && xAxis.position === 'bottom'
    const centeredVerticalCategoryAxis = centerAxisLabel && basicStyle.layout === 'horizontal'
    const categoryAxisRotate = Number(xAxis.axisLabel.rotate) || 0
    const rotatedCategoryAxisHasVerticalProjection =
      Math.abs(Math.sin((categoryAxisRotate * Math.PI) / 180)) > 0.001
    const keepCategoryAxisLabelInsidePlot =
      visibleCategoryAxisLabel &&
      basicStyle.layout === 'horizontal' &&
      (centeredVerticalCategoryAxis || rotatedCategoryAxisHasVerticalProjection)
    // 上下对称布局中的类目文字横向排列，最左和最右文字确实可能伸出画布，需要修正左右边界
    const protectCategoryAxisBoundary =
      visibleCategoryAxisLabel &&
      (basicStyle.layout === 'vertical' || keepCategoryAxisLabelInsidePlot)
    const categoryAxisLabelStyle = centeredVerticalCategoryAxis
      ? this.getCenteredCategoryAxisLabelStyle(xAxis)
      : this.getAxisLabelStyle({ ...xAxis, position })
    const axisStyle = {
      axis: {
        x: {
          zIndex: 1,
          // 类目轴只修正沿刻度排列方向的首尾越界，居中偏移仍然只用于共享中轴
          dataeaseAxisLabelOverflow: protectCategoryAxisBoundary ? undefined : false,
          dataeaseAxisLabelOverflowSides: protectCategoryAxisBoundary
            ? basicStyle.layout === 'vertical'
              ? ['left', 'right']
              : ['top', 'bottom']
            : undefined,
          // 中轴及旋转后的外置分类轴只显示完整落在 Plot 内的文字
          dataeaseAxisLabelInsidePlot: keepCategoryAxisLabelInsidePlot ? true : undefined,
          dataeaseAxisLabelCenter: centerAxisLabel ? 'visible' : undefined,
          // 轴组件间距继续使用 G2 布局的统一处理，不在业务图表重复覆盖
          position: position,
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelFontSize: xAxis.axisLabel.fontSize,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...categoryAxisLabelStyle,
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
    const reserveHiddenCenterLabel = centerAxisLabel
    // 根因是维度轴标签实际挂在左侧子图上，右侧如果完全隐藏该轴，左右绘图区宽度会不一致
    const secondXAxis = {
      label: false,
      tick: xAxis.axisLine.show && ['right', 'bottom'].includes(position),
      position: POSITION_MAP[position],
      line: xAxis.axisLine.show && ['right', 'bottom'].includes(position)
    }
    if (reserveHiddenCenterLabel) {
      // 相邻子图使用透明文字取得相同尺寸，再由公共布局各保留半份空间
      merge(secondXAxis, categoryAxisLabelStyle, {
        label: true,
        labelOpacity: 0,
        labelFillOpacity: 0,
        dataeaseAxisLabelCenter: 'reserve'
      })
    }
    merge(secondMark, axisStyle, {
      axis: {
        x: secondXAxis
      }
    })
    // 中间接缝只清理 inset 和 padding，单独设置某一侧 margin 会让 G2 的默认上下留白重新生效
    if (position === 'left') {
      defaultsDeep(firstMark, {
        insetRight: 0,
        paddingRight: 0
      })
      defaultsDeep(secondMark, {
        insetLeft: 0,
        paddingLeft: 0
      })
    }
    if (position === 'top') {
      defaultsDeep(firstMark, {
        insetBottom: 0,
        paddingBottom: 0
      })
      defaultsDeep(secondMark, {
        insetTop: 0,
        paddingTop: 0
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
    const hideInnerBaselineLabel = basicStyle.layout === 'horizontal'
    const yAxisOption = this.getValueAxis(chart, yAxis, hideInnerBaselineLabel)
    const yAxisExtOption = this.getValueAxis(chart, yAxisExt, hideInnerBaselineLabel)
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
    const firstViewOuterSide = basicStyle.layout === 'horizontal' ? 'left' : 'top'
    const secondViewOuterSide = basicStyle.layout === 'horizontal' ? 'right' : 'bottom'
    // 数值轴只修正各自的外边界和轴所在外侧，中间共享边界继续保持不动
    yAxisOption.dataeaseAxisLabelOverflowSides = Array.from(
      new Set([firstViewOuterSide, yAxisOption.position])
    )
    yAxisExtOption.dataeaseAxisLabelOverflowSides = Array.from(
      new Set([secondViewOuterSide, yAxisExtOption.position])
    )
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
    const formatterMap = getSeriesTooltipFormatterMap(tooltipAttr)
    const getAxisFormatter = (item, axisType: 'yAxis' | 'yAxisExt') => {
      const axis = axisType === 'yAxis' ? yAxis : yAxisExt
      const fieldId = item?.quotaList?.[0]?.id ?? axis[0]?.id
      return getSeriesTooltipFormatter(formatterMap, fieldId, axis, axisType) ?? axis[0]
    }
    const tooltipOptions: G2Spec = {
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          mount: createTooltipWrapper(chart),
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
              hideLeft = !isSeriesTooltipFormatterShown(formatterMap, yAxis[0]?.id, 'yAxis')
              hideRight = !isSeriesTooltipFormatterShown(formatterMap, yAxisExt[0]?.id, 'yAxisExt')
            }
            const result = []
            const [item] = items
            if ((item.left && !hideLeft) || (item.right && !hideRight)) {
              const formatter = getAxisFormatter(item, item.left ? 'yAxis' : 'yAxisExt')
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
                const formatter = getAxisFormatter(anotherItem, item.left ? 'yAxisExt' : 'yAxis')
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
              const formatter = getSeriesTooltipFormatter(
                formatterMap,
                item.fieldId,
                chart.extTooltip
              )
              if (formatter && isSeriesTooltipFormatterShown(formatterMap, item.fieldId)) {
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
    const formatterMap = label.seriesLabelFormatter?.reduce((pre, next) => {
      const seriesId = next.seriesId ?? next.id
      pre[seriesId] = next
      if (!next.seriesId || next.seriesId === next.id) {
        pre[next.id] = next
      }
      return pre
    }, {} as Record<string, SeriesFormatter>)
    // 同一字段可同时占用主副值轴，系列样式按字段与槽位组合键读取
    const getLabelFormatter = (data, axisType: 'yAxis' | 'yAxisExt') => {
      const fieldId = data?.quotaList?.[0]?.id ?? data?.id
      return formatterMap?.[`${fieldId}-${axisType}`] ?? formatterMap?.[fieldId]
    }
    if (label.seriesLabelFormatter?.every(item => !item.show)) {
      return options
    }
    const getLabelOpt = (axisType: 'yAxis' | 'yAxisExt') => ({
      text: d => {
        if (d.value === null) {
          return ''
        }
        if (!label.seriesLabelFormatter?.length) {
          return d.value
        }
        const labelCfg = getLabelFormatter(d, axisType)
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
        fillOpacity: 1,
        // 标签不参与命中测试，避免遮挡柱体鼠标事件
        pointerEvents: 'none',
        fontSize: d => {
          if (!label.seriesLabelFormatter?.length) {
            return 12
          }
          const labelCfg = getLabelFormatter(d, axisType)
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
          const labelCfg = getLabelFormatter(d, axisType)
          if (!labelCfg?.show) {
            return 'black'
          }
          // 条件样式只改变柱体颜色，数据标签继续使用标签面板配置
          return labelCfg.color
        },
        position: d => {
          if (!label.seriesLabelFormatter?.length) {
            return 'top'
          }
          const labelCfg = getLabelFormatter(d, axisType)
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
    })
    if (getLabelFormatter(yAxis[0], 'yAxis')?.show !== false) {
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
      const firstLabelOpt = merge({}, getLabelOpt('yAxis'), {
        style: {
          position,
          textAlign,
          textBaseline
        }
      })
      merge(firstMark, { labels: [firstLabelOpt] })
    }
    if (getLabelFormatter(yAxisExt[0], 'yAxisExt')?.show !== false) {
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
      const secondLabelOpt = merge({}, getLabelOpt('yAxisExt'), {
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
    // 隐藏组件时不创建独立图例子层，避免图例消失后仍保留 ratio 占位
    if (chart.dashboardHidden || !legend.show) {
      return options
    }
    const { basicStyle } = parseJson(chart.customAttr)
    const [firstData, secondData] = chart.data.data
    const [firstAxis] = chart.yAxis
    const [secondAxis] = chart.yAxisExt
    const flexOptions = options as any
    const chartOptions = this.getChartOptions(options)
    const legendFontSize = legend.fontSize ?? 12
    const legendMarkerSize = (legend.size ?? 4) * 2
    const legendItemSpacing = 8
    const legendRowPadding = 8
    const getDisplayName = (axis, fallback) => {
      const name = isEmpty(axis?.chartShowName) ? axis?.name : axis.chartShowName
      return `${name ?? fallback ?? ''}`
    }
    // 图例内部键只负责区分主副值轴，展示名称始终读取当前轴字段配置
    const legendItems = [
      {
        key: `yAxis-${firstAxis?.id ?? 'first'}`,
        name: getDisplayName(firstAxis, firstData.name),
        color: hexColorToRGBA(basicStyle.colors[0], basicStyle.alpha)
      },
      {
        key: `yAxisExt-${secondAxis?.id ?? 'second'}`,
        name: getDisplayName(secondAxis, secondData.name),
        color: hexColorToRGBA(basicStyle.colors[1], basicStyle.alpha)
      }
    ]
    const legendNameMap = legendItems.reduce((map, item) => {
      map[item.key] = item.name
      return map
    }, {} as Record<string, string>)
    const getLegendName = value => {
      const legendKey =
        typeof value === 'object' && value !== null ? value.id ?? value.value ?? value.label : value
      return legendNameMap[legendKey] ?? `${legendKey ?? ''}`
    }
    const chartContainer = chart.container as unknown
    const containerDom =
      typeof document === 'undefined' || !chartContainer
        ? undefined
        : typeof chartContainer === 'string'
        ? document.getElementById(chartContainer)
        : typeof (chartContainer as HTMLElement).getBoundingClientRect === 'function'
        ? (chartContainer as HTMLElement)
        : undefined
    const containerRect = containerDom?.getBoundingClientRect()
    const legendItemHeight = Math.ceil(Math.max(legendFontSize * 1.3, legendMarkerSize))
    const legendItemWidths = legendItems.map(item =>
      Math.ceil(
        Math.max(
          SIDE_LEGEND_MIN_LABEL_WIDTH,
          measureLegendTextWidth(item.name, legendFontSize, chart.fontFamily || 'sans-serif')
        ) +
          legendMarkerSize +
          legendItemSpacing +
          SIDE_LEGEND_DEFAULT_COL_PADDING
      )
    )
    const getLegendGap = (direction: 'col' | 'row', legendFirst: boolean) =>
      direction === 'row' ? 8 : legendFirst ? 8 : 4
    const getLegendContentSize = (direction: 'col' | 'row', legendFirst = false) => {
      const legendGap = getLegendGap(direction, legendFirst)
      return direction === 'col'
        ? legendItemHeight + legendGap
        : Math.max(...legendItemWidths) + legendGap
    }
    const getLegendRatio = (direction: 'col' | 'row', legendFirst = false) => {
      const mainSize = direction === 'col' ? containerRect?.height : containerRect?.width
      const legendContentSize = getLegendContentSize(direction, legendFirst)
      const legendMainSize =
        direction === 'row'
          ? Math.min(legendContentSize, getSideLegendMaxWidth(Number(containerRect?.width)))
          : legendContentSize
      if (!mainSize || mainSize <= 0) {
        const fallbackLegendRatio = Math.max(1, Math.ceil(legendMainSize / 16))
        return legendFirst ? [fallbackLegendRatio, 20] : [20, fallbackLegendRatio]
      }
      const safeLegendSize = Math.max(1, Math.min(legendMainSize, mainSize - 1))
      const chartMainSize = Math.max(mainSize - safeLegendSize, 1)
      return legendFirst ? [safeLegendSize, chartMainSize] : [chartMainSize, safeLegendSize]
    }
    const setLegendRatio = (direction: 'col' | 'row', legendFirst = false) => {
      flexOptions.dataeaseBidirectionalLegendFlex = {
        direction,
        legendFirst,
        legendContentSize: getLegendContentSize(direction, legendFirst)
      } satisfies BidirectionalLegendFlexLayout
      flexOptions.ratio = getLegendRatio(direction, legendFirst)
    }
    const keepTopLegendPlotInset = () => {
      if (basicStyle.layout !== 'horizontal') {
        return
      }
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
    const horizontalLegendTextStyle = getHorizontalLegendTextStyle(legendFontSize)
    const enableHorizontalLegendText = legendOption => {
      Object.assign(legendOption, horizontalLegendTextStyle)
      const labelFormatter = horizontalLegendTextStyle.labelFormatter
      const itemPoptip = horizontalLegendTextStyle.itemPoptip
      legendOption.labelFormatter = value => labelFormatter(getLegendName(value))
      legendOption.itemPoptip = ({ id, label }) => itemPoptip({ id: getLegendName(id), label })
    }
    const enableSideLegendLayout = legendOption => {
      legendOption.dataeaseSideLegendAutoLayout = true
      legendOption.dataeaseSideLegendMaxWidthRatio = 1
      legendOption.navOrientation = 'vertical'
      legendOption.maxCols = 1
    }
    const legendOpt: any = {
      key: 'legends',
      type: 'legends',
      scale: {
        color: {
          type: 'ordinal',
          domain: legendItems.map(item => item.key),
          range: legendItems.map(item => item.color)
        }
      },
      position: 'top',
      layout: {
        justifyContent: 'center',
        alignItems: 'center'
      },
      crossPadding: 8,
      rowPadding: legendRowPadding,
      colPadding: SIDE_LEGEND_DEFAULT_COL_PADDING,
      itemSpacing: [legendItemSpacing, legendItemSpacing, legendItemSpacing],
      itemMarker: legend.icon,
      labelFormatter: getLegendName,
      ...getCategoryLegendStyle(legendMarkerSize, legendFontSize, legend.color)
    }
    const hPosition = ['left', 'center', 'right'].includes(legend.hPosition)
      ? legend.hPosition
      : 'center'
    const rawVPosition = ['top', 'center', 'bottom'].includes(legend.vPosition)
      ? legend.vPosition
      : 'bottom'
    const vPosition = hPosition === 'center' && rawVPosition === 'center' ? 'top' : rawVPosition
    if (hPosition === 'center') {
      flexOptions.direction = 'col'
      legendOpt.layout.flexDirection = 'row'
      legendOpt.maxRows = 1
      enableHorizontalLegendText(legendOpt)
      if (vPosition === 'top') {
        legendOpt.position = 'top'
        legendOpt.crossPadding = getLegendGap('col', true)
        legendOpt.marginTop = 0
        legendOpt.marginBottom = 0
        setLegendRatio('col', true)
        flexOptions.children.unshift(legendOpt)
        keepTopLegendPlotInset()
      }
      if (vPosition === 'bottom') {
        legendOpt.position = 'bottom'
        legendOpt.crossPadding = getLegendGap('col', false)
        legendOpt.marginTop = 0
        legendOpt.marginBottom = 0
        setLegendRatio('col')
        flexOptions.children.push(legendOpt)
      }
      return options
    }
    if (vPosition === 'center') {
      flexOptions.direction = 'row'
      flexOptions.padding = 0
      if (chartOptions) {
        chartOptions.margin = 0
      }
      legendOpt.position = hPosition
      legendOpt.layout.justifyContent = 'center'
      legendOpt.layout.flexDirection = 'col'
      legendOpt.crossPadding = getLegendGap('row', hPosition === 'left')
      legendOpt.marginLeft = 0
      legendOpt.marginRight = 0
      enableSideLegendLayout(legendOpt)
      if (hPosition === 'left') {
        setLegendRatio('row', true)
        flexOptions.children.unshift(legendOpt)
      }
      if (hPosition === 'right') {
        setLegendRatio('row')
        flexOptions.children.push(legendOpt)
      }
      return options
    }
    flexOptions.direction = 'col'
    legendOpt.position = vPosition
    legendOpt.layout.justifyContent = hPosition === 'left' ? 'flex-start' : 'flex-end'
    legendOpt.layout.flexDirection = 'row'
    legendOpt.maxRows = 1
    enableHorizontalLegendText(legendOpt)
    if (vPosition === 'top') {
      legendOpt.crossPadding = getLegendGap('col', true)
      legendOpt.marginTop = 0
      legendOpt.marginBottom = 0
      setLegendRatio('col', true)
      flexOptions.children.unshift(legendOpt)
      keepTopLegendPlotInset()
    }
    if (vPosition === 'bottom') {
      legendOpt.crossPadding = getLegendGap('col', false)
      legendOpt.marginTop = 0
      legendOpt.marginBottom = 0
      setLegendRatio('col')
      flexOptions.children.push(legendOpt)
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
