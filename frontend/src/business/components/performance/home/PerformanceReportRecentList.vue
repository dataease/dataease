<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">{{$t('api_report.title')}}</span>
    </template>
    <el-table border :data="tableData" class="adjust-table table-content" @row-click="link" height="300px">
      <el-table-column prop="name" :label="$t('commons.name')" width="150" show-overflow-tooltip/>
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
      <el-table-column prop="triggerMode" width="150" :label="'触发方式'">
        <template v-slot:default="scope">
          <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
        </template>
      </el-table-column>
      <el-table-column  prop="status" :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <ms-performance-report-status :row="row"/>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>

  import MsPerformanceReportStatus from "../report/PerformanceReportStatus";
  import ReportTriggerModeItem from "../../common/tableItem/ReportTriggerModeItem";

  export default {
    name: "MsPerformanceReportRecentList",
    components: {ReportTriggerModeItem, MsPerformanceReportStatus},
    data() {
      return {
        result: {},
        tableData: []
      }
    },

    methods: {
      search() {
        this.result = this.$get("/performance/report/recent/5", response => {
          this.tableData = response.data;
        });
      },
      link(row) {
        this.$router.push({
          path: '/performance/report/view/' + row.id,
        })
      }
    },

    created() {
      this.search();
    },
    activated() {
      this.search();
    }
  }
</script>

<style scoped>

  .el-table {
    cursor:pointer;
  }

</style>
