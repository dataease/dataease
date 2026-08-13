/* eslint-disable prettier/prettier */
import {
  copyString,
  hexColorToRGBA,
  isAlphaColor,
  isTransparent,
  parseJson,
  resetRgbOpacity
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
  EXTRA_FIELD,
  SERIES_NUMBER_FIELD,
  setTooltipContainerStyle,
  SHAPE_STYLE_MAP,
  SpreadSheet,
  S2Style,
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
import {createVNode, render} from 'vue'
import TableTooltip from '@/views/chart/components/editor/common/TableTooltip.vue'
import Exceljs from 'exceljs'
import {saveAs} from 'file-saver'
import {ElMessage} from 'element-plus-secondary'
import {useI18n} from '@/hooks/web/useI18n'
import { Image as GImage } from '@antv/g'

const {t: i18nt} = useI18n()

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
    const {basicStyle, tableHeader, tableCell} = customAttr
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
      const {tableHeaderAlign, tableTitleFontSize} = tableHeader
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
      const {tableItemAlign, tableItemFontSize, enableTableCrossBG} = tableCell
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

export function getStyle(chart: Chart, dataConfig: S2DataConfig): S2Style {
  const style: S2Style = {}
  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    const {basicStyle, tableHeader, tableCell} = customAttr
    style.colCell = {
      height: tableHeader.tableTitleHeight
    }
    style.dataCell = {
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
          const {xAxis} = parseJson(chart)
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
        let fieldCount = dataConfig.meta.length
        let totalWidthPercent = dataConfig.meta?.reduce((p, n) => {
          return p + (fieldMap[n.field]?.width ?? 10)
        }, 0)
        if (customAttr.tableHeader.showIndex) {
          const indexWidth = fieldMap[SERIES_NUMBER_FIELD]?.width ?? 10
          totalWidthPercent += indexWidth
          fieldCount += 1
        }
        const fullFilled = parseInt(totalWidthPercent.toFixed(0)) === 100
        const widthArr = []
        style.colCell.width = node => {
          const width = node.spreadsheet.container.context.config.container.offsetWidth
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
            if (widthArr.length === fieldCount - 1) {
              const curTotalWidth = widthArr.reduce((p, n) => {
                return p + n
              }, 0)
              const restWidth = width - curTotalWidth
              widthArr.splice(0)
              if (restWidth < resultWidth) {
                return restWidth
              }
              if (restWidth === resultWidth) {
                return restWidth - 1
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
        style.colCell.width = basicStyle.tableColumnWidth
        break
      }
      case 'colAdapt': {
        style.layoutWidthType = 'colAdaptive'
        const parentNodeWidthMap = {}
        const nodeMaxWidthMap = {}
        const quotaLabelMap = chart.yAxis?.reduce((p, n) => {
          p[n.dataeaseName] = n.chartShowName || n.name
          return p
        }, {}) || {}
        let calcCount = 50
        // 透视表列自适应仅按最后两层列头内容计算宽度，最多采样 50 个节点
        style.colCell.width = node => {
          const spreadsheet = node.spreadsheet
          const colHeaderTheme = spreadsheet.theme.colCell.bolderText
          const padding = spreadsheet.theme.colCell.cell.padding
          const paddingWidth = (padding?.left || 8) + (padding?.right || 8) + 12
          // 小计、总计和第一层表头直接按文本宽度计算
          if (node.isTotals || node.parent.id === 'root') {
            let label = node.value
            if (node.field === EXTRA_FIELD) {
              label = quotaLabelMap[node.value] || label
            }
            return spreadsheet.measureTextWidth(label, colHeaderTheme) + paddingWidth
          }

          const parentWidth = parentNodeWidthMap[node.parent.id]
          if (!parentWidth || calcCount < 50) {
            const parentLabel = node.parent.value
            const parentTextWidth =
              spreadsheet.measureTextWidth(parentLabel, colHeaderTheme) + paddingWidth
            parentNodeWidthMap[node.parent.id] = parentTextWidth
            const siblingsTextWidthMap = {}
            const siblingsWidth = node.parent.children.reduce((p, sibling) => {
              let label = sibling.value
              if (sibling.field === EXTRA_FIELD) {
                label = quotaLabelMap[sibling.value] || label
              }
              const pureTextWidth = spreadsheet.measureTextWidth(label, colHeaderTheme)
              if (sibling.field === EXTRA_FIELD) {
                siblingsTextWidthMap[sibling.value] = pureTextWidth
              }
              calcCount++
              return p + pureTextWidth + paddingWidth
            }, 0)
            const siblingFields = Object.keys(siblingsTextWidthMap)
            const expandOffsetWidth =
              siblingFields.length && siblingsWidth < parentTextWidth
                ? (parentTextWidth - siblingsWidth) / siblingFields.length
                : 0
            siblingFields.forEach(field => {
              const width =
                siblingsTextWidthMap[field] + Math.ceil(expandOffsetWidth) + paddingWidth
              nodeMaxWidthMap[field] = Math.max(nodeMaxWidthMap[field] || 0, width)
            })
            return nodeMaxWidthMap[node.value]
          }

          return (
            nodeMaxWidthMap[node.value] ||
            spreadsheet.measureTextWidth(node.value, colHeaderTheme) + paddingWidth
          )
        }
        break
      }
      // 查看详情用，均分铺满
      default: {
        delete style.layoutWidthType
        style.colCell.width = node => {
          const width = node.spreadsheet.container.context.config.container.offsetWidth
          const fieldsSize = node.spreadsheet.dataCfg.meta.length
          if (!fieldsSize) {
            return 0
          }
          const columnCount = tableHeader.showIndex ? fieldsSize + 1 : fieldsSize
          const minWidth = Math.floor(width / columnCount) - 1
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

export function getConditions(
  chart: Chart,
  displayFieldNames?: string[],
  drillFieldMap: Record<string, string> = {}
) {
  const {threshold} = parseJson(chart.senior)
  if (!threshold.enable) {
    return
  }
  const res = {
    text: [],
    background: []
  }
  const conditions = getEffectiveTableConditions(threshold.tableThreshold ?? [])
  const allFields = chart.type === 'table-normal' ? [...chart.xAxis, ...chart.yAxis] : [...chart.xAxis]
  const fieldIdToName = allFields.reduce((acc, field) => {
    acc[field.id] = field.dataeaseName
    return acc
  }, {})
  const allColumnNames = displayFieldNames?.length
    ? displayFieldNames
    : allFields.map(field => field.dataeaseName)
  if (conditions?.length > 0) {
    const {tableCell, basicStyle, tableHeader} = parseJson(chart.customAttr)
    // 合并单元格时斑马纹失效
    const enableTableCrossBG = chart.type === 'table-info' ? tableCell.enableTableCrossBG && !tableCell.mergeCells : tableCell.enableTableCrossBG
    const valueColor = isAlphaColor(tableCell.tableFontColor)
      ? tableCell.tableFontColor
      : hexColorToRGBA(tableCell.tableFontColor, basicStyle.alpha)
    const valueBgColor = enableTableCrossBG
      ? null
      : isAlphaColor(tableCell.tableItemBgColor)
        ? tableCell.tableItemBgColor
        : hexColorToRGBA(tableCell.tableItemBgColor, basicStyle.alpha)
    const filedValueMap = getFieldValueMap(chart)
    const targetRulesMap = {}

    for (let i = 0; i < conditions.length; i++) {
      const fieldItem = conditions[i]
      if (!fieldItem.conditions) continue

      for (let j = 0; j < fieldItem.conditions.length; j++) {
        const rule = fieldItem.conditions[j]
        let targets = []
        if (rule.target === 'total_row') {
          targets = [...allColumnNames]
          if (tableHeader.showIndex) {
            targets.push(SERIES_NUMBER_FIELD)
          }
        } else if (rule.target === 'custom' && rule.targetFieldId) {
          const targetName = resolveDisplayFieldName(
            fieldIdToName[rule.targetFieldId],
            drillFieldMap
          )
          if (targetName) targets = [targetName]
        } else {
          // 兼容历史配置，缺少 target 时仍作用于当前字段
          targets = [resolveDisplayFieldName(fieldItem.field.dataeaseName, drillFieldMap)]
        }

        new Set(targets).forEach(targetName => {
          if (!targetRulesMap[targetName]) {
            targetRulesMap[targetName] = []
          }
          targetRulesMap[targetName].push({
            rule,
            sourceField: fieldItem.field,
            fieldIndex: i,
            conditionIndex: j
          })
        })
      }
    }

    for (const targetName in targetRulesMap) {
      const rules = sortTableTargetRules(targetRulesMap[targetName])
      res.text.push({
        field: targetName,
        mapping(value, rowData) {
          if (value === undefined && !rowData) {
            return null
          }
          return {
            fill: mappingRulesColor(
              value,
              valueColor,
              rules,
              'color',
              filedValueMap,
              rowData,
              targetName
            )
          }
        }
      })
      res.background.push({
        field: targetName,
        mapping(value, rowData) {
          if (value === undefined && !rowData) {
            return null
          }
          const fill = mappingRulesColor(
            value,
            valueBgColor,
            rules,
            'backgroundColor',
            filedValueMap,
            rowData,
            targetName
          )
          if (isTransparent(fill)) {
            return null
          }
          return {fill}
        }
      })
    }
  }
  return res
}

export function getPivotConditions(chart: Chart) {
  const {threshold} = parseJson(chart.senior)
  if (!threshold.enable) {
    return
  }
  const res = {
    text: [],
    background: []
  }
  const conditions = getEffectiveTableConditions(threshold.tableThreshold ?? [])
  if (!conditions.length) {
    return res
  }

  const allFields = [...chart.xAxis, ...chart.xAxisExt, ...chart.yAxis]
  const fieldIdToName = allFields.reduce((acc, field) => {
    acc[field.id] = field.dataeaseName
    return acc
  }, {})
  const xFields = chart.xAxis.map(field => field.dataeaseName)
  const xExtFields = chart.xAxisExt.map(field => field.dataeaseName)
  const yFields = chart.yAxis.map(field => field.dataeaseName)
  const {tableCell, basicStyle, tableHeader} = parseJson(chart.customAttr)
  const valueColor = getTableConditionColor(tableCell.tableFontColor, basicStyle.alpha)
  const valueBgColor = tableCell.enableTableCrossBG
    ? null
    : getTableConditionColor(tableCell.tableItemBgColor, basicStyle.alpha)
  const colHeaderValueColor = getTableConditionColor(
    tableHeader.tableHeaderFontColor,
    basicStyle.alpha
  )
  const colHeaderBgColor = getTableConditionColor(
    tableHeader.tableHeaderBgColor,
    basicStyle.alpha
  )
  const rowHeaderValueColor = getTableConditionColor(
    tableHeader.tableHeaderColFontColor,
    basicStyle.alpha
  )
  const rowHeaderBgColor = getTableConditionColor(
    tableHeader.tableHeaderColBgColor,
    basicStyle.alpha
  )
  const filedValueMap = getFieldValueMap(chart)
  const targetRulesMap = {}

  for (let i = 0; i < conditions.length; i++) {
    const fieldItem = conditions[i]
    if (!fieldItem.conditions) continue

    for (let j = 0; j < fieldItem.conditions.length; j++) {
      const rule = fieldItem.conditions[j]
      let targets = []
      if (rule.target === 'total_row') {
        if (xFields.includes(fieldItem.field.dataeaseName)) {
          targets.push(...xFields)
          if (basicStyle.quotaPosition === 'row') targets.push(EXTRA_FIELD)
        }
        if (xExtFields.includes(fieldItem.field.dataeaseName)) {
          targets.push(...xExtFields)
          if (basicStyle.quotaPosition !== 'row') targets.push(EXTRA_FIELD)
        }
        targets.push(...yFields)
      } else if (rule.target === 'custom' && rule.targetFieldId) {
        const targetName = fieldIdToName[rule.targetFieldId]
        if (targetName) targets = [targetName]
      } else {
        targets = [fieldItem.field.dataeaseName]
      }

      new Set(targets).forEach(targetName => {
        if (!targetRulesMap[targetName]) {
          targetRulesMap[targetName] = []
        }
        targetRulesMap[targetName].push({
          rule,
          sourceField: fieldItem.field,
          fieldIndex: i,
          conditionIndex: j
        })
      })
    }
  }

  for (const targetName in targetRulesMap) {
    const rules = sortTableTargetRules(targetRulesMap[targetName])
    let defaultValueColor = valueColor
    let defaultBgColor = valueBgColor
    if (xFields.includes(targetName)) {
      defaultValueColor = rowHeaderValueColor
      defaultBgColor = rowHeaderBgColor
    } else if (xExtFields.includes(targetName)) {
      defaultValueColor = colHeaderValueColor
      defaultBgColor = colHeaderBgColor
    }

    res.text.push({
      field: targetName,
      mapping(value, rowData) {
        if (rowData?.cornerType) return null
        return {
          fill: mappingRulesColor(
            value,
            defaultValueColor,
            rules,
            'color',
            filedValueMap,
            rowData,
            targetName,
            true
          )
        }
      }
    })
    res.background.push({
      field: targetName,
      mapping(value, rowData) {
        if (rowData?.cornerType) return null
        const fill = mappingRulesColor(
          value,
          defaultBgColor,
          rules,
          'backgroundColor',
          filedValueMap,
          rowData,
          targetName,
          true
        )
        if (isTransparent(fill)) return null
        return {fill}
      }
    })
  }
  return res
}

function getTableConditionColor(color, alpha) {
  return isAlphaColor(color) ? color : hexColorToRGBA(color, alpha)
}

function getEffectiveTableConditions(conditions: TableThreshold[]) {
  const lastFieldIndex = new Map()
  conditions.forEach((item, index) => {
    lastFieldIndex.set(item.field?.dataeaseName, index)
  })
  // S2 同一字段重复配置时最后一组生效，保持存量规则优先级
  return conditions.filter((item, index) => lastFieldIndex.get(item.field?.dataeaseName) === index)
}

function sortTableTargetRules(rules) {
  // 同一字段内和字段组之间都按后配置的样式优先
  return [...rules].sort((a, b) => {
    if (a.fieldIndex === b.fieldIndex) {
      return b.conditionIndex - a.conditionIndex
    }
    return b.fieldIndex - a.fieldIndex
  })
}

function resolveDisplayFieldName(fieldName: string, drillFieldMap: Record<string, string>) {
  if (!fieldName) return fieldName
  return Object.keys(drillFieldMap).find(name => drillFieldMap[name] === fieldName) ?? fieldName
}

function mappingRulesColor(
  value,
  defaultColor,
  rules,
  type,
  filedValueMap,
  rowData,
  targetName,
  pivot = false
) {
  for (let i = 0; i < rules.length; i++) {
    const {rule, sourceField} = rules[i]
    if (
      pivot &&
      (rowData?.isTotals ||
        rowData?.isGrandTotals ||
        rowData?.isSubTotals ||
        rowData?.field === EXTRA_FIELD) &&
      rule.target !== 'total_row'
    ) {
      continue
    }

    const sourceValue = getRuleSourceValue(value, rowData, sourceField.dataeaseName, targetName)
    if (!sourceValue.found) continue
    if (matchTableCondition(sourceValue.value, rule, sourceField, filedValueMap, rowData)) {
      return rule[type]
    }
  }
  return defaultColor
}

function getRuleSourceValue(value, rowData, sourceName, targetName) {
  if (rowData && Object.prototype.hasOwnProperty.call(rowData, sourceName)) {
    return {found: true, value: rowData[sourceName]}
  }
  if (rowData?.query && Object.prototype.hasOwnProperty.call(rowData.query, sourceName)) {
    return {found: true, value: rowData.query[sourceName]}
  }
  if (sourceName === targetName) {
    return {found: true, value}
  }
  return {found: false, value: undefined}
}

function matchTableCondition(value, rule, sourceField, filedValueMap, rowData) {
  const empty = value === null || value === undefined || value === ''
  if (rule.term === 'null') return empty
  if (rule.term === 'not_null') return !empty
  if (rule.term === 'default') return true

  let targetValue
  let min
  let max
  if (rule.type === 'dynamic') {
    if (rule.term === 'between') {
      min = parseFloat(getValue(rule.dynamicMinField, filedValueMap, rowData))
      max = parseFloat(getValue(rule.dynamicMaxField, filedValueMap, rowData))
    } else {
      targetValue = getValue(rule.dynamicField, filedValueMap, rowData)
    }
  } else if (rule.term === 'between') {
    min = parseFloat(rule.min)
    max = parseFloat(rule.max)
  } else {
    targetValue = rule.value
  }

  if ([2, 3, 4].includes(sourceField.deType)) {
    const current = parseFloat(value)
    const target = parseFloat(targetValue)
    if (rule.term === 'between') return !empty && min <= current && current <= max
    if (rule.term === 'eq') return current === target
    if (rule.term === 'not_eq') return current !== target
    if (rule.term === 'lt') return current < target
    if (rule.term === 'gt') return current > target
    if (rule.term === 'le') return !empty && current <= target
    if (rule.term === 'ge') return !empty && current >= target
    return false
  }

  if ([0, 5].includes(sourceField.deType)) {
    if (rule.term === 'eq') return value === targetValue
    if (rule.term === 'not_eq') return value !== targetValue
    if (rule.term === 'like') return !empty && String(value).includes(String(targetValue))
    if (rule.term === 'not like') return !empty && !String(value).includes(String(targetValue))
    return false
  }

  if (empty || targetValue === null || targetValue === undefined || targetValue === '') {
    return false
  }
  const isSpecialTimeFormat =
    sourceField.dateStyle === 'H_m_s' ||
    (sourceField.dateStyle && sourceField.dateStyle.length > 5 && sourceField.dateStyle.length < 11)
  const current = isSpecialTimeFormat
    ? String(value)
    : new Date(String(value).replace(/-/g, '/') + ' GMT+8').getTime()
  const target = isSpecialTimeFormat
    ? String(targetValue)
    : new Date(String(targetValue).replace(/-/g, '/') + ' GMT+8').getTime()
  if (rule.term === 'eq') return current === target
  if (rule.term === 'not_eq') return current !== target
  if (rule.term === 'lt') return current < target
  if (rule.term === 'gt') return current > target
  if (rule.term === 'le') return current <= target
  if (rule.term === 'ge') return current >= target
  return false
}

export function mappingColor(value, defaultColor, field, type, filedValueMap?, rowData?) {
  let color = null
  let hitCondition = null
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
        hitCondition = t
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
        hitCondition = t
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else {
      // time
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
        hitCondition = fc
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    }
  }
  if (hitCondition && hitCondition.target === 'custom') {
    return {
      targetFieldId: hitCondition.targetFieldId,
      color
    }
  } else {
    return {
      targetFieldId: field.fieldId,
      color
    }
  }

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
    const {iconName} = showOptions
    if (iconName) {
      this.showSortTooltip(showOptions)
      return
    }
    super.show(showOptions)
  }

  showSortTooltip(showOptions) {
    const {position, meta, event} = showOptions
    const {autoAdjustBoundary, adjustPosition} = this.spreadsheet.options.tooltip || {}
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

    const {x, y} = getAutoAdjustPosition({
      spreadsheet: this.spreadsheet,
      position,
      tooltipContainer: container,
      autoAdjustBoundary
    })

    this.position = adjustPosition?.({position: {x, y}, event}) ?? {
      x,
      y
    }

    setTooltipContainerStyle(container, {
      style: {
        left: `${this.position?.x}px`,
        top: `${this.position?.y}px`,
        pointerEvents: 'all',
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
  const {tableHeaderFontColor, tableHeaderSort} = parseJson(chart.customAttr).tableHeader
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
      src: sortDefault
    },
    {
      name: `customSortUp${randomSuffix}`,
      src: sortUp
    },
    {
      name: `customSortDown${randomSuffix}`,
      src: sortDown
    }
  ]
  option.headerActionIcons = [
    {
      icons: [
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
        const {meta, event} = props
        meta.spreadsheet.showTooltip({
          position: {
            x: event.clientX,
            y: event.clientY
          },
          event,
          iconName: props.name,
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
  const {tooltip} = parseJson(chart.customAttr)
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
    adjustPosition: ({event}) => {
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
  if (selectState.stateName === InteractionStateName.DATA_CELL_BRUSH_SELECTED) {
    const {cells} = selectState
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
      InteractionName.DATA_CELL_BRUSH_SELECTION
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
          const value = cellMeta.data?.[cellMeta.valueField] || cellMeta.data?.raw?.[cellMeta.valueField]
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
  const {x, y} = event
  const result = {x: x + 15, y}
  if (!s2Instance) {
    return result
  }
  const {height, width} = s2Instance.getCanvasElement().getBoundingClientRect()
  const {offsetHeight, offsetWidth} = s2Instance.tooltip.getContainer()
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
  const layoutResult = instance.facet.getLayoutResult()
  const {meta, fields} = instance.dataCfg
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
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (rowLength >= 2) {
      worksheet.mergeCells(index + 1, 1, index + 1, rowLength)
    }
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
    }
  })
  fields?.rows?.forEach((row, index) => {
    const cell = worksheet.getCell(colLength + 1, index + 1)
    cell.value = metaMap[row]?.name ?? row
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    cell.border = {
      bottom: {style: 'thick', color: {argb: '00000000'}}
    }
    if (index === fields.rows.length - 1) {
      cell.border.right = {style: 'thick', color: {argb: '00000000'}}
    }
  })
  // 行头
  const {rowLeafNodes, rowsHierarchy, rowNodes} = layoutResult
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
    const {rowIndex} = node
    const writeRowIndex = rowIndex + 1 + colLength + 1
    const writeColIndex = node.level + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = node.value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (writeColIndex < maxColIndex) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, writeRowIndex, maxColIndex)
    }
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
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
    const value = node.value
    const cell = worksheet.getCell(writeRowIndex, node.level + 1)
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
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
  const {colLeafNodes, colNodes, colsHierarchy} = layoutResult
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
    const {colIndex} = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 1 + rowLength
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    let value = node.value
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
    cell.border = {
      bottom: {style: 'thick', color: {argb: '00000000'}}
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
    const value = node.value
    const writeColIndex = colIndex + rowLength
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (mergeRowCount > 1 || width > 1) {
      worksheet.mergeCells(
        writeRowIndex,
        writeColIndex,
        writeRowIndex + mergeRowCount - 1,
        writeColIndex + width - 1
      )
    }
  })
    const formatterMap = chart.yAxis.reduce((p, n) => {
    if (n.dataeaseName) {
      p[n.dataeaseName] = n.formatterCfg
    }
    return p
  }, {})
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = instance.facet.getCellMeta(rowIndex, colIndex)
      const {fieldValue} = dataCellMeta
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, rowLength + colIndex + 1)
        const value = meta?.formatter?.(fieldValue) || fieldValue.toString()
        cell.alignment = {vertical: 'middle', horizontal: 'center'}
        if (typeof value === 'number') {
          cell.value = value
        } else if (typeof value === 'string') {
          const formatterCfg = formatterMap?.[dataCellMeta.valueField]
          const result = extractNumber(value, formatterCfg)
          if (typeof result === 'string') {
            cell.value = result
          } else {
            cell.value = result.value
            cell.numFmt = result.numFmt
          }
        }
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
  const layoutResult = instance.facet.getLayoutResult()
  const {meta, fields} = instance.dataCfg
  const rowLength = fields?.rows?.length || 0
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
  const colHead = fields.columns?.[0]
  if (colHead) {
    const cell = worksheet.getCell(1, 1)
    cell.value = metaMap[colHead]?.name ?? colHead
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
    }
    worksheet.mergeCells(1, 1, 1, rowLength + 1)
  }
  fields?.rows?.forEach((row, index) => {
    const cell = worksheet.getCell(2, index + 1)
    cell.value = metaMap[row]?.name ?? row
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    cell.border = {bottom : {style: 'thick', color: {argb: '00000000'}}}
  })
  const quotaColLabel = chart.customAttr.basicStyle.quotaColLabel ?? t('dataset.value')
  const quotaColHeadCell = worksheet.getCell(2, rowLength + 1)
  quotaColHeadCell.value = quotaColLabel
  quotaColHeadCell.alignment = {vertical: 'middle', horizontal: 'center'}
  quotaColHeadCell.border = {
    bottom: {style: 'thick', color: {argb: '00000000'}},
    right: {style: 'thick', color: {argb: '00000000'}}
  }
  // 行头
  const {rowLeafNodes, rowsHierarchy, rowNodes} = layoutResult
  const notLeafNodeHeightMap: Record<string, number> = {}
  rowLeafNodes.forEach(node => {
    // 行头的高度由子节点相加决定，也就是行头子节点中包含的叶子节点数量
    let curNode = node.parent
    while (curNode) {
      const height = notLeafNodeHeightMap[curNode.id] ?? 0
      notLeafNodeHeightMap[curNode.id] = height + 1
      curNode = curNode.parent
    }
    const {rowIndex} = node
    const writeRowIndex = rowIndex + 3
    const writeColIndex = node.level + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    let value = node.value
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
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
    const writeRowIndex = rowIndex + 2
    const mergeColCount = node.children[0].level - node.level
    const cell = worksheet.getCell(writeRowIndex, node.level + 1)
    cell.value = node.value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
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
  const {colLeafNodes, colNodes, colsHierarchy} = layoutResult
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
    const {colIndex} = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + rowLength + 2
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    const value = node.value
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
    cell.border = {
      bottom: {style: 'thick', color: {argb: '00000000'}}
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
    const value = node.value
    const writeColIndex = colIndex + rowLength + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (width > 1) {
      worksheet.mergeCells(
        writeRowIndex,
        writeColIndex,
        writeRowIndex,
        writeColIndex + width - 1
      )
    }
  })
    const formatterMap = chart.yAxis.reduce((p, n) => {
    if (n.dataeaseName) {
      p[n.dataeaseName] = n.formatterCfg
    }
    return p
  }, {})
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = instance.facet.getCellMeta(rowIndex, colIndex)
      const {fieldValue} = dataCellMeta
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, rowLength + colIndex + 2)
        const value = meta?.formatter?.(fieldValue) || fieldValue.toString()
        cell.alignment = {vertical: 'middle', horizontal: 'center'}
        if (typeof value === 'number') {
          cell.value = value
        } else if (typeof value === 'string') {
          const formatterCfg = formatterMap?.[dataCellMeta.valueField]
          const result = extractNumber(value, formatterCfg)
          if (typeof result === 'string') {
            cell.value = result
          } else {
            cell.value = result.value
            cell.numFmt = result.numFmt
          }
        }
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
  const layoutResult = instance.facet.getLayoutResult()
  if (layoutResult.colLeafNodes.length + 1 > 16384) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_col_exceed'))
    return
  }
  const {meta, fields} = instance.dataCfg
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
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
    }
  })
  const maxColHeight = layoutResult.colsHierarchy.maxLevel + 1
  const rowName = fields?.rows?.map(row => metaMap[row]?.name ?? row).join('/')
  const cell = worksheet.getCell(colLength + 1, 1)
  cell.value = rowName
  cell.alignment = {vertical: 'middle', horizontal: 'center'}
  cell.border = {
    right: {style: 'thick', color: {argb: '00000000'}},
    bottom: {style: 'thick', color: {argb: '00000000'}}
  }
  //行头
  const {rowLeafNodes} = layoutResult
  rowLeafNodes.forEach((node, index) => {
    const cell = worksheet.getCell(maxColHeight + index + 1, 1)
    cell.value = repeat('  ', node.level) + node.value
    cell.alignment = {vertical: 'middle', horizontal: 'left'}
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
    }
  })
  // 列头
  const notLeafNodeWidthMap: Record<string, number> = {}
  const {colLeafNodes} = layoutResult
  colLeafNodes.forEach(node => {
    let curNode = node.parent
    while (curNode) {
      const width = notLeafNodeWidthMap[curNode.id] ?? 0
      notLeafNodeWidthMap[curNode.id] = width + 1
      curNode = curNode.parent
    }
    const {colIndex} = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 1 + 1
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    let value = node.value
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
    cell.border = {
      bottom: {style: 'thick', color: {argb: '00000000'}}
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
    cell.value = node.value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (mergeRowCount > 1 || width > 1) {
      worksheet.mergeCells(
        writeRowIndex,
        writeColIndex,
        writeRowIndex + mergeRowCount - 1,
        writeColIndex + width - 1
      )
    }
  })
    const formatterMap = chart.yAxis.reduce((p, n) => {
    if (n.dataeaseName) {
      p[n.dataeaseName] = n.formatterCfg
    }
    return p
  }, {})
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = instance.facet.getCellMeta(rowIndex, colIndex)
      const {fieldValue} = dataCellMeta
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, colIndex + 1 + 1)
        const value = meta?.formatter?.(fieldValue) || fieldValue.toString()
        cell.alignment = {vertical: 'middle', horizontal: 'center'}
        if (typeof value === 'number') {
          cell.value = value
        } else if (typeof value === 'string') {
          const formatterCfg = formatterMap?.[dataCellMeta.valueField]
          const result = extractNumber(value, formatterCfg)
          if (typeof result === 'string') {
            cell.value = result
          } else {
            cell.value = result.value
            cell.numFmt = result.numFmt
          }
        }
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
  const layoutResult = instance.facet.getLayoutResult()
  if (layoutResult.colLeafNodes.length + 1 > 16384) {
    ElMessage.warning(i18nt('chart.pivot_export_invalid_col_exceed'))
    return
  }
  const {meta, fields} = instance.dataCfg
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
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
    }
  })
  const quotaColLabel = chart.customAttr.basicStyle.quotaColLabel ?? t('dataset.value')
  const maxColHeight = layoutResult.colsHierarchy.maxLevel + 1
  const rowName = fields?.rows?.map(row => metaMap[row]?.name ?? row).concat(quotaColLabel).join('/')
  const cell = worksheet.getCell(colLength, 1)
  cell.value = rowName
  cell.alignment = {vertical: 'middle', horizontal: 'center'}
  cell.border = {
    right: {style: 'thick', color: {argb: '00000000'}},
    bottom: {style: 'thick', color: {argb: '00000000'}}
  }
  //行头
  const {rowLeafNodes} = layoutResult
  rowLeafNodes.forEach((node, index) => {
    const cell = worksheet.getCell(maxColHeight + index + 1, 1)
    let value = node.value
    if (node.field === '$$extra$$' && metaMap[value]?.name) {
      value = metaMap[value].name
    }
    cell.value = repeat('  ', node.level) + value
    cell.alignment = {vertical: 'middle', horizontal: 'left'}
    cell.border = {
      right: {style: 'thick', color: {argb: '00000000'}}
    }
  })
  // 列头
  const notLeafNodeWidthMap: Record<string, number> = {}
  const {colLeafNodes} = layoutResult
  colLeafNodes.forEach(node => {
    let curNode = node.parent
    while (curNode) {
      const width = notLeafNodeWidthMap[curNode.id] ?? 0
      notLeafNodeWidthMap[curNode.id] = width + 1
      curNode = curNode.parent
    }
    const {colIndex} = node
    const writeRowIndex = node.level + 1
    const writeColIndex = colIndex + 2
    const cell = worksheet.getCell(writeRowIndex, writeColIndex)
    cell.value = node.value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (writeRowIndex < maxColHeight) {
      worksheet.mergeCells(writeRowIndex, writeColIndex, maxColHeight, writeColIndex)
    }
    cell.border = {
      bottom: {style: 'thick', color: {argb: '00000000'}}
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
    cell.value = node.value
    cell.alignment = {vertical: 'middle', horizontal: 'center'}
    if (width > 1) {
      worksheet.mergeCells(
        writeRowIndex,
        writeColIndex,
        writeRowIndex,
        writeColIndex + width - 1
      )
    }
  })
    const formatterMap = chart.yAxis.reduce((p, n) => {
    if (n.dataeaseName) {
      p[n.dataeaseName] = n.formatterCfg
    }
    return p
  }, {})
  //  单元格数据
  for (let rowIndex = 0; rowIndex < rowLeafNodes.length; rowIndex++) {
    for (let colIndex = 0; colIndex < colLeafNodes.length; colIndex++) {
      const dataCellMeta = instance.facet.getCellMeta(rowIndex, colIndex)
      const {fieldValue} = dataCellMeta
      if (fieldValue === 0 || fieldValue) {
        const meta = metaMap[dataCellMeta.valueField]
        const cell = worksheet.getCell(rowIndex + maxColHeight + 1, colIndex + 2)
        const value = meta?.formatter?.(fieldValue) || fieldValue.toString()
        cell.alignment = {vertical: 'middle', horizontal: 'center'}
        if (typeof value === 'number') {
          cell.value = value
        } else if (typeof value === 'string') {
          const formatterCfg = formatterMap?.[dataCellMeta.valueField]
          const result = extractNumber(value, formatterCfg)
          if (typeof result === 'string') {
            cell.value = result
          } else {
            cell.value = result.value
            cell.numFmt = result.numFmt
          }
        }
      }
    }
  }
  const buffer = await workbook.xlsx.writeBuffer()
  const dataBlob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8'
  })
  saveAs(dataBlob, `${chart.title ?? '透视表'}.xlsx`)
}

