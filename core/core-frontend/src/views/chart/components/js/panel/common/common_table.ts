import { hexColorToRGBA, parseJson, resetRgbOpacity } from '../..//util'
import { DEFAULT_COLOR_CASE, DEFAULT_SIZE } from '@/views/chart/components/editor/util/chart'
import { S2Theme, Style, TextAlign } from '@antv/s2'

export function getCustomTheme(chart: Chart) {
  const headerColor = hexColorToRGBA(
    DEFAULT_COLOR_CASE.tableHeaderBgColor,
    DEFAULT_COLOR_CASE.alpha
  )
  const itemColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableItemBgColor, DEFAULT_COLOR_CASE.alpha)
  const borderColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableBorderColor, DEFAULT_COLOR_CASE.alpha)
  const headerAlign = DEFAULT_SIZE.tableHeaderAlign as TextAlign
  const itemAlign = DEFAULT_SIZE.tableItemAlign as TextAlign
  const scrollBarColor = DEFAULT_COLOR_CASE.tableScrollBarColor
  const scrollBarHoverColor = DEFAULT_COLOR_CASE.tableScrollBarHoverColor

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
        verticalBorderWidth: chart.type === 'table-pivot' ? 1 : 0
      },
      text: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      measureText: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
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
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign,
        textBaseline: chart.type === 'table-pivot' ? 'top' : 'middle'
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      measureText: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      seriesText: {
        fill: DEFAULT_COLOR_CASE.tableItemBgColor,
        fontSize: DEFAULT_SIZE.tableItemFontSize,
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
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      measureText: {
        fill: DEFAULT_COLOR_CASE.tableHeaderFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
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
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableItemFontSize,
        textAlign: itemAlign
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableItemFontSize,
        textAlign: itemAlign
      },
      measureText: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableItemFontSize,
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
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))
      const h_c = hexColorToRGBA(c.tableHeaderBgColor, c.alpha)
      const i_c = hexColorToRGBA(c.tableItemBgColor, c.alpha)
      const b_c = c.tableBorderColor
        ? hexColorToRGBA(c.tableBorderColor, c.alpha)
        : hexColorToRGBA(DEFAULT_COLOR_CASE.tableBorderColor, c.alpha)
      theme.splitLine.horizontalBorderColor = b_c
      theme.splitLine.verticalBorderColor = b_c

      theme.cornerCell.cell.backgroundColor = h_c
      theme.cornerCell.cell.horizontalBorderColor = b_c
      theme.cornerCell.cell.verticalBorderColor = b_c
      theme.cornerCell.bolderText.fill = c.tableHeaderFontColor
        ? c.tableHeaderFontColor
        : c.tableFontColor
      theme.cornerCell.text.fill = c.tableHeaderFontColor
        ? c.tableHeaderFontColor
        : c.tableFontColor
      theme.cornerCell.measureText.fill = c.tableHeaderFontColor
        ? c.tableHeaderFontColor
        : c.tableFontColor

      if (chart.type === 'table-pivot') {
        theme.rowCell.cell.backgroundColor = h_c
        theme.rowCell.cell.horizontalBorderColor = b_c
        theme.rowCell.cell.verticalBorderColor = b_c
        theme.rowCell.bolderText.fill = c.tableHeaderFontColor
          ? c.tableHeaderFontColor
          : c.tableFontColor
        theme.rowCell.text.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
        theme.rowCell.measureText.fill = c.tableHeaderFontColor
          ? c.tableHeaderFontColor
          : c.tableFontColor
        theme.rowCell.seriesText.fill = c.tableHeaderFontColor
          ? c.tableHeaderFontColor
          : c.tableFontColor
      } else {
        theme.rowCell.cell.backgroundColor = i_c // 这个参数其实只对开启序号列的行头生效
        theme.rowCell.cell.horizontalBorderColor = i_c
        theme.rowCell.cell.verticalBorderColor = i_c
        theme.rowCell.bolderText.fill = c.tableFontColor
        theme.rowCell.text.fill = c.tableFontColor
        theme.rowCell.measureText.fill = c.tableFontColor
        theme.rowCell.seriesText.fill = c.tableFontColor
      }

      theme.colCell.cell.backgroundColor = h_c
      theme.colCell.cell.horizontalBorderColor = b_c
      theme.colCell.cell.verticalBorderColor = b_c
      theme.colCell.bolderText.fill = c.tableHeaderFontColor
        ? c.tableHeaderFontColor
        : c.tableFontColor
      theme.colCell.text.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
      theme.colCell.measureText.fill = c.tableHeaderFontColor
        ? c.tableHeaderFontColor
        : c.tableFontColor

      theme.dataCell.cell.crossBackgroundColor = i_c
      theme.dataCell.cell.backgroundColor = i_c
      theme.dataCell.cell.horizontalBorderColor = b_c
      theme.dataCell.cell.verticalBorderColor = b_c
      theme.dataCell.bolderText.fill = c.tableFontColor
      theme.dataCell.text.fill = c.tableFontColor
      theme.dataCell.measureText.fill = c.tableFontColor

      theme.scrollBar.thumbColor = c.tableScrollBarColor
      theme.scrollBar.thumbHoverColor = resetRgbOpacity(c.tableScrollBarColor, 1.5)
    }
    // size
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      const h_a = (
        s.tableHeaderAlign ? s.tableHeaderAlign : DEFAULT_SIZE.tableHeaderAlign
      ) as TextAlign
      const i_a = (s.tableItemAlign ? s.tableItemAlign : DEFAULT_SIZE.tableItemAlign) as TextAlign

      theme.cornerCell.bolderText.fontSize = s.tableTitleFontSize
      theme.cornerCell.bolderText.textAlign = h_a
      theme.cornerCell.text.fontSize = s.tableTitleFontSize
      theme.cornerCell.text.textAlign = h_a
      theme.cornerCell.measureText.fontSize = s.tableTitleFontSize
      theme.cornerCell.measureText.textAlign = h_a

      if (chart.type === 'table-pivot') {
        theme.rowCell.bolderText.fontSize = s.tableTitleFontSize
        theme.rowCell.bolderText.textAlign = h_a
        theme.rowCell.text.fontSize = s.tableTitleFontSize
        theme.rowCell.text.textAlign = h_a
        theme.rowCell.measureText.fontSize = s.tableTitleFontSize
        theme.rowCell.measureText.textAlign = h_a
        theme.rowCell.seriesText.fontSize = s.tableTitleFontSize
        theme.rowCell.seriesText.textAlign = h_a
      } else {
        // 序号列的数字单元格内容样式使用指标的内容样式而不是表头的内容样式
        theme.rowCell.bolderText.fontSize = s.tableItemFontSize
        theme.rowCell.bolderText.textAlign = i_a
        theme.rowCell.text.fontSize = s.tableItemFontSize
        theme.rowCell.text.textAlign = i_a
        theme.rowCell.measureText.fontSize = s.tableItemFontSize
        theme.rowCell.measureText.textAlign = i_a
        theme.rowCell.seriesText.fontSize = s.tableItemFontSize
        theme.rowCell.seriesText.textAlign = i_a
      }
      theme.rowCell.seriesNumberWidth = s.tableColumnWidth

      theme.colCell.bolderText.fontSize = s.tableTitleFontSize
      theme.colCell.bolderText.textAlign = h_a
      theme.colCell.text.fontSize = s.tableTitleFontSize
      theme.colCell.text.textAlign = h_a
      theme.colCell.measureText.fontSize = s.tableTitleFontSize
      theme.colCell.measureText.textAlign = h_a

      theme.dataCell.bolderText.fontSize = s.tableItemFontSize
      theme.dataCell.bolderText.textAlign = i_a
      theme.dataCell.text.fontSize = s.tableItemFontSize
      theme.dataCell.text.textAlign = i_a
      theme.dataCell.measureText.fontSize = s.tableItemFontSize
      theme.dataCell.measureText.textAlign = i_a
    }
  }

  return theme
}

export function getSize(chart: Chart): Style {
  const size: Style = {}
  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    // size
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      size.colCfg = {
        height: s.tableTitleHeight
      }
      size.cellCfg = {
        height: s.tableItemHeight
      }
      if (s.tableColumnMode && s.tableColumnMode === 'adapt') {
        delete size.cellCfg.width
        size.layoutWidthType = 'compact'
      } else {
        delete size.layoutWidthType
        size.cellCfg.width = s.tableColumnWidth
      }
    }
  }

  return size
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
        valueBgColor = hexColorToRGBA(c.tableItemBgColor, c.alpha)
      }
    }

    for (let i = 0; i < conditions.length; i++) {
      const field = conditions[i]
      res.text.push({
        field: field.field.dataeaseName,
        mapping(value) {
          return {
            fill: mappingColor(value, valueColor, field, 'color')
          }
        }
      })
      res.background.push({
        field: field.field.dataeaseName,
        mapping(value) {
          return {
            fill: mappingColor(value, valueBgColor, field, 'backgroundColor')
          }
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
