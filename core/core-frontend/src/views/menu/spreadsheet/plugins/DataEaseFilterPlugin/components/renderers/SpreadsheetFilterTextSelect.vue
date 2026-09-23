<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed, onBeforeMount, onBeforeUnmount, ref, watch } from 'vue'
import { debounce, isEqual } from 'lodash-es'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import {
  enumSpreadsheetFilterValueObj,
  getSpreadsheetFilterEnumValue
} from '../../../../api/filter-option'
import { sortSpreadsheetFilterDatasetRows } from '../../utils/filter-values'

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
  'options-ready': []
}>()

const loading = ref(false)
const options = ref<SelectOption[]>([])

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

const loadAutoOptions = async (): Promise<SelectOption[]> => {
  const fieldIds = getAutoFieldIds()
  if (!fieldIds.length) {
    return []
  }

  const values = await getSpreadsheetFilterEnumValue({
    fieldIds,
    resultMode: resultMode.value
  })

  return values
    .filter(value => value !== undefined && value !== null)
    .map(value => ({
      label: String(value),
      value: String(value)
    }))
}

const loadDatasetOptions = async (): Promise<SelectOption[]> => {
  const queryId = getDatasetQueryFieldId()
  const displayId = getDatasetDisplayFieldId()
  // 排序字段是可选配置，未设置时仍应按查询字段和显示字段加载候选值。
  if (!queryId || !displayId) {
    return []
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
  const sortedValues = sortSpreadsheetFilterDatasetRows(values, props.condition)
  sortedValues.forEach(item => {
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

  return Array.from(optionMap.values())
}

const optionRequestKey = computed(() => {
  const condition = props.condition
  if (condition.optionSource === 'manual') {
    return JSON.stringify(['manual', condition.manualOptions])
  }
  if (condition.optionSource === 'dataset') {
    return JSON.stringify([
      'dataset',
      condition.queryFieldId,
      condition.displayFieldId,
      condition.sortFieldId,
      condition.sortType === 'customSort' ? 'asc' : condition.sortType || 'asc',
      condition.sortList || [],
      resultMode.value
    ])
  }
  return JSON.stringify([
    'auto',
    getAutoFieldIds(),
    resultMode.value
  ])
})

const syncDefaultFirstItem = () => {
  if (
    !props.condition.defaultValueEnabled ||
    !props.condition.defaultValueFirstItem
  ) {
    return
  }

  const firstOption = options.value[0]
  let firstValue: unknown = props.condition.multiple ? [] : ''
  if (firstOption) {
    firstValue = props.condition.multiple ? [firstOption.value] : firstOption.value
  }
  if (!isEqual(props.modelValue, firstValue)) {
    emit('update:modelValue', firstValue)
  }
}

let loadSequence = 0

const loadOptions = async () => {
  const requestKey = optionRequestKey.value
  const sequence = ++loadSequence
  loading.value = true
  try {
    let nextOptions: SelectOption[] = []
    if (props.condition.optionSource === 'manual') {
      nextOptions = props.condition.manualOptions.map(value => ({
        label: String(value),
        value: String(value)
      }))
    } else if (props.condition.optionSource === 'dataset') {
      nextOptions = await loadDatasetOptions()
    } else {
      nextOptions = await loadAutoOptions()
    }

    if (sequence !== loadSequence || requestKey !== optionRequestKey.value) {
      return
    }
    options.value = nextOptions
    // 默认首项属于初始化结果，父组件会等待所有候选项就绪后再放行实例首次查询。
    syncDefaultFirstItem()
  } catch (error) {
    if (sequence !== loadSequence || requestKey !== optionRequestKey.value) {
      return
    }
    options.value = []
  } finally {
    if (sequence === loadSequence && requestKey === optionRequestKey.value) {
      loading.value = false
      emit('options-ready')
    }
  }
}

const debounceLoadOptions = debounce(() => {
  void loadOptions()
}, 300)

// 查询栏宿主已保证组件稳定挂载，首次加载直接执行，后续配置变化再防抖合并。
onBeforeMount(() => {
  void loadOptions()
})

onBeforeUnmount(() => {
  debounceLoadOptions.cancel()
  loadSequence += 1
})

watch(
  optionRequestKey,
  () => debounceLoadOptions()
)

watch(
  [
    () => props.condition.defaultValueEnabled,
    () => props.condition.defaultValueFirstItem,
    () => props.condition.multiple
  ],
  syncDefaultFirstItem
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
    v-if="condition.displayForm === 'tile'"
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
