<template>
  <ms-report-export-template :title="title" :type="$t('report.load_test_report')">
    <el-card id="testOverview">
      <template v-slot:header >
        <span class="title">{{$t('report.test_overview')}}</span>
      </template>
      <ms-report-test-overview :report="report" ref="testOverview"/>
    </el-card>
    <el-card id="requestStatistics" title="'requestStatistics'">
      <template v-slot:header >
        <span class="title">{{$t('report.test_request_statistics')}}</span>
      </template>
      <ms-report-request-statistics :report="report" ref="requestStatistics"/>
    </el-card>
    <el-card id="errorLog" title="'errorLog'">
      <template v-slot:header >
        <span class="title">{{$t('report.test_error_log')}}</span>
      </template>
      <ms-report-error-log :report="report" ref="errorLog"/>
    </el-card>
  </ms-report-export-template>
</template>

<script>

  import MsReportErrorLog from './components/ErrorLog';
  import MsReportRequestStatistics from './components/RequestStatistics';
  import MsReportTestOverview from './components/TestOverview';

  import {checkoutTestManagerOrTestUser} from "@/common/js/utils";
  import MsPerformanceReportExport from "./PerformanceReportExport";
  import MsReportTitle from "../../common/components/report/MsReportTitle";
  import MsReportExportTemplate from "../../common/components/report/MsReportExportTemplate";


  export default {
    name: "MsPerformanceReportExport",
    components: {
      MsReportExportTemplate,
      MsReportTitle,
      MsReportErrorLog,
      MsReportRequestStatistics,
      MsReportTestOverview,
    },
    props: ['report','title']
  }
</script>

<style scoped>

  .el-card {
    margin-top: 20px;
    margin-bottom: 20px;
    border-style: none;
  }

</style>
