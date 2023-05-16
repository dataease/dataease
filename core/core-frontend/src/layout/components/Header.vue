<script lang="ts" setup>
import { computed } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { resolvePath } from '@/router/establish'
import { Icon } from '@/components/icon-custom'
import { ElHeader, ElMenu, ElMenuItem } from 'element-plus-secondary'
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

const routers = permissionStore.getRoutersNotHidden

console.log('routers', routers)

const handleSelect = (index: string) => {
  // 自定义事件
  if (isExternal(index)) {
    window.open(index, '_blank')
  } else {
    push(index)
  }
}
</script>

<template>
  <el-header class="header-flex">
    <Icon className="logo" name="DataEase"></Icon>
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
    >
      <el-menu-item v-for="item in routers" :key="item.path" :index="resolvePath(item)">
        {{ item.meta.title }}
      </el-menu-item>
    </el-menu>
    <div class="operate-setting" v-if="!desktop">
      <OrgSwicther />
      <LangSelector />
      <TopDoc />
      <AccountOperator />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
.header-flex {
  display: flex;
  align-items: center;
  .operate-setting {
    margin-left: auto;
    float: right;
    height: 56px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    &:focus {
      outline: none;
    }
    div {
      padding: 0 10px;
    }
  }
}
.flex-grow {
  flex-grow: 1;
}
.logo {
  width: 140px;
  height: 45px;
  margin-right: 100px;
}
</style>
