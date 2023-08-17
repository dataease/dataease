<script lang="ts" setup>
import { computed } from 'vue'
import Header from './components/Header.vue'
import HeaderSystem from './components/HeaderSystem.vue'
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
    <HeaderSystem v-if="systemMenu"></HeaderSystem>
    <Header v-else></Header>
    <el-container class="layout-container">
      <Sidebar v-if="systemMenu" class="layout-sidebar">
        <Menu style="height: 100%"></Menu>
      </Sidebar>
      <Main class="layout-main" :class="{ 'with-sider': systemMenu }"></Main>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.common-layout {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #fff;
  color: #1f2329;

  .layout-container {
    .layout-sidebar {
      height: calc(100vh - 56px);
    }

    .layout-main {
      flex: 1;
      background-color: var(--MainBG, #f5f6f7);
      padding: 0;
    }

    .with-sider {
      padding: 10px 24px 24px 24px;
    }
  }
}
</style>
