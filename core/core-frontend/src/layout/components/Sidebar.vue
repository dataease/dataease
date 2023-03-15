<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { ElAside } from 'element-plus-secondary'
const bbc = ref()
let asideWidth = ref()
onMounted(() => {
  useMoveLine('DATASET', bbc.value?.$el, asideWidth)
})
const width = computed(() => `${asideWidth.value || 260}px`)
</script>

<template>
  <el-aside ref="bbc" :width="width">
    <slot />
  </el-aside>
</template>

<style lang="less">
.el-aside {
  position: relative;
}
.sidebar-move-line {
  width: 2px;
  height: 100%;
  position: absolute;
  background: red;
  cursor: col-resize;
}
</style>
