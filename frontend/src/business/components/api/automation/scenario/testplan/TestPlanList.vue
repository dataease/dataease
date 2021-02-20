<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :is-tester-permission="true" :condition.sync="condition"
                       @search="initTableData" :showCreate="false"
                       :title="$t('test_track.plan.test_plan')"/>
    </template>

    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      @sort-change="sort"
      @select-all="select"
      @select="select">
      <el-table-column type="selection"/>

      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="userName"
        :label="$t('test_track.plan.plan_principal')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="status"
        column-key="status"
        :filters="statusFilters"
        :label="$t('test_track.plan.plan_status')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <el-dropdown class="test-case-status" @command="statusChange">
              <span class="el-dropdown-link">
                <plan-status-table-item :value="scope.row.status"/>
              </span>
              <el-dropdown-menu slot="dropdown" chang>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser" :command="{id: scope.row.id, status: 'Prepare'}">
                  {{ $t('test_track.plan.plan_status_prepare') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{id: scope.row.id, status: 'Underway'}">
                  {{ $t('test_track.plan.plan_status_running') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{id: scope.row.id, status: 'Completed'}">
                  {{ $t('test_track.plan.plan_status_completed') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </template>
      </el-table-column>
      <el-table-column
        prop="stage"
        column-key="stage"
        :filters="stageFilters"
        :label="$t('test_track.plan.plan_stage')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <plan-stage-table-item :stage="scope.row.stage"/>
        </template>
      </el-table-column>
      <el-table-column
        prop="projectName"
        :label="$t('test_track.home.test_rate')"
        min-width="100"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <el-progress :percentage="scope.row.testRate"></el-progress>
        </template>
      </el-table-column>
      <el-table-column
        prop="projectName"
        :label="$t('test_track.plan.plan_project')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        sortable
        prop="plannedStartTime"
        :label="$t('test_track.plan.planned_start_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.plannedStartTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="plannedEndTime"
        :label="$t('test_track.plan.planned_end_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.plannedEndTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="actualStartTime"
        :label="$t('test_track.plan.actual_start_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.actualStartTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="actualEndTime"
        :label="$t('test_track.plan.actual_end_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.actualEndTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </el-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <test-report-template-list @openReport="openReport" ref="testReportTemplateList"/>
    <test-case-report-view @refresh="initTableData" ref="testCaseReportView"/>
    <ms-delete-confirm :title="$t('test_track.plan.plan_delete')" @delete="_handleDelete" ref="deleteConfirm" :with-tip="enableDeleteTip">
      {{$t('test_track.plan.plan_delete_tip')}}
    </ms-delete-confirm>

    <ms-dialog-footer style="float: right;margin: 20px"
                      @confirm="confirm" @cancel="cancel">
    </ms-dialog-footer>

  </el-card>
</template>

<script>
import MsCreateBox from '../../../../settings/CreateBox';
import MsTablePagination from '../../../../../components/common/pagination/TablePagination';
import MsTableHeader from "../../../../common/components/MsTableHeader";
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";
import MsTableOperator from "../../../../common/components/MsTableOperator";
import PlanStatusTableItem from "../../../../track/common/tableItems/plan/PlanStatusTableItem";
import PlanStageTableItem from "../../../../track/common/tableItems/plan/PlanStageTableItem";
import {checkoutTestManagerOrTestUser} from "@/common/js/utils";
import TestReportTemplateList from "../../../../track/plan/view/comonents/TestReportTemplateList";
import TestCaseReportView from "../../../../track/plan/view/comonents/report/TestCaseReportView";
import MsDeleteConfirm from "../../../../common/components/MsDeleteConfirm";
import {TEST_PLAN_CONFIGS} from "../../../../common/components/search/search-components";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
import {getCurrentProjectID} from "../../../../../../common/js/utils";
import {_filter, _sort} from "@/common/js/tableUtils";


export default {
  name: "TestPlanList",
  components: {
    MsDeleteConfirm,
    TestCaseReportView,
    TestReportTemplateList,
    PlanStageTableItem,
    PlanStatusTableItem,
    MsTableOperator, MsTableOperatorButton, MsDialogFooter, MsTableHeader, MsCreateBox, MsTablePagination
  },
  data() {
      return {
        result: {},
        selection: [],
        enableDeleteTip: false,
        queryPath: "/test/plan/list",
        deletePath: "/test/plan/delete",
        condition: {
          components: TEST_PLAN_CONFIGS
        },
        currentPage: 1,
        pageSize: 10,
        isTestManagerOrTestUser: false,
        total: 0,
        tableData: [],
        statusFilters: [
          {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
          {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
          {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'}
        ],
        stageFilters: [
          {text: this.$t('test_track.plan.smoke_test'), value: 'smoke'},
          {text: this.$t('test_track.plan.system_test'), value: 'system'},
          {text: this.$t('test_track.plan.regression_test'), value: 'regression'},
        ],
      }
    },
    watch: {
      '$route'(to, from) {
        if (to.path.indexOf("/track/plan/all") >= 0) {
          this.initTableData();
        }
      }
    },
    created() {
      this.projectId = this.$route.params.projectId;
      this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
      this.initTableData();
    },
    methods: {
      confirm() {
        if (this.selection.length==0) {
          this.$warning(this.$t("api_test.definition.request.test_plan_select"));
        }else{
          this.$emit('addTestPlan', this.selection);
        }
      },
      cancel(){
        this.$emit('cancel');
      },
      select(selection) {
        this.selection = selection.map(s => s.id);
        this.$emit('selection', selection);
      },
      initTableData() {
        if (this.planId) {
          this.condition.planId = this.planId;
        }
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          this.condition.nodeIds = this.selectNodeIds;
        }
        if (!getCurrentProjectID()) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          for (let i = 0; i < this.tableData.length; i++) {
            let path = "/test/plan/project";
            this.$post(path, {planId: this.tableData[i].id}, res => {
              let arr = res.data;
              let projectIds = arr.map(data => data.id);
              this.$set(this.tableData[i], "projectIds", projectIds);
            })
          }
        });
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      testPlanCreate() {
        this.$emit('openTestPlanEditDialog');
      },
      handleEdit(testPlan) {
        this.$emit('testPlanEdit', testPlan);
      },
      statusChange(param) {
        this.$post('/test/plan/edit', param, () => {
          for (let i = 0; i < this.tableData.length; i++) {
            if (this.tableData[i].id == param.id) {
              this.tableData[i].status = param.status;
              break;
            }
          }
        });
      },
      handleDelete(testPlan) {
        this.enableDeleteTip = testPlan.status === 'Underway' ? true : false;
        this.$refs.deleteConfirm.open(testPlan);
      },
      _handleDelete(testPlan) {
        let testPlanId = testPlan.id;
        this.$post('/test/plan/delete/' + testPlanId, {}, () => {
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
          // 发送广播，刷新 head 上的最新列表
          TrackEvent.$emit(LIST_CHANGE);
        });
      },
      intoPlan(row, event, column) {
        this.$router.push('/track/plan/view/' + row.id);
      },
      filter(filters) {
        _filter(filters, this.condition);
        this.initTableData();
      },
      sort(column) {
        _sort(column, this.condition);
        this.initTableData();
      },
      openTestReportTemplate(data) {
        this.$refs.testReportTemplateList.open(data.id);
      },
      openReport(planId, reportId) {
        if (reportId) {
          this.$refs.testCaseReportView.open(planId, reportId);
        }
      },
    }
  }
</script>

<style scoped>

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .el-table {
    cursor: pointer;
  }
</style>
