<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search"
                           :title="$t('api_report.title')"
                           :show-create="false"/>
        </template>
        <el-table ref="reportListTable" border :data="tableData" class="adjust-table table-content" @sort-change="sort"
                  @select-all="handleSelectAll"
                  @select="handleSelect"
                  @filter-change="filter" @row-click="handleView">
          <el-table-column
            type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <el-dropdown slot="header" style="width: 14px">
              <span class="el-dropdown-link" style="width: 14px">
                <i class="el-icon-arrow-down el-icon--right" style="margin-left: 0px"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native.stop="isSelectDataAll(true)">
                  {{ $t('api_test.batch_menus.select_all_data', [total]) }}
                </el-dropdown-item>
                <el-dropdown-item @click.native.stop="isSelectDataAll(false)">
                  {{ $t('api_test.batch_menus.select_show_data', [tableData.length]) }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <template v-slot:default="scope">
              <show-more-btn v-tester :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.name')" width="200" show-overflow-tooltip prop="name">
          </el-table-column>

          <el-table-column prop="scenarioName" :label="$t('api_test.automation.scenario_name')" width="150"
                           show-overflow-tooltip/>
          <el-table-column prop="userName" :label="$t('api_test.creator')" width="150" show-overflow-tooltip/>
          <el-table-column prop="createTime" width="250" :label="$t('commons.create_time')" sortable>
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerMode" width="150" :label="$t('commons.trigger_mode.name')"
                           column-key="triggerMode" :filters="triggerFilters">
            <template v-slot:default="scope">
              <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('commons.status')"
                           column-key="status"
                           :filters="statusFilters">
            <template v-slot:default="{row}">
              <ms-api-report-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column width="150" :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operator-button :tip="$t('api_report.detail')" icon="el-icon-s-data"
                                        @exec="handleView(scope.row)" type="primary"/>
              <ms-table-operator-button :is-tester-permission="true" :tip="$t('api_report.delete')"
                                        icon="el-icon-delete" @exec="handleDelete(scope.row)" type="danger"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>

    <el-drawer :visible.sync="debugVisible" :destroy-on-close="true" direction="ltr" :withHeader="false"
               :title="$t('test_track.plan_view.test_result')" :modal="false" size="90%">
      <ms-api-report-detail :report-id="reportId" :currentProjectId="currentProjectId" :info-db="true"
                            @refresh="search"/>
    </el-drawer>
  </ms-container>
</template>

<script>
import MsTablePagination from "../../../common/pagination/TablePagination";
import MsTableHeader from "../../../common/components/MsTableHeader";
import MsContainer from "../../../common/components/MsContainer";
import MsMainContainer from "../../../common/components/MsMainContainer";
import MsApiReportStatus from "./ApiReportStatus";
import {getCurrentProjectID} from "@/common/js/utils";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import ReportTriggerModeItem from "../../../common/tableItem/ReportTriggerModeItem";
import {REPORT_CONFIGS} from "../../../common/components/search/search-components";
import {ApiEvent, LIST_CHANGE} from "@/business/components/common/head/ListEvent";
import ShowMoreBtn from "../../../track/case/components/ShowMoreBtn";
import MsApiReportDetail from "./ApiReportDetail";
import {_filter, _sort} from "@/common/js/tableUtils";

export default {
  components: {
    ReportTriggerModeItem,
    MsTableOperatorButton,
    MsApiReportStatus, MsMainContainer, MsContainer, MsTableHeader, MsTablePagination, ShowMoreBtn, MsApiReportDetail
  },
  data() {
    return {
      result: {},
      reportId: "",
      debugVisible: false,
      condition: {
        components: REPORT_CONFIGS
      },
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      currentProjectId: "",
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'},
        {text: 'Success', value: 'Success'},
      ],
      triggerFilters: [
        {text: this.$t('commons.trigger_mode.manual'), value: 'MANUAL'},
        {text: this.$t('commons.trigger_mode.schedule'), value: 'SCHEDULE'},
        {text: this.$t('commons.trigger_mode.api'), value: 'API'}
      ],
      buttons: [
        {
          name: this.$t('api_report.batch_delete'), handleClick: this.handleBatchDelete
        }
      ],
      selectRows: new Set(),
      selectAll: false,
      unSelection: [],
      selectDataCounts: 0,
    }
  },

  watch: {
    '$route': 'init',
  },

  methods: {
    search() {
      if (this.testId !== 'all') {
        this.condition.testId = this.testId;
      }
      this.condition.projectId = getCurrentProjectID();
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      let url = "/api/scenario/report/list/" + this.currentPage + "/" + this.pageSize;
      this.result = this.$post(url, this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.selectRows.clear();
        this.unSelection = data.listObject.map(s => s.id);
      });
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleView(report) {
      this.reportId = report.id;
      this.currentProjectId = report.projectId;
      this.debugVisible = true;
    },
    handleDelete(report) {
      this.$alert(this.$t('api_report.delete_confirm') + report.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.result = this.$post("/api/scenario/report/delete", {id: report.id}, () => {
              this.$success(this.$t('commons.delete_success'));
              this.search();
              // 发送广播，刷新 head 上的最新列表
              ApiEvent.$emit(LIST_CHANGE);
            });
          }
        }
      });
    },
    init() {
      this.testId = this.$route.params.testId;
      this.search();
    },
    sort(column) {
      _sort(column, this.condition);
      this.init();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.init();
    },
    handleSelect(selection, row) {
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
      this.selectRowsCount(this.selectRows)
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
      this.selectRowsCount(this.selectRows)
    },
    handleBatchDelete() {
      this.$alert(this.$t('api_report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            let sendParam = {};
            sendParam.ids = ids;
            sendParam.selectAllDate = this.isSelectAllDate;
            sendParam.unSelectIds = this.unSelection;
            sendParam = Object.assign(sendParam, this.condition);
            this.$post('/api/scenario/report/batch/delete', sendParam, () => {
              this.selectRows.clear();
              this.$success(this.$t('commons.delete_success'));
              this.search();
              // 发送广播，刷新 head 上的最新列表
              ApiEvent.$emit(LIST_CHANGE);
            });
          }
        }
      });
    },
    selectRowsCount(selection) {
      let selectedIDs = this.getIds(selection);
      let allIDs = this.tableData.map(s => s.id);
      this.unSelection = allIDs.filter(function (val) {
        return selectedIDs.indexOf(val) === -1
      });
      if (this.isSelectAllDate) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = selection.size;
      }
    },
    isSelectDataAll(dataType) {
      this.isSelectAllDate = dataType;
      this.selectRowsCount(this.selectRows)
      //如果已经全选，不需要再操作了
      if (this.selectRows.size != this.tableData.length) {
        this.$refs.reportListTable.toggleAllSelection(true);
      }
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    }
  },

  created() {
    this.init();
  }
}
</script>

<style scoped>
.table-content {
  width: 100%;
}
</style>
