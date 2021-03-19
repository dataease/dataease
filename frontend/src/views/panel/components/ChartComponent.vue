<template>
  <div class="Echarts" style="height: 100%;display: flex;margin-top: 10px;">
    <div id="echart" style="width: 100%;height: 80vh;" />
  </div>
</template>

<script>
import { BASE_BAR, BASE_LINE } from '../chart/chart'

export default {
  name: 'ChartComponent',
  props: {
    chart: {
      type: Object,
      required: true
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
    }
  },
  mounted() {
    // 基于准备好的dom，初始化echarts实例
    this.myChart = this.$echarts.init(document.getElementById('echart'))
  },
  methods: {
    drawEcharts() {
      debugger
      const chart = this.chart
      let chart_option = {}
      // todo type
      if (chart.type === 'bar') {
        chart_option = JSON.parse(JSON.stringify(BASE_BAR))
      } else if (chart.type === 'line') {
        chart_option = JSON.parse(JSON.stringify(BASE_LINE))
      }
      // console.log(chart_option);
      // 处理data
      if (chart.data) {
        chart_option.title.text = chart.title
        chart_option.xAxis.data = chart.data.x
        chart.data.series.forEach(function(y) {
          chart_option.legend.data.push(y.name)
          chart_option.series.push(y)
        })
      }
      // console.log(chart_option);
      // 处理shape attr
      if (chart.customAttr) {
        if (chart.customAttr.color) {
          chart_option.color = chart.customAttr.color.colors
        }
      }
      this.myEcharts(chart_option)
    },
    myEcharts(option) {
      // 指定图表的配置项和数据
      const chart = this.myChart
      setTimeout(chart.setOption(option, true), 500)
      window.onresize = function() {
        chart.resize()
      }
    }
  }
}
</script>

<style scoped>

</style>
