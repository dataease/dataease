<script lang="ts" setup>
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { ElAside } from 'element-plus-secondary'
const { width, node } = useMoveLine('DATASET')
</script>

<template>
  <el-aside ref="node" :width="width + 'px'">
    <slot />
  </el-aside>
</template>

<style lang="less">
.ed-aside {
  position: relative;
}
.sidebar-move-line {
  width: 4px;
  height: 100%;
  z-index: 1;
  position: absolute;
  cursor: col-resize;
  &:hover::after,
  &.dragging::after {
    width: 1px;
    height: 100%;
    content: '';
    z-index: 2;
    position: absolute;
    right: -1px;
    top: 0;
    background: var(--ed-color-primary);
  }
}
</style>
