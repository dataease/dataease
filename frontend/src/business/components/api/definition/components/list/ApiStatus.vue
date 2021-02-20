<template>
  <span>
    <ms-tag v-if="value === 'Prepare'" type="info" effect="plain" :content="$t('test_track.plan.plan_status_prepare')"/>
    <ms-tag v-if="value === 'Underway'" type="warning" effect="plain" :content="$t('test_track.plan.plan_status_running')"/>
    <ms-tag v-if="value === 'Completed'" type="success" effect="plain" :content="$t('test_track.plan.plan_status_completed')"/>
    <ms-tag v-if="value === 'Trash'" type="danger" effect="plain" :content="$t('test_track.plan.plan_status_trash')"/>
    </span>
</template>

<script>
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "ApiStatus",
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
