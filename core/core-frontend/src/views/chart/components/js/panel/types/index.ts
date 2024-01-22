import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

export enum ChartRenderType {
  ANT_V = 'antv',
  ECHARTS = 'echarts',
  CUSTOM = 'custom'
}

export enum ChartLibraryType {
  G2_PLOT = 'g2plot',
  L7_PLOT = 'l7plot',
  ECHARTS = 'echarts',
  S2 = 's2',
  RICH_TEXT = 'rich-text',
  INDICATOR = 'indicator'
}

export abstract class AbstractChartView {
  render: ChartRenderType
  library: ChartLibraryType
  name: string
  title: string
  abstract properties: EditorProperty[]
  abstract propertyInner: EditorPropertyInner
  abstract axis: AxisType[]
  abstract axisConfig: AxisConfig
  /**
   * 在新建和切换视图的时候处理默认值
   * @param chart 数据库视图对象
   */
  setupDefaultOptions(chart: ChartObj): ChartObj {
    return chart
  }

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
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.chart_group')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    extStack: {
      name: `${t('chart.stack_item')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  protected constructor(library: ChartLibraryType, name: string, defaultData?: any[]) {
    super(ChartRenderType.ANT_V, library, name, defaultData)
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
