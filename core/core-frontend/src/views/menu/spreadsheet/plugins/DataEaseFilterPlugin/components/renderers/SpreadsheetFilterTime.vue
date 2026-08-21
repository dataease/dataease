<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed } from 'vue'
import { ElConfigProvider, ElDatePicker } from 'element-plus-secondary'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { useI18n } from '@/hooks/web/useI18n'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import {
  getTimePickerType,
  getTimeValueFormat,
  isTimeDisabled
} from '../../utils/time-filter'

const { t } = useI18n()
const localeStore = useLocaleStoreWithOut()
const elLocale = computed(() => localeStore.getCurrentLocale.elLocale)

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
  <el-config-provider :locale="elLocale" namespace="ed">
    <el-date-picker
      class="spreadsheet-filter-time"
      :class="{ 'spreadsheet-filter-time--range': isRange }"
      :model-value="modelValue"
      :type="getTimePickerType(granularity)"
      :value-format="getTimeValueFormat(granularity)"
      :placeholder="placeholder || t('common.please_select')"
      :start-placeholder="t('datasource.start_time')"
      :end-placeholder="t('datasource.end_time')"
      :disabled="disabled"
      :disabled-date="pickerOptions.disabledDate"
      :append-to="popperAppendTo"
      popper-class="spreadsheet-filter-runtime-popper"
      :popper-options="popperOptions"
      @update:model-value="value => emit('update:modelValue', value)"
    />
  </el-config-provider>
</template>

<style scoped>
.spreadsheet-filter-time {
  width: 100% !important;
  min-width: 0;
}
</style>
