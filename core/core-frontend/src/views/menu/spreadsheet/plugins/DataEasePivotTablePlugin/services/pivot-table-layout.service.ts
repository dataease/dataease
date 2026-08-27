import {
  findConfiguredField,
  getFieldDisplayScale,
  getFieldNumberFormat,
  toNativeCellValue
} from '../utils/field-format'
import type { FieldItemData } from '../../../types/plugin'
import type {
  PivotTableConfig,
  PivotTableField,
  PivotTableQueryResult
} from '../types'

export interface PivotLayoutRange {
  startRow: number
  endRow: number
  startColumn: number
  endColumn: number
}

export interface PivotTableCornerLayout {
  range: PivotLayoutRange
  values: any[][]
  twoParts: [string, string]
  threeParts: [string, string, string]
}

export interface PivotTableValueRegion {
  range: PivotLayoutRange
  values: any[][]
}

export interface PivotTableLayout {
  values: any[][]
  displayScales: number[][]
  rowCount: number
  columnCount: number
  headerRowCount: number
  headerColumnCount: number
  dataRange?: PivotLayoutRange
  merges: PivotLayoutRange[]
  axisHeaderValues: PivotTableValueRegion[]
  corner?: PivotTableCornerLayout
}

interface AxisEntry {
  key: string
  labels: unknown[]
  labelFields: Array<FieldItemData | undefined>
  mergeKeys: string[]
  metric?: PivotTableField
}

export class PivotTableLayoutService {
  build(
    config: PivotTableConfig,
    result: PivotTableQueryResult
  ): PivotTableLayout {
    const rowConfigured = config.data.zones.rows || []
    const columnConfigured = config.data.zones.columns || []
    const quotaFields = result.data.quotaFields || []
    const records = result.data.rowData || []
    const quotaInRows = rowConfigured.some(field => field.groupType === 'q')
    const quotaInColumns = columnConfigured.some(field => field.groupType === 'q')
    const configuredFields = [...rowConfigured, ...columnConfigured]
    const rowFieldLabels = this.axisDisplayFields(rowConfigured)
    const columnFieldLabels = this.axisDisplayFields(columnConfigured)
    const hasVisibleQuota = configuredFields.some(
      field => field.groupType === 'q' && field.hidden !== true
    )
    const visibleQuotaFields = quotaFields.filter(field => {
      const configuredField = findConfiguredField(configuredFields, field)
      return configuredField?.hidden !== true
    })

    const rowEntries = this.buildAxisEntries(
      rowConfigured,
      result.data.rowFields || [],
      quotaInRows ? quotaFields : [],
      records
    )
    const columnEntries = this.buildAxisEntries(
      columnConfigured,
      result.data.columnFields || [],
      quotaInColumns ? quotaFields : [],
      records
    )

    // 角头尺寸必须由最终可见字段决定，空数据时也要保留字段名表头。
    const rowDepth = rowFieldLabels.length
    const columnDepth = columnFieldLabels.length
    const hasQuota = visibleQuotaFields.length > 0
    const topRows = columnDepth > 0 ? columnDepth : (rowDepth > 0 ? 1 : 0)
    const leftColumns = rowDepth > 0
      ? rowDepth
      : (columnDepth > 0 ? 1 : 0)
    const dataRows = rowEntries.length > 0
      ? rowEntries.length
      : (hasQuota ? 1 : 0)
    const dataColumns = columnEntries.length > 0
      ? columnEntries.length
      : (hasQuota ? 1 : 0)
    const rowCount = Math.max(1, topRows + dataRows)
    const columnCount = Math.max(1, leftColumns + dataColumns)
    const values = Array.from({ length: rowCount }, () =>
      Array.from({ length: columnCount }, () => '')
    )
    const displayScales = Array.from({ length: rowCount }, () =>
      Array.from({ length: columnCount }, () => 1)
    )

    this.fillAxisHeaders(
      values,
      rowEntries,
      columnEntries,
      topRows,
      leftColumns
    )
    this.fillAxisFieldHeaders(
      values,
      rowFieldLabels,
      columnFieldLabels,
      topRows,
      leftColumns
    )
    this.fillSingleAxisLabels(
      values,
      rowFieldLabels,
      columnFieldLabels.length > 0,
      quotaInRows,
      topRows,
      leftColumns,
      dataColumns
    )

    if (hasQuota) {
      this.fillMetricValues(
        values,
        displayScales,
        config,
        result,
        rowEntries,
        columnEntries,
        topRows,
        leftColumns,
        quotaInRows,
        quotaInColumns
      )
    }

    const dataRange = dataRows > 0 && dataColumns > 0
      ? {
          startRow: topRows,
          endRow: topRows + dataRows - 1,
          startColumn: leftColumns,
          endColumn: leftColumns + dataColumns - 1
        }
      : undefined
    const merges = [
      ...this.buildRowHeaderMerges(rowEntries, topRows),
      ...this.buildColumnHeaderMerges(columnEntries, leftColumns)
    ]
    const axisHeaderValues = this.buildAxisHeaderValueRegions(
      values,
      rowCount,
      columnCount,
      topRows,
      leftColumns
    )
    const corner = this.buildCornerLayout(
      values,
      rowConfigured,
      columnConfigured,
      rowFieldLabels,
      columnFieldLabels,
      hasVisibleQuota,
      topRows,
      leftColumns
    )

    return {
      values,
      displayScales,
      rowCount,
      columnCount,
      headerRowCount: topRows,
      headerColumnCount: leftColumns,
      dataRange,
      merges,
      axisHeaderValues,
      corner
    }
  }

