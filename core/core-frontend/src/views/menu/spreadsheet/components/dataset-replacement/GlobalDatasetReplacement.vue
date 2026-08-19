<script setup lang="ts">
import { ref, type ComponentPublicInstance } from 'vue'
import { CircleCheckFilled, Filter, WarningFilled } from '@element-plus/icons-vue'
import { useI18n } from '@/hooks/web/useI18n'
import iconDashboardOutlined from '@/assets/svg/icon_dashboard_outlined.svg'
import iconDatasetOutlined from '@/assets/svg/icon_dataset_outlined.svg'
import type {
  DatasetIdentity,
  DatasetMapping,
  ReplacementField
} from '../../plugins/DataEaseDatasetReplacementPlugin/types'
import DatasetReplacementFieldMapping from './DatasetReplacementFieldMapping.vue'
import ReplacementDatasetSelect from './ReplacementDatasetSelect.vue'
import { useDatasetReplacementOutsideClick } from './useDatasetReplacementOutsideClick'

defineProps<{
  mappings: DatasetMapping[]
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
const expanded = ref<string>()
const itemElements = new Map<string, HTMLElement>()

const isExpanded = (mapping: DatasetMapping) =>
  expanded.value === mapping.source.dataset.id

const openMapping = (mapping: DatasetMapping) => {
  expanded.value = mapping.source.dataset.id
}

const setItemElement = (
  datasetId: string,
  element: Element | ComponentPublicInstance | null
) => {
  if (element instanceof HTMLElement) {
    itemElements.set(datasetId, element)
  } else {
    itemElements.delete(datasetId)
  }
}

useDatasetReplacementOutsideClick(
  () => (expanded.value ? itemElements.get(expanded.value) : undefined),
  () => {
    expanded.value = undefined
  }
)

const unmatchedCount = (mapping: DatasetMapping) =>
  mapping.fields.filter(field => !field.target).length

const handleDatasetSelected = (
  mapping: DatasetMapping,
  dataset: DatasetIdentity,
  fields: ReplacementField[]
) => {
  expanded.value = mapping.source.dataset.id
  emit('selectDataset', mapping, dataset, fields)
}

const handleDatasetClear = (mapping: DatasetMapping) => {
  if (isExpanded(mapping)) {
    expanded.value = undefined
  }
  emit('clearDataset', mapping)
}
</script>

<template>
  <div class="global-dataset-replacement">
    <div class="global-list-header">
      <span>
        {{
          t('spreadsheet.dataset_replacement.used_dataset_count', {
            count: mappings.length
          })
        }}
      </span>
      <span>{{ t('spreadsheet.dataset_replacement.replace_to_dataset') }}</span>
    </div>

    <div
      v-for="mapping in mappings"
      :key="mapping.source.dataset.id"
      :ref="element => setItemElement(mapping.source.dataset.id, element)"
      class="global-dataset-item"
    >
      <div class="global-dataset-row">
        <button
          type="button"
          class="source-dataset"
          :title="mapping.source.dataset.name"
          @click="openMapping(mapping)"
        >
          <el-icon class="dataset-icon">
            <Icon>
              <iconDatasetOutlined class="svg-icon" />
            </Icon>
          </el-icon>
          <span class="dataset-name">{{ mapping.source.dataset.name }}</span>
          <el-tooltip
            placement="top"
            effect="light"
            popper-class="global-dataset-usage-tooltip"
          >
            <template #content>
              <div class="dataset-usage-list">
                <div
                  v-for="component in mapping.source.components"
                  :key="component.type === 'filter' ? 'filter' : component.id"
                  class="dataset-usage-item"
                >
                  <el-icon>
                    <Filter v-if="component.type === 'filter'" />
                    <Icon v-else>
                      <iconDashboardOutlined class="svg-icon" />
                    </Icon>
                  </el-icon>
                  <span>
                    {{
                      component.type === 'filter'
                        ? t('spreadsheet.dataset_replacement.filter_component')
                        : component.name
                    }}
                  </span>
                </div>
              </div>
            </template>
            <span class="component-count" @click.stop>
              <el-icon>
                <Icon>
                  <iconDashboardOutlined class="svg-icon" />
                </Icon>
              </el-icon>
              <span>{{ mapping.source.componentCount }}</span>
            </span>
          </el-tooltip>
        </button>

        <div class="dataset-connector"></div>

        <div class="target-dataset">
          <ReplacementDatasetSelect
            :model-value="mapping.target?.id"
            :popover-width="348"
            clearable
            show-dataset-icon
            @selected="(dataset, fields) => handleDatasetSelected(mapping, dataset, fields)"
            @clear="handleDatasetClear(mapping)"
          />
        </div>

        <div v-if="mapping.target" class="mapping-status">
          <template v-if="unmatchedCount(mapping)">
            <el-icon class="unmatched"><WarningFilled /></el-icon>
            <span class="unmatched">{{ unmatchedCount(mapping) }}</span>
          </template>
          <el-icon v-else class="matched"><CircleCheckFilled /></el-icon>
        </div>
      </div>

      <DatasetReplacementFieldMapping
        v-if="isExpanded(mapping)"
        :mapping="mapping"
        variant="global"
        @select-field="(fieldKey, target) => emit('selectField', mapping, fieldKey, target)"
      />
    </div>
  </div>
</template>

<style scoped lang="less">
.global-dataset-replacement {
  width: 100%;
}

.global-list-header,
.global-dataset-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 36px minmax(0, 1fr) 32px;
  column-gap: 4px;
  align-items: center;
}

.global-list-header {
  height: 22px;
  padding: 0 4px;
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

.global-dataset-item {
  position: relative;
  overflow: visible;
}

.global-dataset-row {
  box-sizing: border-box;
  min-height: 40px;
  padding: 4px;
  border-radius: 6px;
  transition: background-color 0.2s ease;

  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.source-dataset {
  min-width: 0;
  height: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border: 0;
  border-radius: 6px;
  color: #1f2329;
  background: #f5f6f7;
  cursor: pointer;
  font: inherit;
  text-align: left;
}

.dataset-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  color: #14c0ff;

  :deep(.svg-icon) {
    width: 16px;
    height: 16px;
  }
}

.dataset-name {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  color: #1f2329;
  font-size: 14px;
  line-height: 22px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.component-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  color: #646a73;
  font-size: 14px;
  line-height: 22px;

  .ed-icon,
  :deep(.svg-icon) {
    width: 16px;
    height: 16px;
  }

  :deep(path) {
    fill: currentColor;
  }
}

.dataset-connector {
  width: 36px;
  border-top: 1px dashed #d9dcdf;
}

.target-dataset {
  min-width: 0;

  :deep(.dataset-select-trigger) {
    border-color: #d9dcdf;
  }

  :deep(.trigger-text),
  :deep(.trigger-placeholder) {
    font-size: 14px !important;
    line-height: 22px;
  }
}

.mapping-status {
  display: flex;
  align-items: center;
  gap: 2px;
  padding-left: 4px;
  font-size: 14px;

  .unmatched {
    color: #f54a45;
  }

  .matched {
    color: #34c724;
  }
}

</style>

<style lang="less">
.global-dataset-usage-tooltip {
  max-width: 280px;
  padding: 8px 12px !important;
  border-color: #dee0e3 !important;

  .dataset-usage-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .dataset-usage-item {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #1f2329;
    font-size: 12px;
    line-height: 20px;

    .ed-icon,
    .svg-icon {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      color: #646a73;
    }

    path {
      fill: currentColor;
    }

    span {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
