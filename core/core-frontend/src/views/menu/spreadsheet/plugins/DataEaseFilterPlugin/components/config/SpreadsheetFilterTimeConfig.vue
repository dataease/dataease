<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Operation } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElConfigProvider } from 'element-plus-secondary'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterTimeFilterRange
} from '../../../../types/plugin'
import SpreadsheetFilterRenderer from '../renderers/SpreadsheetFilterRenderer.vue'
import SpreadsheetFilterTimeRangeDialog from './SpreadsheetFilterTimeRangeDialog.vue'
import { resolveDynamicTimeDefault } from '../../utils/time-filter'

const localeStore = useLocaleStoreWithOut()
const elLocale = computed(() => localeStore.getCurrentLocale.elLocale)

const props = defineProps<{ condition: SpreadsheetFilterCondition }>()
const rangePopoverVisible = ref(false)
const isRange = computed(() => props.condition.displayType === 'timeRange')
const allUnits = [
  { label: '年', value: 'year' },
  { label: '月', value: 'month' },
  { label: '日', value: 'day' }
]
const currentGranularity = computed(() =>
  (isRange.value ? props.condition.timeRangeGranularity : props.condition.timeGranularity).replace(
    'range',
    ''
  )
)
const units = computed(() => {
  const count =
    currentGranularity.value === 'year' ? 1 : currentGranularity.value === 'month' ? 2 : 3
  return allUnits.slice(0, count)
})
const relativeOptions = computed(() => {
  const granularity = isRange.value
    ? props.condition.timeRangeGranularity.replace('range', '')
    : props.condition.timeGranularity
  if (granularity === 'year')
    return [
      { label: '今年', value: 'thisYear' },
      { label: '去年', value: 'lastYear' },
      { label: '自定义', value: 'custom' }
    ]
  if (granularity === 'month') {
    const base = [
      { label: '本月', value: 'thisMonth' },
      { label: '上月', value: 'lastMonth' }
    ]
    return isRange.value
      ? [
          ...base,
          { label: '本季度', value: 'thisQuarter' },
          { label: '最近3个月', value: 'LastThreeMonths' },
          { label: '最近6个月', value: 'LastSixMonths' },
          { label: '最近12个月', value: 'LastTwelveMonths' },
          { label: '年初至本月', value: 'YearToThisMonth' },
          { label: '年初至上月末', value: 'YearToLastMonthEnd' },
          { label: '自定义', value: 'custom' }
        ]
      : [...base, { label: '自定义', value: 'custom' }]
  }
  if (isRange.value)
    return [
      { label: '今天', value: 'today' },
      { label: '昨天', value: 'yesterday' },
      { label: '本周', value: 'thisWeek' },
      { label: '本月', value: 'thisMonth' },
      { label: '最近3天', value: 'LastThreeDays' },
      { label: '月初至今', value: 'monthBeginning' },
      { label: '年初至今', value: 'yearBeginning' },
      { label: '年初至上月末', value: 'YearToLastMonthEnd' },
      { label: '月初至昨天', value: 'monthToYesterday' },
      { label: '完整上月', value: 'LastMonthFull' },
      { label: '自定义', value: 'custom' }
    ]
  return [
    { label: '今天', value: 'today' },
    { label: '昨天', value: 'yesterday' },
    { label: '月初', value: 'monthBeginning' },
    { label: '月底', value: 'monthEnd' },
    { label: '年初', value: 'yearBeginning' },
    { label: '自定义', value: 'custom' }
  ]
})
const dynamicPreview = computed(() => resolveDynamicTimeDefault(props.condition))
const rangeConfigured = computed(() => {
  const range = props.condition.timeFilterRange
  return range?.intervalType !== 'none' || !!range?.dynamicWindow
})
const applyRange = (value: SpreadsheetFilterTimeFilterRange) => {
  props.condition.timeFilterRange = value
}
const normalizeRelativeDefaults = () => {
  props.condition.timeDynamicDefault.offset.value = Math.abs(
    props.condition.timeDynamicDefault.offset.value || 0
  )
  props.condition.timeDynamicDefault.offset.relativeToCurrent ||= 'custom'
  props.condition.timeDynamicDefault.offset.direction ||= 'before'
  props.condition.timeDynamicDefault.time ||= dayjs().format('HH:mm:ss')
  for (const side of ['start', 'end'] as const) {
    props.condition.timeRangeDynamicDefault[side].value = Math.abs(
      props.condition.timeRangeDynamicDefault[side].value || 0
    )
    props.condition.timeRangeDynamicDefault[side].relativeToCurrent ||= 'custom'
    props.condition.timeRangeDynamicDefault[side].direction ||= 'before'
    props.condition.timeRangeDynamicDefault[side].time ||= dayjs().format('HH:mm:ss')
  }
}
normalizeRelativeDefaults()
watch(
  units,
  options => {
    const allowed = new Set(options.map(item => item.value))
    const fallback = options[options.length - 1]?.value || 'year'
    if (!allowed.has(props.condition.timeDynamicDefault.offset.unit)) {
      props.condition.timeDynamicDefault.offset.unit = fallback
    }
    for (const side of ['start', 'end'] as const) {
      if (!allowed.has(props.condition.timeRangeDynamicDefault[side].unit)) {
        props.condition.timeRangeDynamicDefault[side].unit = fallback
      }
    }
  },
  { immediate: true }
)
</script>

