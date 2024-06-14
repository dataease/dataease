<template>
  <div class="date-picker-vant">
    <el-date-picker
      v-if="element.options !== null && element.options.attrs !== null && show"
      ref="dateRef"
      v-model="values"
      :popper-class="'coustom-date-picker' + ' ' + extPoperClass"
      :class="{ 'show-required-tips': showRequiredTips }"
      :type="componentType"
      :range-separator="$t(element.options.attrs.rangeSeparator)"
      :start-placeholder="
        showRequiredTips
          ? $t('panel.required_tips')
          : $t(element.options.attrs.startPlaceholder)
      "
      :end-placeholder="
        showRequiredTips
          ? $t('panel.required_tips')
          : $t(element.options.attrs.endPlaceholder)
      "
      :placeholder="
        showRequiredTips
          ? $t('panel.required_tips')
          : $t(element.options.attrs.placeholder)
      "
      :append-to-body="inScreen"
      value-format="timestamp"
      :format="labelFormat"
      :size="size"
      :editable="false"
      :picker-options="pickerOptions"
      :default-time="defaultRangeTime"
      @change="dateChange"
      @focus="toFocus"
      @blur="onBlur"
    />
    <div
      v-if="isMobileStatus"
      class="vant-mobile"
      :class="isRange && 'wl50'"
      @click="showPopup"
    />
    <div
      v-if="isMobileStatus && isRange"
      class="vant-mobile"
      :class="
        ['datetimerange', 'datetime', 'daterange'].includes(componentType) &&
          'wr50'
      "
      @click="showPopupRight"
    />
    <van-popup
      v-if="isMobileStatus"
      v-model="showDate"
      get-container="body"
      position="bottom"
      style="height: auto"
    >
      <van-datetime-picker
        v-if="showdDatetimePicker"
        v-model="currentDate"
        :type="componentTypeVant"
        title="选择时间"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="confirm"
        @cancel="cancel"
      />
      <van-picker
        v-else
        title="选择时间"
        :default-index="defaultIndex"
        show-toolbar
        :columns="columns"
        @confirm="onConfirm"
        @cancel="onCancel"
      />
    </van-popup>
  </div>
</template>

