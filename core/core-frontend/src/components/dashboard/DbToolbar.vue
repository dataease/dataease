<script setup lang="ts">
import dvBatch from '@/assets/svg/dv-batch.svg'
import dvDashboard from '@/assets/svg/dv-dashboard.svg'
import dvHidden from '@/assets/svg/dv-hidden.svg'
import dvFilter from '@/assets/svg/dv-filter.svg'
import dvMedia from '@/assets/svg/dv-media.svg'
import dvMoreCom from '@/assets/svg/dv-more-com.svg'
import dvTab from '@/assets/svg/dv-tab.svg'
import dvText from '@/assets/svg/dv-text.svg'
import dvView from '@/assets/svg/dv-view.svg'
import icon_params_setting from '@/assets/svg/icon_params_setting.svg'
import icon_phone_outlined from '@/assets/svg/icon_phone_outlined.svg'
import icon_copy_filled from '@/assets/svg/icon_copy_filled.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_undo_outlined from '@/assets/svg/icon_undo_outlined.svg'
import icon_redo_outlined from '@/assets/svg/icon_redo_outlined.svg'
import icon_pc_fullscreen from '@/assets/svg/icon_pc_fullscreen.svg'
import dvPreviewOuter from '@/assets/svg/dv-preview-outer.svg'
import dvRecoverOutlined from '@/assets/svg/dv-recover_outlined.svg'
import dvCancelPublish from '@/assets/svg/icon_undo_outlined.svg'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import eventBus from '@/utils/eventBus'
import { useEmbedded } from '@/store/modules/embedded'
import { deepCopy } from '@/utils/utils'
import { nextTick, reactive, ref, computed, onBeforeUnmount, onMounted } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useAppStoreWithOut } from '@/store/modules/app'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import Icon from '../icon-custom/src/Icon.vue'
import ComponentGroup from '@/components/visualization/ComponentGroup.vue'
import UserViewGroup from '@/custom-component/component-group/UserViewGroup.vue'
import QueryGroup from '@/custom-component/component-group/QueryGroup.vue'
import MediaGroup from '@/custom-component/component-group/MediaGroup.vue'
import TextGroup from '@/custom-component/component-group/TextGroup.vue'
import ComponentButton from '@/components/visualization/ComponentButton.vue'
import ComponentButtonLabel from '@/components/visualization/ComponentButtonLabel.vue'
import MultiplexingCanvas from '@/views/common/MultiplexingCanvas.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { getPanelAllLinkageInfo, saveLinkage } from '@/api/visualization/linkage'
import { queryVisualizationJumpInfo } from '@/api/visualization/linkJump'
import {
  canvasSave,
  canvasSaveWithParams,
  checkCanvasChangePre,
  findAllViewsId,
  initCanvasData
} from '@/utils/canvasUtils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import TabsGroup from '@/custom-component/component-group/TabsGroup.vue'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import OuterParamsSet from '@/components/visualization/OuterParamsSet.vue'
import { XpackComponent } from '@/components/plugin'
import DbMoreComGroup from '@/custom-component/component-group/DbMoreComGroup.vue'
import { useCache } from '@/hooks/web/useCache'
import DeFullscreen from '@/components/visualization/common/DeFullscreen.vue'
import DeAppApply from '@/views/common/DeAppApply.vue'
import { useUserStoreWithOut } from '@/store/modules/user'
import { updatePublishStatus } from '@/api/visualization/dataVisualization'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const copyStore = copyStoreWithOut()
const { styleChangeTimes, snapshotIndex } = storeToRefs(snapshotStore)
const resourceAppOpt = ref(null)
const {
  linkageSettingStatus,
  curLinkageView,
  componentData,
  dvInfo,
  canvasViewInfo,
  editMode,
  batchOptStatus,
  targetLinkageInfo,
  curBatchOptComponents,
  appData
} = storeToRefs(dvMainStore)
const dvModel = 'dashboard'
const multiplexingRef = ref(null)
const fullScreeRef = ref(null)
let nameEdit = ref(false)
let inputName = ref('')
let nameInput = ref(null)
const state = reactive({
  preBatchComponentData: [],
  preBatchCanvasViewInfo: {}
})
const resourceGroupOpt = ref(null)
const outerParamsSetRef = ref(null)
const { wsCache } = useCache('localStorage')
const userStore = useUserStoreWithOut()
const isIframe = computed(() => appStore.getIsIframe)
const desktop = wsCache.get('app.desktop')
const emits = defineEmits(['recoverToPublished'])

