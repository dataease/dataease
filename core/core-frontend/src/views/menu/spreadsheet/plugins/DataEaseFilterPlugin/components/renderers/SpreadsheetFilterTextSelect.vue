<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed, ref, watch } from 'vue'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import {
  enumSpreadsheetFilterValueObj,
  getSpreadsheetFilterEnumValue,
  getSpreadsheetFilterFieldTree
} from '../../../../api/filter-option'

interface SelectOption {
  label: string
  value: string
  children?: SelectOption[]
}

const props = defineProps<{
  modelValue: unknown
  condition: SpreadsheetFilterCondition
  placeholder?: string
  isConfig?: boolean
  disabled?: boolean
  popperAppendTo?: string
  popperOptions?: Partial<Options>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
}>()

const loading = ref(false)
const options = ref<SelectOption[]>([])
const treeOptions = ref<SelectOption[]>([])

const isTreeSelect = computed(() => props.condition.displayType === 'treeSelect')
const resultMode = computed(() => props.condition.optionCountMode === 'all' ? 1 : 0)
const selectedValues = computed(() =>
  (Array.isArray(props.modelValue) ? props.modelValue : [props.modelValue])
    .filter(value => value !== undefined && value !== null && value !== '')
    .map(String)
)
const configMultipleValue = ref<string[]>([])
const selectRef = ref()
const tileScrollbarRef = ref()

const getAutoFieldIds = () =>
  props.condition.linkedFields
    .map(field => field.fieldId)
    .filter(fieldId => fieldId !== undefined && fieldId !== null && fieldId !== '')

const getDatasetQueryFieldId = () =>
  props.condition.queryFieldId

const getDatasetDisplayFieldId = () =>
  props.condition.displayFieldId

const mapTreeOptions = (nodes: Record<string, unknown>[] = []): SelectOption[] =>
  nodes.map(node => {
    const children = Array.isArray(node.children)
      ? mapTreeOptions(node.children as Record<string, unknown>[])
      : []
    const value = String(node.id ?? node.value ?? node.text ?? node.label ?? '')
    return {
      label: String(node.text ?? node.label ?? node.name ?? value),
      value,
      children
    }
  })

const loadAutoOptions = async () => {
  const fieldIds = getAutoFieldIds()
  if (!fieldIds.length) {
    options.value = []
    return
  }

  const values = await getSpreadsheetFilterEnumValue({
    fieldIds,
    resultMode: resultMode.value
  })

  options.value = values
    .filter(value => value !== undefined && value !== null)
    .map(value => ({
      label: String(value),
      value: String(value)
    }))
}

const loadDatasetOptions = async () => {
  const queryId = getDatasetQueryFieldId()
  const displayId = getDatasetDisplayFieldId()
  if (!queryId || !displayId || !props.condition.sortFieldId) {
    options.value = []
    return
  }

  const values = await enumSpreadsheetFilterValueObj({
    queryId,
    displayId,
    sortId: props.condition.sortFieldId,
    sort: props.condition.sortType === 'customSort' ? 'asc' : props.condition.sortType || 'asc',
    resultMode: resultMode.value,
    searchText: ''
  })

  const optionMap = new Map<string, SelectOption>()
  values.forEach(item => {
    const value = item[queryId]
    if (value === undefined || value === null) {
      return
    }
    const optionValue = String(value)
    if (optionMap.has(optionValue)) {
      return
    }
    optionMap.set(optionValue, {
      label: String(item[displayId || queryId] ?? value),
      value: optionValue
    })
  })

  options.value = Array.from(optionMap.values())
}

const loadTreeOptions = async () => {
  const fieldIds = (
    props.condition.optionSource === 'dataset'
      ? [getDatasetQueryFieldId()].filter(
        (fieldId): fieldId is string | number =>
          fieldId !== undefined && fieldId !== null && fieldId !== ''
      )
      : getAutoFieldIds()
  )
  if (!fieldIds.length) {
    treeOptions.value = []
    return
  }

  const values = await getSpreadsheetFilterFieldTree({
    fieldIds,
    resultMode: resultMode.value
  })

  treeOptions.value = mapTreeOptions(values)
}

