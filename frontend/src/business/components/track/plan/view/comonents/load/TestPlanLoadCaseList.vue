<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <test-plan-load-case-list-header
          :condition="condition"
          :plan-id="planId"
          @refresh="initTable"
          @relevanceCase="$emit('relevanceCase')"
        />
      </template>

      <el-table v-loading="result.loading"
                border
                :data="tableData" row-key="id" class="test-content adjust-table"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelectionChange" :height="screenHeight">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore && !isReadOnly" :buttons="buttons" :size="selectRows.size"/>
          </template>
        </el-table-column>

        <el-table-column prop="num" label="ID" show-overflow-tooltip/>
        <el-table-column
          prop="caseName"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="projectName"
          :label="$t('load_test.project_name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="userName"
          :label="$t('load_test.user_name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          sortable
          prop="createTime"
          :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          column-key="status"
          :filters="statusFilters"
          :label="$t('commons.status')">
          <template v-slot:default="{row}">
            <ms-performance-test-status :row="row"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="caseStatus"
          :label="$t('test_track.plan.load_case.execution_status')">
          <template v-slot:default="{row}">
            <el-tag size="mini" type="danger" v-if="row.caseStatus === 'error'">
              {{ row.caseStatus }}
            </el-tag>
            <el-tag size="mini" type="success" v-else-if="row.caseStatus === 'success'">
              {{ row.caseStatus }}
            </el-tag>
            <el-tag size="mini" v-else-if="row.caseStatus === 'run'">
              {{ row.caseStatus }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('test_track.plan.load_case.report')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <div v-loading="loading === scope.row.id">
              <el-link type="info" @click="getReport(scope.row)" v-if="scope.row.loadReportId">
                {{ $t('test_track.plan.load_case.view_report') }}
              </el-link>
              <span v-else> - </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isReadOnly" :label="$t('commons.operating')" align="center">
          <template v-slot:default="scope">
            <ms-table-operator-button class="run-button" :is-tester-permission="true" :tip="$t('api_test.run')"
                                      icon="el-icon-video-play"
                                      @exec="run(scope.row)" v-tester/>
            <ms-table-operator-button :is-tester-permission="true" :tip="$t('test_track.plan_view.cancel_relevance')"
                                      icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)" v-tester/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <load-case-report :report-id="reportId" ref="loadCaseReport" @refresh="initTable"/>
  </div>
</template>

<script>
import TestPlanLoadCaseListHeader
  from "@/business/components/track/plan/view/comonents/load/TestPlanLoadCaseListHeader";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import LoadCaseReport from "@/business/components/track/plan/view/comonents/load/LoadCaseReport";
import {_filter, _sort} from "@/common/js/tableUtils";

export default {
  name: "TestPlanLoadCaseList",
  components: {
    LoadCaseReport,
    TestPlanLoadCaseListHeader,
    ShowMoreBtn,
    MsTablePagination,
    MsPerformanceTestStatus,
    MsTableOperatorButton
  },
  data() {
    return {
      condition: {},
      result: {},
      tableData: [],
      selectRows: new Set(),
      currentPage: 1,
      pageSize: 10,
      total: 0,
      status: 'default',
      screenHeight: document.documentElement.clientHeight - 330,//屏幕高度
      buttons: [
        {
          name: this.$t('test_track.plan.load_case.unlink_in_bulk'), handleClick: this.handleDeleteBatch
        },
        {
          name: this.$t('test_track.plan.load_case.batch_exec_cases'), handleClick: this.handleRunBatch
        }
      ],
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      reportId: '',
      loading: false,
      statusScheduler: null
    }
  },
  props: {
    selectProjectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    planId: String,
    clickType: String,
  },
  created() {
    this.initTable();
    this.refreshStatus();
  },
  watch: {
    selectProjectId() {
      this.initTable();
    },
    planId() {
      this.initTable();
    }
  },
  methods: {
    initTable() {
      this.selectRows = new Set();
      this.condition.testPlanId = this.planId;
      if (this.selectProjectId && this.selectProjectId !== 'root') {
        this.condition.projectId = this.selectProjectId;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        }else{
          this.condition.status = null;
        }
        this.status = 'all';
      }
      this.$post("/test/plan/load/case/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
        let data = response.data;
        let {itemCount, listObject} = data;
        this.total = itemCount;
        this.tableData = listObject;
      })
    },
    refreshStatus() {
      this.refreshScheduler = setInterval(() => {
        // 如果有状态不是最终状态则定时查询
        let arr = this.tableData.filter(data => data.status !== 'Completed' && data.status !== 'Error' && data.status !== 'Saved');
        arr.length > 0 ? this.initTable() : clearInterval(this.refreshScheduler);
      }, 8000);
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
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            this.result = this.$post('/test/plan/load/case/batch/delete', ids, () => {
              this.selectRows.clear();
              this.initTable();
              this.$success(this.$t('test_track.cancel_relevance_success'));
            });
          }
        }
      })
    },
    handleRunBatch() {
      this.selectRows.forEach(loadCase => {
        this._run(loadCase);
      })
      this.refreshStatus();
    },
    run(loadCase) {
      this._run(loadCase);
      this.refreshStatus();
    },
    _run(loadCase) {
      this.$post('/test/plan/load/case/run', {
        id: loadCase.loadCaseId,
        testPlanLoadId: loadCase.id,
        triggerMode: 'CASE'
      }).then(() => {
        this.$notify.success({
          title: loadCase.caseName,
          message: this.$t('test_track.plan.load_case.exec').toString()
        });
        this.updateStatus(loadCase, 'run');
      }).catch(() => {
        this.updateStatus(loadCase, 'error');
        this.$notify.error({
          title: loadCase.caseName,
          message: this.$t('test_track.plan.load_case.error').toString()
        });
      })
    },
    updateStatus(loadCase, status) {
      this.$post('/test/plan/load/case/update', {id: loadCase.id, status: status}, () => {
        this.$post('/test/plan/edit/status/' + loadCase.testPlanId, {}, () => {
          this.initTable();
        });
      });
    },
    handleDelete(loadCase) {
      this.result = this.$get('/test/plan/load/case/delete/' + loadCase.id, () => {
        this.$success(this.$t('test_track.cancel_relevance_success'));
        this.$emit('refresh');
        this.initTable();
      });
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTable();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTable();
    },
    getReport(data) {
      const {loadReportId} = data;
      this.reportId = loadReportId;
      this.loading = data.id;
      this.$post('/test/plan/load/case/report/exist', {
        testPlanLoadCaseId: data.id,
        reportId: loadReportId
      }, response => {
        let exist = response.data;
        this.loading = "";
        if (exist) {
          this.$refs.loadCaseReport.drawer = true;
        } else {
          this.$warning(this.$t('test_track.plan.load_case.report_not_found'));
        }
      })
    },
    cancelRefresh() {
      if (this.refreshScheduler) {
        clearInterval(this.refreshScheduler);
      }
    }
  },
  beforeDestroy() {
    console.log('beforeDestroy')
    this.cancelRefresh();
  },
}
</script>

<style scoped>
/deep/ .run-button {
  background-color: #409EFF;
  border-color: #409EFF;
}
</style>
