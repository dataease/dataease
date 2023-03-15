<script lang="ts" setup>
import { ref } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { ElHeader, ElMenu, ElMenuItem } from 'element-plus-secondary'
import { Icon } from '@/components/Icon'
import { useI18n } from '@/hooks/web/useI18n'
import { useRouter } from 'vue-router'

const activeIndex = ref('1')
const { t } = useI18n()
const permissionStore = usePermissionStore()

const routers = permissionStore.getRoutersNotHidden
const { push } = useRouter()

console.log('routers', routers)

const handleSelect = (index: string) => {
  // 自定义事件
  if (isExternal(index)) {
    window.open(index, '_blank')
  } else {
    push(index)
  }
}

const resolvePath = (item: AppRouteRecordRaw) => {
  // 如果是首页，就返回重定向路由
  if (item.path === '/') {
    return item.redirect as string
  }

  // 如果有子项，默认跳转第一个子项路由
  let path = ''
  /**
   * item 路由子项
   * parent 路由父项
   */
  const getDefaultPath = (item, parent) => {
    // 如果path是个外部链接（不建议），直接返回链接，存在个问题：如果是外部链接点击跳转后当前页内容还是上一个路由内容
    if (isExternal(item.path)) {
      path = item.path
      return
    }
    // 第一次需要父项路由拼接，所以只是第一个传parent
    if (parent) {
      path += parent.path + '/' + item.path
    } else {
      path += '/' + item.path
    }
    // 如果还有子项，继续递归
    if (item.children) {
      getDefaultPath(item.children[0], item)
    }
  }

  if (item.children) {
    getDefaultPath(item.children[0], item)
    return path
  }

  return item.path
}
</script>

<template>
  <el-header class="header-flex">
    <Icon className="logo" name="area"></Icon>
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
    <div class="operate-setting">{{ t('common.inputText') }}</div>
  </el-header>
</template>

<style lang="less" scoped>
.header-flex {
  display: flex;
  align-items: center;
  .operate-setting {
    margin-left: auto;
  }
}
.flex-grow {
  flex-grow: 1;
}
.logo {
  width: 140px;
  height: 45px;
}
</style>
