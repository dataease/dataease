<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import eventBus from '@/utils/eventBus'
import { ref, nextTick, computed, toRefs, onMounted } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useAppStoreWithOut } from '@/store/modules/app'
import { storeToRefs } from 'pinia'
import Icon from '../icon-custom/src/Icon.vue'
import ComponentGroup from '@/components/visualization/ComponentGroup.vue'
import UserViewGroup from '@/custom-component/component-group/UserViewGroup.vue'
import MediaGroup from '@/custom-component/component-group/MediaGroup.vue'
import TextGroup from '@/custom-component/component-group/TextGroup.vue'
import CommonGroup from '@/custom-component/component-group/CommonGroup.vue'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import { canvasSave, initCanvasData } from '@/utils/canvasUtils'
import { changeSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import MoreComGroup from '@/custom-component/component-group/MoreComGroup.vue'
import { XpackComponent } from '@/components/plugin'
import { useCache } from '@/hooks/web/useCache'
import QueryGroup from '@/custom-component/component-group/QueryGroup.vue'
import ComponentButton from '@/components/visualization/ComponentButton.vue'
import OuterParamsSet from '@/components/visualization/OuterParamsSet.vue'
import MultiplexingCanvas from '@/views/common/MultiplexingCanvas.vue'
import ComponentButtonLabel from '@/components/visualization/ComponentButtonLabel.vue'
import DeFullscreen from '@/components/visualization/common/DeFullscreen.vue'
import DeAppApply from '@/views/common/DeAppApply.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useUserStoreWithOut } from '@/store/modules/user'
let nameEdit = ref(false)
let inputName = ref('')
let nameInput = ref(null)
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { styleChangeTimes, snapshotIndex } = storeToRefs(snapshotStore)
const resourceGroupOpt = ref(null)
const resourceAppOpt = ref(null)
const dvToolbarMain = ref(null)
const { componentData, canvasStyleData, canvasViewInfo, dvInfo, editMode, appData } =
  storeToRefs(dvMainStore)
let scaleEdit = 100
const { wsCache } = useCache('localStorage')
const dvModel = 'dataV'
const outerParamsSetRef = ref(null)
const fullScreeRef = ref(null)
const userStore = useUserStoreWithOut()

const props = defineProps({
  createType: {
    type: String,
    default: 'create'
  }
})

const { createType } = toRefs(props)

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
  snapshotStore.undo()
}

const redo = () => {
  snapshotStore.redo()
}

const preview = () => {
  //根据当前宽度变更画布scale 同时记录变更之前
  dvMainStore.setEditMode('preview')
  nextTick(() => {
    scaleEdit = canvasStyleData.value.scale
    const newScale = getFullScale()
    changeSizeWithScale(newScale)
  })
}

const edit = () => {
  dvMainStore.setEditMode('edit')
  changeSizeWithScale(scaleEdit)
}

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
    }
    return
  }
  saveResource()
}

const saveResource = () => {
  if (styleChangeTimes.value > 0) {
    eventBus.emit('hideArea-canvas-main')
    nextTick(() => {
      canvasSave(() => {
        snapshotStore.resetStyleChangeTimes()
        wsCache.delete('DE-DV-CATCH-' + dvInfo.value.id)
        ElMessage.success('保存成功')
        window.history.pushState({}, '', `#/dvCanvas?dvId=${dvInfo.value.id}`)
        if (appData.value) {
          initCanvasData(dvInfo.value.id, 'dataV', () => {
            useEmitt().emitter.emit('refresh-dataset-selector')
            resourceAppOpt.value.close()
            dvMainStore.setAppDataInfo(null)
            useEmitt().emitter.emit('calcData-all')
            snapshotStore.resetSnapshot()
          })
        }
      })
    })
  }
}

const clearCanvas = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setComponentData([])
  snapshotStore.recordSnapshotCache('renderChart')
}

const editCanvasName = () => {
  nameEdit.value = true
  inputName.value = dvInfo.value.name
  nextTick(() => {
    nameInput.value.focus()
  })
}

const backToMain = () => {
  let url = '#/screen/index'
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
const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)

