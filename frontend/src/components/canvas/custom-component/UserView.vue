<template>
  <div
    v-loading="requestStatus==='waiting'"
    :class="[
      {
        ['active']: active
      },
      'rect-shape'
    ]"
  >
    <!--    <i v-if="requestStatus==='success'" style="right:25px;position: absolute;z-index: 2" class="icon iconfont icon-fangda" @click.stop="openChartDetailsDialog" />-->
    <div v-if="requestStatus==='error'" class="chart-error-class">
      <div style="font-size: 12px; color: #9ea6b2;height: 100%;display: flex;align-items: center;justify-content: center;">
        {{ message }},{{ $t('chart.chart_show_error') }}
        <br>
        {{ $t('chart.chart_error_tips') }}
      </div>
    </div>
    <chart-component v-if="requestStatus==='success'&&chart.type && !chart.type.includes('table') && !chart.type.includes('text')" :ref="element.propValue.id" class="chart-class" :chart="chart" />
    <table-normal v-if="requestStatus==='success'&&chart.type && chart.type.includes('table')" :ref="element.propValue.id" :chart="chart" class="table-class" />
    <label-normal v-if="requestStatus==='success'&&chart.type && chart.type.includes('text')" :ref="element.propValue.id" :chart="chart" class="table-class" />
  </div>
</template>

<script>

import { viewData } from '@/api/panel/panel'
import { viewInfo } from '@/api/link'
import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'
import LabelNormal from '../../../views/chart/components/normal/LabelNormal'
import { uuid } from 'vue-uuid'

import { mapState } from 'vuex'
import { isChange } from '@/utils/conditionUtil'
import { BASE_CHART_STRING } from '@/views/chart/chart/chart'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy } from '@/components/canvas/utils/utils'
import { getToken, getLinkToken } from '@/utils/auth'
export default {
  name: 'UserView',
  components: { ChartComponent, TableNormal, LabelNormal },
  props: {
    element: {
      type: Object,
      default: null
    },
    // filters: {
    //   type: Array,
    //   required: false,
    //   default: null
    // },
    outStyle: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    },
    componentIndex: {
      type: Number,
      required: false
    }
  },
  data() {
    return {
      refId: null,
      chart: BASE_CHART_STRING,
      requestStatus: 'waiting',
      message: null
    }
  },
  computed: {
    filter() {
      const filter = {}
      filter.filter = this.element.filters
      return filter
    },
    filters() {
      // 必要 勿删勿该  watch数组，哪怕发生变化 oldValue等于newValue ，深拷贝解决
      if (!this.element.filters) return []
      return JSON.parse(JSON.stringify(this.element.filters))
    },
    ...mapState([
      'canvasStyleData'
    ])
  },

  watch: {
    'filters': function(val1, val2) {
      // this.getData(this.element.propValue.viewId)
      isChange(val1, val2) && this.getData(this.element.propValue.viewId)
    },
    // deep监听panel 如果改变 提交到 store
    canvasStyleData: {
      handler(newVal, oldVla) {
        // this.chart.stylePriority == panel 优先使用仪表板样式
        this.mergeStyle()
      },
      deep: true
    },
    // 监听外部的样式变化
    outStyle: {
      handler(newVal, oldVla) {
        if (this.$refs[this.element.propValue.id]) {
          this.$refs[this.element.propValue.id].chartResize()
        }
      },
      deep: true
    },
    // 监听外部计时器变化
    searchCount: function(val1) {
      if (val1 > 0) {
        this.getData(this.element.propValue.viewId)
      }
    }
  },

  created() {
    this.refId = uuid.v1
    // this.filter.filter = this.$store.getters.conditions
    this.getData(this.element.propValue.viewId)
  },
  mounted() {
  },
  methods: {
    mergeStyle() {
      // this.chart.stylePriority == panel 优先使用仪表板样式
      if ((this.requestStatus === 'success' || this.requestStatus === 'merging') && this.chart.stylePriority === 'panel' && this.canvasStyleData.chart) {
        const customAttrChart = JSON.parse(this.chart.customAttr)
        const customStyleChart = JSON.parse(this.chart.customStyle)

        const customAttrPanel = JSON.parse(this.canvasStyleData.chart.customAttr)
        const customStylePanel = JSON.parse(this.canvasStyleData.chart.customStyle)

        // 组件样式-标题设置 - 标题修改为组件自己控制
        // customStyleChart.text = customStylePanel.text
        // 组件样式-背景设置
        customStyleChart.background = customStylePanel.background
        // 图形属性-颜色设置
        if (this.chart.type.includes('table')) {
          customAttrChart.color = customAttrPanel.tableColor
        } else {
          customAttrChart.color = customAttrPanel.color
        }

        this.chart = {
          ...this.chart,
          customAttr: JSON.stringify(customAttrChart),
          customStyle: JSON.stringify(customStyleChart)
        }
      }
    },
    getData(id) {
      if (id) {
        this.requestStatus = 'waiting'
        this.message = null

        // 增加判断 仪表板公共连接中使用viewInfo 正常使用viewData
        let method = viewData
        if (!getToken() && getLinkToken()) {
          method = viewInfo
        }

        method(id, this.filter).then(response => {
          // 将视图传入echart组件
          if (response.success) {
            this.chart = response.data
            this.requestStatus = 'merging'
            this.mergeStyle()
            this.requestStatus = 'success'
          } else {
            this.requestStatus = 'error'
            this.message = response.message
          }
          return true
        }).catch(err => {
          this.requestStatus = 'error'
          if (err && err.response && err.response.data) {
            this.message = err.response.data.message
          } else {
            this.message = err
          }
          return true
        })
      }
    },
    viewIdMatch(viewIds, viewId) {
      return !viewIds || viewIds.length === 0 || viewIds.includes(viewId)
    },
    openChartDetailsDialog() {
      const tableChart = deepCopy(this.chart)
      tableChart.customAttr = JSON.parse(this.chart.customAttr)
      tableChart.customStyle = JSON.parse(this.chart.customStyle)
      tableChart.customAttr.color.tableHeaderBgColor = '#f8f8f9'
      tableChart.customAttr.color.tableItemBgColor = '#ffffff'
      tableChart.customAttr.color.tableFontColor = '#7c7e81'
      tableChart.customAttr.color.tableStripe = true
      tableChart.customStyle.text.show = false
      tableChart.customAttr = JSON.stringify(tableChart.customAttr)
      tableChart.customStyle = JSON.stringify(tableChart.customStyle)
      eventBus.$emit('openChartDetailsDialog', { chart: this.chart, tableChart: tableChart })
    }
  }
}
</script>

<style lang="scss" scoped>
.rect-shape {
    width: 100%;
    height: 100%;
    overflow: hidden;
}
.chart-class{
  height: 100%;
}
.table-class{
  height: 100%;
}
.chart-error-class{
  text-align: center;
  height: calc(100% - 84px);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ece7e7;
}
.active {

}

.active >>> .icon-fangda{
  z-index: 2;
  display:block!important;
}

.rect-shape > i{
  right: 5px;
  color: gray;
  position: absolute;
}

.rect-shape >>> i:hover {
  color: red;
}

.rect-shape:hover >>> .icon-fangda {
  z-index: 2;
  display:block;
}

.rect-shape>>>.icon-fangda {
  display:none
}

.rect-shape:hover >>> .icon-shezhi {
  z-index: 2;
  display:block;
}

.rect-shape>>>.icon-shezhi {
  display:none
}
</style>
