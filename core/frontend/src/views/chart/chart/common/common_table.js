import { hexColorToRGBA, resetRgbOpacity } from '@/views/chart/chart/util'
import { DEFAULT_COLOR_CASE, DEFAULT_SIZE } from '@/views/chart/chart/chart'
import Exceljs from 'exceljs'
import { saveAs } from 'file-saver'
import i18n from '@/lang'
import {Message} from "element-ui";

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
      const i_s_c = hexColorToRGBA(c.tableItemSubBgColor, c.alpha)
      const enableTableCrossBG = c.enableTableCrossBG
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

      // 为了与Echarts统一，奇数行是原来颜色
      if (enableTableCrossBG) {
        theme.dataCell.cell.crossBackgroundColor = i_c
        theme.dataCell.cell.backgroundColor = i_s_c
      } else {
        theme.dataCell.cell.crossBackgroundColor = i_c
        theme.dataCell.cell.backgroundColor = i_c
      }
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
      switch (s.tableColumnMode) {
        case 'adapt': {
          delete size.cellCfg.width
          size.layoutWidthType = 'compact'
          break
        }
        case 'field': {
          delete size.layoutWidthType
          const fieldMap = s.tableFieldWidth?.reduce((p, n) => {
            p[n.fieldId] = n
            return p
          }, {}) || {}
          // 下钻字段使用入口字段的宽度
          if (chart.drill) {
            const xAxis = JSON.parse(chart.xaxis)
            const curDrillField = chart.drillFields[chart.drillFilters.length]
            const drillEnterFieldIndex = xAxis.findIndex(item => item.id === chart.drillFilters[0].fieldId)
            const drillEnterField = xAxis[drillEnterFieldIndex]
            fieldMap[curDrillField.dataeaseName] = {
              width: fieldMap[drillEnterField.dataeaseName]?.width
            }
          }
          size.colCfg.width = node => {
            const width = node.spreadsheet.container.cfg.el.offsetWidth
            if (!s.tableFieldWidth?.length) {
              const columnCount = s.showIndex ? chart.data.fields.length + 1 : chart.data.fields.length
              return width / columnCount
            }
            const baseWidth = width / 100
            return fieldMap[node.field] ? fieldMap[node.field].width * baseWidth : baseWidth * 10
          }
          break
        }
        default: {
          delete size.layoutWidthType
          size.cellCfg.width = s.tableColumnWidth
        }
      }
    }
  }

  return size
}

export async function exportPivotExcel(instance, chart) {
  const { meta, fields } = instance.dataCfg
  const rowLength = fields?.rows?.length || 0
  const colLength = fields?.columns?.length || 0
  const valueLength = fields?.values?.length || 0
  if (!(rowLength && valueLength)) {
    Message.warning({
      message: i18n.t('chart.pivot_export_empty_fields'),
      type: 'warning',
      showClose: true,
      duration: 5000
    })
    return
  }
  const workbook = new Exceljs.Workbook()
  const worksheet = workbook.addWorksheet(chart.title)
  const metaMap = meta?.reduce((p, n) => {
    if (n.field) {
      p[n.field] = n
    }
    return p
  }, {})
  // 角头
  fields.columns?.forEach((column, index) => {
    const cell = worksheet.getCell(index + 1, 1)
    cell.value = metaMap[column]?.name ?? column
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (rowLength >= 2) {
      worksheet.mergeCells(index + 1, 1, index + 1, rowLength)
    }
  })
  fields?.rows?.forEach((row, index) => {
    const cell = worksheet.getCell(colLength + 1, index + 1)
    cell.value = metaMap[row]?.name ?? row
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
  })
  const { layoutResult } = instance.facet
  // 行头
  const { rowLeafNodes, rowsHierarchy, rowNodes } = layoutResult
  const maxColIndex = rowsHierarchy.maxLevel + 1
  const notLeafNodeHeightMap = {}
  rowLeafNodes.forEach(node => {
    // 行头的高度由子节点相加决定，也就是行头子节点中包含的叶子节点数量
    let curNode = node.parent
    while (curNode) {
      const height = notLeafNodeHeightMap[curNode.id] ?? 0
      notLeafNodeHeightMap[curNode.id] = height + 1
      curNode = curNode.parent
    }
    const { rowIndex } = node
    const writeRowIndex = rowIndex + 1 + colLength + 1
    const writeColIndex = node.level + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = node.label
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (writeColIndex < maxColIndex) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, writeRowIndex, maxColIndex)
    }
  })

  const getNodeStartRowIndex = (node) => {
    if (!node.children?.length) {
      return node.rowIndex + 1
    } else {
      return getNodeStartRowIndex(node.children[0])
    }
  }
  rowNodes?.forEach(node => {
    if (node.isLeaf) {
      return
    }
    const rowIndex = getNodeStartRowIndex(node)
    const height = notLeafNodeHeightMap[node.id]
    const writeRowIndex = rowIndex + colLength + 1
    const mergeColCount = node.children[0].level - node.level
    const value = node.label
    const cell = worksheet.getCell(writeRowIndex, node.level + 1)
    cell.value = value
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (mergeColCount > 1 || height > 1) {
      worksheet.mergeCells(
        writeRowIndex,
        node.level + 1,
        writeRowIndex + height - 1,
        node.level + mergeColCount
      )
    }
  })

  // 列头
  const { colLeafNodes, colNodes, colsHierarchy } = layoutResult
  const maxColHeight = colsHierarchy.maxLevel + 1
  const notLeafNodeWidthMap = {}
  colLeafNodes.forEach(node => {
    // 列头的宽度由子节点相加决定，也就是列头子节点中包含的叶子节点数量
    let curNode = node.parent
    while (curNode) {
      const width = notLeafNodeWidthMap[curNode.id] ?? 0
      notLeafNodeWidthMap[curNode.id] = width + 1
      curNode = curNode.parent
    }
    const { colIndex } = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 1 + rowLength
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    let value = node.label
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = value
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
  })
  const getNodeStartColIndex = (node) => {
    if (!node.children?.length) {
      return node.colIndex + 1
    } else {
      return getNodeStartColIndex(node.children[0])
    }
  }
  colNodes.forEach(node => {
    if (node.isLeaf) {
      return
    }
    const colIndex = getNodeStartColIndex(node)
    const width = notLeafNodeWidthMap[node.id]
    const writeRowIndex = node.level + 1
    const mergeRowCount = node.children[0].level - node.level
    const value = node.label
    const writeColIndex = colIndex + rowLength
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = value
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (mergeRowCount > 1 || width > 1) {
      worksheet.mergeCells(
        writeRowIndex,
        writeColIndex,
        writeRowIndex + mergeRowCount - 1,
        writeColIndex + width - 1
      )
    }
  })
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = layoutResult.getCellMeta(rowIndex, colIndex)
      const { fieldValue } = dataCellMeta
      if (fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, rowLength + colIndex + 1)
        const value = meta?.formatter?.(fieldValue) || fieldValue.toString()
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.value = value
      }
    }
  }
  const buffer = await workbook.xlsx.writeBuffer()
  const dataBlob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
  })
  saveAs(dataBlob, `${chart.title ?? '透视表'}.xlsx`)
}
