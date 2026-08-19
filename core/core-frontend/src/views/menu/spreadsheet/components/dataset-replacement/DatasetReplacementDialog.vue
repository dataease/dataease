<script setup lang="ts">
import { computed, watch } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type {
  DatasetIdentity,
  DatasetMapping,
  DatasetReplacementScope,
  ReplacementField
} from '../../plugins/DataEaseDatasetReplacementPlugin/types'
import GlobalDatasetReplacement from './GlobalDatasetReplacement.vue'
import ComponentDatasetReplacement from './ComponentDatasetReplacement.vue'
import { useDatasetReplacementDraft } from './useDatasetReplacementDraft'

const props = withDefaults(defineProps<{
  modelValue: boolean
  univerInstance?: any
  univerApi?: any
  scope?: DatasetReplacementScope
  componentId?: string
}>(), {
  scope: 'workbook'
})

const emit = defineEmits<{
  'update:modelValue': [visible: boolean]
  success: []
}>()

const { t } = useI18n()
const {
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
} = useDatasetReplacementDraft()

const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
const isComponentScope = computed(() => props.scope === 'component')
const dialogTitle = computed(() =>
  t(
    isComponentScope.value
      ? 'spreadsheet.dataset_replacement.component_title'
      : 'spreadsheet.dataset_replacement.global_title'
  )
)

watch(
  () => props.modelValue,
  value => {
    if (value) {
      initialize(
        props.univerInstance,
        props.univerApi,
        props.scope,
        props.componentId
      )
    }
  }
)

const handleSelectDataset = (
  mapping: DatasetMapping,
  dataset: DatasetIdentity,
  fields: ReplacementField[]
) => {
  selectTargetDataset(mapping, dataset, fields)
}

const handleSelectField = (
  mapping: DatasetMapping,
  fieldKey: string,
  target: ReplacementField
) => {
  selectTargetField(mapping, fieldKey, target)
}

const handleClearDataset = (mapping: DatasetMapping) => {
  clearTargetDataset(mapping)
}

const handleSubmit = async () => {
  if (!complete.value) {
    ElMessage.warning(t('spreadsheet.dataset_replacement.incomplete_warning'))
    return
  }
  try {
    const result = await submit()
    visible.value = false
    emit('success')
    if (result.refreshFailedComponentIds.length) {
      ElMessage.warning(
        t('spreadsheet.dataset_replacement.refresh_warning', {
          count: result.refreshFailedComponentIds.length
        })
      )
    } else {
      ElMessage.success(t('spreadsheet.dataset_replacement.success'))
    }
  } catch (e) {
    ElMessage.error(
      e instanceof Error ? e.message : t('spreadsheet.dataset_replacement.failed')
    )
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    width="840px"
    top="8vh"
    destroy-on-close
    :close-on-click-modal="false"
    :title="dialogTitle"
    :class="[
      'dataset-replacement-dialog',
      {
        'is-component': isComponentScope,
        'is-global': !isComponentScope
      }
    ]"
  >
    <div v-loading="loading" class="dialog-content">
      <el-alert
        v-if="error"
        type="error"
        :closable="false"
        :title="error"
      />
      <el-empty
        v-else-if="!loading && !draft?.mappings.length"
        :description="t('spreadsheet.dataset_replacement.no_used_dataset')"
      />
      <template v-else-if="draft">
        <ComponentDatasetReplacement
          v-if="isComponentScope && draft.mappings[0]"
          :mapping="draft.mappings[0]"
          @select-dataset="handleSelectDataset"
          @select-field="handleSelectField"
          @clear-dataset="handleClearDataset"
        />
        <GlobalDatasetReplacement
          v-else
          :mappings="draft.mappings"
          @select-dataset="handleSelectDataset"
          @select-field="handleSelectField"
          @clear-dataset="handleClearDataset"
        />
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">
        {{ t('common.cancel') }}
      </el-button>
      <el-button
        type="primary"
        :disabled="!complete"
        :loading="submitting"
        @click="handleSubmit"
      >
        {{ t('spreadsheet.dataset_replacement.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less">
.dataset-replacement-dialog {
  .ed-dialog__body {
    padding: 16px 24px 8px;
  }
  .dialog-content {
    min-height: 260px;
    max-height: 64vh;
    overflow: auto;
  }
}

.dataset-replacement-dialog.is-component {
  overflow: visible;

  .ed-dialog__body {
    overflow: visible;
  }

  .dialog-content {
    min-height: 96px;
    overflow: visible;
  }
}

.dataset-replacement-dialog.is-global {
  overflow: visible;
  border-radius: 12px;

  .ed-dialog__header {
    margin-right: 0;
    padding: 24px 24px 0;
  }

  .ed-dialog__title {
    color: #1f2329;
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
  }

  .ed-dialog__headerbtn {
    top: 24px;
    right: 24px;
    width: 20px;
    height: 20px;
  }

  .ed-dialog__body {
    padding: 16px 24px;
    overflow: visible;
  }

  .dialog-content {
    min-height: 0;
    max-height: none;
    overflow: visible;
  }

  .ed-dialog__footer {
    padding: 0 24px 24px;

    .ed-button {
      width: 80px;
      height: 32px;
      border-radius: 6px;
    }

  }
}
</style>
