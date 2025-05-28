<script lang="ts" setup>
import type { DatePickType } from 'element-plus-secondary'
import { toRefs, computed, watch } from 'vue'
import { type TimeRange } from './time-format'
import { useI18n } from '@/hooks/web/useI18n'
import DynamicTime from './DynamicTimeFiltering.vue'
import DynamicTimeRange from './DynamicTimeRangeFiltering.vue'
import { ManipulateType } from 'dayjs'
const props = withDefaults(
  defineProps<{
    timeRange: TimeRange
    timeGranularity: DatePickType
  }>(),
  {
    timeRange: () => ({
      intervalType: 'none',
      dynamicWindow: false,
      maximumSingleQuery: 0,
      regularOrTrends: 'fixed',
      relativeToCurrentRange: 'custom',
      regularOrTrendsValue: '',
      relativeToCurrent: 'custom',
      timeNum: 0,
      relativeToCurrentType: 'year',
      around: 'f',
      timeNumRange: 0,
      relativeToCurrentTypeRange: 'year',
      aroundRange: 'f'
    }),
    timeGranularity: 'year'
  }
)

const { t } = useI18n()
const intervalTypeList = [
  {
    label: t('chart.line_symbol_none'),
    value: 'none'
  },
  {
    label: t('v_query.start_at'),
    value: 'start'
  },
  {
    label: t('v_query.end_at'),
    value: 'end'
  },
  {
    label: t('v_query.time_interval'),
    value: 'timeInterval'
  }
]

const regularOrTrendsTitle = computed(() => {
  return intervalTypeList.find(ele => ele.value === timeRange.value.intervalType).label
})
const { timeRange } = toRefs(props)
const dynamicTime = computed(() => {
  return timeRange.value.regularOrTrends !== 'fixed'
})
const filterTypeCom = computed(() => {
  const { intervalType } = timeRange.value
  return intervalType === 'timeInterval' ? DynamicTimeRange : DynamicTime
})
const timeGranularityMultiple = computed<DatePickType>(() => {
  return (props.timeGranularity + 'range') as DatePickType
})
const aroundList = [
  {
    label: t('dynamic_time.before'),
    value: 'f'
  },
  {
    label: t('dynamic_time.after'),
    value: 'b'
  }
]
const relativeToCurrentTypeList = computed(() => {
  if (!timeRange.value) return []
  let index = ['year', 'month', 'date', 'datetime'].indexOf(props.timeGranularity) + 1
  return [
    {
      label: t('dynamic_time.year'),
      value: 'year'
    },
    {
      label: t('dynamic_time.month'),
      value: 'month'
    },
    {
      label: t('dynamic_time.date'),
      value: 'day'
    }
  ].slice(0, index)
})

const relativeToCurrentList = computed(() => {
  let list = []
  if (!timeRange.value) return list
  switch (props.timeGranularity) {
    case 'year':
      list = [
        {
          label: t('dynamic_year.current'),
          value: 'thisYear'
        },
        {
          label: t('dynamic_year.last'),
          value: 'lastYear'
        }
      ]
      break
    case 'month':
      list = [
        {
          label: t('cron.this_month'),
          value: 'thisMonth'
        },
        {
          label: t('dynamic_month.last'),
          value: 'lastMonth'
        }
      ]
      break
    case 'date':
      list = [
        {
          label: t('dynamic_time.today'),
          value: 'today'
        },
        {
          label: t('dynamic_time.yesterday'),
          value: 'yesterday'
        },
        {
          label: t('dynamic_time.firstOfMonth'),
          value: 'monthBeginning'
        },
        {
          label: t('dynamic_time.firstOfYear'),
          value: 'yearBeginning'
        }
      ]
      break
    case 'datetime':
      list = [
        {
          label: t('dynamic_time.today'),
          value: 'today'
        },
        {
          label: t('dynamic_time.yesterday'),
          value: 'yesterday'
        },
        {
          label: t('dynamic_time.firstOfMonth'),
          value: 'monthBeginning'
        },
        {
          label: t('dynamic_time.firstOfYear'),
          value: 'yearBeginning'
        }
      ]
      break

    default:
      break
  }

  return [
    ...list,
    {
      label: t('dynamic_time.custom'),
      value: 'custom'
    }
  ]
})

