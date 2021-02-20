<template>
  <div class="item">
    <div class="item-title">
      {{title}}
    </div>
    <div>
      <slot></slot>
    </div>
  </div>
</template>

<script>
    export default {
      name: "ApiReportReqestHeaderItem",
      props: {title: String}
    }
</script>

<style scoped>

  .item {
    width: 120px;
    height: 50px;
    display: inline-block;
  }

  .item-title {
    margin-bottom: 20px;
  }

</style>
