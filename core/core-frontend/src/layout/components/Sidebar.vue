<script lang="ts" setup>
import { ref } from 'vue'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { ElAside } from 'element-plus-secondary'
const line = ref()
const { width } = useMoveLine('DATASET', line)
</script>

<template>
  <el-aside ref="line" :width="width + 'px'">
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
