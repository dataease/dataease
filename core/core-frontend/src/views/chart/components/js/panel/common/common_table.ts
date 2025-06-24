/* eslint-disable prettier/prettier */
import {
  copyString,
  hexColorToRGBA,
  isAlphaColor,
  isTransparent,
  parseJson,
  resetRgbOpacity,
  safeDecimalSum,
  safeDecimalMean
} from '../..//util'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_TABLE_CELL,
  DEFAULT_TABLE_HEADER
} from '@/views/chart/components/editor/util/chart'
import {
  BaseTooltip,
  DataCellBrushSelection,
  FONT_FAMILY,
  getAutoAdjustPosition,
  getEmptyPlaceholder,
  getPolygonPoints,
  getTooltipDefaultOptions,
  InteractionName,
  InteractionStateName,
  MergedCell,
  MergedCellInfo,
  type Meta,
  type Node,
  type PivotSheet,
  renderPolygon,
  renderText,
  S2DataConfig,
  S2Event,
  S2Options,
  S2Theme,
  SERIES_NUMBER_FIELD,
  setTooltipContainerStyle,
  SHAPE_STYLE_MAP,
  SpreadSheet,
  Style,
  TableColCell,
  TableDataCell,
  updateShapeAttr,
  ViewMeta
} from '@antv/s2'
import {
  cloneDeep,
  filter,
  find,
  intersection,
  keys,
  map,
  maxBy,
  meanBy,
  merge,
  minBy,
  repeat,
  sumBy,
  size,
  sum
} from 'lodash-es'
import { createVNode, render } from 'vue'
import TableTooltip from '@/views/chart/components/editor/common/TableTooltip.vue'
import Exceljs from 'exceljs'
import { saveAs } from 'file-saver'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import Decimal from 'decimal.js'


const { t: i18nt } = useI18n()

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
  const textFontFamily =
    chart.fontFamily && chart.fontFamily !== 'inherit' ? chart.fontFamily : FONT_FAMILY
  const theme: S2Theme = {
    background: {
      color: '#00000000'
    },
    splitLine: {
      horizontalBorderColor: borderColor,
      horizontalBorderColorOpacity: 1,
      horizontalBorderWidth: 1,
      verticalBorderColor: borderColor,
      verticalBorderColorOpacity: 1,
      verticalBorderWidth: 1,
      showShadow: false
    },
    cornerCell: {
      cell: {
        backgroundColor: headerColor,
        horizontalBorderColor: borderColor,
        verticalBorderColor: borderColor
      },
      text: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
      },
      bolderText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
      },
      measureText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
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
        textBaseline: 'middle',
        fontFamily: textFontFamily
      },
      bolderText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
      },
      measureText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
      },
      seriesText: {
        fill: DEFAULT_TABLE_CELL.tableItemBgColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: itemAlign,
        fontFamily: textFontFamily
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
        textAlign: headerAlign,
        fontFamily: textFontFamily
      },
      bolderText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
      },
      measureText: {
        fill: DEFAULT_TABLE_HEADER.tableHeaderFontColor,
        fontSize: DEFAULT_TABLE_HEADER.tableTitleFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
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
        textAlign: itemAlign,
        fontFamily: textFontFamily
      },
      bolderText: {
        fill: DEFAULT_TABLE_CELL.tableFontColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: itemAlign,
        fontFamily: textFontFamily
      },
      measureText: {
        fill: DEFAULT_TABLE_CELL.tableFontColor,
        fontSize: DEFAULT_TABLE_CELL.tableItemFontSize,
        textAlign: headerAlign,
        fontFamily: textFontFamily
      }
    },
    scrollBar: {
      thumbColor: scrollBarColor,
      thumbHoverColor: scrollBarHoverColor,
      size: 8,
      hoverSize: 12
    }
  }

  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    const { basicStyle, tableHeader, tableCell } = customAttr
    // basic
    if (basicStyle) {
      const tableBorderColor = basicStyle.tableBorderColor
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
            verticalBorderColor: tableBorderColor,
            interactionState: {
              hoverFocus: {
                borderOpacity: basicStyle.showHoverStyle === false ? 0 : 1
              }
            }
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
      let tableHeaderBgColor = tableHeader.tableHeaderBgColor
      if (!isAlphaColor(tableHeaderBgColor)) {
        tableHeaderBgColor = hexColorToRGBA(tableHeaderBgColor, basicStyle.alpha)
      }
      const fontStyle = tableHeader.isItalic ? 'italic' : 'normal'
      const fontWeight = tableHeader.isBolder === false ? 'normal' : 'bold'
      const { tableHeaderAlign, tableTitleFontSize } = tableHeader
      const tmpTheme: S2Theme = {
        cornerCell: {
          cell: {
            backgroundColor: tableHeaderBgColor
          },
          bolderText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          text: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          measureText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          }
        },
        colCell: {
          cell: {
            backgroundColor: tableHeaderBgColor
          },
          bolderText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          text: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          measureText: {
            fill: tableHeaderFontColor,
            fontSize: tableTitleFontSize,
            textAlign: tableHeaderAlign,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          }
        }
      }
      merge(theme, tmpTheme)
      // 这边设置为 0 的话就会显示表头背景颜色，所以要判断一下表头是否关闭
      if (tableHeader.showHorizonBorder === false && tableHeader.showTableHeader !== false) {
        const tmpTheme: S2Theme = {
          splitLine: {
            horizontalBorderColor: tableHeaderBgColor,
            horizontalBorderWidth: 0,
            horizontalBorderColorOpacity: 0
          },
          colCell: {
            cell: {
              horizontalBorderColor: tableHeaderBgColor,
              horizontalBorderWidth: 0
            }
          }
        }
        merge(theme, tmpTheme)
      }
      if (tableHeader.showVerticalBorder === false && tableHeader.showTableHeader !== false) {
        const tmpTheme: S2Theme = {
          splitLine: {
            verticalBorderColor: tableHeaderBgColor,
            verticalBorderWidth: 0,
            verticalBorderColorOpacity: 0
          },
          colCell: {
            cell: {
              verticalBorderColor: tableHeaderBgColor,
              verticalBorderWidth: 0
            }
          },
          cornerCell: {
            cell: {
              verticalBorderColor: tableHeaderBgColor,
              verticalBorderWidth: 0
            }
          }
        }
        merge(theme, tmpTheme)
      }
    }
    // cell
    if (tableCell) {
      const tableFontColor = hexColorToRGBA(tableCell.tableFontColor, basicStyle.alpha)
      let tableItemBgColor = tableCell.tableItemBgColor
      if (!isAlphaColor(tableItemBgColor)) {
        tableItemBgColor = hexColorToRGBA(tableItemBgColor, basicStyle.alpha)
      }
      let tableItemSubBgColor = tableCell.tableItemSubBgColor
      if (!isAlphaColor(tableItemSubBgColor)) {
        tableItemSubBgColor = hexColorToRGBA(tableItemSubBgColor, basicStyle.alpha)
      }
      const fontStyle = tableCell.isItalic ? 'italic' : 'normal'
      const fontWeight = tableCell.isBolder === false ? 'normal' : 'bold'
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
            fontSize: tableItemFontSize,
            fontFamily: textFontFamily
          },
          text: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontFamily: textFontFamily
          },
          measureText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontFamily: textFontFamily
          },
          seriesText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontFamily: textFontFamily
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
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          text: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          measureText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          },
          seriesText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight,
            fontFamily: textFontFamily
          }
        }
      }
      merge(theme, tmpTheme)
      if (tableCell.showHorizonBorder === false) {
        const tmpTheme: S2Theme = {
          dataCell: {
            cell: {
              horizontalBorderColor: tableItemBgColor,
              horizontalBorderWidth: 0
            }
          }
        }
        merge(theme, tmpTheme)
      }
      if (tableCell.showVerticalBorder === false) {
        const tmpTheme: S2Theme = {
          splitLine: {
            verticalBorderWidth: 0,
            verticalBorderColorOpacity: 0
          },
          dataCell: {
            cell: {
              verticalBorderColor: tableItemBgColor,
              verticalBorderWidth: 0
            }
          }
        }
        merge(theme, tmpTheme)
      }
    }
  }

  return theme
}

