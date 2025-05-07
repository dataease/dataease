import { G2Spec, type Chart as G2Chart } from '@antv/g2'
import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import { configEmptyDataStyle } from '@/views/chart/components/js/panel/common/common_antv'
import { parseJson, setupSeriesColor } from '../../../util'

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
          const orient = l.orient
          const legendSymbol = l.icon
          const legendSize = l.size
          const legendFontSize = l.fontSize
          const legendColor = l.color
          // position 图例布局
          // layoutJustifyContent 图例实例布局
          let layoutJustifyContent = 'center'
          // 根据图例方向和位置设置布局和位置
          if (orient === 'horizontal') {
            // 水平布局
            position = l.vPosition === 'center' ? 'bottom' : l.vPosition
            layoutJustifyContent =
              l.hPosition === 'left' && l.vPosition !== 'center'
                ? 'flex-start'
                : l.hPosition === 'right' && l.vPosition !== 'center'
                ? 'flex-end'
                : 'center'
          } else {
            // 垂直布局
            position = l.hPosition === 'center' ? 'left' : l.hPosition
            layoutJustifyContent =
              l.vPosition === 'top' && l.hPosition !== 'center'
                ? 'flex-start'
                : l.vPosition === 'bottom' && l.hPosition !== 'center'
                ? 'flex-end'
                : 'center'
          }
          legend = {
            orientation: orient,
            position,
            layout: {
              justifyContent: layoutJustifyContent
            },
            itemMarker: legendSymbol,
            itemMarkerSize: legendSize,
            itemLabelFontSize: legendFontSize,
            itemLabelFill: legendColor,
            navPageNumFontSize: legendSize,
            navPageNumFill: legendColor,
            navButtonSize: legendSize,
            navOrientation: position === 'left' || position === 'right' ? 'vertical' : 'horizontal'
          }
        } else {
          legend = false
        }
      }
    }
    return legend
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setupSeriesColor(chart, data)
  }

  public setupSubSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
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
