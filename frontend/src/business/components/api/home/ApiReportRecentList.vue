<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">{{$t('api_report.title')}}</span>
    </template>
    <el-table border :data="tableData" class="adjust-table table-content" @row-click="link" height="300px">
      <el-table-column prop="name"  :label="$t('commons.name')" width="150" show-overflow-tooltip/>
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
          <ms-api-report-status :row="row"/>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
  import MsApiReportStatus from "../report/ApiReportStatus";
  import ReportTriggerModeItem from "../../common/tableItem/ReportTriggerModeItem";

  export default {
    name: "MsApiReportRecentList",

    components: {ReportTriggerModeItem, MsApiReportStatus},

    data() {
      return {
        result: {},
        tableData: [],
        loading: false
      }
    },

    methods: {
      search() {
        this.result = this.$get("/api/report/recent/5", response => {
          this.tableData = response.data;
        });
      },
      link(row) {
        this.$router.push({
          path: '/api/report/view/' + row.id,
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