export function getStyle(chart: Chart, dataConfig: S2DataConfig): Style {
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
        // 铺满
        const totalWidthPercent = dataConfig.meta?.reduce((p, n) => {
          return p + (fieldMap[n.field]?.width ?? 10)
        }, 0)
        const fullFilled = parseInt(totalWidthPercent.toFixed(0)) === 100
        const widthArr = []
        style.colCfg.width = node => {
          const width = node.spreadsheet.container.cfg.el.getBoundingClientRect().width
          if (!basicStyle.tableFieldWidth?.length) {
            const fieldsSize = chart.data.fields.length
            const columnCount = tableHeader.showIndex ? fieldsSize + 1 : fieldsSize
            return width / columnCount
          }
          const baseWidth = width / 100
          const tmpWidth = fieldMap[node.field]
            ? fieldMap[node.field].width * baseWidth
            : baseWidth * 10
          const resultWidth = parseInt(tmpWidth.toFixed(0))
          if (fullFilled) {
            if (widthArr.length === dataConfig.meta.length - 1) {
              const curTotalWidth = widthArr.reduce((p, n) => {
                return p + n
              }, 0)
              const restWidth = width - curTotalWidth
              widthArr.splice(0)
              if (restWidth < resultWidth) {
                return restWidth
              }
            } else {
              widthArr.push(resultWidth)
            }
          }
          return resultWidth
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
          const fieldsSize = node.spreadsheet.dataCfg.meta.length
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
    // 合并单元格时斑马纹失效
    const enableTableCrossBG =
      chart.type === 'table-info'
        ? tableCell.enableTableCrossBG && !tableCell.mergeCells
        : tableCell.enableTableCrossBG
    const valueColor = isAlphaColor(tableCell.tableFontColor)
      ? tableCell.tableFontColor
      : hexColorToRGBA(tableCell.tableFontColor, basicStyle.alpha)
    const valueBgColor = enableTableCrossBG
      ? null
      : isAlphaColor(tableCell.tableItemBgColor)
      ? tableCell.tableItemBgColor
      : hexColorToRGBA(tableCell.tableItemBgColor, basicStyle.alpha)
    const headerValueColor = tableHeader.tableHeaderFontColor
    const headerValueBgColor = isAlphaColor(tableHeader.tableHeaderBgColor)
      ? tableHeader.tableHeaderBgColor
      : hexColorToRGBA(tableHeader.tableHeaderBgColor, basicStyle.alpha)
    const filedValueMap = getFieldValueMap(chart)
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
            fill: mappingColor(value, defaultValueColor, field, 'color', filedValueMap, rowData)
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
          const fill = mappingColor(
            value,
            defaultBgColor,
            field,
            'backgroundColor',
            filedValueMap,
            rowData
          )
          if (isTransparent(fill)) {
            return null
          }
          return { fill }
        }
      })
    }
  }
  return res
}

