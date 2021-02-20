<template>
  <el-row type="flex" justify="start" :gutter="20" class="status-button">
    <el-col>
      <el-button :disabled="isReadOnly || isFailure" type="success" round size="mini"
                 :icon="status == 'Pass' ? 'el-icon-check' : ''"
                 @click="setStatus('Pass')"> {{$t('test_track.plan_view.pass')}}
      </el-button>
    </el-col>
    <el-col>
      <el-button :disabled="isReadOnly" type="danger" round size="mini"
                 :icon="status == 'Failure' ? 'el-icon-check' : ''"
                 @click="setStatus('Failure')"> {{$t('test_track.plan_view.failure')}}
      </el-button>
    </el-col>
    <el-col>
      <el-button :disabled="isReadOnly" type="warning" round size="mini"
                 :icon="status == 'Blocking' ? 'el-icon-check' : ''"
                 @click="setStatus('Blocking')"> {{$t('test_track.plan_view.blocking')}}
      </el-button>
    </el-col>
    <el-col>
      <el-button :disabled="isReadOnly" type="info" round size="mini"
                 :icon="status == 'Skip' ? 'el-icon-check' : ''"
                 @click="setStatus('Skip')"> {{$t('test_track.plan_view.skip')}}
      </el-button>
    </el-col>
  </el-row>
</template>

<script>
  export default {
    name: "TestPlanTestCaseStatusButton",
    props: {
      status: {
        type: String
      },
      isReadOnly: {
        type: Boolean,
        default: false
      },
      isFailure: {
        type: Boolean,
        default: false
      }
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
