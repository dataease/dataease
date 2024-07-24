import { Scene } from '@antv/l7-scene'
import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType,
  ChartWrapper
} from '@/views/chart/components/js/panel/types'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { parseJson } from '@/views/chart/components/js/util'
import { ILayer } from '@antv/l7plot'
import {
  configL7Label,
  configL7Tooltip,
  configL7Zoom
} from '@/views/chart/components/js/panel/common/common_antv'
import { queryMapKeyApi } from '@/api/setting/sysParameter'
import { useMapStoreWithOut } from '@/store/modules/map'
const mapStore = useMapStoreWithOut()

export type L7DrawConfig<P> = AntVDrawOptions<P>
export interface L7Config extends ILayer {
  handleConfig?: (arg0: Scene) => void
  [key: string]: string | any
}
export class L7Wrapper<
  O extends L7Config | Array<L7Config>,
  S extends Scene
> extends ChartWrapper<S> {
  private readonly config: O | Array<O>
  private readonly scene: S | null = null
  constructor(scene: S, l7config: O | Array<O> | undefined) {
    super()
    this.chartInstance = scene
    this.config = l7config
    this.scene = scene
  }
  destroy = () => {
    if (!this.chartInstance) {
      return
    }
    this.chartInstance?.destroy()
  }
  render = () => {
    if (this.scene && this.config) {
      this.scene.on('loaded', () => {
        if (Array.isArray(this.config)) {
          this.config?.forEach(p => {
            this.handleConfig(p)
          })
        } else {
          this.handleConfig(this.config)
        }
      })
    }
  }

  handleConfig = (config: L7Config) => {
    if (config) {
      if (config.handleConfig) {
        config.handleConfig?.(this.scene)
      } else {
        this.scene.addLayer(config)
      }
    }
  }
}
export abstract class L7ChartView<
  S extends Scene,
  O extends L7Config
> extends AntVAbstractChartView {
  public abstract drawChart(drawOption: L7DrawConfig<O>): L7Wrapper<O, S> | any

  protected configEmptyDataStrategy(chart: Chart, options: O): O {
    const { functionCfg } = parseJson(chart.senior)
    const emptyDataStrategy = functionCfg.emptyDataStrategy
    if (!emptyDataStrategy || emptyDataStrategy === 'breakLine') {
      return options
    }
    const data = cloneDeep(options.sourceOption.data)
    if (emptyDataStrategy === 'setZero') {
      data.forEach(item => {
        item.value === null && (item.value = 0)
      })
    }
    if (emptyDataStrategy === 'ignoreData') {
      for (let i = data.length - 1; i >= 0; i--) {
        if (data[i].value === null) {
          data.splice(i, 1)
        }
      }
    }
    options.sourceOption.data = data
    return options
  }

  protected configZoomButton(chart: Chart, plot: S) {
    configL7Zoom(chart, plot)
  }

  protected configLabel(chart: Chart, options: O): O {
    const label = configL7Label(chart)
    defaultsDeep(options.label, label)
    return options
  }

  protected configTooltip(chart: Chart, options: O): O {
    const tooltip = configL7Tooltip(chart)
    defaultsDeep(options.tooltip, tooltip)
    return options
  }

  protected constructor(name: string, defaultData: any[]) {
    super(ChartLibraryType.L7, name, defaultData)
  }

  protected getMapKey = async () => {
    if (!mapStore.mapKey) {
      await queryMapKeyApi().then(res => mapStore.setKey(res.data))
    }
    return mapStore.mapKey
  }

  protected abstract setupOptions(chart: Chart, options: O): O
}
