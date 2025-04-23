import { G2Spec, type Chart as G2Chart } from '@antv/g2'
import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import { configEmptyDataStyle } from '@/views/chart/components/js/panel/common/common_antv'
import { setupSeriesColor } from '../../../util'

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
