<template>
  <el-row type="flex" justify="start" :gutter="20" class="status-button">
    <el-col>
      <el-button type="info" round size="mini"
                 :icon="status == 'Prepare' ? 'el-icon-check' : ''"
                 @click="setStatus('Prepare')"> {{$t('test_track.plan.plan_status_prepare')}}</el-button>
    </el-col>
    <el-col>
      <el-button type="primary" round size="mini"
                 :icon="status == 'Underway' ? 'el-icon-check' : ''"
                 @click="setStatus('Underway')"> {{$t('test_track.plan.plan_status_running')}}</el-button>
    </el-col>
    <el-col>
      <el-button type="success" round size="mini"
                 :icon="status == 'Completed' ? 'el-icon-check' : ''"
                 @click="setStatus('Completed')"> {{$t('test_track.plan.plan_status_completed')}}</el-button>
    </el-col>
  </el-row>
</template>

<script>
  export default {
    name: "TestPlanStatusButton",
    props: {
      status: {
        type: String
      },
    },
    methods: {
      setStatus(status) {
        this.$emit('statusChange', status);
      }
    }
  }
</script>

<style scoped>
  .el-row {
    width: 50%;
  }

</style>
