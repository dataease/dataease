<script setup lang="ts">
import { ElAside, ElContainer } from 'element-plus-secondary'
import DeResourceTree from '@/views/common/DeResourceTree.vue'
import { findById } from '@/api/dataVisualization'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'

const dvMainStore = dvMainStoreWithOut()
const canvasDataPreview = ref(null)
const canvasStylePreview = ref(null)

const loadCanvasData = dvId => {
  findById(dvId).then(res => {
    const canvasInfo = res.data
    const bashInfo = {
      id: canvasInfo.id,
      name: canvasInfo.name,
      pid: canvasInfo.pid,
      status: canvasInfo.status,
      selfWatermarkStatus: canvasInfo.selfWatermarkStatus
    }
    canvasDataPreview.value = JSON.parse(canvasInfo.componentData)
    canvasStylePreview.value = JSON.parse(canvasInfo.canvasStyleData)
    dvMainStore.updateCurDvInfo(bashInfo)
  })
}
</script>

<template>
  <div class="dv-preview">
    <el-aside class="resource-area">
      <de-resource-tree @node-click="loadCanvasData"></de-resource-tree>
    </el-aside>
    <el-container class="preview-area">
      <de-preview
        v-if="canvasStylePreview"
        :component-data="canvasDataPreview"
        :canvas-style-data="canvasStylePreview"
      ></de-preview>
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
