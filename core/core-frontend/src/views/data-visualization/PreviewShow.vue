<script setup lang="ts">
import { ElAside, ElContainer } from 'element-plus-secondary'
import DeResourceTree from '@/views/common/DeResourceTree.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { nextTick, reactive, ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import PreviewHead from '@/views/data-visualization/PreviewHead.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { storeToRefs } from 'pinia'
import { toPng } from 'html-to-image'
import { initCanvasData } from '@/utils/canvasUtils'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { usePermissionStoreWithOut } from '@/store/modules/permission'

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const previewCanvasContainer = ref(null)
const dvPreview = ref(null)
const slideShow = ref(true)
const requestStore = useRequestStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const dataInitState = ref(true)

const props = defineProps({
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  }
})

const loadCanvasData = (dvId, weight?) => {
  dataInitState.value = false
  initCanvasData(
    dvId,
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      dvInfo['weight'] = weight
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap
      dvMainStore.updateCurDvInfo(dvInfo)
      dataInitState.value = true
      nextTick(() => {
        dvPreview.value.restore()
      })
    }
  )
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

const slideOpenChange = () => {
  slideShow.value = !slideShow.value
}

const reload = id => {
  loadCanvasData(id, state.dvInfo.weight)
}

const resourceNodeClick = data => {
  loadCanvasData(data.id, data.weight)
}

const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})
</script>

<template>
  <div class="dv-preview">
    <el-aside class="resource-area" :class="{ 'close-side': !slideShow }">
      <de-resource-tree
        v-show="slideShow"
        :cur-canvas-type="'dataV'"
        :show-position="showPosition"
        @node-click="resourceNodeClick"
      ></de-resource-tree>
    </el-aside>
    <el-container
      class="preview-area"
      v-loading="requestStore.loadingMap[permissionStore.currentPath]"
    >
      <div @click="slideOpenChange" class="flexible-button-area">
        <el-icon v-if="slideShow"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </div>
      <template v-if="dvInfo.name">
        <preview-head
          v-show="showPosition === 'preview'"
          @reload="reload"
          @download="htmlToImage"
        ></preview-head>
        <div ref="previewCanvasContainer" class="content">
          <div class="content-inner">
            <de-preview
              ref="dvPreview"
              v-if="state.canvasStylePreview && dataInitState"
              :component-data="state.canvasDataPreview"
              :canvas-style-data="state.canvasStylePreview"
              :canvas-view-info="state.canvasViewInfoPreview"
              :dv-info="state.dvInfo"
              :cur-gap="state.curPreviewGap"
              :show-position="showPosition"
            ></de-preview>
          </div>
        </div>
      </template>
      <template v-else>
        <empty-background description="请在左侧选择数据大屏" img-type="select" />
      </template>
    </el-container>
  </div>
</template>

<style lang="less">
::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}
.dv-preview {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  background: #ffffff;
  .resource-area {
    height: 100%;
    width: 300px;
    padding: 16px;
    border-right: 1px solid #d7d7d7;
  }
  .preview-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    position: relative;
    transition: 0.5s;
    .content {
      flex: 1;
      width: 100%;
      overflow-x: hidden;
      overflow-y: auto;
      align-items: center;
      .content-inner {
        width: 100%;
        height: calc(100vh - 95px);
        display: flex;
        overflow-y: auto;
        align-items: center;
      }
    }
  }
}

.close-side {
  width: 0px !important;
  padding: 0px !important;
  border-right: 0px !important;
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
