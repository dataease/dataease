<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="initTableData"
                         :show-create="false" :tip="$t('commons.search_by_id_name_tag')">
          <template v-slot:title>
            <node-breadcrumb class="table-title" :nodes="selectParentNodes" @refresh="breadcrumbRefresh"/>
          </template>
          <template v-slot:button>
            <ms-table-button :is-tester-permission="true" v-if="!showMyTestCase" icon="el-icon-s-custom"
                             :content="$t('test_track.plan_view.my_case')" @click="searchMyTestCase"/>
            <ms-table-button :is-tester-permission="true" v-if="showMyTestCase" icon="el-icon-files"
                             :content="$t('test_track.plan_view.all_case')" @click="searchMyTestCase"/>
            <ms-table-button :is-tester-permission="true" icon="el-icon-connection"
                             :content="$t('test_track.plan_view.relevance_test_case')"
                             @click="$emit('openTestCaseRelevanceDialog')"/>
            <ms-table-button :is-tester-permission="true" icon="el-icon-document-remove"
                             :content="$t('test_track.plan_view.cancel_all_relevance')" @click="handleDeleteBatch"/>
          </template>
        </ms-table-header>
      </template>

      <executor-edit ref="executorEdit" :select-ids="new Set(Array.from(this.selectRows).map(row => row.id))"
                     @refresh="initTableData"/>
      <status-edit ref="statusEdit" :plan-id="planId"
                   :select-ids="new Set(Array.from(this.selectRows).map(row => row.id))" @refresh="initTableData"/>

      <el-table
        class="adjust-table"
        border
        @select-all="handleSelectAll"
        @filter-change="filter"
        @sort-change="sort"
        @select="handleSelectionChange"
        row-key="id"
        @row-click="showDetail"
        @header-dragend="headerDragend"
        :data="tableData">

        <el-table-column
          type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="num"
          sortable="custom"
          :label="$t('commons.id')"
          min-width="120px"

          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          min-width="120px"

          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          :filters="priorityFilters"
          column-key="priority"
          min-width="100px"

          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="type"
          :filters="typeFilters"
          column-key="type"
          :label="$t('test_track.case.type')"
          min-width="80px"

          show-overflow-tooltip>
          <template v-slot:default="scope">
            <type-table-item :value="scope.row.type"/>
          </template>
        </el-table-column>

        <el-table-column prop="tags" :label="$t('commons.tag')" min-width="120px"
                         >
          <template v-slot:default="scope">
            <div v-for="(tag, index) in scope.row.showTags" :key="tag + '_' + index">
              <ms-tag type="success" effect="plain" :content="tag"/>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="method"
          :filters="methodFilters"
          column-key="method"
          :label="$t('test_track.case.method')"
          min-width="100px"

          show-overflow-tooltip>
          <template v-slot:default="scope">
            <method-table-item :value="scope.row.method"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="nodePath"
          :label="$t('test_track.case.module')"
          min-width="120px"

          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="projectName"
          :label="$t('test_track.plan.plan_project')"
          min-width="120px"

          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          :label="$t('test_track.issue.issue')"
          min-width="80px"

          show-overflow-tooltip>
          <template v-slot:default="scope">
            <el-popover
              placement="right"
              width="400"
              trigger="hover">
              <el-table border class="adjust-table" :data="scope.row.issuesContent" style="width: 100%">
                <el-table-column prop="title" :label="$t('test_track.issue.title')" show-overflow-tooltip/>
                <el-table-column prop="description" :label="$t('test_track.issue.description')">
                  <template v-slot:default="scope">
                    <el-popover
                      placement="left"
                      width="400"
                      trigger="hover"
                    >
                      <ckeditor :editor="editor" disabled :config="editorConfig"
                                v-model="scope.row.description"/>
                      <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
                    </el-popover>
                  </template>
                </el-table-column>
                <el-table-column prop="platform" :label="$t('test_track.issue.platform')"/>
              </el-table>
              <el-button slot="reference" type="text">{{ scope.row.issuesSize }}</el-button>
            </el-popover>
          </template>
        </el-table-column>


        <el-table-column
          prop="executorName"
          :filters="executorFilters"
          min-width="100px"

          column-key="executor"
          :label="$t('test_track.plan_view.executor')">
        </el-table-column>

        <el-table-column
          prop="status"
          :filters="statusFilters"
          column-key="status"
          min-width="100px"

          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <span @click.stop="clickt = 'stop'">
              <el-dropdown class="test-case-status" @command="statusChange">
                <span class="el-dropdown-link">
                  <status-table-item :value="scope.row.status"/>
                </span>
                <el-dropdown-menu slot="dropdown" chang>
                  <el-dropdown-item :disabled="!isTestManagerOrTestUser" :command="{id: scope.row.id, status: 'Pass'}">
                    {{ $t('test_track.plan_view.pass') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                    :command="{id: scope.row.id, status: 'Failure'}">
                    {{ $t('test_track.plan_view.failure') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                    :command="{id: scope.row.id, status: 'Blocking'}">
                    {{ $t('test_track.plan_view.blocking') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!isTestManagerOrTestUser" :command="{id: scope.row.id, status: 'Skip'}">
                    {{ $t('test_track.plan_view.skip') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </template>
        </el-table-column>

        <el-table-column
          sortable
          prop="updateTime"
          :label="$t('commons.update_time')"
          min-width="120px"

          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          min-width="100"
          :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator-button :is-tester-permission="true" :tip="$t('commons.edit')" icon="el-icon-edit"
                                      @exec="handleEdit(scope.row)"/>
            <ms-table-operator-button :is-tester-permission="true" :tip="$t('test_track.plan_view.cancel_relevance')"
                                      icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <functional-test-case-edit
        ref="testPlanTestCaseEdit"
        :search-param.sync="condition"
        @refresh="initTableData"
        :is-read-only="isReadOnly"
        @refreshTable="search"/>

    </el-card>
    <batch-edit ref="batchEdit" @batchEdit="batchEdit"
                :type-arr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')"/>
  </div>
</template>

<script>
import ExecutorEdit from '../ExecutorEdit';
import StatusEdit from '../StatusEdit';
import FunctionalTestCaseEdit from "./FunctionalTestCaseEdit";
import MsTipButton from '../../../../../common/components/MsTipButton';
import MsTablePagination from '../../../../../common/pagination/TablePagination';
import MsTableHeader from '../../../../../common/components/MsTableHeader';
import MsTableButton from '../../../../../common/components/MsTableButton';
import NodeBreadcrumb from '../../../../common/NodeBreadcrumb';

import {ROLE_TEST_MANAGER, ROLE_TEST_USER, TokenKey, WORKSPACE_ID} from "@/common/js/constants";
import {checkoutTestManagerOrTestUser, hasRoles} from "@/common/js/utils";
import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
import StatusTableItem from "../../../../common/tableItems/planview/StatusTableItem";
import TypeTableItem from "../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../common/tableItems/planview/MethodTableItem";
import MsTableOperator from "../../../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../../../common/components/MsTableOperatorButton";
import {TEST_CASE_CONFIGS} from "../../../../../common/components/search/search-components";
import ShowMoreBtn from "../../../../case/components/ShowMoreBtn";
import BatchEdit from "../../../../case/components/BatchEdit";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
import {hub} from "@/business/components/track/plan/event-bus";
import MsTag from "@/business/components/common/components/MsTag";
import {_filter, _sort} from "@/common/js/tableUtils";


export default {
  name: "FunctionalTestCaseList",
  components: {
    FunctionalTestCaseEdit,
    MsTableOperatorButton,
    MsTableOperator,
    MethodTableItem,
    TypeTableItem,
    StatusTableItem,
    PriorityTableItem, StatusEdit, ExecutorEdit, MsTipButton, MsTablePagination,
    MsTableHeader, NodeBreadcrumb, MsTableButton, ShowMoreBtn,
    BatchEdit, MsTag
  },
  data() {
    return {
      result: {},
      deletePath: "/test/case/delete",
      condition: {
        components: TEST_CASE_CONFIGS
      },
      showMyTestCase: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      status: 'default',
      selectRows: new Set(),
      testPlan: {},
      isReadOnly: false,
      isTestManagerOrTestUser: false,
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      methodFilters: [
        {text: this.$t('test_track.case.manual'), value: 'manual'},
        {text: this.$t('test_track.case.auto'), value: 'auto'}
      ],
      typeFilters: [
        {text: this.$t('commons.functional'), value: 'functional'},
        {text: this.$t('commons.performance'), value: 'performance'},
        {text: this.$t('commons.api'), value: 'api'}
      ],
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan_view.pass'), value: 'Pass'},
        {text: this.$t('test_track.plan_view.failure'), value: 'Failure'},
        {text: this.$t('test_track.plan_view.blocking'), value: 'Blocking'},
        {text: this.$t('test_track.plan_view.skip'), value: 'Skip'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
      ],
      executorFilters: [],
      showMore: false,
      buttons: [
        {
          name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleBatchEdit
        },
        {
          name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch
        }
      ],
      typeArr: [
        {id: 'status', name: this.$t('test_track.plan_view.execute_result')},
        {id: 'executor', name: this.$t('test_track.plan_view.executor')},
      ],
      valueArr: {
        executor: [],
        status: [
          {name: this.$t('test_track.plan_view.pass'), id: 'Pass'},
          {name: this.$t('test_track.plan_view.failure'), id: 'Failure'},
          {name: this.$t('test_track.plan_view.blocking'), id: 'Blocking'},
          {name: this.$t('test_track.plan_view.skip'), id: 'Skip'}
        ]
      },
      editor: ClassicEditor,
      editorConfig: {
        // 'increaseIndent','decreaseIndent'
        toolbar: [],
      },
    }
  },
  props: {
    planId: {
      type: String
    },
    clickType: String,
    selectNodeIds: {
      type: Array
    },
    selectParentNodes: {
      type: Array
    }
  },
  watch: {
    planId() {
      this.refreshTableAndPlan();
    },
    selectNodeIds() {
      this.search();
    }
  },
  mounted() {
    hub.$on("openFailureTestCase", row => {
      this.isReadOnly = true;
      this.condition.status = 'Failure';
      this.$refs.testPlanTestCaseEdit.openTestCaseEdit(row);
    });
    this.refreshTableAndPlan();
    this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    this.getMaintainerOptions();
  },
  beforeDestroy() {
    hub.$off("openFailureTestCase");
  },
  methods: {
    initTableData() {
      if (this.planId) {
        // param.planId = this.planId;
        this.condition.planId = this.planId;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        // param.nodeIds = this.selectNodeIds;
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (this.planId) {
        this.result = this.$post(this.buildPagePath('/test/plan/case/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          for (let i = 0; i < this.tableData.length; i++) {
            if (this.tableData[i]) {
              this.$set(this.tableData[i], "showTags", JSON.parse(this.tableData[i].tags));
              this.$set(this.tableData[i], "issuesSize", 0);
              this.$get("/issues/get/" + this.tableData[i].caseId).then(response => {
                let issues = response.data.data;
                if (this.tableData[i]) {
                  this.$set(this.tableData[i], "issuesSize", issues.length);
                  this.$set(this.tableData[i], "issuesContent", issues);
                }
              }).catch(() => {
                this.$set(this.tableData[i], "issuesContent", [{
                  title: '获取缺陷失败',
                  description: '获取缺陷失败',
                  platform: '获取缺陷失败'
                }]);
              })
            }
          }
          this.selectRows.clear();
        });
      }
    },
    showDetail(row, event, column) {
      this.isReadOnly = true;
      this.$refs.testPlanTestCaseEdit.openTestCaseEdit(row);
    },
    refresh() {
      this.condition = {components: TEST_CASE_CONFIGS};
      this.selectRows.clear();
      this.$emit('refresh');
    },
    breadcrumbRefresh() {
      this.showMyTestCase = false;
      this.refresh();
    },
    refreshTableAndPlan() {
      this.getTestPlanById();
      this.initTableData();
    },
    refreshTestPlanRecent() {
      if (hasRoles(ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        let param = {};
        param.id = this.planId;
        param.updateTime = Date.now();
        this.$post('/test/plan/edit', param);
      }
    },
    search() {
      this.initTableData();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleEdit(testCase, index) {
      this.isReadOnly = false;
      if (!checkoutTestManagerOrTestUser()) {
        this.isReadOnly = true;
      }
      this.$refs.testPlanTestCaseEdit.openTestCaseEdit(testCase);
    },
    handleDelete(testCase) {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + ' ' + testCase.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(testCase);
          }
        }
      });
    },
    handleDeleteBatch() {
      if (this.tableData.length < 1) {
        this.$warning(this.$t('test_track.plan_view.no_case_relevance'));
        return;
      }
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            if (this.selectRows.size > 0) {
              let ids = Array.from(this.selectRows).map(row => row.id);
              this._handleBatchDelete(ids);
            } else {
              if (this.planId) {
                this.condition.planId = this.planId;
              }
              if (this.selectNodeIds && this.selectNodeIds.length > 0) {
                this.condition.nodeIds = this.selectNodeIds;
              }
              // 根据条件查询计划下所有的关联用例
              this.$post('/test/plan/case/list/all', this.condition, res => {
                let data = res.data;
                let ids = data.map(d => d.id);
                this._handleBatchDelete(ids);
              })
            }
          }
        }
      });
    },
    _handleBatchDelete(ids) {
      this.result = this.$post('/test/plan/case/batch/delete', {ids: ids}, () => {
        this.selectRows.clear();
        this.$emit("refresh");
        this.$success(this.$t('test_track.cancel_relevance_success'));
      });
    },
    _handleDelete(testCase) {
      let testCaseId = testCase.id;
      this.result = this.$post('/test/plan/case/delete/' + testCaseId, {}, () => {
        this.$emit("refresh");
        this.$success(this.$t('test_track.cancel_relevance_success'));
      });
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.tableData.forEach(item => {
          this.$set(item, "showMore", true);
          this.selectRows.add(item);
        });
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        })
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
    },
    handleBatch(type) {
      if (this.selectRows.size < 1) {
        this.$warning(this.$t('test_track.plan_view.select_manipulate'));
        return;
      }
      if (type === 'executor') {
        this.$refs.executorEdit.openExecutorEdit();
      } else if (type === 'status') {
        this.$refs.statusEdit.openStatusEdit();
      } else if (type === 'delete') {
        this.handleDeleteBatch();
      }
    },
    searchMyTestCase() {
      this.showMyTestCase = !this.showMyTestCase;
      if (this.showMyTestCase) {
        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.condition.executor = user.id;
      } else {
        this.condition.executor = null;
      }
      this.initTableData();
    },
    statusChange(param) {
      this.$post('/test/plan/case/edit', param, () => {
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id == param.id) {
            this.tableData[i].status = param.status;
            break;
          }
        }
      });
    },
    getTestPlanById() {
      if (this.planId) {
        this.$post('/test/plan/get/' + this.planId, {}, response => {
          this.testPlan = response.data;
          this.refreshTestPlanRecent();
        });
      }
    },

    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTableData();
    },
    headerDragend(newWidth,oldWidth,column,event){
      let finalWidth = newWidth;
      if(column.minWidth>finalWidth){
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    batchEdit(form) {
      let param = {};
      param[form.type] = form.value;
      param.ids = Array.from(this.selectRows).map(row => row.id);
      this.$post('/test/plan/case/batch/edit', param, () => {
        this.selectRows.clear();
        this.status = '';
        this.$post('/test/plan/edit/status/' + this.planId);
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
      });
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.valueArr.executor = response.data;
        this.executorFilters = response.data.map(u => {
          return {text: u.name, value: u.id}
        });
      });
    }
  }
}
</script>

<style scoped>

.search {
  margin-left: 10px;
  width: 240px;
}

.test-case-status, .el-table {
  cursor: pointer;
}

.el-tag {
  margin-left: 10px;
}
</style>
