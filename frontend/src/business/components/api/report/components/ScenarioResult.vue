<template>
  <div class="scenario-result">
    <div @click="active">
      <el-row :gutter="10" type="flex" align="middle" class="info">
        <el-col :span="16">
          <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
          {{scenario.name}}
        </el-col>
      </el-row>
    </div>
    <el-collapse-transition>
      <div v-show="isActive">
        <ms-request-result v-for="(request, index) in scenario.requestResults" :key="index" :request="request"
                           v-on:requestResult="requestResult"
                           :scenarioName="scenario.name"/>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script>
  import MsRequestResult from "./RequestResult";

  export default {
    name: "MsScenarioResult",

    components: {MsRequestResult},

    props: {
      scenario: Object
    },

    data() {
      return {
        isActive: false
      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      },
      requestResult(requestResult) {
        this.$emit("requestResult", requestResult);
      }
    },

    computed: {
      assertion() {
        return this.scenario.passAssertions + " / " + this.scenario.totalAssertions;
      },
      success() {
        return this.scenario.error === 0;
      }
    }
  }
</script>

<style scoped>
  .scenario-result {
    width: 100%;
    padding: 2px 0;
  }

  .scenario-result + .scenario-result {
    border-top: 1px solid #DCDFE6;
  }

  .scenario-result .info {
    height: 40px;
    cursor: pointer;
  }

  .scenario-result .icon {
    padding: 5px;
  }

  .scenario-result .icon.is-active {
    transform: rotate(90deg);
  }

</style>