  private buildAxisEntries(
    configuredFields: PivotTableField[],
    dimensionFields: PivotTableField[],
    quotaFields: PivotTableField[],
    records: Record<string, unknown>[]
  ): AxisEntry[] {
    if (!configuredFields.length) {
      return []
    }

    const dimensionTuples = this.uniqueDimensionTuples(dimensionFields, records)
    const configuredDimensionFields = dimensionFields.map(field =>
      findConfiguredField(configuredFields, field) ?? field
    )
    // 完整维度元组继续用于轴定位，标签和合并键只保留实际展示的层级。
    const visibleDimensionIndexes = configuredDimensionFields
      .map((field, index) => field.hidden === true ? -1 : index)
      .filter(index => index >= 0)
    const labelFields = visibleDimensionIndexes.map(index => configuredDimensionFields[index])
    const visibleQuotaFields = quotaFields.filter(field => {
      const configuredField = findConfiguredField(configuredFields, field)
      return configuredField?.hidden !== true
    })
    if (!visibleQuotaFields.length) {
      return dimensionTuples.map(tuple => {
        const labels = visibleDimensionIndexes.map(index => tuple.values[index])
        return {
          key: this.axisKey(tuple.values),
          labels,
          labelFields,
          mergeKeys: this.buildMergeKeys(labels)
        }
      })
    }

    const tuples = dimensionTuples.length
      ? dimensionTuples
      : [{ values: [] as unknown[] }]
    const visibleConfiguredFields = configuredFields.filter(field => field.hidden !== true)
    const quotaFirst = visibleConfiguredFields[0]?.groupType === 'q'
    const entries: AxisEntry[] = []

    if (quotaFirst) {
      for (const quota of visibleQuotaFields) {
        for (const tuple of tuples) {
          const dimensionLabels = visibleDimensionIndexes.map(index => tuple.values[index])
          const labels = [this.fieldLabel(quota), ...dimensionLabels]
          const mergeValues = [{ quotaId: String(quota.id) }, ...dimensionLabels]
          entries.push({
            key: this.axisKey(tuple.values, quota),
            labels,
            labelFields: [undefined, ...labelFields],
            mergeKeys: this.buildMergeKeys(mergeValues),
            metric: quota
          })
        }
      }
      return entries
    }

    for (const tuple of tuples) {
      for (const quota of visibleQuotaFields) {
        const dimensionLabels = visibleDimensionIndexes.map(index => tuple.values[index])
        const labels = [...dimensionLabels, this.fieldLabel(quota)]
        const mergeValues = [...dimensionLabels, { quotaId: String(quota.id) }]
        entries.push({
          key: this.axisKey(tuple.values, quota),
          labels,
          labelFields: [...labelFields, undefined],
          mergeKeys: this.buildMergeKeys(mergeValues),
          metric: quota
        })
      }
    }
    return entries
  }

