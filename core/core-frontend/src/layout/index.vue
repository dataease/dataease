<script lang="ts" setup>
import { computed, ref } from 'vue'
import Header from './components/Header.vue'
import HeaderSystem from './components/HeaderSystem.vue'
import Sidebar from './components/Sidebar.vue'
import Menu from './components/Menu.vue'
import Main from './components/Main.vue'
import CollapseBar from './components/CollapseBar.vue'
import { ElContainer } from 'element-plus-secondary'
import { useRoute } from 'vue-router'
import { XpackComponent } from '@/components/plugin'
const route = useRoute()
const systemMenu = computed(() => route.path.includes('system'))
const settingMenu = computed(() => route.path.includes('sys-setting'))
const marketMenu = computed(() => route.path.includes('template-market'))
const toolboxMenu = computed(() => route.path.includes('toolbox'))
const isCollapse = ref(false)
const setCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <div class="common-layout">
    <HeaderSystem
      v-if="settingMenu || marketMenu || toolboxMenu"
      :title="toolboxMenu ? '工具箱' : marketMenu ? '模板中心' : ''"
    />
    <Header v-else></Header>
    <el-container class="layout-container">
      <template v-if="systemMenu || settingMenu || toolboxMenu">
        <Sidebar v-if="!isCollapse" class="layout-sidebar">
          <div @click="setCollapse" v-if="systemMenu && !isCollapse" class="org-config-center">
            组织管理中心
          </div>
          <Menu :style="{ height: systemMenu ? 'calc(100% - 48px)' : '100%' }"></Menu>
        </Sidebar>
        <el-aside class="layout-sidebar layout-sidebar-collapse" v-else>
          <Menu
            :collapse="isCollapse"
            :style="{ height: systemMenu ? 'calc(100% - 48px)' : '100%' }"
          ></Menu>
        </el-aside>
        <CollapseBar @setCollapse="setCollapse" :isCollapse="isCollapse"></CollapseBar>
      </template>

      <Main
        class="layout-main"
        :class="{ 'with-sider': systemMenu || settingMenu || toolboxMenu }"
      ></Main>
    </el-container>
  </div>
  <XpackComponent jsname="L2NvbXBvbmVudC9sb2dpbi9Qd2RJbnZhbGlkVGlwcw==" />
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
      position: relative;
      &::after {
        content: '';
        width: 100%;
        height: 1px;
        background: #1f232926;
        position: absolute;
        bottom: 48px;
        left: 0;
      }
    }

    .layout-sidebar-collapse {
      width: 64px;
    }

    .org-config-center {
      height: 48px;
      padding-left: 24px;
      display: flex;
      align-items: center;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      color: #8f959e;
      border-bottom: 1px solid #1f232926;
      position: sticky;
      top: 0;
      left: 0;
      background: #fff;
      z-index: 10;
    }

    .layout-main {
      flex: 1;
      background-color: var(--MainBG, #f5f6f7);
      padding: 0;
    }

    .with-sider {
      padding: 16px 24px 24px 24px;
    }
    .with-sider:has(.appearance-foot) {
      padding: 16px 24px 0px 24px !important;
    }
  }
}
</style>
