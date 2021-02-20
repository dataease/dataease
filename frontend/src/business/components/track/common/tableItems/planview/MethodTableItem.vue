<template>
  <div>
    <span v-if="value == 'manual'">{{$t('test_track.case.manual')}}</span>
    <span v-if="value == 'auto'">{{$t('test_track.case.auto')}}</span>
  </div>
</template>

<script>
    export default {
      name: "MethodTableItem",
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