export function mappingColor(value, defaultColor, field, type, filedValueMap?, rowData?) {
  let color = null
  for (let i = 0; i < field.conditions.length; i++) {
    let flag = false
    const t = field.conditions[i]
    let tv, max, min
    if (t.type === 'dynamic') {
      if (t.term === 'between') {
        max = parseFloat(getValue(t.dynamicMaxField, filedValueMap, rowData))
        min = parseFloat(getValue(t.dynamicMinField, filedValueMap, rowData))
      } else {
        tv = getValue(t.dynamicField, filedValueMap, rowData)
      }
    } else {
      if (t.term === 'between') {
        min = parseFloat(t.min)
        max = parseFloat(t.max)
      } else {
        tv = t.value
      }
    }
    if (field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) {
      tv = parseFloat(tv)
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
      } else if (t.term === 'default') {
        color = t[type]
        flag = true
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else if (field.field.deType === 0 || field.field.deType === 5) {
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
      } else if (t.term === 'default') {
        color = t[type]
        flag = true
      }
      if (flag) {
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else {
      // time
      if (!tv || !value) {
        break
      }
      const fc = field.conditions[i]
      tv = new Date(tv.replace(/-/g, '/') + ' GMT+8').getTime()
      const v = new Date(value.replace(/-/g, '/') + ' GMT+8').getTime()
      if (fc.term === 'eq') {
        if (v === tv) {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'not_eq') {
        if (v !== tv) {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'lt') {
        if (v < tv) {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'gt') {
        if (v > tv) {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'le') {
        if (v <= tv) {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'ge') {
        if (v >= tv) {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'default') {
        color = fc[type]
        flag = true
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

function getFieldValueMap(view) {
  const fieldValueMap = {}
  if (view.data && view.data.dynamicAssistLines && view.data.dynamicAssistLines.length > 0) {
    view.data.dynamicAssistLines.forEach(ele => {
      fieldValueMap[ele.summary + '-' + ele.fieldId] = ele.value
    })
  }
  return fieldValueMap
}

function getValue(field, filedValueMap, rowData) {
  if (field.summary === 'value') {
    return rowData ? rowData[field.field?.dataeaseName] : undefined
  } else {
    return filedValueMap[field.summary + '-' + field.fieldId]
  }
}

export function handleTableEmptyStrategy(chart: Chart) {
  let newData = (chart.data?.tableRow || []) as Record<string, any>[]
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
        // 分组
        if (meta.colIndex === -1) {
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
        const parent = document.getElementById(chart.container)
        if (parent?.childNodes?.length) {
          const child = Array.from(parent.childNodes)
            .filter(node => node.nodeType === Node.ELEMENT_NODE)
            .find(node => node.classList.contains('antv-s2-tooltip-container'))
          if (child) {
            const left = child.offsetLeft + child.clientWidth
            if (left > parent.offsetWidth) {
              const newLeft = parent.offsetWidth - child.clientWidth - 10
              child.style.left = `${newLeft}px`
            }
          }
        }
      }
    }
  ]
}

export function configTooltip(chart: Chart, option: S2Options) {
  const { tooltip } = parseJson(chart.customAttr)
  const textFontFamily = chart.fontFamily ? chart.fontFamily : FONT_FAMILY
  option.tooltip = {
    ...option.tooltip,
    style: {
      background: tooltip.backgroundColor,
      fontSize: tooltip.fontSize + 'px',
      fontFamily: textFontFamily,
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

export function copyContent(s2Instance: SpreadSheet, event, fieldMeta) {
  event.preventDefault()
  const cell = s2Instance.getCell(event.target)
  const valueField = cell.getMeta().valueField
  const cellMeta = cell.getMeta()
  const selectState = s2Instance.interaction.getState()
  let content = ''
  // 多选
  if (selectState.stateName === InteractionStateName.SELECTED) {
    const { cells } = selectState
    if (!cells?.length) {
      return
    }
    if (cells.length === 1) {
      const curCell = cells[0]
      if (cell.getMeta().id === curCell.id) {
        const cellMeta = cell.getMeta()
        const value = cellMeta.data?.[cellMeta.valueField]
        const metaObj = find(fieldMeta, m => m.field === cellMeta.valueField)
        let fieldVal = value?.toString()
        if (metaObj) {
          fieldVal = metaObj.formatter(value)
        }
        copyString(fieldVal, true)
      }
      s2Instance.interaction.clearState()
      return
    }
    const brushSelection = s2Instance.interaction.interactions.get(
      InteractionName.BRUSH_SELECTION
    ) as DataCellBrushSelection
    const selectedCells: TableDataCell[] = brushSelection.getScrollBrushRangeCells(cells)
    selectedCells.sort((a, b) => {
      const aMeta = a.getMeta()
      const bMeta = b.getMeta()
      if (aMeta.rowIndex !== bMeta.rowIndex) {
        return aMeta.rowIndex - bMeta.rowIndex
      }
      return aMeta.colIndex - bMeta.colIndex
    })
    // 点击已选的就复制，未选的就忽略
    let validClick = false
    const matrix = selectedCells.reduce((p, n) => {
      if (
        n.getMeta().colIndex === cellMeta.colIndex &&
        n.getMeta().rowIndex === cellMeta.rowIndex
      ) {
        validClick = true
      }
      const arr = p[n.getMeta().rowIndex]
      if (!arr) {
        p[n.getMeta().rowIndex] = [n]
      } else {
        arr.push(n)
      }
      return p
    }, {}) as Record<number, TableDataCell[]>
    if (validClick) {
      keys(matrix).forEach(k => {
        const arr = matrix[k] as TableDataCell[]
        arr.forEach((cell, index) => {
          const cellMeta = cell.getMeta()
          const value = cellMeta.data?.[cellMeta.valueField]
          const metaObj = find(fieldMeta, m => m.field === cellMeta.valueField)
          let fieldVal = value?.toString()
          if (metaObj) {
            fieldVal = metaObj.formatter(value)
          }
          if (fieldVal === undefined || fieldVal === null) {
            fieldVal = ''
          }
          if (index !== arr.length - 1) {
            fieldVal += '\t'
          }
          content += fieldVal
        })
        content = content + '\n'
      })
      if (content) {
        copyString(content, true)
      }
    }
    s2Instance.interaction.clearState()
    return
  }
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

export async function exportGridPivot(instance: PivotSheet, chart: ChartObj) {
  const { layoutResult } = instance.facet
  const { meta, fields } = instance.dataCfg
  const rowLength = fields?.rows?.length || 0
  const colLength = fields?.columns?.length || 0
  const colNums = layoutResult.colLeafNodes.length + rowLength
  if (colNums > 16384) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_col_exceed'))
    return
  }
  const workbook = new Exceljs.Workbook()
  const worksheet = workbook.addWorksheet(i18nt('chart.chart_data'))
  const metaMap: Record<string, Meta> = meta?.reduce((p, n) => {
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
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  fields?.rows?.forEach((row, index) => {
    const cell = worksheet.getCell(colLength + 1, index + 1)
    cell.value = metaMap[row]?.name ?? row
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    cell.border = {
      bottom: { style: 'thick', color: { argb: '00000000' } }
    }
    if (index === fields.rows.length - 1) {
      cell.border.right = { style: 'thick', color: { argb: '00000000' } }
    }
  })
  // 行头
  const { rowLeafNodes, rowsHierarchy, rowNodes } = layoutResult
  const maxColIndex = rowsHierarchy.maxLevel + 1
  const notLeafNodeHeightMap: Record<string, number> = {}
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
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })

  const getNodeStartRowIndex = (node: Node) => {
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
  const notLeafNodeWidthMap: Record<string, number> = {}
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
    cell.border = {
      bottom: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  const getNodeStartColIndex = (node: Node) => {
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
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, rowLength + colIndex + 1)
        const value = meta?.formatter?.(fieldValue) || fieldValue
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.value = isNumeric(value) ? parseFloat(value) : value
      }
    }
  }
  const buffer = await workbook.xlsx.writeBuffer()
  const dataBlob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
  })
  saveAs(dataBlob, `${chart.title ?? '透视表'}.xlsx`)
}

export async function exportRowQuotaGridPivot(instance: PivotSheet, chart: ChartObj) {
  const { layoutResult } = instance.facet
  const { meta, fields } = instance.dataCfg
  const rowLength = fields?.rows?.length || 0
  const colLength = fields?.columns?.length || 0
  const colNums = layoutResult.colLeafNodes.length + rowLength
  if (colNums > 16384) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_col_exceed'))
    return
  }
  const workbook = new Exceljs.Workbook()
  const worksheet = workbook.addWorksheet(i18nt('chart.chart_data'))
  const metaMap: Record<string, Meta> = meta?.reduce((p, n) => {
    if (n.field) {
      p[n.field] = n
    }
    return p
  }, {})
  // 角头
  if (colLength > 1) {
    fields.columns.forEach((column: string, index) => {
      if (index >= colLength - 1) {
        return
      }
      const cell = worksheet.getCell(index + 1, 1)
      cell.value = metaMap[column]?.name ?? column
      cell.alignment = { vertical: 'middle', horizontal: 'center' }
      cell.border = {
        right: { style: 'thick', color: { argb: '00000000' } }
      }
      worksheet.mergeCells(index + 1, 1, index + 1, rowLength + 1)
    })
  }
  fields?.rows?.forEach((row, index) => {
    const cell = worksheet.getCell(colLength === 0 ? 1 : colLength, index + 1)
    cell.value = metaMap[row]?.name ?? row
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    cell.border = { bottom: { style: 'thick', color: { argb: '00000000' } } }
  })
  const quotaColLabel = chart.customAttr.basicStyle.quotaColLabel ?? t('dataset.value')
  const quotaColHeadCell = worksheet.getCell(colLength === 0 ? 1 : colLength, rowLength + 1)
  quotaColHeadCell.value = quotaColLabel
  quotaColHeadCell.alignment = { vertical: 'middle', horizontal: 'center' }
  quotaColHeadCell.border = {
    bottom: { style: 'thick', color: { argb: '00000000' } },
    right: { style: 'thick', color: { argb: '00000000' } }
  }
  // 行头
  const { rowLeafNodes, rowNodes } = layoutResult
  const notLeafNodeHeightMap: Record<string, number> = {}
  rowLeafNodes.forEach(node => {
    // 行头的高度由子节点相加决定，也就是行头子节点中包含的叶子节点数量
    let curNode = node.parent
    while (curNode) {
      const height = notLeafNodeHeightMap[curNode.id] ?? 0
      notLeafNodeHeightMap[curNode.id] = height + 1
      curNode = curNode.parent
    }
    const { rowIndex } = node
    const writeRowIndex = rowIndex + 2 + (colLength === 0 ? 1 : colLength - 1)
    const writeColIndex = node.level + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    let value = node.label
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = value
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })

  const getNodeStartRowIndex = (node: Node) => {
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
    const writeRowIndex = rowIndex + 1 + (colLength === 0 ? 1 : colLength - 1)
    const mergeColCount = node.children[0].level - node.level
    const cell = worksheet.getCell(writeRowIndex, node.level + 1)
    cell.value = node.label
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
  const notLeafNodeWidthMap: Record<string, number> = {}
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
    const writeColIndex = colIndex + rowLength + 2
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    const value = node.label
    cell.value = value
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
    cell.border = {
      bottom: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  const getNodeStartColIndex = (node: Node) => {
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
    const value = node.label
    const writeColIndex = colIndex + rowLength + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = value
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (width > 1) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, writeRowIndex, writeColIndex + width - 1)
    }
  })
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = layoutResult.getCellMeta(rowIndex, colIndex)
      const { fieldValue } = dataCellMeta
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, rowLength + colIndex + 2)
        const value = meta?.formatter?.(fieldValue) || fieldValue
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.value = isNumeric(value) ? parseFloat(value) : value
      }
    }
  }
  const buffer = await workbook.xlsx.writeBuffer()
  const dataBlob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
  })
  saveAs(dataBlob, `${chart.title ?? '透视表'}.xlsx`)
}

export async function exportTreePivot(instance: PivotSheet, chart: ChartObj) {
  const layoutResult = instance.facet.layoutResult
  if (layoutResult.colLeafNodes.length + 1 > 16384) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_col_exceed'))
    return
  }
  const { meta, fields } = instance.dataCfg
  const colLength = fields?.columns?.length || 0
  const workbook = new Exceljs.Workbook()
  const worksheet = workbook.addWorksheet(i18nt('chart.chart_data'))
  const metaMap: Record<string, Meta> = meta?.reduce((p, n) => {
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
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  const maxColHeight = layoutResult.colsHierarchy.maxLevel + 1
  const rowName = fields?.rows?.map(row => metaMap[row]?.name ?? row).join('/')
  const cell = worksheet.getCell(colLength + 1, 1)
  cell.value = rowName
  cell.alignment = { vertical: 'middle', horizontal: 'center' }
  cell.border = {
    right: { style: 'thick', color: { argb: '00000000' } },
    bottom: { style: 'thick', color: { argb: '00000000' } }
  }
  //行头
  const { rowLeafNodes } = layoutResult
  rowLeafNodes.forEach((node, index) => {
    const cell = worksheet.getCell(maxColHeight + index + 1, 1)
    cell.value = repeat('  ', node.level) + node.label
    cell.alignment = { vertical: 'middle', horizontal: 'left' }
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  // 列头
  const notLeafNodeWidthMap: Record<string, number> = {}
  const { colLeafNodes } = layoutResult
  colLeafNodes.forEach(node => {
    let curNode = node.parent
    while (curNode) {
      const width = notLeafNodeWidthMap[curNode.id] ?? 0
      notLeafNodeWidthMap[curNode.id] = width + 1
      curNode = curNode.parent
    }
    const { colIndex } = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 1 + 1
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
    cell.border = {
      bottom: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  const colNodes = layoutResult.colNodes
  const getNodeStartIndex = (node: Node) => {
    if (!node.children?.length) {
      return node.colIndex + 1
    } else {
      return getNodeStartIndex(node.children[0])
    }
  }
  colNodes.forEach(node => {
    if (node.isLeaf) {
      return
    }
    const colIndex = getNodeStartIndex(node)
    const width = notLeafNodeWidthMap[node.id]
    const writeRowIndex = node.level + 1
    const mergeRowCount = node.children[0].level - node.level
    const writeColIndex = colIndex + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = node.label
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
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, colIndex + 1 + 1)
        const value = meta?.formatter?.(fieldValue) || fieldValue
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.value = isNumeric(value) ? parseFloat(value) : value
      }
    }
  }
  const buffer = await workbook.xlsx.writeBuffer()
  const dataBlob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
  })
  saveAs(dataBlob, `${chart.title ?? '透视表'}.xlsx`)
}

export async function exportRowQuotaTreePivot(instance: PivotSheet, chart: ChartObj) {
  const layoutResult = instance.facet.layoutResult
  if (layoutResult.colLeafNodes.length + 1 > 16384) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_col_exceed'))
    return
  }
  const { meta, fields } = instance.dataCfg
  const colLength = fields?.columns?.length || 0
  const workbook = new Exceljs.Workbook()
  const worksheet = workbook.addWorksheet(i18nt('chart.chart_data'))
  const metaMap: Record<string, Meta> = meta?.reduce((p, n) => {
    if (n.field) {
      p[n.field] = n
    }
    return p
  }, {})

  // 角头
  fields.columns?.forEach((column, index) => {
    if (index >= fields.columns.length - 1) {
      return
    }
    const cell = worksheet.getCell(index + 1, 1)
    cell.value = metaMap[column]?.name ?? column
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  const quotaColLabel = chart.customAttr.basicStyle.quotaColLabel ?? t('dataset.value')
  const maxColHeight = layoutResult.colsHierarchy.maxLevel + 1
  const rowName = fields?.rows
    ?.map(row => metaMap[row]?.name ?? row)
    .concat(quotaColLabel)
    .join('/')
  const cell = worksheet.getCell(colLength, 1)
  cell.value = rowName
  cell.alignment = { vertical: 'middle', horizontal: 'center' }
  cell.border = {
    right: { style: 'thick', color: { argb: '00000000' } },
    bottom: { style: 'thick', color: { argb: '00000000' } }
  }
  //行头
  const { rowLeafNodes } = layoutResult
  rowLeafNodes.forEach((node, index) => {
    const cell = worksheet.getCell(maxColHeight + index + 1, 1)
    let value = node.label
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = repeat('  ', node.level) + value
    cell.alignment = { vertical: 'middle', horizontal: 'left' }
    cell.border = {
      right: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  // 列头
  const notLeafNodeWidthMap: Record<string, number> = {}
  const { colLeafNodes } = layoutResult
  colLeafNodes.forEach(node => {
    let curNode = node.parent
    while (curNode) {
      const width = notLeafNodeWidthMap[curNode.id] ?? 0
      notLeafNodeWidthMap[curNode.id] = width + 1
      curNode = curNode.parent
    }
    const { colIndex } = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 2
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = node.label
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
    cell.border = {
      bottom: { style: 'thick', color: { argb: '00000000' } }
    }
  })
  const colNodes = layoutResult.colNodes
  const getNodeStartIndex = (node: Node) => {
    if (!node.children?.length) {
      return node.colIndex + 1
    } else {
      return getNodeStartIndex(node.children[0])
    }
  }
  colNodes.forEach(node => {
    if (node.isLeaf) {
      return
    }
    const colIndex = getNodeStartIndex(node)
    const width = notLeafNodeWidthMap[node.id]
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = node.label
    cell.alignment = { vertical: 'middle', horizontal: 'center' }
    if (width > 1) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, writeRowIndex, writeColIndex + width - 1)
    }
  })
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = layoutResult.getCellMeta(rowIndex, colIndex)
      const { fieldValue } = dataCellMeta
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, colIndex + 2)
        const value = meta?.formatter?.(fieldValue) || fieldValue
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.value = isNumeric(value) ? parseFloat(value) : value
      }
    }
  }
  const buffer = await workbook.xlsx.writeBuffer()
  const dataBlob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
  })
  saveAs(dataBlob, `${chart.title ?? '透视表'}.xlsx`)
}