const loadOptions = async () => {
  loading.value = true
  try {
    if (props.condition.optionSource === 'manual') {
      options.value = props.condition.manualOptions.map(value => ({
        label: String(value),
        value: String(value)
      }))
      treeOptions.value = []
    } else if (isTreeSelect.value) {
      await loadTreeOptions()
    } else if (props.condition.optionSource === 'dataset') {
      await loadDatasetOptions()
    } else {
      await loadAutoOptions()
    }

    if (
      props.condition.defaultValueEnabled &&
      props.condition.defaultValueFirstItem &&
      options.value.length &&
      !selectedValues.value.length
    ) {
      emit(
        'update:modelValue',
        props.condition.multiple ? [options.value[0].value] : options.value[0].value
      )
    }
  } catch (error) {
    options.value = []
    treeOptions.value = []
    console.error('[SpreadsheetFilterTextSelect] Failed to load filter options:', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => [
    props.condition.id,
    props.condition.displayType,
    props.condition.optionSource,
    props.condition.queryFieldId,
    props.condition.displayFieldId,
    props.condition.sortFieldId,
    props.condition.sortType,
    props.condition.optionCountMode,
    props.condition.defaultValueEnabled,
    props.condition.defaultValueFirstItem,
    props.condition.manualOptions.join(','),
    props.condition.linkedFields.map(field => `${field.pluginId}:${field.fieldId}`).join(',')
  ],
  () => {
    void loadOptions()
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  value => {
    configMultipleValue.value = Array.isArray(value) ? value.map(String) : []
  },
  { immediate: true }
)

const isTileSelected = (value: string) => selectedValues.value.includes(value)

const toggleTileOption = (value: string) => {
  if (!props.condition.multiple) {
    emit('update:modelValue', isTileSelected(value) ? '' : value)
    return
  }
  const nextValues = isTileSelected(value)
    ? selectedValues.value.filter(item => item !== value)
    : [...selectedValues.value, value]
  emit('update:modelValue', nextValues)
}

const allOptionsSelected = computed(
  () => !!options.value.length && configMultipleValue.value.length === options.value.length
)
const someOptionsSelected = computed(
  () => configMultipleValue.value.length > 0 && !allOptionsSelected.value
)
const toggleAllOptions = (checked: boolean) => {
  configMultipleValue.value = checked ? options.value.map(option => option.value) : []
}
const clearConfigMultiple = () => {
  configMultipleValue.value = []
}
const confirmConfigMultiple = () => {
  emit('update:modelValue', [...configMultipleValue.value])
  selectRef.value?.blur?.()
}
const handleSelectUpdate = (value: unknown) => {
  if (props.isConfig && props.condition.multiple) {
    configMultipleValue.value = Array.isArray(value) ? value.map(String) : []
    return
  }
  emit('update:modelValue', value)
}

const handleTileWheel = (event: WheelEvent) => {
  const scrollbar = tileScrollbarRef.value
  const wrap = scrollbar?.wrapRef as HTMLElement | undefined
  if (!wrap || wrap.scrollWidth <= wrap.clientWidth) return
  event.preventDefault()
  scrollbar.setScrollLeft(wrap.scrollLeft + (event.deltaY || event.deltaX))
}
</script>

<template>
  <el-scrollbar
    v-if="!isTreeSelect && condition.displayForm === 'tile'"
    ref="tileScrollbarRef"
    class="spreadsheet-filter-tile-scrollbar"
    @wheel="handleTileWheel"
  >
    <div
      class="spreadsheet-filter-tile"
      :class="{ 'is-disabled': disabled }"
    >
      <button
        v-for="option in options"
        :key="option.value"
        type="button"
        class="spreadsheet-filter-tile__option"
        :class="{ 'is-selected': isTileSelected(option.value) }"
        :disabled="disabled"
        @click="toggleTileOption(option.value)"
      >
        {{ option.label }}
      </button>
    </div>
  </el-scrollbar>
  <el-tree-select
    v-else-if="isTreeSelect"
    :model-value="modelValue"
    :data="treeOptions"
    :placeholder="placeholder || '请选择'"
    :loading="loading"
    :disabled="disabled"
    :multiple="condition.multiple"
    :show-checkbox="condition.multiple"
    :append-to="popperAppendTo"
    popper-class="spreadsheet-filter-runtime-popper"
    :popper-options="popperOptions"
    check-strictly
    filterable
    clearable
    @update:model-value="value => emit('update:modelValue', value)"
  />
  <el-select
    v-else
    ref="selectRef"
    :model-value="isConfig && condition.multiple ? configMultipleValue : modelValue"
    :multiple="condition.multiple"
    collapse-tags
    :max-collapse-tags="1"
    :placeholder="placeholder || `请选择（${condition.multiple ? '多选' : '单选'}）`"
    :loading="loading"
    :disabled="disabled"
    :append-to="popperAppendTo"
    popper-class="spreadsheet-filter-runtime-popper"
    :popper-options="popperOptions"
    filterable
    clearable
    @update:model-value="handleSelectUpdate"
  >
    <template v-if="isConfig && condition.multiple" #header>
      <el-checkbox
        :model-value="allOptionsSelected"
        :indeterminate="someOptionsSelected"
        @change="checked => toggleAllOptions(!!checked)"
      >
        全选
      </el-checkbox>
    </template>
    <el-option
      v-for="option in options"
      :key="option.value"
      :label="option.label"
      :value="option.value"
    />
    <template v-if="isConfig && condition.multiple" #footer>
      <div class="spreadsheet-filter-multiple-footer">
        <el-button text @click.stop="clearConfigMultiple">清空</el-button>
        <el-button type="primary" @click.stop="confirmConfigMultiple">确定</el-button>
      </div>
    </template>
  </el-select>
</template>

<style scoped lang="less">
.spreadsheet-filter-tile-scrollbar {
  width: 100%;
  background-color: var(--ed-fill-color-blank);
}

.spreadsheet-filter-tile {
  width: max-content;
  min-width: 100%;
  display: flex;
  align-items: center;
  gap: 24px;

  &.is-disabled {
    cursor: not-allowed;

    .spreadsheet-filter-tile__option {
      cursor: not-allowed;
    }
  }

  &__option {
    height: 32px;
    padding: 0 0 4px;
    border: 0;
    border-bottom: 2px solid transparent;
    flex-shrink: 0;
    background: transparent;
    color: #1f2329;
    font-size: 14px;
    cursor: pointer;

    &.is-selected {
      color: var(--ed-color-primary);
      border-bottom-color: var(--ed-color-primary);
    }
  }
}

.spreadsheet-filter-multiple-footer {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin: -8px -12px;

  .ed-button {
    height: 40px;
    margin: 0;
    border-radius: 0;
  }
}
</style>
