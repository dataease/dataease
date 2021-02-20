<template>
  <el-main class="ms-main-container">
    <slot></slot>
  </el-main>
</template>

<script>
  export default {
    name: "MsMainContainer"
  }
</script>

<style scoped>

  .ms-main-container {
    padding: 15px;
    height: calc(100vh - 80px);
  }

</style>