function isNumeric(value: string): boolean {
  return /^[+-]?\d+(\.\d+)?$/.test(value)
}

export async function exportPivotExcel(instance: PivotSheet, chart: ChartObj) {
  const { fields } = instance.dataCfg
  const rowLength = fields?.rows?.length || 0
  const valueLength = fields?.values?.length || 0
  if (!(rowLength && valueLength)) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_field'))
    return
  }
  const { quotaPosition } = chart.customAttr.basicStyle
  if (chart.customAttr.basicStyle.tableLayoutMode !== 'tree') {
    if (quotaPosition === 'row') {
      exportRowQuotaGridPivot(instance, chart)
    } else {
      exportGridPivot(instance, chart)
    }
  } else {
    if (quotaPosition === 'row') {
      exportRowQuotaTreePivot(instance, chart)
    } else {
      exportTreePivot(instance, chart)
    }
  }
}

export function configMergeCells(chart: Chart, options: S2Options, dataConfig: S2DataConfig) {
  const { mergeCells } = parseJson(chart.customAttr).tableCell
  const { showIndex } = parseJson(chart.customAttr).tableHeader
  if (mergeCells) {
    options.frozenColCount = 0
    options.frozenRowCount = 0
    const fields = chart.data.fields || []
    const fieldsMap =
      fields.reduce((p, n) => {
        p[n.dataeaseName] = n
        return p
      }, {}) || {}
    const quotaIndex = dataConfig.meta.findIndex(m => fieldsMap[m.field]?.groupType === 'q')
    const data = chart.data?.tableRow
    if (quotaIndex === 0 || !data?.length) {
      return
    }
    const mergedColInfo: number[][][] = [[[0, data.length - 1]]]
    const mergedCellsInfo = []
    const axisToMerge = dataConfig.meta.filter((_, i) => i < quotaIndex || quotaIndex === -1)
    axisToMerge.forEach((a, i) => {
      const preMergedColInfo = mergedColInfo[i]
      const curMergedColInfo = []
      mergedColInfo.push(curMergedColInfo)
      preMergedColInfo.forEach(range => {
        const [start, end] = range
        let lastVal = data[start][a.field]
        let lastIndex = start
        for (let index = start; index <= end; index++) {
          const curVal = data[index][a.field]
          if (curVal !== lastVal || index === end) {
            const curRange = index - lastIndex
            if (curRange > 1 || (index === end && curRange === 1 && lastVal === curVal)) {
              const tmpMergeCells = []
              const textIndex = curRange % 2 === 1 ? (curRange - 1) / 2 : curRange / 2 - 1
              for (let j = 0; j < curRange; j++) {
                tmpMergeCells.push({
                  colIndex: showIndex ? i + 1 : i,
                  rowIndex: lastIndex + j,
                  showText: j === textIndex
                })
              }
              if (index === end && lastVal === curVal) {
                tmpMergeCells.push({
                  colIndex: showIndex ? i + 1 : i,
                  rowIndex: index,
                  showText: false
                })
              }
              mergedCellsInfo.push(tmpMergeCells)
              curMergedColInfo.push([
                lastIndex,
                index === end && lastVal === curVal ? index : index - 1
              ])
            }
            lastVal = curVal
            lastIndex = index
          }
        }
      })
    })
    if (showIndex) {
      const indexMergedCells = mergedCellsInfo.filter(cells => cells[0].colIndex === 1)
      indexMergedCells.forEach(cells => {
        const tmpCells = cloneDeep(cells)
        tmpCells.forEach(cell => {
          cell.colIndex = 0
        })
        mergedCellsInfo.unshift(tmpCells)
      })
    }
    options.mergedCellsInfo = mergedCellsInfo
    options.mergedCell = (sheet, cells, meta) => {
      if (showIndex && meta.colIndex === 0) {
        meta.fieldValue = getRowIndex(mergedCellsInfo, meta)
      }
      meta.deFieldType = fieldsMap[meta.valueField]?.deType
      return new CustomMergedCell(sheet, cells, meta)
    }
  }
}