defineProps({
  createType: {
    type: String,
    default: 'create'
  }
})

const editCanvasName = () => {
  nameEdit.value = true
  inputName.value = dvInfo.value.name
  nextTick(() => {
    nameInput.value.focus()
  })
}
const closeEditCanvasName = () => {
  nameEdit.value = false
  if (!inputName.value || !inputName.value.trim()) {
    return
  }
  if (inputName.value.trim() === dvInfo.value.name) {
    return
  }
  if (inputName.value.trim().length > 64 || inputName.value.trim().length < 1) {
    ElMessage.warning(t('components.length_1_64_characters'))
    editCanvasName()
    return
  }
  dvInfo.value.name = inputName.value
  inputName.value = ''
}

const undo = () => {
  if (snapshotIndex.value > 0) {
    snapshotStore.undo()
    eventBus.emit('matrix-canvasInit', false)
  }
}

const redo = () => {
  if (snapshotIndex.value !== snapshotStore.snapshotData.length - 1) {
    snapshotStore.redo()
    eventBus.emit('matrix-canvasInit', false)
  }
}

const previewInner = () => {
  fullScreeRef.value.toggleFullscreen()
}

const previewOuter = () => {
  if (!dvInfo.value.id || dvInfo.value.dataState === 'prepare') {
    ElMessage.warning(t('components.current_page_first'))
    return
  }
  canvasSave(() => {
    let url = '#/preview?dvId=' + dvInfo.value.id + '&ignoreParams=true&editPreview=true'
    if (embeddedStore.baseUrl) {
      url = `${embeddedStore.baseUrl}${url}`.replaceAll('\/\/#', '\/#')
    }
    const newWindow = window.open(url, '_blank')
    initOpenHandler(newWindow)
  })
}

const edit = () => {
  dvMainStore.setEditMode('edit')
}

const queryList = computed(() => {
  let arr = []
  componentData.value.forEach(com => {
    if (com.innerType === 'VQuery') {
      arr.push(com)
    }
    if ('DeTabs' === com.innerType) {
      com.propValue.forEach(itx => {
        arr = [...itx.componentData.filter(item => item.innerType === 'VQuery'), ...arr]
      })
    }
  })
  return arr
})

const resourceOptFinish = param => {
  if (param && param.opt === 'newLeaf') {
    dvInfo.value.dataState = 'ready'
    dvInfo.value.pid = param.pid
    dvInfo.value.name = param.name
    saveCanvasWithCheck(param.withPublish, param.status)
  }
}

const recoverToPublished = () => {
  emits('recoverToPublished')
}

const publishStatusChange = status => {
  const targetViewIds = []
  findAllViewsId(componentData.value, targetViewIds)
  // do update
  updatePublishStatus({
    id: dvInfo.value.id,
    name: dvInfo.value.name,
    mobileLayout: dvInfo.value.mobileLayout,
    activeViewIds: targetViewIds,
    status,
    type: 'dashboard'
  }).then(() => {
    dvMainStore.updateDvInfoCall(status)
    status
      ? ElMessage.success(t('visualization.published_success'))
      : ElMessage.success(t('visualization.cancel_publish_tips'))
  })
}

const saveCanvasWithCheck = (withPublish = false, status?) => {
  if (userStore.getOid && wsCache.get('user.oid') && userStore.getOid !== wsCache.get('user.oid')) {
    ElMessageBox.confirm(t('components.from_other_organizations'), {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: t('components.close_the_page'),
      cancelButtonText: t('common.cancel'),
      autofocus: false,
      showClose: false
    }).then(() => {
      window.close()
    })
    return
  }
  if (dvInfo.value.dataState === 'prepare') {
    if (appData.value) {
      // 应用保存
      const params = {
        base: {
          pid: '',
          name: dvInfo.value.name,
          datasetFolderPid: null,
          datasetFolderName: dvInfo.value.name
        },
        appData: appData.value
      }
      nextTick(() => {
        resourceAppOpt.value.init(params)
      })
    } else {
      const params = { name: dvInfo.value.name, leaf: true, id: dvInfo.value.pid || '0' }
      resourceGroupOpt.value.optInit('leaf', params, 'newLeaf', true, { withPublish, status })
      return
    }
  }
  checkCanvasChangePre(() => {
    saveResource({ withPublish, status })
  })
}

