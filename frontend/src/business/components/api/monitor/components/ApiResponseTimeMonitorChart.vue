<template>
  <common-monitor-chart>
    <template>
      <div id="response-time-chart" :style="{ width:'100%',height:'100%' }">
        <chart :options="getOptions()" :style="{ width:'100%' }"></chart>
      </div>
    </template>
  </common-monitor-chart>
</template>

<script>
import CommonMonitorChart from '@/business/components/api/monitor/components/CommonMonitorChart';

export default {

  name: 'ApiResponseTimeMonitorChart',
  components: {CommonMonitorChart},
  props: [
    'data',
    'xAxis'
  ],
  methods: {
    getOptions() {
      return {
        title: {text: '响应时间趋势'},
        tooltip: {},
        toolbox: {
          show: true,
          feature: {
            dataZoom: {
              yAxisIndex: 'none'
            },
            dataView: {
              readOnly: false
            },
            restore: {},
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'category',
          data: this.xAxis
        },
        yAxis: {

        },
        dataZoom: [{
          start: 0
        }],
        series: [{
          type: 'line',
          smooth: true,
          data: this.data
        }],
      };
    }
  },
};

</script>

<style scoped>

</style>
