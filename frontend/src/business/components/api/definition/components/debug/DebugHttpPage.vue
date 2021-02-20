<template>

  <div class="card-container" v-loading="loading">
    <el-card class="card-content">
      <el-form :model="debugForm" :rules="rules" ref="debugForm" :inline="true" label-position="right">
        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>

        <el-form-item :label="$t('api_report.request')" prop="url">
          <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-model="debugForm.url"
                    class="ms-http-input" size="small" :disabled="testCase!=undefined" @blur="urlChange">
            <el-select v-model="debugForm.method" slot="prepend" style="width: 100px" size="small">
              <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button v-if="scenario" size="small" type="primary" @click="handleCommand"> {{$t('commons.test')}}</el-button>
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand"
                       @command="handleCommand" size="small" v-if="testCase===undefined && !scenario">
            {{$t('commons.test')}}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="save_as">{{$t('api_test.definition.request.save_as_case')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-form-item>

        <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :isShowEnable="true" :headers="request.headers" :request="request" :response="responseData"/>
      </el-form>
      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-request-result-tail :response="responseData" ref="debugResult"/>

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
  import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
  import MsResponseResult from "../response/ResponseResult";
  import MsRequestMetric from "../response/RequestMetric";
  import {getUUID, getCurrentUser} from "@/common/js/utils";
  import MsResponseText from "../response/ResponseText";
  import MsRun from "../Run";
  import {createComponent} from "../jmeter/components";
  import {REQ_METHOD} from "../../model/JsonData";
  import MsRequestResultTail from "../response/RequestResultTail";
  import MsJmxStep from "../step/JmxStep";
  import {KeyValue} from "../../model/ApiTestModel";
  import MsApiCaseList from "../case/ApiCaseList";

  export default {
    name: "ApiConfig",
    components: {MsRequestResultTail, MsResponseResult, MsApiRequestForm, MsRequestMetric, MsResponseText, MsRun, MsJmxStep, MsApiCaseList},
    props: {
      currentProtocol: String,
      testCase: {},
      scenario: Boolean,
    },
    data() {
      let validateURL = (rule, value, callback) => {
        try {
          new URL(this.debugForm.url);
          callback();
        } catch (e) {
          callback(this.$t('api_test.request.url_invalid'));
        }
      };
      return {
        rules: {
          method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          url: [
            {max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur'},
            /*
                        {validator: validateURL, trigger: 'blur'}
            */
          ],
        },
        debugForm: {method: REQ_METHOD[0].id, environmentId: ""},
        options: [],
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        loading: false,
        debugResultId: "",
        runData: [],
        reportId: "",
        reqOptions: REQ_METHOD,
        createCase: "",
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
        this.createHttp();
      }
    },
    watch: {
      debugResultId() {
        this.getResult()
      },
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
      createHttp() {
        this.request = createComponent("HTTPSamplerProxy");
      },
      runDebug() {
        this.$refs['debugForm'].validate((valid) => {
          if (valid) {
            this.loading = true;
            this.request.url = this.debugForm.url;
            this.request.method = this.debugForm.method;
            this.request.name = getUUID().substring(0, 8);
            this.runData = [];
            this.runData.push(this.request);
            /*触发执行操作*/
            this.reportId = getUUID().substring(0, 8);
          }
        })
      },
      runRefresh(data) {
        this.responseData = data;
        this.loading = false;
        this.$refs.debugResult.reload();
      },
      saveAsApi() {
        this.$refs['debugForm'].validate((valid) => {
          if (valid) {
            this.debugForm.id = null;
            this.request.id = getUUID();
            this.debugForm.request = this.request;
            this.debugForm.userId = getCurrentUser().id;
            this.debugForm.status = "Underway";
            this.debugForm.protocol = this.currentProtocol;
            this.$emit('saveAs', this.debugForm);
          }
          else {
            return false;
          }
        })
      },
      saveAs() {
        this.$refs['debugForm'].validate((valid) => {
          if (valid) {
            this.request.id = getUUID();
            this.request.method = this.debugForm.method;
            this.request.path = this.debugForm.path;
            this.protocol = this.currentProtocol;
            this.debugForm.id = this.request.id;
            this.debugForm.request = this.request;
            this.debugForm.userId = getCurrentUser().id;
            this.debugForm.status = "Underway";
            this.debugForm.protocol = this.currentProtocol;
            this.debugForm.saved = true;
            this.$refs.caseList.saveApiAndCase(this.debugForm);
          }
          else {
            return false;
          }
        })
      },
      urlChange() {
        if (!this.debugForm.url) return;
        let url = this.getURL(this.addProtocol(this.debugForm.url));
        if (url && url.pathname) {
          if (this.debugForm.url.indexOf('?') != -1) {
            this.debugForm.url = decodeURIComponent(this.debugForm.url.substr(0, this.debugForm.url.indexOf("?")));
          }
          this.debugForm.path = url.pathname;
        } else {
          this.debugForm.path = url;
        }

      },
      addProtocol(url) {
        if (url) {
          if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
            return "https://" + url;
          }
        }
        return url;
      },
      getURL(urlStr) {
        try {
          let url = new URL(urlStr);
          url.searchParams.forEach((value, key) => {
            if (key && value) {
              this.request.arguments.splice(0, 0, new KeyValue({name: key, required: false, value: value}));
            }
          });
          return url;
        } catch (e) {
          return urlStr;
        }
      },
    }
  }
</script>

<style scoped>
  .ms-http-input {
    width: 500px;
    margin-top: 5px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }
</style>
