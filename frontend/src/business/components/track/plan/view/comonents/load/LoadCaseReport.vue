<template>
  <el-drawer
    class="load-case-report-drawer"
    :visible.sync="drawer"
    direction="ltr"
    @close="handleClose"
    size="80%">
    <load-case-report-view :report-id="reportId" ref="loadCaseReportView"/>
  </el-drawer>
</template>

<script>
import LoadCaseReportView from "@/business/components/track/plan/view/comonents/load/LoadCaseReportView";

export default {
  name: "LoadCaseReport",
  components: {LoadCaseReportView},
  data() {
    return {
      drawer: false,
    }
  },
  props: {
    reportId: String
  },
  methods: {
    handleClose() {
      this.drawer = false;
      this.$emit('refresh');
    }
  },
}
</script>

<style scoped>
.load-case-report-drawer >>> .el-drawer__header {
  margin-bottom: 0;
}
</style>
