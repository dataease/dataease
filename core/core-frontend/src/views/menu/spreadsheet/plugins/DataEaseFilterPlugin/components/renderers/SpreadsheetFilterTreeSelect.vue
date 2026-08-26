<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed, onBeforeMount, onBeforeUnmount, ref, watch } from 'vue'
import { debounce, isEqual } from 'lodash-es'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterTreePath
} from '../../../../types/plugin'
import { getSpreadsheetFilterFieldTree } from '../../../../api/filter-option'
import {
  normalizeSpreadsheetFilterTreePath,
  resolveSpreadsheetFilterTreeLevelValue
} from '../../utils/filter-tree-values'

interface SourceTreeNode {
  id: string | number
  text?: string
  children?: SourceTreeNode[]
}

interface TreeOption {
  value: string
  label: string
  children: TreeOption[]
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
  'update:modelValue': [value: SpreadsheetFilterTreePath | SpreadsheetFilterTreePath[] | undefined]
  'options-ready': []
}>()

const loading = ref(false)
const options = ref<TreeOption[]>([])
let pathMap = new Map<string, SpreadsheetFilterTreePath>()

const makeKey = (path: SpreadsheetFilterTreePath) =>
  path.map(item => `${String(item.treeFieldId)}:${encodeURIComponent(String(item.value))}`).join('/')

const mapOptions = (
  nodes: SourceTreeNode[],
  targetPathMap: Map<string, SpreadsheetFilterTreePath>,
  parentPath: SpreadsheetFilterTreePath = []
): TreeOption[] =>
  (nodes || []).map((node, index) => {
    const treeField = props.condition.treeFields[parentPath.length]
    const path = treeField
      ? [
          ...parentPath,
          {
            treeFieldId: treeField.fieldId,
            value: resolveSpreadsheetFilterTreeLevelValue(node.id, parentPath.length)
          }
        ]
      : parentPath
    const value = makeKey(path) || `unknown-${index}`
    targetPathMap.set(value, path)
    return {
      value,
      label: node.text ?? String(node.id),
      children: mapOptions(node.children || [], targetPathMap, path)
    }
  })

const normalizePaths = (value: unknown): SpreadsheetFilterTreePath[] => {
  if (!Array.isArray(value) || !value.length) return []
  const first = value[0] as Record<string, unknown> | unknown[]
  const paths = !Array.isArray(first) && first && 'treeFieldId' in first
    ? [value as SpreadsheetFilterTreePath]
    : value as SpreadsheetFilterTreePath[]
  return paths.map(normalizeSpreadsheetFilterTreePath)
}

const selectedKeys = computed({
  get: () => {
    const keys = normalizePaths(props.modelValue).map(makeKey)
    return props.condition.multiple ? keys : keys[0]
  },
  set: value => {
    const keys = Array.isArray(value) ? value : value ? [value] : []
    const paths = keys.map(key => pathMap.get(String(key))).filter((path): path is SpreadsheetFilterTreePath => !!path)
    emit('update:modelValue', props.condition.multiple ? paths : paths[0])
  }
})

const emitFirstItem = () => {
  const first = options.value[0]
  const firstPath = first ? pathMap.get(first.value) : undefined
  let firstValue: SpreadsheetFilterTreePath | SpreadsheetFilterTreePath[] | undefined
  if (firstPath) {
    firstValue = props.condition.multiple ? [firstPath] : firstPath
  } else {
    firstValue = props.condition.multiple ? [] : undefined
  }
  if (!isEqual(props.modelValue, firstValue)) {
    emit('update:modelValue', firstValue)
  }
}

const optionRequestKey = computed(() => JSON.stringify([
  props.condition.treeFields.map(field => field.fieldId).filter(Boolean),
  props.condition.optionCountMode === 'all' ? 1 : 0
]))

const syncDefaultFirstItem = () => {
  if (
    props.condition.defaultValueEnabled &&
    props.condition.defaultValueFirstItem
  ) {
    emitFirstItem()
  }
}

let loadSequence = 0

const loadOptions = async () => {
  const requestKey = optionRequestKey.value
  const sequence = ++loadSequence
  const fieldIds = props.condition.treeFields.map(field => field.fieldId).filter(Boolean)
  if (!fieldIds.length) {
    options.value = []
    pathMap.clear()
    loading.value = false
    syncDefaultFirstItem()
    emit('options-ready')
    return
  }
  loading.value = true
  try {
    const values = await getSpreadsheetFilterFieldTree({
      fieldIds,
      resultMode: props.condition.optionCountMode === 'all' ? 1 : 0
    })
    if (sequence !== loadSequence || requestKey !== optionRequestKey.value) {
      return
    }
    const nextPathMap = new Map<string, SpreadsheetFilterTreePath>()
    options.value = mapOptions((values || []) as SourceTreeNode[], nextPathMap)
    pathMap = nextPathMap
    // 默认首项属于初始化结果，父组件会等待所有候选项就绪后再放行实例首次查询。
    syncDefaultFirstItem()
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
</script>

<template>
  <el-tree-select
    v-model="selectedKeys"
    class="spreadsheet-filter-tree-select"
    :data="options"
    :loading="loading"
    :disabled="disabled"
    :multiple="condition.multiple"
    :show-checkbox="condition.multiple"
    :check-strictly="!condition.multiple"
    :render-after-expand="false"
    :placeholder="placeholder || '请选择'"
    :append-to="popperAppendTo"
    popper-class="spreadsheet-filter-runtime-popper"
    :popper-options="popperOptions"
    collapse-tags
    collapse-tags-tooltip
    clearable
    filterable
  />
</template>

<style scoped>
.spreadsheet-filter-tree-select {
  width: 100%;
}
</style>
