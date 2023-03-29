<script lang="ts" setup>
import { computed } from 'vue'
import Header from './components/Header.vue'
import Sidebar from './components/Sidebar.vue'
import Menu from './components/Menu.vue'
import Main from './components/Main.vue'
import { ElContainer } from 'element-plus-secondary'
import { useRoute } from 'vue-router'
const route = useRoute()
const systemMenu = computed(() => route.path.includes('system'))
</script>

<template>
  <div class="common-layout">
    <Header></Header>
    <el-container class="layout-container">
      <Sidebar v-if="systemMenu" class="layout-sidebar">
        <Menu style="height: 100%"></Menu>
      </Sidebar>
      <Main class="layout-main"></Main>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.common-layout {
  width: 100vw;
  height: 100vh;

  .layout-container {
    .layout-sidebar {
      height: calc(100vh - 60px);
    }

    .layout-main {
      flex: 1;
      padding: 24px;
      background-color: var(--MainBG, #f5f6f7);
    }
  }
}
</style>
