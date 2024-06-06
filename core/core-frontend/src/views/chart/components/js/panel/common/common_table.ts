import { copyString, hexColorToRGBA, parseJson, resetRgbOpacity } from '../..//util'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_TABLE_CELL,
  DEFAULT_TABLE_HEADER
} from '@/views/chart/components/editor/util/chart'
import {
  BaseTooltip,
  getAutoAdjustPosition,
  getTooltipDefaultOptions,
  S2Theme,
  setTooltipContainerStyle,
  Style,
  S2Options,
  SERIES_NUMBER_FIELD
} from '@antv/s2'
import { keys, intersection, filter, cloneDeep, merge, find } from 'lodash-es'
import { createVNode, render } from 'vue'
import TableTooltip from '@/views/chart/components/editor/common/TableTooltip.vue'

export function getCustomTheme(chart: Chart): S2Theme {
  const headerColor = hexColorToRGBA(
    DEFAULT_TABLE_HEADER.tableHeaderBgColor,
    DEFAULT_BASIC_STYLE.alpha
  )
  const headerAlign = DEFAULT_TABLE_HEADER.tableHeaderAlign
  const itemColor = hexColorToRGBA(DEFAULT_TABLE_CELL.tableItemBgColor, DEFAULT_BASIC_STYLE.alpha)
  const itemAlign = DEFAULT_TABLE_CELL.tableItemAlign
  const borderColor = hexColorToRGBA(
    DEFAULT_BASIC_STYLE.tableBorderColor,
    DEFAULT_BASIC_STYLE.alpha
  )
  const scrollBarColor = DEFAULT_BASIC_STYLE.tableScrollBarColor
  const scrollBarHoverColor = resetRgbOpacity(scrollBarColor, 3)

  const theme: S2Theme = {
    background: {
      color: '#00000000'
    },
    splitLine: {
      horizontalBorderColor: borderColor,
      verticalBorderColor: borderColor
    },
    cornerCell: {
      cell: {
        backgroundColor: headerColor,
        horizontalBorderColor: borderColor,
        verticalBorderColor: borderColor,
        verticalBorderWidth: 0
      },
      text: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      },
      measureText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      }
    },
    rowCell: {
      cell: {
        backgroundColor: headerColor,
        horizontalBorderColor: borderColor,
        verticalBorderColor: borderColor
      },
      text: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        textBaseline: 'middle'
      },
      bolderText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      },
      measureText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      },
      seriesText: {
        fill: DEFAULT_TABLE_CELL.tableItemBgColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: itemAlign
      }
    },
    colCell: {
      cell: {
        backgroundColor: headerColor,
        horizontalBorderColor: borderColor,
        verticalBorderColor: borderColor
      },
      text: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      },
      measureText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign
      }
    },
    dataCell: {
      cell: {
        backgroundColor: itemColor,
        horizontalBorderColor: borderColor,
        verticalBorderColor: borderColor
      },
      text: {
        fill: DEFAULT_TABLE_CELL.tableFontColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: itemAlign
      },
      bolderText: {
        fill: DEFAULT_TABLE_CELL.tableFontColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: itemAlign
      },
      measureText: {
        fill: DEFAULT_TABLE_CELL.tableFontColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: headerAlign
      }
    },
    scrollBar: {
      thumbColor: scrollBarColor,
      thumbHoverColor: scrollBarHoverColor
    }
  }

  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    const { basicStyle, tableHeader, tableCell } = customAttr
    // basic
    if (basicStyle) {
      const tableBorderColor = hexColorToRGBA(basicStyle.tableBorderColor, basicStyle.alpha)
      const tableScrollBarColor = basicStyle.tableScrollBarColor
      const tmpTheme: S2Theme = {
        splitLine: {
          horizontalBorderColor: tableBorderColor,
          verticalBorderColor: tableBorderColor
        },
        cornerCell: {
          cell: {
            horizontalBorderColor: tableBorderColor,
            verticalBorderColor: tableBorderColor
          }
        },
        colCell: {
          cell: {
            horizontalBorderColor: tableBorderColor,
            verticalBorderColor: tableBorderColor
          }
        },
        dataCell: {
          cell: {
            horizontalBorderColor: tableBorderColor,
            verticalBorderColor: tableBorderColor
          }
        },
        scrollBar: {
          thumbColor: tableScrollBarColor,
          thumbHoverColor: resetRgbOpacity(tableScrollBarColor, 1.5)
        }
      }
      merge(theme, tmpTheme)
    }
    // header
    if (tableHeader) {
      const tableHeaderFontColor = hexColorToRGBA(
        tableHeader.tableHeaderFontColor,
        basicStyle.alpha
      )
      const tableHeaderBgColor = hexColorToRGBA(tableHeader.tableHeaderBgColor, basicStyle.alpha)
      const { tableHeaderAlign, tableTitleFontSize } = tableHeader
      const tmpTheme: S2Theme = {
        cornerCell: {
          cell: {
            backgroundColor: tableHeaderBgColor
          },
          bolderText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign
          },
          text: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign
          },
          measureText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign
          }
        },
        colCell: {
          cell: {
            backgroundColor: tableHeaderBgColor
          },
          bolderText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign
          },
          text: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign
          },
          measureText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign
          }
        }
      }
      merge(theme, tmpTheme)
    }
    // cell
    if (tableCell) {
      const tableFontColor = hexColorToRGBA(tableCell.tableFontColor, basicStyle.alpha)
      const tableItemBgColor = hexColorToRGBA(tableCell.tableItemBgColor, basicStyle.alpha)
      const tableItemSubBgColor = hexColorToRGBA(tableCell.tableItemSubBgColor, basicStyle.alpha)
      const { tableItemAlign, tableItemFontSize, enableTableCrossBG } = tableCell
      const tmpTheme: S2Theme = {
        rowCell: {
          cell: {
            backgroundColor: tableItemBgColor,
            horizontalBorderColor: tableItemBgColor,
            verticalBorderColor: tableItemBgColor
          },
          bolderText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          },
          text: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          },
          measureText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          },
          seriesText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          }
        },
        dataCell: {
          cell: {
            crossBackgroundColor: enableTableCrossBG ? tableItemSubBgColor : tableItemBgColor,
            backgroundColor: tableItemBgColor
          },
          bolderText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          },
          text: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          },
          measureText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          },
          seriesText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize
          }
        }
      }
      merge(theme, tmpTheme)
    }
  }

  return theme
}

