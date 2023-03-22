<script setup lang="ts">
import { ElMessage } from 'element-plus-secondary'
import { generateID } from '@/utils/generateID'
import toast from '@/utils/toast'
import Preview from '@/components/data-visualization/canvas/Preview'
import AceEditor from '@/components/data-visualization/canvas/AceEditor.vue'
import { commonStyle, commonAttr } from '@/custom-component/component-list'
import eventBus from '@/utils/eventBus'
import { $ } from '@/utils/utils'
import changeComponentsSizeWithScale, {
  changeComponentSizeWithScale
} from '@/utils/changeComponentsSizeWithScale'
import { ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'

const isShowPreview = ref(false)
const isShowAceEditor = ref(false)
const isScreenshot = ref(false)
let timer = null
const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { curComponent, canvasStyleData, curComponentIndex, componentData } = storeToRefs(dvMainStore)
const { areaData } = storeToRefs(composeStore)
let scale = ref(canvasStyleData.value.scale)

const handleScaleChange = () => {
  clearTimeout(timer)
  timer = setTimeout(() => {
    // 画布比例设一个最小值，不能为 0
    // eslint-disable-next-line no-bitwise
    scale.value = ~~scale.value || 1
    changeComponentsSizeWithScale(scale.value)
  }, 1000)
}

const handleAceEditorChange = () => {
  isShowAceEditor.value = !isShowAceEditor.value
}

const closeEditor = () => {
  handleAceEditorChange()
}

const lock = () => {
  lockStore.lock()
}

const unlock = () => {
  lockStore.unlock()
}

const compose = () => {
  composeStore.compose()
  snapshotStore.recordSnapshot()
}

const decompose = () => {
  composeStore.decompose()
  snapshotStore.recordSnapshot()
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

      // 根据画面比例修改组件样式比例 https://github.com/woai3c/visual-drag-demo/issues/91
      changeComponentSizeWithScale(component)
      dvMainStore.addComponent({ component: component, index: undefined })
      snapshotStore.recordSnapshot()

      // 修复重复上传同一文件，@change 不触发的问题
      $('#input').setAttribute('type', 'text')
      $('#input').setAttribute('type', 'file')
    }

    img.src = fileResult
  }

  reader.readAsDataURL(file)
}

const preview = isScreenshot => {
  this.isScreenshot = isScreenshot
  this.isShowPreview = true
  dvMainStore.setEditMode('preview')
}

const save = () => {
  localStorage.setItem('canvasData', JSON.stringify(componentData))
  localStorage.setItem('canvasStyle', JSON.stringify(canvasStyleData))
  ElMessage.success('保存成功')
}

const clearCanvas = () => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setComponentData([])
  snapshotStore.recordSnapshot()
}

const handlePreviewChange = () => {
  isShowPreview.value = false
  dvMainStore.setEditMode('edit')
}

eventBus.on('preview', preview)
eventBus.on('save', save)
eventBus.on('clearCanvas', clearCanvas)
</script>

<template>
  <div>
    <div class="toolbar">
      <el-button @click="handleAceEditorChange">JSON</el-button>
      <el-button @click="undo">撤消</el-button>
      <el-button @click="redo">重做</el-button>
      <label for="input" class="insert">
        插入图片
        <input id="input" type="file" hidden @change="handleFileChange" />
      </label>

      <el-button style="margin-left: 10px" @click="preview(false)">预览</el-button>
      <el-button @click="save">保存</el-button>
      <el-button @click="clearCanvas">清空画布</el-button>
      <el-button :disabled="!areaData.components.length" @click="compose">组合</el-button>
      <el-button
        :disabled="!curComponent || curComponent.isLock || curComponent.component != 'Group'"
        @click="decompose"
      >
        拆分
      </el-button>

      <el-button :disabled="!curComponent || curComponent.isLock" @click="lock">锁定</el-button>
      <el-button :disabled="!curComponent || !curComponent.isLock" @click="unlock">解锁</el-button>
      <el-button @click="preview(true)">截图</el-button>

      <div class="canvas-config">
        <span>画布大小</span>
        <input v-model="canvasStyleData.width" />
        <span>*</span>
        <input v-model="canvasStyleData.height" />
      </div>
      <div class="canvas-config">
        <span>画布比例</span>
        <input v-model="scale" @input="handleScaleChange" /> %
      </div>
    </div>

    <!-- 预览 -->
    <Preview v-if="isShowPreview" :is-screenshot="isScreenshot" @close="handlePreviewChange" />
    <AceEditor v-if="isShowAceEditor" @closeEditor="closeEditor" />
  </div>
</template>

<style lang="scss" scoped>
.toolbar {
  padding: 15px 10px;
  white-space: nowrap;
  overflow-x: auto;
  background: #fff;
  border-bottom: 1px solid #ddd;

  .canvas-config {
    display: inline-block;
    margin-left: 10px;
    font-size: 14px;
    color: #606266;

    input {
      width: 50px;
      margin-left: 4px;
      outline: none;
      padding: 0 5px;
      border: 1px solid #ddd;
      color: #606266;
    }

    span {
      margin-left: 10px;
    }
  }

  .insert {
    display: inline-block;
    line-height: 1;
    white-space: nowrap;
    cursor: pointer;
    background: #fff;
    border: 1px solid #dcdfe6;
    color: #606266;
    appearance: none;
    text-align: center;
    box-sizing: border-box;
    outline: 0;
    margin: 0;
    transition: 0.1s;
    font-weight: 500;
    padding: 9px 15px;
    font-size: 12px;
    border-radius: 3px;
    margin-left: 10px;

    &:active {
      color: #3a8ee6;
      border-color: #3a8ee6;
      outline: 0;
    }

    &:hover {
      background-color: #ecf5ff;
      color: #3a8ee6;
    }
  }
}
</style>
