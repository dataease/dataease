<template>
  <div class="request-form">
    <component @runDebug="runDebug" :is="component" :is-read-only="isReadOnly" :request="request" :scenario="scenario"/>
    <el-divider v-if="isCompleted"></el-divider>
    <ms-request-result-tail v-loading="debugReportLoading" v-if="isCompleted" :requestType="request.type" :request="request.debugRequestResult ? request.debugRequestResult : {responseResult: {}, subRequestResults: []}"
                            :scenario-name="request.debugScenario ? request.debugScenario.name : ''" ref="msDebugResult"/>
  </div>
</template>

<script>
  import {JSR223Processor, Request, RequestFactory, Scenario} from "../../model/ScenarioModel";
  import MsApiHttpRequestForm from "./ApiHttpRequestForm";
  import MsApiTcpRequestForm from "./ApiTcpRequestForm";
  import MsApiDubboRequestForm from "./ApiDubboRequestForm";
  import MsScenarioResults from "../../../report/components/ScenarioResults";
  import MsRequestResultTail from "../../../report/components/RequestResultTail";
  import MsApiSqlRequestForm from "./ApiSqlRequestForm";

export default {
  name: "MsApiRequestForm",
  components: {MsApiSqlRequestForm, MsRequestResultTail, MsScenarioResults, MsApiDubboRequestForm, MsApiHttpRequestForm, MsApiTcpRequestForm},
  props: {
    scenario: Scenario,
    request: Request,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    debugReportId: String
  },
    data() {
      return {
        reportId: "",
        content: {scenarios:[]},
        debugReportLoading: false,
        showDebugReport: false,
        jsonPathList:[]
      }
    },
    computed: {
      component({request: {type}}) {
        let name;
        switch (type) {
          case RequestFactory.TYPES.DUBBO:
            name = "MsApiDubboRequestForm";
            break;
          case RequestFactory.TYPES.SQL:
            name = "MsApiSqlRequestForm";
            break;
        case RequestFactory.TYPES.TCP:
          name = "MsApiTcpRequestForm";
          break;
          default:
            name = "MsApiHttpRequestForm";
        }
        return name;
      },
      isCompleted() {
        return !!this.request.debugReport;
      }
    },
    watch: {
      debugReportId() {
        this.getReport();
      }
    },
    mounted() {
      //兼容旧版本 beanshell
      if (!this.request.jsr223PreProcessor.script && this.request.beanShellPreProcessor) {
        this.request.jsr223PreProcessor = new JSR223Processor(this.request.beanShellPreProcessor);
      }
      if (!this.request.jsr223PostProcessor.script && this.request.beanShellPostProcessor) {
        this.request.jsr223PostProcessor = new JSR223Processor(this.request.beanShellPostProcessor);
      }
    },
    methods: {
      getReport() {
        if (this.debugReportId) {
          this.debugReportLoading = true;
          this.showDebugReport = true;
          this.request.debugReport = {};
          let url = "/api/report/get/" + this.debugReportId;
          this.$get(url, response => {
            let report = response.data || {};
            let res = {};

            if (response.data) {
              try {
                res = JSON.parse(report.content);
              } catch (e) {
                throw e;
              }
              if (res) {
                this.debugReportLoading = false;
                this.request.debugReport = res;
                if (res.scenarios && res.scenarios.length > 0) {
                  this.request.debugScenario = res.scenarios[0];
                  this.request.debugRequestResult = this.request.debugScenario.requestResults[0];
                  this.deleteReport(this.debugReportId);
                } else {
                  this.request.debugScenario = new Scenario();
                  this.request.debugRequestResult = {responseResult: {}, subRequestResults: []};
                }
              } else {
                setTimeout(this.getReport, 2000)
              }
            } else {
              this.debugReportLoading = false;
            }
          });
        }
      },
      deleteReport(reportId) {
        this.$post('/api/report/delete', {id: reportId});
      },
      runDebug() {
        this.$emit('runDebug', this.request);
      }
    }
  }
</script>

<style scoped>

  .scenario-results {
    margin-top: 20px;
  }

  .request-form >>> .debug-button {
    margin-left: auto;
    display: block;
    margin-right: 10px;
  }

</style>
