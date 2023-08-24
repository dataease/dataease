<template>
  <el-date-picker
    v-if="element.options!== null && element.options.attrs!==null && show"
    ref="dateRef"
    v-model="values"
    :popper-class="'coustom-date-picker' + ' ' + extPoperClass"
    :type="componentType"
    :range-separator="$t(element.options.attrs.rangeSeparator)"
    :start-placeholder="$t(element.options.attrs.startPlaceholder)"
    :end-placeholder="$t(element.options.attrs.endPlaceholder)"
    :placeholder="$t(element.options.attrs.placeholder)"
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
</template>

<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
import { timeSection } from '@/utils'
import bus from '@/utils/bus'
import customInput from '@/components/widget/deWidget/customInput'
import { mapState } from 'vuex'
export default {
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
    }
  },
  data() {
    return {
      operator: 'between',
      values: null,
      onFocus: false,
      show: true,
      timer: null
    }
  },
  computed: {
    extPoperClass() {
      if (this.labelFormat && this.labelFormat.includes('HH') && !this.labelFormat.includes('HH:mm')) {
        return 'de-no-minite'
      }
      return ''
    },
    defaultoptions() {
      if (!this.element || !this.element.options || !this.element.options.attrs.default) return ''
      return JSON.stringify(this.element.options.attrs.default)
    },
    defaultValueStr() {
      if (!this.element || !this.element.options || !this.element.options.value) return ''
      return this.element.options.value.toString()
    },
    viewIds() {
      if (!this.element || !this.element.options || !this.element.options.attrs.viewIds) return ''
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
        result = this.element.serviceName === 'timeDateWidget' ? 'datetime' : 'datetimerange'
      }
      return result
    },
    labelFormat() {
      const result = 'yyyy-MM-dd'
      if (this.isTimeWidget && this.element.options.attrs.showTime && this.element.options.attrs.accuracy) {
        return result + ' ' + this.element.options.attrs.accuracy
      }
      return null
    },
    pickerOptions() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      if (this.element.options.attrs.type === 'daterange' && widget.shortcuts) {
        const cuts = widget.shortcuts()
        const result = cuts.map(cut => {
          return {
            text: this.$t(cut.text),
            onClick: picker => {
              const param = cut.callBack()
              picker.$emit('pick', param)
            }
          }
        })
        return {
          shortcuts: result
        }
      }
      return null
    },
    defaultRangeTime() {
      if (this.element.options.attrs.type === 'daterange' && this.element.options.attrs.showTime) {
        return ['00:00:00', '23:59:59']
      }
      return null
    },
    ...mapState([
      'canvasStyleData'
    ])

  },
  watch: {
    canvasStyleData: {
      handler(newVal, oldVla) {
        this.canvasStyleDataInit()
      },
      deep: true
    },
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    'defaultValueStr': function(value, old) {
      if (this.element.options.attrs.default.isDynamic) {
        return
      }
      if (value === old) return
      this.values = this.fillValueDerfault()
      this.dateChange(value)
    },
    'defaultoptions': function(val, old) {
      if (!this.element.options.attrs.default.isDynamic) {
        this.values = this.fillValueDerfault()
        this.dateChange(this.values)
        return
      }
      if (val === old) return
      const widget = ApplicationContext.getService(this.element.serviceName)
      this.values = widget.dynamicDateFormNow(this.element)
      this.dateChange(this.values)
    },
    'labelFormat': function(val, old) {
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
    this.canvasStyleDataInit()
  },
  mounted() {
    bus.$on('onScroll', this.onScroll)
    if (this.inDraw) {
      bus.$on('reset-default-value', this.resetDefaultValue)
    }
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer)
    bus.$off('onScroll', this.onScroll)
    bus.$off('reset-default-value', this.resetDefaultValue)
  },
  methods: {
    loadInit() {
      if (this.element.options.attrs.default && this.element.options.attrs.default.isDynamic) {
        if (this.element.options.attrs.default) {
          const widget = ApplicationContext.getService(this.element.serviceName)
          this.values = widget.dynamicDateFormNow(this.element)
          this.dateChange(this.values)
        }
        return
      }
      if (this.element.options.value) {
        this.values = this.fillValueDerfault()
        this.dateChange(this.values)
      }
    },
    canvasStyleDataInit() {
      if (this.inDraw && this.canvasStyleData.refreshViewEnable && this.element.options.attrs.default && this.element.options.attrs.default.isDynamic) {
        this.searchCount = 0
        this.timer && clearInterval(this.timer)
        let refreshTime = 300000
        if (this.canvasStyleData.refreshTime && this.canvasStyleData.refreshTime > 0) {
          if (this.canvasStyleData.refreshUnit === 'second') {
            refreshTime = this.canvasStyleData.refreshTime * 1000
          } else {
            refreshTime = this.canvasStyleData.refreshTime * 60000
          }
        }
        this.timer = setInterval(() => {
          this.loadInit()
          this.searchCount++
        }, refreshTime)
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
    resetDefaultValue(id) {
      if (this.inDraw && this.manualModify && this.element.id === id) {
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
      const param = this.getCondition()
      !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
    },
    dateChange(value) {
      if (!this.inDraw) {
        if (value === null) {
          this.element.options.value = ''
        } else {
          this.element.options.value = Array.isArray(value) ? value.join() : value.toString()
        }
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
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
        return timeSection(parseFloat(value), this.componentType || this.element.options.attrs.type, this.labelFormat)
      }
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (this.element.options.attrs.type === 'daterange') {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV ===
          '[object Object]') {
          return []
        }
        return defaultV.split(',').map(item => parseFloat(item))
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV ===
          '[object Object]') {
          return null
        }
        return parseFloat(defaultV.split(',')[0])
      }
    }
  }
}

</script>

<style lang="scss">
.coustom-date-picker {
  border: 1px solid var(--BrDateColor, #dfe4ed) !important;
  background: var(--BgDateColor, #FFFFFF) !important;

  .el-picker-panel__sidebar {
    background: var(--BgDateColor, #FFFFFF) !important;
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
    background: var(--BgDateColor, #FFFFFF) !important;
  }

  .el-date-range-picker__time-picker-wrap,
  .el-date-picker__time-header {
    .el-input__inner {
      border: 1px solid var(--BrDateColor, #dfe4ed) !important;
      color: var(--DateColor, #606266);
      background: var(--BgDateColor, #FFFFFF) !important;
    }
  }

  .el-picker-panel__link-btn:nth-child(2) {
    color: var(--DateColor, #409eff);
    background: var(--BgDateColor, #FFFFFF) !important;
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
    color: #409EFF;
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
