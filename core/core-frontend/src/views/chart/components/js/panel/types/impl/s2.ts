import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import {
  S2Theme,
  SpreadSheet,
  Style,
  S2Options,
  Meta,
  SERIES_NUMBER_FIELD,
  setTooltipContainerStyle,
  S2DataConfig,
  S2Event
} from '@antv/s2'
import {
  configHeaderInteraction,
  configMergeCells,
  configTooltip,
  getConditions,
  getCustomTheme,
  getStyle,
  handleTableEmptyStrategy
} from '@/views/chart/components/js/panel/common/common_table'
import '@antv/s2/dist/style.min.css'
import { find } from 'lodash-es'

declare interface PageInfo {
  currentPage: number
  pageSize: number
  total: number
}

export interface S2DrawOptions<O> extends AntVDrawOptions<O> {
  pageInfo?: PageInfo
  resizeAction?: (...args: any) => void
  touchAction?: (...args: any) => void
}
export abstract class S2ChartView<P extends SpreadSheet> extends AntVAbstractChartView {
  public abstract drawChart(drawOption: S2DrawOptions<P>): P
  protected constructor(name: string, defaultData: any[]) {
    super(ChartLibraryType.S2, name, defaultData)
  }
  protected configTheme(chart: Chart): S2Theme {
    return getCustomTheme(chart)
  }

  protected configStyle(chart: Chart, s2DataConfig: S2DataConfig): Style {
    return getStyle(chart, s2DataConfig)
  }

  protected configEmptyDataStrategy(chart: Chart): Record<string, any>[] {
    return handleTableEmptyStrategy(chart)
  }

  protected configTooltip(chart: Chart, option: S2Options) {
    configTooltip(chart, option)
  }

  protected configHeaderInteraction(chart: Chart, option: S2Options) {
    configHeaderInteraction(chart, option)
  }

  protected configConditions(chart: Chart) {
    return getConditions(chart)
  }

  protected configMergeCells(chart: Chart, option: S2Options, dataConfig: S2DataConfig) {
    configMergeCells(chart, option, dataConfig)
  }

  protected showTooltip(s2Instance: P, event, metaConfig: Meta[]) {
    const cell = s2Instance.getCell(event.target)
    const meta = cell.getMeta()
    let content = ''
    let field
    switch (cell.cellType) {
      case 'dataCell':
      case 'mergedCell':
        if (meta.valueField === SERIES_NUMBER_FIELD) {
          content = meta.fieldValue.toString()
          break
        }
        field = find(metaConfig, item => item.field === meta.valueField)
        if (meta.fieldValue === 0) {
          content = '0'
        }
        if (meta.fieldValue) {
          content = field?.formatter?.(meta.fieldValue)
        }
        break
      case 'rowCell':
      case 'colCell':
        content = meta.label
        field = find(metaConfig, item => item.field === content)
        if (field) {
          content = field.name
        }
        break
    }
    if (!content) {
      return
    }
    event.s2Instance = s2Instance
    const style = s2Instance.options.tooltip.style
    setTooltipContainerStyle(s2Instance.tooltip.container, { style })
    s2Instance.showTooltip({
      position: {
        x: event.clientX,
        y: event.clientY
      },
      content,
      meta,
      event
    })
  }

  protected configTouchEvent(s2Instance: P, option: S2DrawOptions<P>, meta: Meta[]) {
    const { touchAction } = option
    // touch action
    s2Instance.once(S2Event.LAYOUT_AFTER_RENDER, () => {
      const touchActionInit = s2Instance.store.get('touchActionInit')
      if (touchActionInit) {
        return
      }
      s2Instance.store.set('touchActionInit', true)
      const canvas = s2Instance.getCanvasElement()
      let startTime = Date.now()
      canvas.addEventListener('touchstart', () => {
        startTime = Date.now()
      })
      canvas.addEventListener('touchend', e => {
        const duration = Date.now() - startTime
        // 超过 300ms 触发复制
        if (duration > 300) {
          return
        }
        const canvasPosition = canvas.getBoundingClientRect()
        const touchPosition = [e.changedTouches[0].pageX, e.changedTouches[0].pageY]
        const relativePosition = [
          touchPosition[0] - canvasPosition.x,
          touchPosition[1] - canvasPosition.y
        ]
        const shape = s2Instance.container.getShape(relativePosition[0], relativePosition[1])
        // 图片单元格，表头排序图标点击放大图片
        if (shape.cfg?.type === 'image') {
          return
        }
        const callback = () => {
          e.preventDefault()
          e.stopPropagation()
          if (shape) {
            const event = {
              target: shape,
              x: relativePosition[0],
              y: relativePosition[1],
              clientX: touchPosition[0],
              clientY: touchPosition[1],
              originEvent: e
            }
            this.showTooltip(s2Instance, event, meta)
          }
        }
        touchAction(callback)
      })
    })
  }
}