  private uniqueDimensionTuples(
    fields: PivotTableField[],
    records: Record<string, unknown>[]
  ): Array<{ values: unknown[] }> {
    if (!fields.length) {
      return []
    }
    const tuples = new Map<string, { values: unknown[] }>()
    for (const record of records) {
      const values = fields.map(field => record[field.dataeaseName || ''])
      const key = this.axisKey(values)
      if (!tuples.has(key)) {
        tuples.set(key, { values })
      }
    }
    return Array.from(tuples.values())
  }

  private fillAxisHeaders(
    values: any[][],
    rowEntries: AxisEntry[],
    columnEntries: AxisEntry[],
    topRows: number,
    leftColumns: number
  ): void {
    rowEntries.forEach((entry, rowIndex) => {
      entry.labels.forEach((label, level) => {
        values[topRows + rowIndex][level] = this.displayValue(
          label,
          entry.labelFields[level]
        )
      })
    })

    columnEntries.forEach((entry, columnIndex) => {
      entry.labels.forEach((label, level) => {
        values[level][leftColumns + columnIndex] = this.displayValue(
          label,
          entry.labelFields[level]
        )
      })
    })
  }

  private fillAxisFieldHeaders(
    values: any[][],
    rowFieldLabels: string[],
    columnFieldLabels: string[],
    topRows: number,
    leftColumns: number
  ): void {
    if (rowFieldLabels.length && columnFieldLabels.length && topRows > 0 && leftColumns > 0) {
      const rowHeaderRow = topRows - 1
      rowFieldLabels.forEach((label, index) => {
        if (index < leftColumns) {
          values[rowHeaderRow][index] = label
        }
      })

      const columnHeaderColumn = leftColumns - 1
      columnFieldLabels.forEach((label, index) => {
        if (index < topRows && index !== rowHeaderRow) {
          values[index][columnHeaderColumn] = label
        }
      })
      return
    }

    if (rowFieldLabels.length && topRows > 0) {
      rowFieldLabels.forEach((label, index) => {
        if (index < leftColumns) {
          values[0][index] = label
        }
      })
    }

    if (columnFieldLabels.length && leftColumns > 0) {
      columnFieldLabels.forEach((label, index) => {
        if (index < topRows) {
          values[index][0] = label
        }
      })
    }
  }

  private fillSingleAxisLabels(
    values: any[][],
    rowFieldLabels: string[],
    hasColumnFields: boolean,
    quotaInRows: boolean,
    topRows: number,
    leftColumns: number,
    dataColumns: number
  ): void {
    if (rowFieldLabels.length && !hasColumnFields && topRows > 0) {
      rowFieldLabels.forEach((field, index) => {
        if (index < leftColumns) {
          values[0][index] = field
        }
      })
      if (quotaInRows && dataColumns > 0) {
        values[0][leftColumns] = '值'
      }
    }
  }

