import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import { S2Theme, SpreadSheet, Style } from '@antv/s2'
import {
  getConditions,
  getCustomTheme,
  getStyle,
  handleTableEmptyStrategy
} from '@/views/chart/components/js/panel/common/common_table'

declare interface PageInfo {
  currentPage: number
  pageSize: number
  total: number
}

export interface S2DrawOptions<O> extends AntVDrawOptions<O> {
  pageInfo?: PageInfo
  tableHeaderClick?: (args: any[]) => void
}

export abstract class S2ChartView<P extends SpreadSheet> extends AntVAbstractChartView {
  public abstract drawChart(drawOption: S2DrawOptions<P>): P
  protected constructor(name: string, defaultData: any[]) {
    super(ChartLibraryType.S2, name, defaultData)
  }
  protected configTheme(chart: Chart): S2Theme {
    return getCustomTheme(chart)
  }

  protected configStyle(chart: Chart): Style {
    return getStyle(chart)
  }

  protected configEmptyDataStrategy(chart: Chart): Record<string, any>[] {
    return handleTableEmptyStrategy(chart)
  }

  protected configConditions(chart: Chart) {
    return getConditions(chart)
  }
}
