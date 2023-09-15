<script setup lang="ts">
import { ElAside, ElContainer } from 'element-plus-secondary'
import DeResourceTree from '@/views/common/DeResourceTree.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { reactive, nextTick, ref, toRefs, onBeforeMount } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import PreviewHead from '@/views/data-visualization/PreviewHead.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { storeToRefs } from 'pinia'
import { toPng } from 'html-to-image'
import { initCanvasData, initCanvasDataPrepare } from '@/utils/canvasUtils'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import JsPDF from 'jspdf'

const dvMainStore = dvMainStoreWithOut()
const previewCanvasContainer = ref(null)
const dashboardPreview = ref(null)
const slideShow = ref(true)
const requestStore = useRequestStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const dataInitState = ref(true)
const downloadStatus = ref(false)
const { dvInfo } = storeToRefs(dvMainStore)
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})

const { width, node } = useMoveLine('DASHBOARD')

const props = defineProps({
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  }
})

const { showPosition } = toRefs(props)

const loadCanvasData = (dvId, weight?) => {
  // 复用不设置 dvMain 中的componentData 等画布信息
  const initMethod = showPosition.value === 'multiplexing' ? initCanvasDataPrepare : initCanvasData
  dataInitState.value = false
  initMethod(
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
      dataInitState.value = true
      nextTick(() => {
        dashboardPreview.value.restore()
      })
    }
  )
}

const download = type => {
  downloadStatus.value = true
  nextTick(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    toPng(vueDom)
      .then(dataUrl => {
        if (type === 'img') {
          const a = document.createElement('a')
          a.setAttribute('download', dvInfo.value.name)
          a.href = dataUrl
          a.click()
        } else {
          const contentWidth = vueDom.offsetWidth
          const contentHeight = vueDom.offsetHeight
          const lp = contentWidth > contentHeight ? 'l' : 'p'
          const PDF = new JsPDF(lp, 'pt', [contentWidth, contentHeight])
          PDF.addImage(dataUrl, 'PNG', 0, 0, contentWidth, contentHeight)
          PDF.save(dvInfo.value.name + '.pdf')
        }
      })
      .catch(error => {
        console.error('oops, something went wrong!', error)
      })
      .finally(() => {
        downloadStatus.value = false
      })
  })
}

const htmlToImage = () => {
  toPng(previewCanvasContainer.value.querySelector('.canvas-container'))
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', state.dvInfo.name)
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

const getPreviewStateInfo = () => {
  return state
}

const reload = id => {
  loadCanvasData(id, state.dvInfo.weight)
}

const resourceNodeClick = data => {
  loadCanvasData(data.id, data.weight)
}

onBeforeMount(() => {
  dvMainStore.canvasDataInit()
})

defineExpose({
  getPreviewStateInfo
})
</script>

<template>
  <div class="dv-preview dv-teleport-query">
    <el-aside
      class="resource-area"
      :class="{ 'close-side': !slideShow }"
      ref="node"
      :style="{ width: width + 'px' }"
    >
      <de-resource-tree
        v-show="slideShow"
        :cur-canvas-type="'dashboard'"
        :show-position="showPosition"
        @node-click="resourceNodeClick"
      />
    </el-aside>
    <el-container
      class="preview-area"
      v-loading="requestStore.loadingMap[permissionStore.currentPath]"
    >
      <div
        @click="slideOpenChange"
        v-if="showPosition === 'preview' && false"
        class="flexible-button-area"
      >
        <el-icon v-if="slideShow"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </div>
      <!--从store中判断当前是否有点击仪表板 复用时也符合-->
      <template v-if="dvInfo.name">
        <preview-head
          v-if="showPosition === 'preview'"
          @reload="reload"
          @download="download"
        ></preview-head>
        <div ref="previewCanvasContainer" class="content">
          <de-preview
            ref="dashboardPreview"
            v-if="state.canvasStylePreview && dataInitState"
            :dv-info="state.dvInfo"
            :cur-gap="state.curPreviewGap"
            :component-data="state.canvasDataPreview"
            :canvas-style-data="state.canvasStylePreview"
            :canvas-view-info="state.canvasViewInfoPreview"
            :show-position="showPosition"
            :download-status="downloadStatus"
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
  overflow: hidden;
  display: flex;
  background: #ffffff;
  .resource-area {
    position: relative;
    height: 100%;
    width: 279px;
    padding: 0;
    border-right: 1px solid #d7d7d7;
    //transition: 0.5s;
  }
  .preview-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    position: relative;
    //transition: 0.5s;

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
