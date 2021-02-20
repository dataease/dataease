<template>
  <el-dialog :title="$t('api_report.title')" :visible.sync="reportVisible">
    <el-table border class="adjust-table" :data="tableData" v-loading="result.loading">
      <el-table-column :label="$t('commons.name')" width="150" show-overflow-tooltip>
        <template v-slot:default="scope">
          <el-link type="info" @click="link(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column width="250" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column width="250" :label="$t('commons.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <ms-api-report-status :row="row"/>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </el-dialog>
</template>

<script>
import MsApiReportStatus from "../report/ApiReportStatus";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";

export default {
  name: "MsApiReportDialog",

  components: {MsApiReportStatus, MsTablePagination},

  props: ["testId"],

  data() {
    return {
      reportVisible: false,
      result: {},
      tableData: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
    }
  },

  methods: {
    open() {
      this.reportVisible = true;

      this.search();
    },
    link(row) {
      this.reportVisible = false;

      this.$router.push({
        path: '/api/report/view/' + row.id,
      })
    },
    search() {
      let url = "/api/report/list/" + this.testId + "/" + this.currentPage + "/" + this.pageSize;
      this.result = this.$get(url, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
  },
}
</script>

<style scoped>
</style>
