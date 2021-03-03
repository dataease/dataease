<template>
  <div class="report-export">
    <ms-report-title :title="title" :type="type"/>
    <slot></slot>
  </div>
</template>

<script>
    import MsReportTitle from "./MsReportTitle";
    export default {
      name: "MsReportExportTemplate",
      components: {MsReportTitle},
      props: {title: String, type: String},
    }
</script>

<style scoped>

  .report-export {
    padding: 30px;
  }

  .report-title {
    margin-bottom: 30px;
  }

  .report-export {
    background: white;
  }

</style>
