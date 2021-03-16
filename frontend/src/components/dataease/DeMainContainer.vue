<template>
  <el-main class="ms-main-container">
    <slot />
  </el-main>
</template>

<script>
export default {
  name: 'DeMainContainer'
}
</script>

<style scoped>

  .ms-main-container {
    padding: 15px;
    height: calc(100vh - 80px);
  }

</style>
