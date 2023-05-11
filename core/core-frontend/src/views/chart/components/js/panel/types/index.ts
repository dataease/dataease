import { Options } from '@antv/g2plot/src/types/common'
import { Plot } from '@antv/g2plot'

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
  chart: object
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
  O extends Options,
  P extends Plot<O>
> extends AbstractChartView {
  /**
   * 根据参数构建视图对象然后返回
   * @param drawOptions 视图配置参数
   * @return 生成的视图对象，类型为 Plot 的子类
   */
  public abstract drawChart(drawOptions: G2PlotDrawOptions<P>): P

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