export function getRowIndex(mergedCellsInfo: MergedCellInfo[][], meta: ViewMeta): number {
  if (!mergedCellsInfo?.length) {
    return meta.rowIndex + 1
  }
  let curRangeStartIndex = meta.rowIndex
  const lostCells = mergedCellsInfo.reduce((p, n) => {
    if (n[0].colIndex !== 0) {
      return p
    }
    const start = n[0].rowIndex
    const end = n[n.length - 1].rowIndex
    const lost = end - start
    if (meta.rowIndex >= start && meta.rowIndex <= end) {
      curRangeStartIndex = start
    }
    if (meta.rowIndex > end) {
      return p + lost
    }
    return p
  }, 0)
  return curRangeStartIndex - lostCells + 1
}

class CustomMergedCell extends MergedCell {
  protected drawBackgroundShape() {
    const allPoints = getPolygonPoints(this.cells)
    // 处理条件样式，这里没有用透明度
    // 因为合并的单元格是单独的图层，透明度降低的话会显示底下未合并的单元格，需要单独处理被覆盖的单元格
    const { backgroundColor: fill } = this.getBackgroundColor()
    const cellTheme = this.theme.dataCell.cell
    this.backgroundShape = renderPolygon(this, {
      points: allPoints,
      stroke: cellTheme.horizontalBorderColor,
      fill,
      lineHeight: cellTheme.horizontalBorderWidth
    })
  }

