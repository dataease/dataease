<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.automation.scenario_ref')" :visible.sync="visible"
             width="45%" :destroy-on-close="true" >
    <span>{{ $t('api_test.automation.scenario_ref') }}：</span>
    <div class="refs" v-loading="scenarioLoading">
      <div v-for="(item, index) in scenarioRefs" :key="index" class="el-button--text">
        <el-link @click="openScenario(item)">
          {{ item.name }}
        </el-link>
      </div>
    </div>

    <span>{{ $t('api_test.automation.plan_ref') }}：</span>
    <div class="refs">
      <div v-for="(item, index) in planRefs" :key="index" class="el-button--text">
        <router-link :to="'/track/plan/view/' + item.id">
          {{ item.name }}
        </router-link>
      </div>
    </div>

    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="visible = false" @keydown.enter.native.prevent>
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>

  export default {
    name: "MsReferenceView",
    components: {},
    data() {
      return {
        visible: false,
        scenarioLoading: false,
        scenarioRefs: [],
        planRefs: []
      }
    },
    methods: {
      getReferenceData(row) {
        this.scenarioLoading = true;
        this.scenarioRefs = [];
        this.$post("/api/automation/getReference/", row, response => {
          this.scenarioRefs = response.data.scenarioList;
          this.planRefs = response.data.testPlanList;
          this.scenarioLoading = false;
        })
      },
      open(row) {
        this.getReferenceData(row);
        this.visible = true
      },
      openScenario(item) {
        this.$emit('openScenario', item);
        this.visible = false;
      }
    }
  }
</script>

<style scoped>
  .refs {
    min-height: 50px;
    max-height: 200px;
    overflow-y: auto;
    font-size: 12px;
    padding-bottom: 10px;
  }

  .el-button--text .el-link.el-link--default {
    font-size: 12px;
    color: #4b1980;
    font-weight: 400;
    text-decoration: underline;
  }
</style>
