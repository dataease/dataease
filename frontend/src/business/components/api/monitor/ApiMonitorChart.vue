<template>
  <div class="monitor-view">
    <div class="response-time-view">
      <el-card shadow="hover">
        <api-response-time-monitor-chart :data="rspTimeData" :xAxis="rspTimexAxis"></api-response-time-monitor-chart>
      </el-card>
    </div>
    <div class="error-monitor-view">
      <el-card shadow="hover">
        <api-error-monitor-chart :api-url="apiUrl" :data="rspCodeData" :xAxis="rspCodexAxis"></api-error-monitor-chart>
      </el-card>
    </div>
  </div>
</template>

<script>

import ApiResponseTimeMonitorChart from '@/business/components/api/monitor/components/ApiResponseTimeMonitorChart';
import ApiErrorMonitorChart from '@/business/components/api/monitor/components/ApiErrorMonitorChart';

export default {
  name: 'MsApiMonitorChart',
  props: [
    'rspTimeData',
    'rspTimexAxis',
    'rspCodeData',
    'rspCodexAxis',
    'apiUrl'
  ],
  data() {
    return {};
  },
  components: {
    ApiErrorMonitorChart,
    ApiResponseTimeMonitorChart,
  },
  methods: {}
};
</script>

<style scoped>
.error-monitor-view {
  margin-top: 20px;
}

</style>
