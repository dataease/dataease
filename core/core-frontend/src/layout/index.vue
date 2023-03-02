<script lang="ts" setup>
import Header from './components/Header.vue'
import Sidebar from './components/Sidebar.vue'
import Main from './components/Main.vue'
import { ElContainer } from 'element-plus-secondary'
</script>

<template>
  <div class="common-layout">
    <Header></Header>
    <el-container class="layout-container">
      <Sidebar class="layout-sidebar"></Sidebar>
      <Main class="layout-main"></Main>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.common-layout {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;

  .layout-container {
    // flex: 1;
    // width: 100%;
    // display: flex;

    .layout-sidebar {
      //   height: 100%;
    }

    .layout-main {
      flex: 1;
      padding: 24px;

      //   height: 100%;
    }
  }
}
</style>