  private fillMetricValues(
    values: any[][],
    displayScales: number[][],
    config: PivotTableConfig,
    result: PivotTableQueryResult,
    rowEntries: AxisEntry[],
    columnEntries: AxisEntry[],
    topRows: number,
    leftColumns: number,
    quotaInRows: boolean,
    quotaInColumns: boolean
  ): void {
    const rowDimensions = result.data.rowFields || []
    const columnDimensions = result.data.columnFields || []
    const rowIndex = new Map(rowEntries.map((entry, index) => [entry.key, index]))
    const columnIndex = new Map(columnEntries.map((entry, index) => [entry.key, index]))
    const configuredFields = [
      ...config.data.zones.rows,
      ...config.data.zones.columns
    ]

    for (const record of result.data.rowData || []) {
      const rowValues = rowDimensions.map(field => record[field.dataeaseName || ''])
      const columnValues = columnDimensions.map(field => record[field.dataeaseName || ''])

      for (const quota of result.data.quotaFields || []) {
        const targetRow = rowEntries.length
          ? rowIndex.get(this.axisKey(rowValues, quotaInRows ? quota : undefined))
          : 0
        const targetColumn = columnEntries.length
          ? columnIndex.get(this.axisKey(columnValues, quotaInColumns ? quota : undefined))
          : 0
        if (targetRow == null || targetColumn == null) {
          continue
        }

        const configuredQuota = findConfiguredField(configuredFields, quota)
        if (configuredQuota?.hidden === true) {
          continue
        }
        const rawValue = record[quota.dataeaseName || '']
        const nativeValue = toNativeCellValue(rawValue, configuredQuota)
        const numberFormat = getFieldNumberFormat(configuredQuota, nativeValue)
        displayScales[topRows + targetRow][leftColumns + targetColumn] =
          getFieldDisplayScale(configuredQuota)
        values[topRows + targetRow][leftColumns + targetColumn] =
          numberFormat && nativeValue !== ''
            ? {
                v: nativeValue,
                s: {
                  n: {
                    pattern: numberFormat
                  }
                }
              }
            : nativeValue
      }
    }
  }

  private axisDisplayFields(fields: PivotTableField[]): string[] {
    const labels: string[] = []
    let quotaAdded = false
    for (const field of fields) {
      if (field.hidden === true) {
        continue
      }
      if (field.groupType === 'q') {
        if (!quotaAdded) {
          labels.push('指标')
          quotaAdded = true
        }
      } else {
        labels.push(this.fieldLabel(field))
      }
    }
    return labels
  }

  private axisDimensionLabels(fields: PivotTableField[]): string[] {
    return fields
      .filter(field => field.groupType !== 'q' && field.hidden !== true)
      .map(field => this.fieldLabel(field))
  }

  private buildAxisHeaderValueRegions(
    values: any[][],
    rowCount: number,
    columnCount: number,
    topRows: number,
    leftColumns: number
  ): PivotTableValueRegion[] {
    const regions: PivotTableValueRegion[] = []

    if (topRows > 0 && leftColumns < columnCount) {
      regions.push(this.buildValueRegion(values, {
        startRow: 0,
        endRow: topRows - 1,
        startColumn: leftColumns,
        endColumn: columnCount - 1
      }))
    }

    if (leftColumns > 0 && topRows < rowCount) {
      regions.push(this.buildValueRegion(values, {
        startRow: topRows,
        endRow: rowCount - 1,
        startColumn: 0,
        endColumn: leftColumns - 1
      }))
    }

    return regions
  }

  private buildValueRegion(
    values: any[][],
    range: PivotLayoutRange
  ): PivotTableValueRegion {
    const regionValues = values
      .slice(range.startRow, range.endRow + 1)
      .map(row => row.slice(range.startColumn, range.endColumn + 1))
    return { range, values: regionValues }
  }

