import { Plot } from '@antv/g2plot'
import {
  configL7Label,
  configL7Style,
  configL7Tooltip,
  getAnalyse,
  getAnalyseHorizontal,
  getLabel,
  getLegend,
  getSlider,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis
} from '@/views/chart/components/js/panel/common/common_antv'
import { PickOptions } from '@antv/g2plot/lib/core/plot'
import { PlotOptions } from '@antv/l7plot/dist/lib/types/plot'
import { Plot as L7Plot } from '@antv/l7plot/dist/lib/core/plot/index'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { deepAssign } from '@antv/g2plot/lib/utils'
import { ViewLevel } from '@antv/l7plot/dist/lib/plots/choropleth/types'
export enum ChartRenderType {
  ANT_V = 'antv',
  ECHARTS = 'echarts'
}

export enum ChartLibraryType {
  G2_PLOT = 'g2plot',
  L7_PLOT = 'l7plot',
  ECHARTS = 'echarts'
}
export abstract class AbstractChartView {
  render: ChartRenderType
  library: ChartLibraryType
  name: string

  protected defaultData: any[]

  protected constructor(
    render: ChartRenderType,
    library: ChartLibraryType,
    name: string,
    defaultData?: any[]
  ) {
    this.render = render
    this.library = library
    this.name = name
    this.defaultData = defaultData
  }
}

export interface AntVDrawOptions<O> {
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

export abstract class AntVAbstractChartView extends AbstractChartView {
  protected constructor(library: ChartLibraryType, name: string, defaultData?: any[]) {
    super(ChartRenderType.ANT_V, library, name, defaultData)
  }
}

export interface G2PlotDrawOptions<O> extends AntVDrawOptions<O> {
  /**
   * 缩放比例
   */
  scale?: number
}

/**
 * G2Plot 的视图抽象类
 */
export abstract class G2PlotChartView<
  O extends PickOptions,
  P extends Plot<O>
> extends AntVAbstractChartView {
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
    const annotations = getAnalyse(chart)
    return { ...options, annotations }
  }

  protected configAnalyseHorizontal(chart: Chart, options: O): O {
    const annotations = getAnalyseHorizontal(chart)
    return { ...options, annotations }
  }
  /**
   * 流式配置公共参数，处理常用的配置，后续如果有其他通用配置也可以放进来，需要单独配置的属性在各个视图自行实现。
   * @param chart 数据库视图对象。
   * @param options 各个视图的参数，泛化的 Options，可以自行扩展，比如加个扩展 X 轴或者扩展 Y 轴字段。
   */
  protected abstract setupOptions(chart: Chart, options: O): O
  protected constructor(name: string, defaultData: any[]) {
    super(ChartLibraryType.G2_PLOT, name, defaultData)
  }
}

/**
 * Echarts 视图的抽象类
 */
export abstract class EchartsChartView extends AbstractChartView {
  protected constructor(name: string, defaultData: any[]) {
    super(ChartRenderType.ECHARTS, ChartLibraryType.ECHARTS, name, defaultData)
  }
}
export interface L7PlotDrawOptions<P> extends AntVDrawOptions<P> {
  areaId?: string
  level?: ViewLevel['level']
  geoJson?: FeatureCollection
}
// S2 or others to be defined next
export abstract class L7PlotChartView<
  O extends PlotOptions,
  P extends L7Plot<O>
> extends AntVAbstractChartView {
  public abstract drawChart(drawOption: L7PlotDrawOptions<P>): P

  protected configLabel(chart: Chart, options: O): O {
    const label = configL7Label(chart)
    deepAssign(options.label, label)
    return options
  }

  protected configStyle(chart: Chart, options: O): O {
    const style = configL7Style(chart)
    deepAssign(options['style'], style)
    return options
  }

  protected configTooltip(chart: Chart, options: O): O {
    const tooltip = configL7Tooltip(chart)
    deepAssign(options.tooltip, tooltip)
    return options
  }

  protected constructor(name: string, defaultData?: any[]) {
    super(ChartLibraryType.L7_PLOT, name)
    this.defaultData = defaultData
  }
  protected abstract setupOptions(chart: Chart, options: O): O
}
