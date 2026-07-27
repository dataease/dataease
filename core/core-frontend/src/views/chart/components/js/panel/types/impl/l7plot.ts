import type { ViewLevel } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import type { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import type { PlotOptions } from '@antv/l7plot/dist/esm/types/plot'
import type { Plot as L7Plot } from '@antv/l7plot/dist/esm/core/plot'
import {
  configL7Label,
  configL7Legend,
  configL7PlotZoom,
  configL7Style,
  configL7Tooltip
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { parseJson } from '@/views/chart/components/js/util'

export interface L7PlotDrawOptions<P> extends AntVDrawOptions<P> {
  areaId?: string
  level?: ViewLevel['level']
  geoJson?: FeatureCollection
  scope?: string[]
  // 表示GADM层级关系，name1@name2@name3,用于过滤geojson数据
  gadmName?: string
}
// S2 or others to be defined next
export abstract class L7PlotChartView<
  O extends PlotOptions,
  P extends L7Plot<O>
> extends AntVAbstractChartView {
  public abstract drawChart(drawOption: L7PlotDrawOptions<P>): P | Promise<P>

  protected configLabel(chart: Chart, options: O): O {
    const label = configL7Label(chart)
    defaultsDeep(options.label, label)
    return options
  }

  protected configStyle(chart: Chart, options: O): O {
    const style = configL7Style(chart)
    defaultsDeep(options['style'], style)
    return options
  }

  protected configTooltip(chart: Chart, options: O): O {
    const tooltip = configL7Tooltip(chart)
    defaultsDeep(options.tooltip, tooltip)
    return options
  }
  protected configLegend(chart: Chart, options: O): O {
    const legend = configL7Legend(chart)
    defaultsDeep(options, { legend })
    return options
  }

  protected getDataByEmptyDataStrategy(chart: Chart, sourceData: any[]): any[] {
    const { functionCfg } = parseJson(chart.senior)
    const emptyDataStrategy = functionCfg.emptyDataStrategy
    if (!emptyDataStrategy || emptyDataStrategy === 'breakLine') {
      return sourceData
    }
    const data = cloneDeep(sourceData)
    if (emptyDataStrategy === 'setZero') {
      data.forEach(item => {
        item.value === null && (item.value = 0)
        item.dynamicTooltipValue?.length > 0 &&
          item.dynamicTooltipValue.forEach(ele => {
            ele.value === null && (ele.value = 0)
          })
      })
    }
    if (emptyDataStrategy === 'ignoreData') {
      for (let i = data.length - 1; i >= 0; i--) {
        if (data[i].value === null) {
          data.splice(i, 1)
        }
        for (let j = data[i]?.dynamicTooltipValue?.length - 1; j >= 0; j--) {
          if (data[i].dynamicTooltipValue[j].value === null) {
            data[i].dynamicTooltipValue.splice(j, 1)
          }
        }
      }
    }
    return data
  }

  protected getIgnoredDataFields(chart: Chart): Set<string> {
    const { functionCfg } = parseJson(chart.senior)
    if (functionCfg.emptyDataStrategy !== 'ignoreData') {
      return new Set()
    }
    return (chart.data?.data || []).reduce((fields, item) => {
      if (item.value === null) {
        item.field !== undefined && fields.add(String(item.field))
        item.name !== undefined && fields.add(String(item.name))
      }
      return fields
    }, new Set<string>())
  }

  protected configEmptyDataStrategy(chart: Chart, options: O): O {
    // 提前返回的地图渲染分支也复用同一份空值策略处理
    options.source.data = this.getDataByEmptyDataStrategy(chart, options.source.data)
    return options
  }

  protected configZoomButton(chart: Chart, plot: P) {
    configL7PlotZoom(chart, plot)
  }
  protected constructor(name: string, defaultData?: any[]) {
    super(ChartLibraryType.L7_PLOT, name)
    this.defaultData = defaultData
  }
  protected abstract setupOptions(chart: Chart, options: O, context?: Record<string, any>): O
}
