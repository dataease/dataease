<script lang="ts" setup>
import { computed } from 'vue'
import { ElMenu } from 'element-plus-secondary'
import { useRoute, useRouter } from 'vue-router'
import { isExternal } from '@/utils/validate'
import MenuItem from './MenuItem.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const tempColor = computed(() => {
  return {
    '--temp-color':
      (appearanceStore.themeColor === 'custom' ? appearanceStore.customColor : '#3370FF') + '1A'
  }
})
defineProps({
  collapse: Boolean
})

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
    :style="tempColor"
    @select="menuSelect"
    :default-active="activeIndex"
    class="el-menu-vertical"
    :collapse="collapse"
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
  .ed-menu-item:not(.is-active) {
    &:hover {
      background-color: #1f23291a !important;
    }
  }
  .is-active:not(.ed-sub-menu) {
    background-color: var(--temp-color);
  }
  :deep(.ed-sub-menu) {
    margin: 0;
    .ed-sub-menu__title {
      &:hover {
        background-color: #1f23291a;
      }
    }
    .ed-menu-item:not(.is-active) {
      &:hover {
        background-color: #1f23291a !important;
      }
    }
    ul.ed-menu {
      li.ed-menu-item {
        i {
          width: 4px !important;
        }
      }
    }
  }
  :deep(.ed-sub-menu.is-active) {
    .ed-sub-menu__title {
      color: var(--ed-color-primary);
    }
    .is-active {
      background-color: var(--temp-color);
    }
  }
}
</style>
