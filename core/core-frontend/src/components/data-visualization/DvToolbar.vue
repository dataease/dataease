<script setup lang="ts">
import dvFilter from '@/assets/svg/dv-filter.svg'
import dvMaterial from '@/assets/svg/dv-material.svg'
import dvMedia from '@/assets/svg/dv-media.svg'
import dvMoreCom from '@/assets/svg/dv-more-com.svg'
import dvTab from '@/assets/svg/dv-tab.svg'
import dvText from '@/assets/svg/dv-text.svg'
import dvView from '@/assets/svg/dv-view.svg'
import icon_params_setting from '@/assets/svg/icon_params_setting.svg'
import icon_copy_filled from '@/assets/svg/icon_copy_filled.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_undo_outlined from '@/assets/svg/icon_undo_outlined.svg'
import icon_redo_outlined from '@/assets/svg/icon_redo_outlined.svg'
import dvRecoverOutlined from '@/assets/svg/dv-recover_outlined.svg'
import dvCancelPublish from '@/assets/svg/icon_undo_outlined.svg'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import eventBus from '@/utils/eventBus'
import { ref, nextTick, computed, onBeforeUnmount, onMounted } from 'vue'
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
import {
  canvasSave,
  canvasSaveWithParams,
  checkCanvasChangePre,
  findAllViewsId,
  initCanvasData
} from '@/utils/canvasUtils'
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
import TabsGroup from '@/custom-component/component-group/TabsGroup.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { updatePublishStatus } from '@/api/visualization/dataVisualization'

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
const { t } = useI18n()
const emits = defineEmits(['recoverToPublished'])

defineProps({
  createType: {
    type: String,
    default: 'create'
  }
})
const closeEditCanvasName = () => {
  nameEdit.value = false
  if (!inputName.value || !inputName.value.trim()) {
    return
  }
  if (inputName.value.trim() === dvInfo.value.name) {
    return
  }
  if (inputName.value.trim().length > 64 || inputName.value.trim().length < 1) {
    ElMessage.warning('名称字段长度1-64个字符')
    editCanvasName()
    return
  }
  dvInfo.value.name = inputName.value
  inputName.value = ''
}

const recoverToPublished = () => {
  emits('recoverToPublished')
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
    saveCanvasWithCheck(param.withPublish, param.status)
  }
}

const saveCanvasWithCheck = (withPublish = false, status?) => {
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
      nextTick(() => {
        resourceAppOpt.value.init(params)
      })
    } else {
      const params = {
        name: dvInfo.value.name,
        leaf: true,
        id: dvInfo.value.pid || '0'
      }
      resourceGroupOpt.value.optInit('leaf', params, 'newLeaf', true, { withPublish, status })
    }
    return
  }
  checkCanvasChangePre(() => {
    saveResource({ withPublish, status })
  })
}

