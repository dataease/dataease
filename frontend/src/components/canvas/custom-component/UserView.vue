<template>
  <div v-loading="requestStatus==='waiting'" class="rect-shape">
    <div v-if="requestStatus==='error'" style=";width: 100%;height: 100%;background-color: #ece7e7; text-align: center">
      <div style="font-size: 12px; color: #9ea6b2;">
        获取数据出错 请联系管理员<br>
        {{ message }}
      </div>
    </div>
    <chart-component v-if="requestStatus==='success'&&chart.type && !chart.type.includes('table')" :ref="element.propValue.id" class="chart-class" :chart="chart" />
    <table-normal v-if="requestStatus==='success'&&chart.type && chart.type.includes('table')" :chart="chart" class="table-class" />
  </div>
</template>

<script>

import { viewData } from '@/api/panel/panel'
import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'

import { mapState } from 'vuex'

import {
  DEFAULT_COLOR_CASE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_LABEL,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_BACKGROUND_COLOR
} from '@/views/chart/chart/chart'

export default {
  name: 'UserView',
  components: { ChartComponent, TableNormal },
  props: {
    element: {
      type: Object
    },
    filter: {
      type: Object,
      required: false,
      default: function() {
        return {
          filter: []
        }
      }
    }
  },
  watch: {
    filter(val) {
      this.getData(this.element.propValue.viewId)
    },
    // deep监听panel 如果改变 提交到 store
    canvasStyleData: {
      handler(newVal, oldVla) {
        // this.chart.stylePriority == panel 优先使用仪表盘样式
        this.mergeStyle()
      },
      deep: true
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),
  data() {
    return {
      chart: {
        stylePriority: 'panel',
        xaxis: '[]',
        yaxis: '[]',
        show: true,
        type: 'panel',
        title: '',
        customAttr: JSON.stringify({
          color: DEFAULT_COLOR_CASE,
          size: DEFAULT_SIZE,
          label: DEFAULT_LABEL,
          tooltip: DEFAULT_TOOLTIP
        }),
        customStyle: JSON.stringify({
          text: DEFAULT_TITLE_STYLE,
          legend: DEFAULT_LEGEND_STYLE,
          xAxis: DEFAULT_XAXIS_STYLE,
          yAxis: DEFAULT_YAXIS_STYLE,
          background: DEFAULT_BACKGROUND_COLOR
        }),
        customFilter: '[]'
      },
      requestStatus: 'waiting',
      message: null
    }
  },
  created() {
    this.getData(this.element.propValue.viewId)
  },
  mounted() {

  },
  methods: {
    mergeStyle() {
      // this.chart.stylePriority == panel 优先使用仪表盘样式
      if ((this.requestStatus === 'success' || this.requestStatus === 'merging') && this.chart.stylePriority === 'panel' && this.canvasStyleData.chart) {
        const customAttrChart = JSON.parse(this.chart.customAttr)
        const customStyleChart = JSON.parse(this.chart.customStyle)

        const customAttrPanel = JSON.parse(this.canvasStyleData.chart.customAttr)
        const customStylePanel = JSON.parse(this.canvasStyleData.chart.customStyle)

        // 组件样式-标题设置
        customStyleChart.text = customStylePanel.text
        // 组件样式-背景设置
        customStyleChart.background = customStylePanel.background
        // 图形属性-颜色设置
        customAttrChart.color = customAttrPanel.color

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
        viewData(id, this.filter).then(response => {
          // 将视图传入echart组件
          if (response.success) {
            this.chart = response.data
            this.requestStatus = 'merging'
            this.mergeStyle()
            this.requestStatus = 'success'
          } else {
            this.requestStatus = 'error'
            this.message = response.massage
          }
          return true
        }).catch(err => {
          this.requestStatus = 'error'
          this.message = err
          return true
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.rect-shape {
    width: 100%;
    height: 100%;
    overflow: auto;
}
.chart-class{
  height: 100%;
}
.table-class{
  height: 100%;
}
</style>
