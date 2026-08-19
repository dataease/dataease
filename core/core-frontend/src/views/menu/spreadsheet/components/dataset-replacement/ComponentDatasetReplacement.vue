<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import Icon from '@/components/icon-custom/src/Icon.vue'
import icon_dataset_outlined from '@/assets/svg/icon_dataset_outlined.svg'
import type {
  DatasetIdentity,
  DatasetMapping,
  ReplacementField
} from '../../plugins/DataEaseDatasetReplacementPlugin/types'
import ReplacementDatasetSelect from './ReplacementDatasetSelect.vue'
import DatasetReplacementFieldMapping from './DatasetReplacementFieldMapping.vue'
import { useDatasetReplacementOutsideClick } from './useDatasetReplacementOutsideClick'

const props = defineProps<{
  mapping: DatasetMapping
}>()

const emit = defineEmits<{
  selectDataset: [
    mapping: DatasetMapping,
    dataset: DatasetIdentity,
    fields: ReplacementField[]
  ]
  selectField: [
    mapping: DatasetMapping,
    fieldKey: string,
    target: ReplacementField
  ]
  clearDataset: [mapping: DatasetMapping]
}>()

const { t } = useI18n()
const mappingExpanded = ref(false)
const replacementRef = ref<HTMLElement>()
const replacementDatasetSelectRef = ref<InstanceType<typeof ReplacementDatasetSelect>>()
const unmatchedCount = computed(() =>
  props.mapping.fields.filter(field => !field.target).length
)

const openMapping = () => {
  mappingExpanded.value = true
}

useDatasetReplacementOutsideClick(
  () => replacementRef.value,
  () => {
    mappingExpanded.value = false
  }
)

const openTargetDataset = () => {
  replacementDatasetSelectRef.value?.open()
}

const handleDatasetSelected = (
  dataset: DatasetIdentity,
  fields: ReplacementField[]
) => {
  mappingExpanded.value = true
  emit('selectDataset', props.mapping, dataset, fields)
}

const handleDatasetClear = () => {
  mappingExpanded.value = false
  emit('clearDataset', props.mapping)
}
</script>

<template>
  <div ref="replacementRef" class="component-dataset-replacement">
    <div class="dataset-labels">
      <span>{{ t('spreadsheet.dataset_replacement.current_component_dataset') }}</span>
      <span>{{ t('spreadsheet.dataset_replacement.replace_to_dataset') }}</span>
    </div>
    <div class="dataset-comparison">
      <button type="button" class="source-dataset" @click="openMapping">
        <el-icon>
          <Icon name="icon_dataset_outlined">
            <icon_dataset_outlined />
          </Icon>
        </el-icon>
        <span>{{ mapping.source.dataset.name }}</span>
      </button>
      <span class="comparison-line"></span>
      <div class="target-dataset" @click="openTargetDataset">
        <ReplacementDatasetSelect
          ref="replacementDatasetSelectRef"
          :model-value="mapping.target?.id"
          :popover-width="348"
          clearable
          @selected="handleDatasetSelected"
          @clear="handleDatasetClear"
        />
      </div>
      <span v-if="mapping.target && unmatchedCount" class="unmatched-status">
        <span class="status-icon">!</span>
        {{ unmatchedCount }}
      </span>
    </div>

    <Transition name="mapping-expand">
      <DatasetReplacementFieldMapping
        v-if="mappingExpanded"
        variant="component"
        :mapping="mapping"
        @select-field="(fieldKey, target) => emit('selectField', mapping, fieldKey, target)"
      />
    </Transition>
  </div>
</template>

<style scoped lang="less">
.component-dataset-replacement {
  position: relative;
  padding-top: 4px;
}

.dataset-labels,
.dataset-comparison {
  display: grid;
  grid-template-columns: 348px 44px 348px 48px;
  align-items: center;
}

.dataset-labels {
  margin-bottom: 8px;
  color: #646a73;
  font-size: 14px;
  line-height: 22px;

  span:first-child {
    grid-column: 1;
  }

  span:last-child {
    grid-column: 3;
  }
}

.source-dataset {
  display: flex;
  align-items: center;
  width: 348px;
  height: 32px;
  padding: 0 12px;
  overflow: hidden;
  color: #1f2329;
  background: #f5f6f7;
  border: 0;
  border-radius: 4px;
  cursor: pointer;
  font: inherit;
  text-align: left;
  gap: 8px;

  .ed-icon {
    flex: 0 0 16px;
    width: 16px;
    height: 16px;
    color: #0fbefe;

    svg {
      width: 16px;
      height: 16px;
    }
  }

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.comparison-line {
  height: 1px;
  margin: 0 6px;
  border-top: 1px dashed #dee0e3;
}

.target-dataset {
  width: 348px;
  min-width: 0;
  cursor: pointer;
}

.unmatched-status {
  display: inline-flex;
  align-items: center;
  margin-left: 6px;
  color: #646a73;
  font-size: 14px;
  gap: 4px;
}

.dataset-comparison {
  box-sizing: border-box;
  width: 100%;
  padding: 4px 0 4px 4px;
  border-radius: 6px;

  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.status-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  color: #fff;
  background: #f54a45;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
}

.mapping-expand-enter-active,
.mapping-expand-leave-active {
  transform-origin: top center;
  transition:
    opacity 160ms ease-out,
    transform 160ms ease-out;
}

.mapping-expand-enter-from,
.mapping-expand-leave-to {
  opacity: 0;
  transform: translateY(-8px) scaleY(0.98);
}
</style>
