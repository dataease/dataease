<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane :label="$t('organization.defect_manage')" name="defect">
        <bug-management/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>

  import BugManagement from "./BugManagement";

  export default {
    name: "ServiceIntegration",
    components: {
      BugManagement
    },
    data() {
      return {
        activeName: 'defect'
      }
    }
  }
</script>

<style scoped>

</style>
