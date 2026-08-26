<script setup lang="ts">
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useI18n } from '@/hooks/web/useI18n'
import type {
  DatasetMapping,
  FieldUsage,
  ReplacementField
} from '../../plugins/DataEaseDatasetReplacementPlugin/types'
import { filterCompatibleFields } from '../../plugins/DataEaseDatasetReplacementPlugin/utils/field-compatibility'
import { getSpreadsheetFieldIcon } from '../../utils/field-icon'

const props = defineProps<{
  mapping: DatasetMapping
  variant: 'component' | 'global'
}>()

const emit = defineEmits<{
  selectField: [fieldKey: string, target: ReplacementField]
}>()

const { t } = useI18n()
const keyword = ref('')
const targetKeywords = ref<Record<string, string>>({})

const visibleFields = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return props.mapping.fields
  return props.mapping.fields.filter(item =>
    item.source.name.toLowerCase().includes(text) ||
    item.target?.name.toLowerCase().includes(text)
  )
})

const selectTarget = (fieldKey: string, targetId: string) => {
  const target = props.mapping.targetFields.find(field => field.id === targetId)
  if (target) emit('selectField', fieldKey, target)
}

const getCompatibleTargetFields = (source: FieldUsage) =>
  filterCompatibleFields(source, props.mapping.targetFields)

const getVisibleTargetFields = (source: FieldUsage) => {
  const text = (targetKeywords.value[source.key] || '').trim().toLowerCase()
  const fields = getCompatibleTargetFields(source)
  if (!text) return fields
  return fields.filter(field => field.name.toLowerCase().includes(text))
}

const handleTargetVisibleChange = (fieldKey: string, visible: boolean) => {
  if (!visible) targetKeywords.value[fieldKey] = ''
}
</script>

<template>
  <div
    class="field-mapping"
    :class="{
      'is-component': variant === 'component',
      'is-global': variant === 'global'
    }"
  >
    <div class="mapping-summary">
      <span>
        {{ t('spreadsheet.dataset_replacement.used_fields') }}:
        {{ mapping.fields.length }}
      </span>
      <span>
        <template v-if="mapping.target">
          {{ t('spreadsheet.dataset_replacement.matched_fields') }}:
          {{ mapping.fields.filter(item => item.target).length }}/{{ mapping.fields.length }}
        </template>
        <template v-else>
          {{ t('spreadsheet.dataset_replacement.select_dataset_first') }}
        </template>
      </span>
    </div>
    <div class="mapping-content">
      <div class="mapping-search-row">
        <el-input
          v-model="keyword"
          clearable
          :prefix-icon="Search"
          :placeholder="
            t(
              variant === 'global'
                ? 'spreadsheet.dataset_replacement.search'
                : 'spreadsheet.dataset_replacement.search_field'
            )
          "
        />
      </div>
      <div v-if="visibleFields.length" class="mapping-list">
        <div v-for="item in visibleFields" :key="item.source.key" class="mapping-row">
          <div class="source-field">
            <el-icon class="field-type" :class="item.source.groupType">
              <Icon>
                <component :is="getSpreadsheetFieldIcon(item.source)" class="svg-icon" />
              </Icon>
            </el-icon>
            <span class="field-name">{{ item.source.name }}</span>
          </div>
          <div v-if="mapping.target" class="mapping-connector"></div>
          <div v-else class="empty-mapping-connector"></div>
          <el-select
            v-if="mapping.target"
            :model-value="item.target?.id"
            class="target-field-select"
            :class="{ 'is-unmatched': !item.target }"
            popper-class="replacement-target-field-popper"
            :no-data-text="t('spreadsheet.dataset_replacement.no_search_result')"
            :placeholder="t('spreadsheet.dataset_replacement.select_field')"
            @change="selectTarget(item.source.key, $event)"
            @visible-change="handleTargetVisibleChange(item.source.key, $event)"
          >
            <template v-if="item.target" #prefix>
              <el-icon class="target-field-type" :class="item.target.groupType">
                <Icon>
                  <component :is="getSpreadsheetFieldIcon(item.target)" class="svg-icon" />
                </Icon>
              </el-icon>
            </template>
            <template #header>
              <div class="target-field-dropdown-header" @click.stop>
                <div class="target-field-count">
                  {{
                    t('spreadsheet.dataset_replacement.field_count', {
                      count: getCompatibleTargetFields(item.source).length
                    })
                  }}
                </div>
                <div class="target-field-search">
                  <el-input
                    v-model="targetKeywords[item.source.key]"
                    :prefix-icon="Search"
                    :placeholder="t('spreadsheet.dataset_replacement.search')"
                    @keydown.stop
                  />
                </div>
              </div>
            </template>
            <el-option
              v-for="field in getVisibleTargetFields(item.source)"
              :key="field.id"
              :label="field.name"
              :value="field.id"
              :class="['replacement-target-field-option', field.groupType]"
            >
              <div class="target-field-option">
                <el-icon class="target-field-type" :class="field.groupType">
                  <Icon>
                    <component :is="getSpreadsheetFieldIcon(field)" class="svg-icon" />
                  </Icon>
                </el-icon>
                <span>{{ field.name }}</span>
              </div>
            </el-option>
          </el-select>
          <div v-else class="empty-target-field"></div>
        </div>
      </div>
      <el-empty
        v-else
        :description="t('spreadsheet.dataset_replacement.no_search_result')"
        :image-size="72"
      />
    </div>
  </div>
