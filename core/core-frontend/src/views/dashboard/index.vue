<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { findComponentAttr } from '../../utils/components'
import DvSidebar from '../../components/visualization/DvSidebar.vue'
import router from '@/router'
import MobileConfigPanel from './MobileConfigPanel.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmitt } from '@/hooks/web/useEmitt'
import DbToolbar from '@/components/dashboard/DbToolbar.vue'
import ViewEditor from '@/views/chart/components/editor/index.vue'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import DbCanvasAttr from '@/components/dashboard/DbCanvasAttr.vue'
import { decompressionPre, initCanvasData, onInitReady } from '@/utils/canvasUtils'
import ChartStyleBatchSet from '@/views/chart/components/editor/editor-style/ChartStyleBatchSet.vue'
import DeCanvas from '@/views/canvas/DeCanvas.vue'
import { check, compareStorage } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
import { cloneDeep } from 'lodash-es'
import { useEmbedded } from '@/store/modules/embedded'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { watermarkFind } from '@/api/watermark'
import { XpackComponent } from '@/components/plugin'
import { Base64 } from 'js-base64'
import CanvasCacheDialog from '@/components/visualization/CanvasCacheDialog.vue'
import { deepCopy } from '@/utils/utils'
const interactiveStore = interactiveStoreWithOut()
import { useRequestStoreWithOut } from '@/store/modules/request'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import eventBus from '@/utils/eventBus'
import { useI18n } from '@/hooks/web/useI18n'
import DashboardHiddenComponent from '@/components/dashboard/DashboardHiddenComponent.vue'
import { recoverToPublished } from '@/api/visualization/dataVisualization'
const embeddedStore = useEmbedded()
const { wsCache } = useCache()
const canvasCacheOutRef = ref(null)
const deCanvasRef = ref(null)
const eventCheck = e => {
  if (e.key === 'panel-weight' && !compareStorage(e.oldValue, e.newValue)) {
    const resourceId = embeddedStore.resourceId || router.currentRoute.value.query.resourceId
    const opt = embeddedStore.opt || router.currentRoute.value.query.opt
    if (!(opt && opt === 'create')) {
      check(wsCache.get('panel-weight'), resourceId as string, 4)
    }
  }
}
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const requestStore = useRequestStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const {
  fullscreenFlag,
  componentData,
  curComponent,
  canvasStyleData,
  canvasViewInfo,
  editMode,
  batchOptStatus,
  hiddenListStatus,
  lastHiddenComponent,
  dvInfo
} = storeToRefs(dvMainStore)
const dataInitState = ref(false)
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const { t } = useI18n()

const state = reactive({
  datasetTree: [],
  sourcePid: null,
  canvasId: 'canvas-main',
  opt: null,
  resourceId: null
})

const initDataset = () => {
  getDatasetTree({}).then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}

const otherEditorShow = computed(() => {
  return Boolean(
    curComponent.value &&
      (!['UserView', 'VQuery'].includes(curComponent.value?.component) ||
        (curComponent.value?.component === 'UserView' &&
          curComponent.value?.innerType === 'picture-group')) &&
      !batchOptStatus.value &&
      !hiddenListStatus.value
  )
})

const otherEditorTitle = computed(() => {
  return curComponent.value?.component === 'UserView'
    ? t('visualization.attribute')
    : curComponent.value?.label || t('visualization.attribute')
})

const viewEditorShow = computed(() => {
  return Boolean(
    curComponent.value &&
      ['UserView', 'VQuery'].includes(curComponent.value.component) &&
      curComponent.value.innerType !== 'picture-group' &&
      !batchOptStatus.value &&
      !hiddenListStatus.value
  )
})
const checkPer = async resourceId => {
  if (!window.DataEaseBi || !resourceId) {
    return true
  }
  const request = { busiFlag: 'dashboard', resourceTable: 'core' }
  await interactiveStore.setInteractive(request)
  return check(wsCache.get('panel-weight'), resourceId, 4)
}

const mobileConfig = ref(false)

const onMobileConfig = () => {
  const canvasStyleDataCopy = cloneDeep(canvasStyleData.value)
  if (!canvasStyleDataCopy.mobileSetting) {
    canvasStyleDataCopy.mobileSetting = {
      backgroundColorSelect: false,
      background: '',
      color: '#ffffff',
      backgroundImageEnable: false,
      customSetting: false
    }
  }
  dvMainStore.setCanvasStyle(canvasStyleDataCopy)
  nextTick(() => {
    mobileConfig.value = true
    dvMainStore.setCurComponent({ component: null, index: null })
  })
}

const loadFinish = ref(false)
const newWindowFromDiv = ref(false)
let p = null
const XpackLoaded = () => p(true)

