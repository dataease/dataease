<template>

  <div class="card-container">
    <!-- HTTP 请求参数 -->
    <ms-edit-complete-http-api @runTest="runTest" @saveApi="saveApi" @createRootModelInTree="createRootModelInTree" :request="request" :response="response"
                               :basisData="currentApi" :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'HTTP'" ref="httpApi"/>
    <!-- TCP -->
    <ms-edit-complete-tcp-api :request="request" @runTest="runTest" @createRootModelInTree="createRootModelInTree" @saveApi="saveApi" :basisData="currentApi"
                              :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'TCP'" ref="tcpApi"/>
    <!--DUBBO-->
    <ms-edit-complete-dubbo-api :request="request" @runTest="runTest" @createRootModelInTree="createRootModelInTree" @saveApi="saveApi" :basisData="currentApi"
                                :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'DUBBO'" ref="dubboApi"/>
    <!--SQL-->
    <ms-edit-complete-sql-api :request="request" @runTest="runTest" @createRootModelInTree="createRootModelInTree" @saveApi="saveApi" :basisData="currentApi"
                              :moduleOptions="moduleOptions" :syncTabs="syncTabs" v-if="currentProtocol === 'SQL'" ref="sqlApi"/>
  </div>
</template>

<script>
import MsEditCompleteHttpApi from "./complete/EditCompleteHTTPApi";
import MsEditCompleteTcpApi from "./complete/EditCompleteTCPApi";
import MsEditCompleteDubboApi from "./complete/EditCompleteDubboApi";
import MsEditCompleteSqlApi from "./complete/EditCompleteSQLApi";

import {Body} from "../model/ApiTestModel";
import {getUUID} from "@/common/js/utils";
import {createComponent, Request} from "./jmeter/components";
import Sampler from "./jmeter/components/sampler/sampler";
import {WORKSPACE_ID} from '@/common/js/constants';
import {handleCtrlSEvent} from "../../../../../common/js/utils";

export default {
  name: "ApiConfig",
  components: {MsEditCompleteHttpApi, MsEditCompleteTcpApi, MsEditCompleteDubboApi, MsEditCompleteSqlApi},
  data() {
    return {
      reqUrl: "",
      request: Sampler,
      config: {},
      response: {},
      maintainerOptions: [],
    }
    },
    props: {
      currentApi: {},
      moduleOptions: {},
      currentProtocol: String,
      syncTabs: Array,
      projectId: String
    },
    created() {
      this.getMaintainerOptions();
      switch (this.currentProtocol) {
        case Request.TYPES.SQL:
          this.initSql();
          break;
        case Request.TYPES.DUBBO:
          this.initDubbo();
          break;
        case Request.TYPES.TCP:
          this.initTcp();
          break;
        default:
          this.initHttp();
          break;
      }
      this.formatApi();
      this.addListener();
    },
    methods: {
      addListener() {
        document.addEventListener("keydown", this.createCtrlSHandle);
        // document.addEventListener("keydown", (even => handleCtrlSEvent(even, this.$refs.httpApi.saveApi)));
      },
      removeListener() {
        document.removeEventListener("keydown", this.createCtrlSHandle);
      },
      createCtrlSHandle(event) {
        if (this.$refs.httpApi) {
          handleCtrlSEvent(event, this.$refs.httpApi.saveApi);
        }
        else if (this.$refs.tcpApi) {
          handleCtrlSEvent(event, this.$refs.tcpApi.saveApi);
        }
        else if (this.$refs.dubboApi) {
          handleCtrlSEvent(event, this.$refs.dubboApi.saveApi);
        }
        else if (this.$refs.sqlApi) {
          handleCtrlSEvent(event, this.$refs.sqlApi.saveApi);
        }
      },
      runTest(data) {
        this.setParameters(data);
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('runTest', data);
        })
      },
      createRootModelInTree() {
        this.$emit("createRootModel");
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      },
      setRequest() {
        if (this.currentApi.request != undefined && this.currentApi.request != null) {
          if (Object.prototype.toString.call(this.currentApi.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
            this.request = this.currentApi.request;
          } else {
            this.request = JSON.parse(this.currentApi.request);
          }
          if (!this.request.headers) {
            this.request.headers = [];
          }
          this.currentApi.request = this.request;
          return true;
        }
        return false;
      },
      initSql() {
        if (!this.setRequest()) {
          this.request = createComponent("JDBCSampler");
          this.currentApi.request = this.request;
        }
        if (!this.currentApi.request.variables) {
          this.currentApi.request.variables = [];
        }
      },
      initDubbo() {
        if (!this.setRequest()) {
          this.request = createComponent("DubboSampler");
          this.currentApi.request = this.request;
        }
      },
      initTcp() {
        if (!this.setRequest()) {
          this.request = createComponent("TCPSampler");
          this.currentApi.request = this.request;
        }
      },
      initHttp() {
        if (!this.setRequest()) {
          this.request = createComponent("HTTPSamplerProxy");
          this.currentApi.request = this.request;
        }
      },
      formatApi() {
        if (this.currentApi.response != null && this.currentApi.response != 'null' && this.currentApi.response != undefined) {
          if (Object.prototype.toString.call(this.currentApi.response).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
            this.response = this.currentApi.response;
          } else {
            this.response = JSON.parse(this.currentApi.response);
          }
        } else {
          this.response = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
        }
        if (this.currentApi != null && this.currentApi.id != null) {
          this.reqUrl = "/api/definition/update";
        } else {
          this.reqUrl = "/api/definition/create";
        }
        if (!this.request.hashTree) {
          this.request.hashTree = [];
        }
        if (this.request.body && !this.request.body.binary) {
          this.request.body.binary = [];
        }
        // 处理导入数据缺失问题
        if (this.response.body) {
          let body = new Body();
          Object.assign(body, this.response.body);
          if (!body.binary) {
            body.binary = [];
          }
          if (!body.kvs) {
            body.kvs = [];
          }
          if (!body.binary) {
            body.binary = [];
          }
          this.response.body = body;
        }
        if (this.currentApi.moduleId && this.currentApi.moduleId === "root") {
          this.currentApi.moduleId = "";
        }
      },
      saveApi(data) {
        this.setParameters(data);
        let bodyFiles = this.getBodyUploadFiles(data);
        this.$fileUpload(this.reqUrl, null, bodyFiles, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.reqUrl = "/api/definition/update";
          this.$emit('saveApi', data);
        });
      },
      setParameters(data) {
        data.projectId = this.projectId;
        this.request.name = this.currentApi.name;
        data.protocol = this.currentProtocol;
        data.request = this.request;
        data.request.name = data.name;
        if (this.currentProtocol === "DUBBO" || this.currentProtocol === "dubbo://") {
          data.request.protocol = "dubbo://";
        } else {
          data.request.protocol = this.currentProtocol;
        }
        data.id = data.request.id;
        if (!data.method) {
          data.method = this.currentProtocol;
        }
        data.response = this.response;
      },
      getBodyUploadFiles(data) {
        let bodyUploadFiles = [];
        data.bodyUploadIds = [];
        let request = data.request;
        if (request.body) {
          if (request.body.kvs) {
            request.body.kvs.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    let fileId = getUUID().substring(0, 8);
                    item.name = item.file.name;
                    item.id = fileId;
                    data.bodyUploadIds.push(fileId);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
          if (request.body.binary) {
            request.body.binary.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    let fileId = getUUID().substring(0, 8);
                    item.name = item.file.name;
                    item.id = fileId;
                    data.bodyUploadIds.push(fileId);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
        }
        return bodyUploadFiles;
      },
    }
  }
</script>

<style scoped>


</style>
