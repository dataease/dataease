<script setup lang="ts">
import { ElMessage } from 'element-plus-secondary'
import { generateID } from '@/utils/generateID'
import toast from '@/utils/toast'
import { commonStyle, commonAttr } from '@/custom-component/component-list'
import eventBus from '@/utils/eventBus'
import { $ } from '@/utils/utils'
import { changeComponentSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { nextTick, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import Icon from '../icon-custom/src/Icon.vue'
import { update, save } from '@/api/visualization/dataVisualization'
import ComponentGroup from '@/components/visualization/ComponentGroup.vue'
import UserViewGroup from '@/custom-component/component-group/UserViewGroup.vue'
import QueryGroup from '@/custom-component/component-group/QueryGroup.vue'
import MediaGroup from '@/custom-component/component-group/MediaGroup.vue'
import TextGroup from '@/custom-component/component-group/TextGroup.vue'
import ComponentButton from '@/components/visualization/ComponentButton.vue'
import MultiplexingCanvas from '@/views/common/MultiplexingCanvas.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { getPanelAllLinkageInfo, saveLinkage } from '@/api/visualization/linkage'
import { queryVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { canvasSave } from '@/utils/canvasUtils'
const { t } = useI18n()
const isShowPreview = ref(false)
const isScreenshot = ref(false)
let timer = null
const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const {
  linkageSettingStatus,
  curLinkageView,
  curComponent,
  canvasStyleData,
  curComponentIndex,
  componentData,
  dvInfo,
  canvasViewInfo,
  editMode,
  batchOptStatus,
  targetLinkageInfo
} = storeToRefs(dvMainStore)
const { areaData } = storeToRefs(composeStore)
const dvModel = 'dashboard'
const multiplexingRef = ref(null)
let scale = ref(canvasStyleData.value.scale)
let nameEdit = ref(false)
let inputName = ref('')
let nameInput = ref(null)

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
  dvInfo.value.name = inputName.value
  inputName.value = ''
}

const lock = () => {
  lockStore.lock()
}

const unlock = () => {
  lockStore.unlock()
}

const compose = () => {
  composeStore.compose()
  snapshotStore.recordSnapshot('db-compose')
}

const decompose = () => {
  composeStore.decompose()
  snapshotStore.recordSnapshot('db-decompose')
}

const undo = () => {
  snapshotStore.undo()
}

const redo = () => {
  snapshotStore.redo()
}

const handleFileChange = e => {
  const file = e.target.files[0]
  if (!file.type.includes('image')) {
    toast('只能插入图片')
    return
  }

  const reader = new FileReader()
  reader.onload = res => {
    const fileResult = res.target.result
    const img = new Image()
    img.onload = () => {
      const component = {
        ...commonAttr,
        id: generateID(),
        component: 'Picture',
        label: '图片',
        icon: '',
        propValue: {
          url: fileResult,
          flip: {
            horizontal: false,
            vertical: false
          }
        },
        style: {
          ...commonStyle,
          top: 0,
          left: 0,
          width: img.width,
          height: img.height
        }
      }

      // 根据画面比例修改组件样式比例
      changeComponentSizeWithScale(component)
      dvMainStore.addComponent({ component: component, index: undefined })
      snapshotStore.recordSnapshot('db-handleFileChange')

      $('#input').setAttribute('type', 'text')
      $('#input').setAttribute('type', 'file')
    }

    if (typeof fileResult === 'string') {
      img.src = fileResult
    }
  }

  reader.readAsDataURL(file)
}

const preview = () => {
  dvMainStore.setEditMode('preview')
}

const edit = () => {
  dvMainStore.setEditMode('edit')
}

const saveCanvas = () => {
  dvMainStore.matrixSizeAdaptor()
  canvasSave(() => {
    ElMessage.success('保存成功')
  })
}

const clearCanvas = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setComponentData([])
  snapshotStore.recordSnapshot('db-clearCanvas')
}

const handlePreviewChange = () => {
  isShowPreview.value = false
  dvMainStore.setEditMode('edit')
}

const backToMain = () => {
  // window.opener.focus()
  window.opener.focus()
}

const multiplexingCanvasOpen = () => {
  multiplexingRef.value.dialogInit()
}

eventBus.on('preview', preview)
eventBus.on('save', saveCanvas)
eventBus.on('clearCanvas', clearCanvas)

const openDataBoardSetting = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
}

