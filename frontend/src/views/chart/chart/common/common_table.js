import { hexColorToRGBA } from '@/views/chart/chart/util'
import { DEFAULT_COLOR_CASE, DEFAULT_SIZE } from '@/views/chart/chart/chart'

export function getCustomTheme(chart) {
  const headerColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableHeaderBgColor, DEFAULT_COLOR_CASE.alpha)
  const itemColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableItemBgColor, DEFAULT_COLOR_CASE.alpha)
  const borderColor = hexColorToRGBA(DEFAULT_COLOR_CASE.tableBorderColor, DEFAULT_COLOR_CASE.alpha)
  const headerAlign = DEFAULT_SIZE.tableHeaderAlign
  const itemAlign = DEFAULT_SIZE.tableItemAlign

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
        verticalBorderColor: borderColor
      },
      text: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
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
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      }
    },
    colCell: {
      cell: {
        backgroundColor: headerColor,
        horizontalBorderColor: borderColor,
        verticalBorderColor: borderColor
      },
      text: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize,
        textAlign: headerAlign
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
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
      }
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
      theme.cornerCell.bolderText.fill = c.tableFontColor
      theme.cornerCell.text.fill = c.tableFontColor

      theme.rowCell.cell.backgroundColor = h_c
      theme.rowCell.cell.horizontalBorderColor = b_c
      theme.rowCell.cell.verticalBorderColor = b_c
      theme.rowCell.bolderText.fill = c.tableFontColor
      theme.rowCell.text.fill = c.tableFontColor

      theme.colCell.cell.backgroundColor = h_c
      theme.colCell.cell.horizontalBorderColor = b_c
      theme.colCell.cell.verticalBorderColor = b_c
      theme.colCell.bolderText.fill = c.tableFontColor
      theme.colCell.text.fill = c.tableFontColor

      theme.dataCell.cell.backgroundColor = i_c
      theme.dataCell.cell.horizontalBorderColor = b_c
      theme.dataCell.cell.verticalBorderColor = b_c
      theme.dataCell.bolderText.fill = c.tableFontColor
      theme.dataCell.text.fill = c.tableFontColor
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

      theme.rowCell.bolderText.fontSize = parseInt(s.tableTitleFontSize)
      theme.rowCell.bolderText.textAlign = h_a
      theme.rowCell.text.fontSize = parseInt(s.tableTitleFontSize)
      theme.rowCell.text.textAlign = h_a

      theme.colCell.bolderText.fontSize = parseInt(s.tableTitleFontSize)
      theme.colCell.bolderText.textAlign = h_a
      theme.colCell.text.fontSize = parseInt(s.tableTitleFontSize)
      theme.colCell.text.textAlign = h_a

      theme.dataCell.bolderText.fontSize = parseInt(s.tableItemFontSize)
      theme.dataCell.bolderText.textAlign = i_a
      theme.dataCell.text.fontSize = parseInt(s.tableItemFontSize)
      theme.dataCell.text.textAlign = i_a
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
