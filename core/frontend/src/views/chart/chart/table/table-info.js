import {
  TableSheet,
  S2Event,
  PivotSheet,
  DataCell,
  EXTRA_FIELD,
  TOTAL_VALUE,
  BaseTooltip,
  getAutoAdjustPosition,
  getTooltipDefaultOptions,
  setTooltipContainerStyle,
  SERIES_NUMBER_FIELD
} from '@antv/s2'
import { getCustomTheme, getSize } from '@/views/chart/chart/common/common_table'
import { DEFAULT_COLOR_CASE, DEFAULT_TOTAL } from '@/views/chart/chart/chart'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'
import { handleTableEmptyStrategy, hexColorToRGBA } from '@/views/chart/chart/util'
import { maxBy, minBy, find } from 'lodash-es'
import TableTooltip from '@/views/chart/components/table/TableTooltip.vue'
class SortTooltip extends BaseTooltip {
  vueCom
  constructor(spreadsheet, vueCom) {
    super(spreadsheet)
    this.vueCom = vueCom
  }

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
    const { autoAdjustBoundary, adjustPosition } =
    this.spreadsheet.options.tooltip || {}
    this.visible = true
    this.options = showOptions
    const container = this.getContainer()
    // 用 vue 手动 patch
    const vNode = this.vueCom.$createElement(TableTooltip, {
      props: {
        table: this.spreadsheet,
        meta
      }
    })
    this.spreadsheet.tooltip.container.innerHTML = ''
    const childElement = document.createElement('div')
    this.spreadsheet.tooltip.container.appendChild(childElement)
    this.vueCom.__patch__(childElement, vNode, false, true)

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
        pointerEvents: enterable ? 'all' : 'none'
      },
      visible: true
    })
  }
}
export function baseTableInfo(s2, container, chart, action, tableData, pageInfo, vueCom) {
  const containerDom = document.getElementById(container)

  // fields
  let fields = chart.data.fields
  if (!fields || fields.length === 0) {
    if (s2) {
      s2.destroy()
    }
    return
  }

  const columns = []
  const meta = []
  // 记录下钻起始字段的index
  let xAxis = []
  try {
    xAxis = JSON.parse(chart.xaxis)
  } catch (err) {
    xAxis = JSON.parse(JSON.stringify(chart.xaxis))
  }
  const nameMap = xAxis.reduce((pre, next) => {
    pre[next.dataeaseName] = next
    return pre
  }, {})
  if (chart.drill) {
    let drillFields = []
    try {
      drillFields = JSON.parse(chart.drillFields)
    } catch (err) {
      drillFields = JSON.parse(JSON.stringify(chart.drillFields))
    }
    // 总下钻过滤字段
    const drillFilters = JSON.parse(JSON.stringify(chart.drillFilters)).map(i => i.fieldId)
    // 当前下钻字段
    const curDrillField = drillFields[chart.drillFilters.length]
    drillFilters.push(curDrillField.id)
    // 下钻入口字段的下标
    const drillEnterFieldIndex = xAxis.findIndex(item => item.id === drillFilters[0])
    // 移除所有下钻字段，调整当前下钻字段到下钻入口位置
    fields = fields.filter(item => !drillFilters.includes(item.id))
    fields.splice(drillEnterFieldIndex, 0, curDrillField)
  }
  fields.forEach(ele => {
    const f = nameMap[ele.dataeaseName]
    columns.push(ele.dataeaseName)
    meta.push({
      field: ele.dataeaseName,
      name: ele.name,
      formatter: function(value) {
        if (!f) {
          return value
        }
        if (value === null || value === undefined) {
          return value
        }
        if (f.groupType === 'd') {
          return value
        } else {
          if (f.formatterCfg) {
            const v = valueFormatter(value, f.formatterCfg)
            return v.includes('NaN') ? value : v
          } else {
            const v = valueFormatter(value, formatterItem)
            return v.includes('NaN') ? value : v
          }
        }
      }
    })
  })
  // 空值处理
  const newData = handleTableEmptyStrategy(tableData, chart)
  // data config
  const s2DataConfig = {
    fields: {
      columns: columns
    },
    meta: meta,
    data: newData
  }

  const customAttr = JSON.parse(chart.customAttr)
  const sortIconMap = {
    'asc': 'SortUp',
    'desc': 'SortDown'
  }
  // options
  const s2Options = {
    width: containerDom.offsetWidth,
    height: containerDom.offsetHeight,
    showSeriesNumber: customAttr.size.showIndex,
    style: getSize(chart),
    tooltip: {
      renderTooltip: sheet => new SortTooltip(sheet, vueCom),
      getContainer: () => containerDom,
      adjustPosition: ({ event }) => {
        return getTooltipPosition(event)
      },
      style: {
        position: 'absolute',
        padding: '4px 2px'
      }
    },
    headerActionIcons: [
      {
        iconNames: ['GroupAsc', 'SortUp', 'SortDown'],
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
          return iconName === 'GroupAsc'
        },
        onClick: (props) => {
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
    ],
    conditions: getConditions(chart),
    frozenColCount: customAttr.size.tableColumnFreezeHead ?? 0,
    frozenRowCount: customAttr.size.tableRowFreezeHead ?? 0
  }
  // 开启序号之后，第一列就是序号列，修改 label 即可
  if (s2Options.showSeriesNumber) {
    s2Options.colCell = (node) => {
      if (node.colIndex === 0) {
        node.label = customAttr.size.indexLabel
        if (!customAttr.size.indexLabel || customAttr.size.showTableHeader === false) {
          node.label = ' '
        }
      }
    }
    s2Options.dataCell = (viewMeta) => {
      if (viewMeta.colIndex === 0) {
        viewMeta.fieldValue = (pageInfo.pageSize * (pageInfo.page - 1)) + viewMeta.rowIndex + 1
      }
      return new DataCell(viewMeta, viewMeta?.spreadsheet)
    }
  }
  // 隐藏表头，保留顶部的分割线, 禁用表头横向 resize
  if (customAttr.size.showTableHeader === false) {
    s2Options.style.colCfg.height = 1
    s2Options.interaction = {
      resize: {
        colCellVertical: false
      }
    }
    s2Options.colCell = (node) => {
      node.label = ' '
    }
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)

  // click
  s2.on(S2Event.DATA_CELL_CLICK, action)
  // hover
  const size = customAttr.size
  if (size.tableColTooltip?.show) {
    s2.on(S2Event.COL_CELL_HOVER, event => showTooltip(s2, event))
  }
  if (size.tableCellTooltip?.show) {
    s2.on(S2Event.DATA_CELL_HOVER, event => showTooltipValue(s2, event, meta))
  }
  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}

export function baseTableNormal(s2, container, chart, action, tableData, vueCom) {
  const containerDom = document.getElementById(container)
  if (!containerDom) return

  // fields
  const fields = chart.data.fields
  if (!fields || fields.length === 0) {
    if (s2) {
      s2.destroy()
    }
    return
  }

  const columns = []
  const meta = []

  // add drill list
  if (chart.drill) {
    let drillFields = []
    try {
      drillFields = JSON.parse(chart.drillFields)
    } catch (err) {
      drillFields = JSON.parse(JSON.stringify(chart.drillFields))
    }
    const drillField = drillFields[chart.drillFilters.length]

    const drillFilters = JSON.parse(JSON.stringify(chart.drillFilters))
    const drillExp = drillFilters[drillFilters.length - 1].datasetTableField

    // 记录下钻起始字段的index
    let xAxis
    try {
      xAxis = JSON.parse(chart.xaxis)
    } catch (err) {
      xAxis = JSON.parse(JSON.stringify(chart.xaxis))
    }
    let index = 0
    for (let i = 0; i < xAxis.length; i++) {
      if (xAxis[i].id === drillFilters[0].fieldId) {
        index = i
        break
      }
    }

    // 移除所有下钻字段
    const removeField = []
    for (let i = 0; i < chart.drillFilters.length; i++) {
      const ele = chart.drillFilters[i].datasetTableField
      removeField.push(ele.dataeaseName)
    }

    // build field
    fields.forEach(ele => {
      if (removeField.indexOf(ele.dataeaseName) < 0) {
        // 用下钻字段替换当前字段
        if (drillExp.dataeaseName === ele.dataeaseName) {
          columns.push(drillField.dataeaseName)
          meta.push({
            field: drillField.dataeaseName,
            name: drillField.name
          })
        } else {
          const f = getCurrentField(chart.yaxis, ele)
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name,
            formatter: function(value) {
              if (!f) {
                return value
              }
              if (value === null || value === undefined) {
                return value
              }
              if (f.formatterCfg) {
                return valueFormatter(value, f.formatterCfg)
              } else {
                return valueFormatter(value, formatterItem)
              }
            }
          })
        }
      }
    })

    // 修正下钻字段的index，获取下钻位置元素添加到index位置，并删除
    let yAxis
    try {
      yAxis = JSON.parse(chart.yaxis)
    } catch (err) {
      yAxis = JSON.parse(JSON.stringify(chart.yaxis))
    }
    const ele = columns[columns.length - 1 - yAxis.length]
    columns.splice(index, 0, ele)
    columns.splice(columns.length - 1 - yAxis.length, 1)
  } else {
    fields.forEach(ele => {
      const f = getCurrentField(chart.yaxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name,
        formatter: function(value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.formatterCfg) {
            return valueFormatter(value, f.formatterCfg)
          } else {
            return valueFormatter(value, formatterItem)
          }
        }
      })
    })
  }

  // 空值处理
  const newData = handleTableEmptyStrategy(tableData, chart)
  // data config
  const s2DataConfig = {
    fields: {
      columns: columns
    },
    meta: meta,
    data: newData
  }

  const customAttr = JSON.parse(chart.customAttr)
  const sortIconMap = {
    'asc': 'SortUp',
    'desc': 'SortDown'
  }
  // options
  const s2Options = {
    width: containerDom.offsetWidth,
    height: containerDom.offsetHeight,
    showSeriesNumber: customAttr.size.showIndex,
    style: getSize(chart),
    tooltip: {
      renderTooltip: sheet => new SortTooltip(sheet, vueCom),
      getContainer: () => containerDom,
      adjustPosition: ({ event }) => {
        return getTooltipPosition(event)
      },
      style: {
        position: 'absolute',
        padding: '4px 2px'
      }
    },
    headerActionIcons: [
      {
        iconNames: ['GroupAsc', 'SortUp', 'SortDown'],
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
          return iconName === 'GroupAsc'
        },
        onClick: (props) => {
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
    ],
    conditions: getConditions(chart),
    frozenColCount: customAttr.size.tableColumnFreezeHead ?? 0,
    frozenRowCount: customAttr.size.tableRowFreezeHead ?? 0
  }
  // 开启序号之后，第一列就是序号列，修改 label 即可
  if (s2Options.showSeriesNumber) {
    s2Options.colCell = (node) => {
      if (node.colIndex === 0) {
        node.label = customAttr.size.indexLabel
        if (!customAttr.size.indexLabel || customAttr.size.showTableHeader === false) {
          node.label = ' '
        }
      }
    }
    s2Options.dataCell = (viewMeta) => {
      return new DataCell(viewMeta, viewMeta?.spreadsheet)
    }
  }
  // 隐藏表头，保留顶部的分割线, 禁用表头横向 resize
  if (customAttr.size.showTableHeader === false) {
    s2Options.style.colCfg.height = 1
    s2Options.interaction = {
      resize: {
        colCellVertical: false
      }
    }
    s2Options.colCell = (node) => {
      node.label = ' '
    }
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new TableSheet(containerDom, s2DataConfig, s2Options)

  // click
  s2.on(S2Event.DATA_CELL_CLICK, action)
  // hover
  const size = customAttr.size
  if (size.tableColTooltip?.show) {
    s2.on(S2Event.COL_CELL_HOVER, event => showTooltip(s2, event))
  }
  if (size.tableCellTooltip?.show) {
    s2.on(S2Event.DATA_CELL_HOVER, event => showTooltipValue(s2, event, meta))
  }
  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}

export function baseTablePivot(s2, container, chart, action, headerAction, tableData) {
  const containerDom = document.getElementById(container)

  // row and column
  const columnFields = JSON.parse(chart.xaxis)
  const rowFields = JSON.parse(chart.xaxisExt)
  const valueFields = JSON.parse(chart.yaxis)
  const c = []; const r = []; const v = []
  columnFields.forEach(ele => {
    c.push(ele.dataeaseName)
  })
  rowFields.forEach(ele => {
    r.push(ele.dataeaseName)
  })
  valueFields.forEach(ele => {
    v.push(ele.dataeaseName)
  })

  // fields
  const fields = chart.data.fields
  if (!fields || fields.length === 0) {
    if (s2) {
      s2.destroy()
    }
    return
  }

  const columns = []
  const meta = []
  const fieldMap = fields.reduce((pre, next) => {
    pre[next['dataeaseName']] = next['name']
    return pre
  }, {})

  // add drill list
  if (chart.drill) {
    const drillFields = JSON.parse(chart.drillFields)
    const drillField = drillFields[chart.drillFilters.length]

    const drillFilters = JSON.parse(JSON.stringify(chart.drillFilters))
    const drillExp = drillFilters[drillFilters.length - 1].datasetTableField

    // 移除所有下钻字段
    const removeField = []
    for (let i = 0; i < chart.drillFilters.length; i++) {
      const ele = chart.drillFilters[i].datasetTableField
      removeField.push(ele.dataeaseName)
    }

    // build field
    fields.forEach(ele => {
      if (removeField.indexOf(ele.dataeaseName) < 0) {
        // 用下钻字段替换当前字段
        if (drillExp.dataeaseName === ele.dataeaseName) {
          columns.push(drillField.dataeaseName)
          meta.push({
            field: drillField.dataeaseName,
            name: drillField.name
          })
        } else {
          const f = getCurrentField(chart.yaxis, ele)
          columns.push(ele.dataeaseName)
          meta.push({
            field: ele.dataeaseName,
            name: ele.name,
            formatter: function(value) {
              if (!f) {
                return value
              }
              if (value === null || value === undefined) {
                return value
              }
              if (f.formatterCfg) {
                return valueFormatter(value, f.formatterCfg)
              } else {
                return valueFormatter(value, formatterItem)
              }
            }
          })
        }
      }
    })
  } else {
    fields.forEach(ele => {
      const f = getCurrentField(chart.yaxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name,
        formatter: function(value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.formatterCfg) {
            return valueFormatter(value, f.formatterCfg)
          } else {
            return valueFormatter(value, formatterItem)
          }
        }
      })
    })
  }

  // total config
  let totalCfg = {}
  const chartObj = JSON.parse(JSON.stringify(chart))
  let customAttr
  if (chartObj.customAttr) {
    if (Object.prototype.toString.call(chartObj.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chartObj.customAttr))
    } else {
      customAttr = JSON.parse(chartObj.customAttr)
    }
    if (customAttr.totalCfg) {
      totalCfg = customAttr.totalCfg
    } else {
      totalCfg = JSON.parse(JSON.stringify(DEFAULT_TOTAL))
    }
  }
  totalCfg.row.subTotalsDimensions = r
  totalCfg.col.subTotalsDimensions = c

  // 解析合计、小计排序
  const sortParams = []
  if (totalCfg.row.totalSort && totalCfg.row.totalSort !== 'none' && c.length > 0 && totalCfg.row.showGrandTotals && v.indexOf(totalCfg.row.totalSortField) > -1) {
    const sort = {
      sortFieldId: c[0],
      sortMethod: totalCfg.row.totalSort.toUpperCase(),
      sortByMeasure: TOTAL_VALUE,
      query: {
        [EXTRA_FIELD]: totalCfg.row.totalSortField
      }
    }
    sortParams.push(sort)
  }
  if (totalCfg.col.totalSort && totalCfg.col.totalSort !== 'none' && r.length > 0 && totalCfg.col.showGrandTotals && v.indexOf(totalCfg.col.totalSortField) > -1) {
    const sort = {
      sortFieldId: r[0],
      sortMethod: totalCfg.col.totalSort.toUpperCase(),
      sortByMeasure: TOTAL_VALUE,
      query: {
        [EXTRA_FIELD]: totalCfg.col.totalSortField
      }
    }
    sortParams.push(sort)
  }
  // 自定义总计小计
  const totals = [
    totalCfg.row.calcTotals,
    totalCfg.row.calcSubTotals,
    totalCfg.col.calcTotals,
    totalCfg.col.calcSubTotals
  ]
  totals.forEach(total => {
    if (total.cfg?.length) {
      delete total.aggregation
      const totalCfgMap = total.cfg.reduce((p, n) => {
        p[n.dataeaseName] = n
        return p
      }, {})
      total.calcFunc = (query, data) => {
        return customCalcFunc(query, data, totalCfgMap)
      }
    }
  })
  // 空值处理
  const newData = handleTableEmptyStrategy(tableData, chart)
  // data config
  const s2DataConfig = {
    fields: {
      rows: r,
      columns: c,
      values: v
    },
    meta: meta,
    data: newData,
    sortParams: sortParams
  }

  // options
  const s2Options = {
    width: containerDom.offsetWidth,
    height: containerDom.offsetHeight,
    style: getSize(chart),
    totals: totalCfg,
    conditions: getConditions(chart),
    tooltip: {
      getContainer: () => containerDom,
      adjustPosition: ({ event }) => {
        return getTooltipPosition(event)
      },
      style: {
        position: 'absolute',
        padding: '4px 2px'
      }
    },
  }

  // 开始渲染
  if (s2) {
    s2.destroy()
  }
  s2 = new PivotSheet(containerDom, s2DataConfig, s2Options)

  // click
  s2.on(S2Event.DATA_CELL_CLICK, action)
  s2.on(S2Event.ROW_CELL_CLICK, headerAction)
  s2.on(S2Event.COL_CELL_CLICK, headerAction)
  // hover
  const size = customAttr?.size
  if (size?.tableRowTooltip?.show) {
    s2.on(S2Event.ROW_CELL_HOVER, event => showTooltip(s2, event, fieldMap))
  }
  if (size?.tableColTooltip?.show) {
    s2.on(S2Event.COL_CELL_HOVER, event => showTooltip(s2, event, fieldMap))
  }
  if (size.tableCellTooltip?.show) {
    s2.on(S2Event.DATA_CELL_HOVER, event => showTooltipValue(s2, event, meta))
  }
  // theme
  const customTheme = getCustomTheme(chart)
  s2.setThemeCfg({ theme: customTheme })

  return s2
}

