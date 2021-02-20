<template>
  <div>
    <span v-if="!reportId">{{$t('commons.not_performed_yet')}}</span>
    <ms-api-report-view-detail v-if="reportId" :report-id="reportId"/>
  </div>
</template>

<script>

import MsApiReportViewDetail from "../../../../../api/report/ApiReportViewDetail";

export default {
    name: "ApiTestResult",
    components: {MsApiReportViewDetail},
    props: ['reportId'],
}
</script>
<style>
</style>

<style scoped>
</style>