  drawTextShape(): void {
    if (this.meta.deFieldType === 7) {
      drawImage.apply(this)
    } else {
      super.drawTextShape()
    }
  }
}

export class CustomDataCell extends TableDataCell {
  /**
   * 重写这个方法是为了处理底部的汇总行取消 hover 状态时设置 border 为 1,
   * 这样会导致单元格隐藏横边边框失败，出现一条白线
   */
  hideInteractionShape() {
    this.stateShapes.forEach(shape => {
      updateShapeAttr(shape, SHAPE_STYLE_MAP.backgroundOpacity, 0)
      updateShapeAttr(shape, SHAPE_STYLE_MAP.backgroundColor, 'transparent')
      updateShapeAttr(shape, SHAPE_STYLE_MAP.borderOpacity, 0)
      updateShapeAttr(shape, SHAPE_STYLE_MAP.borderWidth, 0)
      updateShapeAttr(shape, SHAPE_STYLE_MAP.borderColor, 'transparent')
    })
  }

  /**
   * 重写绘制文本内容的方法
   * @protected
   */
  protected drawTextShape() {
    if (this.meta.autoWrap) {
      drawTextShape(this, false)
    } else {
      super.drawTextShape()
    }
  }
}

export class CustomTableColCell extends TableColCell {
  /**
   * 重写是为了表头文本内容的换行
   * @protected
   */
  protected drawTextShape() {
    if (this.meta.autoWrap) {
      drawTextShape(this, true)
    } else {
      super.drawTextShape()
    }
  }
}

/**
 * 绘制文本 换行
 * @param cell
 * @param isHeader
 */
const drawTextShape = (cell, isHeader) => {
  // 换行符
  const lineBreak = '\n'
  // 省略号
  const ellipsis = '...'
  // 用户配置的最大行数
  const maxLines = cell.meta.maxLines ?? 1
  const {
    options: { placeholder }
  } = cell.spreadsheet
  const emptyPlaceholder = getEmptyPlaceholder(this, placeholder)
  // 单元格文本
  const { formattedValue } = cell.getFormattedFieldValue()
  // 获取文本样式
  const textStyle = cell.getTextStyle()
  // 宽度能放几个字符，就放几个，放不下就换行
  let wrapText = getWrapText(
    formattedValue ? formattedValue?.toString() : emptyPlaceholder,
    textStyle,
    cell.meta.width,
    cell.spreadsheet
  )
  const lines = wrapText.split(lineBreak)
  let extraStyleFontSize = textStyle.fontSize
  // 不是表头，处理文本高度和换行
  if (!isHeader) {
    const textHeight = getWrapTextHeight(
      wrapText.replaceAll(lineBreak, ''),
      textStyle,
      cell.spreadsheet,
      maxLines
    )
    const lineCountInCell = Math.floor(cell.meta.height / textHeight)
    const wrapTextArr = lines.slice(0, lineCountInCell)

    // 根据行数调整换行后的文本内容
    wrapText = lineCountInCell < 1 ? ellipsis : wrapTextArr.join(lineBreak) || ellipsis
    const resultWrapArr = wrapText.split(lineBreak)
    // 控制最大行数
    if (
      !wrapText.endsWith(ellipsis) &&
      (lines.length > maxLines || lines.length > lineCountInCell)
    ) {
      // 第一行的字符个数
      const firstLineStrNumber = resultWrapArr[0].length
      const temp = resultWrapArr.slice(0, Math.min(maxLines, lineCountInCell))
      // 修改最后一行的字符,按照第一行字符个数-1，修改最后一行的字符为...
      temp[temp.length - 1] = temp[temp.length - 1].slice(0, firstLineStrNumber - 1) + ellipsis
      wrapText = temp.join(lineBreak)
    }
    if (wrapText === ellipsis) {
      extraStyleFontSize = 12
    }
  } else {
    const resultWrapArr = wrapText.split(lineBreak)
    // 控制最大行数
    if (lines.length > maxLines) {
      const temp = resultWrapArr.slice(0, maxLines)
      // 第一行的字符个数
      const firstLineStrNumber = resultWrapArr[0].length
      // 修改最后一行的字符
      temp[temp.length - 1] = temp[temp.length - 1].slice(0, firstLineStrNumber - 1) + ellipsis
      wrapText = temp.join(lineBreak)
    }
  }
  // 设置最终文本和其宽度
  cell.actualText = wrapText
  cell.actualTextWidth = cell.spreadsheet.measureTextWidth(wrapText, textStyle)

  // 获取文本位置并渲染文本
  const position = cell.getTextPosition()
  // 绘制文本
  cell.textShape = renderText(cell, [cell.textShape], position.x, position.y, wrapText, textStyle, {
    fontSize: extraStyleFontSize
  })

  // 将文本形状添加到形状数组
  cell.textShapes.push(cell.textShape)
}