const saveResource = (checkParams?) => {
  if (styleChangeTimes.value > 0 || checkParams.withPublish) {
    eventBus.emit('hideArea-canvas-main')
    nextTick(() => {
      canvasSaveWithParams(checkParams, () => {
        snapshotStore.resetStyleChangeTimes()
        wsCache.delete('DE-DV-CATCH-' + dvInfo.value.id)
        let url = window.location.href
        url = url.replace(/(#\/[^?]*)(?:\?[^#]*)?/, `$1?dvId=${dvInfo.value.id}`)
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
          initCanvasData(dvInfo.value.id, { busiFlag: 'dataV', resourceTable: 'snapshot' }, () => {
            useEmitt().emitter.emit('refresh-dataset-selector')
            resourceAppOpt.value.close()
            dvMainStore.setAppDataInfo(null)
            useEmitt().emitter.emit('calcData-all')
            snapshotStore.resetSnapshot()
          })
        }
        if (checkParams.withPublish) {
          publishStatusChange(checkParams.status)
        } else {
          ElMessage.success(t('commons.save_success'))
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
    ElMessageBox.confirm(t('visualization.change_save_tips'), {
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
  wsCache.set('dv-info-id', dvInfo.value.id)
  if (!!history.state.back) {
    history.back()
  } else {
    window.open(url, '_self')
  }
}
const openHandler = ref(null)

const onDvNameChange = () => {
  snapshotStore.recordSnapshotCache('onDvNameChange')
}

const getFullScale = () => {
  const curWidth = dvToolbarMain.value.clientWidth
  return (curWidth * 100) / canvasStyleData.value.width
}
const appStore = useAppStoreWithOut()
const multiplexingRef = ref(null)

onMounted(() => {
  eventBus.on('preview', preview)
  eventBus.on('save', saveCanvasWithCheck)
  eventBus.on('clearCanvas', clearCanvas)
})

onBeforeUnmount(() => {
  eventBus.off('preview', preview)
  eventBus.off('save', saveCanvasWithCheck)
  eventBus.off('clearCanvas', clearCanvas)
  dvMainStore.setAppDataInfo(null)
})

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

const multiplexingCanvasOpen = () => {
  multiplexingRef.value.dialogInit('dataV')
}

const publishStatusChange = status => {
  const targetViewIds = []
  findAllViewsId(componentData.value, targetViewIds)
  // do update
  updatePublishStatus({
    id: dvInfo.value.id,
    name: dvInfo.value.name,
    mobileLayout: dvInfo.value.mobileLayout,
    status,
    activeViewIds: targetViewIds,
    type: 'dataV'
  }).then(() => {
    dvMainStore.updateDvInfoCall(status)
    status
      ? ElMessage.success(t('visualization.published_success'))
      : ElMessage.success(t('visualization.cancel_publish_tips'))
  })
}

const isIframe = computed(() => appStore.getIsIframe)
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
          <Icon name="icon_left_outlined">
            <icon_left_outlined class="svg-icon toolbar-icon" />
          </Icon>
        </el-icon>
        <div class="left-area">
          <span id="dv-canvas-name" class="name-area" @dblclick="editCanvasName">
            {{ dvInfo.name }}
          </span>
          <div class="opt-area">
            <el-tooltip effect="light" :content="$t('visualization.undo')" placement="bottom">
              <el-icon
                class="toolbar-hover-icon"
                :class="{ 'toolbar-icon-disabled': snapshotIndex < 1 }"
                @click="undo()"
              >
                <Icon name="icon_undo_outlined">
                  <icon_undo_outlined class="svg-icon" />
                </Icon>
              </el-icon>
            </el-tooltip>
            <el-tooltip effect="light" :content="$t('commons.reduction')" placement="bottom">
              <el-icon
                class="toolbar-hover-icon opt-icon-redo"
                :class="{
                  'toolbar-icon-disabled': snapshotIndex === snapshotStore.snapshotData.length - 1
                }"
                @click="redo()"
              >
                <Icon name="icon_redo_outlined">
                  <icon_redo_outlined class="svg-icon" />
                </Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </div>
        <div class="middle-area">
          <component-group
            show-split-line
            is-label
            :base-width="410"
            :icon-name="dvView"
            :title="t('visualization.view')"
          >
            <user-view-group></user-view-group>
          </component-group>
          <component-group
            :base-width="115"
            :show-split-line="true"
            is-label
            :icon-name="dvFilter"
            :title="t('visualization.query_component')"
          >
            <query-group :dv-model="dvModel"></query-group>
          </component-group>
          <component-group
            is-label
            :base-width="215"
            :icon-name="dvText"
            :title="t('visualization.text_html')"
          >
            <text-group></text-group>
          </component-group>
          <component-group
            is-label
            placement="bottom"
            :base-width="328"
            :icon-name="dvMedia"
            :title="t('visualization.media')"
          >
            <media-group></media-group>
          </component-group>
          <component-group is-label :base-width="115" :icon-name="dvTab" title="Tab">
            <tabs-group :dv-model="dvModel"></tabs-group>
          </component-group>
          <component-group
            is-label
            :base-width="215"
            :icon-name="dvMoreCom"
            :title="t('visualization.more')"
          >
            <more-com-group></more-com-group>
          </component-group>
          <component-group
            is-label
            :base-width="410"
            :icon-name="dvMaterial"
            :show-split-line="true"
            :title="t('visualization.source_material')"
          >
            <common-group></common-group>
          </component-group>
          <component-button-label
            :icon-name="icon_copy_filled"
            :title="t('visualization.multiplexing')"
            is-label
            @customClick="multiplexingCanvasOpen"
          ></component-button-label>
        </div>
      </template>
      <div class="right-area">
        <el-tooltip
          effect="dark"
          :content="t('visualization.external_parameter_settings')"
          placement="bottom"
        >
          <component-button
            v-show="editMode === 'edit'"
            :tips="t('visualization.external_parameter_settings')"
            @custom-click="openOuterParamsSet"
            :icon-name="icon_params_setting"
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
          {{ t('visualization.edit') }}
        </el-button>
        <el-button
          v-else-if="!isIframe"
          class="preview-button"
          @click="fullScreenPreview"
          style="float: right"
        >
          {{ t('visualization.preview') }}
        </el-button>
        <el-button
          @click="saveCanvasWithCheck()"
          :disabled="styleChangeTimes < 1"
          style="float: right; margin-right: 12px"
          type="primary"
        >
          {{ t('visualization.save') }}
        </el-button>
        <el-dropdown
          :disabled="dvInfo.status === 0"
          popper-class="menu-outer-dv_popper-toolbar"
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
                  <Icon name="icon_left_outlined">
                    <dv-recover-outlined class="svg-icon toolbar-icon" />
                  </Icon>
                </el-icon>
                {{ t('visualization.publish_recover') }}
              </el-dropdown-item>
              <el-dropdown-item
                @click.stop="publishStatusChange(0)"
                v-if="[1, 2].includes(dvInfo.status)"
              >
                <el-icon class="handle-icon">
                  <Icon name="icon_left_outlined">
                    <dv-cancel-publish class="svg-icon toolbar-icon" />
                  </Icon>
                </el-icon>
                {{ t('visualization.cancel_publish') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
  <outer-params-set ref="outerParamsSetRef"></outer-params-set>
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

<style lang="less">
.menu-outer-dv_popper-toolbar {
  border: 1px solid rgba(67, 67, 67, 1) !important;
  background-color: rgba(41, 41, 41, 1) !important;
  .ed-dropdown-menu {
    background-color: rgba(41, 41, 41, 1) !important;
  }
  .ed-dropdown-menu__item {
    color: rgba(235, 235, 235, 1) !important;
  }
  .handle-icon {
    color: rgba(166, 166, 166, 1) !important;
  }
}
</style>
