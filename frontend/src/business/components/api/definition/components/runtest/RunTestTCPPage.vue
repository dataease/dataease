<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="loading">

      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">

        <!-- 操作按钮 -->
        <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                     @command="handleCommand" size="small" style="float: right;margin-right: 20px">
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

        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          {{$t('api_test.definition.request.run_env')}}：
          <environment-select :type="'TCP'" :current-data="api" :project-id="projectId"/>
        </el-form-item>

        <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
        <!-- TCP 请求参数 -->
        <ms-basis-parameters :request="api.request" @callback="runTest" ref="requestForm"/>

      </el-form>

      <!--返回结果-->
      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-request-result-tail :response="responseData" ref="runResult"/>

      <ms-jmx-step :request="api.request" :response="responseData"/>

    </el-card>

    <!-- 加载用例 -->
    <ms-api-case-list @apiCaseClose="apiCaseClose" @refresh="refresh" @selectTestCase="selectTestCase" :currentApi="api" :refreshSign="refreshSign"
                      :loaded="loaded" :createCase="createCase"
                      ref="caseList"/>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="api.environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" ref="runTest"/>

  </div>
</template>

<script>
import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import {getUUID, uuid} from "@/common/js/utils";
import MsApiCaseList from "../case/ApiCaseList";
import MsContainer from "../../../../common/components/MsContainer";
import MsBottomContainer from "../BottomContainer";
import MsRequestResultTail from "../response/RequestResultTail";
import MsRun from "../Run";
import MsBasisParameters from "../request/tcp/TcpBasisParameters";
import {REQ_METHOD} from "../../model/JsonData";
import EnvironmentSelect from "../environment/EnvironmentSelect";
import MsJmxStep from "../step/JmxStep";

export default {
  name: "RunTestTCPPage",
  components: {
    MsJmxStep,
    EnvironmentSelect,
    MsApiRequestForm,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    MsRequestResultTail,
    MsRun,
      MsBasisParameters
    },
    data() {
      return {
        visible: false,
        api: {},
        loaded: false,
        loading: false,
        currentRequest: {},
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        reqOptions: REQ_METHOD,
        environments: [],
        refreshSign: "",
        createCase: "",
        rules: {
          environmentId: [{required: true, message: this.$t('api_test.definition.request.run_env'), trigger: 'change'}],
        },
        runData: [],
        reportId: "",
      }
    },
    props: {apiData: {}, currentProtocol: String,syncTabs: Array, projectId: String},
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
            return this.$refs['requestForm'].validate();
        }
      },
      refresh(){
        this.$emit('refresh');
      },
      runTest() {
        this.$refs['apiData'].validate((valid) => {
          if (valid) {
            this.loading = true;
            this.api.request.name = this.api.id;
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
        this.$refs.caseList.open();
        this.visible = true;
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
        this.createCase = getUUID();
        this.$refs.caseList.open();
        this.loaded = false;
      },
      saveAsApi() {
        let data = {};
        this.api.request.id = uuid();
        data.request = JSON.stringify(this.api.request);
        data.method = this.api.method;
        data.status = this.api.status;
        data.userId = this.api.userId;
        data.description = this.api.description;
        this.$emit('saveAsApi', data);
        this.$emit('refresh');
      },
      updateApi() {
        let url = "/api/definition/update";
        let bodyFiles = this.getBodyUploadFiles();
        this.$fileUpload(url, null, bodyFiles, this.api, () => {
          this.$success(this.$t('commons.save_success'));
          if (this.syncTabs.indexOf(this.api.id) === -1) {
            this.syncTabs.push(this.api.id);
          }
          this.$emit('saveApi', this.api);
        });
      },
      selectTestCase(item) {
        if (item != null) {
          this.api.request = item.request;
        } else {
          this.api.request = this.currentRequest;
        }
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

  .environment-button {
    margin-left: 20px;
    padding: 7px;
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
