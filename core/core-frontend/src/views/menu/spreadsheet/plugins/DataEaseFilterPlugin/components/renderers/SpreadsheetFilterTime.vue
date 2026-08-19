<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed } from 'vue'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import {
  getTimePickerType,
  getTimeValueFormat,
  isTimeDisabled
} from '../../utils/time-filter'

const props = defineProps<{
  modelValue: unknown
  condition: SpreadsheetFilterCondition
  placeholder?: string
  disabled?: boolean
  popperAppendTo?: string
  popperOptions?: Partial<Options>
}>()

const emit = defineEmits<{ 'update:modelValue': [value: unknown] }>()

const isRange = computed(() => props.condition.displayType === 'timeRange')
const granularity = computed(() =>
  isRange.value ? props.condition.timeRangeGranularity : props.condition.timeGranularity
)
const pickerOptions = computed(() => ({
  disabledDate: (date: Date) =>
    isTimeDisabled(
      date,
      props.condition,
      Array.isArray(props.modelValue) ? (props.modelValue as [string, string]) : undefined
    )
}))
</script>

<template>
  <el-date-picker
    class="spreadsheet-filter-time"
    :class="{ 'spreadsheet-filter-time--range': isRange }"
    :model-value="modelValue"
    :type="getTimePickerType(granularity)"
    :value-format="getTimeValueFormat(granularity)"
    :placeholder="placeholder || '请选择时间'"
    start-placeholder="开始时间"
    end-placeholder="结束时间"
    :disabled="disabled"
    :disabled-date="pickerOptions.disabledDate"
    :append-to="popperAppendTo"
    popper-class="spreadsheet-filter-runtime-popper"
    :popper-options="popperOptions"
    @update:model-value="value => emit('update:modelValue', value)"
  />
</template>

<style scoped>
.spreadsheet-filter-time {
  width: 100%;
}
</style>
