<script setup lang="ts">
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { storeToRefs } from 'pinia'

const contextmenuStore = contextmenuStoreWithOut()

const { menuTop, menuLeft, menuShow } = storeToRefs(contextmenuStore)
</script>

<template>
  <context-menu-details
    v-show="menuShow"
    class="contextmenu"
    :style="{ top: menuTop + 'px', left: menuLeft + 'px' }"
  ></context-menu-details>
</template>

<style lang="less" scoped>
.contextmenu {
  position: absolute;
}
</style>
