<script lang="ts" setup>
import { toRefs, computed, PropType } from 'vue'
import { type TimeRange } from './time-format'
import DynamicTime from './DynamicTimeFiltering.vue'
import DynamicTimeRange from './DynamicTimeRangeFiltering.vue'
const props = defineProps({
  timeRange: {
    type: Object as PropType<TimeRange>,
    default: () => ({
      intervalType: 'none',
      dynamicWindow: false,
      maximumSingleQuery: 0,
      regularOrTrends: 'fixed',
      regularOrTrendsValue: '',
      relativeToCurrent: 'custom',
      timeNum: 0,
      relativeToCurrentType: 'year',
      around: 'f',
      timeNumRange: 0,
      relativeToCurrentTypeRange: 'year',
      aroundRange: 'f'
    })
  },
  timeGranularityMultiple: {
    type: String,
    default: 'yearrange'
  }
})
const intervalTypeList = [
  {
    label: '无',
    value: 'none'
  },
  {
    label: '开始于',
    value: 'start'
  },
  {
    label: '结束于',
    value: 'end'
  },
  {
    label: '时间区间',
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

const aroundList = [
  {
    label: '前',
    value: 'f'
  },
  {
    label: '后',
    value: 'b'
  }
]
const relativeToCurrentTypeList = computed(() => {
  if (!timeRange.value) return []
  let index =
    ['yearrange', 'monthrange', 'daterange', 'datetimerange'].indexOf(
      props.timeGranularityMultiple
    ) + 1
  return [
    {
      label: '年',
      value: 'year'
    },
    {
      label: '月',
      value: 'month'
    },
    {
      label: '日',
      value: 'day'
    }
  ].slice(0, index)
})

const relativeToCurrentTypeListTips = computed(() => {
  return (relativeToCurrentTypeList.value[relativeToCurrentTypeList.value.length - 1] || {}).label
})
const relativeToCurrentList = computed(() => {
  let list = []
  if (!timeRange.value) return list
  switch (props.timeGranularityMultiple) {
    case 'yearrange':
      list = [
        {
          label: '今年',
          value: 'thisYear'
        },
        {
          label: '去年',
          value: 'lastYear'
        }
      ]
      break
    case 'monthrange':
      list = [
        {
          label: '本月',
          value: 'thisMonth'
        },
        {
          label: '上月',
          value: 'lastMonth'
        }
      ]
      break
    case 'daterange':
      list = [
        {
          label: '今天',
          value: 'today'
        },
        {
          label: '昨天',
          value: 'yesterday'
        },
        {
          label: '月初',
          value: 'monthBeginning'
        },
        {
          label: '年初',
          value: 'yearBeginning'
        }
      ]
      break
    case 'datetimerange':
      list = [
        {
          label: '今天',
          value: 'today'
        },
        {
          label: '昨天',
          value: 'yesterday'
        },
        {
          label: '月初',
          value: 'monthBeginning'
        },
        {
          label: '年初',
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
      label: '自定义',
      value: 'custom'
    }
  ]
})
</script>

<template>
  <div class="set-time-filtering-range">
    <div class="title">设置时间筛选范围</div>
    <div class="list-item">
      <div class="label">区间类型</div>
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
            <el-radio label="fixed">固定时间</el-radio>
            <el-radio label="dynamic">动态时间</el-radio>
          </el-radio-group>
        </div>
        <template v-if="dynamicTime && timeRange.intervalType !== 'timeInterval'">
          <div class="setting">
            <div class="setting-label">相对当前</div>
            <div class="setting-value select">
              <el-select v-model="timeRange.relativeToCurrent">
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
              <el-select v-model="timeRange.relativeToCurrentType">
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select v-model="timeRange.around">
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
          <div
            class="setting"
            :class="
              ['yearrange', 'monthrange'].includes(timeGranularityMultiple) && 'is-year-month-range'
            "
          >
            <div class="setting-label">开始时间</div>
            <div class="setting-input range">
              <el-input-number v-model="timeRange.timeNum" :min="0" controls-position="right" />
              <el-select v-model="timeRange.relativeToCurrentType">
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select v-model="timeRange.around">
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
            :class="
              ['yearrange', 'monthrange'].includes(timeGranularityMultiple) && 'is-year-month-range'
            "
          >
            <div class="setting-label">结束时间</div>
            <div class="setting-input range">
              <el-input-number
                v-model="timeRange.timeNumRange"
                :min="0"
                controls-position="right"
              />
              <el-select v-model="timeRange.relativeToCurrentTypeRange">
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select v-model="timeRange.aroundRange">
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
      </div>
      <div class="parameters" :class="dynamicTime && 'setting'">
        <div class="setting-label" v-if="dynamicTime">预览</div>
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
    <div class="list-item">
      <div class="label">
        <el-checkbox v-model="timeRange.dynamicWindow" label="动态查询时间窗口" />
      </div>
      <div v-if="timeRange.dynamicWindow" class="setting-content maximum-single-query">
        单次查询最多
        <el-input-number
          v-model="timeRange.maximumSingleQuery"
          :min="1"
          controls-position="right"
        />
        {{ relativeToCurrentTypeListTips }}
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
