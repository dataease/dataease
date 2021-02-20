<template>
  <el-menu-item :index="this.index">
    <el-button type="text" class="create-button">{{this.title}}</el-button>
  </el-menu-item>
</template>

<script>
  export default {
    name: "MsCreateButton",
    props: {
      index: String,
      title: String,
      click: Function
    }
  }
</script>

<style scoped>
  .create-button {
    padding-left: 18px;
  }
</style>
