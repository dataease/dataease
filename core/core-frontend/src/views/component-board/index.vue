<script setup lang="ts">
// 核心Vue模块
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import router from '@/router'

// 状态管理相关
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'

// UI组件
import DbToolbar from '@/components/component-board/DbToolbar.vue'
import CanvasCacheDialog from '@/components/visualization/CanvasCacheDialog.vue'
import DeDataSider from '@/views/component-board/dataSider.vue'
import DeChartSider from '@/views/component-board/chartSider.vue'
import DeMain from '@/views/component-board/main.vue'
import { XpackComponent } from '@/components/plugin'

// API和工具函数
import { getDatasetTree } from '@/api/dataset'
import { watermarkFind } from '@/api/watermark'
import { recoverToPublished } from '@/api/visualization/dataVisualization'
import { decompressionPre, initCanvasData, onInitReady } from '@/utils/canvasUtils'
import { check, compareStorage } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
import { deepCopy } from '@/utils/utils'
import eventBus from '@/utils/eventBus'
import { guid } from '@/views/visualized/data/dataset/form/util.js'

// 第三方库
import { cloneDeep } from 'lodash-es'

// 类型导入
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'

import { findNewComponentFromList } from '@/custom-component/component-list' // 左侧列表数据
import { getCanvasStyle, syncShapeItemStyle } from '@/utils/style'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'

// 状态管理初始化
const interactiveStore = interactiveStoreWithOut()
const embeddedStore = useEmbedded()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const requestStore = useRequestStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const appStore = useAppStoreWithOut()

// 工具函数和hooks
const { wsCache } = useCache()

// 响应式状态
const {
  curComponent,
  canvasStyleData,
  canvasViewInfo,
  editMode,
  batchOptStatus,
  hiddenListStatus,
  dvInfo,
  curOriginThemes
} = storeToRefs(dvMainStore)

// 本地状态
const canvasCacheOutRef = ref(null)
const deCanvasRef = ref(null)
const dataInitState = ref(false)
const mobileConfig = ref(false)
const loadFinish = ref(false)
const newWindowFromDiv = ref(false)
let p = null

// 共享状态
const state = reactive({
  datasetTree: [],
  sourcePid: null,
  canvasId: 'canvas-main',
  opt: null,
  resourceId: null
})
// 计算属性
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

// 方法
const initDataset = () => {
  getDatasetTree({}).then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}
const eventCheck = e => {
  if (e.key === 'panel-weight' && !compareStorage(e.oldValue, e.newValue)) {
    const resourceId = embeddedStore.resourceId || router.currentRoute.value.query.resourceId
    const opt = embeddedStore.opt || router.currentRoute.value.query.opt
    if (!(opt && opt === 'create')) {
      check(wsCache.get('panel-weight'), resourceId as string, 4)
    }
  }
}
const checkPer = async resourceId => {
  if (!window.DataEaseBi || !resourceId) {
    return true
  }
  const request = { busiFlag: 'dashboard', resourceTable: 'core' }
  await interactiveStore.setInteractive(request)
  return check(wsCache.get('panel-weight'), resourceId, 4)
}

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
    initLocalCanvasData(()=>{})
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

// 通过实时监听的方式直接添加组件
const handleNewFromCanvasMain = newComponentInfo => {
  const { componentName, innerType, staticMap } = newComponentInfo
  if (componentName) {
    const component = findNewComponentFromList(componentName, innerType, curOriginThemes, staticMap)
    syncShapeItemStyle(component, 300, 300)
    component.id = guid()
    // component.y = undefined
    // component.x = cyGridster.value.findPositionX(component)
    dvMainStore.addComponent({
      component: component,
      index: undefined
    })
    adaptCurThemeCommonStyle(component)
    // nextTick(() => {
    //   cyGridster.value.addItemBox(component) //在适当的时候初始化布局组件
    //   nextTick(() => {
    //     scrollTo(component.y)
    //   })
    // })
    snapshotStore.recordSnapshotCacheWithPositionChange('renderChart', component.id)
  }
}

onMounted(async () => {
  document.body.style.overflow = 'hidden'
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
  const templateParams = embeddedStore.templateParams || router.currentRoute.value.query.templateParams
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
    // 初始化数据
    handleNewFromCanvasMain({ componentName: 'UserView', innerType: 'table-info' })
  }
})

onUnmounted(() => {
  document.body.style.overflow = ''
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
    <db-toolbar @recoverToPublished="doRecoverToPublished" />
    <el-container
      class="dv-layout-container"
      :class="{ 'preview-content': editMode === 'preview' }"
      element-loading-background="rgba(0, 0, 0, 0)"
    >
      <de-data-sider :themes="'light'" :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"></de-data-sider>
      <de-chart-sider :themes="'light'" :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"></de-chart-sider>
      <de-main :themes="'light'" :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"></de-main>
    </el-container>
  </div>
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