const batchOptStatusChange = value => {
  dvMainStore.setBatchOptStatus(value)
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
  saveLinkage(request).then(rsp => {
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
</script>

<template>
  <div class="toolbar-main">
    <div class="toolbar" :class="{ 'preview-state-head': editMode === 'preview' }">
      <el-icon class="custom-el-icon back-icon" @click="backToMain()">
        <Icon class="toolbar-icon hover-icon" name="icon_left_outlined" />
      </el-icon>
      <div class="left-area" v-show="editMode === 'edit'">
        <span id="canvas-name" class="name-area" @dblclick="editCanvasName">{{ dvInfo.name }}</span>
        <div class="opt-area">
          <el-icon class="opt-icon-undo" @click="undo()">
            <Icon class="toolbar-hover-icon" name="icon_undo_outlined"></Icon>
          </el-icon>
          <el-icon class="opt-icon-redo" @click="redo()">
            <Icon class="toolbar-hover-icon" name="icon_redo_outlined"></Icon>
          </el-icon>
        </div>
      </div>
      <div class="middle-area" v-show="!batchOptStatus && !linkageSettingStatus">
        <component-group
          :base-width="410"
          :show-split-line="true"
          :icon-name="'dv-view'"
          themes="light"
          title="图表"
        >
          <user-view-group themes="light" :dv-model="dvModel"></user-view-group>
        </component-group>
        <component-group
          :base-width="148"
          :show-split-line="true"
          themes="light"
          icon-name="dv-filter"
          title="查询组件"
        >
          <query-group></query-group>
        </component-group>
        <component-group themes="light" :base-width="148" icon-name="dv-text" title="文本">
          <text-group themes="light" :dv-model="dvModel"></text-group>
        </component-group>
        <component-group themes="light" icon-name="dv-media" title="图片">
          <media-group themes="light" :dv-model="dvModel"></media-group>
        </component-group>
        <component-button :show-split-line="true" icon-name="dv-tab" title="Tab"></component-button>
        <component-button
          icon-name="dv-copy"
          title="复用"
          @customClick="multiplexingCanvasOpen"
        ></component-button>
      </div>
      <div class="right-area" v-show="!batchOptStatus && !linkageSettingStatus">
        <component-button
          tips="批量操作"
          @custom-click="batchOptStatusChange(true)"
          icon-name="dv-batch"
        ></component-button>
        <component-button
          tips="仪表板配置"
          @custom-click="openDataBoardSetting"
          icon-name="dv-dashboard"
        ></component-button>
        <el-divider direction="vertical" />
        <component-button tips="移动端布局" icon-name="dv_mobile_layout"></component-button>
        <el-button
          class="custom-normal-button"
          v-show="editMode === 'edit'"
          @click="preview()"
          style="float: right; margin-right: 12px"
          >预览</el-button
        >
        <el-button
          v-show="editMode === 'edit'"
          @click="saveCanvas()"
          style="float: right; margin-right: 12px"
          type="primary"
          >保存</el-button
        >
      </div>

      <div class="right-area full-area" v-show="batchOptStatus">
        <el-button
          class="custom-normal-button"
          @click="batchOptStatusChange(false)"
          style="float: right; margin-right: 12px"
        >
          <!--          <Icon style="width: 20px; height: 20px" name="dv-batch"></Icon>-->
          退出批量操作</el-button
        >
        <el-button @click="saveBatchChange" style="float: right; margin-right: 12px" type="primary"
          >确定</el-button
        >
      </div>

      <div class="right-area full-area" v-show="linkageSettingStatus">
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
      <input ref="nameInput" v-model="inputName" @blur="closeEditCanvasName" />
    </Teleport>

    <el-button
      v-show="editMode === 'preview'"
      icon="EditPen"
      @click="edit()"
      class="edit-button"
      type="primary"
      >编辑</el-button
    >
    <multiplexing-canvas ref="multiplexingRef"></multiplexing-canvas>
  </div>
</template>

<style lang="less" scoped>
.full-area {
  flex: 1;
}
.edit-button {
  right: 10px;
  top: 10px;
  position: absolute;
  z-index: 2;
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
      .opt-icon-undo {
        font-size: 18px;
      }
      .opt-icon-redo {
        margin-left: 12px;
        font-size: 18px;
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
.custom-normal-button {
  background-color: rgba(0, 0, 0, 0);
  border-color: #a6a6a6;
  color: #ffffff;
  &:hover {
    color: #3370ff;
  }
}
</style>