  private buildCornerLayout(
    values: any[][],
    rowFields: PivotTableField[],
    columnFields: PivotTableField[],
    rowFieldLabels: string[],
    columnFieldLabels: string[],
    hasVisibleQuota: boolean,
    topRows: number,
    leftColumns: number
  ): PivotTableCornerLayout | undefined {
    if (topRows <= 0 || leftColumns <= 0) {
      return undefined
    }

    // 默认字段名矩阵作为关闭斜线表头时的恢复源，不能依赖合并后的单元格值。
    const range: PivotLayoutRange = {
      startRow: 0,
      endRow: topRows - 1,
      startColumn: 0,
      endColumn: leftColumns - 1
    }
    const cornerValues = this.buildValueRegion(values, range).values
    const rowDimensionLabels = this.axisDimensionLabels(rowFields)
    const columnDimensionLabels = this.axisDimensionLabels(columnFields)

    return {
      range,
      values: cornerValues,
      twoParts: [rowFieldLabels.join('/'), columnFieldLabels.join('/')],
      threeParts: [
        rowDimensionLabels.join('/'),
        columnDimensionLabels.join('/'),
        hasVisibleQuota ? '指标' : ''
      ]
    }
  }

  private buildMergeKeys(values: unknown[]): string[] {
    // 每一级都带上之前的可见父层级，避免跨可见父层级合并。
    return values.map((_, index) => JSON.stringify(
      values.slice(0, index + 1).map(value => value == null ? null : value)
    ))
  }

  private axisKey(values: unknown[], quota?: PivotTableField): string {
    return JSON.stringify([
      ...values.map(value => value == null ? null : value),
      quota ? String(quota.id) : null
    ])
  }

  private fieldLabel(field: PivotTableField): string {
    return field.chartShowName || field.name
  }

  private displayValue(value: unknown, field?: FieldItemData): unknown {
    if (value == null) {
      return ''
    }

    // 轴定位与合并继续使用原始值，仅在写入单元格时转换日期等展示值。
    const nativeValue = toNativeCellValue(value, field)
    const numberFormat = getFieldNumberFormat(field, nativeValue)
    if (!numberFormat || nativeValue === '') {
      return nativeValue
    }

    return {
      v: nativeValue,
      s: {
        n: {
          pattern: numberFormat
        }
      }
    }
  }

  private buildRowHeaderMerges(entries: AxisEntry[], topRows: number): PivotLayoutRange[] {
    if (entries.length <= 1) {
      return []
    }

    const depth = entries[0]?.labels.length || 0
    const merges: PivotLayoutRange[] = []
    let groups = [{ start: 0, end: entries.length - 1 }]

    for (let level = 0; level < depth; level++) {
      const nextGroups: typeof groups = []

      for (const group of groups) {
        let runStart = group.start
        for (let index = group.start + 1; index <= group.end + 1; index++) {
          const current = index <= group.end ? entries[index]?.mergeKeys[level] : undefined
          const previous = entries[index - 1]?.mergeKeys[level]

          if (index <= group.end && current === previous) {
            continue
          }

          nextGroups.push({ start: runStart, end: index - 1 })
          if (index - runStart > 1 && previous !== '') {
            merges.push({
              startRow: topRows + runStart,
              endRow: topRows + index - 1,
              startColumn: level,
              endColumn: level
            })
          }
          runStart = index
        }
      }

      groups = nextGroups
    }

    return merges
  }

  private buildColumnHeaderMerges(entries: AxisEntry[], leftColumns: number): PivotLayoutRange[] {
    if (entries.length <= 1) {
      return []
    }

    const depth = entries[0]?.labels.length || 0
    const merges: PivotLayoutRange[] = []
    let groups = [{ start: 0, end: entries.length - 1 }]

    for (let level = 0; level < depth; level++) {
      const nextGroups: typeof groups = []

      for (const group of groups) {
        let runStart = group.start
        for (let index = group.start + 1; index <= group.end + 1; index++) {
          const current = index <= group.end ? entries[index]?.mergeKeys[level] : undefined
          const previous = entries[index - 1]?.mergeKeys[level]

          if (index <= group.end && current === previous) {
            continue
          }

          nextGroups.push({ start: runStart, end: index - 1 })
          if (index - runStart > 1 && previous !== '') {
            merges.push({
              startRow: level,
              endRow: level,
              startColumn: leftColumns + runStart,
              endColumn: leftColumns + index - 1
            })
          }
          runStart = index
        }
      }

      groups = nextGroups
    }

    return merges
  }
}