const backHandler = (url: string) => {
  if (isEmbedded.value) {
    embeddedStore.clearState()
    useEmitt().emitter.emit('changeCurrentComponent', 'ScreenPanel')
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
  dvMainStore.canvasStateChange({ key: 'curPointArea', value: 'base' })
  wsCache.delete('DE-DV-CATCH-' + dvInfo.value.id)
  window.open(url, '_self')
}
const openHandler = ref(null)

const onDvNameChange = () => {
  snapshotStore.recordSnapshotCache()
}

const getFullScale = () => {
  const curWidth = dvToolbarMain.value.clientWidth
  return (curWidth * 100) / canvasStyleData.value.width
}
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const multiplexingRef = ref(null)

eventBus.on('preview', preview)
eventBus.on('save', saveCanvasWithCheck)
eventBus.on('clearCanvas', clearCanvas)

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

const multiplexingCanvasOpen = () => {
  multiplexingRef.value.dialogInit('dataV')
}

const fullScreenPreview = () => {
  dvMainStore.canvasStateChange({ key: 'curPointArea', value: 'base' })
  fullScreeRef.value.toggleFullscreen()
}
</script>

<template>
  <div class="toolbar-main" ref="dvToolbarMain">
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
        <el-icon class="custom-el-icon back-icon" @click="backToMain()">
          <Icon class="toolbar-icon" name="icon_left_outlined" />
        </el-icon>
        <div class="left-area">
          <span id="dv-canvas-name" class="name-area" @dblclick="editCanvasName">
            {{ dvInfo.name }}
          </span>
          <div class="opt-area">
            <el-tooltip effect="ndark" :content="$t('visualization.undo')" placement="bottom">
              <el-icon
                class="toolbar-hover-icon"
                :class="{ 'toolbar-icon-disabled': snapshotIndex < 1 }"
                @click="undo()"
              >
                <Icon name="icon_undo_outlined"></Icon>
              </el-icon>
            </el-tooltip>
            <el-tooltip effect="ndark" :content="$t('commons.reduction')" placement="bottom">
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
        <div class="middle-area">
          <component-group
            show-split-line
            is-label
            :base-width="410"
            icon-name="dv-view"
            title="图表"
          >
            <user-view-group></user-view-group>
          </component-group>
          <component-group
            :base-width="115"
            :show-split-line="true"
            is-label
            icon-name="dv-filter"
            title="查询组件"
          >
            <query-group :dv-model="dvModel"></query-group>
          </component-group>
          <component-group is-label :base-width="215" icon-name="dv-text" title="文本">
            <text-group></text-group>
          </component-group>
          <component-group
            is-label
            placement="bottom"
            :base-width="315"
            icon-name="dv-media"
            title="媒体"
          >
            <media-group></media-group>
          </component-group>
          <component-group is-label :base-width="215" icon-name="dv-more-com" title="更多">
            <more-com-group></more-com-group>
          </component-group>
          <component-group
            is-label
            :base-width="410"
            icon-name="dv-material"
            :show-split-line="true"
            title="素材"
          >
            <common-group></common-group>
          </component-group>
          <component-button-label
            icon-name="icon_copy_filled"
            title="复用"
            is-label
            @customClick="multiplexingCanvasOpen"
          ></component-button-label>
        </div>
      </template>
      <div class="right-area">
        <el-tooltip effect="dark" content="外部参数设置" placement="bottom">
          <component-button
            v-show="editMode === 'edit'"
            tips="外部参数设置"
            @custom-click="openOuterParamsSet"
            icon-name="icon_params_setting"
          />
        </el-tooltip>
        <div v-show="editMode === 'edit'" class="divider"></div>
        <el-button
          v-if="editMode === 'preview'"
          icon="EditPen"
          @click="edit()"
          class="preview-button"
          type="primary"
        >
          编辑
        </el-button>
        <el-button v-else class="preview-button" @click="fullScreenPreview" style="float: right">
          预览
        </el-button>
        <el-button
          @click="saveCanvasWithCheck()"
          :disabled="styleChangeTimes < 1"
          style="float: right; margin-right: 12px"
          type="primary"
        >
          保存
        </el-button>
      </div>
    </div>
    <Teleport v-if="nameEdit" :to="'#dv-canvas-name'">
      <input
        @keydown.stop
        @keyup.stop
        @change="onDvNameChange"
        ref="nameInput"
        minlength="2"
        maxlength="64"
        v-model="inputName"
        @blur="closeEditCanvasName"
      />
    </Teleport>

    <de-resource-group-opt
      @finish="resourceOptFinish"
      cur-canvas-type="dataV"
      ref="resourceGroupOpt"
    />
    <de-app-apply
      ref="resourceAppOpt"
      :component-data="componentData"
      :dv-info="dvInfo"
      :canvas-view-info="canvasViewInfo"
      cur-canvas-type="dataV"
      @saveAppCanvas="saveCanvasWithCheck"
    ></de-app-apply>
  </div>
  <de-fullscreen ref="fullScreeRef" show-position="dvEdit"></de-fullscreen>
  <multiplexing-canvas ref="multiplexingRef"></multiplexing-canvas>
  <outer-params-set ref="outerParamsSetRef"> </outer-params-set>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
</template>

<style lang="less" scoped>
.toolbar-main {
  position: relative;
}
.preview-state-head {
  height: 0px !important;
  overflow: hidden;
  padding: 0;
  margin: 0;
}
.edit-button {
  right: 10px;
  top: 10px;
  position: absolute;
  z-index: 10;
}
.toolbar {
  height: @top-bar-height;
  white-space: nowrap;
  overflow-x: auto;
  background: #1a1a1a;
  color: #ffffff;
  box-shadow: 0px 2px 4px 0px rgba(31, 35, 41, 0.12);
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
      color: @dv-canvas-main-font-color;
      input {
        position: absolute;
        left: 0;
        width: 100%;
        color: @dv-canvas-main-font-color;
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
  background-color: transparent;
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

.divider {
  background: #ffffff4d;
  width: 1px;
  height: 18px;
  margin-right: 20px;
  margin-left: 10px;
}
</style>
