<script setup lang="ts">
import DeResourceTree from '@/views/common/DeResourceTree.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ArrowSide from '@/views/common/DeResourceArrow.vue'
import { nextTick, onBeforeMount, reactive, ref, computed } from 'vue'
import PreviewHead from '@/views/data-visualization/PreviewHead.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { storeToRefs } from 'pinia'
import { useAppStoreWithOut } from '@/store/modules/app'
import { initCanvasData, initCanvasDataPrepare } from '@/utils/canvasUtils'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { Icon } from '@/components/icon-custom'
import { download2AppTemplate, downloadCanvas2 } from '@/utils/imgUtils'
import MultiplexPreviewShow from '@/views/data-visualization/MultiplexPreviewShow.vue'
import DvPreview from '@/views/data-visualization/DvPreview.vue'
import AppExportForm from '@/components/de-app/AppExportForm.vue'
import { personInfoApi } from '@/api/user'
import { ElMessage } from 'element-plus-secondary'

const dvMainStore = dvMainStoreWithOut()
const { dvInfo, canvasViewDataInfo } = storeToRefs(dvMainStore)
const previewCanvasContainer = ref(null)
const dvPreviewRef = ref(null)
const slideShow = ref(true)
const requestStore = useRequestStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const dataInitState = ref(true)
const downloadStatus = ref(false)
const { width, node } = useMoveLine('DASHBOARD')
const appExportFormRef = ref(null)
const props = defineProps({
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  noClose: {
    required: false,
    type: Boolean,
    default: false
  }
})

const resourceTreeRef = ref()

const hasTreeData = computed(() => {
  return resourceTreeRef.value?.hasData
})

const mounted = computed(() => {
  return resourceTreeRef.value?.mounted
})

const rootManage = computed(() => {
  return resourceTreeRef.value?.rootManage
})
const appStore = useAppStoreWithOut()

const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

function createNew() {
  resourceTreeRef.value?.createNewObject()
}

const loadCanvasData = (dvId, weight?) => {
  const initMethod = props.showPosition === 'multiplexing' ? initCanvasDataPrepare : initCanvasData
  dataInitState.value = false
  initMethod(
    dvId,
    'dataV',
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
      if (props.showPosition === 'preview') {
        dvMainStore.updateCurDvInfo(dvInfo)
        nextTick(() => {
          dvPreviewRef.value?.restore()
        })
      }
    }
  )
}

const download = type => {
  downloadStatus.value = true
  setTimeout(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    downloadCanvas2(type, vueDom, state.dvInfo.name, () => {
      downloadStatus.value = false
    })
  }, 200)
}

const fileDownload = (downloadType, attachParams) => {
  downloadStatus.value = true
  nextTick(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    download2AppTemplate(downloadType, vueDom, state.dvInfo.name, attachParams, () => {
      downloadStatus.value = false
    })
  })
}

const downloadAsAppTemplate = downloadType => {
  if (downloadType === 'template') {
    fileDownload(downloadType, null)
  } else if (downloadType === 'app') {
    downLoadToAppPre()
  }
}

const downLoadToAppPre = () => {
  const result = checkTemplate()
  if (result && result.length > 0) {
    ElMessage.warning(`当前仪表板中[${result}]属于模版视图，无法导出，请先设置数据集！`)
  } else {
    appExportFormRef.value.init({
      appName: state.dvInfo.name,
      icon: null,
      version: '2.0',
      creator: state.userLoginInfo?.name,
      required: '2.9.0',
      description: null
    })
  }
}
const checkTemplate = () => {
  let templateViewNames = ','
  Object.keys(canvasViewDataInfo.value).forEach(key => {
    const viewInfo = canvasViewDataInfo.value[key]
    if (viewInfo.dataFrom === 'template') {
      templateViewNames = templateViewNames + viewInfo.title + ','
    }
  })
  return templateViewNames.slice(1)
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
  curPreviewGap: 0,
  userLoginInfo: {}
})

const sideTreeStatus = ref(true)
const changeSideTreeStatus = val => {
  sideTreeStatus.value = val
}

const mouseenter = () => {
  appStore.setArrowSide(true)
}

const mouseleave = () => {
  appStore.setArrowSide(false)
}

const getPreviewStateInfo = () => {
  return state
}

const downLoadApp = appAttachInfo => {
  fileDownload('app', appAttachInfo)
}

const findUserData = callback => {
  personInfoApi().then(rsp => {
    callback(rsp)
  })
}