<template>
  <el-config-provider :locale="elLocale" namespace="ed">
    <el-form-item label="时间粒度">
      <el-select v-if="!isRange" v-model="condition.timeGranularity">
        <el-option label="年" value="year" /><el-option label="年月" value="month" />
        <el-option label="年月日" value="date" /><el-option label="年月日时分秒" value="datetime" />
      </el-select>
      <el-select v-else v-model="condition.timeRangeGranularity">
        <el-option label="年" value="yearrange" /><el-option label="年月" value="monthrange" />
        <el-option label="年月日" value="daterange" /><el-option
          label="年月日时分秒"
          value="datetimerange"
        />
      </el-select>
    </el-form-item>

    <div class="time-config__full-row">
      <div class="time-config__block">
        <el-checkbox v-model="condition.timeFilterRangeEnabled">设置时间筛选范围</el-checkbox>
        <div class="time-config__range-actions">
          <el-popover
            v-model:visible="rangePopoverVisible"
            trigger="click"
            :show-arrow="false"
            placement="bottom-start"
            :width="520"
            popper-class="spreadsheet-filter-time-range-popper"
            :disabled="!condition.timeFilterRangeEnabled"
          >
            <template #reference>
              <el-button
                class="time-config__setting"
                link
                type="primary"
                :disabled="!condition.timeFilterRangeEnabled"
              >
                <el-icon><Operation /></el-icon>设置
              </el-button>
            </template>
            <SpreadsheetFilterTimeRangeDialog
              :value="condition.timeFilterRange"
              :is-range="isRange"
              :granularity="isRange ? condition.timeRangeGranularity : condition.timeGranularity"
              @change="applyRange"
            />
          </el-popover>
          <span v-if="rangeConfigured" class="time-config__configured">已配置</span>
        </div>
      </div>
    </div>

    <div class="time-config__full-row">
      <el-checkbox v-model="condition.defaultValueEnabled">设置默认值</el-checkbox>
    </div>
    <template v-if="condition.defaultValueEnabled">
      <el-form-item label="默认值类型">
        <el-radio-group v-model="condition.timeDefaultType">
          <el-radio label="dynamic">动态时间</el-radio>
          <el-radio label="fixed">固定时间</el-radio>
        </el-radio-group>
      </el-form-item>
      <div v-if="condition.timeDefaultType === 'fixed'" class="time-config__fixed-value">
        <SpreadsheetFilterRenderer
          v-model="condition.defaultValue"
          :condition="condition"
          is-config
        />
      </div>
      <template v-else-if="!isRange">
        <div class="time-config__nested-row">
          <el-radio-group v-model="condition.timeDynamicDefault.offset.relativeToCurrent">
            <el-radio v-for="item in relativeOptions" :key="item.value" :label="item.value">{{
              item.label
            }}</el-radio>
          </el-radio-group>
        </div>
        <el-form-item
          v-if="condition.timeDynamicDefault.offset.relativeToCurrent === 'custom'"
          label="时间"
        >
          <div
            class="time-config__custom"
            :class="{ 'time-config__custom--with-time': condition.timeGranularity === 'datetime' }"
          >
            <el-input-number
              step-strictly
              v-model="condition.timeDynamicDefault.offset.value"
              :min="0"
              controls-position="right"
            />
            <el-select v-model="condition.timeDynamicDefault.offset.unit"
              ><el-option v-for="unit in units" :key="unit.value" v-bind="unit"
            /></el-select>
            <el-select v-model="condition.timeDynamicDefault.offset.direction"
              ><el-option label="前" value="before" /><el-option label="后" value="after"
            /></el-select>
            <div v-if="condition.timeGranularity === 'datetime'" class="time-config__time-control">
              <el-time-picker
                v-model="condition.timeDynamicDefault.time"
                value-format="HH:mm:ss"
                format="HH:mm:ss"
                style="width: 100%"
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="预览"
          ><SpreadsheetFilterRenderer :model-value="dynamicPreview" :condition="condition" disabled
        /></el-form-item>
      </template>
      <template v-else>
        <div class="time-config__nested-row">
          <el-radio-group v-model="condition.timeRangeDynamicDefault.start.relativeToCurrent">
            <el-radio v-for="item in relativeOptions" :key="item.value" :label="item.value">{{
              item.label
            }}</el-radio>
          </el-radio-group>
        </div>
        <template v-if="condition.timeRangeDynamicDefault.start.relativeToCurrent === 'custom'">
          <el-form-item
            v-for="side in (['start', 'end'] as const)"
            :key="side"
            :label="side === 'start' ? '开始时间' : '结束时间'"
          >
            <div
              class="time-config__custom"
              :class="{
                'time-config__custom--with-time': condition.timeRangeGranularity === 'datetimerange'
              }"
            >
              <el-input-number
                v-model="condition.timeRangeDynamicDefault[side].value"
                :min="0"
                step-strictly
                controls-position="right"
              />
              <el-select v-model="condition.timeRangeDynamicDefault[side].unit"
                ><el-option v-for="unit in units" :key="unit.value" v-bind="unit"
              /></el-select>
              <el-select v-model="condition.timeRangeDynamicDefault[side].direction"
                ><el-option label="前" value="before" /><el-option label="后" value="after"
              /></el-select>
              <div
                v-if="condition.timeRangeGranularity === 'datetimerange'"
                class="time-config__time-control"
              >
                <el-time-picker
                  v-model="condition.timeRangeDynamicDefault[side].time"
                  value-format="HH:mm:ss"
                  format="HH:mm:ss"
                  style="width: 100%"
                />
              </div>
            </div>
          </el-form-item>
        </template>
        <el-form-item label="预览"
          ><SpreadsheetFilterRenderer :model-value="dynamicPreview" :condition="condition" disabled
        /></el-form-item>
      </template>
    </template>
  </el-config-provider>
