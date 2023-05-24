import { hexColorToRGBA, resetRgbOpacity } from '@/views/chart/chart/util'
import { DEFAULT_COLOR_CASE, DEFAULT_SIZE } from '@/views/chart/chart/chart'

export function getCustomTheme(chart) {
  const headerColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableHeaderBgColor, DEFAULT_COLOR_CASE.alpha)
  const itemColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableItemBgColor, DEFAULT_COLOR_CASE.alpha)
  const borderColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableBorderColor, DEFAULT_COLOR_CASE.alpha)
  const headerAlign = DEFAULT_SIZE.tableHeaderAlign
  const itemAlign = DEFAULT_SIZE.tableItemAlign
  const scrollBarColor = DEFAULT_COLOR_CASE.tableScrollBarColor
  const scrollBarHoverColor = DEFAULT_COLOR_CASE.tableScrollBarHoverColor

  const theme = {
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

  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))
      const h_c = hexColorToRGBA(c.tableHeaderBgColor, c.alpha)
      const i_c = hexColorToRGBA(c.tableItemBgColor, c.alpha)
      const b_c = c.tableBorderColor ? hexColorToRGBA(c.tableBorderColor, c.alpha) : hexColorToRGBA(DEFAULT_COLOR_CASE.tableBorderColor, c.alpha)
      theme.splitLine.horizontalBorderColor = b_c
      theme.splitLine.verticalBorderColor = b_c

      theme.cornerCell.cell.backgroundColor = h_c
      theme.cornerCell.cell.horizontalBorderColor = b_c
      theme.cornerCell.cell.verticalBorderColor = b_c
      theme.cornerCell.bolderText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
      theme.cornerCell.text.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
      theme.cornerCell.measureText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor

      if (chart.type === 'table-pivot') {
        theme.rowCell.cell.backgroundColor = h_c
        theme.rowCell.cell.horizontalBorderColor = b_c
        theme.rowCell.cell.verticalBorderColor = b_c
        theme.rowCell.bolderText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
        theme.rowCell.text.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
        theme.rowCell.measureText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
        theme.rowCell.seriesText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
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
      theme.colCell.bolderText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
      theme.colCell.text.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor
      theme.colCell.measureText.fill = c.tableHeaderFontColor ? c.tableHeaderFontColor : c.tableFontColor

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
      const h_a = s.tableHeaderAlign ? s.tableHeaderAlign : DEFAULT_SIZE.tableHeaderAlign
      const i_a = s.tableItemAlign ? s.tableItemAlign : DEFAULT_SIZE.tableItemAlign

      theme.cornerCell.bolderText.fontSize = parseInt(s.tableTitleFontSize)
      theme.cornerCell.bolderText.textAlign = h_a
      theme.cornerCell.text.fontSize = parseInt(s.tableTitleFontSize)
      theme.cornerCell.text.textAlign = h_a
      theme.cornerCell.measureText.fontSize = parseInt(s.tableTitleFontSize)
      theme.cornerCell.measureText.textAlign = h_a

      if (chart.type === 'table-pivot') {
        theme.rowCell.bolderText.fontSize = parseInt(s.tableTitleFontSize)
        theme.rowCell.bolderText.textAlign = h_a
        theme.rowCell.text.fontSize = parseInt(s.tableTitleFontSize)
        theme.rowCell.text.textAlign = h_a
        theme.rowCell.measureText.fontSize = parseInt(s.tableTitleFontSize)
        theme.rowCell.measureText.textAlign = h_a
        theme.rowCell.seriesText.fontSize = parseInt(s.tableTitleFontSize)
        theme.rowCell.seriesText.textAlign = h_a
      } else {
        // 序号列的数字单元格内容样式使用指标的内容样式而不是表头的内容样式
        theme.rowCell.bolderText.fontSize = parseInt(s.tableItemFontSize)
        theme.rowCell.bolderText.textAlign = i_a
        theme.rowCell.text.fontSize = parseInt(s.tableItemFontSize)
        theme.rowCell.text.textAlign = i_a
        theme.rowCell.measureText.fontSize = parseInt(s.tableItemFontSize)
        theme.rowCell.measureText.textAlign = i_a
        theme.rowCell.seriesText.fontSize = parseInt(s.tableItemFontSize)
        theme.rowCell.seriesText.textAlign = i_a
      }
      theme.rowCell.seriesNumberWidth = parseInt(s.tableColumnWidth)

      theme.colCell.bolderText.fontSize = parseInt(s.tableTitleFontSize)
      theme.colCell.bolderText.textAlign = h_a
      theme.colCell.text.fontSize = parseInt(s.tableTitleFontSize)
      theme.colCell.text.textAlign = h_a
      theme.colCell.measureText.fontSize = parseInt(s.tableTitleFontSize)
      theme.colCell.measureText.textAlign = h_a

      theme.dataCell.bolderText.fontSize = parseInt(s.tableItemFontSize)
      theme.dataCell.bolderText.textAlign = i_a
      theme.dataCell.text.fontSize = parseInt(s.tableItemFontSize)
      theme.dataCell.text.textAlign = i_a
      theme.dataCell.measureText.fontSize = parseInt(s.tableItemFontSize)
      theme.dataCell.measureText.textAlign = i_a
    }
  }

  return theme
}

export function getSize(chart) {
  const size = {}
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
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
