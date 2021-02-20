<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search"
                           :title="$t('api_report.title')"
                           :show-create="false"/>
        </template>
        <el-table border :data="tableData" class="adjust-table table-content" @sort-change="sort"
                  @select-all="handleSelectAll"
                  @select="handleSelect"
                  @filter-change="filter" @row-click="handleView">
          <el-table-column
            type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <template v-slot:default="scope">
              <show-more-btn v-tester :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.name')" width="200" show-overflow-tooltip prop="name">
          </el-table-column>
          <el-table-column prop="testName" :label="$t('api_report.test_name')" width="200" show-overflow-tooltip/>
          <el-table-column prop="projectName" :label="$t('load_test.project_name')" width="150" show-overflow-tooltip/>
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
  </ms-container>
</template>

<script>
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsApiReportStatus from "./ApiReportStatus";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import ReportTriggerModeItem from "../../common/tableItem/ReportTriggerModeItem";
import {REPORT_CONFIGS} from "../../common/components/search/search-components";
import {ApiEvent, LIST_CHANGE} from "@/business/components/common/head/ListEvent";
import ShowMoreBtn from "../../track/case/components/ShowMoreBtn";
import {_filter, _sort} from "@/common/js/tableUtils";

export default {
  components: {
    ReportTriggerModeItem,
    MsTableOperatorButton,
    MsApiReportStatus, MsMainContainer, MsContainer, MsTableHeader, MsTablePagination, ShowMoreBtn
  },
  data() {
    return {
      result: {},
      condition: {
        components: REPORT_CONFIGS
      },
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
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

      let url = "/api/report/list/" + this.currentPage + "/" + this.pageSize;
      this.result = this.$post(url, this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.selectRows.clear();
      });
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleView(report) {
      this.$router.push({
        path: '/api/report/view/' + report.id,
      })
    },
    handleDelete(report) {
      this.$alert(this.$t('api_report.delete_confirm') + report.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.result = this.$post("/api/report/delete", {id: report.id}, () => {
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
    handleBatchDelete() {
      this.$alert(this.$t('api_report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            this.$post('/api/report/batch/delete', {ids: ids}, () => {
              this.selectRows.clear();
              this.$success(this.$t('commons.delete_success'));
              this.search();
              // 发送广播，刷新 head 上的最新列表
              ApiEvent.$emit(LIST_CHANGE);
            });
          }
        }
      });
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
