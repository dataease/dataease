<template>
  <div class="Echarts" style="height: 100%;display: flex;margin-top: 10px;">
    <div id="echart" style="width: 100%;height: 80vh;" />
  </div>
</template>

<script>
import { BASE_BAR, BASE_LINE } from '../chart/chart'
import { baseBarOption } from '../chart/bar/baseBar'
import { baseLineOption } from '../chart/line/baseLine'

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
      this.drawEcharts()
    }
  },
  mounted() {
    // 基于准备好的dom，初始化echarts实例
    this.myChart = this.$echarts.init(document.getElementById('echart'))
  },
  methods: {
    drawEcharts() {
      const chart = this.chart
      let chart_option = {}
      // todo type
      if (chart.type === 'bar') {
        chart_option = baseBarOption(JSON.parse(JSON.stringify(BASE_BAR)), chart)
      } else if (chart.type === 'line') {
        chart_option = baseLineOption(JSON.parse(JSON.stringify(BASE_LINE)), chart)
      }
      // console.log(chart_option);
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