export function getStyle(chart: Chart): Style {
  const style: Style = {}
  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    const { basicStyle, tableHeader, tableCell } = customAttr
    style.colCfg = {
      height: tableHeader.tableTitleHeight
    }
    style.cellCfg = {
      height: tableCell.tableItemHeight
    }
    switch (basicStyle.tableColumnMode) {
      case 'adapt': {
        delete style.cellCfg.width
        style.layoutWidthType = 'compact'
        break
      }
      case 'field': {
        delete style.layoutWidthType
        const fieldMap =
          basicStyle.tableFieldWidth?.reduce((p, n) => {
            p[n.fieldId] = n
            return p
          }, {}) || {}
        // 下钻字段使用入口字段的宽度
        if (chart.drill) {
          const { xAxis } = parseJson(chart)
          const curDrillField = chart.drillFields[chart.drillFilters.length]
          const drillEnterFieldIndex = xAxis.findIndex(
            item => item.id === chart.drillFilters[0].fieldId
          )
          const drillEnterField = xAxis[drillEnterFieldIndex]
          fieldMap[curDrillField.dataeaseName] = {
            width: fieldMap[drillEnterField.dataeaseName]?.width
          }
        }
        style.colCfg.width = node => {
          const width = node.spreadsheet.container.cfg.el.offsetWidth
          if (!basicStyle.tableFieldWidth?.length) {
            const fieldsSize = chart.data.fields.length
            const columnCount = tableHeader.showIndex ? fieldsSize + 1 : fieldsSize
            return width / columnCount
          }
          const baseWidth = width / 100
          return fieldMap[node.field] ? fieldMap[node.field].width * baseWidth : baseWidth * 10
        }
        break
      }
      case 'custom': {
        style.colCfg.width = basicStyle.tableColumnWidth
        break
      }
      // 查看详情用，均分铺满
      default: {
        delete style.layoutWidthType
        style.colCfg.width = node => {
          const width = node.spreadsheet.container.cfg.el.offsetWidth
          const fieldsSize = chart.data?.fields?.length
          if (!fieldsSize) {
            return 0
          }
          const columnCount = tableHeader.showIndex ? fieldsSize + 1 : fieldsSize
          const minWidth = width / columnCount
          return Math.max(minWidth, basicStyle.tableColumnWidth)
        }
      }
    }
  }

  return style
}

