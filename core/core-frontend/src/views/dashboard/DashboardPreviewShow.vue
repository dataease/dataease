<script setup lang="ts">
import { ElAside, ElContainer } from 'element-plus-secondary'
import DeResourceTree from '@/views/common/DeResourceTree.vue'
import { findById } from '@/api/visualization/dataVisualization'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { computed, nextTick, ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import PreviewHead from '@/views/data-visualization/PreviewHead.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { storeToRefs } from 'pinia'
import { toPng } from 'html-to-image'

const curCanvasType = 'dashboard'
const dvMainStore = dvMainStoreWithOut()
const canvasDataPreview = ref([])
const canvasStylePreview = ref(null)
const canvasViewInfoPreview = ref({})

const { dvInfo } = storeToRefs(dvMainStore)
const previewCanvasContainer = ref(null)
const dashboardPreview = ref(null)
const slideShow = ref(true)

const loadCanvasData = dvId => {
  findById(dvId).then(res => {
    const canvasInfo = res.data
    const bashInfo = {
      id: canvasInfo.id,
      name: canvasInfo.name,
      pid: canvasInfo.pid,
      status: canvasInfo.status,
      selfWatermarkStatus: canvasInfo.selfWatermarkStatus,
      type: canvasInfo.type
    }
    canvasDataPreview.value = JSON.parse(canvasInfo.componentData)
    canvasStylePreview.value = JSON.parse(canvasInfo.canvasStyleData)
    canvasViewInfoPreview.value = canvasInfo.canvasViewInfo
    dvMainStore.updateCurDvInfo(bashInfo)
    nextTick(() => {
      dashboardPreview.value.restore()
    })
  })
}

const htmlToImage = () => {
  toPng(previewCanvasContainer.value.querySelector('.canvas-container'))
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', dvInfo.value.name)
      a.href = dataUrl
      a.click()
    })
    .catch(error => {
      console.error('oops, something went wrong!', error)
    })
}

const curGap = computed(() => {
  return canvasStylePreview.value && canvasStylePreview.value['dashboard']['gap'] === 'yes'
    ? canvasStylePreview.value['dashboard']['gapSize']
    : 0
})

const slideOpenChange = () => {
  slideShow.value = !slideShow.value
}
</script>

<template>
  <div class="dv-preview">
    <el-aside class="resource-area" :class="{ 'close-side': !slideShow }">
      <de-resource-tree
        v-show="slideShow"
        :cur-canvas-type="'dashboard'"
        @node-click="loadCanvasData"
      ></de-resource-tree>
    </el-aside>
    <el-container class="preview-area">
      <div @click="slideOpenChange" class="flexible-button-area">
        <el-icon v-if="slideShow"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </div>
      <template v-if="dvInfo.name">
        <preview-head @reload="loadCanvasData" @download="htmlToImage"></preview-head>
        <div ref="previewCanvasContainer" class="content">
          <de-preview
            ref="dashboardPreview"
            v-if="canvasStylePreview"
            :dv-info="dvInfo"
            :cur-gap="curGap"
            :component-data="canvasDataPreview"
            :canvas-style-data="canvasStylePreview"
            :canvas-view-info="canvasViewInfoPreview"
          ></de-preview>
        </div>
      </template>
      <template v-else>
        <empty-background description="请在左侧选择数据大屏" img-type="select" />
      </template>
    </el-container>
  </div>
</template>

<style lang="less">
.dv-preview {
  width: 100%;
  height: 100%;
  display: flex;
  background: #ffffff;
  .resource-area {
    position: relative;
    height: 100%;
    width: 300px;
    padding: 16px;
    border-right: 1px solid #d7d7d7;
    transition: 0.5s;
  }
  .preview-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    position: relative;
    transition: 0.5s;
    position: relative;
    .content {
      display: flex;
      width: 100%;
      height: 100%;
      overflow-x: hidden;
      overflow-y: auto;
      align-items: center;
    }
  }
}

.close-side {
  width: 0px !important;
  padding: 0px !important;
}

.flexible-button-area {
  position: absolute;
  height: 60px;
  width: 16px;
  left: 0;
  top: calc(50% - 30px);
  background-color: #ffffff;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  border-top: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
}
</style>
