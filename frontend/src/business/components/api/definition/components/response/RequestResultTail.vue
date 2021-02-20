<template>
  <div class="request-result">
    <ms-request-metric v-if="showMetric" :response="response"/>
    <ms-response-result :currentProtocol="currentProtocol" :response="response"/>
  </div>
</template>

<script>
  import MsResponseResult from "../response/ResponseResult";
  import MsRequestMetric from "../response/RequestMetric";

  export default {
    name: "MsRequestResultTail",
    components: {MsRequestMetric, MsResponseResult},
    props: {
      response: Object,
      currentProtocol: String,
      showMetric: {
        type: Boolean,
        default() {
          return true;
        }
      }
    },

    data() {
      return {
        isCodeEditAlive: true,
      }
    },

    methods: {
      reload() {
        this.isCodeEditAlive = false;
        this.$nextTick(() => (this.isCodeEditAlive = true));
      }
    },
  }
</script>

<style scoped>
  .request-result {
    width: 100%;
    min-height: 40px;
    padding: 2px 0;
  }

  .request-result .info {
    background-color: #F9F9F9;
    margin-left: 20px;
    cursor: pointer;
  }

  .request-result .method {
    color: #1E90FF;
    font-size: 14px;
    font-weight: 500;
    line-height: 40px;
    padding-left: 5px;
  }

  .request-result .url {
    color: #7f7f7f;
    font-size: 12px;
    font-weight: 400;
    margin-top: 4px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    word-break: break-all;
  }

  .request-result .tab .el-tabs__header {
    margin: 0;
  }

  .request-result .text {
    height: 300px;
    overflow-y: auto;
  }

  .sub-result .info {
    background-color: #FFF;
  }

  .sub-result .method {
    border-left: 5px solid #1E90FF;
    padding-left: 20px;
  }

  .sub-result:last-child {
    border-bottom: 1px solid #EBEEF5;
  }

  .request-result .icon.is-active {
    transform: rotate(90deg);
  }

</style>