/**
 * 计算表头高度
 * @param info 单元格信息
 * @param newChart
 * @param tableHeader 表头配置
 * @param basicStyle 表格基础样式
 * @param layoutResult
 */
export const calculateHeaderHeight = (info, newChart, tableHeader, basicStyle, layoutResult) => {
  if (tableHeader.showTableHeader === false) return
  const ev = layoutResult || newChart.facet.layoutResult
  const maxLines = basicStyle.maxLines ?? 1
  const textStyle = { ...newChart.theme.cornerCell.text }
  const sourceText = info.info.meta.value
  let maxHeight = getWrapTextHeight(
    getWrapText(sourceText, textStyle, info.info.resizedWidth, ev.spreadsheet),
    textStyle,
    ev.spreadsheet,
    maxLines
  )

  // 获取最大高度的列，排除当前列
  const maxHeightCol = ev.colLeafNodes
    .filter(n => n.colIndex !== info.info.meta.colIndex)
    .reduce(
      (maxHeightNode, currentNode) => {
        const wrapTextHeight = getWrapTextHeight(
          getWrapText(currentNode.value, textStyle, currentNode.width, currentNode.spreadsheet),
          textStyle,
          currentNode.spreadsheet,
          maxLines
        )
        return wrapTextHeight > maxHeightNode.height
          ? { height: wrapTextHeight, colIndex: currentNode.colIndex }
          : maxHeightNode
      },
      { height: 0 }
    )

  // 使用最大高度
  maxHeight = Math.max(maxHeight, maxHeightCol.height) + textStyle.fontSize + 10.5

  if (layoutResult) {
    if (basicStyle.tableColumnMode === 'adapt') maxHeight -= textStyle.fontSize - 2
    ev.colLeafNodes.forEach(n => (n.height = maxHeight))
    ev.colsHierarchy.height = maxHeight
  }

  newChart.store.set('autoCalcHeight', maxHeight)
}

/**
 * 获取换行文本
 * 累加字符串单个字符的宽度，超过单元格宽度时，添加换行
 * @param sourceText
 * @param textStyle
 * @param cellWidth
 * @param spreadsheet
 */
const getWrapText = (sourceText, textStyle, cellWidth, spreadsheet) => {
  if (!sourceText && sourceText !== 0) return ''
  sourceText = sourceText.toString().trim()
  const getTextWidth = text => spreadsheet.measureTextWidthRoughly(text, textStyle)

  let resultWrapText = ''
  let restText = ''
  let restTextWidth = 0
  for (let i = 0; i < sourceText.length; i++) {
    const char = sourceText[i]
    const charWidth = getTextWidth(char)
    restTextWidth += charWidth
    restText += char
    // 中文时，需要单元格宽度减去16个文字宽度，否则会超出单元格宽度
    const cWidth = char.charCodeAt(0) >= 128 ? 12 : 8
    // 添加换行
    if (restTextWidth >= cellWidth - textStyle.fontSize - cWidth) {
      // 最后一个字符不添加换行符
      resultWrapText += restText + (i !== sourceText.length - 1 ? '\n' : '')
      restText = ''
      restTextWidth = 0
    }
  }

  resultWrapText += restText
  return resultWrapText
}
/**
 * 计算文本行高
 * @param wrapText
 * @param textStyle
 * @param spreadsheet
 * @param maxLines 最大行数
 */
const getWrapTextHeight = (wrapText, textStyle, spreadsheet, maxLines) => {
  // 行内最高
  let maxHeight = 0
  // 获取最高字符的高度
  for (const char of wrapText) {
    const h = textStyle.fontSize / (char.charCodeAt(0) >= 128 ? 5 : 2.5)
    maxHeight = Math.max(maxHeight, spreadsheet.measureTextHeight(char, textStyle) + h)
  }
  // 行数
  const lines = wrapText.split('\n').length
  return Math.min(lines, maxLines) * maxHeight
}

