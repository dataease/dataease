<template>
  <div class="request-result">
    <div @click="active">
      <el-row :gutter="10" type="flex" align="middle" class="info">
        <el-col :span="4">
          <div class="method">
            {{ request.method }}
          </div>
        </el-col>
        <el-col :span="15">
          <div class="name">{{ request.name }}</div>
          <el-tooltip effect="dark" :content="request.url" placement="bottom" :open-delay="800">
            <div class="url">{{ request.url }}</div>
          </el-tooltip>
        </el-col>
        <el-col :span="5">
          <div class="success">
            <el-tag size="mini" type="success" v-if="request.success">
              {{ $t('api_report.success') }}
            </el-tag>
            <el-tag size="mini" type="danger" v-else>
              {{ $t('api_report.fail') }}
            </el-tag>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
  import MsRequestMetric from "./RequestMetric";
  import MsAssertionResults from "./AssertionResults";
  import MsRequestText from "./RequestText";
  import MsResponseText from "./ResponseText";

  export default {
    name: "MsRequestResult",
    components: {MsResponseText, MsRequestText, MsAssertionResults, MsRequestMetric},
    props: {
      request: Object,
      scenarioName: String
    },

    data() {
      return {}
    },

    methods: {
      active() {
        this.$emit("requestResult", {request: this.request, scenarioName: this.scenarioName});
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

</style>
