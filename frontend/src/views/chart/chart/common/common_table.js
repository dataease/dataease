import { hexColorToRGBA } from '@/views/chart/chart/util'
import { DEFAULT_COLOR_CASE, DEFAULT_SIZE } from '@/views/chart/chart/chart'

export function getCustomTheme(chart) {
  const theme = {
    background: {
      color: '#00000000'
    },
    colCell: {
      cell: {
        backgroundColor: hexColorToRGBA(DEFAULT_COLOR_CASE.tableHeaderBgColor, DEFAULT_COLOR_CASE.alpha)
      },
      bolderText: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableTitleFontSize
      }
    },
    dataCell: {
      cell: {
        backgroundColor: hexColorToRGBA(DEFAULT_COLOR_CASE.tableItemBgColor, DEFAULT_COLOR_CASE.alpha)
      },
      text: {
        fill: DEFAULT_COLOR_CASE.tableFontColor,
        fontSize: DEFAULT_SIZE.tableItemFontSize
      }
    }
  }

  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))
      theme.colCell.cell.backgroundColor = hexColorToRGBA(c.tableHeaderBgColor, c.alpha)
      theme.colCell.bolderText.fill = c.tableFontColor
      theme.dataCell.cell.backgroundColor = hexColorToRGBA(c.tableItemBgColor, c.alpha)
      theme.dataCell.text.fill = c.tableFontColor
    }
    // size
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      theme.colCell.bolderText.fontSize = parseInt(s.tableTitleFontSize)
      theme.dataCell.text.fontSize = parseInt(s.tableItemFontSize)
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
      if (!s.tableColumnMode || s.tableColumnMode === 'adapt') {
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