defineExpose({
  getPreviewStateInfo
})

onBeforeMount(() => {
  if (props.showPosition === 'preview') {
    dvMainStore.canvasDataInit()
  }
  findUserData(res => {
    state.userLoginInfo = res.data
  })
})
</script>

<template>
  <div class="dv-preview">
    <ArrowSide
      v-if="!noClose"
      :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
      @change-side-tree-status="changeSideTreeStatus"
      :isInside="!sideTreeStatus"
    ></ArrowSide>
    <el-aside
      class="resource-area"
      @mouseenter="mouseenter"
      @mouseleave="mouseleave"
      :class="{ 'close-side': !slideShow, retract: !sideTreeStatus }"
      ref="node"
      :style="{ width: width + 'px' }"
    >
      <ArrowSide
        v-if="!noClose"
        :isInside="!sideTreeStatus"
        :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
        @change-side-tree-status="changeSideTreeStatus"
      ></ArrowSide>
      <de-resource-tree
        ref="resourceTreeRef"
        v-show="slideShow"
        :cur-canvas-type="'dataV'"
        :show-position="showPosition"
        @node-click="resourceNodeClick"
      />
    </el-aside>
    <el-container
      class="preview-area"
      :class="{ 'no-data': !hasTreeData }"
      v-loading="requestStore.loadingMap[permissionStore.currentPath]"
    >
      <div @click="slideOpenChange" class="flexible-button-area" v-if="false">
        <el-icon v-if="slideShow"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </div>
      <template v-if="dvInfo.name">
        <preview-head
          v-if="showPosition === 'preview'"
          @reload="reload"
          @download="download"
          @downloadAsAppTemplate="downloadAsAppTemplate"
        />
        <div
          v-if="showPosition === 'multiplexing' && dataInitState"
          class="content multiplexing-content"
        >
          <multiplex-preview-show
            :component-data="state.canvasDataPreview"
            :canvas-style-data="state.canvasStylePreview"
            :canvas-view-info="state.canvasViewInfoPreview"
            :dv-info="state.dvInfo"
          ></multiplex-preview-show>
        </div>
        <div v-if="showPosition === 'preview'" ref="previewCanvasContainer" class="content">
          <dv-preview
            ref="dvPreviewRef"
            v-if="state.canvasStylePreview && dataInitState"
            :show-position="showPosition"
            :canvas-data-preview="state.canvasDataPreview"
            :canvas-style-preview="state.canvasStylePreview"
            :canvas-view-info-preview="state.canvasViewInfoPreview"
            :dv-info="state.dvInfo"
            :cur-preview-gap="state.curPreviewGap"
            :download-status="downloadStatus"
          ></dv-preview>
        </div>
      </template>
      <template v-else-if="hasTreeData && mounted">
        <empty-background description="请在左侧选择数据大屏" img-type="select" />
      </template>
      <template v-else-if="mounted">
        <empty-background description="暂无数据大屏" img-type="none">
          <el-button v-if="rootManage && !isDataEaseBi" @click="createNew" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined" />
            </template>
            {{ $t('commons.create') }}数据大屏
          </el-button>
        </empty-background>
      </template>
    </el-container>
  </div>
  <app-export-form
    ref="appExportFormRef"
    :dv-info="state.dvInfo"
    :component-data="state.canvasDataPreview"
    :canvas-view-info="state.canvasViewInfoPreview"
    @downLoadApp="downLoadApp"
  ></app-export-form>
</template>

<style lang="less">
.dv-preview {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  background: #ffffff;
  position: relative;
  .resource-area {
    position: relative;
    height: 100%;
    width: 279px;
    padding: 0;
    overflow: visible;
    border-right: 1px solid #d7d7d7;

    &.retract {
      display: none;
    }
  }
  .preview-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    position: relative;
    //transition: 0.5s;

    &.no-data {
      background-color: rgba(245, 246, 247, 1);
    }

    .content {
      flex: 1;
      width: 100%;
      overflow-x: hidden;
      overflow-y: auto;
      align-items: center;
      .content-outer {
        width: 100%;
        height: calc(100vh - 112px);
        background: #f5f6f7;
        display: flex;
        overflow-y: auto;
        align-items: center;
        flex-direction: column;
        justify-content: center; /* 上下居中 */
        .content-inner {
          width: 100%;
          height: auto;
          overflow-x: hidden;
          overflow-y: auto;
        }
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

.multiplexing-content {
  padding: 12px;
  background-color: rgb(245, 246, 247);
}
</style>