</template>

<style scoped lang="less">
.source-field {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.field-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.field-mapping.is-component {
  position: absolute;
  z-index: 10;
  top: calc(100% + 4px);
  left: 0;
  box-sizing: border-box;
  width: 100%;
  margin-top: 0;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #dee0e3;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(31, 35, 41, 0.12);

  .mapping-summary {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 44px minmax(0, 1.13fr);
    margin-bottom: 10px;
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

  :deep(.ed-input) {
    width: calc((100% - 44px) / 2.13);
    margin-bottom: 10px;
  }

  .mapping-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .mapping-row {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 44px minmax(0, 1.13fr);
    align-items: center;
    min-height: 32px;
    border-top: 0;
    gap: 0;
  }

  .mapping-connector {
    width: 36px;
    margin: 0 4px;
    border-top: 1px dashed #d9dcdf;
  }

  .source-field {
    height: 32px;
    padding: 0 10px;
    background: #e8f0ff;
    border: 1px solid #3370ff;
    border-radius: 4px;
  }

  .source-field:has(.field-type.q) {
    background: #e8fffb;
    border-color: #00b8a9;
  }

  .field-type {
    width: 16px;
    height: 20px;
    line-height: 20px;
    background: transparent;
    border-radius: 0;
  }

  .field-type.q {
    color: #00a99d;
    background: transparent;
  }

  :deep(.ed-select) {
    width: 100%;
  }

  .target-field-select {
    :deep(.ed-select__prefix::after) {
      display: none;
    }

    :deep(.ed-select__wrapper) {
      min-height: 32px;
      padding: 4px 12px;
      border-radius: 6px;
      box-shadow: 0 0 0 1px #d9dcdf inset;
    }

    :deep(.ed-select__selected-item) {
      color: #1f2329;
      font-size: 14px;
      line-height: 22px;
    }

    :deep(.ed-select__placeholder) {
      color: #8f959e;
    }
  }

  .target-field-select.is-unmatched {
    :deep(.ed-select__wrapper) {
      box-shadow: 0 0 0 1px #f54a45 inset !important;
    }
  }

  .target-field-type {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: #3370ff;

    &.q {
      color: #04b49c;
    }

    :deep(.svg-icon) {
      width: 16px;
      height: 16px;
    }
  }

  .empty-target-field {
    width: 100%;
    height: 32px;
  }
}

.field-mapping.is-global {
  position: absolute;
  z-index: 20;
  top: calc(100% + 4px);
  left: 4px;
  box-sizing: border-box;
  width: calc(100% - 8px);
  margin: 0;
  padding: 4px;
  background: #fff;
  border: 1px solid #dee0e3;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(31, 35, 41, 0.12);
  transform-origin: top center;
  animation: global-field-mapping-drop 0.18s ease-out;

  .mapping-summary {
    display: grid;
    grid-template-columns: minmax(0, 0.885fr) 36px minmax(0, 1fr);
    column-gap: 4px;
    height: 32px;
    padding: 0 4px;
    color: #646a73;
    font-size: 14px;
    line-height: 32px;

    span:first-child {
      grid-column: 1;
      padding: 0 8px;
    }

    span:last-child {
      grid-column: 3;
      padding: 0 8px;
    }
  }

  .mapping-search-row {
    position: sticky;
    z-index: 1;
    top: 0;
    display: grid;
    grid-template-columns: minmax(0, 0.885fr) 36px minmax(0, 1fr);
    column-gap: 4px;
    padding: 4px;
    background: #fff;
  }

  .mapping-search-row :deep(.ed-input) {
    grid-column: 1;
    width: 100%;
    height: 32px;
    margin: 0;
  }

  :deep(.ed-input__wrapper) {
    height: 32px;
    padding: 0 12px;
    border-radius: 6px;
    box-shadow: 0 0 0 1px #d9dcdf inset;
  }

  .mapping-content {
    max-height: 248px;
    overflow-y: auto;
  }

  .mapping-row {
    box-sizing: border-box;
    display: grid;
    grid-template-columns: minmax(0, 0.885fr) 36px minmax(0, 1fr);
    column-gap: 4px;
    align-items: center;
    min-height: 40px;
    padding: 4px;
    border-top: 0;
  }

  .mapping-connector {
    width: 36px;
    border-top: 1px dashed #d9dcdf;
  }

  .source-field {
    height: 32px;
    padding: 0 8px;
    background: rgba(51, 112, 255, 0.1);
    border: 1px solid #3370ff;
    border-radius: 6px;
  }

  .source-field:has(.field-type.q) {
    background: rgba(4, 180, 156, 0.1);
    border-color: #04b49c;
  }

  .field-type {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: #3370ff;
    background: transparent;
    border-radius: 0;

    &.q {
      color: #04b49c;
      background: transparent;
    }

    :deep(.svg-icon) {
      width: 16px;
      height: 16px;
    }
  }

  :deep(.ed-select) {
    width: 100%;
  }

  .target-field-select {
    :deep(.ed-select__prefix::after) {
      display: none;
    }

    :deep(.ed-select__wrapper) {
      min-height: 32px;
      padding: 4px 12px;
      border-radius: 6px;
      box-shadow: 0 0 0 1px #d9dcdf inset;
    }

    :deep(.ed-select__selected-item) {
      color: #1f2329;
      font-size: 14px;
      line-height: 22px;
    }

    :deep(.ed-select__placeholder) {
      color: #8f959e;
    }
  }

  .target-field-select.is-unmatched {
    :deep(.ed-select__wrapper) {
      box-shadow: 0 0 0 1px #f54a45 inset !important;
    }
  }

  .target-field-type {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: #3370ff;

    &.q {
      color: #04b49c;
    }

    :deep(.svg-icon) {
      width: 16px;
      height: 16px;
    }
  }

  .empty-target-field {
    width: 100%;
    height: 32px;
  }
}

@keyframes global-field-mapping-drop {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

<style lang="less">
.replacement-target-field-popper {
  padding: 4px !important;
  border: 1px solid #dee0e3 !important;
  border-radius: 6px !important;

  .ed-select-dropdown__header {
    padding: 0 !important;
    border-bottom: 0 !important;
  }

  .target-field-count {
    height: 32px;
    box-sizing: border-box;
    padding: 5px 8px;
    color: #646a73;
    font-size: 14px;
    line-height: 22px;
  }

  .target-field-search {
    height: 40px;
    box-sizing: border-box;
    padding: 4px 8px;

    .ed-input,
    .ed-input__wrapper {
      width: 100%;
      height: 32px;
    }

    .ed-input__wrapper {
      padding: 0 12px;
      border-radius: 6px;
      box-shadow: 0 0 0 1px #d9dcdf inset;
    }
  }

  .ed-select-dropdown__list {
    padding: 8px !important;
  }

  .ed-select-dropdown__item.replacement-target-field-option {
    height: 32px;
    margin-bottom: 8px;
    padding: 5px 8px;
    color: #1f2329;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    background: rgba(51, 112, 255, 0.1);
    border: 1px solid #3370ff;
    border-radius: 6px;

    &:last-child {
      margin-bottom: 0;
    }

    &.q {
      background: rgba(4, 180, 156, 0.1);
      border-color: #04b49c;
    }

    &.is-hovering,
    &:hover {
      background: rgba(51, 112, 255, 0.16);
    }

    &.q.is-hovering,
    &.q:hover {
      background: rgba(4, 180, 156, 0.16);
    }

    &.selected {
      color: #1f2329;
      font-weight: 400;
    }

    &.selected::after {
      display: none;
    }
  }

  .target-field-option {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    span {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .target-field-type {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: #3370ff;

    &.q {
      color: #04b49c;
    }

    .svg-icon {
      width: 16px;
      height: 16px;
    }
  }
}
</style>
