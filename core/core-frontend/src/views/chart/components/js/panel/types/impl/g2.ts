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

export const LEGEND_NAV_CONTROLLER_PADDING = 10
export const LEGEND_NAV_CONTROLLER_SPACING = 12
export const getLegendNavButtonPath = (size: number) => [
  ['M', -size / 2, -size / 2],
  ['L', size / 2, -size / 2],
  ['L', 0, size / 2],
  ['Z']
]

// 统一分类图例的 marker、label 与分页器样式，避免各图表配置漂移
export const getCategoryLegendStyle = (markerSize: number, fontSize: number, color: string) => ({
  itemMarkerSize: markerSize,
  itemLabelFontSize: fontSize,
  itemLabelFill: color,
  itemLabelFillOpacity: 1,
  itemLabelOpacity: 1,
  itemMarkerLineWidth: 0,
  navPageNumFontSize: fontSize,
  navPageNumFill: color,
  navPageNumFillOpacity: 1,
  navButtonD: getLegendNavButtonPath(markerSize),
  navButtonSize: markerSize,
  navButtonFill: color,
  navButtonFillOpacity: 1,
  navOrientation: 'horizontal',
  navControllerPadding: LEGEND_NAV_CONTROLLER_PADDING,
  navControllerSpacing: LEGEND_NAV_CONTROLLER_SPACING
})

let axisLabelMeasureContext: CanvasRenderingContext2D | null | undefined

const measureAxisLabelWidth = (value: unknown, fontSize: number) => {
  const label =
    typeof value === 'object' && value !== null
      ? `${value['label'] ?? value['value'] ?? ''}`
      : `${value ?? ''}`
  axisLabelMeasureContext ??= document.createElement('canvas').getContext('2d')
  if (axisLabelMeasureContext) {
    axisLabelMeasureContext.font = `${fontSize}px sans-serif`
    return axisLabelMeasureContext.measureText(label).width
  }
  return Array.from(label).reduce(
    (width, char) => width + fontSize * (/[^\x00-\xff]/.test(char) ? 1 : 0.6),
    0
  )
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
    if (position === 'top' || position === 'bottom') {
      const direction = position === 'top' ? -1 : 1
      return {
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
      return {
        labelSpacing: 4 + (fontSize * rotateRatio) / 2,
        labelTextAlign: position === 'left' ? 'right' : 'left',
        labelTextBaseline: 'middle',
        labelTransform: `rotate(${rotate})`
      }
    }
    return { labelTransform: `rotate(${rotate})` }
  }

  protected getLegend = (chart: Chart) => {
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
          const legendSize = l.size
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
            ...(verticalLegend ? { maxCols: 1 } : { maxRows: 1 })
          }
        } else {
          legend = false
        }
      }
    }
    return legend
  }

  protected getAxis(axis: DeepPartial<ChartAxisStyle>): AxisComponent {
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
      tick: axis.axisLabel.show && axis.axisLabel.showTick !== false,
      tickLineWidth: axis.axisLine.lineStyle.width,
      tickStroke: axis.axisLine.lineStyle.color,
      tickOpacity: 1,
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