function extractNumber(formattedValue: string, formatterCfg: BaseFormatter): {
  value: number
  numFmt: string
} | string {
  if (!formatterCfg) {
    return formattedValue
  }
  let result = formattedValue
  if (formatterCfg.type === 'percent') {
    result = result.slice(0, -1) // 去掉百分号
    if (formatterCfg.thousandSeparator) {
      result = result.replace(/,/g, '')
    }
    //科学计数法
    if (result.includes('e')) {
      const valueArr = result.match(/^[+-]?\d+(\.\d+)?(e[+-]?\d+)?/)
      if (!valueArr?.length) {
        return formattedValue
      }
      const valueStr = valueArr[0]
      const value = parseFloat(valueStr)
      let numFmt = '0.'
      const number = valueStr.split('e')[0]
      numFmt += '0'.repeat(number.slice(1).length)
      numFmt += 'E+0"%"'
      return {
        value,
        numFmt
      }
    }
    const value = parseFloat(result)
    let numFmt = '#'
    if (formatterCfg.thousandSeparator) {
      numFmt += ',#'
    }
    if (Math.abs(value) < 1) {
      numFmt = '0'
    }
    if (formatterCfg.decimalCount > 0) {
      numFmt += `.${'0'.repeat(formatterCfg.decimalCount)}`
    }
    numFmt += '"%"'
    return {
      value,
      numFmt
    }
  }
  if (formatterCfg.suffix) {
    const suffix = formatterCfg.suffix
    if (result.endsWith(suffix)) {
      result = result.slice(0, -suffix.length)
    }
  }
  if (formatterCfg.thousandSeparator) {
    result = result.replace(/,/g, '')
  }
  //科学计数法
  if (result.includes('e')) {
    const valueArr = result.match(/^[+-]?\d+(\.\d+)?(e[+-]?\d+)?/)
    if (!valueArr?.length) {
      return formattedValue
    }
    const valueStr = valueArr[0]
    const value = parseFloat(valueStr)
    let numFmt = '0.'
    const number = valueStr.split('e')[0]
    numFmt += '0'.repeat(number.slice(1).length)
    numFmt += 'E+0'
    const suffix = formattedValue.slice(valueStr.length)
    if (suffix) {
      numFmt += `"${suffix}"`
    }
    return {
      value,
      numFmt
    }
  }
  const valueArr = result.match(/^[+-]?\d+(\.\d+)?/)
  if (!valueArr?.length) {
    return formattedValue
  }
  const valueStr = valueArr[0]
  const value = parseFloat(valueStr)
  const unit = result.slice(valueStr.length)
  let numFmt = '#'
  if (formatterCfg.thousandSeparator) {
    numFmt += ',#'
  }
  if (Math.abs(value) < 1) {
    numFmt = '0'
  }
  if (formatterCfg.type === 'value') {
    if (formatterCfg.decimalCount > 0) {
      numFmt += `.${'0'.repeat(formatterCfg.decimalCount)}`
    }
  } else {
    if (valueStr.indexOf('.') > -1) {
      const decimalLength = valueStr.split('.')[1].length
      numFmt += `.${'0'.repeat(decimalLength)}`
    }
  }
  if (unit) {
    numFmt += `"${unit}"`
  }
  numFmt += `"${formatterCfg.suffix}"`
  return {
    value,
    numFmt
  }
}

