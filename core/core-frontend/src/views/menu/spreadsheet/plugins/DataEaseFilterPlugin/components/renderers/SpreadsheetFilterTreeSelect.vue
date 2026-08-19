<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed, ref, watch } from 'vue'
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
}>()

const loading = ref(false)
const options = ref<TreeOption[]>([])
const pathMap = new Map<string, SpreadsheetFilterTreePath>()

const makeKey = (path: SpreadsheetFilterTreePath) =>
  path.map(item => `${String(item.treeFieldId)}:${encodeURIComponent(String(item.value))}`).join('/')

const mapOptions = (nodes: SourceTreeNode[], parentPath: SpreadsheetFilterTreePath = []): TreeOption[] =>
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
    pathMap.set(value, path)
    return {
      value,
      label: node.text ?? String(node.id),
      children: mapOptions(node.children || [], path)
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
  if (!first) return
  emit('update:modelValue', props.condition.multiple ? [pathMap.get(first.value)!] : pathMap.get(first.value))
}

const loadOptions = async () => {
  const fieldIds = props.condition.treeFields.map(field => field.fieldId).filter(Boolean)
  pathMap.clear()
  if (!fieldIds.length) {
    options.value = []
    return
  }
  loading.value = true
  try {
    const values = await getSpreadsheetFilterFieldTree({
      fieldIds,
      resultMode: props.condition.optionCountMode === 'all' ? 1 : 0
    })
    options.value = mapOptions((values || []) as SourceTreeNode[])
    if (
      props.condition.defaultValueEnabled &&
      props.condition.defaultValueFirstItem &&
      !normalizePaths(props.modelValue).length
    ) {
      emitFirstItem()
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => props.condition.treeFields.map(field => field.fieldId).join(','),
  loadOptions,
  { immediate: true }
)

watch(
  () => props.condition.defaultValueFirstItem,
  enabled => {
    if (enabled && options.value.length) emitFirstItem()
  }
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
