<template>
  <el-dropdown @command="handleCommand" class="scenario-ext-btn">
    <el-link type="primary" :underline="false">
      <el-icon class="el-icon-more"></el-icon>
    </el-link>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="ref">{{ $t('api_test.automation.view_ref') }}</el-dropdown-item>
      <el-dropdown-item command="create_performance">{{ $t('api_test.create_performance_test') }}</el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>

export default {
  name: "MsApiCaseTableExtendBtns",
  components: {},
  props: {
    row: Object,
  },
  data() {
    return {
      planVisible: false,
    }
  },

  methods: {
    handleCommand(cmd) {
      if (this.row.id) {
        switch (cmd) {
          case  "ref":
            this.$emit("showCaseRef", this.row);
            break;
          case "create_performance":
            this.$emit("showEnvironment", this.row);
            break;
        }
      } else {
        this.$warning(this.$t('api_test.automation.save_case_info'))
      }
    },
    createPerformance(row, environment) {
      this.$emit("createPerformance", row, environment);
    }
  }
}
</script>

<style scoped>
.scenario-ext-btn {
  margin-left: 10px;
}
</style>
