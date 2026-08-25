<script setup lang="ts">
import { ref } from 'vue'
import type { Field } from '@/api/chart'
import { useI18n } from '@/hooks/web/useI18n'
import DatasetSelect from '../dataset-panel/dataset-select.vue'
import type {
  DatasetIdentity,
  ReplacementField
} from '../../plugins/DataEaseDatasetReplacementPlugin/types'

const props = withDefaults(defineProps<{
  modelValue?: string
  popoverWidth?: number
  clearable?: boolean
  showDatasetIcon?: boolean
}>(), {
  popoverWidth: 280,
  clearable: false,
  showDatasetIcon: false
})

const emit = defineEmits<{
  selected: [dataset: DatasetIdentity, fields: ReplacementField[]]
  clear: []
}>()

const { t } = useI18n()
const selectedId = ref<string | number>('')
const selectedNode = ref<any>()
const datasetSelectRef = ref<InstanceType<typeof DatasetSelect>>()

const handleDatasetChange = (datasetId: string | number) => {
  selectedId.value = datasetId
}

const handleFieldsLoaded = (payload: { dimensions: Field[]; quotas: Field[] }) => {
  const id = String(selectedId.value)
  if (!id) return
  const fields = [...payload.dimensions, ...payload.quotas].map(field => ({
    ...field,
    id: String(field.id),
    name: field.name,
    dataeaseName: (field as any).dataeaseName,
    groupType: field.groupType === 'q' ? 'q' as const : 'd' as const,
    deType: field.deType,
    type: (field as any).type
  }))
  emit('selected', {
    id,
    name: selectedNode.value?.name || id
  }, fields)
}

const open = () => {
  datasetSelectRef.value?.open()
}

const handleClear = () => {
  selectedId.value = ''
  selectedNode.value = undefined
  emit('clear')
}

defineExpose({ open })
</script>

<template>
  <DatasetSelect
    ref="datasetSelectRef"
    :model-value="modelValue"
    :popover-width="props.popoverWidth"
    :clearable="props.clearable"
    :show-dataset-icon="props.showDatasetIcon"
    :show-create-dataset="false"
    :clear-confirm-text="t('spreadsheet.dataset_replacement.clear_matches_confirm')"
    @dataset-change="handleDatasetChange"
    @dataset-node-change="selectedNode = $event"
    @fields-loaded="handleFieldsLoaded"
    @clear="handleClear"
  />
</template>
