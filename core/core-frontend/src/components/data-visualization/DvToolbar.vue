<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { generateID } from '@/utils/generateID'
import toast from '@/utils/toast'
import Preview from '@/components/data-visualization/canvas/Preview.vue'
import { commonStyle, commonAttr } from '@/custom-component/component-list'
import eventBus from '@/utils/eventBus'
import { $ } from '@/utils/utils'
import changeComponentsSizeWithScale, {
  changeComponentSizeWithScale
} from '@/utils/changeComponentsSizeWithScale'
import { ref, nextTick } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import Icon from '../icon-custom/src/Icon.vue'
import { update, save, deleteLogic } from '@/api/visualization/dataVisualization'
import ComponentGroup from '@/components/visualization/ComponentGroup.vue'
import UserViewGroup from '@/custom-component/component-group/UserViewGroup.vue'
import MediaGroup from '@/custom-component/component-group/MediaGroup.vue'
import TextGroup from '@/custom-component/component-group/TextGroup.vue'
import CommonGroup from '@/custom-component/component-group/CommonGroup.vue'

const isShowPreview = ref(false)
const isScreenshot = ref(false)
let timer = null
let nameEdit = ref(false)
let inputName = ref('')
let nameInput = ref(null)
const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { styleChangeTimes } = storeToRefs(snapshotStore)
const {
  curComponent,
  canvasStyleData,
  curComponentIndex,
  componentData,
  dvInfo,
  canvasViewInfo,
  editMode
} = storeToRefs(dvMainStore)
const { areaData } = storeToRefs(composeStore)
let scale = ref(canvasStyleData.value.scale)

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

const handleScaleChange = () => {
  clearTimeout(timer)
  timer = setTimeout(() => {
    // 画布比例设一个最小值，不能为 0
    // eslint-disable-next-line no-bitwise
    scale.value = ~~scale.value || 1
    changeComponentsSizeWithScale(scale.value)
  }, 1000)
}

const lock = () => {
  lockStore.lock()
}

const unlock = () => {
  lockStore.unlock()
}

const compose = () => {
  composeStore.compose()
  snapshotStore.recordSnapshot('dv-compose')
}

const decompose = () => {
  composeStore.decompose()
  snapshotStore.recordSnapshot('dv-decompose')
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
      snapshotStore.recordSnapshot('dv-handleFileChange')

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
  const canvasInfo = {
    canvasStyleData: JSON.stringify(canvasStyleData.value),
    componentData: JSON.stringify(componentData.value),
    canvasViewInfo: canvasViewInfo.value,
    ...dvInfo.value
  }
  save(canvasInfo).then(res => {
    snapshotStore.resetStyleChangeTimes()
    ElMessage.success('保存成功')
  })
}

const clearCanvas = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setComponentData([])
  snapshotStore.recordSnapshot('dv-clearCanvas')
}

const handlePreviewChange = () => {
  isShowPreview.value = false
  dvMainStore.setEditMode('edit')
}

const editCanvasName = () => {
  nameEdit.value = true
  inputName.value = dvInfo.value.name
  nextTick(() => {
    nameInput.value.focus()
  })
}

const backToMain = () => {
  const url = '#/screen/index?dvId=' + dvInfo.value.id
  if (styleChangeTimes.value > 0) {
    ElMessageBox.confirm('当前的更改尚未保存，确定退出吗？', {
      confirmButtonType: 'danger',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      window.open(url, '_self')
    })
  } else {
    window.open(url, '_self')
  }
}

eventBus.on('preview', preview)
eventBus.on('save', saveCanvas)
eventBus.on('clearCanvas', clearCanvas)
</script>

<template>
  <div class="toolbar-main">
    <div class="toolbar" :class="{ 'preview-state-head': editMode === 'preview' }">
      <el-icon class="custom-el-icon back-icon" @click="backToMain()">
        <Icon class="toolbar-icon" name="icon_left_outlined" />
      </el-icon>
      <div class="left-area">
        <span id="dv-canvas-name" class="name-area" @dblclick="editCanvasName">{{
          dvInfo.name
        }}</span>
        <div class="opt-area">
          <el-icon class="opt-icon-undo" @click="undo()">
            <Icon class="toolbar-hover-icon" name="icon_undo_outlined"></Icon>
          </el-icon>
          <el-icon class="opt-icon-redo" @click="redo()">
            <Icon class="toolbar-hover-icon" name="icon_redo_outlined"></Icon>
          </el-icon>
        </div>
      </div>
      <div class="middle-area">
        <component-group :base-width="410" icon-name="dv-view" title="图表">
          <user-view-group></user-view-group>
        </component-group>
        <component-group :base-width="115" icon-name="dv-text" title="文本">
          <text-group></text-group>
        </component-group>
        <component-group :base-width="115" icon-name="dv-media" title="媒体">
          <media-group></media-group>
        </component-group>
        <component-group :base-width="410" icon-name="dv-material" title="素材">
          <common-group></common-group>
        </component-group>
        <!--        <component-group icon-name="dv-params" title="参数">-->
        <!--          <div>setting</div>-->
        <!--        </component-group>-->
      </div>
      <div class="right-area">
        <el-button
          class="custom-normal-button"
          @click="preview()"
          style="float: right; margin-right: 12px"
          >预览</el-button
        >
        <el-button
          @click="saveCanvas()"
          :disabled="styleChangeTimes < 1"
          style="float: right; margin-right: 12px"
          type="primary"
          >保存</el-button
        >
      </div>
    </div>
    <Teleport v-if="nameEdit" :to="'#dv-canvas-name'">
      <input ref="nameInput" v-model="inputName" @blur="closeEditCanvasName" />
    </Teleport>
    <!-- 预览 -->
    <Preview v-if="isShowPreview" :is-screenshot="isScreenshot" @close="handlePreviewChange" />
    <el-button
      v-show="editMode === 'preview'"
      icon="EditPen"
      @click="edit()"
      class="edit-button"
      type="primary"
      >编辑</el-button
    >
  </div>
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
  z-index: 2;
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
      .opt-icon-undo {
        font-size: 22px;
      }
      .opt-icon-redo {
        margin-left: 12px;
        font-size: 22px;
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
