<template>
  <el-card class="scenario-results">
    <ms-scenario-result v-for="(scenario, index) in scenarios" :key="index" :scenario="scenario" :indexNumber="index"
                        v-on:requestResult="requestResult"/>
  </el-card>
</template>

<script>
  import MsScenarioResult from "./ScenarioResult";

  export default {
    name: "MsScenarioResults",

    components: {MsScenarioResult},

    props: {
      scenarios: Array
    },
    methods: {
      requestResult(requestResult) {
        this.$emit("requestResult", requestResult);
      }
    }
  }
</script>

<style scoped>
  .scenario-header {
    border: 1px solid #EBEEF5;
    background-color: #F9FCFF;
    border-left: 0;
    border-right: 0;
    padding: 5px 0;
  }
</style>
