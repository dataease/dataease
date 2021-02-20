<template>
  <div v-if="visible">
    <ms-drawer :size="60" @close="apiCaseClose" direction="bottom">
      <template v-slot:header>
        <api-case-header
          :api="api"
          @getApiTest="getApiTest"
          @setEnvironment="setEnvironment"
          @addCase="addCase"
          @selectAll="selectAll"
          :condition="condition"
          :priorities="priorities"
          :apiCaseList="apiCaseList"
          :is-read-only="isReadOnly"
          :project-id="projectId"
          :is-case-edit="isCaseEdit"
          ref="header"
        />
      </template>

      <el-container v-loading="result.loading">
        <el-main>
          <div v-for="(item,index) in apiCaseList" :key="item.id ? item.id : item.uuid">
            <api-case-item v-loading="singleLoading && singleRunId === item.id || batchLoadingIds.indexOf(item.id) > -1"
                           @refresh="refresh"
                           @singleRun="singleRun"
                           @copyCase="copyCase"
                           @showExecResult="showExecResult"
                           @batchEditCase="batchEditCase"
                           @batchRun="batchRun"
                           :environment="environment"
                           :is-case-edit="isCaseEdit"
                           :api="api"
                           :api-case="item" :index="index" ref="apiCaseItem"/>
          </div>
        </el-main>
      </el-container>
    </ms-drawer>

    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" ref="runTest"/>
    <!--批量编辑-->
    <ms-batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"/>
  </div>
