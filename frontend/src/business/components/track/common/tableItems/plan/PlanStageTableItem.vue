<template>
    <div>
      <span v-if="stage == 'smoke'">{{$t('test_track.plan.smoke_test')}}</span>
      <span v-if="stage == 'functional'">{{$t('test_track.plan.functional_test')}}</span>
      <span v-if="stage == 'integration'">{{$t('test_track.plan.integration_testing')}}</span>
      <span v-if="stage == 'system'">{{$t('test_track.plan.system_test')}}</span>
      <span v-if="stage == 'regression'">{{$t('test_track.plan.regression_test')}}</span>
      <span v-if="stage == 'version'">{{$t('test_track.plan.version_validation')}}</span>
    </div>
</template>

<script>
    export default {
      name: "PlanStageTableItem",
      components: {},
      props: {
        stage: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
