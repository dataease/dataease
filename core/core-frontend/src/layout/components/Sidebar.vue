<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { ElAside } from 'element-plus-secondary'
const line = ref()
let asideWidth = ref()
onMounted(() => {
  useMoveLine('DATASET', line.value?.$el, asideWidth)
})
const width = computed(() => `${asideWidth.value || 260}px`)
</script>

<template>
  <el-aside ref="line" :width="width">
    <slot />
  </el-aside>
</template>

<style lang="less">
.ed-aside {
  position: relative;
}
.sidebar-move-line {
  width: 2px;
  height: 100%;
  position: absolute;
  cursor: col-resize;
}
</style>
