<script setup lang="ts">
import { ElAside, ElContainer } from 'element-plus-secondary'
import DeResourceTree from '@/views/common/DeResourceTree.vue'
</script>

<template>
  <div class="dv-preview">
    <el-aside class="resource-area">
      <de-resource-tree></de-resource-tree>
    </el-aside>
    <el-container class="preview-area">
      <span>大屏预览区</span>
    </el-container>
  </div>
</template>

<style lang="less">
.dv-preview {
  width: 100%;
  height: 100vh;
  display: flex;
  .resource-area {
    height: 100%;
    width: 300px;
    padding: 10px;
    border-right: 1px solid #d7d7d7;
  }
  .preview-area {
    flex: 1;
  }
}
</style>