// 导出获取汇总行的函数
export function getSummaryRow(data, axis, sumCon = []) {
  const summaryObj = { SUMMARY: true }
  for (let i = 0; i < axis.length; i++) {
    const a = axis[i].dataeaseName
    let savedAxis = find(sumCon, s => s.field === a)
    if (savedAxis) {
      if (savedAxis.summary == undefined) {
        savedAxis.summary = 'sum' // 默认汇总方式为求和
      }
      if (savedAxis.show == undefined) {
        savedAxis.show = true // 默认显示汇总结果
      }
    } else {
      savedAxis = {
        field: a,
        summary: 'sum',
        show: true
      }
    }
    // 如果配置为不显示，则跳过该字段
    if (!savedAxis.show) {
      continue
    }
    // 根据汇总方式处理数据
    switch (savedAxis.summary) {
      case 'sum':
        // 计算字段的总和
        summaryObj[a] = safeDecimalSum(data, a)
        break
      case 'avg':
        // 计算字段的平均值
        summaryObj[a] = safeDecimalMean(data, a)
        break
      case 'max':
        // 计算字段的最大值
        summaryObj[a] = maxBy(
          filter(data, d => parseFloat(d[a]) !== undefined),
          d => parseFloat(d[a]) // 提取数值
        )[a]
        break
      case 'min':
        // 计算字段的最小值
        summaryObj[a] = minBy(
          filter(data, d => parseFloat(d[a]) !== undefined),
          d => parseFloat(d[a]) // 提取数值
        )[a]
        break
      case 'var_pop':
        // 计算总体方差（需要至少2个数据点）
        if (data.length < 2) {
          continue
        } else {
          const mean = safeDecimalMean(data, a) // 计算平均值
          // 计算每个数据点与平均值的差的平方
          const squaredDeviations = map(data, d => {
            const value = new Decimal(d[a] ?? 0) // 获取字段值，如果不存在则使用0
            const dev = value.minus(mean) // 计算差值
            return dev.times(dev) // 计算平方
          })
          // 计算方差（平方差的平均值）
          const variance = squaredDeviations.reduce((acc, val) => acc.plus(val), new Decimal(0))
          summaryObj[a] = variance.dividedBy(data.length - 1).toNumber() // 计算总体方差
        }
        break
      case 'stddev_pop':
        // 计算总体标准差（需要至少2个数据点）
        if (data.length < 2) {
          continue
        } else {
          const mean = safeDecimalMean(data, a) // 计算平均值
          // 计算每个数据点与平均值的差的平方
          const squaredDeviations = map(data, d => {
            const value = new Decimal(d[a] ?? 0) // 获取字段值，如果不存在则使用0
            const dev = value.minus(mean) // 计算差值
            return dev.times(dev) // 计算平方
          })
          // 计算方差（平方差的平均值）
          const variance = squaredDeviations.reduce((acc, val) => acc.plus(val), new Decimal(0))
          summaryObj[a] = variance.dividedBy(data.length - 1).sqrt().toNumber() // 计算总体标准差
        }
        break
    }
  }

  // 返回汇总结果对象
  return summaryObj
}


export class SummaryCell extends CustomDataCell {
  getTextStyle() {
    const textStyle = cloneDeep(this.theme.colCell.bolderText)
    textStyle.textAlign = this.theme.dataCell.text.textAlign
    return textStyle
  }

  getBackgroundColor() {
    const { backgroundColor, backgroundColorOpacity } = this.theme.colCell.cell
    return { backgroundColor, backgroundColorOpacity }
  }
}

/**
 * 配置空数据样式
 * @param newChart
 * @param basicStyle
 * @param newData
 * @param container
 */
export const configEmptyDataStyle = (newChart, basicStyle, newData, container) => {
  /**
   * 辅助函数：移除空数据dom
   */
  const removeEmptyDom = () => {
    const emptyElement = document.getElementById(container + '_empty')
    if (emptyElement) {
      emptyElement.parentElement.removeChild(emptyElement)
    }
  }
  removeEmptyDom()
  if (newData.length) return
  newChart.on(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, ev => {
    removeEmptyDom()
    if (!newData.length) {
      const emptyDom = document.createElement('div')
      const left = Math.min(newChart.options.width, ev.colsHierarchy.width) / 2 - 32
      emptyDom.id = container + '_empty'
      emptyDom.textContent = i18nt('data_set.no_data')
      emptyDom.setAttribute(
        'style',
        `position: absolute;
        left: ${left}px;
        top: 50%;`
      )
      const parent = document.getElementById(container)
      parent.insertBefore(emptyDom, parent.firstChild)
    }
  })
}

export const getLeafNodes = (tree: Array<ColumnNode>): ColumnNode[] => {
  const result: ColumnNode[] = []
  const inorderTraversal = node => {
    if (!node.children?.length) {
      // 叶子节点，添加到结果数组
      result.push(node)
      return
    }
    // 中序遍历
    for (let i = 0; i < node.children?.length; i++) {
      inorderTraversal(node.children[i])
    }
  }

  // 遍历树中所有节点
  tree.forEach(node => inorderTraversal(node))
  return result
}

export const getColumns = (fields, cols: Array<ColumnNode>) => {
  const result = []
  for (let i = 0; i < cols.length; i++) {
    if (fields.includes(cols[i].key)) {
      result.push(cols[i])
    }
    if (cols[i].children?.length) {
      result.push(...getColumns(fields, cols[i].children as Array<ColumnNode>))
    }
  }
  return result
}

export function drawImage() {
  const img = new Image()
  const { x, y, width, height, fieldValue } = this.meta
  img.src = fieldValue as string
  img.setAttribute('crossOrigin', 'anonymous')
  img.onload = () => {
    !this.cfg.children && (this.cfg.children = [])
    const { width: imgWidth, height: imgHeight } = img
    const ratio = Math.max(imgWidth / width, imgHeight / height)
    // 不铺满，部分留白
    const imgShowWidth = (imgWidth / ratio) * 0.8
    const imgShowHeight = (imgHeight / ratio) * 0.8
    this.textShape = this.addShape('image', {
      attrs: {
        x: x + (imgShowWidth < width ? (width - imgShowWidth) / 2 : 0),
        y: y + (imgShowHeight < height ? (height - imgShowHeight) / 2 : 0),
        width: imgShowWidth,
        height: imgShowHeight,
        img
      }
    })
  }
}


export function calcTreeWidth(node) {
  if (!node.children?.length) {
    return node.width
  }
  return node.children.reduce((pre, cur) => {
    return pre + calcTreeWidth(cur)
  }, 0)
}

export function getStartPosition(node) {
  if (!node.children?.length) {
    return node.x
  }
  return getStartPosition(node.children[0])
}

function getMaxTreeDepth(nodes) {
  if (!nodes?.length) {
    return 0
  }
  return Math.max(
    ...nodes.map(node => {
      if (!node.children?.length) {
        return 1
      }
      return getMaxTreeDepth(node.children) + 1
    })
  )
}

export function summaryRowStyle(newChart, newData, tableCell, tableHeader, showSummary) {
  if (!showSummary || !newData.length) return
  const columns = newChart.dataCfg.fields.columns
  const showHeader = tableHeader.showTableHeader === true
  // 不显示表头时，减少一个表头的高度
  const headerAndSummaryHeight = showHeader ? getMaxTreeDepth(columns) + 1 : 1
  newChart.on(S2Event.LAYOUT_BEFORE_RENDER, () => {
    const totalHeight =
      tableHeader.tableTitleHeight * headerAndSummaryHeight +
      tableCell.tableItemHeight * (newData.length - 1)
    if (totalHeight < newChart.container.cfg.height) {
      newChart.options.height =
        totalHeight < newChart.container.cfg.height - 8 ? totalHeight + 8 : totalHeight
    }
  })
}