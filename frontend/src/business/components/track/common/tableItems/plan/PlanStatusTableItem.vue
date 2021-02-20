<template>
    <div>
      <ms-tag v-if="value == 'Prepare'" type="info" :content="$t('test_track.plan.plan_status_prepare')"/>
      <ms-tag v-if="value == 'Underway'" type="primary" :content="$t('test_track.plan.plan_status_running')"/>
      <ms-tag v-if="value == 'Completed'" type="success" :content="$t('test_track.plan.plan_status_completed')"/>
      <ms-tag v-if="value === 'Trash'" type="danger" effect="plain" :content="$t('test_track.plan.plan_status_trash')"/>
    </div>
</template>

<script>
import MsTag from "../../../../common/components/MsTag";

export default {
      name: "PlanStatusTableItem",
      components: {MsTag},
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
