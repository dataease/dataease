import type { IWorkbookData } from '@univerjs/core'
import { useI18n } from '@/hooks/web/useI18n'
import type { BusiTreeNode } from '@/models/tree/TreeNode'
import { findById, tree, SpreadsheetPublishStatus } from '@/views/menu/spreadsheet/api'
import { loadSpreadsheetBindingContext } from '@/views/menu/spreadsheet/utils/binding-context'
import type {
  SpreadsheetFilterConfig,
  TablePluginConfig
} from '@/views/menu/spreadsheet/types/plugin'
import {
  extractPluginInstancesFromWorkbook,
  FILTER_PLUGIN_RESOURCE_NAME
} from '@/views/menu/spreadsheet/utils/plugin-resource'

export interface SpreadsheetJumpTreeNode extends BusiTreeNode {
  status?: SpreadsheetPublishStatus
  disabled: boolean
  children?: SpreadsheetJumpTreeNode[]
}

export interface SpreadsheetJumpTarget {
  id: string
  title: string
  type: 'table-info' | 'table-pivot' | 'filter'
  disabled: boolean
  fields: Array<{ id: string; name: string; deType?: number }>
}

export const loadSpreadsheetJumpTree = async (): Promise<SpreadsheetJumpTreeNode[]> => {
  const nodes = await tree({ busiFlag: 'spreadsheet' })
  const filterPublished = (items: BusiTreeNode[]): SpreadsheetJumpTreeNode[] => {
    const result: SpreadsheetJumpTreeNode[] = []
    for (const item of items) {
      const node = item as SpreadsheetJumpTreeNode
      const status = node.status ?? node.extraFlag1 ?? SpreadsheetPublishStatus.Unpublished
      // 发布后有未发布修改的资源仍可跳转到正式版本。
      if (node.leaf && status === SpreadsheetPublishStatus.Unpublished) continue
      const children = node.children ? filterPublished(node.children) : undefined
      if (!node.leaf && !children?.length) continue
      result.push({
        ...node,
        disabled: !node.weight,
        children
      })
    }
    return result
  }
  return filterPublished(nodes as BusiTreeNode[])
}

export const loadSpreadsheetJumpTargets = async (
  id: string | number
): Promise<SpreadsheetJumpTarget[]> => {
  const { t } = useI18n()
  const spreadsheet = await findById(id)
  if (!spreadsheet || !spreadsheet.sheetData) {
    throw new Error(t('spreadsheet.load_error'))
  }
  if (
    (spreadsheet.status ?? SpreadsheetPublishStatus.Unpublished) ===
    SpreadsheetPublishStatus.Unpublished
  ) {
    throw new Error(t('spreadsheet.unpublished'))
  }
  const workbook = JSON.parse(spreadsheet.sheetData) as IWorkbookData
  const context = await loadSpreadsheetBindingContext(workbook, true)
  const regionIds = new Set(
    extractPluginInstancesFromWorkbook(workbook)
      .filter((plugin): plugin is TablePluginConfig => ['detail', 'pivot'].includes(plugin.type))
      .filter(plugin => !!workbook.sheets?.[plugin.placement?.sheetId])
      .map(plugin => plugin.id)
  )
  const selectableConditions = new Set<string>()
  for (const resource of workbook.resources || []) {
    if (resource.name !== FILTER_PLUGIN_RESOURCE_NAME || !resource.data) continue
    const config = JSON.parse(resource.data) as SpreadsheetFilterConfig
    if (!config.visible) continue
    for (const condition of config.conditions) {
      const linkedFields =
        condition.displayType === 'treeSelect'
          ? (condition.treeLevelMappings || []).flatMap(mapping => mapping.linkedFields || [])
          : condition.linkedFields || []
      // 隐藏查询栏或已失效的关联不能让跳转悄悄退化成无条件查询。
      const valid =
        linkedFields.length > 0 &&
        linkedFields.every(field => {
          const area = context.dataAreas.find(item => item.id === field.pluginId)
          return (
            area &&
            regionIds.has(area.id) &&
            (field.datasetId == null || String(field.datasetId) === area.datasetId) &&
            area.fields.some(item => item.id === String(field.fieldId))
          )
        })
      if (valid) selectableConditions.add(condition.id)
    }
  }
  // 跳转直接绑定已发布的数据区域及查询条件，不要求目标先配置外参。
  const targets: SpreadsheetJumpTarget[] = [
    ...context.dataAreas.map<SpreadsheetJumpTarget>(area => ({
      id: area.id,
      title: area.name,
      type: area.pluginType === 'detail' ? 'table-info' : 'table-pivot',
      disabled: !regionIds.has(area.id) || !area.fields.length,
      fields: area.fields
    })),
    ...context.conditions.map(condition => ({
      id: condition.id,
      title: condition.name,
      type: 'filter' as const,
      disabled: !selectableConditions.has(condition.id),
      fields: [{ id: '1000001', name: t('visualization.filter_no_select') }]
    }))
  ]
  if (new Set(targets.map(target => target.id)).size !== targets.length) {
    throw new Error(t('spreadsheet.binding_ambiguous_targets'))
  }
  return targets
}
