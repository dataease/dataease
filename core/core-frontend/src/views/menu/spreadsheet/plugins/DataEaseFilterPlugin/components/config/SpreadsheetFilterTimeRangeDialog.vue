<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import dayjs from 'dayjs'
import { ElConfigProvider } from 'element-plus-secondary'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import type { SpreadsheetFilterTimeFilterRange } from '../../../../types/plugin'
import { resolveTimeFilterBounds } from '../../utils/time-filter'

const localeStore = useLocaleStoreWithOut()
const elLocale = computed(() => localeStore.getCurrentLocale.elLocale)

const props = defineProps<{
  value: SpreadsheetFilterTimeFilterRange
  isRange: boolean
  granularity: string
}>()
const emit = defineEmits<{ change: [value: SpreadsheetFilterTimeFilterRange] }>()

const local = reactive<SpreadsheetFilterTimeFilterRange>({ intervalType: 'none' })
const allUnits = [
  { label: '年', value: 'year' },
  { label: '月', value: 'month' },
  { label: '日', value: 'day' }
]
const baseGranularity = computed(() => props.granularity.replace('range', ''))
const units = computed(() => {
  const count = baseGranularity.value === 'year' ? 1 : baseGranularity.value === 'month' ? 2 : 3
  return allUnits.slice(0, count)
})
const singlePickerTypes = {
  year: 'year',
  month: 'month',
  date: 'date',
  datetime: 'datetime'
} as const
const rangePickerTypes = {
  year: 'yearrange',
  month: 'monthrange',
  date: 'daterange',
  datetime: 'datetimerange'
} as const
const pickerType = computed(() => {
  const granularity = baseGranularity.value as keyof typeof singlePickerTypes
  return local.intervalType === 'timeInterval'
    ? rangePickerTypes[granularity]
    : singlePickerTypes[granularity]
})
const pickerValueFormat = computed(() => {
  if (baseGranularity.value === 'year') return 'YYYY'
  if (baseGranularity.value === 'month') return 'YYYY-MM'
  if (baseGranularity.value === 'datetime') return 'YYYY-MM-DD HH:mm:ss'
  return 'YYYY-MM-DD'
})
const singleRelativeOptions = computed(() => {
  const granularity = props.granularity.replace('range', '')
  if (granularity === 'year') {
    return [
      { label: '今年', value: 'thisYear' },
      { label: '去年', value: 'lastYear' },
      { label: '自定义', value: 'custom' }
    ]
  }
  if (granularity === 'month') {
    return [
      { label: '本月', value: 'thisMonth' },
      { label: '上月', value: 'lastMonth' },
      { label: '自定义', value: 'custom' }
    ]
  }
  return [
    { label: '今天', value: 'today' },
    { label: '昨天', value: 'yesterday' },
    { label: '月初', value: 'monthBeginning' },
    { label: '年初', value: 'yearBeginning' },
    { label: '自定义', value: 'custom' }
  ]
})
const rangeRelativeOptions = computed(() => {
  const granularity = props.granularity.replace('range', '')
  if (granularity === 'year') return singleRelativeOptions.value
  if (granularity === 'month')
    return [
      { label: '本月', value: 'thisMonth' },
      { label: '上月', value: 'lastMonth' },
      { label: '本季度', value: 'thisQuarter' },
      { label: '最近3个月', value: 'LastThreeMonths' },
      { label: '最近6个月', value: 'LastSixMonths' },
      { label: '最近12个月', value: 'LastTwelveMonths' },
      { label: '年初至本月', value: 'YearToThisMonth' },
      { label: '年初至上月末', value: 'YearToLastMonthEnd' },
      { label: '自定义', value: 'custom' }
    ]
  return [
    { label: '今天', value: 'today' },
    { label: '昨天', value: 'yesterday' },
    { label: '最近3天', value: 'LastThreeDays' },
    { label: '月初至今', value: 'monthBeginning' },
    { label: '年初至今', value: 'yearBeginning' },
    { label: '年初至上月末', value: 'YearToLastMonthEnd' },
    { label: '月初至昨天', value: 'monthToYesterday' },
    { label: '自定义', value: 'custom' }
  ]
})

