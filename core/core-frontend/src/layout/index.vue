<script lang="ts" setup>
import { computed, ref } from 'vue'
import Header from './components/Header.vue'
import HeaderSystem from './components/HeaderSystem.vue'
import Sidebar from './components/Sidebar.vue'
import Menu from './components/Menu.vue'
import Main from './components/Main.vue'
import CollapseBar from './components/CollapseBar.vue'
import { ElContainer } from 'element-plus-secondary'
import { useRoute } from 'vue-router_2'
import { XpackComponent } from '@/components/plugin'
import { useI18n } from '@/hooks/web/useI18n'
const route = useRoute()
const systemMenu = computed(() => route.path.includes('system'))
const settingMenu = computed(() => route.path.includes('sys-setting'))
const marketMenu = computed(() => route.path.includes('template-market'))
const toolboxMenu = computed(() => route.path.includes('toolbox'))
const msgFillMenu = computed(() => route.path.includes('msg-fill'))
const isCollapse = ref(false)
const setCollapse = () => {
  isCollapse.value = !isCollapse.value
}
const { t } = useI18n()
</script>

<template>
  <div class="common-layout">
    <HeaderSystem
      v-if="settingMenu || marketMenu || toolboxMenu || msgFillMenu"
      :title="
        toolboxMenu
          ? t('toolbox.name')
          : marketMenu
          ? t('toolbox.template_center')
          : msgFillMenu
          ? t('v_query.msg_center')
          : ''
      "
    />
    <Header v-else></Header>
    <el-container class="layout-container">
      <template v-if="systemMenu || settingMenu || toolboxMenu || msgFillMenu">
        <Sidebar v-if="!isCollapse" class="layout-sidebar">
          <div
            @click="setCollapse"
            v-if="(systemMenu || msgFillMenu) && !isCollapse"
            class="org-config-center"
          >
            {{ msgFillMenu ? t('v_query.msg_center') : t('toolbox.org_center') }}
          </div>
          <Menu
            :style="{ height: systemMenu || msgFillMenu ? 'calc(100% - 48px)' : '100%' }"
          ></Menu>
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
  min-width: 1000px;
  overflow-x: auto;

  .layout-container {
    .layout-sidebar {
      height: calc(100vh - 106px);
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
