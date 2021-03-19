<template>
  <div class="Echarts" style="height: 100%;display: flex;margin-top: 10px;">
    <div :id="chartId" style="width: 100%;height: 100%;" />
  </div>
</template>

<script>
import { BASE_BAR, BASE_LINE, HORIZONTAL_BAR, BASE_PIE, BASE_FUNNEL } from '../chart/chart'
import { baseBarOption, stackBarOption, horizontalBarOption, horizontalStackBarOption } from '../chart/bar/bar'
import { baseLineOption } from '../chart/line/line'
import { basePieOption } from '../chart/pie/pie'
import { baseFunnelOption } from '../chart/funnel/funnel'

export default {
  name: 'ChartComponent',
  props: {
    chart: {
      type: Object,
      required: true
    },
    chartId: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      myChart: {}
    }
  },
  watch: {
    chart() {
      debugger
      this.drawEcharts()
    },
    resize() {
      this.drawEcharts()
    }
  },
  mounted() {
    // 基于准备好的dom，初始化echarts实例
    this.myChart = this.$echarts.init(document.getElementById(this.chartId))
    this.drawEcharts()
  },
  methods: {
    drawEcharts() {
      debugger
      const chart = this.chart
      let chart_option = {}
      // todo type
      if (chart.type === 'bar') {
        chart_option = baseBarOption(JSON.parse(JSON.stringify(BASE_BAR)), chart)
      } else if (chart.type === 'bar-stack') {
        chart_option = stackBarOption(JSON.parse(JSON.stringify(BASE_BAR)), chart)
      } else if (chart.type === 'bar-horizontal') {
        chart_option = horizontalBarOption(JSON.parse(JSON.stringify(HORIZONTAL_BAR)), chart)
      } else if (chart.type === 'bar-horizontal-stack') {
        chart_option = horizontalStackBarOption(JSON.parse(JSON.stringify(HORIZONTAL_BAR)), chart)
      } else if (chart.type === 'line') {
        chart_option = baseLineOption(JSON.parse(JSON.stringify(BASE_LINE)), chart)
      } else if (chart.type === 'pie') {
        chart_option = basePieOption(JSON.parse(JSON.stringify(BASE_PIE)), chart)
      } else if (chart.type === 'funnel') {
        chart_option = baseFunnelOption(JSON.parse(JSON.stringify(BASE_FUNNEL)), chart)
      }
      console.log(chart_option)
      this.myEcharts(chart_option)
    },
    myEcharts(option) {
      // 指定图表的配置项和数据
      const chart = this.myChart
      setTimeout(chart.setOption(option, true), 500)
      window.onresize = function() {
        chart.resize()
      }
    },
    chartResize() {
      // 指定图表的配置项和数据
      const chart = this.myChart
      chart.resize()
    }
  }
}
</script>

<style scoped>

</style>