const relativeToCurrentListRange = computed(() => {
  let list = []
  if (!timeRange.value) return list
  switch (props.timeGranularity) {
    case 'year':
      list = [
        {
          label: t('dynamic_year.current'),
          value: 'thisYear'
        },
        {
          label: t('dynamic_year.last'),
          value: 'lastYear'
        }
      ]
      break
    case 'month':
      list = [
        {
          label: t('cron.this_month'),
          value: 'thisMonth'
        },
        {
          label: t('dynamic_month.last'),
          value: 'lastMonth'
        },
        {
          label: t('v_query.last_3_months'),
          value: 'LastThreeMonths'
        },
        {
          label: t('v_query.last_6_months'),
          value: 'LastSixMonths'
        },
        {
          label: t('v_query.last_12_months'),
          value: 'LastTwelveMonths'
        }
      ]
      break
    case 'date':
    case 'datetime':
      list = [
        {
          label: t('dynamic_time.today'),
          value: 'today'
        },
        {
          label: t('dynamic_time.yesterday'),
          value: 'yesterday'
        },
        {
          label: t('v_query.last_3_days'),
          value: 'LastThreeDays'
        },
        {
          label: t('v_query.month_to_date'),
          value: 'monthBeginning'
        },
        {
          label: t('v_query.year_to_date'),
          value: 'yearBeginning'
        }
      ]
      break

    default:
      break
  }

  return [
    ...list,
    {
      label: t('dynamic_time.custom'),
      value: 'custom'
    }
  ]
})
watch(
  () => relativeToCurrentListRange.value,
  val => {
    if (!val.some(ele => ele.value === timeRange.value.relativeToCurrentRange)) {
      timeRange.value.relativeToCurrentRange = val[0].value
    }
  },
  { immediate: true }
)

watch(
  () => relativeToCurrentList.value,
  val => {
    if (!val.some(ele => ele.value === timeRange.value.relativeToCurrent)) {
      timeRange.value.relativeToCurrent = val[0].value
    }
  },
  { immediate: true }
)