</template>

<style scoped>
.time-config__block {
  width: 100%;
}
.time-config__full-row {
  margin-bottom: 18px;
}
.time-config__nested-row {
  margin-bottom: 18px;
  padding-left: 48px;
}
.time-config__fixed-value {
  width: calc(100% - 48px);
  margin-bottom: 18px;
  padding-left: 48px;
}
.time-config__range-actions {
  display: flex;
  align-items: center;
  gap: 0;
  margin-top: 4px;
}
.time-config__setting {
  margin: 0;
  padding: 0;
}
.time-config__configured {
  display: inline-block;
  padding: 1px 4px;
  line-height: 14px;
  margin-left: 2px;
  border-radius: 2px;
  font-size: 10px;
  color: #646a73;
  background: #f0f1f3;
}
.time-config__custom {
  display: flex;
  gap: 8px;
  width: 100%;
  min-width: 0;
}
.time-config__custom > * {
  flex: 1 1 0;
  width: 0;
  min-width: 0;
}
.time-config__custom > :first-child {
  flex-grow: 0.8;
}
.time-config__custom :deep(.el-input-number) {
  width: 100%;
  min-width: 0;
}
.time-config__custom--with-time > :first-child {
  flex-grow: 0.8;
}
.time-config__custom--with-time > :nth-child(2),
.time-config__custom--with-time > :nth-child(3) {
  flex-grow: 0.7;
}
.time-config__custom--with-time > :nth-child(4) {
  flex-grow: 1.5;
}
.time-config__time-control {
  width: 0;
  min-width: 0;
}
.time-config__time-control :deep(.el-date-editor) {
  width: 100% !important;
  min-width: 0;
}
</style>
