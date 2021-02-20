<template>
  <ms-report-export-template :title="title" :type="$t('report.test_plan_report')">
    <div v-for="(item, index) in previews" :key="item.id">
      <template-component :isReportView="true" :metric="metric" :preview="item" :index="index" ref="templateComponent"/>
    </div>
  </ms-report-export-template>
</template>

<script>
    import MsReportExportTemplate from "../../../../common/components/report/MsReportExportTemplate";
    import TemplateComponent from "./report/TemplateComponent/TemplateComponent";
    import MsReportTitle from "../../../../common/components/report/MsReportTitle";
    export default {
      name: "MsTestCaseReportExport",
      components: {MsReportTitle, TemplateComponent, MsReportExportTemplate},
      props: ['previews', 'title', 'metric']
    }
</script>

<style scoped>

  .report-export >>> .template-component {
    width: 99%;
    margin-top: 30px;
    margin-bottom: 30px;
  }

  .report-export >>> .report-title {
    margin-bottom: 50px;
  }


</style>
