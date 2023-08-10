<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { formatRoute } from '@/router/establish'
import HeaderMenuItem from './HeaderMenuItem.vue'
import { Icon } from '@/components/icon-custom'
import { ElHeader, ElMenu } from 'element-plus-secondary'
import SystemCfg from './SystemCfg.vue'
import { useRouter, useRoute } from 'vue-router'
import OrgSwicther from '@/layout/components/OrgSwitcher.vue'
import LangSelector from '@/layout/components/LangSelector.vue'
import TopDoc from '@/layout/components/TopDoc.vue'
import AccountOperator from '@/layout/components/AccountOperator.vue'
import { isDesktop } from '@/utils/ModelUtil'
const { push } = useRouter()
const route = useRoute()

const desktop = isDesktop()
const activeIndex = computed(() => {
  if (route.path.includes('system')) {
    return '/system/user'
  }
  return route.path
})
const permissionStore = usePermissionStore()

const routers: any[] = formatRoute(permissionStore.getRoutersNotHidden as AppCustomRouteRecordRaw[])
const showSystem = ref(false)
const handleSelect = (index: string) => {
  // 自定义事件
  if (isExternal(index)) {
    window.open(index, '_blank')
  } else {
    push(index)
  }
}
const initShowSystem = () => {
  showSystem.value = permissionStore.getRouters.some(route => route.path === '/system')
}
onMounted(() => {
  initShowSystem()
})
</script>

<template>
  <el-header class="header-flex">
    <Icon className="logo" name="logo"></Icon>
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
    >
      <HeaderMenuItem v-for="menu in routers" :key="menu.path" :menu="menu"></HeaderMenuItem>
    </el-menu>
    <div class="operate-setting" v-if="!desktop">
      <OrgSwicther />
      <LangSelector />
      <TopDoc />
      <SystemCfg v-if="showSystem" />
      <AccountOperator />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
.header-flex {
  display: flex;
  align-items: center;
  height: 56px;
  background-color: #050e21;
  padding: 0 24px;
  .operate-setting {
    margin-left: auto;
    display: flex;
    align-items: center;
    &:focus {
      outline: none;
    }
  }

  .ed-menu {
    background-color: #050e21;
    height: 56px;
  }

  .ed-menu--horizontal {
    border: none;
    .ed-menu-item,
    :deep(.ed-sub-menu__title) {
      color: rgba(255, 255, 255, 0.8);
      line-height: 50px;
      border-bottom: none;

      &.is-active {
        border-bottom: none;
        color: #ffffff !important;
        background-color: #245bdb;
      }
    }

    > .is-active {
      :deep(.ed-sub-menu__title) {
        color: #ffffff !important;
        background-color: #245bdb;
      }
    }

    .ed-menu-item:not(.is-disabled):hover,
    :deep(.ed-sub-menu__title):not(.is-disabled):hover {
      color: #ffffff;
      background-color: #245bdb;
    }
  }
}

.logo {
  width: 134px;
  height: 34px;
  margin-right: 48px;
}
</style>

<style lang="less">
.header-flex {
  .operate-setting {
    .ed-icon {
      cursor: pointer;
      color: rgba(255, 255, 255, 0.8);
      margin: 0 11px;
      font-size: 18px;
    }
  }
}
</style>