const saveResource = (checkParams?) => {
  wsCache.delete('DE-DV-CATCH-' + dvInfo.value.id)
  if (styleChangeTimes.value > 0 || checkParams.withPublish) {
    dvMainStore.matrixSizeAdaptor()
    queryList.value.forEach(ele => {
      useEmitt().emitter.emit(`updateQueryCriteria${ele.id}`)
    })
    try {
      canvasSaveWithParams(checkParams, () => {
        snapshotStore.resetStyleChangeTimes()
        let url = window.location.href
        url = url.replace(/(#\/[^?]*)(?:\?[^#]*)?/, `$1?resourceId=${dvInfo.value.id}`)
        if (!embeddedStore.baseUrl) {
          window.history.replaceState(
            {
              path: url
            },
            '',
            url
          )
        }
        if (appData.value) {
          initCanvasData(
            dvInfo.value.id,
            { busiFlag: 'dashboard', resourceTable: 'snapshot' },
            () => {
              useEmitt().emitter.emit('refresh-dataset-selector')
              useEmitt().emitter.emit('calcData-all')
              resourceAppOpt.value.close()
              dvMainStore.setAppDataInfo(null)
              snapshotStore.resetSnapshot()
            }
          )
        }
        if (checkParams.withPublish) {
          publishStatusChange(checkParams.status)
        } else {
          ElMessage.success(t('commons.save_success'))
        }
      })
    } catch (e) {
      console.error(e)
    }
  }
}

const clearCanvas = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setComponentData([])
  snapshotStore.recordSnapshotCache('renderChart')
}

const backToMain = () => {
  let url = '#/panel/index'
  if (dvInfo.value.id) {
    url = url + '?dvId=' + dvInfo.value.id
  }
  if (styleChangeTimes.value > 0) {
    ElMessageBox.confirm(t('components.sure_to_exit'), {
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      backHandler(url)
    })
  } else {
    backHandler(url)
  }
}
const embeddedStore = useEmbedded()

const backHandler = (url: string) => {
  if (isEmbedded.value) {
    embeddedStore.clearState()
    useEmitt().emitter.emit('changeCurrentComponent', 'DashboardPanel')
    return
  }
  if (window['dataease-embedded-host'] && openHandler?.value) {
    const pm = {
      methodName: 'embeddedInteractive',
      args: {
        eventName: 'de-dashboard-editor-back',
        args: 'Just a demo that descript dataease embedded interactive'
      }
    }
    openHandler.value.invokeMethod(pm)
    return
  }
  wsCache.delete('DE-DV-CATCH-' + dvInfo.value.id)
  wsCache.set('db-info-id', dvInfo.value.id)
  if (!!history.state.back) {
    history.back()
  } else {
    window.open(url, '_self')
  }
}

const multiplexingCanvasOpen = () => {
  multiplexingRef.value.dialogInit()
}
onMounted(() => {
  eventBus.on('preview', previewInner)
  eventBus.on('save', saveCanvasWithCheck)
  eventBus.on('clearCanvas', clearCanvas)
})
onBeforeUnmount(() => {
  eventBus.off('preview', previewInner)
  eventBus.off('save', saveCanvasWithCheck)
  eventBus.off('clearCanvas', clearCanvas)
  dvMainStore.setAppDataInfo(null)
})
const openDataBoardSetting = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setHiddenListStatus(false)
}

const openHiddenList = () => {
  dvMainStore.setHiddenListStatus()
}

const openMobileSetting = () => {
  if (!dvInfo.value.id || dvInfo.value.dataState === 'prepare') {
    ElMessage.warning(t('components.current_page_first'))
    return
  }
  useEmitt().emitter.emit('mobileConfig')
}

