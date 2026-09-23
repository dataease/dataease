import type { IWorkbookData } from '@univerjs/core'
import { Inject } from '@univerjs/core'
import type { PluginSnapshotCleaner } from '../../../services/plugin-snapshot-cleaning.service'
import { DETAIL_TABLE_PLUGIN_RESOURCE_NAME } from '../../../utils/plugin-resource'
import type { DetailTableConfig } from '../types'
import { DetailTableDisplayStateService } from './detail-table-display-state.service'

interface WorkbookWithResources extends Partial<IWorkbookData> {
  resources?: Array<{
    name: string
    data: string
  }>
}

const parseCellAddress = (cellAddress: string) => {
  const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
  if (!match) {
    throw new Error(`Invalid cell address: ${cellAddress}`)
  }

  const colStr = match[1].toUpperCase()
  let col = 0
  for (let i = 0; i < colStr.length; i++) {
    col = col * 26 + (colStr.charCodeAt(i) - 64)
  }

  return {
    row: parseInt(match[2], 10) - 1,
    col: col - 1
  }
}

export class DetailTableSnapshotCleaner implements PluginSnapshotCleaner<DetailTableConfig> {
  readonly type = 'detail' as const

  constructor(
    @Inject(DetailTableDisplayStateService)
    private readonly displayStateService: DetailTableDisplayStateService
  ) {}

  async clean(snapshot: Partial<IWorkbookData>, plugin: DetailTableConfig): Promise<void> {
    const keepStyle = this.hasFields(plugin)
    if (!keepStyle) {
      this.removePluginResource(snapshot, plugin.id)
    }

    const state = this.displayStateService.get(plugin.id)
    if (!state) {
      return
    }

    const targetSheet = (snapshot.sheets || {})[state.sheetId] as Record<string, any> | undefined

    if (!targetSheet?.cellData) {
      return
    }

    if (state.rowCount <= 0 || state.colCount <= 0) {
      return
    }

    const startPos = parseCellAddress(state.startCell)
    const candidateStyleIds = new Set<string>()

    for (let row = 0; row < state.rowCount; row++) {
      const rowIndex = startPos.row + row
      const rowData = targetSheet.cellData[rowIndex]
      if (!rowData) {
        continue
      }

      for (let col = 0; col < state.colCount; col++) {
        const colIndex = startPos.col + col
        const cellData = rowData[colIndex]
        if (!cellData) {
          continue
        }

        if (typeof cellData.s === 'string') {
          candidateStyleIds.add(cellData.s)
        }
        const persistentStyle = this.removePluginNumberFormat(cellData.s, snapshot.styles)
        if (keepStyle && persistentStyle != null) {
          rowData[colIndex] = { s: persistentStyle }
        } else {
          delete rowData[colIndex]
        }
      }

      if (Object.keys(rowData).length === 0) {
        delete targetSheet.cellData[rowIndex]
      }
    }

    this.removeUnusedStyles(snapshot, candidateStyleIds)
  }

  private hasFields(plugin: DetailTableConfig): boolean {
    return Array.isArray(plugin.data?.zones?.fields) && plugin.data.zones.fields.length > 0
  }

  private removePluginNumberFormat(
    style: unknown,
    styles?: IWorkbookData['styles']
  ): unknown {
    if (style == null) {
      return undefined
    }

    const resolvedStyle =
      typeof style === 'string'
        ? styles?.[style]
        : style
    if (!resolvedStyle || typeof resolvedStyle !== 'object') {
      return undefined
    }

    const { n: _numberFormat, ...persistentStyle } = resolvedStyle
    return Object.keys(persistentStyle).length > 0 ? persistentStyle : undefined
  }

  private removeUnusedStyles(
    snapshot: Partial<IWorkbookData>,
    candidateStyleIds: Set<string>
  ): void {
    if (!snapshot.styles || candidateStyleIds.size === 0) {
      return
    }

    const referencedStyleIds = new Set<string>()
    this.collectStyleReferences(snapshot.sheets, candidateStyleIds, referencedStyleIds)
    this.collectStyleReferences(snapshot.defaultStyle, candidateStyleIds, referencedStyleIds)

    candidateStyleIds.forEach(styleId => {
      if (!referencedStyleIds.has(styleId)) {
        delete snapshot.styles?.[styleId]
      }
    })
  }

  private collectStyleReferences(
    value: unknown,
    candidateStyleIds: Set<string>,
    references: Set<string>
  ): void {
    if (typeof value === 'string') {
      if (candidateStyleIds.has(value)) {
        references.add(value)
      }
      return
    }
    if (!value || typeof value !== 'object') {
      return
    }
    Object.values(value).forEach(item =>
      this.collectStyleReferences(item, candidateStyleIds, references)
    )
  }

  private removePluginResource(snapshot: Partial<IWorkbookData>, pluginId: string): void {
    const resource = (snapshot as WorkbookWithResources).resources?.find(
      item => item.name === DETAIL_TABLE_PLUGIN_RESOURCE_NAME
    )
    if (!resource?.data) {
      return
    }

    try {
      const instances = JSON.parse(resource.data) as DetailTableConfig[]
      if (!Array.isArray(instances)) {
        return
      }

      resource.data = JSON.stringify(instances.filter(instance => instance.id !== pluginId))
    } catch (error) {
    }
  }
}