<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
import { timeSection } from '@/utils'
import dayjs from 'dayjs'
import {
  getThisStart,
  getLastStart,
  getAround
} from '@/views/panel/filter/filterMain/time-format-dayjs.js'
import bus from '@/utils/bus'
import customInput from '@/components/widget/deWidget/customInput'
import {
  dateMap,
  years,
  seconds
} from '@/components/widget/deWidget/serviceNameFn'
import { mapState } from 'vuex'
import vanPopup from 'vant/lib/popup'
import vanDatetimePicker from 'vant/lib/datetime-picker'
import vanPicker from 'vant/lib/picker'
import 'vant/lib/popup/style'
import 'vant/lib/datetime-picker/style'
import 'vant/lib/picker/style'
export default {
  components: { vanPopup, vanDatetimePicker, vanPicker },
  mixins: [customInput],
  props: {
    canvasId: {
      type: String,
      required: true
    },
    element: {
      type: Object,
      default: null
    },
    inDraw: {
      type: Boolean,
      default: true
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    size: String,
    isRelation: {
      type: Boolean,
      default: false
    },
    terminal: {
      type: String,
      default: 'pc'
    }
  },
  data() {
    return {
      showDate: false,
      startWindowTime: 0,
      minDate: new Date(1980, 0, 1),
      maxDate: new Date(2025, 10, 1),
      currentDate: new Date(),
      operator: 'between',
      defaultIndex: 2,
      columns: years,
      values: null,
      onFocus: false,
      show: true,
      selectSecondInput: false,
      selectSecond: false,
      outTimer: null,
      innerTimer: null
    }
  },
  computed: {
    isMobileStatus() {
      return this.mobileStatus || this.terminal === 'mobile'
    },
    isRange() {
      if (!this.isMobileStatus) return false
      return ['datetimerange', 'daterange'].includes(this.componentType)
    },
    showdDatetimePicker() {
      if (!this.isMobileStatus) return false
      if (this.showSecond && this.selectSecond) return false
      return this.componentTypeVant !== 'year'
    },
    showSecond() {
      if (!this.isMobileStatus) return false
      return this.labelFormat?.endsWith('ss')
    },
    componentTypeVant() {
      if (!this.isMobileStatus) return ''
      if (this.showSecond) {
        return 'datetime'
      }
      if (this.labelFormat?.endsWith('mm')) {
        return 'datetime'
      }
      if (this.labelFormat?.endsWith('HH')) {
        return 'datehour'
      }
      return dateMap[this.componentType]
    },
    extPoperClass() {
      if (
        this.labelFormat &&
        this.labelFormat.includes('HH') &&
        !this.labelFormat.includes('HH:mm')
      ) {
        return 'de-no-minite'
      }
      return ''
    },
    defaultoptions() {
      if (
        !this.element ||
        !this.element.options ||
        !this.element.options.attrs.default
      ) {
        return ''
      }
      return JSON.stringify(this.element.options.attrs.default)
    },
    defaultValueStr() {
      if (
        !this.element ||
        !this.element.options ||
        !this.element.options.value
      ) {
        return ''
      }
      return this.element.options.value.toString()
    },
    viewIds() {
      if (
        !this.element ||
        !this.element.options ||
        !this.element.options.attrs.viewIds
      ) {
        return ''
      }
      return this.element.options.attrs.viewIds.toString()
    },
    manualModify() {
      return !!this.element.options.manualModify
    },
    isTimeWidget() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      return widget.isTimeWidget && widget.isTimeWidget()
    },
    componentType() {
      let result = this.element.options.attrs.type || 'date'
      if (this.isTimeWidget && this.element.options.attrs.showTime) {
        result =
          this.element.serviceName === 'timeDateWidget'
            ? 'datetime'
            : 'datetimerange'
      }
      return result
    },
    labelFormat() {
      const result = 'yyyy-MM-dd'
      if (
        this.isTimeWidget &&
        this.element.options.attrs.showTime &&
        this.element.options.attrs.accuracy
      ) {
        return result + ' ' + this.element.options.attrs.accuracy
      }
      return null
    },
    pickerOptions() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      if (this.element.options.attrs.type === 'daterange' && widget.shortcuts) {
        const cuts = widget.shortcuts()
        const result = cuts.map((cut) => {
          return {
            text: this.$t(cut.text),
            onClick: (picker) => {
              const param = cut.callBack()
              this.startWindowTime = param[0]
              const disabled = param.some((ele) => {
                return this.disabledDate(ele - 1000)
              })
              this.startWindowTime = 0
              if (disabled) return
              picker.$emit('pick', param)
            }
          }
        })
        return {
          shortcuts: result,
          disabledDate: (val) => {
            return this.disabledDate(val)
          },
          onPick: ({ minDate }) => {
            this.startWindowTime = +new Date(minDate)
          }
        }
      }
      return null
    },
    defaultRangeTime() {
      if (
        this.element.options.attrs.type === 'daterange' &&
        this.element.options.attrs.showTime
      ) {
        return ['00:00:00', '23:59:59']
      }
      return null
    },
    showRequiredTips() {
      return (
        this.inDraw &&
        this.element.options.attrs.required &&
        (!this.values || this.values.length === 0)
      )
    },
    ...mapState(['canvasStyleData', 'mobileStatus'])
  },
  watch: {
    values: function(val, old) {
      if (!this.inDraw) {
        this.$emit('widget-value-changed', val)
      }
    },
    viewIds: function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    defaultValueStr: function(value, old) {
      if (this.element.options.attrs.default.isDynamic) {
        return
      }
      if (value === old) return
      this.values = this.fillValueDerfault()
      this.dateChange(value)
    },
    defaultoptions: function(val, old) {
      if (!this.element.options.attrs.default.isDynamic) {
        this.values = this.fillValueDerfault(false)
        this.dateChange(this.values)
        return
      }
      if (val === old) return
      const widget = ApplicationContext.getService(this.element.serviceName)
      this.values = widget.dynamicDateFormNow(this.element)
      this.dateChange(this.values)
    },
    labelFormat: function(val, old) {
      if (val !== old) {
        this.show = false
        this.$nextTick(() => {
          this.show = true
        })
      }
    }
  },
  created() {
    this.loadInit()
    this.$nextTick(() => {
      this.dynamicRefresh()
    })
  },
  mounted() {
    bus.$on('onScroll', this.onScroll)
    if (this.inDraw) {
      bus.$on('reset-default-value', this.resetDefaultValue)
    }
    const _this = this
    window.addEventListener('message', function(event) {
      if (event.data === 'closeFilterComponent') {
        _this.$refs.dateRef.hidePicker()
      }
    })
  },
  beforeDestroy() {
    this.clearTime()
    bus.$off('onScroll', this.onScroll)
    bus.$off('reset-default-value', this.resetDefaultValue)
  },
  methods: {
    disabledDate(val) {
      const timeStamp = +new Date(val)
      if (!this.element.options.attrs.setTimeRange) {
        return false
      }
      const {
        intervalType,
        regularOrTrends,
        regularOrTrendsValue,
        relativeToCurrent,
        timeNum,
        relativeToCurrentType,
        around,
        dynamicWindow,
        maximumSingleQuery,
        timeNumRange,
        relativeToCurrentTypeRange,
        aroundRange
      } = this.element.options.attrs.timeRange || {}
      let isDynamicWindowTime = false
      if (this.startWindowTime && dynamicWindow) {
        isDynamicWindowTime =
          dayjs(this.startWindowTime)
            .add(maximumSingleQuery, 'day')
            .startOf('day')
            .valueOf() -
            1000 <
          timeStamp
      }
      if (intervalType === 'none') {
        if (dynamicWindow) return isDynamicWindowTime
        return false
      }
      let startTime
      if (relativeToCurrent === 'custom') {
        startTime = getAround(
          relativeToCurrentType,
          around === 'f' ? 'subtract' : 'add',
          timeNum
        )
      } else {
        switch (relativeToCurrent) {
          case 'thisYear':
            startTime = getThisStart('year')
            break
          case 'lastYear':
            startTime = getLastStart('year')
            break
          case 'thisMonth':
            startTime = getThisStart('month')
            break
          case 'lastMonth':
            startTime = getLastStart('month')
            break
          case 'today':
            startTime = getThisStart('day')
            break
          case 'yesterday':
            startTime = getLastStart('day')
            break
          case 'monthBeginning':
            startTime = getThisStart('month')
            break
          case 'yearBeginning':
            startTime = getThisStart('year')
            break

          default:
            break
        }
      }
      const startValue =
        regularOrTrends === 'fixed' ? regularOrTrendsValue : startTime

      if (intervalType === 'start') {
        return timeStamp < +new Date(startValue) || isDynamicWindowTime
      }

      if (intervalType === 'end') {
        return timeStamp > +new Date(startValue) || isDynamicWindowTime
      }

      if (intervalType === 'timeInterval') {
        const startTime =
          regularOrTrends === 'fixed'
            ? regularOrTrendsValue[0]
            : getAround(
              relativeToCurrentType,
              around === 'f' ? 'subtract' : 'add',
              timeNum
            )
        const endTime =
          regularOrTrends === 'fixed'
            ? regularOrTrendsValue[1]
            : getAround(
              relativeToCurrentTypeRange,
              aroundRange === 'f' ? 'subtract' : 'add',
              timeNumRange
            )
        return (
          timeStamp < +new Date(startTime) - 1000 ||
          timeStamp > +new Date(endTime) ||
          isDynamicWindowTime
        )
      }
    },
    showPopupRight() {
      // eslint-disable-next-line
      const [_, end] = this.values || [];
      this.currentDate = new Date(end || new Date())
      this.selectSecondInput = true
      this.showDate = true
    },
    cancel() {
      this.showDate = false
    },
    confirm() {
      this.setArrValue()
      if (this.showSecond) {
        this.columns = seconds
        this.selectSecond = true
      }
      if (this.selectSecond || this.componentTypeVant === 'year') {
        return
      }
      this.showDate = false
      this.mobileDateChange()
    },
    onCancel() {
      this.showDate = false
      if (this.showSecond) {
        this.selectSecond = false
      }
    },
    setArrValue(val) {
      if (!this.isRange) {
        if (this.selectSecond) {
          this.values = this.values + val * 1000
          return
        }
        this.values = val ? +new Date(val) : +new Date(this.currentDate)
        return
      }
      const [start, end] = this.values || []
      if (this.selectSecond) {
        if (this.selectSecondInput) {
          this.values = [start, +new Date(this.currentDate) + val * 1000]
        } else {
          this.values = [+new Date(this.currentDate) + val * 1000, end]
        }
        return
      }
      if (this.selectSecondInput) {
        this.values = [start, +new Date(this.currentDate)]
      } else {
        this.values = [+new Date(this.currentDate), end]
      }
    },
    onConfirm(val) {
      this.showDate = false
      this.setArrValue(val)
      if (this.showSecond) {
        this.columns = years
        this.selectSecond = false
      }
      this.mobileDateChange()
    },
    mobileDateChange() {
      if (this.isRange) {
        const [start, end] = this.values || []
        if (!start || !end) return
      }
      this.dateChange(this.values)
    },
    showPopup() {
      if (this.isRange) {
        const [start] = this.values || []
        this.currentDate = new Date(start || new Date())
      } else {
        this.currentDate = new Date(this.values || new Date())
        if (this.componentTypeVant === 'year') {
          this.defaultIndex = years.findIndex(
            (ele) => `${this.currentDate.getFullYear()}` === ele
          )
        }
      }
      this.selectSecondInput = false
      this.showDate = true
    },
    loadInit() {
      this.clearTime()
      const existLastValidFilters =
        this.$store.state.lastValidFilters &&
        this.$store.state.lastValidFilters[this.element.id]
      if (
        this.element.options.attrs.default?.isDynamic &&
        !existLastValidFilters
      ) {
        return this.refreshHandler()
      }
      if (this.element.options.value || existLastValidFilters) {
        this.values = this.fillValueDerfault()
        this.dateChange(this.values)
      }
    },
    refreshHandler() {
      if (this.element.options.attrs.default?.isDynamic) {
        const widget = ApplicationContext.getService(this.element.serviceName)
        this.values = widget.dynamicDateFormNow(this.element)
        this.dateChange(this.values)
        return true
      }
      return false
    },
    clearTime() {
      if (this.outTimer) {
        clearTimeout(this.outTimer)
        this.outTimer = null
      }
      if (this.innerTimer) {
        clearInterval(this.innerTimer)
        this.innerTimer = null
      }
    },
    dynamicRefresh() {
      if (this.inDraw && this.element.options.attrs.default?.isDynamic) {
        const nowDate = new Date()
        const nowTime = nowDate.getTime()
        const tomorrow = new Date(
          `${nowDate.getFullYear()}-${nowDate.getMonth() + 1}-${
            nowDate.getDate() + 1
          } 00:00:01`
        )
        const tomorrowTime = tomorrow.getTime()
        this.clearTime()
        this.outTimer = setTimeout(() => {
          if (this.inDraw) {
            this.refreshHandler()
          }
          this.innerTimer = setInterval(
            () => {
              if (this.inDraw) {
                this.refreshHandler()
              }
            },
            24 * 3600 * 1000
          )
        }, tomorrowTime - nowTime)
      }
    },
    clearHandler() {
      this.values = null
    },
    onScroll() {
      if (this.onFocus) {
        this.$refs.dateRef.hidePicker()
      }
    },
    textSame(str1, str2) {
      if (str1 === null && str2 === null) {
        return true
      }
      if (
        str1 !== null &&
        str2 !== null &&
        typeof str1 !== 'undefined' &&
        typeof str2 !== 'undefined'
      ) {
        return str1.toString() === str2.toString()
      }
      return false
    },
    resetDefaultValue(ele) {
      const id = ele.id
      const eleVal = ele.options.value.toString()
      if (
        this.inDraw &&
        this.manualModify &&
        this.element.id === id &&
        !this.textSame(this.values, eleVal) &&
        this.textSame(this.defaultValueStr, eleVal)
      ) {
        if (!this.element.options.attrs.default.isDynamic) {
          this.values = this.fillValueDerfault()
          this.dateChange(this.values)
          return
        }
        const widget = ApplicationContext.getService(this.element.serviceName)
        this.values = widget.dynamicDateFormNow(this.element)
        this.dateChange(this.values)
      }
    },
    onBlur() {
      this.startWindowTime = 0
      this.onFocus = false
    },
    toFocus() {
      this.onFocus = true
      this.$nextTick(() => {
        this.handleCoustomStyle()
      })
    },
    search() {
      this.setCondition()
    },
    getCondition() {
      const param = {
        canvasId: this.canvasId,
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      param.value = this.formatValues(param.value)
      return param
    },
    setCondition() {
      if (this.showRequiredTips) {
        return
      }
      const param = this.getCondition()
      !this.isRelation &&
        this.inDraw &&
        this.$store.commit('addViewFilter', param)
    },
    dateChange(value) {
      if (!this.inDraw) {
        if (value === null) {
          this.element.options.value = ''
        } else {
          this.element.options.value = Array.isArray(value)
            ? value.join()
            : value.toString()
        }
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
        if (!this.showRequiredTips) {
          this.$store.commit('setLastValidFilters', {
            componentId: this.element.id,
            val:
              this.values && Array.isArray(this.values)
                ? this.values.join(',')
                : this.values
          })
        }
      }
      this.setCondition()
    },
    formatFilterValue() {
      if (this.values === null) return []
      if (Array.isArray(this.values)) return this.values
      return [this.values]
    },
    formatValues(values) {
      if (!values || values.length === 0) {
        return []
      }
      if (this.element.options.attrs.type === 'daterange') {
        if (values.length !== 2) {
          return null
        }
        let start = values[0]
        let end = values[1]
        start = timeSection(start, 'datetime', this.labelFormat)[0]
        end = timeSection(end, 'datetime', this.labelFormat)[1]

        const results = [start, end]
        return results
      } else {
        const value = values[0]
        return timeSection(
          parseFloat(value),
          this.componentType || this.element.options.attrs.type,
          this.labelFormat
        )
      }
    },
    fillValueDerfault(useLastFilter = true) {
      let defaultV =
        this.element.options.value === null
          ? ''
          : this.element.options.value.toString()
      if (this.inDraw && useLastFilter) {
        let lastFilters = null
        if (this.$store.state.lastValidFilters) {
          lastFilters = this.$store.state.lastValidFilters[this.element.id]
          if (lastFilters) {
            defaultV =
              lastFilters.val === null || typeof lastFilters.val === 'undefined'
                ? ''
                : lastFilters.val.toString()
          }
        }
      }
      if (this.element.options.attrs.type === 'daterange') {
        if (
          defaultV === null ||
          typeof defaultV === 'undefined' ||
          defaultV === '' ||
          defaultV === '[object Object]'
        ) {
          return []
        }
        return defaultV.split(',').map((item) => parseFloat(item))
      } else {
        if (
          defaultV === null ||
          typeof defaultV === 'undefined' ||
          defaultV === '' ||
          defaultV === '[object Object]'
        ) {
          return null
        }
        return parseFloat(defaultV.split(',')[0])
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.show-required-tips {
  border-color: #ff0000 !important;
  ::v-deep .el-input__inner {
    color: #ff0000 !important;
  }
  ::v-deep input::placeholder {
    color: #ff0000 !important;
  }
  ::v-deep i {
    color: #ff0000 !important;
  }
}
.show-required-tips ::v-deep .el-input__inner,
input {
  border-color: #ff0000 !important;
}

.show-required-tips ::v-deep .el-input__inner,
input::placeholder {
  color: #ff0000 !important;
}
.show-required-tips ::v-deep i {
  color: #ff0000 !important;
}
</style>
<style lang="scss">
.date-picker-vant {
  position: relative;
  width: 100%;
  .el-date-editor {
    width: 100% !important;
  }
  .vant-mobile {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    &.wl50 {
      width: 50%;
    }
    &.wr50 {
      left: auto;
      right: 0;
      width: 50%;
    }
  }
}
.coustom-date-picker {
  right: 0px;
  border: 1px solid var(--BrDateColor, #dfe4ed) !important;
  background: var(--BgDateColor, #ffffff) !important;

  .el-picker-panel__sidebar {
    background: var(--BgDateColor, #ffffff) !important;
    border-right: 1px solid var(--BrDateColor, #dfe4ed) !important;

    .el-picker-panel__shortcut {
      color: var(--DateColor, #606266);
    }
  }

  .el-date-range-picker__time-header,
  .el-date-picker__time-header {
    border-bottom: 1px solid var(--BrDateColor, #dfe4ed) !important;
  }

  .el-picker-panel__footer {
    border-top: 1px solid var(--BrDateColor, #dfe4ed) !important;
    background: var(--BgDateColor, #ffffff) !important;
  }

  .el-date-range-picker__time-picker-wrap,
  .el-date-picker__time-header {
    .el-input__inner {
      border: 1px solid var(--BrDateColor, #dfe4ed) !important;
      color: var(--DateColor, #606266);
      background: var(--BgDateColor, #ffffff) !important;
    }
  }

  .el-picker-panel__link-btn:nth-child(2) {
    color: var(--DateColor, #409eff);
    background: var(--BgDateColor, #ffffff) !important;
    border: 1px solid var(--BrDateColor, #dfe4ed) !important;
  }

  .popper__arrow,
  .popper__arrow::after {
    display: none !important;
  }

  .el-date-range-picker__content.is-left {
    border-right: 1px solid var(--BrDateColor, #e6ebf5) !important;
  }

  .el-date-table th,
  .el-date-picker__header--bordered {
    border-bottom: 1px solid var(--BrDateColor, #e6ebf5) !important;
  }

  .el-date-table td.in-range:not(.end-date):not(.start-date) div span {
    color: #3370ff;
  }

  .el-date-range-picker__header,
  .el-date-table th,
  .el-date-table__row,
  .el-month-table td .cell,
  .el-year-table td .cell,
  .el-picker-panel__icon-btn,
  .el-date-picker__header-label {
    color: var(--DateColor, #606266);
  }

  .el-month-table td.current:not(.disabled) .cell,
  .el-month-table td.today:not(.disabled) .cell,
  .el-year-table td.current:not(.disabled) .cell,
  .el-year-table td.today:not(.disabled) .cell {
    color: #409eff;
  }
}

.de-no-minite {
  .el-time-spinner__wrapper {
    width: 100% !important;
  }

  .el-scrollbar:nth-of-type(2) {
    display: none;
  }
}
</style>
