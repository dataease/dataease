import { computed, ref, shallowRef } from 'vue'
import { getDsDetailsWithPerm } from '@/api/dataset'
import type { Field } from '@/api/chart'
import type {
  DatasetIdentity,
  DatasetMapping,
  DatasetReplacementScope,
  ReplacementDraft,
  ReplacementField,
  ReplacementResult
} from '../../plugins/DataEaseDatasetReplacementPlugin/types'
import { SpreadsheetDatasetReplacementService } from '../../plugins/DataEaseDatasetReplacementPlugin/services/spreadsheet-dataset-replacement.service'

const normalizeField = (field: Field): ReplacementField => ({
  ...field,
  id: String(field.id),
  name: field.name,
  dataeaseName: (field as any).dataeaseName,
  groupType: field.groupType === 'q' ? 'q' : 'd',
  deType: field.deType,
  type: (field as any).type
})

const loadDataset = async (datasetId: string): Promise<{
  dataset: DatasetIdentity
  fields: ReplacementField[]
}> => {
  const response = await getDsDetailsWithPerm([datasetId])
  const detail = response?.[0] as any
  if (!detail) {
    throw new Error(`Dataset ${datasetId} is unavailable`)
  }
  const fields = detail.fields || {}
  return {
    dataset: {
      id: datasetId,
      name: detail.name || detail.tableName || datasetId
    },
    fields: [
      ...(fields.dimensionList || []),
      ...(fields.quotaList || [])
    ].map(normalizeField)
  }
}

export const useDatasetReplacementDraft = () => {
  const loading = ref(false)
  const submitting = ref(false)
  const error = ref('')
  const draft = ref<ReplacementDraft>()
  const service = shallowRef<SpreadsheetDatasetReplacementService>()
  const unitId = ref('')

  const complete = computed(() =>
    Boolean(draft.value && service.value?.isComplete(draft.value))
  )

  const initialize = async (
    univerInstance: any,
    univerApi: any,
    scope: DatasetReplacementScope,
    componentId?: string
  ): Promise<void> => {
    loading.value = true
    error.value = ''
    draft.value = undefined
    try {
      const injector = univerInstance?.univer?.__getInjector?.()
      const workbook = univerApi?.getActiveWorkbook?.()
      unitId.value = String(workbook?.getId?.() || workbook?.getUnitId?.() || '')
      if (!injector || !unitId.value) {
        throw new Error('Spreadsheet replacement service is not ready')
      }

      service.value = injector.get(SpreadsheetDatasetReplacementService)
      const usages = service.value.collectUsage(unitId.value, componentId)
      const hydratedUsages = await Promise.all(usages.map(async usage => {
        const detail = await loadDataset(usage.dataset.id)
        return service.value!.hydrateUsage(usage, detail.dataset, detail.fields)
      }))
      draft.value = service.value.createDraft(scope, hydratedUsages, componentId)
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  const selectTargetDataset = (
    mapping: DatasetMapping,
    dataset: DatasetIdentity,
    fields: ReplacementField[]
  ): void => {
    service.value?.setTargetDataset(mapping, dataset, fields)
  }

  const selectTargetField = (
    mapping: DatasetMapping,
    fieldKey: string,
    target: ReplacementField
  ): void => {
    service.value?.setTargetField(mapping, fieldKey, target)
  }

  const clearTargetDataset = (mapping: DatasetMapping): void => {
    service.value?.clearTargetDataset(mapping)
  }

  const submit = async (): Promise<ReplacementResult> => {
    if (!service.value || !draft.value || !unitId.value) {
      throw new Error('Spreadsheet replacement service is not ready')
    }
    submitting.value = true
    try {
      return await service.value.replace(unitId.value, draft.value)
    } finally {
      submitting.value = false
    }
  }

  return {
    loading,
    submitting,
    error,
    draft,
    complete,
    initialize,
    selectTargetDataset,
    selectTargetField,
    clearTargetDataset,
    submit
  }
}