const doUseCache = flag => {
  const canvasCache = wsCache.get('DE-DV-CATCH-' + state.resourceId)
  if (flag && canvasCache) {
    const canvasCacheSeries = deepCopy(canvasCache)
    snapshotStore.snapshotPublish(canvasCacheSeries)
    dataInitState.value = true
    setTimeout(() => {
      snapshotStore.recordSnapshotCache('doUseCache')
      // 使用缓存时，初始化的保存按钮为激活状态
      snapshotStore.recordSnapshotCache('renderChart')
    }, 1500)
  } else {
    initLocalCanvasData()
    wsCache.delete('DE-DV-CATCH-' + state.resourceId)
  }
}

const initLocalCanvasData = callBack => {
  const { resourceId, opt, sourcePid } = state
  const busiFlag = opt === 'copy' ? 'dashboard-copy' : 'dashboard'
  initCanvasData(
    resourceId,
    { busiFlag, resourceTable: 'snapshot', source: 'main-edit' },
    function () {
      dataInitState.value = true
      if (dvInfo.value && opt === 'copy') {
        dvInfo.value.dataState = 'prepare'
        dvInfo.value.optType = 'copy'
        dvInfo.value.pid = sourcePid
        setTimeout(() => {
          snapshotStore.recordSnapshotCache('initLocalCanvasData')
        }, 1500)
      }
      onInitReady({ resourceId: resourceId })
      callBack && callBack()
    }
  )
}
onMounted(async () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setHiddenListStatus(false)
  snapshotStore.initSnapShot()
  if (window.location.hash.includes('#/dashboard')) {
    newWindowFromDiv.value = true
  }
  await new Promise(r => (p = r))
  loadFinish.value = true
  useEmitt({
    name: 'mobileConfig',
    callback: () => {
      onMobileConfig()
    }
  })
  window.addEventListener('storage', eventCheck)
  window.addEventListener('message', winMsgHandle)
  const resourceId = embeddedStore.resourceId || router.currentRoute.value.query.resourceId
  const pid = embeddedStore.pid || router.currentRoute.value.query.pid
  const opt = embeddedStore.opt || router.currentRoute.value.query.opt
  const createType = embeddedStore.createType || router.currentRoute.value.query.createType
  const templateParams =
    embeddedStore.templateParams || router.currentRoute.value.query.templateParams
  const checkResult = await checkPer(resourceId)
  if (!checkResult) {
    return
  }
  initDataset()

  state.sourcePid = pid
  state.opt = opt
  state.resourceId = resourceId
  if (resourceId) {
    dataInitState.value = false
    const canvasCache = wsCache.get('DE-DV-CATCH-' + resourceId)
    if (canvasCache) {
      canvasCacheOutRef.value?.dialogInit({ canvasType: 'dashboard', resourceId: resourceId })
    } else {
      initLocalCanvasData(() => {
        // do init
      })
    }
  } else if (opt && opt === 'create') {
    dataInitState.value = false
    let watermarkBaseInfo
    try {
      await watermarkFind().then(rsp => {
        watermarkBaseInfo = rsp.data
        watermarkBaseInfo.settingContent = JSON.parse(watermarkBaseInfo.settingContent)
      })
    } catch (e) {
      console.error('can not find watermark info')
    }
    let deTemplateData
    let preName
    if (createType === 'template') {
      const templateParamsApply = JSON.parse(Base64.decode(decodeURIComponent(templateParams + '')))
      await decompressionPre(templateParamsApply, result => {
        deTemplateData = result
        preName = deTemplateData.baseInfo?.preName
      })
    }
    nextTick(() => {
      dvMainStore.createInit('dashboard', null, pid, watermarkBaseInfo, preName)
      // 从模板新建
      if (createType === 'template') {
        wsCache.delete('de-template-data')
        dvMainStore.setComponentData(deTemplateData['componentData'])
        dvMainStore.setCanvasStyle(deTemplateData['canvasStyleData'])
        dvMainStore.setCanvasViewInfo(deTemplateData['canvasViewInfo'])
        dvMainStore.setAppDataInfo(deTemplateData['appData'])
        setTimeout(() => {
          snapshotStore.recordSnapshotCache('template')
        }, 1500)
        if (dvMainStore.getAppDataInfo()) {
          eventBus.emit('save')
        }
      }
      dataInitState.value = true
      // preOpt
      canvasStyleData.value.component.chartTitle.color = '#000000'
    })
  } else {
    let url = '#/panel/index'
    window.open(url, '_self')
  }
})

// 目标校验： 需要校验targetSourceId 是否是当前可视化资源ID
const winMsgHandle = event => {
  const msgInfo = event.data
  if (msgInfo?.targetSourceId === dvInfo.value.id + '')
    if (msgInfo.type === 'webParams') {
      // 网络消息处理
      winMsgWebParamsHandle(msgInfo)
    }
}

const winMsgWebParamsHandle = msgInfo => {
  const params = msgInfo.params
  dvMainStore.addWebParamsFilter(params)
}

