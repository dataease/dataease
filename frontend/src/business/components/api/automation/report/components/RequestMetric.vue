<template>
  <div class="metric-container">
    <el-row type="flex">
      <el-col>
        <div style="font-size: 14px;color: #AAAAAA;float: left">{{$t('api_report.response_code')}} :</div>
        <div style="font-size: 14px;color:#61C550;margin-top:2px;margin-left:10px;float: left">{{request.responseResult.responseCode ? request.responseResult.responseCode :'0'}}</div>
      </el-col>
      <el-col>
        <div style="font-size: 14px;color: #AAAAAA;float: left">{{$t('api_report.response_time')}} :</div>
        <div style="font-size: 14px;color:#61C550;margin-top:2px;margin-left:10px;float: left">{{request.responseResult.responseTime?request.responseResult.responseTime:0}} ms</div>
      </el-col>
      <el-col>
        <div style="font-size: 14px;color: #AAAAAA;float: left">{{$t('api_report.response_size')}} :</div>
        <div style="font-size: 14px;color:#61C550; margin-top:2px;margin-left:10px;float: left">{{request.responseResult.responseSize?request.responseResult.responseSize:0}} bytes</div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "MsRequestMetric",

    props: {
      request: Object
    },

    computed: {
      error() {
        return this.request.responseResult.responseCode >= 400;
      }
    }
  }
</script>

<style scoped>
  .metric-container {
    padding: 20px;
  }

  .metric {
    padding: 20px;
    border: 1px solid #EBEEF5;
    min-width: 120px;
    height: 114px;
  }

  .metric + .metric {
    margin-left: 20px;
  }

  .metric .value {
    font-size: 16px;
    font-weight: 500;
    word-break: break-all;
  }

  .metric .name {
    color: #404040;
    opacity: 0.5;
    padding: 5px 0;
  }

  .metric.horizontal {
    width: 100%;
  }

  .metric .code {
    min-width: 120px;
  }

  .metric .code .value {
    color: #67C23A;
  }

  .metric .code .value.error {
    color: #F56C6C;
  }

  .metric .split {
    height: 114px;
    border-left: 1px solid #EBEEF5;
    margin-right: 20px;
  }

  .metric .message {
    max-height: 114px;
    overflow-y: auto;
  }
</style>