export async function exportPivotExcel(instance: PivotSheet, chart: ChartObj) {
  const {fields} = instance.dataCfg
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
  const {mergeCells} = parseJson(chart.customAttr).tableCell
  const {showIndex} = parseJson(chart.customAttr).tableHeader
  if (mergeCells) {
    options.frozen.colCount = 0
    options.frozen.rowCount = 0
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
    const {backgroundColor: fill, backgroundColorOpacity: fillOpacity} = this.getBackgroundColor()
    const cellTheme = this.theme.dataCell.cell
    this.backgroundShape = renderPolygon(this, {
      points: allPoints,
      stroke: cellTheme.horizontalBorderColor,
      fill
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

}

export function getSummaryRow(data, axis, sumCon = []) {
  const summaryObj = { SUMMARY: true }
  for (let i = 0; i < axis.length; i++) {
    const a = axis[i].dataeaseName
    let savedAxis = find(sumCon, s => s.field === a)
    if (savedAxis) {
      if (savedAxis.summary == undefined) {
        savedAxis.summary = 'sum'
      }
      if (savedAxis.show == undefined) {
        savedAxis.show = true
      }
    } else {
      savedAxis = {
        field: a,
        summary: 'sum',
        show: true
      }
    }
    if (!savedAxis.show) {
      continue
    }
    switch (savedAxis.summary) {
      case 'sum':
        summaryObj[a] = sumBy(data, d => parseFloat(d[a]) || 0)
        break
      case 'avg':
        summaryObj[a] = meanBy(data, d => parseFloat(d[a]) || 0)
        break
      case 'max':
        summaryObj[a] = maxBy(
          filter(data, d => parseFloat(d[a]) !== undefined),
          d => parseFloat(d[a])
        )[a]
        break
      case 'min':
        summaryObj[a] = minBy(
          filter(data, d => parseFloat(d[a]) !== undefined),
          d => parseFloat(d[a])
        )[a]
        break
      case 'var_pop': //方差
        if (data.length < 2) {
          continue
        } else {
          const mean = meanBy(data, d => parseFloat(d[a]) || 0) // 计算均值
          const squaredDeviations = map(data, d => ((parseFloat(d[a]) || 0) - mean) ** 2) // 计算偏差平方
          summaryObj[a] = sum(squaredDeviations) / (size(data) - 1) // 样本方差（分母n-1）
        }
        break
      case 'stddev_pop': //标准差
        if (data.length < 2) {
          continue
        } else {
          const mean = meanBy(data, d => parseFloat(d[a]) || 0) // 计算均值
          const squaredDeviations = map(data, d => ((parseFloat(d[a]) || 0) - mean) ** 2) // 计算偏差平方
          const sampleVariance = sum(squaredDeviations) / (size(data) - 1) // 样本方差（分母n-1）
          summaryObj[a] = Math.sqrt(sampleVariance) // 样本标准差
        }
        break
    }
  }
  return summaryObj
}

/**
 * 汇总行样式,紧贴在单元格后面
 * @param newChart
 * @param newData
 * @param tableCell
 * @param tableHeader
 * @param showSummary
 */
export const summaryRowStyle = (newChart, newData, tableCell, tableHeader, showSummary) => {
  if (!showSummary || !newData.length) return
  newChart.on(S2Event.LAYOUT_BEFORE_RENDER, () => {
    const showHeader = tableHeader.showTableHeader === true
    // 不显示表头时，减少一个表头的高度
    const headerAndSummaryHeight = showHeader ? 2 : 1
    const totalHeight =
      tableHeader.tableTitleHeight * headerAndSummaryHeight +
      tableCell.tableItemHeight * (newData.length - 1)
    if (totalHeight < newChart.container.context.config.height) {
      newChart.options.height =
        totalHeight < newChart.container.context.config.height - 8 ? totalHeight + 8 : totalHeight
    }
  })
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

export class SummaryCell extends CustomDataCell {
  getTextStyle() {
    const textStyle = cloneDeep(this.theme.colCell.bolderText)
    textStyle.textAlign = this.theme.dataCell.text.textAlign
    return textStyle
  }

  getBackgroundColor() {
    const {backgroundColor, backgroundColorOpacity, } = this.theme.colCell.cell
    return {backgroundColor, backgroundColorOpacity, intelligentReverseTextColor: false}
  }
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


export const mapKeyToField = (nodes: Array<ColumnNode>) => {
  nodes.forEach(node => {
    if (node.key) {
      node.field = node.key
      delete node.key
    }
    if (node.children) {
      mapKeyToField(node.children as Array<ColumnNode>)
    }
  })
}

export const setupColumnTitle = (nodes: Array<ColumnNode>, nameMap: Record<string, string>) => {
  nodes.forEach(node => {
    if (nameMap[node.field]) {
      node.title = nameMap[node.field]
    }
    if (node.children) {
      setupColumnTitle(node.children as Array<ColumnNode>, nameMap)
    }
  })
}

export const getColumns = (fields, cols: Array<ColumnNode>): Array<ColumnNode> => {
  const result = []
  for (let i = 0; i < cols.length; i++) {
    if (fields.includes(cols[i].field)) {
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
  const {x, y, width, height, fieldValue} = this.meta
  img.src = fieldValue as string
  img.setAttribute('crossOrigin', 'anonymous')
  img.onload = () => {
    this.children?.length && this.removeChildren()
    const {width: imgWidth, height: imgHeight} = img
    const ratio = Math.max(imgWidth / width, imgHeight / height)
    // 不铺满，部分留白
    const imgShowWidth = (imgWidth / ratio) * 0.8
    const imgShowHeight = (imgHeight / ratio) * 0.8
    this.appendChild(new GImage({
      style: {
        x: x + (imgShowWidth < width ? (width - imgShowWidth) / 2 : 0),
        y: y + (imgShowHeight < height ? (height - imgShowHeight) / 2 : 0),
        width: imgShowWidth,
        height: imgShowHeight,
        src: img
      }
    }))
  }
}

export function mappingColorCustom(value, defaultColor, field, type, filedValueMap?, rowData?) {
  let color = null
  let hitCondition = null
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
        if (value !== null && value <= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'ge') {
        if (value !== null && value >= tv) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'between') {
        if (value !== null && min <= value && value <= max) {
          color = t[type]
          flag = true
        }
      } else if (t.term === 'default') {
        color = t[type]
        flag = true
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
        hitCondition = t
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
        hitCondition = t
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    } else {
      const fc = field.conditions[i]
      if (fc.term === 'null') {
        if (value === null && value === undefined && value === '') {
          color = fc[type]
          flag = true
        }
      } else if (fc.term === 'not_null') {
        if (value !== null && value !== undefined && value !== '') {
          color = fc[type]
          flag = true
        }
      }
      if (flag) {
        hitCondition = fc
        break
      }
      // time
      if (!tv || !value) {
        break
      }
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
        hitCondition = fc
        break
      } else if (i === field.conditions.length - 1) {
        color = defaultColor
      }
    }
  }
  if (hitCondition && hitCondition.target === 'custom') {
    return {
      targetFieldId: hitCondition.targetFieldId,
      color
    }
  } else {
    return {
      targetFieldId: field.fieldId,
      color
    }
  }
}
