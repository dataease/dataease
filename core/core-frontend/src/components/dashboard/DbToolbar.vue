<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import eventBus from '@/utils/eventBus'
import { useEmbedded } from '@/store/modules/embedded'
import { deepCopy } from '@/utils/utils'
import { nextTick, reactive, ref, computed, toRefs } from 'vue'
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
import { canvasSave, initCanvasData } from '@/utils/canvasUtils'
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

const props = defineProps({
  createType: {
    type: String,
    default: 'create'
  }
})

const { createType } = toRefs(props)

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
  if (inputName.value.trim().length > 64 || inputName.value.trim().length < 2) {
    ElMessage.warning('名称字段长度2-64个字符')
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
  if (!dvInfo.value.id) {
    ElMessage.warning('请先保存当前页面')
    return
  }
  canvasSave(() => {
    const url = '#/preview?dvId=' + dvInfo.value.id
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
    saveCanvasWithCheck()
  }
}

const saveCanvasWithCheck = () => {
  if (userStore.getOid && wsCache.get('user.oid') && userStore.getOid !== wsCache.get('user.oid')) {
    ElMessageBox.confirm('已切换至新组织，无权保存其他组织的资源', {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: '关闭页面',
      cancelButtonText: '取消',
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
      resourceAppOpt.value.init(params)
    } else {
      const params = { name: dvInfo.value.name, leaf: true, id: dvInfo.value.pid }
      resourceGroupOpt.value.optInit('leaf', params, 'newLeaf', true)
      return
    }
  }
  saveResource()
}

const saveResource = () => {
  wsCache.delete('DE-DV-CATCH-' + dvInfo.value.id)
  if (styleChangeTimes.value > 0) {
    dvMainStore.matrixSizeAdaptor()
    queryList.value.forEach(ele => {
      useEmitt().emitter.emit(`updateQueryCriteria${ele.id}`)
    })
    try {
      canvasSave(() => {
        snapshotStore.resetStyleChangeTimes()
        ElMessage.success('保存成功')
        window.history.pushState({}, '', `#/dashboard?resourceId=${dvInfo.value.id}`)
        if (appData.value) {
          initCanvasData(dvInfo.value.id, 'dashboard', () => {
            useEmitt().emitter.emit('refresh-dataset-selector')
            useEmitt().emitter.emit('calcData-all')
            resourceAppOpt.value.close()
            dvMainStore.setAppDataInfo(null)
            snapshotStore.resetSnapshot()
          })
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
    ElMessageBox.confirm('当前的更改尚未保存，确定退出吗？', {
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
  window.open(url, '_self')
}

const multiplexingCanvasOpen = () => {
  multiplexingRef.value.dialogInit()
}

eventBus.on('preview', previewInner)
eventBus.on('save', saveCanvasWithCheck)
eventBus.on('clearCanvas', clearCanvas)

const openDataBoardSetting = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
}

const openMobileSetting = () => {
  if (!dvInfo.value.id || dvInfo.value.dataState === 'prepare') {
    ElMessage.warning('请先保存当前页面')
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
  dvMainStore.setBatchOptStatus(value)
}

const openOuterParamsSet = () => {
  if (componentData.value.length === 0) {
    ElMessage.warning('当前仪表板为空，请先添加组件')
    return
  }
  if (!dvInfo.value.id) {
    ElMessage.warning('请先保存当前页面')
    return
  }
  outerParamsSetRef.value.optInit()
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
    ElMessage.success('保存成功')
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
  snapshotStore.recordSnapshotCache()
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
          <Icon class="toolbar-icon" name="icon_left_outlined" />
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
                <Icon name="icon_undo_outlined"></Icon>
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
                <Icon name="icon_redo_outlined"></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </div>
        <div class="left-area" v-if="batchOptStatus">
          <el-col class="adapt-count">
            <span>已选 {{ curBatchOptComponents.length }} 项</span>
          </el-col>
        </div>
        <div class="middle-area" v-if="!batchOptStatus && !linkageSettingStatus">
          <component-group
            :base-width="410"
            :show-split-line="true"
            is-label
            :icon-name="'dv-view'"
            themes="light"
            title="图表"
          >
            <user-view-group themes="light" :dv-model="dvModel"></user-view-group>
          </component-group>
          <component-group
            :base-width="115"
            :show-split-line="true"
            is-label
            themes="light"
            icon-name="dv-filter"
            title="查询组件"
          >
            <query-group themes="light" :dv-model="dvModel"></query-group>
          </component-group>
          <component-group
            is-label
            themes="light"
            :base-width="115"
            icon-name="dv-text"
            title="富文本"
          >
            <text-group themes="light" :dv-model="dvModel"></text-group>
          </component-group>
          <component-group
            is-label
            themes="light"
            placement="bottom"
            :base-width="315"
            icon-name="dv-media"
            title="媒体"
          >
            <media-group themes="light" :dv-model="dvModel"></media-group>
          </component-group>
          <component-group themes="light" is-label :base-width="115" icon-name="dv-tab" title="Tab">
            <tabs-group themes="light" :dv-model="dvModel"></tabs-group>
          </component-group>
          <component-group
            themes="light"
            show-split-line
            is-label
            :base-width="115"
            icon-name="dv-more-com"
            title="更多"
          >
            <db-more-com-group themes="light" :dv-model="dvModel"></db-more-com-group>
          </component-group>
          <component-button-label
            icon-name="icon_copy_filled"
            title="复用"
            is-label
            @customClick="multiplexingCanvasOpen"
          ></component-button-label>
        </div>
      </template>

      <div class="right-area" v-if="!batchOptStatus && !linkageSettingStatus">
        <template v-if="editMode !== 'preview'">
          <el-tooltip effect="dark" content="外部参数设置" placement="bottom">
            <component-button
              tips="外部参数设置"
              @custom-click="openOuterParamsSet"
              icon-name="icon_params_setting"
            />
          </el-tooltip>
          <el-tooltip effect="dark" content="批量操作" placement="bottom">
            <component-button
              tips="批量操作"
              @custom-click="batchOptStatusChange(true)"
              icon-name="dv-batch"
            />
          </el-tooltip>

          <el-tooltip effect="dark" content="仪表板配置" placement="bottom">
            <component-button
              tips="仪表板配置"
              @custom-click="openDataBoardSetting"
              icon-name="dv-dashboard"
            />
          </el-tooltip>
          <div class="divider"></div>
          <el-tooltip :offset="14" effect="dark" content="切换至移动端布局" placement="bottom">
            <component-button
              tips="切换至移动端布局"
              @custom-click="openMobileSetting"
              icon-name="icon_phone_outlined"
            />
          </el-tooltip>
        </template>

        <el-dropdown v-if="editMode === 'edit'" trigger="hover">
          <el-button class="preview-button" style="float: right; margin-right: 12px">
            预览
          </el-button>
          <template #dropdown>
            <el-dropdown-menu class="drop-style">
              <el-dropdown-item @click="previewInner">
                <el-icon style="margin-right: 8px; font-size: 16px">
                  <Icon name="icon_pc_fullscreen" />
                </el-icon>
                全屏预览
              </el-dropdown-item>
              <el-dropdown-item @click="previewOuter()">
                <el-icon style="margin-right: 8px; font-size: 16px">
                  <Icon name="dv-preview-outer" />
                </el-icon>
                新页面预览
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
          编辑
        </el-button>

        <el-button
          v-if="editMode === 'edit' || editMode === 'preview'"
          :disabled="styleChangeTimes < 1"
          @click="saveCanvasWithCheck()"
          style="float: right; margin-right: 12px"
          type="primary"
        >
          保存
        </el-button>
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
          复制</el-button
        >

        <el-button
          text
          icon="Delete"
          class="custom-normal-button"
          @click="batchDelete"
          :disabled="curBatchOptComponents.length === 0"
          style="float: right; margin-right: 12px"
        >
          删除</el-button
        >

        <el-button @click="saveBatchChange" style="float: right; margin-right: 12px" type="primary"
          >完成</el-button
        >
      </div>

      <div class="right-area full-area" v-if="linkageSettingStatus">
        <el-button
          class="custom-normal-button"
          @click="cancelLinkageSetting()"
          style="float: right; margin-right: 12px"
        >
          取消</el-button
        >
        <el-button
          @click="saveLinkageSetting"
          style="float: right; margin-right: 12px"
          type="primary"
          >确定</el-button
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
  width: 120px;
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
  border-color: #a6a6a6;
  color: #ffffff;
  &:hover {
    color: #ffffff;
    background-color: rgba(255, 255, 255, 0.05);
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
