<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <test-plan-case-list-header
          :project-id="getProjectId()"
          :condition="condition"
          :plan-id="planId"
          @refresh="initTable"
          @relevanceCase="$emit('relevanceCase')"
          @setEnvironment="setEnvironment"
          v-if="isPlanModel"/>
      </template>

      <el-table v-loading="result.loading"
                border
                :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelect" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore && !isReadOnly" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>

        <el-table-column prop="num" sortable="custom" label="ID" show-overflow-tooltip/>
        <el-table-column prop="name" sortable="custom" :label="$t('api_test.definition.api_name')" show-overflow-tooltip/>

        <el-table-column
          prop="priority"
          :filters="priorityFilters"
          sortable="custom"
          column-key="priority"
          :label="$t('test_track.case.priority')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          :label="$t('api_test.definition.api_path')"
          show-overflow-tooltip/>

        <el-table-column
          prop="createUser"
          column-key="user_id"
          sortable="custom"
          :filters="userFilters"
          :label="'创建人'"
          show-overflow-tooltip/>

        <el-table-column
          sortable="custom"
          width="160"
          :label="$t('api_test.definition.api_last_time')"
          prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="tags" :label="$t('commons.tag')">
          <template v-slot:default="scope">
            <div v-for="(itemName,index)  in scope.row.tags" :key="index">
              <ms-tag type="success" effect="plain" :content="itemName"/>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="'执行状态'" min-width="130" align="center">
          <template v-slot:default="scope">
            <div v-loading="rowLoading === scope.row.id">
              <el-link type="danger"
                       v-if="scope.row.execResult && scope.row.execResult === 'error'"
                       @click="getReportResult(scope.row)" v-text="getResult(scope.row.execResult)"/>
              <el-link v-else-if="scope.row.execResult && scope.row.execResult === 'success'"
                       @click="getReportResult(scope.row)" v-text="getResult(scope.row.execResult)">

              </el-link>
              <div v-else v-text="getResult(scope.row.execResult)"/>

              <div v-if="scope.row.id" style="color: #999999;font-size: 12px">
                <span> {{ scope.row.updateTime | timestampFormatDate }}</span>
                {{ scope.row.updateUser }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column v-if="!isReadOnly" :label="$t('commons.operating')" align="center">
          <template v-slot:default="scope">
            <ms-table-operator-button class="run-button" :is-tester-permission="true" :tip="$t('api_test.run')" icon="el-icon-video-play"
                                      @exec="singleRun(scope.row)" v-tester/>
            <ms-table-operator-button :is-tester-permission="true" :tip="$t('test_track.plan_view.cancel_relevance')"
                                      icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)" v-tester/>
          </template>
        </el-table-column>

      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <test-plan-api-case-result :response="response" ref="apiCaseResult"/>

      <!-- 执行组件 -->
      <ms-run :debug="false" :type="'API_PLAN'" :reportId="reportId" :run-data="runData"
              @runRefresh="runRefresh" ref="runTest"/>

    </el-card>
  </div>

</template>

<script>

import MsTableOperator from "../../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../../common/components/MsTableOperatorButton";
import MsTablePagination from "../../../../../common/pagination/TablePagination";
import MsTag from "../../../../../common/components/MsTag";
import MsApiCaseList from "../../../../../api/definition/components/case/ApiCaseList";
import ApiCaseList from "../../../../../api/definition/components/case/ApiCaseList";
import MsContainer from "../../../../../common/components/MsContainer";
import MsBottomContainer from "../../../../../api/definition/components/BottomContainer";
import ShowMoreBtn from "../../../../case/components/ShowMoreBtn";
import MsBatchEdit from "../../../../../api/definition/components/basis/BatchEdit";
import {API_METHOD_COLOUR, CASE_PRIORITY, RESULT_MAP} from "../../../../../api/definition/model/JsonData";
import {getCurrentProjectID} from "@/common/js/utils";
import ApiListContainer from "../../../../../api/definition/components/list/ApiListContainer";
import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
import {getBodyUploadFiles, getUUID} from "../../../../../../../common/js/utils";
import TestPlanCaseListHeader from "./TestPlanCaseListHeader";
import MsRun from "../../../../../api/definition/components/Run";
import TestPlanApiCaseResult from "./TestPlanApiCaseResult";
import TestPlan from "../../../../../api/definition/components/jmeter/components/test-plan";
import ThreadGroup from "../../../../../api/definition/components/jmeter/components/thread-group";
import {WORKSPACE_ID} from "@/common/js/constants";
import {_filter, _sort} from "@/common/js/tableUtils";