export function getCurrentField(valueFieldList: Axis[], field: ChartViewField) {
  let list = []
  let res = null
  try {
    list = parseJson(valueFieldList)
  } catch (err) {
    list = JSON.parse(JSON.stringify(valueFieldList))
  }
  if (list) {
    for (let i = 0; i < list.length; i++) {
      const f = list[i]
      if (field.dataeaseName === f.dataeaseName) {
        res = f
        break
      }
    }
  }

  return res
}

export function getConditions(chart: Chart) {
  const { threshold } = parseJson(chart.senior)
  if (!threshold.enable) {
    return
  }
  const res = {
    text: [],
    background: []
  }
  const conditions = threshold.tableThreshold ?? []

  const dimFields = [...chart.xAxis, ...chart.xAxisExt].map(i => i.dataeaseName)
  if (conditions?.length > 0) {
    const { tableCell, basicStyle, tableHeader } = parseJson(chart.customAttr)
    const enableTableCrossBG = tableCell.enableTableCrossBG
    const valueColor = tableCell.tableFontColor
    const valueBgColor = enableTableCrossBG
      ? null
      : hexColorToRGBA(tableCell.tableItemBgColor, basicStyle.alpha)
    const headerValueColor = tableHeader.tableHeaderFontColor
    const headerValueBgColor = hexColorToRGBA(tableHeader.tableHeaderBgColor, basicStyle.alpha)
    for (let i = 0; i < conditions.length; i++) {
      const field = conditions[i]
      let defaultValueColor = valueColor
      let defaultBgColor = valueBgColor
      // 透视表表头颜色配置
      if (chart.type === 'table-pivot' && dimFields.includes(field.field.dataeaseName)) {
        defaultValueColor = headerValueColor
        defaultBgColor = headerValueBgColor
      }
      res.text.push({
        field: field.field.dataeaseName,
        mapping(value, rowData) {
          // 总计小计
          if (rowData?.isTotals) {
            return null
          }
          // 表头
          if (rowData?.id && rowData?.field === rowData.id) {
            return null
          }
          return {
            fill: mappingColor(value, defaultValueColor, field, 'color')
          }
        }
      })
      res.background.push({
        field: field.field.dataeaseName,
        mapping(value, rowData) {
          if (rowData?.isTotals) {
            return null
          }
          if (rowData?.id && rowData?.field === rowData.id) {
            return null
          }
          const fill = mappingColor(value, defaultBgColor, field, 'backgroundColor')
          return fill ? { fill } : null
        }
      })
    }
  }
  return res
}

