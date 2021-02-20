<template>
  <div id="body">
    <router-view name="sidebar" class="sidebar"/>
    <router-view name="content" class="content"/>
  </div>
</template>

<script>
  export default {
    name: "MsView"
  }
</script>

<style scoped>
  #body {
    width: 100%;
    height: calc(100vh - 40px);
    /*background-color: #F5F5F5;*/
  }

  .sidebar {
    position: relative;
    width: 330px;
    height: 100%;
    max-width: 750px;
    min-width: 250px;
  }

  .content {
    width: 100%;
    height: 100%;
  }
</style>
