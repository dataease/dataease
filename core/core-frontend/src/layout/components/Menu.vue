<script lang="ts" setup>
import { ref, computed } from 'vue'
import { ElMenu } from 'element-plus-secondary'
import { useRoute, useRouter } from 'vue-router'
import { isExternal } from '@/utils/validate'
import MenuItem from './MenuItem.vue'
const isCollapse = ref(false)
const route = useRoute()
const { push } = useRouter()
const menuList = computed(() => route.matched[0]?.children || [])
const path = computed(() => route.matched[0]?.path)

const activeIndex = computed(() => {
  const arr = route.path.split('/')
  return arr[arr.length - 1]
})
const menuSelect = (index: string, indexPath: string[]) => {
  //   自定义事件
  if (isExternal(index)) {
    window.open(index)
  } else {
    push(`${path.value}/${indexPath.join('/')}`)
  }
}
</script>

<template>
  <el-menu
    @select="menuSelect"
    :default-active="activeIndex"
    class="el-menu-vertical"
    :collapse="isCollapse"
  >
    <MenuItem v-for="menu in menuList" :key="menu.path" :menu="menu"></MenuItem>
  </el-menu>
</template>

<style lang="less" scoped>
.ed-menu-vertical:not(.ed-menu--collapse) {
  width: 100%;
  min-height: 400px;
}
.ed-menu {
  border: none;
}
</style>
