<template>
  <ms-container>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <ms-api-test-recent-list/>
        </el-col>
        <el-col :span="12">
          <ms-api-report-recent-list/>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <ms-test-heatmap :values="values"/>
        </el-col>
        <el-col :span="12">
          <ms-schedule-list :group="'API_TEST'"/>
        </el-col>
      </el-row>
    </ms-main-container>
  </ms-container>

</template>

<script>
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsApiTestRecentList from "./ApiTestRecentList";
  import MsApiReportRecentList from "./ApiReportRecentList";
  import MsTestHeatmap from "../../common/components/MsTestHeatmap";
  import MsScheduleList from "./ScheduleList";

  export default {
    name: "ApiTestHome",

    components: {
      MsScheduleList,
      MsTestHeatmap, MsApiReportRecentList, MsApiTestRecentList, MsMainContainer, MsContainer
    },

    data() {
      return {
        values: [],
        result: {},
      }
    },
    activated() {
      this.getValues();
    },
    mounted() {
      this.getValues();
    },
    methods: {
      getValues() {
        this.result = this.$get('/api/report/dashboard/tests', response => {
          this.values = response.data;
        });
      }
    }
  }
</script>

<style scoped>
  .el-row {
    margin-bottom: 20px;
  }

  .el-row:last-child {
    margin-bottom: 0;
  }

</style>
