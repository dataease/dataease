<template>
  <ms-api-report-view-detail :report-id="reportId"/>
</template>

<script>

import MsApiReportViewDetail from "./ApiReportViewDetail";

export default {
  name: "MsApiReportView",
  components: {
    MsApiReportViewDetail,
  },
  computed: {
    reportId: function () {
      return this.$route.params.reportId;
    },
  }
}
</script>
<style>
</style>
<style scoped>

</style>