function mappingColor(value, defaultColor, field, type) {
  let color
  for (let i = 0; i < field.conditions.length; i++) {
    let flag = false
    const t = field.conditions[i]
    if (field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) {
      const tv = parseFloat(t.value)
      if (t.term === 'eq') {
        if (value === tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (value !== tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'lt') {
        if (value < tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'gt') {
        if (value > tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'le') {
        if (value <= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'ge') {
        if (value >= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'between') {
        const min = parseFloat(t.min)
        const max = parseFloat(t.max)
        if (min <= value && value <= max) {
          color = t[type]
          flag = true
        }
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else if (field.field.deType === 0 || field.field.deType === 5) {
      const tv = t.value
      if (t.term === 'eq') {
        if (value === tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (value !== tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'like') {
        if (value.includes(tv)) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not like') {
        if (!value.includes(tv)) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'null') {
        if (value === null || value === undefined || value === '') {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_null') {
        if (value !== null && value !== undefined && value !== '') {
          color = t[type]
          flag = true
        }
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else {
      // time
      const tv = new Date(t.value.replace(/-/g, '/') + ' GMT+8').getTime()
      const v = new Date(value.replace(/-/g, '/') + ' GMT+8').getTime()
      if (t.term === 'eq') {
        if (v === tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (v !== tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'lt') {
        if (v < tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'gt') {
        if (v > tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'le') {
        if (v <= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'ge') {
        if (v >= tv) {
          color = t[type]
          flag = true
        }
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    }
  }
  return color
}

export function handleTableEmptyStrategy(chart: Chart) {
  let newData = chart.data?.tableRow as Record<string, any>[]
  let intersectionArr = []
  const senior = parseJson(chart.senior)
  let emptyDataStrategy = senior?.functionCfg?.emptyDataStrategy
  if (!emptyDataStrategy) {
    emptyDataStrategy = 'breakLine'
  }
  const emptyDataFieldCtrl = senior?.functionCfg?.emptyDataFieldCtrl
  if (emptyDataStrategy !== 'breakLine' && emptyDataFieldCtrl?.length && newData?.length) {
    const deNames = keys(newData[0])
    intersectionArr = intersection(deNames, emptyDataFieldCtrl)
  }
  if (intersectionArr.length) {
    newData = cloneDeep(newData)
    for (let i = newData.length - 1; i >= 0; i--) {
      for (let j = 0, tmp = intersectionArr.length; j < tmp; j++) {
        const deName = intersectionArr[j]
        if (newData[i][deName] === null) {
          if (emptyDataStrategy === 'setZero') {
            newData[i][deName] = 0
          }
          if (emptyDataStrategy === 'ignoreData') {
            newData = filter(newData, (_, index) => index !== i)
            break
          }
        }
      }
    }
  }
  return newData
}
export class SortTooltip extends BaseTooltip {
  show(showOptions) {
    const { iconName } = showOptions
    if (iconName) {
      this.showSortTooltip(showOptions)
      return
    }
    super.show(showOptions)
  }

  showSortTooltip(showOptions) {
    const { position, options, meta, event } = showOptions
    const { enterable } = getTooltipDefaultOptions(options)
    const { autoAdjustBoundary, adjustPosition } = this.spreadsheet.options.tooltip || {}
    this.visible = true
    this.options = showOptions
    const container = this['getContainer']()
    // 用 vue 手动 patch
    const vNode = createVNode(TableTooltip, {
      table: this.spreadsheet,
      meta
    })
    this.spreadsheet.tooltip.container.innerHTML = ''
    const childElement = document.createElement('div')
    this.spreadsheet.tooltip.container.appendChild(childElement)
    render(vNode, childElement)

    const { x, y } = getAutoAdjustPosition({
      spreadsheet: this.spreadsheet,
      position,
      tooltipContainer: container,
      autoAdjustBoundary
    })

    this.position = adjustPosition?.({ position: { x, y }, event }) ?? {
      x,
      y
    }

    setTooltipContainerStyle(container, {
      style: {
        left: `${this.position?.x}px`,
        top: `${this.position?.y}px`,
        pointerEvents: enterable ? 'all' : 'none',
        zIndex: 9999,
        position: 'absolute',
        color: 'black',
        background: 'white',
        fontSize: '16px'
      },
      visible: true
    })
  }
}
const SORT_DEFAULT =
  '<svg t="1711681787276" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4355" width="200" height="200"><path d="M922.345786 372.183628l-39.393195 38.687114L676.138314 211.079416l0 683.909301-54.713113 0L621.425202 129.010259l53.320393 0L922.345786 372.183628zM349.254406 894.989741 101.654214 651.815349l39.393195-38.687114 206.814276 199.792349L347.861686 129.010259l54.713113 0 0 765.978459L349.254406 894.988718z" fill="{fill}" p-id="4356"></path></svg>'
const SORT_UP =
  '<svg t="1711682928245" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11756" width="200" height="200"><path d="M960 704L512 256 64 704z" fill="{fill}" p-id="11757"></path></svg>'
const SORT_DOWN =
  '<svg t="1711681879346" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4655" width="200" height="200"><path d="M64 320l448 448 448-448z" fill="{fill}" p-id="4656"></path></svg>'

function svg2Base64(svg) {
  return `data:image/svg+xml;charset=utf-8;base64,${btoa(svg)}`
}

export function configHeaderInteraction(chart: Chart, option: S2Options) {
  const { tableHeaderFontColor, tableHeaderSort } = parseJson(chart.customAttr).tableHeader
  if (!tableHeaderSort) {
    return
  }
  const iconColor = tableHeaderFontColor ?? '#666'
  const sortDefault = svg2Base64(SORT_DEFAULT.replace('{fill}', iconColor))
  const sortUp = svg2Base64(SORT_UP.replace('{fill}', iconColor))
  const sortDown = svg2Base64(SORT_DOWN.replace('{fill}', iconColor))
  // 防止缓存
  const randomSuffix = Math.random()
  const sortIconMap = {
    asc: `customSortUp${randomSuffix}`,
    desc: `customSortDown${randomSuffix}`
  }
  option.customSVGIcons = [
    {
      name: `customSortDefault${randomSuffix}`,
      svg: sortDefault
    },
    {
      name: `customSortUp${randomSuffix}`,
      svg: sortUp
    },
    {
      name: `customSortDown${randomSuffix}`,
      svg: sortDown
    }
  ]
  option.headerActionIcons = [
    {
      iconNames: [
        `customSortDefault${randomSuffix}`,
        `customSortUp${randomSuffix}`,
        `customSortDown${randomSuffix}`
      ],
      belongsCell: 'colCell',
      displayCondition: (meta, iconName) => {
        if (meta.field === SERIES_NUMBER_FIELD) {
          return false
        }
        const sortMethodMap = meta.spreadsheet.store.get('sortMethodMap')
        const sortType = sortMethodMap?.[meta.field]
        if (sortType) {
          return iconName === sortIconMap[sortType]
        }
        return iconName === `customSortDefault${randomSuffix}`
      },
      onClick: props => {
        const { meta, event } = props
        meta.spreadsheet.showTooltip({
          position: {
            x: event.clientX,
            y: event.clientY
          },
          event,
          ...props
        })
      }
    }
  ]
}

export function configTooltip(chart: Chart, option: S2Options) {
  const { tooltip } = parseJson(chart.customAttr)
  option.tooltip = {
    ...option.tooltip,
    style: {
      background: tooltip.backgroundColor,
      fontSize: tooltip.fontSize + 'px',
      color: tooltip.color,
      boxShadow: 'rgba(0, 0, 0, 0.1) 0px 4px 8px 0px',
      borderRadius: '3px',
      padding: '4px 12px',
      opacity: 0.95,
      position: 'absolute'
    },
    adjustPosition: ({ event }) => {
      return getTooltipPosition(event)
    }
  }
}

export function copyContent(s2Instance, event, fieldMeta) {
  event.preventDefault()
  const cell = s2Instance.getCell(event.target)
  const valueField = cell.getMeta().valueField
  const cellMeta = cell.getMeta()
  let content
  // 单元格
  if (cellMeta?.data) {
    const value = cellMeta.data[valueField]
    const metaObj = find(fieldMeta, m => m.field === valueField)
    content = value?.toString()
    if (metaObj) {
      content = metaObj.formatter(value)
    }
  } else {
    // 列头&行头
    const fieldMap = fieldMeta?.reduce((p, n) => {
      p[n.field] = n.name
      return p
    }, {})
    content = cellMeta.value
    if (fieldMap?.[content]) {
      content = fieldMap[content]
    }
  }
  if (content) {
    copyString(content, true)
  }
}

function getTooltipPosition(event) {
  const s2Instance = event.s2Instance
  const { x, y } = event
  const result = { x: x + 15, y }
  if (!s2Instance) {
    return result
  }
  const { height, width } = s2Instance.getCanvasElement().getBoundingClientRect()
  const { offsetHeight, offsetWidth } = s2Instance.tooltip.getContainer()
  if (offsetWidth > width) {
    result.x = 0
  }
  if (offsetHeight > height) {
    result.y = 0
  }
  if (!(result.x || result.y)) {
    return result
  }
  if (result.x && result.x + offsetWidth > width) {
    result.x -= result.x + offsetWidth - width
  }
  if (result.y) {
    if (result.y > offsetHeight) {
      if (result.y - offsetHeight >= 15) {
        result.y -= offsetHeight + 15
      } else {
        result.y = 0
      }
    } else {
      result.y += 15
    }
  }
  return result
}
