import { copyChartField, getFieldByDQ } from '@/api/chart'
import { useI18n } from '@/hooks/web/useI18n'
import { guid } from '@/views/visualized/data/dataset/form/util'
import type { FieldItemData, TablePluginConfig } from '../../../types/plugin'
import type { DatasetMapping, ReplacementField } from '../types'
import { getDatasetMapping } from './replacement-lookup'

export const replaceCopiedFields = async (
  config: TablePluginConfig,
  mappings: DatasetMapping[]
): Promise<{ fieldOwnerId?: string; fields: Map<string, ReplacementField> }> => {
  const fields = new Map<string, ReplacementField>()
  const mapping = getDatasetMapping(mappings, config.data.datasetId)
  if (!config.fieldOwnerId || !mapping?.target) return { fields }

  const copiedMappings = mapping.fields.filter(({ source }) =>
    String(source.chartId) === config.fieldOwnerId && source.componentIds.includes(config.id)
  )
  if (!copiedMappings.length) return { fields }

  const { t } = useI18n()
  // 替换数据集时重建被引用的副本，旧归属留给已保存版本；失败产生的孤立字段暂不回收。
  const fieldOwnerId = guid()
  const copiedIds = new Set<string>()
  for (const { source, target } of copiedMappings) {
    if (!target) throw new Error(t('spreadsheet.dataset_replacement.incomplete_warning'))
    await copyChartField(target.id, fieldOwnerId)
    const result = await getFieldByDQ(mapping.target.id, fieldOwnerId, {}) as unknown as {
      dimensionList: FieldItemData[]
      quotaList: FieldItemData[]
    }
    const copied = [...(result.dimensionList || []), ...(result.quotaList || [])].find(field =>
      String(field.chartId) === fieldOwnerId &&
      field.originName === `[${target.id}]` &&
      !copiedIds.has(String(field.id))
    )
    if (!copied) throw new Error(t('spreadsheet.dataset_replacement.failed'))
    copiedIds.add(String(copied.id))
    fields.set(source.fieldId, { ...copied, id: String(copied.id) })
  }
  return { fieldOwnerId, fields }
}
