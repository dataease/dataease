<template>
    <div v-if="count && count > 0" class="content">
      {{$t('commons.table.select_tip', [this.count])}}
    </div>
</template>

<script>
    export default {
      name: "TableSelectCountBar",
      props: ['count']
    }
</script>

<style scoped>

  .content {
    text-align: center;
    height: 20px;
    line-height: 20px;
    margin-top: 10px;
  }

</style>