</template>
<script>

  import ApiCaseHeader from "./ApiCaseHeader";
  import ApiCaseItem from "./ApiCaseItem";
  import MsRun from "../Run";
  import {getCurrentProjectID, getUUID} from "@/common/js/utils";
  import MsDrawer from "../../../../common/components/MsDrawer";
  import {CASE_ORDER} from "../../model/JsonData";
  import {API_CASE_CONFIGS} from "@/business/components/common/components/search/search-components";
  import MsBatchEdit from "../basis/BatchEdit";
  import {CASE_PRIORITY, REQ_METHOD, TCP_METHOD, SQL_METHOD, DUBBO_METHOD} from "../../model/JsonData";

  export default {
    name: 'ApiCaseList',
    components: {
      MsDrawer,
      MsRun,
      ApiCaseHeader,
      ApiCaseItem,
      MsBatchEdit
    },
    props: {
      createCase: String,
      loaded: Boolean,
      refreshSign: String,
      currentApi: {
        type: Object
      },
    },
    data() {
      return {
        result: {},
        grades: [],
        environment: {},
        isReadOnly: false,
        selectedEvent: Object,
        priorities: CASE_ORDER,
        apiCaseList: [],
        batchLoadingIds: [],
        singleLoading: false,
        singleRunId: "",
        runData: [],
        selectdCases: [],
        reportId: "",
        projectId: "",
        testCaseId: "",
        checkedCases: new Set(),
        visible: false,
        condition: {
          components: API_CASE_CONFIGS
        },
        api: {},
        typeArr: [
          {id: 'priority', name: this.$t('test_track.case.priority')},
          {id: 'method', name: this.$t('api_test.definition.api_type')},
          {id: 'path', name: this.$t('api_test.request.path')},
        ],
        priorityFilters: [
          {text: 'P0', value: 'P0'},
          {text: 'P1', value: 'P1'},
          {text: 'P2', value: 'P2'},
          {text: 'P3', value: 'P3'}
        ],
        valueArr: {
          priority: CASE_PRIORITY,
          method: REQ_METHOD,
        },
      }
    },
    watch: {
      refreshSign() {
        this.api = this.currentApi;
        this.getApiTest();
      },
      createCase() {
        this.api = this.currentApi;
        this.sysAddition();
      }
    },
    created() {
      this.api = this.currentApi;
      this.projectId = getCurrentProjectID();
      if (this.createCase) {
        this.sysAddition();
      }
    },
    computed: {
      isCaseEdit() {
        return this.testCaseId ? true : false;
      }
    },
    methods: {
      open(api, testCaseId) {
        this.api = api;
        // testCaseId 不为空则为用例编辑页面
        this.testCaseId = testCaseId;
        this.condition = {components: API_CASE_CONFIGS};
        this.getApiTest(true);
        this.visible = true;
      },
      saveApiAndCase(api) {
        this.visible = true;
        this.api = api;
        this.addCase();
      },
      setEnvironment(environment) {
        this.environment = environment;
      },
      sysAddition() {
        this.condition.projectId = this.projectId;
        this.condition.apiDefinitionId = this.api.id;
        this.$post("/api/testcase/list", this.condition, response => {
          this.apiCaseList = response.data;
          this.apiCaseList.forEach(apiCase => {
            if (apiCase.tags && apiCase.tags.length > 0) {
              apiCase.tags = JSON.parse(apiCase.tags);
              this.$set(apiCase, 'selected', false);
            }
            if (Object.prototype.toString.call(apiCase.request).match(/\[object (\w+)\]/)[1].toLowerCase() != 'object') {
              apiCase.request = JSON.parse(apiCase.request);
            }
            if (!apiCase.request.hashTree) {
              apiCase.request.hashTree = [];
            }
          })
          this.addCase();
        });
      },

      apiCaseClose() {
        this.apiCaseList = [];
        this.visible = false;
      },

      runRefresh(data) {
        this.batchLoadingIds = [];
        this.singleLoading = false;
        this.singleRunId = "";
        if (this.$refs.header.isSelectAll) {
          this.$refs.header.isSelectAll = false;
        } else {
          this.apiCaseList.forEach(item => {
            this.$set(item, 'selected', false);
          })
        }
        this.$success(this.$t('organization.integration.successful_operation'));
        this.refresh();
      },

      refresh() {
        this.getApiTest();
        this.$emit('refresh');
      },
      selectAll(isSelectAll) {
        this.apiCaseList.forEach(item => {
          this.$set(item, 'selected', isSelectAll);
        })
      },
      getApiTest(addCase) {
        if (this.api) {
          this.condition.projectId = this.projectId;
          if (this.isCaseEdit) {
            this.condition.id = this.testCaseId;
          } else {
            this.condition.apiDefinitionId = this.api.id;
          }
          this.result = this.$post("/api/testcase/list", this.condition, response => {
            this.apiCaseList = response.data;
            this.apiCaseList.forEach(apiCase => {
              if (apiCase.tags && apiCase.tags.length > 0) {
                apiCase.tags = JSON.parse(apiCase.tags);
                this.$set(apiCase, 'selected', false);
              }
              if (Object.prototype.toString.call(apiCase.request).match(/\[object (\w+)\]/)[1].toLowerCase() != 'object') {
                apiCase.request = JSON.parse(apiCase.request);
              }
              if (!apiCase.request.hashTree) {
                apiCase.request.hashTree = [];
              }
            })
            if (addCase && this.apiCaseList.length == 0 && !this.loaded) {
              this.addCase();
            }
          });
        }
      },
      addCase() {
        if (this.api.request) {
          // 初始化对象
          let request = {};
          if (this.api.request instanceof Object) {
            request = this.api.request;
          } else {
            request = JSON.parse(this.api.request);
          }
          if (!request.hashTree) {
            request.hashTree = [];
          }
          let uuid = getUUID();
          let obj = {apiDefinitionId: this.api.id, name: '', priority: 'P0', active: true, tags: [], uuid: uuid};
          obj.request = request;
          this.apiCaseList.unshift(obj);
        }
      },
      copyCase(data) {
        this.apiCaseList.unshift(data);
      },

      handleClose() {
        this.visible = false;
      },
      showExecResult(row) {
        this.visible = false;
        this.$emit('showExecResult', row);
      },

      singleRun(row) {
        if (!this.environment || !this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        this.runData = [];
        this.singleLoading = true;
        this.singleRunId = row.id;
        row.request.name = row.id;
        this.$get('/api/definition/get/' + row.request.id, response => {
          row.request.path = response.data.path;  //  取的path是对应接口的path，因此查库以获得
          row.request.useEnvironment = this.environment.id;
          this.runData.push(row.request);
          /*触发执行操作*/
          this.reportId = getUUID().substring(0, 8);
        });
      },

      batchRun() {
        if (!this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        this.runData = [];
        this.batchLoadingIds = [];
        if (this.apiCaseList.length > 0) {
          this.apiCaseList.forEach(item => {
            if (item.selected && item.id) {
              item.request.name = item.id;
              item.request.useEnvironment = this.environment.id;
              this.runData.push(item.request);
              this.batchLoadingIds.push(item.id);
            }
          })
          if (this.runData.length > 0) {
            /*触发执行操作*/
            this.reportId = getUUID().substring(0, 8);
          } else {
            this.$warning("请勾选要执行的用例！");
          }
        } else {
          this.$warning("没有可执行的用例！");
        }
      },
      batchEditCase() {
        if (this.apiCaseList.length > 0) {
          this.apiCaseList.forEach(item => {
            if (item.selected && item.id) {
              this.selectdCases.push(item.id);
            }
          })
        }
        if (this.selectdCases.length == 0) {
          this.$warning("请选择用例！");
          return;
        }
        // //根据不同的接口，注入不同的参数
        if (this.currentApi.protocol == 'HTTP') {
          this.valueArr.method = REQ_METHOD;
        } else if (this.currentApi.protocol == 'TCP') {
          this.valueArr.method = TCP_METHOD;
        } else if (this.currentApi.protocol == 'SQL') {
          this.valueArr.method = SQL_METHOD;
        } else if (this.currentApi.protocol == 'DUBBO') {
          this.valueArr.method = DUBBO_METHOD;
        }

        this.$refs.batchEdit.open();
      },
      batchEdit(form) {
        let param = {};
        param[form.type] = form.value;
        param.ids = this.selectdCases;
        param.projectId = getCurrentProjectID();
        if (this.api) {
          param.protocol = this.api.protocol;
        }
        param.selectAllDate = this.isSelectAllDate;
        param.unSelectIds = this.unSelection;
        param = Object.assign(param, this.condition);
        this.$post('/api/testcase/batch/editByParam', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.selectdCases = [];
          this.getApiTest();
        });
      },
    }
  }
</script>

<style scoped>

  .ms-drawer >>> .ms-drawer-body {
    margin-top: 80px;
  }

</style>
