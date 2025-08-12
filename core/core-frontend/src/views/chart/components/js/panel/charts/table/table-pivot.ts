import {
  EXTRA_FIELD,
  PivotSheet,
  S2Event,
  S2Options,
  TOTAL_VALUE,
  S2Theme,
  Totals,
  PivotDataSet,
  Query,
  VALUE_FIELD,
  QueryDataType,
  TotalStatus,
  Aggregation,
  S2DataConfig,
  MergedCell,
  LayoutResult
} from '@antv/s2'
import { formatterItem, valueFormatter } from '../../../formatter'
import { hexColorToRGBA, isAlphaColor, parseJson } from '../../../util'
import { S2ChartView, S2DrawOptions } from '../../types/impl/s2'
import { TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { isNumber, keys, maxBy, merge, minBy, some, isEmpty, get } from 'lodash-es'
import { copyContent, CustomDataCell } from '../../common/common_table'
import Decimal from 'decimal.js'
import { DEFAULT_TABLE_HEADER } from '@/views/chart/components/editor/util/chart'

type DataItem = Record<string, any>

const { t } = useI18n()

class CustomPivotDataset extends PivotDataSet {
  getTotalValue(query: Query, totalStatus?: TotalStatus) {
    const { options } = this.spreadsheet
    const effectiveStatus = some(totalStatus)
    const status = effectiveStatus ? totalStatus : this.getTotalStatus(query)
    const { aggregation, calcFunc } =
      getAggregationAndCalcFuncByQuery(status, options?.totals) || {}

    // 聚合方式从用户配置的 s2Options.totals 取, 在触发前端兜底计算汇总逻辑时, 如果没有汇总的配置, 默认按 [求和] 计算,避免排序失效.
    const defaultAggregation =
      isEmpty(options?.totals) && !this.spreadsheet.isHierarchyTreeType() ? Aggregation.SUM : ''
    const calcAction = calcActionByType[aggregation || defaultAggregation]

    // 前端计算汇总值
    if (calcAction || calcFunc) {
      const data = this.getMultiData(query, {
        queryType: QueryDataType.DetailOnly
      })
      let totalValue: number
      if (calcFunc) {
        totalValue = calcFunc(query, data, this.spreadsheet, status)
      } else if (calcAction) {
        totalValue = calcAction(data, VALUE_FIELD)
      }

      return {
        ...query,
        [VALUE_FIELD]: totalValue,
        [query[EXTRA_FIELD]]: totalValue
      }
    }
  }
}
/**
 * 透视表
 */
export class TablePivot extends S2ChartView<PivotSheet> {
  properties: EditorProperty[] = [
    'border-style',
    'background-overall-component',
    'basic-style-selector',
    'table-header-selector',
    'table-cell-selector',
    'table-total-selector',
    'title-selector',
    'tooltip-selector',
    'function-cfg',
    'threshold',
    'linkage',
    'jump-set'
  ]
  propertyInner = {
    ...TABLE_EDITOR_PROPERTY_INNER,
    'table-header-selector': [
      'tableHeaderBgColor',
      'tableTitleFontSize',
      'tableHeaderFontColor',
      'tableTitleHeight',
      'tableHeaderAlign',
      'showColTooltip',
      'showRowTooltip',
      'showHorizonBorder',
      'showVerticalBorder',
      'rowHeaderFreeze'
    ],
    'table-total-selector': ['row', 'col'],
    'basic-style-selector': [
      'tableColumnMode',
      'tableBorderColor',
      'tableScrollBarColor',
      'alpha',
      'tableLayoutMode',
      'showHoverStyle',
      'quotaPosition',
      'quotaColLabel'
    ]
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'yAxis', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.table_pivot_row')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.dimension')}`,
      type: 'd',
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  public drawChart(drawOption: S2DrawOptions<PivotSheet>): PivotSheet {
    const { container, chart, chartObj, action } = drawOption
    const containerDom = document.getElementById(container)

    const { xAxisExt: columnFields, xAxis: rowFields, yAxis: valueFields } = chart
    const [c, r, v] = [columnFields, rowFields, valueFields].map(arr =>
      arr.map(i => i.dataeaseName)
    )

    // fields
    const { fields, customCalc } = chart.data
    if (!fields || fields.length === 0) {
      if (chartObj) {
        chartObj.destroy()
      }
      return
    }

    const columns = []
    const meta = []

    const valueFieldMap: Record<string, Axis> = [
      ...chart.xAxis,
      ...chart.xAxisExt,
      ...chart.yAxis
    ].reduce((p, n) => {
      p[n.dataeaseName] = n
      return p
    }, {})
    fields.forEach(ele => {
      const f = valueFieldMap[ele.dataeaseName]
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.chartShowName ?? ele.name,
        formatter: value => {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (![2, 3, 4].includes(f.deType) || !isNumber(value)) {
            return value
          }
          if (f.formatterCfg) {
            return valueFormatter(value, f.formatterCfg)
          } else {
            return valueFormatter(value, formatterItem)
          }
        },
        id: ele.id
      })
    })

    // total config
    const { basicStyle, tooltip, tableTotal, tableHeader } = parseJson(chart.customAttr)
    if (!tableTotal.row.subTotalsDimensionsNew || tableTotal.row.subTotalsDimensions == undefined) {
      tableTotal.row.subTotalsDimensions = r
    }
    tableTotal.col.subTotalsDimensions = c

    // 解析合计、小计排序
    const sortParams = []
    let rowTotalSort = false
    if (
      tableTotal.row.totalSort &&
      tableTotal.row.totalSort !== 'none' &&
      c.length > 0 &&
      tableTotal.row.showGrandTotals &&
      v.indexOf(tableTotal.row.totalSortField) > -1
    ) {
      c.forEach(i => {
        const sort = {
          sortFieldId: i,
          sortMethod: tableTotal.row.totalSort.toUpperCase(),
          sortByMeasure: TOTAL_VALUE,
          query: {
            [EXTRA_FIELD]: tableTotal.row.totalSortField
          }
        }
        sortParams.push(sort)
      })
      rowTotalSort = true
    }
    let colTotalSort = false
    if (
      tableTotal.col.totalSort &&
      tableTotal.col.totalSort !== 'none' &&
      r.length > 0 &&
      tableTotal.col.showGrandTotals &&
      v.indexOf(tableTotal.col.totalSortField) > -1
    ) {
      r.forEach(i => {
        const sort = {
          sortFieldId: i,
          sortMethod: tableTotal.col.totalSort.toUpperCase(),
          sortByMeasure: TOTAL_VALUE,
          query: {
            [EXTRA_FIELD]: tableTotal.col.totalSortField
          }
        }
        sortParams.push(sort)
      })
      colTotalSort = true
    }
    //列维度为空，行排序按照指标列来排序，取第一个有排序设置的指标
    if (!columnFields?.length) {
      const sortField = valueFields?.find(v => !['none', 'custom_sort'].includes(v.sort))
      if (sortField) {
        const sort = {
          sortFieldId: r[0],
          sortMethod: sortField.sort.toUpperCase(),
          sortByMeasure: TOTAL_VALUE,
          query: {
            [EXTRA_FIELD]: sortField.dataeaseName
          }
        }
        sortParams.push(sort)
      }
    }
    // 自定义总计小计
    const totals = [
      tableTotal.row.calcTotals,
      tableTotal.row.calcSubTotals,
      tableTotal.col.calcTotals,
      tableTotal.col.calcSubTotals
    ]
    const axisMap = {
      row: chart.xAxis,
      col: chart.xAxisExt,
      quota: chart.yAxis
    }
    // 沒有列维度需要特殊处理
    if (!chart.xAxisExt?.length) {
      //树形模式下，列维度为空，行小计的配置会变成列总计
      if (basicStyle.tableLayoutMode === 'tree') {
        tableTotal.col.calcTotals = tableTotal.row.calcSubTotals
        if (!tableTotal.col.calcTotals.cfg?.length) {
          tableTotal.col.calcTotals.cfg = chart.yAxis.map(y => {
            return {
              dataeaseName: y.dataeaseName,
              aggregation: 'SUM'
            }
          })
        }
      } else {
        // 列总计设置为空
        tableTotal.col.calcTotals.calcFunc = () => '-'
      }
    }
    totals.forEach(total => {
      if (total.cfg?.length) {
        delete total.aggregation
        const totalCfgMap = total.cfg.reduce((p, n) => {
          p[n.dataeaseName] = n
          return p
        }, {})
        total.calcFunc = (query, data, _, status) => {
          return customCalcFunc(query, data, status, chart, totalCfgMap, axisMap, customCalc)
        }
      }
    })
    // 空值处理
    const newData = this.configEmptyDataStrategy(chart)
    // 行列维度排序
    if (!rowTotalSort) {
      c?.forEach((f, i) => {
        if (valueFieldMap[f]?.sort === 'none') {
          return
        }
        const sort = {
          sortFieldId: f
        }
        const sortMethod = valueFieldMap[f]?.sort?.toUpperCase()
        if (sortMethod === 'CUSTOM_SORT') {
          sort.sortBy = valueFieldMap[f].customSort
        } else {
          if ([2, 3, 4].includes(valueFieldMap[f]?.deType)) {
            const fieldValues = newData.map(item => item[f])
            const uniqueValues = [...new Set(fieldValues)]
            uniqueValues.sort((a, b) => {
              return sortMethod === 'ASC' ? a - b : b - a
            })
            sort.sortBy = uniqueValues
          } else if (i === 0) {
            sort.sortMethod = sortMethod
          } else {
            const fieldValues = newData.map(item => item[f])
            const uniqueValues = [...new Set(fieldValues)]

            // 根据配置动态决定排序顺序
            uniqueValues.sort((a, b) => {
              if (!a && !b) {
                return 0
              }
              if (!a) {
                return sortMethod === 'ASC' ? -1 : 1
              }
              if (!b) {
                return sortMethod === 'ASC' ? 1 : -1
              }
              return sortMethod === 'ASC' ? a.localeCompare(b) : b.localeCompare(a)
            })
            sort.sortBy = uniqueValues
          }
        }
        sortParams.push(sort)
      })
    }
    if (!colTotalSort) {
      r?.forEach((f, i) => {
        if (valueFieldMap[f]?.sort === 'none') {
          return
        }
        const sort = {
          sortFieldId: f
        }
        const sortMethod = valueFieldMap[f]?.sort?.toUpperCase()
        if (sortMethod === 'CUSTOM_SORT') {
          sort.sortBy = valueFieldMap[f].customSort
        } else {
          if ([2, 3, 4].includes(valueFieldMap[f]?.deType)) {
            const fieldValues = newData.map(item => item[f])
            const uniqueValues = [...new Set(fieldValues)]
            uniqueValues.sort((a, b) => {
              return sortMethod === 'ASC' ? a - b : b - a
            })
            sort.sortBy = uniqueValues
          } else if (i === 0) {
            sort.sortMethod = sortMethod
          } else {
            const fieldValues = newData.map(item => item[f])
            const uniqueValues = [...new Set(fieldValues)]
            // 根据配置动态决定排序顺序
            uniqueValues.sort((a, b) => {
              if (!a && !b) {
                return 0
              }
              if (!a) {
                return sortMethod === 'ASC' ? -1 : 1
              }
              if (!b) {
                return sortMethod === 'ASC' ? 1 : -1
              }
              return sortMethod === 'ASC' ? a.localeCompare(b) : b.localeCompare(a)
            })
            sort.sortBy = uniqueValues
          }
        }
        sortParams.push(sort)
      })
    }
    // data config
    const s2DataConfig: S2DataConfig = {
      fields: {
        rows: r,
        columns: c,
        values: v,
        valueInCols: !(basicStyle.quotaPosition === 'row')
      },
      meta: meta,
      data: newData,
      sortParams: sortParams
    }
    const s2Options: S2Options = {
      width: containerDom.offsetWidth,
      height: containerDom.offsetHeight,
      totals: tableTotal as Totals,
      cornerExtraFieldText: basicStyle.quotaColLabel ?? t('dataset.value'),
      conditions: this.configConditions(chart),
      tooltip: {
        getContainer: () => containerDom
      },
      hierarchyType: basicStyle.tableLayoutMode ?? 'grid',
      dataSet: spreadSheet => new CustomPivotDataset(spreadSheet),
      interaction: {
        hoverHighlight: !(basicStyle.showHoverStyle === false)
      },
      dataCell: meta => {
        return new CustomDataCell(meta, meta.spreadsheet)
      },
      frozenRowHeader: !(tableHeader.rowHeaderFreeze === false)
    }
    // options
    s2Options.style = this.configStyle(chart, s2DataConfig)
    // 默认展开层级
    if (basicStyle.tableLayoutMode === 'tree') {
      const { defaultExpandLevel } = basicStyle
      if (isNumber(defaultExpandLevel)) {
        if (defaultExpandLevel >= chart.xAxis.length) {
          s2Options.style.rowExpandDepth = defaultExpandLevel
        } else {
          s2Options.style.rowExpandDepth = defaultExpandLevel - 2
        }
      }
      if (defaultExpandLevel === 'all') {
        s2Options.style.rowExpandDepth = chart.xAxis.length
      }
      if (!defaultExpandLevel) {
        s2Options.style.hierarchyCollapse = true
      }
    }
    // 列汇总别名
    if (!(basicStyle.quotaPosition === 'row' && basicStyle.tableLayoutMode === 'tree')) {
      if (
        basicStyle.quotaPosition !== 'row' &&
        chart.xAxisExt?.length &&
        chart.yAxis?.length > 1 &&
        tableTotal.col.showGrandTotals &&
        tableTotal.col.calcTotals?.cfg?.length
      ) {
        const colTotalCfgMap = tableTotal.col.calcTotals.cfg.reduce((p, n) => {
          p[n.dataeaseName] = n
          return p
        }, {})
        s2Options.layoutCoordinate = (_, __, col) => {
          if (col?.isGrandTotals) {
            if (colTotalCfgMap[col.value]?.label) {
              col.label = colTotalCfgMap[col.value].label
            }
          }
        }
      }
      if (
        basicStyle.quotaPosition === 'row' &&
        chart.xAxisExt?.length &&
        chart.yAxis?.length > 1 &&
        tableTotal.row.showGrandTotals &&
        tableTotal.row.calcTotals?.cfg?.length
      ) {
        const rowTotalCfgMap = tableTotal.row.calcTotals.cfg.reduce((p, n) => {
          p[n.dataeaseName] = n
          return p
        }, {})
        // eslint-disable-next-line
        s2Options.layoutCoordinate = (_, row, __) => {
          if (row?.isGrandTotals) {
            if (rowTotalCfgMap[row.value]?.label) {
              row.label = rowTotalCfgMap[row.value].label
            }
          }
        }
      }
    }
    // tooltip
    this.configTooltip(chart, s2Options)
    // 开始渲染
    const s2 = new PivotSheet(containerDom, s2DataConfig, s2Options as unknown as S2Options)
    // 自适应铺满
    if (basicStyle.tableColumnMode === 'adapt') {
      s2.on(S2Event.LAYOUT_RESIZE_COL_WIDTH, () => {
        s2.store.set('lastLayoutResult', s2.facet.layoutResult)
      })
      // 平铺模式行头resize
      s2.on(S2Event.LAYOUT_RESIZE_ROW_WIDTH, () => {
        s2.store.set('lastLayoutResult', s2.facet.layoutResult)
      })
      // 树形模式行头resize
      s2.on(S2Event.LAYOUT_RESIZE_TREE_WIDTH, () => {
        s2.store.set('lastLayoutResult', s2.facet.layoutResult)
      })
      s2.on(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (ev: LayoutResult) => {
        const lastLayoutResult = s2.store.get('lastLayoutResult') as LayoutResult
        if (lastLayoutResult) {
          // 拖动 col 表头 resize
          const colWidthByFieldValue = s2.options.style?.colCfg?.widthByFieldValue
          // 平铺模式拖动 row 表头 resize
          const rowWidthByField = s2.options.style?.rowCfg?.widthByField
          // 树形模式拖动 row 表头 resize
          const treeRowWidth =
            s2.options.style?.treeRowsWidth || lastLayoutResult.rowsHierarchy.width
          const colWidthMap =
            lastLayoutResult.colLeafNodes.reduce((p, n) => {
              p[n.id] = colWidthByFieldValue?.[n.value] ?? n.width
              return p
            }, {}) || {}
          const totalColWidth = ev.colLeafNodes.reduce((p, n) => {
            n.width = colWidthMap[n.id] || n.width
            n.x = p
            return p + n.width
          }, 0)
          ev.colNodes.forEach(n => {
            if (n.isLeaf) {
              return
            }
            n.width = this.getColWidth(n)
            n.x = this.getLeftChild(n).x
          })
          if (basicStyle.tableLayoutMode === 'tree') {
            ev.rowNodes.forEach(n => {
              n.width = treeRowWidth
            })
            ev.rowsHierarchy.width = treeRowWidth
            ev.colsHierarchy.width = totalColWidth
          } else {
            const rowWidthMap =
              lastLayoutResult.rowNodes.reduce((p, n) => {
                p[n.id] = rowWidthByField?.[n.field] ?? n.width
                return p
              }, {}) || {}
            ev.rowNodes.forEach(n => {
              n.x = 0
              n.width = rowWidthMap[n.id] || n.width
              let tmp = n
              while (tmp.parent.id !== 'root') {
                n.x += tmp.parent.width
                tmp = tmp.parent
              }
            })
            const totlaRowWidth = ev.rowsHierarchy.sampleNodesForAllLevels.reduce((p, n) => {
              return p + n.width
            }, 0)
            const maxRowLevel = ev.rowsHierarchy.maxLevel
            ev.rowNodes.forEach(n => {
              // 总计和中间层级的小计需要重新计算宽度
              if (n.isTotalRoot || (n.isSubTotals && n.level < maxRowLevel)) {
                let width = 0
                for (let i = n.level; i <= maxRowLevel; i++) {
                  width += ev.rowsHierarchy.sampleNodesForAllLevels[i].width
                }
                n.width = width
              }
            })
            ev.rowsHierarchy.width = totlaRowWidth
            ev.colsHierarchy.width = totalColWidth
          }
          s2.store.set('lastLayoutResult', undefined)
          return
        }
        const containerWidth = containerDom.getBoundingClientRect().width
        const scale = containerWidth / (ev.colsHierarchy.width + ev.rowsHierarchy.width)
        if (scale <= 1) {
          return
        }
        const totalRowWidth = Math.round(ev.rowsHierarchy.width * scale)
        ev.rowNodes.forEach(n => {
          n.width = Math.round(n.width * scale)
        })
        if (basicStyle.tableLayoutMode !== 'tree') {
          ev.rowNodes.forEach(n => {
            n.x = 0
            let tmp = n
            while (tmp.parent.id !== 'root') {
              n.x += tmp.parent.width
              tmp = tmp.parent
            }
          })
        }
        let totalColWidth = ev.colLeafNodes.reduce((p, n) => {
          n.width = Math.round(n.width * scale)
          n.x = p
          return p + n.width
        }, 0)
        ev.colNodes.forEach(n => {
          if (n.isLeaf) {
            return
          }
          n.width = this.getColWidth(n)
          n.x = this.getLeftChild(n).x
        })
        const totalWidth = totalColWidth + totalRowWidth
        if (totalWidth > containerWidth) {
          // 从最后一列减掉
          ev.colLeafNodes[ev.colLeafNodes.length - 1].width -= totalWidth - containerWidth
          totalColWidth = totalColWidth - (totalWidth - containerWidth)
        }
        ev.colsHierarchy.width = totalColWidth
        ev.rowsHierarchy.width = totalRowWidth
      })
    }
    // tooltip
    const { show } = tooltip
    if (show) {
      s2.on(S2Event.COL_CELL_HOVER, event => this.showTooltip(s2, event, meta))
      s2.on(S2Event.ROW_CELL_HOVER, event => this.showTooltip(s2, event, meta))
      s2.on(S2Event.DATA_CELL_HOVER, event => this.showTooltip(s2, event, meta))
      // touch
      this.configTouchEvent(s2, drawOption, meta)
    }
    // empty data tip
    configEmptyDataStyle(s2, newData)
    // click
    s2.on(S2Event.DATA_CELL_CLICK, ev => this.dataCellClickAction(chart, ev, s2, action))
    s2.on(S2Event.ROW_CELL_CLICK, ev => this.headerCellClickAction(chart, ev, s2, action))
    s2.on(S2Event.COL_CELL_CLICK, ev => this.headerCellClickAction(chart, ev, s2, action))
    // right click
    s2.on(S2Event.GLOBAL_CONTEXT_MENU, event => copyContent(s2, event, meta))
    // theme
    const customTheme = this.configTheme(chart)
    s2.setThemeCfg({ theme: customTheme })

    return s2
  }
  private getColWidth(node) {
    let width = 0
    if (node.children?.length) {
      node.children.forEach(child => {
        width += this.getColWidth(child)
      })
    } else {
      width = node.width
    }
    return width
  }
  private getLeftChild(node) {
    if (!node.children?.length) {
      return node
    }
    return this.getLeftChild(node.children[0])
  }
  private dataCellClickAction(chart: Chart, ev, s2Instance: PivotSheet, callback) {
    const cell = s2Instance.getCell(ev.target)
    const meta = cell.getMeta()
    const nameIdMap = chart.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['id']
      return pre
    }, {})
    const rowData = { ...meta.rowQuery, ...meta.colQuery }
    rowData[meta.valueField] = meta.fieldValue
    const dimensionList = []
    for (const key in rowData) {
      if (nameIdMap[key]) {
        dimensionList.push({ id: nameIdMap[key], value: rowData[key] })
      }
    }
    const param = {
      x: ev.x,
      y: ev.y,
      data: {
        dimensionList,
        name: nameIdMap[meta.valueField],
        sourceType: 'table-pivot',
        quotaList: []
      }
    }
    callback(param)
  }
  private headerCellClickAction(chart: Chart, ev, s2Instance: PivotSheet, callback) {
    const cell = s2Instance.getCell(ev.target)
    const meta = cell.getMeta()
    const rowData = meta.query
    const nameIdMap = chart.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['id']
      return pre
    }, {})
    const dimensionList = []
    for (const key in rowData) {
      if (nameIdMap[key]) {
        dimensionList.push({ id: nameIdMap[key], value: rowData[key] })
      }
    }
    const param = {
      x: ev.x,
      y: ev.y,
      data: {
        dimensionList,
        name: nameIdMap[meta.valueField],
        sourceType: 'table-pivot',
        quotaList: []
      }
    }
    callback(param)
  }
  protected configTheme(chart: Chart): S2Theme {
    const theme = super.configTheme(chart)
    const { basicStyle, tableHeader } = parseJson(chart.customAttr)
    let tableHeaderBgColor = tableHeader.tableHeaderBgColor
    if (!isAlphaColor(tableHeaderBgColor)) {
      tableHeaderBgColor = hexColorToRGBA(tableHeaderBgColor, basicStyle.alpha)
    }
    let tableHeaderCornerBgColor =
      tableHeader.tableHeaderCornerBgColor ?? DEFAULT_TABLE_HEADER.tableHeaderCornerBgColor
    if (!isAlphaColor(tableHeaderCornerBgColor)) {
      tableHeaderCornerBgColor = hexColorToRGBA(tableHeaderCornerBgColor, basicStyle.alpha)
    }
    let tableHeaderColBgColor =
      tableHeader.tableHeaderColBgColor ?? DEFAULT_TABLE_HEADER.tableHeaderColBgColor
    if (!isAlphaColor(tableHeaderColBgColor)) {
      tableHeaderColBgColor = hexColorToRGBA(tableHeaderColBgColor, basicStyle.alpha)
    }
    let tableBorderColor = basicStyle.tableBorderColor
    if (!isAlphaColor(tableBorderColor)) {
      tableBorderColor = hexColorToRGBA(tableBorderColor, basicStyle.alpha)
    }
    const tableHeaderColFontColor = hexColorToRGBA(
      tableHeader.tableHeaderColFontColor,
      basicStyle.alpha
    )
    const tableHeaderCornerFontColor = hexColorToRGBA(
      tableHeader.tableHeaderCornerFontColor,
      basicStyle.alpha
    )
    const colFontStyle = tableHeader.isColItalic ? 'italic' : 'normal'
    const cornerFontStyle = tableHeader.isCornerItalic ? 'italic' : 'normal'
    const colFontWeight = tableHeader.isColBolder === false ? 'normal' : 'bold'
    const cornerFontWeight = tableHeader.isCornerBolder === false ? 'normal' : 'bold'
    const pivotTheme = {
      rowCell: {
        cell: {
          backgroundColor: tableHeaderColBgColor,
          horizontalBorderColor: tableBorderColor,
          verticalBorderColor: tableBorderColor
        },
        text: {
          fill: tableHeaderColFontColor,
          fontSize: tableHeader.tableTitleColFontSize,
          textAlign: tableHeader.tableHeaderColAlign,
          textBaseline: 'top',
          fontStyle: colFontStyle,
          fontWeight: colFontWeight
        },
        bolderText: {
          fill: tableHeaderColFontColor,
          fontSize: tableHeader.tableTitleColFontSize,
          textAlign: tableHeader.tableHeaderColAlign,
          fontStyle: colFontStyle,
          fontWeight: colFontWeight
        },
        measureText: {
          fill: tableHeaderColFontColor,
          fontSize: tableHeader.tableTitleColFontSize,
          textAlign: tableHeader.tableHeaderColAlign,
          fontStyle: colFontStyle,
          fontWeight: colFontWeight
        },
        seriesText: {
          fill: tableHeaderColFontColor,
          fontSize: tableHeader.tableTitleColFontSize,
          textAlign: tableHeader.tableHeaderColAlign,
          fontStyle: colFontStyle,
          fontWeight: colFontWeight
        }
      },
      cornerCell: {
        cell: {
          backgroundColor: tableHeaderCornerBgColor
        },
        text: {
          fill: tableHeaderCornerFontColor,
          fontSize: tableHeader.tableTitleCornerFontSize,
          textAlign: tableHeader.tableHeaderCornerAlign,
          fontStyle: cornerFontStyle,
          fontWeight: cornerFontWeight
        },
        bolderText: {
          fill: tableHeaderCornerFontColor,
          fontSize: tableHeader.tableTitleCornerFontSize,
          textAlign: tableHeader.tableHeaderCornerAlign,
          fontStyle: cornerFontStyle,
          fontWeight: cornerFontWeight
        },
        measureText: {
          fill: tableHeaderCornerFontColor,
          fontSize: tableHeader.tableTitleCornerFontSize,
          textAlign: tableHeader.tableHeaderCornerAlign,
          fontStyle: cornerFontStyle,
          fontWeight: cornerFontWeight
        }
      },
      dataCell: {
        bolderText: {
          fontWeight: 'bold'
        }
      }
    }
    merge(theme, pivotTheme)
    if (tableHeader.showHorizonBorder === false) {
      const tmp: S2Theme = {
        cornerCell: {
          cell: {
            horizontalBorderColor: tableHeaderBgColor,
            horizontalBorderWidth: 0
          }
        },
        rowCell: {
          cell: {
            horizontalBorderColor: tableHeaderBgColor,
            horizontalBorderWidth: 0
          }
        }
      }
      merge(theme, tmp)
    }
    if (tableHeader.showVerticalBorder === false) {
      const tmp: S2Theme = {
        cornerCell: {
          cell: {
            verticalBorderColor: tableHeaderBgColor,
            verticalBorderWidth: 0
          }
        },
        rowCell: {
          cell: {
            verticalBorderColor: tableHeaderBgColor,
            verticalBorderWidth: 0
          }
        }
      }
      merge(theme, tmp)
    }
    return theme
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr } = chart
    if (customAttr.basicStyle.tableColumnMode === 'field') {
      customAttr.basicStyle.tableColumnMode = 'custom'
    }
    return chart
  }

  constructor() {
    super('table-pivot', [])
  }
}
function customCalcFunc(query, data, status, chart, totalCfgMap, axisMap, customCalc) {
  if (!data?.length || !query[EXTRA_FIELD]) {
    return '-'
  }
  const aggregation = totalCfgMap[query[EXTRA_FIELD]]?.aggregation || 'SUM'
  switch (aggregation) {
    case 'SUM': {
      return data.reduce((p, n) => {
        return p + parseFloat(n[query[EXTRA_FIELD]] ?? 0)
      }, 0)
    }
    case 'AVG': {
      const sum = data.reduce((p, n) => {
        return p + parseFloat(n[query[EXTRA_FIELD]] ?? 0)
      }, 0)
      return sum / data.length
    }
    case 'MIN': {
      const result = minBy(data, n => {
        return parseFloat(n[query[EXTRA_FIELD]])
      })
      return result?.[query[EXTRA_FIELD]]
    }
    case 'MAX': {
      const result = maxBy(data, n => {
        return parseFloat(n[query[EXTRA_FIELD]])
      })
      return result?.[query[EXTRA_FIELD]]
    }
    case 'NONE': {
      return '-'
    }
    case 'CUSTOM': {
      const val = getCustomCalcResult(query, axisMap, chart, status, customCalc || {})
      if (val === '' || val === undefined) {
        return '-'
      }
      return parseFloat(val)
    }
    default: {
      return data.reduce((p, n) => {
        return p + parseFloat(n[query[EXTRA_FIELD]] ?? 0)
      }, 0)
    }
  }
}

function getTreeCustomCalcResult(query, axisMap, status: TotalStatus, customCalc) {
  const quotaField = query[EXTRA_FIELD]
  const { row, col } = axisMap
  // 行列交叉总计
  if (status.isRowTotal && status.isColTotal) {
    return customCalc.rowColTotal?.data?.[quotaField]
  }
  // 列总计
  if (status.isColTotal && !status.isRowSubTotal) {
    const { colTotal, rowSubInColTotal } = customCalc
    const path = getTreePath(query, row)
    let val
    if (path.length) {
      const subLevel = getSubLevel(query, row)
      if (subLevel + 1 === row.length && colTotal) {
        path.push(quotaField)
        val = get(colTotal.data, path)
      }
      if (subLevel + 1 < row.length && rowSubInColTotal) {
        const data = rowSubInColTotal?.[subLevel]?.data
        path.push(quotaField)
        val = get(data, path)
      }
    }
    return val
  }
  // 列小计
  if (status.isColSubTotal && !status.isRowTotal && !status.isRowSubTotal) {
    const { colSubTotal } = customCalc
    const subColLevel = getSubLevel(query, col)
    const subRowLevel = getSubLevel(query, row)
    const rowPath = getTreePath(query, row)
    const colPath = getTreePath(query, col)
    const path = [...rowPath, ...colPath]
    let data = colSubTotal?.[subColLevel]?.data
    // 列小计里面的行小计
    if (rowPath.length < row.length) {
      const { rowSubInColSub } = customCalc
      data = rowSubInColSub?.[subRowLevel]?.[subColLevel]?.data
    }
    let val
    if (path.length && data) {
      path.push(quotaField)
      val = get(data, path)
    }
    return val
  }
  // 行总计
  if (status.isRowTotal && !status.isColSubTotal) {
    const { rowTotal } = customCalc
    const path = getTreePath(query, col)
    let val
    if (rowTotal) {
      if (path.length) {
        path.push(quotaField)
        val = get(rowTotal.data, path)
      }
      // 列维度为空，行维度不为空
      if (!col.length && row.length) {
        val = get(rowTotal.data, quotaField)
      }
    }
    return val
  }
  // 行小计
  if (status.isRowSubTotal) {
    // 列维度为空，行小计直接当成列总计
    if (
      (!status.isColTotal && !status.isColSubTotal) ||
      (!col.length && status.isColTotal && status.isRowSubTotal)
    ) {
      const { rowSubTotal } = customCalc
      const rowLevel = getSubLevel(query, row)
      const colPath = getTreePath(query, col)
      const rowPath = getTreePath(query, row)
      const path = [...colPath, ...rowPath]
      const data = rowSubTotal?.[rowLevel]?.data
      let val
      if (path.length && rowSubTotal) {
        path.push(quotaField)
        val = get(data, path)
      }
      return val
    }
  }
  // 行总计里面的列小计
  if (status.isRowTotal && status.isColSubTotal) {
    const { colSubInRowTotal } = customCalc
    const colLevel = getSubLevel(query, col)
    const data = colSubInRowTotal?.[colLevel]?.data
    const colPath = getTreePath(query, col)
    let val
    if (colPath.length && colSubInRowTotal) {
      colPath.push(quotaField)
      val = get(data, colPath)
    }
    return val
  }
  // 列总计里面的行小计
  if (status.isColTotal && status.isRowSubTotal) {
    const { rowSubInColTotal } = customCalc
    const rowSubLevel = getSubLevel(query, row)
    const data = rowSubInColTotal?.[rowSubLevel]?.data
    const path = getTreePath(query, row)
    let val
    if (path.length && rowSubInColTotal) {
      path.push(quotaField)
      val = get(data, path)
    }
    return val
  }
  return '-'
}

function getGridCustomCalcResult(query, axisMap, status: TotalStatus, customCalc) {
  const quotaField = query[EXTRA_FIELD]
  const { row, col } = axisMap
  // 行列交叉总计
  if (status.isRowTotal && status.isColTotal) {
    return customCalc.rowColTotal?.data?.[quotaField]
  }
  // 列总计
  if (status.isColTotal && !status.isRowSubTotal) {
    const { colTotal } = customCalc
    const path = getTreePath(query, row)
    let val
    if (path.length) {
      if (colTotal) {
        path.push(quotaField)
        val = get(colTotal.data, path)
      }
    }
    return val
  }
  // 列小计
  if (status.isColSubTotal && !status.isRowTotal && !status.isRowSubTotal) {
    const { colSubTotal } = customCalc
    const subLevel = getSubLevel(query, col)
    const rowPath = getTreePath(query, row)
    const colPath = getTreePath(query, col)
    const path = [...rowPath, ...colPath]
    const data = colSubTotal?.[subLevel]?.data
    let val
    if (path.length && data) {
      path.push(quotaField)
      val = get(data, path)
    }
    return val
  }
  // 行总计
  if (status.isRowTotal && !status.isColSubTotal) {
    const { rowTotal } = customCalc
    const path = getTreePath(query, col)
    let val
    if (rowTotal) {
      if (path.length) {
        path.push(quotaField)
        val = get(rowTotal.data, path)
      }
      // 列维度为空，行维度不为空
      if (!col.length && row.length) {
        val = get(rowTotal.data, quotaField)
      }
    }
    return val
  }
  // 行小计
  if (status.isRowSubTotal && !status.isColTotal && !status.isColSubTotal) {
    const { rowSubTotal } = customCalc
    const rowLevel = getSubLevel(query, row)
    const colPath = getTreePath(query, col)
    const rowPath = getTreePath(query, row)
    const path = [...colPath, ...rowPath]
    const data = rowSubTotal?.[rowLevel]?.data
    let val
    if (path.length && rowSubTotal) {
      path.push(quotaField)
      val = get(data, path)
    }
    return val
  }
  // 行总计里面的列小计
  if (status.isRowTotal && status.isColSubTotal) {
    const { colSubInRowTotal } = customCalc
    const colLevel = getSubLevel(query, col)
    const data = colSubInRowTotal?.[colLevel]?.data
    const colPath = getTreePath(query, col)
    let val
    if (colPath.length && colSubInRowTotal) {
      colPath.push(quotaField)
      val = get(data, colPath)
    }
    return val
  }
  // 列总计里面的行小计
  if (status.isColTotal && status.isRowSubTotal) {
    const { rowSubInColTotal } = customCalc
    const rowSubLevel = getSubLevel(query, row)
    const data = rowSubInColTotal?.[rowSubLevel]?.data
    const path = getTreePath(query, row)
    let val
    if (path.length && rowSubInColTotal) {
      path.push(quotaField)
      val = get(data, path)
    }
    return val
  }
  // 列小计里面的行小计
  if (status.isColSubTotal && status.isRowSubTotal) {
    const { rowSubInColSub } = customCalc
    const rowSubLevel = getSubLevel(query, row)
    const colSubLevel = getSubLevel(query, col)
    const data = rowSubInColSub?.[rowSubLevel]?.[colSubLevel]?.data
    const rowPath = getTreePath(query, row)
    const colPath = getTreePath(query, col)
    const path = [...rowPath, ...colPath]
    let val
    if (path.length && rowSubInColSub) {
      path.push(quotaField)
      val = get(data, path)
    }
    return val
  }
}
function getCustomCalcResult(query, axisMap, chart: ChartObj, status: TotalStatus, customCalc) {
  const { tableLayoutMode } = chart.customAttr.basicStyle
  if (tableLayoutMode === 'tree') {
    return getTreeCustomCalcResult(query, axisMap, status, customCalc)
  }
  return getGridCustomCalcResult(query, axisMap, status, customCalc)
}

function getSubLevel(query, axis) {
  const fields: [] = axis.map(a => a.dataeaseName)
  let subLevel = -1
  const queryFields = keys(query)
  for (let i = fields.length - 1; i >= 0; i--) {
    const field = fields[i]
    const index = queryFields.findIndex(f => f === field)
    if (index !== -1) {
      subLevel++
    }
  }
  return subLevel
}

function getTreePath(query, axis) {
  const path = []
  const fields = keys(query)
  axis.forEach(a => {
    const index = fields.findIndex(f => f === a.dataeaseName)
    if (index !== -1) {
      path.push(query[a.dataeaseName])
    }
  })
  return path
}

function getAggregationAndCalcFuncByQuery(totalsStatus, totalsOptions) {
  const { isRowTotal, isRowSubTotal, isColTotal, isColSubTotal } = totalsStatus
  const { row, col } = totalsOptions || {}
  const { calcTotals: rowCalcTotals = {}, calcSubTotals: rowCalcSubTotals = {} } = row || {}
  const { calcTotals: colCalcTotals = {}, calcSubTotals: colCalcSubTotals = {} } = col || {}

  const getCalcTotals = (dimensionTotals: CalcTotals, isTotal: boolean) => {
    if ((dimensionTotals.aggregation || dimensionTotals.calcFunc) && isTotal) {
      return {
        aggregation: dimensionTotals.aggregation,
        calcFunc: dimensionTotals.calcFunc
      }
    }
  }

  // 优先级: 列总计/小计 > 行总计/小计
  return (
    getCalcTotals(colCalcTotals, isColTotal) ||
    getCalcTotals(colCalcSubTotals, isColSubTotal) ||
    getCalcTotals(rowCalcTotals, isRowTotal) ||
    getCalcTotals(rowCalcSubTotals, isRowSubTotal)
  )
}

export const isNotNumber = (value: unknown) => {
  if (typeof value === 'number') {
    return Number.isNaN(value)
  }
  if (!value) {
    return true
  }
  if (typeof value === 'string') {
    return Number.isNaN(Number(value))
  }
  return true
}

const processFieldValues = (data: DataItem[], field: string, filterIllegalValue = false) => {
  if (!data?.length) {
    return []
  }

  return data.reduce<Array<Decimal>>((resultArr, item) => {
    const fieldValue = get(item, field)
    const notNumber = isNotNumber(fieldValue)

    if (filterIllegalValue && notNumber) {
      // 过滤非法值
      return resultArr
    }

    const val = notNumber ? 0 : fieldValue
    resultArr.push(new Decimal(val))

    return resultArr
  }, [])
}

export const getDataSumByField = (data: DataItem[], field: string): number => {
  const fieldValues = processFieldValues(data, field)
  if (!fieldValues.length) {
    return 0
  }

  return Decimal.sum(...fieldValues).toNumber()
}

export const getDataExtremumByField = (
  method: 'min' | 'max',
  data: DataItem[],
  field: string
): number => {
  // 防止预处理时默认值 0 影响极值结果，处理时需过滤非法值
  const fieldValues = processFieldValues(data, field, true)
  if (!fieldValues?.length) {
    return
  }

  return Decimal[method](...fieldValues).toNumber()
}

export const getDataAvgByField = (data: DataItem[], field: string): number => {
  const fieldValues = processFieldValues(data, field)
  if (!fieldValues?.length) {
    return 0
  }

  return Decimal.sum(...fieldValues)
    .dividedBy(fieldValues.length)
    .toNumber()
}

const calcActionByType: {
  [type in Aggregation]: (data: DataItem[], field: string) => number
} = {
  [Aggregation.SUM]: getDataSumByField,
  [Aggregation.MIN]: (data, field) => getDataExtremumByField('min', data, field),
  [Aggregation.MAX]: (data, field) => getDataExtremumByField('max', data, field),
  [Aggregation.AVG]: getDataAvgByField
}

class EmptyDataCell extends MergedCell {
  drawTextShape(): void {
    this.meta.fieldValue = ' '
    super.drawTextShape()
    const { rowHeader, columnHeader } = this.spreadsheet.facet
    const offsetX = columnHeader.getConfig().viewportWidth / 2
    const offsetY = rowHeader.getConfig().viewportHeight / 2
    const style = this.getTextStyle()
    const config = {
      attrs: {
        ...style,
        x: offsetX,
        y: offsetY,
        text: t('data_set.no_data'),
        opacity: 1,
        textAlign: 'center',
        textBaseline: 'middle'
      }
    }
    this.addShape('text', config)
  }

  protected drawBackgroundShape(): void {
    const cellTheme = this.theme.dataCell.cell
    cellTheme.backgroundColor = setColorOpacity(cellTheme.backgroundColor, 1)
    super.drawBackgroundShape()
  }
}

export function setColorOpacity(color: string, opacity: number) {
  if (color.indexOf('rgba') !== -1) {
    const colorArr = color.split(',')
    colorArr[3] = `${opacity})`
    return colorArr.join(',')
  }
  if (color.indexOf('rgb') !== -1) {
    return `${color.replace('rgb', 'rgba').replace(')', `,${opacity})`)}`
  }
  if (color.indexOf('#') !== -1) {
    if (color.length === 7) {
      return `${color}${Math.round(opacity * 255).toString(16)}`
    }
    if (color.length === 9) {
      return color.slice(0, 7) + Math.round(opacity * 255).toString(16)
    }
  }
  return color
}

function configEmptyDataStyle(instance: PivotSheet, data: any[]) {
  if (data?.length) {
    return
  }
  instance.on(S2Event.LAYOUT_AFTER_RENDER, () => {
    const { colLeafNodes, rowLeafNodes } = instance.facet?.layoutResult || {}
    if (!colLeafNodes?.length || !rowLeafNodes?.length) {
      return
    }
    const mergedCells = []
    colLeafNodes.forEach((_, colIndex) => {
      rowLeafNodes.forEach((__, rowIndex) => {
        mergedCells.push({ rowIndex, colIndex })
      })
    })
    instance.options.mergedCell = (s, c, m) => new EmptyDataCell(s, c, m)
    instance.interaction.mergeCells(mergedCells)
  })
}
