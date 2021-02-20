<template>

  <div>
    <span v-if="value == 'functional'">{{$t('commons.functional')}}</span>
    <span v-if="value == 'performance'">{{$t('commons.performance')}}</span>
    <span v-if="value == 'api'">{{$t('commons.api')}}</span>
  </div>

</template>

<script>
    export default {
      name: "TypeTableItem",
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
