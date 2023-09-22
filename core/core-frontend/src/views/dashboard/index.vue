<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import findComponent from '../../utils/components'
import DvSidebar from '../../components/visualization/DvSidebar.vue'
import router from '@/router'
import DbToolbar from '@/components/dashboard/DbToolbar.vue'
import ViewEditor from '@/views/chart/components/editor/index.vue'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import DbCanvasAttr from '@/components/dashboard/DbCanvasAttr.vue'
import { initCanvasData } from '@/utils/canvasUtils'
import { ElMessage } from 'element-plus-secondary'
import ChartStyleBatchSet from '@/views/chart/components/editor/editor-style/ChartStyleBatchSet.vue'
import DeCanvas from '@/views/canvas/DeCanvas.vue'

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const activeName = ref('attr')
const { componentData, curComponent, canvasStyleData, canvasViewInfo, editMode, batchOptStatus } =
  storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const dataInitState = ref(false)

const state = reactive({
  datasetTree: [],
  sourcePid: null,
  canvasId: 'canvas-main'
})

const initDataset = () => {
  getDatasetTree({}).then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}

const viewEditorShow = computed(() => {
  return Boolean(
    curComponent.value &&
      ['UserView', 'VQuery'].includes(curComponent.value.component) &&
      !batchOptStatus.value
  )
})

// 全局监听按键事件
onMounted(() => {
  initDataset()
  const { resourceId, opt, pid } = window.DataEaseBi || router.currentRoute.value.query
  state.sourcePid = pid
  if (resourceId) {
    dataInitState.value = false
    initCanvasData(resourceId, function () {
      dataInitState.value = true
    })
  } else if (opt && opt === 'create') {
    dataInitState.value = false
    dvMainStore.createInit('dashboard')
    nextTick(() => {
      dataInitState.value = true
      // preOpt
      canvasStyleData.value.component.chartTitle.color = '#000000'
    })
  } else {
    ElMessage.error('未获取资源ID')
  }
})
</script>

<template>
  <div class="dv-common-layout dv-teleport-query">
    <DbToolbar />
    <el-container
      class="dv-layout-container"
      :class="{ 'preview-content': editMode === 'preview' }"
    >
      <!-- 中间画布 -->
      <main class="center">
        <de-canvas
          style="overflow-x: hidden"
          v-if="dataInitState"
          :canvas-id="state.canvasId"
          :component-data="componentData"
          :canvas-style-data="canvasStyleData"
          :canvas-view-info="canvasViewInfo"
        ></de-canvas>
      </main>
      <!-- 右侧侧组件列表 -->
      <dv-sidebar
        v-if="
          curComponent &&
          !['UserView', 'VQuery'].includes(curComponent.component) &&
          !batchOptStatus
        "
        :theme-info="'light'"
        :title="curComponent.label || '属性'"
        :width="420"
        :side-name="'componentProp'"
        :aside-position="'right'"
        class="left-sidebar"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <component :is="findComponent(curComponent['component'] + 'Attr')" :themes="'light'" />
      </dv-sidebar>
      <dv-sidebar
        v-show="!curComponent && !batchOptStatus"
        :theme-info="'light'"
        title="仪表板配置"
        :width="420"
        aside-position="right"
        class="left-sidebar"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <DbCanvasAttr></DbCanvasAttr>
      </dv-sidebar>
      <view-editor
        v-show="viewEditorShow"
        :themes="'light'"
        :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
        :dataset-tree="state.datasetTree"
        :class="{ 'preview-aside': editMode === 'preview' }"
      ></view-editor>
      <dv-sidebar
        v-if="batchOptStatus"
        :theme-info="'light'"
        title="批量操作设置样式"
        :width="280"
        aside-position="right"
        class="left-sidebar"
        :side-name="'batchOpt'"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <chart-style-batch-set></chart-style-batch-set>
      </dv-sidebar>
    </el-container>
  </div>
</template>

<style lang="less">
.dv-common-layout {
  height: 100vh;
  width: 100vw;
  .dv-layout-container {
    height: calc(100vh - @top-bar-height);
    .left-sidebar {
      height: 100%;
    }
    .center {
      display: flex;
      flex-direction: column;
      height: 100%;
      flex: 1;
      position: relative;
      overflow: auto;
      .content {
        flex: 1;
        width: 100%;
        .db-canvas {
          padding: 2px;
          background-size: 100% 100% !important;
          overflow-y: auto;
          width: 100%;
          height: 100%;
        }
      }
    }
    .right-sidebar {
      height: 100%;
    }
  }
}

.preview-aside {
  border: 0px !important;
  width: 0px !important;
  overflow: hidden;
  padding: 0px;
}

.preview-content {
  height: 100vh !important;
  :deep(.editor-light) {
    border: 0 !important;
  }
}
</style>
