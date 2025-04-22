import { G2Spec, type Chart as G2Chart } from '@antv/g2'
import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import {
  getAnalyse,
  getAnalyseHorizontal,
  getLabel,
  getLegend,
  getMultiSeriesTooltip,
  getSlider,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis,
  getConditions,
  handleConditionsStyle,
  addConditionsStyleColorToData,
  configEmptyDataStyle
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  getColor,
  getGroupColor,
  getSingleDimensionColor,
  getStackColor,
  handleEmptyDataStrategy,
  setupSeriesColor
} from '../../../util'

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

     protected configTheme(chart: Chart, options: O): O {
        const theme = getTheme(chart)
        return { ...options, theme }
      }

      protected configLabel(chart: Chart, options: O): O {
        const label = getLabel(chart)
        return { ...options, label }
      }

      protected configMultiSeriesTooltip(chart: Chart, options: O): O {
        const tooltip = getMultiSeriesTooltip(chart)
        return { ...options, tooltip }
      }

      protected configTooltip(chart: Chart, options: O): O {
        const tooltip = getTooltip(chart)
        return { ...options, tooltip }
      }
      protected configLegend(chart: Chart, options: O): O {
        const legend = getLegend(chart)
        return { ...options, legend }
      }

      protected configXAxis(chart: Chart, options: O): O {
        const xAxis = getXAxis(chart)
        return { ...options, xAxis }
      }

      protected configYAxis(chart: Chart, options: O): O {
        const yAxis = getYAxis(chart)
        return { ...options, yAxis }
      }

      protected configSlider(chart: Chart, options: O): O {
        const slider = getSlider(chart)
        return { ...options, slider }
      }

      protected configAnalyse(chart: Chart, options: O): O {
        const annotations = getAnalyse(chart)
        return {
          ...options,
          annotations: [...annotations, ...((options as unknown as O).annotations || [])]
        }
      }

      protected configAnalyseHorizontal(chart: Chart, options: O): O {
        const annotations = getAnalyseHorizontal(chart)
        return { ...options, annotations }
      }

      protected configEmptyDataStrategy(chart: Chart, options: O): O {
        return handleEmptyDataStrategy(chart, options)
      }

      protected configColor(chart: Chart, options: O): O {
        const color = getColor(chart)
        return { ...options, color }
      }

      protected configGroupColor(chart: Chart, options: O): O {
        const color = getGroupColor(chart, options)
        return { ...options, color }
      }

      protected configStackColor(chart: Chart, options: O): O {
        const color = getStackColor(chart, options)
        return { ...options, color }
      }

      protected configSingleDimensionColor(chart: Chart, options: O): O {
        const color = getSingleDimensionColor(chart, options)
        return { ...options, color }
      }

      public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
        return setupSeriesColor(chart, data)
      }

      public setupSubSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
        return undefined
      }

      protected configConditions(chart: Chart, options: O) {
        const annotations = getConditions(chart)
        return {
          ...options,
          annotations: [...annotations, ...((options as unknown as Options).annotations || [])]
        }
      }

      protected configBarConditions(chart: Chart, options: O) {
        return handleConditionsStyle(chart, options)
      }

      protected addConditionsStyleColorToData(chart: Chart, data: any[]) {
        return addConditionsStyleColorToData(chart, data)
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
