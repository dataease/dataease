<template>

  <div class="card-container" v-loading="loading">
    <el-card class="card-content">
      <el-button v-if="scenario"  style="float: right;margin-right: 20px" size="small" type="primary" @click="handleCommand"> {{$t('commons.test')}}</el-button>
      <el-dropdown v-else split-button type="primary" class="ms-api-buttion" @click="handleCommand"
                   @command="handleCommand" size="small" style="float: right;margin-right: 20px">
        {{$t('commons.test')}}
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="save_as">{{$t('api_test.definition.request.save_as_case')}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>

      <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
      <!-- 请求参数 -->
      <ms-basis-parameters :request="request" ref="requestForm"/>


      <!-- 请求返回数据 -->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-request-result-tail :response="responseData" :currentProtocol="currentProtocol" ref="debugResult"/>

      <ms-jmx-step :request="request" :response="responseData"/>
      <!-- 执行组件 -->
      <ms-run :debug="true" :reportId="reportId" :run-data="runData" @runRefresh="runRefresh" ref="runTest"/>
    </el-card>
    <div v-if="scenario">
      <el-button style="float: right;margin: 20px" type="primary" @click="handleCommand('save_as_api')"> {{$t('commons.save')}}</el-button>
    </div>
    <!-- 加载用例 -->
    <ms-api-case-list :loaded="false" ref="caseList"/>

  </div>
</template>

<script>
  import MsResponseResult from "../response/ResponseResult";
  import MsRequestMetric from "../response/RequestMetric";
  import {getUUID, getCurrentUser} from "@/common/js/utils";
  import MsResponseText from "../response/ResponseText";
  import MsRun from "../Run";
  import {createComponent} from "../jmeter/components";
  import {REQ_METHOD} from "../../model/JsonData";
  import MsRequestResultTail from "../response/RequestResultTail";
  import MsBasisParameters from "../request/dubbo/BasisParameters";
  import MsJmxStep from "../step/JmxStep";
  import MsApiCaseList from "../case/ApiCaseList";

  export default {
    name: "ApiConfig",
    components: {MsRequestResultTail, MsResponseResult, MsRequestMetric, MsResponseText, MsRun, MsBasisParameters, MsJmxStep, MsApiCaseList},
    props: {
      currentProtocol: String,
      scenario: Boolean,
      testCase: {},
    },
    data() {
      return {
        rules: {
          method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          url: [{required: true, message: this.$t('api_test.definition.request.path_all_info'), trigger: 'blur'}],
        },
        debugForm: {method: REQ_METHOD[0].id},
        options: [],
        responseData: {type: 'TCP', responseResult: {}, subRequestResults: []},
        loading: false,
        debugResultId: "",
        runData: [],
        headers: [],
        reportId: "",
        reqOptions: REQ_METHOD,
        request: {},
      }
    },
    created() {
      if (this.testCase) {
        // 执行结果信息
        let url = "/api/definition/report/getReport/" + this.testCase.id;
        this.$get(url, response => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.responseData = data;
          }
        });
        this.request = this.testCase.request;
        if (this.request) {
          this.debugForm.method = this.request.method;
          if (this.request.url) {
            this.debugForm.url = this.request.url;
          } else {
            this.debugForm.url = this.request.path;
          }
        }
      } else {
        this.request = createComponent("DubboSampler");
      }
    },
    watch: {
      debugResultId() {
        this.getResult()
      }
    },
    methods: {
      handleCommand(e) {
        if (e === "save_as") {
          this.saveAs();
        } else if (e === 'save_as_api') {
          this.saveAsApi();
        }
        else {
          this.runDebug();
        }
      },

      runDebug() {
        this.loading = true;
        this.request.name = getUUID().substring(0, 8);
        this.runData = [];
        this.runData.push(this.request);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      },
      runRefresh(data) {
        this.responseData = data;
        this.loading = false;
        this.$refs.debugResult.reload();
      },
      saveAsApi() {
        let obj = {request: this.request};
        obj.request.id = getUUID();
        this.$emit('saveAs', obj);
      },
      saveAs() {
        let obj = {request: this.request};
        obj.request.id = getUUID();
        obj.saved = true;
        obj.protocol = this.currentProtocol;
        obj.status = "Underway";
        obj.method = this.currentProtocol;
        this.$refs.caseList.saveApiAndCase(obj);
      }
    }
  }
</script>

<style scoped>
  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 0px;
  }
</style>