export default {
  name: "TestPlanApiCaseList",
  components: {
    TestPlanApiCaseResult,
    MsRun,
    TestPlanCaseListHeader,
    ApiCaseList,
    PriorityTableItem,
    ApiListContainer,
    MsTableOperatorButton,
    MsTableOperator,
    MsTablePagination,
    MsTag,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    ShowMoreBtn,
    MsBatchEdit
  },
  data() {
    return {
      condition: {},
      selectCase: {},
      result: {},
      moduleId: "",
      status:'default',
      deletePath: "/test/case/delete",
      selectRows: new Set(),
      buttons: [
        {name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch},
        {name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute}
      ],
      typeArr: [
        {id: 'priority', name: this.$t('test_track.case.priority')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      valueArr: {
        priority: CASE_PRIORITY,
        userId: [],
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
      // environmentId: undefined,
      currentCaseProjectId: "",
      runData: [],
      reportId: "",
      response: {},
      rowLoading: "",
      userFilters: []
    }
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    visible: {
      type: Boolean,
      default: false,
    },
    isApiListEnable: {
      type: Boolean,
      default: false,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    model: {
      type: String,
      default() {
        'api'
      }
    },
    planId: String,
    clickType:String
  },
  created: function () {
    this.getMaintainerOptions();
    this.initTable();
  },
  activated() {
    this.status ='default'
  },
  watch: {
    selectNodeIds() {
      this.initTable();
    },
    currentProtocol() {
      this.initTable();
    },
    planId() {
      this.initTable();
    }
  },
  computed: {
    // 测试计划关联测试列表
    isRelevanceModel() {
      return this.model === 'relevance'
    },
    // 测试计划接口用例列表
    isPlanModel() {
      return this.model === 'plan'
    },
    // 接口定义用例列表
    isApiModel() {
      return this.model === 'api'
    },
  },
  methods: {
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.valueArr.userId = response.data;
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id}
        });
      });
    },
    isApiListEnableChange(data) {
      this.$emit('isApiListEnableChange', data);
    },
    initTable() {
      this.selectRows = new Set();
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;

      this.condition.planId = this.planId;

      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      if(this.clickType){
        if(this.status =='default'){
          this.condition.status = this.clickType;
        }else{
          this.condition.status = null;
        }
        this.status = 'all';
      }
      this.result = this.$post('/test/plan/api/case/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        })
      });
    },
    handleSelect(selection, row) {
      row.hashTree = [];
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
      let arr = Array.from(this.selectRows);
      // 选中1个以上的用例时显示更多操作
      if (this.selectRows.size === 1) {
        this.$set(arr[0], "showMore", false);
      } else if (this.selectRows.size === 2) {
        arr.forEach(row => {
          this.$set(row, "showMore", true);
        })
      }
    },
    showExecResult(row) {
      this.$emit('showExecResult', row);
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTable();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTable();
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        if (selection.length === 1) {
          selection.hashTree = [];
          this.selectRows.add(selection[0]);
        } else {
          this.tableData.forEach(item => {
            item.hashTree = [];
            this.$set(item, "showMore", true);
            this.selectRows.add(item);
          });
        }
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        })
      }
    },
    search() {
      this.initTable();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    reductionApi(row) {
      let ids = [row.id];
      this.$post('/api/testcase/reduction/', ids, () => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = {};
            param.ids = Array.from(this.selectRows).map(row => row.id);
            param.planId = this.planId;
            this.$post('/test/plan/api/case/batch/delete', param, () => {
              this.selectRows.clear();
              this.initTable();
              this.$emit('refresh');
              this.$success(this.$t('test_track.cancel_relevance_success'));
            });
          }
        }
      });
    },
    getResult(data) {
      if (RESULT_MAP.get(data)) {
        return RESULT_MAP.get(data);
      } else {
        return RESULT_MAP.get("default");
      }
    },
    runRefresh(data) {
      this.rowLoading = "";
      this.$success(this.$t('schedule.event_success'));
      this.initTable();
    },
    singleRun(row) {
      if (!row.environmentId) {
        this.$warning(this.$t('api_test.environment.select_environment'));
        return;
      }
      this.runData = [];

      this.rowLoading = row.id;

      this.$get('/api/testcase/get/' + row.caseId, (response) => {
        let apiCase = response.data;
        let request = JSON.parse(apiCase.request);
        request.name = row.id;
        request.id = row.id;
        request.useEnvironment = row.environmentId;
        this.runData.push(request);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      });
    },
    batchEdit(form) {
      let arr = Array.from(this.selectRows);
      let ids = arr.map(row => row.id);
      let param = {};
      param[form.type] = form.value;
      param.ids = ids;
      this.$post('/api/testcase/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.initTable();
      });
    },
    handleBatchExecute() {
      this.selectRows.forEach(row => {
        this.$get('/api/testcase/get/' + row.caseId, (response) => {
          let apiCase = response.data;
          let request = JSON.parse(apiCase.request);
          request.name = row.id;
          request.id = row.id;
          request.useEnvironment = row.environmentId;
          let runData = [];
          runData.push(request);
          this.batchRun(runData, getUUID().substring(0, 8));
        });
      });
      this.$message('任务执行中，请稍后刷新查看结果');
      this.search();
    },
    batchRun(runData, reportId) {
      let testPlan = new TestPlan();
      let threadGroup = new ThreadGroup();
      threadGroup.hashTree = [];
      testPlan.hashTree = [threadGroup];
      runData.forEach(item => {
        threadGroup.hashTree.push(item);
      });
      let reqObj = {id: reportId, testElement: testPlan, type: 'API_PLAN', reportId: "run"};
      let bodyFiles = getBodyUploadFiles(reqObj, runData);
      this.$fileUpload("/api/definition/run", null, bodyFiles, reqObj, response => {
      });
    },
    handleDelete(apiCase) {
      this.$get('/test/plan/api/case/delete/' + apiCase.id, () => {
        this.$success(this.$t('test_track.cancel_relevance_success'));
        this.$emit('refresh');
        this.initTable();
      });
      return;
    },
    getProjectId() {
      if (!this.isRelevanceModel) {
        return getCurrentProjectID();
      } else {
        return this.currentCaseProjectId;
      }
    },
    setEnvironment(data) {
      //   this.environmentId = data.id;
    },
    getReportResult(apiCase) {
      let url = "/api/definition/report/getReport/" + apiCase.id + '/' + 'API_PLAN';
      this.$get(url, response => {
        if (response.data) {
          this.response = JSON.parse(response.data.content);
          this.$refs.apiCaseResult.open();
        }
      });
    },
  },
}
</script>

<style scoped>
.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 300px;
  /*margin-bottom: 20px;*/
  margin-right: 20px;
}

</style>
