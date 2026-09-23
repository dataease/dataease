import type { IWorkbookData } from '@univerjs/core'
import type { PluginConfig } from '../types/plugin'

export const DETAIL_TABLE_PLUGIN_RESOURCE_NAME = 'SHEET_DATAEASE_DETAIL_TABLE_PLUGIN' as const
export const PIVOT_TABLE_PLUGIN_RESOURCE_NAME = 'SHEET_DATAEASE_PIVOT_TABLE_PLUGIN' as const
export const FILTER_PLUGIN_RESOURCE_NAME = 'SHEET_DATAEASE_FILTER_PLUGIN' as const
export const SLASH_CELL_PLUGIN_RESOURCE_NAME = 'SHEET_DATAEASE_SLASH_CELL_PLUGIN' as const

interface WorkbookWithResources extends Partial<IWorkbookData> {
  resources?: Array<{
    id?: string
    name: string
    data: string
  }>
}

const PLUGIN_RESOURCE_NAME_MAP: Record<string, string> = {
  detail: DETAIL_TABLE_PLUGIN_RESOURCE_NAME,
  pivot: PIVOT_TABLE_PLUGIN_RESOURCE_NAME,
  filter: FILTER_PLUGIN_RESOURCE_NAME
}

const parsePluginResource = (data?: string): PluginConfig[] => {
  if (!data) {
    return []
  }

  try {
    const parsed = JSON.parse(data)
    return Array.isArray(parsed) ? parsed as PluginConfig[] : []
  } catch (error) {
    return []
  }
}

export const extractPluginInstancesFromWorkbook = (workbookData?: Partial<IWorkbookData>): PluginConfig[] => {
  const resources = (workbookData as WorkbookWithResources | undefined)?.resources || []

  return resources.flatMap(resource => {
    if (!Object.values(PLUGIN_RESOURCE_NAME_MAP).includes(resource.name)) {
      return []
    }

    return parsePluginResource(resource.data)
  })
}

export const withSpreadsheetPluginResource = (
  workbookData: Partial<IWorkbookData>,
  pluginInstances: PluginConfig[]
): Partial<IWorkbookData> => {
  const resourceNames = new Set(Object.values(PLUGIN_RESOURCE_NAME_MAP))
  const nextResources = (((workbookData as WorkbookWithResources).resources || []).filter(
    resource => !resourceNames.has(resource.name)
  ))

  const groupedPlugins = pluginInstances.reduce<Record<string, PluginConfig[]>>((acc, plugin) => {
    const key = plugin.type
    if (!acc[key]) {
      acc[key] = []
    }
    acc[key].push(plugin)
    return acc
  }, {})

  for (const [type, instances] of Object.entries(groupedPlugins)) {
    const resourceName = PLUGIN_RESOURCE_NAME_MAP[type]

    if (!resourceName || !instances.length) {
      continue
    }

    nextResources.push({
      name: resourceName,
      data: JSON.stringify(instances)
    })
  }

  return {
    ...workbookData,
    resources: nextResources
  }
}