const ensure = () => {
  Object.assign(local, JSON.parse(JSON.stringify(props.value || { intervalType: 'none' })))
  local.start ||= { type: 'fixed', value: '' }
  local.end ||= { type: 'fixed', value: '' }
  local.start.dynamic ||= {
    value: 0,
    unit: 'day',
    direction: 'before',
    relativeToCurrent: 'custom'
  }
  local.end.dynamic ||= { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' }
  local.start.dynamic.time ||= dayjs().format('HH:mm:ss')
  local.end.dynamic.time ||= dayjs().format('HH:mm:ss')
  local.relativeToCurrentRange ||= 'custom'
}
watch(() => props.value, ensure, { immediate: true, deep: true })
watch(
  units,
  options => {
    const allowed = new Set(options.map(item => item.value))
    const fallback = options[options.length - 1]?.value || 'year'
    for (const side of ['start', 'end'] as const) {
      if (local[side]?.dynamic && !allowed.has(local[side]!.dynamic!.unit)) {
        local[side]!.dynamic!.unit = fallback
      }
    }
  },
  { immediate: true }
)
watch(
  local,
  value => {
    if (JSON.stringify(value) !== JSON.stringify(props.value)) {
      emit('change', JSON.parse(JSON.stringify(value)))
    }
  },
  { deep: true }
)

const preview = computed(() => {
  const { start, end } = resolveTimeFilterBounds(local)
  if (local.intervalType === 'start') return start?.format(pickerValueFormat.value) || ''
  if (local.intervalType === 'end') return end?.format(pickerValueFormat.value) || ''
  return [start?.format(pickerValueFormat.value) || '', end?.format(pickerValueFormat.value) || '']
})
const activeBoundary = computed(() => (local.intervalType === 'end' ? local.end! : local.start!))
const activeRelativeToCurrent = computed({
  get: () =>
    local.intervalType === 'timeInterval'
      ? local.relativeToCurrentRange || 'custom'
      : activeBoundary.value.dynamic?.relativeToCurrent || 'custom',
  set: value => {
    if (local.intervalType === 'timeInterval') {
      local.relativeToCurrentRange = value
      if (value === 'custom') {
        if (local.start?.dynamic) local.start.dynamic.relativeToCurrent = 'custom'
        if (local.end?.dynamic) local.end.dynamic.relativeToCurrent = 'custom'
      }
    } else if (activeBoundary.value.dynamic) activeBoundary.value.dynamic.relativeToCurrent = value
  }
})
const activeRelativeOptions = computed(() =>
  local.intervalType === 'timeInterval' ? rangeRelativeOptions.value : singleRelativeOptions.value
)
const activeSides = computed<Array<'start' | 'end'>>(() =>
  local.intervalType === 'timeInterval'
    ? ['start', 'end']
    : [local.intervalType === 'end' ? 'end' : 'start']
)
watch(
  () => local.start?.type,
  type => {
    if (local.intervalType === 'timeInterval' && local.end && type) local.end.type = type
  }
)
</script>

<template>
  <el-config-provider :locale="elLocale" namespace="ed">
    <div class="time-range-popover">
      <h4>设置时间筛选范围</h4>
      <div class="time-range-popover__label">区间类型</div>
      <el-radio-group v-model="local.intervalType" class="time-range-popover__radios">
        <el-radio label="none">无</el-radio><el-radio label="start">开始于</el-radio>
        <el-radio label="end">结束于</el-radio><el-radio label="timeInterval">时间区间</el-radio>
      </el-radio-group>

      <template v-if="local.intervalType !== 'none'">
        <div class="time-range-popover__label">
          {{ local.intervalType === 'timeInterval' ? '时间区间' : '时间' }}
        </div>
        <el-radio-group v-model="activeBoundary.type" class="time-range-popover__radios">
          <el-radio label="fixed">固定时间</el-radio><el-radio label="dynamic">动态时间</el-radio>
        </el-radio-group>
        <template v-if="activeBoundary.type === 'dynamic'">
          <div class="time-range-popover__form-row">
            <span>相对当前</span>
            <el-select v-model="activeRelativeToCurrent" :teleported="false">
              <el-option v-for="item in activeRelativeOptions" :key="item.value" v-bind="item" />
            </el-select>
          </div>
          <template v-if="activeRelativeToCurrent === 'custom'">
            <div v-for="side in activeSides" :key="side" class="time-range-popover__form-row">
              <span>{{
                local.intervalType === 'timeInterval'
                  ? side === 'start'
                    ? '开始时间'
                    : '结束时间'
                  : '时间'
              }}</span>
              <div
                class="time-range-popover__custom"
                :class="{ 'time-range-popover__custom--with-time': baseGranularity === 'datetime' }"
              >
                <el-input-number
                  step-strictly
                  v-model="local[side]!.dynamic!.value"
                  :min="0"
                  controls-position="right"
                />
                <el-select v-model="local[side]!.dynamic!.unit" :teleported="false"
                  ><el-option v-for="unit in units" :key="unit.value" v-bind="unit"
                /></el-select>
                <el-select v-model="local[side]!.dynamic!.direction" :teleported="false"
                  ><el-option label="前" value="before" /><el-option label="后" value="after"
                /></el-select>
                <el-time-picker
                  v-if="baseGranularity === 'datetime'"
                  v-model="local[side]!.dynamic!.time"
                  value-format="HH:mm:ss"
                  format="HH:mm:ss"
                  :teleported="false"
                  style="width: 100%"
                />
              </div>
            </div>
          </template>
        </template>
        <div v-else class="time-range-popover__form-row">
          <span>{{ local.intervalType === 'timeInterval' ? '时间区间' : '时间' }}</span>
          <div class="time-range-popover__picker-control">
            <el-date-picker
              v-if="local.intervalType === 'timeInterval'"
              style="width: 100%"
              :model-value="[local.start!.value, local.end!.value]"
              :type="pickerType"
              :teleported="false"
              :value-format="pickerValueFormat"
              @update:model-value="value => { local.start!.value = value?.[0]; local.end!.value = value?.[1] }"
            />
            <el-date-picker
              v-else
              v-model="activeBoundary.value"
              style="width: 100%"
              :type="pickerType"
              :value-format="pickerValueFormat"
              :teleported="false"
            />
          </div>
        </div>
        <div class="time-range-popover__form-row">
          <span>预览</span>
          <div class="time-range-popover__picker-control">
            <el-date-picker
              v-if="local.intervalType === 'timeInterval'"
              :model-value="preview"
              style="width: 100%"
              :type="pickerType"
              disabled
            />
            <el-date-picker
              v-else
              :model-value="preview"
              style="width: 100%"
              :type="pickerType"
              disabled
            />
          </div>
        </div>
      </template>
    </div>
  </el-config-provider>
</template>

<style scoped>
.time-range-popover {
  padding: 8px 4px 16px;
}
.time-range-popover h4 {
  margin: 0 0 20px;
  font-size: 16px;
}
.time-range-popover__label {
  margin: 16px 0 10px;
}
.time-range-popover__radios {
  display: flex;
  flex-wrap: nowrap;
  gap: 14px;
}
.time-range-popover__form-row {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  width: 100%;
  min-width: 0;
  margin-top: 12px;
}
.time-range-popover__form-row > :last-child {
  min-width: 0;
}
.time-range-popover__custom {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 72px 72px;
  gap: 8px;
  min-width: 0;
}
.time-range-popover__custom--with-time {
  grid-template-columns: minmax(0, 1fr) 64px 64px 104px;
}
.time-range-popover__custom > * {
  width: 100%;
  min-width: 0;
}
.time-range-popover__picker-control {
  width: 100%;
  min-width: 0;
}
.time-range-popover__picker-control :deep(.el-date-editor) {
  width: 100% !important;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
}
.time-range-popover__form-row :deep(.el-input-number) {
  width: 100%;
  min-width: 0;
}
</style>
