<template>
  <div class="request-result">
    <div @click="active">
      <el-row :gutter="10" type="flex" align="middle" class="info">
        <el-col :span="12">
          <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
          {{scenarioName}}
        </el-col>
        <el-col :span="4">
          {{$t('api_report.start_time')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.response_time')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.error')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.assertions')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.result')}}
        </el-col>
      </el-row>
      <el-row :gutter="10" type="flex" align="middle" class="info">
        <el-col :span="2">
          <div class="method">
            {{request.method}}
          </div>
        </el-col>
        <el-col :span="10">
          <div class="name">{{request.name}}</div>
          <el-tooltip effect="dark" :content="request.url" placement="bottom" :open-delay="800">
            <div class="url">{{request.url}}</div>
          </el-tooltip>
        </el-col>
        <el-col :span="4">
            {{request.startTime | timestampFormatDate(true) }}
        </el-col>
        <el-col :span="2">
          <div class="time">
            {{request.responseResult.responseTime}}
          </div>
        </el-col>
        <el-col :span="2">
          {{request.error}}
        </el-col>
        <el-col :span="2">
          {{assertion}}
        </el-col>
        <el-col :span="2">
          <el-tag size="mini" type="success" v-if="request.success">
            {{$t('api_report.success')}}
          </el-tag>
          <el-tag size="mini" type="danger" v-else>
            {{$t('api_report.fail')}}
          </el-tag>
        </el-col>
      </el-row>
    </div>
    <el-collapse-transition>
      <div v-show="isActive">
        <el-tabs v-model="activeName" v-show="isActive" v-if="hasSub">
          <el-tab-pane :label="$t('api_report.sub_result')" name="sub">
            <ms-request-result class="sub-result" v-for="(sub, index) in request.subRequestResults"
                               :key="index" :request="sub"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('api_report.request_result')" name="result">
            <ms-request-metric :request="request"/>
            <ms-request-text  :request="request"/>
            <br>
            <ms-response-text :request-type="requestType" :response="request.responseResult"/>
          </el-tab-pane>
        </el-tabs>
        <div v-else>
          <ms-request-metric :request="request"/>
          <ms-request-text v-if="isCodeEditAlive" :request="request"/>
          <br>
          <ms-response-text :request-type="requestType" v-if="isCodeEditAlive" :response="request.responseResult"/>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script>
  import MsRequestMetric from "./RequestMetric";
  import MsAssertionResults from "./AssertionResults";
  import MsRequestText from "./RequestText";
  import MsResponseText from "./ResponseText";
  import MsRequestResult from "./RequestResult";

  export default {
    name: "MsRequestResultTail",
    components: {MsResponseText, MsRequestText, MsAssertionResults, MsRequestMetric, MsRequestResult},
    props: {
      request: Object,
      scenarioName: String,
      requestType: String
    },

    data() {
      return {
        isActive: true,
        activeName: "sub",
        isCodeEditAlive: true
      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      },
      reload() {
        this.isCodeEditAlive = false;
        this.$nextTick(() => (this.isCodeEditAlive = true));
      }
    },

    watch: {
      'request.responseResult'() {
        this.reload();
      }
    },

    computed: {
      assertion() {
        return this.request.passAssertions + " / " + this.request.totalAssertions;
      },
      hasSub() {
        return this.request.subRequestResults.length > 0;
      },
    }
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