const dashboardComponentData = computed(() =>
  componentData.value.filter(item => !item.dashboardHidden)
)

const cancelHidden = item => {
  if (deCanvasRef.value) {
    if (!(lastHiddenComponent.value?.length && lastHiddenComponent.value.includes(item.id))) {
      item.y = undefined
    }
    deCanvasRef.value.addItemBox(item)
    nextTick(() => {
      deCanvasRef.value.canvasInit(false)
    })
    snapshotStore.recordSnapshotCache('cancelHidden')
  }
}

const doRecoverToPublished = () => {
  recoverToPublished({ id: dvInfo.value.id, type: 'dashboard', name: dvInfo.value.name }).then(
    () => {
      state.resourceId = dvInfo.value.id
      state.sourcePid = dvInfo.value.pid
      state.opt = null
      initLocalCanvasData(() => {
        nextTick(() => {
          deCanvasRef.value.canvasInit(false)
          dvMainStore.updateDvInfoCall(1)
          useEmitt().emitter.emit('calcData-all')
        })
      })
    }
  )
}

onUnmounted(() => {
  window.removeEventListener('storage', eventCheck)
  window.removeEventListener('message', winMsgHandle)
})
</script>

<template>
  <div
    class="dv-common-layout dv-teleport-query"
    :class="isDataEaseBi && !newWindowFromDiv && 'dataease-w-h'"
    v-loading="requestStore.loadingMap[permissionStore.currentPath]"
    v-if="loadFinish && !mobileConfig"
  >
    <DbToolbar @recoverToPublished="doRecoverToPublished" />
    <el-container
      class="dv-layout-container"
      :class="{ 'preview-content': editMode === 'preview' }"
      element-loading-background="rgba(0, 0, 0, 0)"
    >
      <!-- 中间画布 -->
      <main class="center" :class="{ 'de-screen-full': fullscreenFlag }">
        <de-canvas
          style="overflow-x: hidden"
          v-if="dataInitState"
          ref="deCanvasRef"
          :canvas-id="state.canvasId"
          :component-data="dashboardComponentData"
          :canvas-style-data="canvasStyleData"
          :canvas-view-info="canvasViewInfo"
          :font-family="canvasStyleData.fontFamily"
        ></de-canvas>
      </main>
      <!-- 右侧侧组件列表 -->
      <dv-sidebar
        v-if="otherEditorShow"
        :theme-info="'light'"
        :title="otherEditorTitle"
        :width="420"
        :side-name="'componentProp'"
        :aside-position="'right'"
        :view="canvasViewInfo[curComponent.id]"
        :element="curComponent"
        class="left-sidebar"
      >
        <component :is="findComponentAttr(curComponent)" :themes="'light'" />
      </dv-sidebar>
      <dv-sidebar
        v-show="!curComponent && !batchOptStatus && !hiddenListStatus"
        :theme-info="'light'"
        :title="t('visualization.dashboard_configuration')"
        :width="420"
        aside-position="right"
        side-name="canvas"
        class="left-sidebar"
      >
        <DbCanvasAttr></DbCanvasAttr>
      </dv-sidebar>
      <div v-show="viewEditorShow" style="height: 100%">
        <view-editor
          :themes="'light'"
          :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
          :dataset-tree="state.datasetTree"
        ></view-editor>
      </div>
      <dv-sidebar
        v-if="batchOptStatus"
        :theme-info="'light'"
        :title="t('visualization.batch_style_set')"
        :width="280"
        aside-position="right"
        class="left-sidebar"
        :side-name="'batchOpt'"
      >
        <chart-style-batch-set></chart-style-batch-set>
      </dv-sidebar>
      <dv-sidebar
        v-if="hiddenListStatus"
        :theme-info="'light'"
        :title="t('visualization.hidden_components')"
        :width="280"
        aside-position="right"
        class="left-sidebar"
      >
        <DashboardHiddenComponent @cancel-hidden="cancelHidden"></DashboardHiddenComponent>
      </dv-sidebar>
    </el-container>
  </div>
  <MobileConfigPanel
    @pcMode="mobileConfig = false"
    v-else-if="loadFinish && mobileConfig"
  ></MobileConfigPanel>
  <XpackComponent
    jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvTmV3V2luZG93SGFuZGxlcg=="
    @loaded="XpackLoaded"
    @load-fail="XpackLoaded"
  />
  <xpack-component jsname="L2NvbXBvbmVudC90aHJlc2hvbGQtd2FybmluZy9UaHJlc2hvbGREaWFsb2c=" />
  <canvas-cache-dialog ref="canvasCacheOutRef" @doUseCache="doUseCache"></canvas-cache-dialog>
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

  &.dataease-w-h {
    height: 100%;
    width: 100%;
    .dv-layout-container {
      height: calc(100% - @top-bar-height);
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
  :deep(.editor-light) {
    border: 0 !important;
  }
}
</style>
