<template>
    <span>
      <ms-tag v-if="value == 'Prepare'" type="info" :content="$t('test_track.plan.plan_status_prepare')"/>
      <ms-tag v-if="value == 'Underway'" type="primary" :content="$t('test_track.plan.plan_status_running')"/>
      <ms-tag v-if="value == 'Pass'" type="success" :content="$t('test_track.plan_view.pass')"/>
      <ms-tag v-if="value == 'UnPass'" type="danger" content="未通过"/>
      <ms-tag v-if="value == 'Failure'" type="danger" :content="$t('test_track.plan_view.failure')"/>
      <ms-tag v-if="value == 'Blocking'" type="warning" :content="$t('test_track.plan_view.blocking')"/>
      <ms-tag v-if="value == 'Skip'" type="info" :content="$t('test_track.plan_view.skip')"/>
    </span>
</template>

<script>
    import MsTag from "../../../../common/components/MsTag";
    export default {
      name: "StatusTableItem",
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
