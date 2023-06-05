import { Plot } from '@antv/g2plot'
import {
  getAnalyse,
  getLabel,
  getLegend,
  getSlider,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis
} from '@/views/chart/components/js/panel/common/common_antv'
import { PickOptions } from '@antv/g2plot/lib/core/plot'

export enum ChartRenderType {
  ANT_V = 'antv',
  ECHARTS = 'echarts'
}

export abstract class AbstractChartView {
  render: string
  name: string
  protected defaultData: any[]

  protected constructor(render: string, name: string) {
    this.render = render
    this.name = name
  }
}

export interface G2PlotDrawOptions<O> {
  /**
   * 生成的视图对象
   */
  chartObj: O
  /**
   * dom容器id
   */
  container: string
  /**
   * 数据库中的视图配置对象
   */
  chart: Chart
  /**
   * 事件回调函数
   * @param args 事件参数
   */
  action?: (...args: any[]) => any
}

/**
 * G2Plot 的视图抽象类
 */
export abstract class G2PlotChartView<
  O extends PickOptions,
  P extends Plot<O>
> extends AbstractChartView {
  /**
   * 根据参数构建视图对象然后返回
   * @param drawOptions 视图配置参数
   * @return 生成的视图对象，类型为 Plot 的子类
   */
  public abstract drawChart(drawOptions: G2PlotDrawOptions<P>): P

  protected configTheme(chart: Chart, options: O): O {
    const theme = getTheme(chart)
    return { ...options, theme }
  }

  protected configLabel(chart: Chart, options: O): O {
    const label = getLabel(chart)
    return { ...options, label }
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
    const analyse = getAnalyse(chart)
    return { ...options, annotations: analyse }
  }
  /**
   * 流式配置公共参数，处理常用的配置，后续如果有其他通用配置也可以放进来，需要单独配置的属性在各个视图自行实现。
   * @param chart 数据库视图对象。
   * @param options 各个视图的参数，泛化的 Options，可以自行扩展，比如加个扩展 X 轴或者扩展 Y 轴字段。
   */
  protected abstract setupOptions(chart: Chart, options: O): O
  protected constructor(name: string, defaultData: any[]) {
    super(ChartRenderType.ANT_V, name)
    this.defaultData = defaultData
  }
}

/**
 * Echarts 视图的抽象类
 */
export abstract class EchartsChartView extends AbstractChartView {
  protected constructor(name: string, defaultData: any[]) {
    super(ChartRenderType.ECHARTS, name)
    this.defaultData = defaultData
  }
}
// S2 or others to be defined next
