<template>
    <span>
      <ms-tag v-if="value === 'Prepare'" type="info" :content="$t('test_track.case.status_prepare')"/>
      <ms-tag v-if="value === 'Pass'" type="success" :content="$t('test_track.case.status_pass')"/>
      <ms-tag v-if="value === 'UnPass'" type="danger" :content="$t('test_track.case.status_un_pass')"/>
    </span>
</template>

<script>
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "ReviewStatus",
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
