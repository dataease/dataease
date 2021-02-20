<template>
  <div class="metric-container">
    <el-row type="flex">
      <el-col>
        <div style="font-size: 14px;color: #AAAAAA;float: left">{{$t('api_report.response_code')}} :</div>
        <div style="font-size: 14px;color:#61C550;margin-top:2px;margin-left:10px;float: left">{{response.responseResult.responseCode ? response.responseResult.responseCode :'0'}}</div>
      </el-col>
      <el-col>
        <div style="font-size: 14px;color: #AAAAAA;float: left">{{$t('api_report.response_time')}} :</div>
        <div style="font-size: 14px;color:#61C550;margin-top:2px;margin-left:10px;float: left">{{response.responseResult.responseTime?response.responseResult.responseTime:0}} ms</div>
      </el-col>
      <el-col>
        <div style="font-size: 14px;color: #AAAAAA;float: left">{{$t('api_report.response_size')}} :</div>
        <div style="font-size: 14px;color:#61C550; margin-top:2px;margin-left:10px;float: left">{{response.responseResult.responseSize?response.responseResult.responseSize:0}} bytes</div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "MsRequestMetric",

    props: {
      response: Object
    },

    computed: {
      error() {
        return this.response && this.response.responseCode && this.response.responseCode >= 400;
      }
    }
  }
</script>

<style scoped>
  .metric-container {
    padding-bottom: 20px;
  }
</style>
