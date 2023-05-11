<template>
  <span style="position: relative;display: inline-block;">
    <i
      class="el-icon-arrow-down el-icon-delete"
      style="position: absolute;top: 6px;right: 24px;color: #878d9f;cursor: pointer;z-index: 1;"
      @click="removeItem"
    />
    <el-dropdown
      trigger="click"
      size="mini"
      @command="clickItem"
    >
      <span class="el-dropdown-link">
        <el-tag
          size="small"
          class="item-axis"
          :type="tagType"
        >
          <span style="float: left">
            <svg-icon
              v-if="item.deType === 0"
              icon-class="field_text"
              class="field-icon-text"
            />
            <svg-icon
              v-if="item.deType === 1"
              icon-class="field_time"
              class="field-icon-time"
            />
            <svg-icon
              v-if="item.deType === 2 || item.deType === 3"
              icon-class="field_value"
              class="field-icon-value"
            />
            <svg-icon
              v-if="item.deType === 5"
              icon-class="field_location"
              class="field-icon-location"
            />
            <svg-icon
              v-if="chart.type ==='chart-mix' && item.chartType === 'bar'"
              icon-class="bar"
              class-name="field-icon-sort"
            />
            <svg-icon
              v-if="chart.type ==='chart-mix' && item.chartType === 'line'"
              icon-class="line"
              class-name="field-icon-sort"
            />
            <svg-icon
              v-if="chart.type ==='chart-mix' && item.chartType === 'scatter'"
              icon-class="scatter"
              class-name="field-icon-sort"
            />
            <svg-icon
              v-if="item.sort === 'asc'"
              icon-class="sort-asc"
              class-name="field-icon-sort"
            />
            <svg-icon
              v-if="item.sort === 'desc'"
              icon-class="sort-desc"
              class-name="field-icon-sort"
            />
          </span>
          <span
            class="item-span-style"
            :title="item.name"
          >{{ item.name }}</span>
          <field-error-tips v-if="tagType === 'danger'" />
          <span
            v-if="chart.type !== 'table-info' && item.summary && !item.chartId"
            class="summary-span"
          >
            {{ $t('chart.' + item.summary) }}<span v-if="false && item.compareCalc && item.compareCalc.type && item.compareCalc.type !== '' && item.compareCalc.type !== 'none'">-{{ $t('chart.' + item.compareCalc.type) }}</span>
          </span>
          <i
            class="el-icon-arrow-down el-icon--right"
            style="position: absolute;top: 6px;right: 10px;"
          />
        </el-tag>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-show="chart.type ==='chart-mix'">
            <el-dropdown
              placement="right-start"
              size="mini"
              style="width: 100%"
              @command="switchChartType"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-s-data" />
                  <span>{{ $t('chart.chart_type') }}</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeSwitch('bar')">{{ $t('chart.chart_bar') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSwitch('line')">{{ $t('chart.chart_line') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSwitch('scatter')">{{ $t('chart.chart_scatter') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item
            v-show="!item.chartId && chart.type !== 'table-info'"
            :divided="chart.type === 'chart-mix'"
          >
            <el-dropdown
              placement="right-start"
              size="mini"
              style="width: 100%"
              @command="summary"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-notebook-2" />
                  <span>{{ $t('chart.summary') }}</span>
                  <span class="summary-span-item">({{ $t('chart.'+item.summary) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1 && item.deType !== 5"
                  :command="beforeSummary('sum')"
                >{{ $t('chart.sum') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1 && item.deType !== 5"
                  :command="beforeSummary('avg')"
                >{{ $t('chart.avg') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1 && item.deType !== 5"
                  :command="beforeSummary('max')"
                >{{ $t('chart.max') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1 && item.deType !== 5"
                  :command="beforeSummary('min')"
                >{{ $t('chart.min') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1 && item.deType !== 5"
                  :command="beforeSummary('stddev_pop')"
                >{{ $t('chart.stddev_pop') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="item.id !== 'count' && item.deType !== 0 && item.deType !== 1 && item.deType !== 5"
                  :command="beforeSummary('var_pop')"
                >{{ $t('chart.var_pop') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSummary('count')">{{ $t('chart.count') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="item.id !== 'count'"
                  :command="beforeSummary('count_distinct')"
                >{{ $t('chart.count_distinct') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>

          <!--同比/环比等快速计算-->
          <el-dropdown-item v-show="!item.chartId && chart.type !== 'table-info'">
            <el-dropdown
              placement="right-start"
              size="mini"
              style="width: 100%"
              @command="quickCalc"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-s-grid" />
                  <span>{{ $t('chart.quick_calc') }}</span>
                  <span class="summary-span-item">({{ !item.compareCalc ? $t('chart.none') : $t('chart.' + item.compareCalc.type) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeQuickCalc('none')">{{ $t('chart.none') }}</el-dropdown-item>
                <el-dropdown-item
                  :disabled="disableEditCompare"
                  :command="beforeQuickCalc('setting')"
                >{{ $t('chart.yoy_label') }}...</el-dropdown-item>
                <el-dropdown-item
                  :disabled="quotaViews.indexOf(chart.type) > -1"
                  :command="beforeQuickCalc('percent')"
                >{{ $t('chart.percent') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>

          <el-dropdown-item :divided="!item.chartId && chart.type !== 'table-info'">
            <el-dropdown
              placement="right-start"
              size="mini"
              style="width: 100%"
              @command="sort"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-sort" />
                  <span>{{ $t('chart.sort') }}</span>
                  <span class="summary-span-item">({{ $t('chart.'+item.sort) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="beforeSort('none')">{{ $t('chart.none') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSort('asc')">{{ $t('chart.asc') }}</el-dropdown-item>
                <el-dropdown-item :command="beforeSort('desc')">{{ $t('chart.desc') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item
            icon="el-icon-files"
            :command="beforeClickItem('filter')"
          >
            <span>{{ $t('chart.filter') }}...</span>
          </el-dropdown-item>
          <el-dropdown-item
            v-if="chart.render === 'antv' && chart.type !== 'gauge' && chart.type !== 'liquid'"
            icon="el-icon-notebook-2"
            divided
            :command="beforeClickItem('formatter')"
          >
            <span>{{ $t('chart.value_formatter') }}...</span>
          </el-dropdown-item>
          <el-dropdown-item
            icon="el-icon-edit-outline"
            divided
            :command="beforeClickItem('rename')"
          >
            <span>{{ $t('chart.show_name_set') }}</span>
          </el-dropdown-item>
          <el-dropdown-item
            icon="el-icon-delete"
            divided
            :command="beforeClickItem('remove')"
          >
            <span>{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </span>
    </el-dropdown>
  </span>
</template>

<script>
import { compareItem } from '@/views/chart/chart/compare'
import { getItemType, getOriginFieldName, resetValueFormatter } from '@/views/chart/components/dragItem/utils'
import FieldErrorTips from '@/views/chart/components/dragItem/components/FieldErrorTips'
import bus from '@/utils/bus'
import { formatterItem } from '@/views/chart/chart/formatter'
import { quotaViews } from '@/views/chart/chart/util'
import { SUPPORT_Y_M } from '@/views/chart/chart/chart'

export default {
  name: 'QuotaItem',
  components: { FieldErrorTips },
  props: {
    param: {
      type: Object,
      required: true
    },
    item: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    dimensionData: {
      type: Array,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      compareItem: compareItem,
      disableEditCompare: false,
      tagType: 'success',
      formatterItem: formatterItem,
      quotaViews: quotaViews
    }
  },
  watch: {
    'chart': function() {
      this.isEnableCompare()
    },
    quotaData: function() {
      this.getItemTagType()
    },
    item: function() {
      this.getItemTagType()
    }
  },
  mounted() {
    this.init()
    this.isEnableCompare()
    bus.$on('reset-change-table', this.getItemTagType)
  },
  beforeDestroy() {
    bus.$off('reset-change-table', this.getItemTagType)
  },
  methods: {
    init() {
      if (!this.item.compareCalc) {
        this.item.compareCalc = JSON.parse(JSON.stringify(this.compareItem))
      }
      if (!this.item.formatterCfg) {
        this.item.formatterCfg = JSON.parse(JSON.stringify(this.formatterItem))
      }
    },
    isEnableCompare() {
      let xAxis = null
      if (Object.prototype.toString.call(this.chart.xaxis) === '[object Array]') {
        xAxis = JSON.parse(JSON.stringify(this.chart.xaxis))
      } else {
        xAxis = JSON.parse(this.chart.xaxis)
      }
      const t1 = xAxis.filter(ele => {
        return ele.deType === 1 && SUPPORT_Y_M.includes(ele.dateStyle)
      })

      if (this.chart.type === 'table-pivot') {
        let xAxisExt = null
        if (Object.prototype.toString.call(this.chart.xaxisExt) === '[object Array]') {
          xAxisExt = JSON.parse(JSON.stringify(this.chart.xaxisExt))
        } else {
          xAxisExt = JSON.parse(this.chart.xaxisExt)
        }
        const t2 = xAxisExt.filter(ele => {
          return ele.deType === 1 && SUPPORT_Y_M.includes(ele.dateStyle)
        })

        t1.push(...t2)
      }

      // 暂时只支持类别轴/维度的时间类型字段
      if (t1.length > 0 && this.chart.type !== 'text' && this.chart.type !== 'label' && this.chart.type !== 'gauge' && this.chart.type !== 'liquid') {
        this.disableEditCompare = false
      } else {
        this.disableEditCompare = true
      }
    },
    clickItem(param) {
      if (!param) {
        return
      }
      switch (param.type) {
        case 'rename':
          this.showRename()
          break
        case 'remove':
          this.removeItem()
          break
        case 'filter':
          this.editFilter()
          break
        case 'formatter':
          this.valueFormatter()
          break
        default:
          break
      }
    },
    beforeClickItem(type) {
      return {
        type: type
      }
    },

    summary(param) {
      this.item.summary = param.type
      this.$emit('onQuotaItemChange', this.item)
    },
    beforeSummary(type) {
      return {
        type: type
      }
    },

    switchChartType(param) {
      this.item.chartType = param.type
      this.$emit('onQuotaItemChange', this.item)
    },
    beforeSwitch(type) {
      return {
        type: type
      }
    },

    quickCalc(param) {
      switch (param.type) {
        case 'none':
          // 选择占比外，设置自动
          resetValueFormatter(this.item)
          this.item.compareCalc.type = 'none'
          this.$emit('onQuotaItemChange', this.item)
          break
        case 'setting':
          // 选择占比外，设置自动
          resetValueFormatter(this.item)
          this.editCompare()
          break
        case 'percent':
          // 选择占比，自动将数值格式设置为百分比并保留2位小数
          this.item.formatterCfg.type = 'percent'
          this.item.formatterCfg.decimalCount = 2

          this.item.compareCalc.type = 'percent'
          this.$emit('onQuotaItemChange', this.item)
          break
        default:
          break
      }
    },
    beforeQuickCalc(type) {
      return {
        type: type
      }
    },

    sort(param) {
      this.item.sort = param.type
      this.$emit('onQuotaItemChange', this.item)
    },
    beforeSort(type) {
      return {
        type: type
      }
    },
    showRename() {
      this.item.index = this.index
      this.item.renameType = 'quota'
      this.item.dsFieldName = getOriginFieldName(this.dimensionData, this.quotaData, this.item)
      this.$emit('onNameEdit', this.item)
    },
    removeItem() {
      this.item.index = this.index
      this.item.removeType = 'quota'
      this.$emit('onQuotaItemRemove', this.item)
    },
    editFilter() {
      this.item.index = this.index
      this.item.filterType = 'quota'
      this.$emit('editItemFilter', this.item)
    },

    editCompare() {
      this.item.index = this.index
      this.item.calcType = 'quota'
      this.$emit('editItemCompare', this.item)
    },
    getItemTagType() {
      this.tagType = getItemType(this.dimensionData, this.quotaData, this.item)
    },

    valueFormatter() {
      this.item.index = this.index
      this.item.formatterType = 'quota'
      this.$emit('valueFormatter', this.item)
    }
  }
}
</script>

<style scoped>
  .item-axis {
    padding: 1px 6px;
    margin: 0 3px 2px 3px;
    text-align: left;
    height: 24px;
    line-height: 22px;
    display: flex;
    border-radius: 4px;
    box-sizing: border-box;
    white-space: nowrap;
    width: 159px;
  }

  .item-axis:hover {
    background-color: #fdfdfd;
    cursor: pointer;
  }

  span {
    font-size: 12px;
  }

  .summary-span-item{
    margin-left: 4px;
    color: #878d9f;
  }

  .summary-span{
    margin-left: 4px;
    color: #878d9f;
    position: absolute;
    right: 40px;
  }

  .inner-dropdown-menu{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%
  }

  .item-span-style{
    display: inline-block;
    width: 80px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
</style>
