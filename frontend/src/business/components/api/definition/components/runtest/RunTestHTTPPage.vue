<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="loading">

      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">

        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
        <!-- 请求方法 -->
        <el-form-item :label="$t('api_report.request')" prop="method">
          <el-select v-model="api.request.method" style="width: 100px" size="small">
            <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          <environment-select :current-data="api" :project-id="projectId"/>
        </el-form-item>

        <!-- 请求地址 -->
        <el-form-item prop="path">
          <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="api.request.path" class="ms-htt-width"
                    size="small" :disabled="false"/>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                       @command="handleCommand" size="small">
            {{$t('commons.test')}}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="load_case">{{$t('api_test.definition.request.load_case')}}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_case">{{$t('api_test.definition.request.save_as_case')}}
              </el-dropdown-item>
              <el-dropdown-item command="update_api">{{$t('api_test.definition.request.update_api')}}</el-dropdown-item>
              <el-dropdown-item command="save_as_api">{{$t('api_test.definition.request.save_as')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

        </el-form-item>

        <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :isShowEnable="true" :headers="api.request.headers" :request="api.request"/>

      </el-form>
      <!--返回结果-->
      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-request-result-tail :response="responseData" ref="runResult"/>

      <ms-jmx-step :request="api.request" :response="responseData"/>

    </el-card>

    <!-- 加载用例 -->
    <ms-api-case-list @selectTestCase="selectTestCase" @refresh="refresh"
                      :loaded="loaded"
                      :refreshSign="refreshSign"
                      :createCase="createCase"
                      :currentApi="api"
                      ref="caseList"/>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="api.environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" ref="runTest"/>

  </div>
</template>

<script>
  import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
  import {downloadFile, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsApiCaseList from "../case/ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import MsRequestResultTail from "../response/RequestResultTail";
  import MsRun from "../Run";
  import {REQ_METHOD} from "../../model/JsonData";
  import EnvironmentSelect from "../environment/EnvironmentSelect";
  import MsJmxStep from "../step/JmxStep";

  export default {
    name: "RunTestHTTPPage",
    components: {
      EnvironmentSelect,
      MsApiRequestForm,
      MsApiCaseList,
      MsContainer,
      MsRequestResultTail,
      MsRun,
      MsJmxStep
    },
    data() {
      return {
        visible: false,
        api: {},
        loaded: false,
        loading: false,
        createCase: "",
        currentRequest: {},
        refreshSign: "",
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        reqOptions: REQ_METHOD,
        rules: {
          method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}],
          environmentId: [{required: true, message: this.$t('api_test.definition.request.run_env'), trigger: 'change'}],
        },
        runData: [],
        reportId: "",
      }
    },
    props: {apiData: {}, currentProtocol: String, syncTabs: Array, projectId: String},
    methods: {
      handleCommand(e) {
        switch (e) {
          case "load_case":
            return this.loadCase();
          case "save_as_case":
            return this.saveAsCase();
          case "update_api":
            return this.updateApi();
          case "save_as_api":
            return this.saveAsApi();
          default:
            return this.runTest();
        }
      },
      runTest() {
        this.$refs['apiData'].validate((valid) => {
          if (valid) {
            this.loading = true;
            this.api.request.name = this.api.id;
            this.api.request.useEnvironment = this.api.environmentId;
            this.api.protocol = this.currentProtocol;
            this.runData = [];
            this.runData.push(this.api.request);
            /*触发执行操作*/
            this.reportId = getUUID().substring(0, 8);
          }
        })
      },
      runRefresh(data) {
        this.responseData = data;
        this.loading = false;
      },
      saveAs() {
        this.$emit('saveAs', this.api);
      },
      loadCase() {
        this.refreshSign = getUUID();
        this.loaded = true;
        this.$refs.caseList.open();
      },
      apiCaseClose() {
        this.visible = false;
      },
      getBodyUploadFiles() {
        let bodyUploadFiles = [];
        this.api.bodyUploadIds = [];
        let request = this.api.request;
        if (request.body) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  this.api.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        return bodyUploadFiles;
      },
      saveAsCase() {
        //用于触发创建操作
        this.createCase = getUUID();
        this.$refs.caseList.open();
        this.loaded = false;
      },
      saveAsApi() {
        let data = {};
        let req = this.api.request;
        req.id = getUUID();
        data.request = JSON.stringify(req);
        data.method = this.api.method;
        data.url = this.api.url;
        data.status = this.api.status;
        data.userId = this.api.userId;
        data.description = this.api.description;
        this.$emit('saveAsApi', data);
      },
      updateApi() {
        let url = "/api/definition/update";
        let bodyFiles = this.getBodyUploadFiles();
        this.api.method = this.api.request.method;
        this.api.path = this.api.request.path;
        this.$fileUpload(url, null, bodyFiles, this.api, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('saveApi', this.api);
          if (this.syncTabs.indexOf(this.api.id) === -1) {
            this.syncTabs.push(this.api.id);
          }
        });
      },
      selectTestCase(item) {
        if (item != null) {
          this.api.request = item.request;
        } else {
          this.api.request = this.currentRequest;
        }
      },

      refresh() {
        this.$emit('refresh');
      },
      getResult() {
        let url = "/api/definition/report/getReport/" + this.api.id;
        this.$get(url, response => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.responseData = data;
          }
        });
      }
    },
    created() {
      // 深度复制
      this.api = JSON.parse(JSON.stringify(this.apiData));
      this.api.protocol = this.currentProtocol;
      this.currentRequest = this.api.request;
      this.getResult();
    }
  }
</script>

<style scoped>
  .ms-htt-width {
    width: 350px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
  }

  /deep/ .el-drawer {
    overflow: auto;
  }
</style>
