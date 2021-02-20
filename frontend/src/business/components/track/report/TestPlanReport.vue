<template>
  <ms-container>

    <ms-main-container>
      <test-plan-report-list
        ref="testPlanReportList"/>
    </ms-main-container>
  </ms-container>
</template>

<script>

import TestPlanReportList from './components/TestPlanReportList';
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {getCurrentProjectID} from "../../../../common/js/utils";

export default {
  name: "TestPlanReport",
  components: {MsMainContainer, MsContainer, TestPlanReportList},
  data() {
    return {}
  },
  activated() {
    this.refreshTestPlanList();
  },
  mounted() {
    this.refreshTestPlanList();
  },
  watch: {
    '$route'(to, from) {
    }
  },
  methods: {
    refreshTestPlanList() {
      this.$refs.testPlanReportList.condition = {};
      this.$refs.testPlanReportList.initTableData();
    }
  }
}
</script>

<style scoped>

</style>
