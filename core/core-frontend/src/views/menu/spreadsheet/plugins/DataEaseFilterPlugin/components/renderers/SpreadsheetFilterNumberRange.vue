<script setup lang="ts">
import type { Options } from '@popperjs/core'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'

const props = defineProps<{
  modelValue: unknown
  condition?: SpreadsheetFilterCondition
  placeholder?: string
  isConfig?: boolean
  disabled?: boolean
  popperAppendTo?: string
  popperOptions?: Partial<Options>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: [number | undefined, number | undefined]]
}>()

const getRangeValue = (value: unknown): [number | undefined, number | undefined] =>
  Array.isArray(value) ? [value[0], value[1]] : [undefined, undefined]

const updateValue = (index: 0 | 1, value: number | undefined) => {
  const nextValue = getRangeValue(props.modelValue)
  nextValue[index] = value
  emit('update:modelValue', nextValue)
}
</script>

<template>
  <div class="spreadsheet-filter-number-range">
    <el-input-number
      :model-value="getRangeValue(modelValue)[0]"
      controls-position="right"
      :placeholder="placeholder || '最小值'"
      @update:model-value="value => updateValue(0, value)"
    />
    <span class="spreadsheet-filter-number-range__separator">-</span>
    <el-input-number
      :model-value="getRangeValue(modelValue)[1]"
      controls-position="right"
      :placeholder="placeholder || '最大值'"
      @update:model-value="value => updateValue(1, value)"
    />
  </div>
</template>

<style scoped lang="less">
.spreadsheet-filter-number-range {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;

  &__separator {
    color: #8f959e;
    flex-shrink: 0;
  }

  :deep(.ed-input-number),
  :deep(.el-input-number) {
    min-width: 0;
    flex: 1;
  }
}
</style>