const batchDelete = () => {
  const componentDataTemp = deepCopy(componentData.value)
  componentDataTemp.forEach(component => {
    if (curBatchOptComponents.value.includes(component.id)) {
      eventBus.emit('removeMatrixItemById-' + component.canvasId, component.id)
    }
    if (component.component === 'DeTabs') {
      component.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          if (curBatchOptComponents.value.includes(tabComponent.id)) {
            eventBus.emit('removeMatrixItemById-' + tabComponent.canvasId, tabComponent.id)
          }
        })
      })
    }
  })
  nextTick(() => {
    saveBatchChange()
  })
}

const batchCopy = () => {
  const multiplexingComponents = {}
  componentData.value.forEach(component => {
    if (curBatchOptComponents.value.includes(component.id)) {
      multiplexingComponents[component.id] = component
    }
    if (component.component === 'DeTabs') {
      component.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          if (curBatchOptComponents.value.includes(tabComponent.id)) {
            multiplexingComponents[tabComponent.id] = tabComponent
          }
        })
      })
    }
  })
  copyStore.copyMultiplexingComponents(
    canvasViewInfo.value,
    multiplexingComponents,
    true,
    'batchOpt'
  )
  saveBatchChange()
}

const batchOptStatusChange = value => {
  if (value) {
    // 如果当前进入批量操作界面 提前保存镜像
    state.preBatchComponentData = deepCopy(componentData.value)
    state.preBatchCanvasViewInfo = deepCopy(canvasViewInfo.value)
  } else {
    state.preBatchComponentData = []
    state.preBatchCanvasViewInfo = {}
  }
  dvMainStore.setHiddenListStatus(false)
  dvMainStore.setBatchOptStatus(value)
}

const openOuterParamsSet = () => {
  if (componentData.value.length === 0) {
    ElMessage.warning(t('components.add_components_first'))
    return
  }
  if (!dvInfo.value.id || dvInfo.value.dataState === 'prepare') {
    ElMessage.warning(t('components.current_page_first'))
    return
  }
  //设置需要先触发保存
  canvasSave(() => {
    outerParamsSetRef.value.optInit()
  })
}

const saveBatchChange = () => {
  batchOptStatusChange(false)
}

const cancelLinkageSetting = () => {
  dvMainStore.clearLinkageSettingInfo()
}

const saveLinkageSetting = () => {
  // 字段检查
  for (const key in targetLinkageInfo.value) {
    let subCheckCount = 0
    const linkageInfo = targetLinkageInfo.value[key]
    const linkageFields = linkageInfo['linkageFields']
    if (linkageFields) {
      linkageFields.forEach(function (linkage) {
        if (!(linkage.sourceField && linkage.targetField)) {
          subCheckCount++
        }
      })
    }

    if (subCheckCount > 0) {
      ElMessage.error(
        t('visualization.datalist') +
          '【' +
          linkageInfo.targetViewName +
          '】' +
          t('visualization.exit_un_march_linkage_field')
      )
      return
    }
  }
  const request = {
    dvId: dvInfo.value.id,
    sourceViewId: curLinkageView.value.id,
    linkageInfo: targetLinkageInfo.value
  }
  saveLinkage(request).then(() => {
    ElMessage.success(t('save_success.common'))
    // 刷新联动信息
    getPanelAllLinkageInfo(dvInfo.value.id).then(rsp => {
      dvMainStore.setNowPanelTrackInfo(rsp.data)
    })
    cancelLinkageSetting()
    // 刷新跳转信息
    queryVisualizationJumpInfo(dvInfo.value.id).then(rsp => {
      dvMainStore.setNowPanelJumpInfo(rsp.data)
    })
  })
}

const onDvNameChange = () => {
  snapshotStore.recordSnapshotCache('onDvNameChange')
}
const appStore = useAppStoreWithOut()
const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)

const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}
</script>

