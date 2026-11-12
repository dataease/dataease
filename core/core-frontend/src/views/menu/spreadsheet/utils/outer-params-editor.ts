import type { IWorkbookData } from '@univerjs/core'
import { getDsDetailsWithPerm, type DatasetDetail } from '@/api/dataset'
import { getFieldByDQ } from '@/api/chart'
import { useI18n } from '@/hooks/web/useI18n'
import type { SpreadsheetOuterParamsContext } from '../types/outer-params'
import type { SpreadsheetFilterConfig, TablePluginConfig } from '../types/plugin'
import { extractPluginInstancesFromWorkbook, FILTER_PLUGIN_RESOURCE_NAME } from './plugin-resource'

interface OuterParamsTableConfig extends TablePluginConfig {
  type: 'detail' | 'pivot'
  data: TablePluginConfig['data'] & {
    datasetName?: string
    dataset?: { name?: string }
  }
  style?: {
    base?: { customBlockName?: boolean; blockName?: string }
  }
}

const getDataAreaName = (workbook: IWorkbookData, plugin: OuterParamsTableConfig): string => {
  const sheetId = plugin.placement?.sheetId
  const sheet = sheetId ? workbook.sheets?.[sheetId] : undefined
  // 使用保存稿中的当前工作表名，不沿用数据区域创建时记录的旧名称。
  const sheetName = sheet?.name || plugin.placement?.sheetName || sheetId || 'Sheet'
  const defaultName = `${sheetName}!${plugin.placement?.startCell || 'A1'}`
  if (plugin.style?.base?.customBlockName) {
    return plugin.style.base.blockName || defaultName
  }
  return defaultName
}

export const loadSpreadsheetOuterParamsEditorContext = async (
  workbook: IWorkbookData
): Promise<SpreadsheetOuterParamsContext> => {
  const { t } = useI18n()
  // 查询栏资源保存的是单个配置对象，明细和透视资源才是实例数组。
  const conditions = (workbook.resources || [])
    .filter(resource => resource.name === FILTER_PLUGIN_RESOURCE_NAME && resource.data)
    .flatMap(resource => {
      const config = JSON.parse(resource.data) as SpreadsheetFilterConfig
      if (!config || !Array.isArray(config.conditions)) {
        throw new Error(t('spreadsheet.outer_params_load_error'))
      }
      return config.conditions
    })
  const plugins = extractPluginInstancesFromWorkbook(workbook).filter(
    (plugin): plugin is OuterParamsTableConfig =>
      plugin.type === 'detail' || plugin.type === 'pivot'
  )
  const conditionIds = new Set(conditions.map(condition => condition.id))
  const pluginIds = new Set(plugins.map(plugin => plugin.id))
  if (conditionIds.size !== conditions.length || pluginIds.size !== plugins.length) {
    throw new Error(t('spreadsheet.outer_params_ambiguous_targets'))
  }

  const datasetIds = plugins
    .map(plugin => plugin.data?.datasetId)
    .filter((id): id is string | number => id !== undefined && id !== null && id !== '')
    .map(String)
  const datasetMap = new Map<string, DatasetDetail>()
  if (datasetIds.length) {
    try {
      const details = await getDsDetailsWithPerm([...new Set(datasetIds)])
      details?.forEach(detail => datasetMap.set(String(detail.id), detail))
    } catch {
      // 字段读取失败仍保留数据区域，避免配置弹窗丢失已有绑定目标。
    }
  }

  const dataAreas = await Promise.all(
    plugins.map(async plugin => {
      const datasetId = plugin.data?.datasetId
      const dataset = datasetId == null ? undefined : datasetMap.get(String(datasetId))
      let datasetFields = dataset?.fields
      if (dataset && plugin.fieldOwnerId) {
        try {
          // 实例字段副本由后端继续校验列权限，不从工作簿中直接取字段候选。
          datasetFields = await getFieldByDQ(datasetId, plugin.fieldOwnerId, {})
        } catch {
          // 副本读取失败时保留已取得的公共字段，与查询栏配置保持一致。
        }
      }
      const fields = [...(datasetFields?.dimensionList || []), ...(datasetFields?.quotaList || [])]
        .filter(field => String(field.id) !== '-1' && !field.agg && !field.desensitized)
        .map(field => ({
          id: String(field.id),
          name: field.name || field.dataeaseName,
          deType: field.deType
        }))
      const datasetName =
        dataset?.name ||
        plugin.data?.datasetName ||
        plugin.data?.dataset?.name ||
        (datasetId ? `数据集 ${datasetId}` : '-')

      return {
        id: plugin.id,
        name: getDataAreaName(workbook, plugin),
        datasetId: datasetId == null ? undefined : String(datasetId),
        datasetName,
        pluginType: plugin.type,
        fields
      }
    })
  )

  return {
    conditions: conditions.map(condition => ({
      id: condition.id,
      name: condition.name,
      displayType: condition.displayType
    })),
    dataAreas
  }
}
