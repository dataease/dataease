import {
  AntVAbstractChartView,
  AntVDrawOptions,
  ChartLibraryType
} from '@/views/chart/components/js/panel/types'
import {
  S2Theme,
  SpreadSheet,
  Style,
  type RawData,
  S2Options,
  Meta,
  SERIES_NUMBER_FIELD,
  setTooltipContainerStyle,
  S2DataConfig,
  S2Event,
  TableSheet,
  ViewMeta
} from '@antv/s2'
import {
  configHeaderInteraction,
  configMergeCells,
  configTooltip,
  getTooltipPosition,
  SortTooltip,
  getConditions,
  getCustomTheme,
  getStyle,
  handleTableEmptyStrategy
} from '@/views/chart/components/js/panel/common/common_table'
import '@antv/s2/dist/s2.min.css'
import { find, merge } from 'lodash-es'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { Renderer as SVGRenderer } from '@antv/g-svg'
import { Image as GImage, type FederatedPointerEvent } from '@antv/g'
import { parseJson } from '@/views/chart/components/js/util'
import { getTableTooltipFields } from '../../charts/table/common'

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

  protected configConditions(
    chart: Chart,
    displayFieldNames?: string[],
    drillFieldMap: Record<string, string> = {}
  ) {
    return getConditions(chart, displayFieldNames, drillFieldMap)
  }

  protected configMergeCells(chart: Chart, option: S2Options, dataConfig: S2DataConfig) {
    configMergeCells(chart, option, dataConfig)
  }

  private getTableTooltipContent(
    sheet: TableSheet,
    meta: ViewMeta,
    metaConfig: Meta[],
    chart: Chart,
    selectedFields: string[]
  ) {
    const row = sheet.dataSet.getRowData(meta) as RawData | undefined
    if (!row || row.SUMMARY || meta.valueField === SERIES_NUMBER_FIELD) {
      return
    }
    // 从 S2 当前展示行取值，排序和分页后仍能命中正确的行，也保留后端脱敏结果。
    const tooltipFields = getTableTooltipFields(chart.type, chart.xAxis, chart.yAxis)
    const fields = selectedFields.filter(
      name =>
        Object.prototype.hasOwnProperty.call(row, name) &&
        tooltipFields.some(field => field.dataeaseName === name)
    )
    if (!fields.length) {
      return
    }
    const visibleColumns = sheet.facet.getColLeafNodes()
    const names = [...new Set([...fields, meta.valueField])]
    const showFieldNames = names.length > 1
    const content = document.createElement('div')
    Object.assign(content.style, {
      boxSizing: 'border-box',
      display: 'grid',
      // 窄提示框内为字段值预留宽度，避免长字段名挤占整行。
      gridTemplateColumns: showFieldNames ? 'minmax(0, 2fr) minmax(0, 3fr)' : 'minmax(0, 1fr)',
      gap: '8px 16px',
      overflow: 'auto',
      overflowWrap: 'anywhere',
      overscrollBehavior: 'contain',
      textAlign: 'left',
      touchAction: 'pan-y',
      whiteSpace: 'pre-wrap'
    })
    content.dataset.tableTooltipCell = meta.id
    for (const name of names) {
      const value = row[name]
      const formatter = metaConfig.find(item => item.field === name)?.formatter
      const formatted = value == null || value === '' ? '—' : formatter?.(value) ?? value
      if (showFieldNames) {
        const field = tooltipFields.find(field => field.dataeaseName === name)
        const label = document.createElement('span')
        label.textContent =
          visibleColumns.find(column => column.field === name)?.value ??
          field?.chartShowName ??
          field?.name ??
          name
        content.appendChild(label)
      }
      const valueElement = document.createElement('span')
      valueElement.style.textAlign = 'right'
      valueElement.textContent = String(formatted)
      content.appendChild(valueElement)
    }
    content.addEventListener('pointerleave', event => {
      if (event.pointerType === 'mouse' && sheet.tooltip.options?.content === content) {
        sheet.hideTooltip()
      }
    })
    return content
  }

  protected showTooltip(s2Instance: P, event, metaConfig: Meta[], chart?: Chart) {
    // 排序菜单打开时，悬浮单元格不能覆盖菜单内容或重置容器样式
    if (s2Instance.tooltip instanceof SortTooltip && s2Instance.tooltip.isSortMenuVisible()) {
      return
    }
    const cell = s2Instance.getCell(event.target)
    if (!cell) {
      return
    }
    const meta = cell.getMeta()
    const activeContent = s2Instance.tooltip.options?.content
    const activeTableTooltip =
      activeContent instanceof HTMLElement && activeContent.dataset.tableTooltipCell
    const tooltipConfig = chart && parseJson(chart.customAttr)?.tooltip
    const selectedFields = tooltipConfig?.tableDisplayFields
    let tableContent: HTMLElement | undefined
    if (
      cell.cellType === 'dataCell' &&
      s2Instance instanceof TableSheet &&
      selectedFields?.length
    ) {
      if (event.pointerType === 'touch' && event.type !== 'touchend') {
        return
      }
      const { tooltip } = s2Instance
      if (tooltip.visible && tooltip.container && activeTableTooltip === meta.id) {
        // 同一单元格只更新位置，保留提示内容和长文本滚动位置。
        event.s2Instance = s2Instance
        event.tableTooltipContent = activeContent
        const position = getTooltipPosition(event)
        tooltip.position = position
        tooltip.container.style.left = `${position.x}px`
        tooltip.container.style.top = `${position.y}px`
        return true
      }
      tableContent = this.getTableTooltipContent(
        s2Instance,
        meta as ViewMeta,
        metaConfig,
        chart,
        selectedFields
      )
      if (tableContent && !tooltipConfig.show) {
        return
      }
    }
    let content: string | HTMLElement = ''
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
        content = meta.value
        field = metaConfig.find(item => item.field === content)
        if (field?.name) {
          content = field.name
        }
        break
    }
    content = tableContent ?? content
    if (!content) {
      if (activeTableTooltip) {
        s2Instance.hideTooltip()
      }
      return
    }
    event.s2Instance = s2Instance
    event.tableTooltipContent = tableContent
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
    return true
  }

  protected configTouchEvent(s2Instance: P, option: S2DrawOptions<P>, meta: Meta[]) {
    const { chart, touchAction } = option
    const hasTableDisplayFields =
      s2Instance instanceof TableSheet &&
      !!parseJson(chart.customAttr)?.tooltip?.tableDisplayFields?.length
    // touch action
    s2Instance.once(S2Event.LAYOUT_AFTER_RENDER, () => {
      const touchActionInit = s2Instance.store.get('touchActionInit')
      if (touchActionInit) {
        return
      }
      s2Instance.store.set('touchActionInit', true)
      const canvas = s2Instance.container
      let startTouch: { x: number; y: number; time: number } | undefined
      let moved = false
      canvas.on('touchstart', (event: FederatedPointerEvent) => {
        moved = !event.isPrimary
        startTouch = { x: event.clientX, y: event.clientY, time: Date.now() }
      })
      if (hasTableDisplayFields) {
        canvas.on('touchmove', (event: FederatedPointerEvent) => {
          if (
            startTouch &&
            Math.hypot(event.clientX - startTouch.x, event.clientY - startTouch.y) > 8
          ) {
            moved = true
          }
        })
      }
      canvas.on('touchend', (event: FederatedPointerEvent) => {
        const touch = startTouch
        startTouch = undefined
        // 超过 300ms 触发复制
        if (!touch || Date.now() - touch.time > 300 || (hasTableDisplayFields && moved)) {
          return
        }
        // 使用画布事件的命中结果，和悬停共用缩放坐标。图片预览和排序图标优先。
        if (event.target instanceof GImage) {
          return
        }
        touchAction?.(() => {
          if (this.showTooltip(s2Instance, event, meta, chart)) {
            event.preventDefault()
          }
        })
      })
    })
  }

  protected configRenderer(option: S2Options) {
    const dvMainStore = dvMainStoreWithOut()
    const enableSvgRenderer = dvMainStore?.canvasStyleData?.enableSvgRenderer
    if (enableSvgRenderer) {
      merge(option, {
        transformCanvasConfig() {
          return {
            renderer: new SVGRenderer(),
            supportsCSSTransform: true
          }
        },
        csp: {
          iconStrategy: 'path'
        }
      })
    }
  }
}