watch(
  () => relativeToCurrentTypeList.value,
  val => {
    if (!val.some(ele => ele.value === timeRange.value.relativeToCurrentType)) {
      timeRange.value.relativeToCurrentType = val[0].value as ManipulateType
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="set-time-filtering-range">
    <div class="title">{{ t('v_query.time_filter_range') }}</div>
    <div class="list-item">
      <div class="label">{{ t('v_query.interval_type') }}</div>
      <div class="setting-content">
        <div class="setting">
          <el-radio-group v-model="timeRange.intervalType">
            <el-radio v-for="ele in intervalTypeList" :key="ele.value" :label="ele.value">{{
              ele.label
            }}</el-radio>
          </el-radio-group>
        </div>
      </div>
    </div>
    <div class="list-item" v-if="timeRange.intervalType !== 'none'">
      <div class="label">{{ regularOrTrendsTitle }}</div>
      <div class="setting-content">
        <div class="setting">
          <el-radio-group v-model="timeRange.regularOrTrends">
            <el-radio label="fixed">{{ t('dynamic_time.fix') }}</el-radio>
            <el-radio label="dynamic">{{ t('dynamic_time.dynamic') }}</el-radio>
          </el-radio-group>
        </div>
        <template v-if="dynamicTime && timeRange.intervalType !== 'timeInterval'">
          <div class="setting" v-if="timeRange.intervalType !== 'timeInterval'">
            <div class="setting-label">{{ t('dynamic_time.relative') }}</div>
            <div class="setting-value select">
              <el-select :teleported="false" v-model="timeRange.relativeToCurrent">
                <el-option
                  v-for="item in relativeToCurrentList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
          <div class="setting" v-if="timeRange.relativeToCurrent === 'custom'">
            <div class="setting-input">
              <el-input-number v-model="timeRange.timeNum" :min="0" controls-position="right" />
              <el-select :teleported="false" v-model="timeRange.relativeToCurrentType">
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select :teleported="false" v-model="timeRange.around">
                <el-option
                  v-for="item in aroundList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
        </template>
        <template v-else-if="dynamicTime && timeRange.intervalType === 'timeInterval'">
          <div class="setting">
            <div class="setting-label">{{ t('dynamic_time.relative') }}</div>
            <div class="setting-value select">
              <el-select :teleported="false" v-model="timeRange.relativeToCurrentRange">
                <el-option
                  v-for="item in relativeToCurrentListRange"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
          <template v-if="timeRange.relativeToCurrentRange === 'custom'">
            <div
              class="setting"
              :class="['year', 'month'].includes(timeGranularity) && 'is-year-month-range'"
            >
              <div class="setting-label">{{ t('datasource.start_time') }}</div>
              <div class="setting-input range">
                <el-input-number
                  step-strictly
                  v-model="timeRange.timeNum"
                  :min="0"
                  controls-position="right"
                />
                <el-select :teleported="false" v-model="timeRange.relativeToCurrentType">
                  <el-option
                    v-for="item in relativeToCurrentTypeList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <el-select :teleported="false" v-model="timeRange.around">
                  <el-option
                    v-for="item in aroundList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </div>
            </div>
            <div
              class="setting"
              :class="['year', 'month'].includes(timeGranularity) && 'is-year-month-range'"
            >
              <div class="setting-label">{{ t('datasource.end_time') }}</div>
              <div class="setting-input range">
                <el-input-number
                  v-model="timeRange.timeNumRange"
                  :min="0"
                  step-strictly
                  controls-position="right"
                />
                <el-select :teleported="false" v-model="timeRange.relativeToCurrentTypeRange">
                  <el-option
                    v-for="item in relativeToCurrentTypeList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <el-select :teleported="false" v-model="timeRange.aroundRange">
                  <el-option
                    v-for="item in aroundList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </div>
            </div>
          </template>
        </template>
      </div>
      <div class="parameters" :class="dynamicTime && 'setting'">
        <div class="setting-label" v-if="dynamicTime">{{ t('template_manage.preview') }}</div>
        <div :class="dynamicTime ? 'setting-value' : 'w100'">
          <component
            :config="timeRange"
            :timeGranularityMultiple="timeGranularityMultiple"
            ref="inputCom"
            :is="filterTypeCom"
          ></component>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less">
.set-time-filtering-range {
  .ed-radio,
  .ed-checkbox.ed-checkbox--default {
    height: 22px;
    margin-right: 24px;
    --ed-radio-input-height: 16px;
    --ed-radio-input-width: 16px;
  }
  .ed-select {
    --ed-select-width: 100px;
  }
  .title {
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    margin-bottom: 16px;
  }
  .list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    flex-wrap: wrap;

    .setting-content {
      width: 100%;
      &.maximum-single-query {
        padding-left: 24px;
        display: flex;
        align-items: center;
        margin-top: 8px;
        .ed-input-number {
          width: 120px;
          margin: 0 8px;
        }
      }
    }

    &.top-item {
      .label {
        margin-bottom: auto;
        padding-top: 5.5px;
      }
    }
    .label {
      width: 100px;
      color: #1f2329;
    }

    .value {
      width: 321px;
      .value {
        margin-top: 8px;
        &:first-child {
          margin-top: -0.5px;
        }
      }
      .ed-select {
        width: 321px;
      }
    }

    .parameters {
      width: 100%;
      margin-top: 8px;
      .w100 {
        width: 100%;
      }
      .ed-date-editor,
      .ed-date-editor--datetime .ed-input__wrapper,
      .ed-select-v2 {
        width: 415px;
      }

      .ed-date-editor {
        .ed-input__wrapper {
          width: 100%;
        }
      }
    }
    .parameters-range {
      width: 100%;
      padding-left: 24px;
      display: flex;
      flex-wrap: wrap;

      .range-title,
      .params-start,
      .params-end {
        width: 50%;
      }

      .params-start,
      .params-end {
        margin-top: 8px;
        .ed-select {
          width: 100%;
        }
      }

      .params-end {
        padding-left: 4px;
      }

      .params-start {
        padding-right: 4px;
      }
    }

    .setting {
      &.setting {
        margin-top: 8px;
      }
      &.parameters {
        width: 100%;
        padding-left: 24px;

        .setting-label {
          margin-left: 0;
        }
        .ed-date-editor {
          width: 308px !important;
        }
      }
      margin-left: auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
      .setting-label {
        width: 80px;
        margin: 0 8px 0 24px;
      }

      .setting-value {
        &.select {
          .ed-select {
            width: 308px;
          }
        }
      }

      .setting-input {
        display: flex;
        padding-left: 112px;
        justify-content: flex-end;
        align-items: center;
        &.range {
          padding-left: 0px;
          width: 308px;
        }
        & > div + div {
          margin-left: 8px;
        }
      }

      &.is-year-month-range {
        .setting-input {
          .ed-date-editor.ed-input {
            display: none;
          }
        }
      }
    }
  }
}
</style>
<style>
.range-filter-time {
  padding: 15px !important;
}
</style>
