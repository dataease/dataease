<script lang="ts" setup>
import { computed } from 'vue'
import { usePermissionStore } from '@/store/modules/permission'
import { isExternal } from '@/utils/validate'
import { resolvePath } from '@/router/establish'
import { ElHeader, ElMenu, ElMenuItem } from 'element-plus-secondary'
import { Icon } from '@/components/Icon'
import { useI18n } from '@/hooks/web/useI18n'
import { useRouter, useRoute } from 'vue-router'
const { push } = useRouter()
const route = useRoute()

const activeIndex = computed(() => {
  if (route.path.includes('system')) {
    return '/system/user'
  }
  return route.path
})
const { t } = useI18n()
const permissionStore = usePermissionStore()

const routers = permissionStore.getRoutersNotHidden

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
  margin-right: 100px;
}
</style>