<template>
  <div class="toolbar-main">
    <div class="toolbar">
      <template v-if="editMode === 'preview'">
        <div class="left-area">
          <span id="canvas-name" class="name-area" style="height: 100%; padding: 10px">
            {{ dvInfo.name }}
          </span>
        </div>
        <div class="middle-area"></div>
      </template>
      <template v-else>
        <el-icon v-if="!batchOptStatus" class="custom-el-icon back-icon" @click="backToMain()">
          <Icon name="icon_left_outlined"
            ><icon_left_outlined class="svg-icon toolbar-icon"
          /></Icon>
        </el-icon>
        <div class="left-area" v-if="editMode === 'edit' && !batchOptStatus">
          <span id="canvas-name" class="name-area" @dblclick="editCanvasName">
            {{ dvInfo.name }}
          </span>
          <div class="opt-area">
            <el-tooltip effect="dark" :content="$t('visualization.undo')" placement="bottom">
              <el-icon
                class="toolbar-hover-icon"
                :class="{ 'toolbar-icon-disabled': snapshotIndex < 1 }"
                :disabled="snapshotIndex < 1"
                @click="undo()"
              >
                <Icon name="icon_undo_outlined"><icon_undo_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>

            <el-tooltip effect="dark" :content="$t('commons.reduction')" placement="bottom">
              <el-icon
                class="toolbar-hover-icon opt-icon-redo"
                :class="{
                  'toolbar-icon-disabled': snapshotIndex === snapshotStore.snapshotData.length - 1
                }"
                @click="redo()"
              >
                <Icon name="icon_redo_outlined"><icon_redo_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </div>
        <div class="left-area" v-if="batchOptStatus">
          <el-col class="adapt-count">
            <span>{{ t('user.selection_info', [curBatchOptComponents.length]) }}</span>
          </el-col>
        </div>
        <div class="middle-area" v-if="!batchOptStatus && !linkageSettingStatus">
          <component-group
            :base-width="410"
            :show-split-line="true"
            is-label
            :icon-name="dvView"
            themes="light"
            :title="t('chart.datalist')"
          >
            <user-view-group themes="light" :dv-model="dvModel"></user-view-group>
          </component-group>
          <component-group
            :base-width="115"
            :show-split-line="true"
            is-label
            themes="light"
            :icon-name="dvFilter"
            :title="t('visualization.filter_component')"
          >
            <query-group themes="light" :dv-model="dvModel"></query-group>
          </component-group>
          <component-group
            is-label
            themes="light"
            :base-width="115"
            :icon-name="dvText"
            :title="t('components.rich_text')"
          >
            <text-group themes="light" :dv-model="dvModel"></text-group>
          </component-group>
          <component-group
            is-label
            themes="light"
            placement="bottom"
            :base-width="328"
            :icon-name="dvMedia"
            :title="t('components.media')"
          >
            <media-group themes="light" :dv-model="dvModel"></media-group>
          </component-group>
          <component-group themes="light" is-label :base-width="115" :icon-name="dvTab" title="Tab">
            <tabs-group themes="light" :dv-model="dvModel"></tabs-group>
          </component-group>
          <component-group
            themes="light"
            show-split-line
            is-label
            :base-width="115"
            :icon-name="dvMoreCom"
            :title="t('visualization.more')"
          >
            <db-more-com-group themes="light" :dv-model="dvModel"></db-more-com-group>
          </component-group>
          <component-button-label
            :icon-name="icon_copy_filled"
            :title="t('visualization.multiplexing')"
            is-label
            @customClick="multiplexingCanvasOpen"
          ></component-button-label>
        </div>
      </template>

      <div class="right-area" v-if="!batchOptStatus && !linkageSettingStatus">
        <template v-if="editMode !== 'preview'">
          <el-tooltip
            effect="dark"
            :content="t('visualization.outer_param_set')"
            placement="bottom"
          >
            <component-button
              :tips="t('visualization.outer_param_set')"
              @custom-click="openOuterParamsSet"
              :icon-name="icon_params_setting"
            />
          </el-tooltip>
          <el-tooltip effect="dark" :content="t('visualization.batch_opt')" placement="bottom">
            <component-button
              :tips="t('visualization.batch_opt')"
              @custom-click="batchOptStatusChange(true)"
              :icon-name="dvBatch"
            />
          </el-tooltip>

          <el-tooltip
            effect="dark"
            :content="t('components.dashboard_configuration')"
            placement="bottom"
          >
            <component-button
              :tips="t('components.dashboard_configuration')"
              @custom-click="openDataBoardSetting"
              :icon-name="dvDashboard"
            />
          </el-tooltip>
          <el-tooltip
            effect="dark"
            :content="t('visualization.hidden_components')"
            placement="bottom"
          >
            <component-button
              :tips="t('visualization.hidden_components')"
              @custom-click="openHiddenList"
              :icon-name="dvHidden"
            />
          </el-tooltip>
          <div class="divider"></div>
          <template v-if="!desktop">
            <el-tooltip
              :offset="14"
              effect="dark"
              :content="t('components.to_mobile_layout')"
              placement="bottom"
            >
              <component-button
                :tips="t('components.to_mobile_layout')"
                @custom-click="openMobileSetting"
                :icon-name="icon_phone_outlined"
              />
            </el-tooltip>
          </template>
        </template>

        <el-dropdown v-if="editMode === 'edit'" trigger="hover">
          <el-button class="preview-button" style="float: right; margin-right: 12px">
            {{ t('visualization.preview') }}
          </el-button>
          <template #dropdown>
            <el-dropdown-menu class="drop-style">
              <el-dropdown-item @click="previewInner" v-if="!isIframe">
                <el-icon style="margin-right: 8px; font-size: 16px">
                  <Icon name="icon_pc_fullscreen"><icon_pc_fullscreen class="svg-icon" /></Icon>
                </el-icon>
                {{ t('visualization.fullscreen_preview') }}
              </el-dropdown-item>
              <el-dropdown-item @click="previewOuter()">
                <el-icon style="margin-right: 8px; font-size: 16px">
                  <Icon><dvPreviewOuter class="svg-icon" /></Icon>
                </el-icon>
                {{ t('work_branch.new_page_preview') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button
          class="custom-normal-button"
          v-if="editMode === 'preview'"
          icon="EditPen"
          @click="edit()"
          type="primary"
        >
          {{ t('data_set.edit') }}
        </el-button>
        <template v-if="editMode === 'edit' || editMode === 'preview'">
          <el-button
            v-if="editMode === 'edit' || editMode === 'preview'"
            :disabled="styleChangeTimes < 1"
            @click="saveCanvasWithCheck()"
            style="float: right; margin-right: 12px"
            type="primary"
          >
            {{ t('data_set.save') }}
          </el-button>
          <el-dropdown
            :disabled="dvInfo.status === 0"
            popper-class="menu-outer-dv_popper"
            trigger="hover"
          >
            <el-button
              @click="saveCanvasWithCheck(true, 1)"
              style="float: right; margin: 0 12px 0 0"
              type="primary"
            >
              {{ t('visualization.publish') }}
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="recoverToPublished" v-if="dvInfo.status === 2">
                  <el-icon class="handle-icon">
                    <Icon name="icon_left_outlined"
                      ><dv-recover-outlined class="svg-icon toolbar-icon"
                    /></Icon>
                  </el-icon>
                  {{ t('visualization.publish_recover') }}
                </el-dropdown-item>
                <el-dropdown-item
                  @click="publishStatusChange(0)"
                  v-if="[1, 2].includes(dvInfo.status)"
                >
                  <el-icon class="handle-icon">
                    <Icon name="icon_left_outlined"
                      ><dv-cancel-publish class="svg-icon toolbar-icon"
                    /></Icon>
                  </el-icon>
                  {{ t('visualization.cancel_publish') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>

      <div class="right-area full-area" v-if="batchOptStatus">
        <el-button
          text
          icon="CopyDocument"
          class="custom-normal-button"
          @click="batchCopy"
          :disabled="curBatchOptComponents.length === 0"
          style="float: right; margin-right: 12px"
        >
          {{ t('data_set.copy') }}</el-button
        >

        <el-button
          text
          icon="Delete"
          class="custom-normal-button"
          @click="batchDelete"
          :disabled="curBatchOptComponents.length === 0"
          style="float: right; margin-right: 12px"
        >
          {{ t('data_set.delete') }}</el-button
        >

        <el-button
          @click="saveBatchChange"
          style="float: right; margin-right: 12px"
          type="primary"
          >{{ t('components.complete') }}</el-button
        >
      </div>

      <div class="right-area full-area" v-if="linkageSettingStatus">
        <el-button
          class="custom-normal-button"
          @click="cancelLinkageSetting()"
          style="float: right; margin-right: 12px"
        >
          {{ t('userimport.cancel') }}</el-button
        >
        <el-button
          @click="saveLinkageSetting"
          style="float: right; margin-right: 12px"
          type="primary"
          >{{ t('userimport.sure') }}</el-button
        >
      </div>
    </div>
    <Teleport v-if="nameEdit" :to="'#canvas-name'">
      <input
        @change="onDvNameChange"
        ref="nameInput"
        v-model="inputName"
        @blur="closeEditCanvasName"
      />
    </Teleport>

    <multiplexing-canvas ref="multiplexingRef"></multiplexing-canvas>
    <de-resource-group-opt
      @finish="resourceOptFinish"
      cur-canvas-type="dashboard"
      ref="resourceGroupOpt"
    />
    <outer-params-set ref="outerParamsSetRef"> </outer-params-set>
  </div>
  <de-fullscreen show-position="edit" ref="fullScreeRef"></de-fullscreen>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
  <de-app-apply
    ref="resourceAppOpt"
    :component-data="componentData"
    :dv-info="dvInfo"
    :canvas-view-info="canvasViewInfo"
    cur-canvas-type="dashboard"
    @saveAppCanvas="saveCanvasWithCheck"
  ></de-app-apply>
</template>

<style lang="less" scoped>
.group_icon + .ed-dropdown,
.group_icon + .ed-button {
  margin-left: 10px;
}
.drop-style {
  :deep(.ed-dropdown-menu__item) {
    padding: 5px 12px !important;
  }
  :deep(.ed-dropdown-menu__item:not(.is_disabled):focus) {
    color: inherit;
    background-color: rgba(31, 35, 41, 0.1);
  }
}
.full-area {
  flex: 1;
}
.edit-button {
  right: 10px;
  top: 10px;
  position: absolute;
  z-index: 10;
}
.toolbar-main {
  position: relative;
}
.preview-state-head {
  height: 0px !important;
  overflow: hidden;
  padding: 0;
  margin: 0;
}
.toolbar {
  height: @top-bar-height;
  white-space: nowrap;
  overflow-x: auto;
  background: #050e21;
  color: #ffffff;
  display: flex;
  transition: 0.5s;
  .back-icon {
    margin-left: 20px;
    margin-top: 22px;
    font-size: 20px;
  }
  .left-area {
    margin-top: 8px;
    margin-left: 14px;
    width: 300px;
    display: flex;
    flex-direction: column;
    .name-area {
      position: relative;
      line-height: 24px;
      height: 24px;
      font-size: 16px;
      width: 300px;
      overflow: hidden;
      cursor: pointer;
      input {
        position: absolute;
        left: 0;
        width: 100%;
        color: #fff;
        background-color: #050e21;
        outline: none;
        border: 1px solid #295acc;
        border-radius: 4px;
        padding: 0 4px;
        height: 100%;
      }
    }
    .opt-area {
      width: 300px;
      text-align: left;
      color: #a6a6a6;

      .opt-icon-redo {
        margin-left: 12px;
      }
    }
  }
  .middle-area {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .right-area {
    width: 400px;
    display: flex;
    align-items: center;
    justify-content: right;

    .divider {
      background: #ffffff4d;
      width: 1px;
      height: 18px;
      margin: 0 10px;
    }
  }
  .custom-el-icon {
    margin-left: 15px;
    color: #ffffff;
    cursor: pointer;
    vertical-align: -0.2em;
  }
  .toolbar-icon {
    width: 20px;
    height: 20px;
  }
}

.preview-button {
  border-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
  background-color: #050e21;
  &:hover,
  &:focus {
    background-color: #121a2c;
    border-color: #595f6b;
  }

  &:active {
    border-color: #616774;
    background-color: #1e2637;
  }
}
.custom-normal-button {
  background-color: transparent;
  border-color: #a6a6a6 !important;
  color: #ffffff !important;
  &:hover {
    color: #ffffff;
    background-color: #ffffff1a !important;
  }
  &:active {
    color: #ffffff;
    background-color: #ffffff33 !important;
  }
  &.is-disabled {
    color: var(--ed-button-disabled-text-color) !important;
  }
}

.adapt-count {
  color: #ffffff;
  margin-left: 10px;
  font-size: 14px;
  font-weight: 400;
  padding-top: 14px;
}
</style>