function getCurrentField(valueFieldList, field) {
  let list = []
  let res = null
  try {
    list = JSON.parse(valueFieldList)
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

function getConditions(chart) {
  const res = {
    text: [],
    background: []
  }
  let conditions
  try {
    const senior = JSON.parse(chart.senior)
    conditions = senior.threshold ? senior.threshold.tableThreshold : null
  } catch (err) {
    const senior = JSON.parse(JSON.stringify(chart.senior))
    conditions = senior.threshold ? senior.threshold.tableThreshold : null
  }

  if (conditions && conditions.length > 0) {
    // table item color
    let valueColor = DEFAULT_COLOR_CASE.tableFontColor
    let valueBgColor = DEFAULT_COLOR_CASE.tableItemBgColor
    if (chart.customAttr) {
      const customAttr = JSON.parse(chart.customAttr)
      // color
      if (customAttr.color) {
        const c = JSON.parse(JSON.stringify(customAttr.color))
        valueColor = c.tableFontColor
        const enableTableCrossBG = c.enableTableCrossBG
        if (!enableTableCrossBG) {
          valueBgColor = hexColorToRGBA(c.tableItemBgColor, c.alpha)
        } else {
          valueBgColor = null
        }
      }
    }

    const filedValueMap = getFieldValueMap(chart)
    for (let i = 0; i < conditions.length; i++) {
      const field = conditions[i]
      res.text.push({
        field: field.field.dataeaseName,
        mapping(value, rowData) {
          return {
            fill: mappingColor(value, valueColor, field, 'color', filedValueMap, rowData)
          }
        }
      })
      res.background.push({
        field: field.field.dataeaseName,
        mapping(value, rowData) {
          const fill = mappingColor(value, valueBgColor, field, 'backgroundColor', filedValueMap, rowData)
          if (fill) {
            return { fill }
          }
          // 返回 null 会使用主题中的背景色
          return null
        }
      })
    }
  }
  return res
}

function getValue(field, filedValueMap, rowData) {
  if (field.summary === 'value') {
    return rowData[field.curField.dataeaseName]
  } else {
    return filedValueMap[field.summary + '-' + field.fieldId]
  }
}

function mappingColor(value, defaultColor, field, type, filedValueMap, rowData) {
  let color
  for (let i = 0; i < field.conditions.length; i++) {
    let flag = false
    const t = field.conditions[i]
    if (field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) {
      let tv, max, min
      if (t.field === '1') {
        if (t.term === 'between') {
          max = parseFloat(getValue(t.maxField, filedValueMap, rowData))
          min = parseFloat(getValue(t.minField, filedValueMap, rowData))
        } else {
          tv = parseFloat(getValue(t.targetField, filedValueMap, rowData))
        }
      } else {
        if (t.term === 'between') {
          min = parseFloat(t.min)
          max = parseFloat(t.max)
        } else {
          tv = parseFloat(t.value)
        }
      }

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
      let tv
      if (t.field === '1') {
        tv = getValue(t.targetField, filedValueMap, rowData)
      } else {
        tv = t.value
      }
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
      let tv
      if (t.field === '1') {
        const fieldValue = getValue(t.targetField, filedValueMap, rowData)
        if (fieldValue) {
          tv = new Date(fieldValue.replace(/-/g, '/') + ' GMT+8').getTime()
        }
      } else {
        tv = new Date(t.value.replace(/-/g, '/') + ' GMT+8').getTime()
      }

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

function showTooltipValue(s2Instance, event, meta) {
  const cell = s2Instance.getCell(event.target)
  const valueField = cell.getMeta().valueField
  const cellMeta = cell.getMeta()
  if (!cellMeta.data) {
    return
  }
  const value = cellMeta.data[valueField]
  const metaObj = find(meta, m =>
    m.field === valueField
  )
  event.s2Instance = s2Instance
  let content = value?.toString()
  if (metaObj) {
    content = metaObj.formatter(value)
  }
  s2Instance.showTooltip({
    position: {
      x: event.clientX,
      y: event.clientY
    },
    content,
    meta: cellMeta,
    event
  })
}

function showTooltip(s2Instance, event, fieldMap) {
  const cell = s2Instance.getCell(event.target)
  const meta = cell.getMeta()
  let content = meta.value
  if (fieldMap?.[content]) {
    content = fieldMap?.[content]
  }
  event.s2Instance = s2Instance
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

function getFieldValueMap(view) {
  const fieldValueMap = {}
  if (view.data && view.data.dynamicAssistData && view.data.dynamicAssistData.length > 0) {
    view.data.dynamicAssistData.forEach(ele => {
      fieldValueMap[ele.summary + '-' + ele.fieldId] = ele.value
    })
  }
  return fieldValueMap
}

function customCalcFunc(query, data, totalCfgMap) {
  if (!data?.length || !query[EXTRA_FIELD]) {
    return 0
  }
  const aggregation = totalCfgMap[query[EXTRA_FIELD]].aggregation
  switch (aggregation) {
    case 'SUM': {
      return data.reduce((p, n) => {
        return p + n[query[EXTRA_FIELD]]
      }, 0)
    }
    case 'AVG': {
      const sum = data.reduce((p, n) => {
        return p + n[query[EXTRA_FIELD]]
      }, 0)
      return sum / data.length
    }
    case 'MIN': {
      const result = minBy(data, n => {
        return n[query[EXTRA_FIELD]]
      })
      return result[query[EXTRA_FIELD]]
    }
    case 'MAX': {
      const result = maxBy(data, n => {
        return n[query[EXTRA_FIELD]]
      })
      return result[query[EXTRA_FIELD]]
    }
    default: {
      return data.reduce((p, n) => {
        return p + n[query[EXTRA_FIELD]]
      }, 0)
    }
  }
}

function getTooltipPosition(event) {
  const s2Instance = event.s2Instance
  const { x, y } = event
  const result = { x: x + 15, y: y + 10 }
  if (!s2Instance) {
    return result
  }
  const { height, width} = s2Instance.getCanvasElement().getBoundingClientRect()
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
    result.x -= (result.x + offsetWidth - width)
  }
  if (result.y && result.y + offsetHeight > height) {
    result.y -= (offsetHeight + 15)
  }
  return result
}
