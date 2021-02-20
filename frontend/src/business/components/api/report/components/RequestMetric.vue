<template>
  <div class="metric-container">
    <el-row type="flex">
      <div class="metric">
        <div class="value">{{request.responseResult.responseTime}} ms</div>
        <div class="name">{{$t('api_report.response_time')}}</div>
        <br>
        <div class="value">{{request.responseResult.latency}} ms</div>
        <div class="name">{{$t('api_report.latency')}}</div>
      </div>
      <div class="metric">
        <div class="value">{{request.requestSize}} bytes</div>
        <div class="name">{{$t('api_report.request_size')}}</div>
        <br>
        <div class="value">{{request.responseResult.responseSize}} bytes</div>
        <div class="name">{{$t('api_report.response_size')}}</div>
      </div>

      <div class="metric horizontal">
        <el-row type="flex">
          <div class="code">
            <div class="value" :class="{'error': error}">{{request.responseResult.responseCode}}</div>
            <div class="name">{{$t('api_report.response_code')}}</div>
          </div>
          <div class="split"></div>
          <div class="message">
            <div class="value">{{request.responseResult.responseMessage}}</div>
            <div class="name">{{$t('api_report.response_message')}}</div>
          </div>
        </el-row>
      </div>
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
