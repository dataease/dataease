<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { computed } from 'vue'
import dayjs, { type Dayjs, type QUnitType } from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import { ElConfigProvider, ElDatePicker } from 'element-plus-secondary'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { useI18n } from '@/hooks/web/useI18n'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import {
  getTimePickerType,
  getTimeValueFormat,
  isTimeDisabled,
  resolveTimeFilterBounds
} from '../../utils/time-filter'

dayjs.extend(quarterOfYear)

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
const valueFormat = computed(() => getTimeValueFormat(granularity.value))
const hasTimeBounds = computed(
  () =>
    props.condition.timeFilterRangeEnabled &&
    (granularity.value === 'datetime' || granularity.value === 'datetimerange')
)
const pickerOptions = computed(() => ({
  disabledDate: (date: Date) =>
    isTimeDisabled(
      date,
      props.condition,
      Array.isArray(props.modelValue) ? (props.modelValue as [string, string]) : undefined
    )
}))

const getDisabledTimeParts = (date: Dayjs | undefined, unit: 'hour' | 'minute' | 'second') => {
  if (!date || !hasTimeBounds.value) return []
  const { start, end } = resolveTimeFilterBounds(props.condition.timeFilterRange)
  const disabled: number[] = []
  const count = unit === 'hour' ? 24 : 60
  // 按当前列的粒度比较，有可选分钟或秒的小时仍可选。
  for (let part = 0; part < count; part++) {
    const candidate = date.set(unit, part)
    if ((start && candidate.isBefore(start, unit)) || (end && candidate.isAfter(end, unit))) {
      disabled.push(part)
    }
  }
  return disabled
}

const updateValue = (value: unknown) => {
  if (!value || !hasTimeBounds.value) {
    emit('update:modelValue', value)
    return
  }
  const { start, end } = resolveTimeFilterBounds(props.condition.timeFilterRange)
  if (start && end && start.isAfter(end, 'second')) return

  // 选中日期时控件可能沿用原有时分秒，手输和快捷选择也要在提交前应用边界。
  const clampTime = (item: unknown) => {
    if (typeof item !== 'string' || !item) return item
    const date = dayjs(item)
    if (start && date.isBefore(start, 'second')) return start.format(valueFormat.value)
    if (end && date.isAfter(end, 'second')) return end.format(valueFormat.value)
    return item
  }
  const nextValue = Array.isArray(value) ? value.map(clampTime) : clampTime(value)
  emit('update:modelValue', nextValue)
}

const shortcuts = computed(() => {
  if (!isRange.value) return []
  const options: { text: string; offset: number; unit: QUnitType }[] = [
    { text: 'dynamic_time.cweek', offset: 0, unit: 'week' },
    { text: 'dynamic_month.current', offset: 0, unit: 'month' },
    { text: 'dynamic_time.cquarter', offset: 0, unit: 'quarter' },
    { text: 'dynamic_year.current', offset: 0, unit: 'year' },
    { text: 'dynamic_time.lweek', offset: -1, unit: 'week' },
    { text: 'dynamic_month.last', offset: -1, unit: 'month' },
    { text: 'dynamic_time.lquarter', offset: -1, unit: 'quarter' },
    { text: 'dynamic_year.last', offset: -1, unit: 'year' },
    { text: 'common.next_week', offset: 1, unit: 'week' },
    { text: 'common.next_month', offset: 1, unit: 'month' },
    { text: 'common.next_quarter', offset: 1, unit: 'quarter' },
    { text: 'common.next_year', offset: 1, unit: 'year' }
  ]
  return options
    .filter(({ unit }) => {
      if (granularity.value === 'yearrange') return unit === 'year'
      if (granularity.value === 'monthrange') return unit !== 'week'
      return true
    })
    .map(({ text, offset, unit }) => ({
      text: t(text),
      onClick: ({ emit }: { emit: (event: 'pick', value: [Dayjs, Dayjs]) => void }) => {
        const period = dayjs().locale(elLocale.value.name).add(offset, unit)
        const range: [Dayjs, Dayjs] = [period.startOf(unit), period.endOf(unit)]
        const format = valueFormat.value
        const selectedRange: [string, string] = [range[0].format(format), range[1].format(format)]
        // Validate the new range against its own start, not the previous selection.
        if (range.some(date => isTimeDisabled(date.toDate(), props.condition, selectedRange)))
          return
        emit('pick', range)
      }
    }))
})
</script>

<template>
  <el-config-provider :locale="elLocale" namespace="ed">
    <el-date-picker
      class="spreadsheet-filter-time"
      :class="{ 'spreadsheet-filter-time--range': isRange }"
      :model-value="modelValue"
      :type="getTimePickerType(granularity)"
      :value-format="valueFormat"
      :placeholder="placeholder || t('common.please_select')"
      :start-placeholder="t('datasource.start_time')"
      :end-placeholder="t('datasource.end_time')"
      :disabled="disabled"
      :disabled-date="pickerOptions.disabledDate"
      :disabled-hours="(_role, date) => getDisabledTimeParts(date, 'hour')"
      :disabled-minutes="(hour, _role, date) => getDisabledTimeParts(date?.hour(hour), 'minute')"
      :disabled-seconds="
        (hour, minute, _role, date) =>
          getDisabledTimeParts(date?.hour(hour).minute(minute), 'second')
      "
      :shortcuts="shortcuts"
      :append-to="popperAppendTo"
      popper-class="spreadsheet-filter-runtime-popper"
      :popper-options="popperOptions"
      @update:model-value="updateValue"
    />
  </el-config-provider>
</template>

<style scoped>
.spreadsheet-filter-time {
  width: 100% !important;
  min-width: 0;
}
</style>
