<template>
  <div class="set-time-filtering-range">
    <div class="title">{{ $t("time.set_time_filtering_range") }}</div>
    <div class="list-item">
      <div class="label">{{ $t("time.interval_type") }}</div>
      <div class="setting-content">
        <div class="setting">
          <el-radio-group v-model="timeRange.intervalType">
            <el-radio
              v-for="ele in intervalTypeList"
              :key="ele.value"
              :label="ele.value"
            >{{ $t(`time.${ele.label}`) }}</el-radio>
          </el-radio-group>
        </div>
      </div>
    </div>
    <div
      v-if="timeRange.intervalType !== 'none'"
      class="list-item"
    >
      <div class="label">{{ $t(`time.${regularOrTrendsTitle}`) }}</div>
      <div class="setting-content">
        <div class="setting">
          <el-radio-group v-model="timeRange.regularOrTrends">
            <el-radio label="fixed">{{ $t("time.fixed_time") }}</el-radio>
            <el-radio label="dynamic">{{ $t("time.dynamic_time") }}</el-radio>
          </el-radio-group>
        </div>
        <template
          v-if="dynamicTime && timeRange.intervalType !== 'timeInterval'"
        >
          <div class="setting">
            <div class="setting-label">
              {{ $t("time.relative_to_current") }}
            </div>
            <div class="setting-value select">
              <el-select
                v-model="timeRange.relativeToCurrent"
                size="small"
              >
                <el-option
                  v-for="item in relativeToCurrentList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
          <div
            v-if="timeRange.relativeToCurrent === 'custom'"
            class="setting"
          >
            <div class="setting-input">
              <el-input-number
                v-model="timeRange.timeNum"
                size="small"
                :min="0"
                controls-position="right"
              />
              <el-select
                v-model="timeRange.relativeToCurrentType"
                size="small"
              >
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
              <el-select
                v-model="timeRange.around"
                size="small"
              >
                <el-option
                  v-for="item in aroundList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
        </template>
        <template
          v-else-if="dynamicTime && timeRange.intervalType === 'timeInterval'"
        >
          <div class="setting">
            <div class="setting-label">{{ $t("time.start_time") }}</div>
            <div class="setting-input range">
              <el-input-number
                v-model="timeRange.timeNum"
                size="small"
                :min="0"
                controls-position="right"
              />
              <el-select
                v-model="timeRange.relativeToCurrentType"
                size="small"
              >
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
              <el-select
                v-model="timeRange.around"
                size="small"
              >
                <el-option
                  v-for="item in aroundList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
          <div class="setting">
            <div class="setting-label">{{ $t("time.end_time") }}</div>
            <div class="setting-input range">
              <el-input-number
                v-model="timeRange.timeNumRange"
                size="small"
                :min="0"
                controls-position="right"
              />
              <el-select
                v-model="timeRange.relativeToCurrentTypeRange"
                size="small"
              >
                <el-option
                  v-for="item in relativeToCurrentTypeList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
              <el-select
                v-model="timeRange.aroundRange"
                size="small"
              >
                <el-option
                  v-for="item in aroundList"
                  :key="item.value"
                  :label="$t(`time.${item.label}`)"
                  :value="item.value"
                />
              </el-select>
            </div>
          </div>
        </template>
      </div>
      <div
        class="parameters"
        :class="dynamicTime && 'setting'"
      >
        <div
          v-if="dynamicTime"
          class="setting-label"
        >
          {{ $t("time.preview") }}
        </div>
        <div :class="dynamicTime ? 'setting-value' : 'w100'">
          <el-date-picker
            :key="timeInterval"
            v-model="timeRange.regularOrTrendsValue"
            :disabled="timeRange.regularOrTrends !== 'fixed'"
            :type="timeInterval"
            size="small"
            :start-placeholder="$t('dataset.start_time')"
            :end-placeholder="$t('dataset.end_time')"
          />
        </div>
      </div>
    </div>
    <div class="list-item">
      <div class="label">
        <el-checkbox
          v-model="timeRange.dynamicWindow"
          :label="$t('time.dynamic_query_time_window')"
        />
      </div>
      <div
        v-if="timeRange.dynamicWindow"
        class="setting-content maximum-single-query"
      >
        {{ $t("time.maximum_single_query") }}
        <el-input-number
          v-model="timeRange.maximumSingleQuery"
          :min="1"
          step-strictly
          size="small"
          controls-position="right"
        />
        {{ $t("time.day_s") }}
      </div>
    </div>
    <div class="popover-foot">
      <de-btn
        secondary
        @click="closeFilter"
      >{{ $t("chart.cancel") }} </de-btn>
      <de-btn
        type="primary"
        @click="changeFilter"
      >{{ $t("chart.confirm") }}
      </de-btn>
    </div>
  </div>
