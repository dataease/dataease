<template>

  <el-container class="ms-container">
    <slot />
  </el-container>

</template>

<script>
export default {
  name: 'DeContainer'
}
</script>

<style scoped>

  .ms-container >>> span.title {
    font-size: 16px;
    font-weight: 500;
    margin-top: 0;
    text-overflow: ellipsis;
    overflow: hidden;
    word-wrap: break-word;
    white-space: nowrap;
  }

</style>
