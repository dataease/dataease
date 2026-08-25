<script setup lang="ts">
import { computed } from 'vue'
import type { Options } from '@popperjs/core'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import SpreadsheetFilterTime from './SpreadsheetFilterTime.vue'
import SpreadsheetFilterNumberRange from './SpreadsheetFilterNumberRange.vue'
import SpreadsheetFilterTextSearch from './SpreadsheetFilterTextSearch.vue'
import SpreadsheetFilterTextSelect from './SpreadsheetFilterTextSelect.vue'
import SpreadsheetFilterTreeSelect from './SpreadsheetFilterTreeSelect.vue'

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
  commit: []
  'options-ready': []
}>()

const component = computed(() => {
  if (props.condition.displayType === 'textSearch') {
    return SpreadsheetFilterTextSearch
  }
  if (props.condition.displayType === 'treeSelect') {
    return SpreadsheetFilterTreeSelect
  }
  if (props.condition.displayType === 'numberRange') {
    return SpreadsheetFilterNumberRange
  }
  if (['time', 'timeRange'].includes(props.condition.displayType)) {
    return SpreadsheetFilterTime
  }
  return SpreadsheetFilterTextSelect
})
</script>

<template>
  <component
    :is="component"
    :model-value="modelValue"
    :condition="condition"
    :placeholder="placeholder"
    :is-config="isConfig"
    :disabled="disabled"
    :popper-append-to="popperAppendTo"
    :popper-options="popperOptions"
    @update:model-value="value => emit('update:modelValue', value)"
    @commit="emit('commit')"
    @options-ready="emit('options-ready')"
  />
</template>