</template>

<script>
import { getThisStart, getLastStart, getAround } from './time-format-dayjs'
import { cloneDeep } from 'lodash-es'
const intervalTypeList = [
  {
    label: 'nothing',
    value: 'none'
  },
  {
    label: 'starting_from',
    value: 'start'
  },
  {
    label: 'end_at',
    value: 'end'
  },
  {
    label: 'time_interval',
    value: 'timeInterval'
  }
]
const aroundList = [
  {
    label: 'ago',
    value: 'f'
  },
  {
    label: 'after',
    value: 'b'
  }
]
const relativeToCurrentTypeList = [
  {
    label: 'year',
    value: 'year'
  },
  {
    label: 'month',
    value: 'month'
  },
  {
    label: 'day',
    value: 'day'
  }
]

const relativeToCurrentList = [
  {
    label: 'today',
    value: 'today'
  },
  {
    label: 'yesterday',
    value: 'yesterday'
  },
  {
    label: 'at_the_beginning_of_the_month',
    value: 'monthBeginning'
  },
  {
    label: 'at_the_beginning_of_the_year',
    value: 'yearBeginning'
  },
  {
    label: 'custom',
    value: 'custom'
  }
]

const defaultObj = {
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
}
export default {
  props: {
    timeRangeData: {
      type: Object,
      default: () => defaultObj
    }
  },
  data() {
    return {
      intervalTypeList,
      relativeToCurrentTypeList,
      aroundList,
      relativeToCurrentList,
      timeRange: {
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
      }
    }
  },
  computed: {
    timeConfig() {
      const {
        timeNum,
        relativeToCurrentType,
        around,
        intervalType,
        relativeToCurrent,
        timeNumRange,
        relativeToCurrentTypeRange,
        aroundRange
      } = this.timeRange
      return {
        timeNum,
        relativeToCurrentType,
        relativeToCurrent,
        around,
        intervalType,
        timeNumRange,
        relativeToCurrentTypeRange,
        aroundRange
      }
    },
    regularOrTrendsTitle() {
      return intervalTypeList.find(
        (ele) => ele.value === this.timeRange.intervalType
      ).label
    },
    dynamicTime() {
      return this.timeRange.regularOrTrends !== 'fixed'
    },
    timeInterval() {
      return this.timeRange.intervalType === 'timeInterval'
        ? 'daterange'
        : 'date'
    }
  },
  watch: {
    timeConfig: {
      handler() {
        this.init()
      },
      deep: true
    }
  },
  created() {
    this.timeRange = cloneDeep(this.timeRangeData)
    this.init()
  },
  methods: {
    init() {
      const {
        timeNum,
        relativeToCurrentType,
        around,
        relativeToCurrent,
        intervalType,
        regularOrTrendsValue,
        timeNumRange,
        relativeToCurrentTypeRange,
        aroundRange,
        regularOrTrends
      } = this.timeRange
      if (regularOrTrends === 'fixed') {
        if (intervalType === 'timeInterval') {
          if (
            Array.isArray(regularOrTrendsValue) &&
            !!regularOrTrendsValue.length
          ) { return }
          this.timeRange.regularOrTrendsValue = [
            getAround(relativeToCurrentType, 'add', 0),
            getAround(relativeToCurrentType, 'add', 1)
          ]
          return
        }
        if (!Array.isArray(regularOrTrendsValue) && !!regularOrTrendsValue) { return }
        this.timeRange.regularOrTrendsValue = new Date()
        return
      }
      if (intervalType === 'timeInterval') {
        const startTime = getAround(
          relativeToCurrentType,
          around === 'f' ? 'subtract' : 'add',
          timeNum
        )
        const endTime = getAround(
          relativeToCurrentTypeRange,
          aroundRange === 'f' ? 'subtract' : 'add',
          timeNumRange
        )
        this.timeRange.regularOrTrendsValue = [startTime, endTime]
        return
      }
      if (relativeToCurrent === 'custom') {
        this.timeRange.regularOrTrendsValue = getAround(
          relativeToCurrentType,
          around === 'f' ? 'subtract' : 'add',
          timeNum
        )
      } else {
        switch (relativeToCurrent) {
          case 'thisYear':
            this.timeRange.regularOrTrendsValue = getThisStart('year')
            break
          case 'lastYear':
            this.timeRange.regularOrTrendsValue = getLastStart('year')
            break
          case 'thisMonth':
            this.timeRange.regularOrTrendsValue = getThisStart('month')
            break
          case 'lastMonth':
            this.timeRange.regularOrTrendsValue = getLastStart('month')
            break
          case 'today':
            this.timeRange.regularOrTrendsValue = getThisStart('day')
            break
          case 'yesterday':
            this.timeRange.regularOrTrendsValue = getLastStart('day')
            break
          case 'monthBeginning':
            this.timeRange.regularOrTrendsValue = getThisStart('month')
            break
          case 'yearBeginning':
            this.timeRange.regularOrTrendsValue = getThisStart('year')
            break

          default:
            break
        }
      }
    },
    closeFilter() {
      this.timeRange = cloneDeep(this.timeRangeData)
      this.$emit('changeData', null)
    },
    changeFilter() {
      const {
        timeNum,
        relativeToCurrentType,
        around,
        intervalType,
        timeNumRange,
        regularOrTrends,
        relativeToCurrentTypeRange,
        aroundRange
      } = this.timeRange
      if (intervalType === 'timeInterval' && regularOrTrends === 'dynamic') {
        const startTime = getAround(
          relativeToCurrentType,
          around === 'f' ? 'subtract' : 'add',
          timeNum
        )
        const endTime = getAround(
          relativeToCurrentTypeRange,
          aroundRange === 'f' ? 'subtract' : 'add',
          timeNumRange
        )
        if (+endTime < +startTime) {
          this.$message.error(this.$t('time.end_time_start_time'))
          return
        }
      }
      this.$emit('changeData', cloneDeep(this.timeRange))
    }
  }
}
</script>
<style lang="less">
.set-time-filtering-range {
  color: #1f2329;
  .el-radio,
  .el-checkbox.el-checkbox--default {
    height: 22px;
    margin-right: 24px;
    --el-radio-input-height: 16px;
    --el-radio-input-width: 16px;
  }
  .popover-foot {
    height: 64px;
    text-align: right;
    padding: 16px;
    width: calc(100% + 24px);
    margin: 0 0 -12px -12px;
    border-top: 1px solid #1f232926;
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
        margin-top: 8px;
        align-items: center;
        .el-input-number {
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
      .el-select {
        width: 321px;
      }
    }

    .parameters {
      width: 100%;
      margin-top: 8px;
      .w100 {
        width: 100%;
      }
      .el-date-editor,
      .el-date-editor--datetime .el-input__wrapper,
      .el-select-v2 {
        width: 415px;
      }

      .el-date-editor {
        .el-input__wrapper {
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
        .el-select {
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
        .el-date-editor {
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
          .el-select {
            width: 308px;
          }
        }
      }

      .setting-input {
        display: flex;
        padding-left: 118px;
        justify-content: flex-end;
        align-items: center;
        &.range {
          padding-left: 0px;
          width: 308px;
        }
        & > div + div {
          margin-left: 8px;
        }

        .el-select {
          width: 97px;
        }
        .el-input-number {
          width: 98px;
        }
      }

      &.is-year-month-range {
        .setting-input {
          .el-date-editor.el-input {
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
