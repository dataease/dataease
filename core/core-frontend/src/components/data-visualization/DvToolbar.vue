<script setup lang="ts">
import { ElMessage } from 'element-plus-secondary'
import { generateID } from '@/utils/generateID'
import toast from '@/utils/toast'
import Preview from '@/components/data-visualization/canvas/Preview.vue'
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
import Icon from '../icon-custom/src/Icon.vue'

const isShowPreview = ref(false)
const isScreenshot = ref(false)
let timer = null
const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { curComponent, canvasStyleData, curComponentIndex, componentData, dvInfo } =
  storeToRefs(dvMainStore)
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

      // 根据画面比例修改组件样式比例
      changeComponentSizeWithScale(component)
      dvMainStore.addComponent({ component: component, index: undefined })
      snapshotStore.recordSnapshot()

      $('#input').setAttribute('type', 'text')
      $('#input').setAttribute('type', 'file')
    }

    if (typeof fileResult === 'string') {
      img.src = fileResult
    }
  }

  reader.readAsDataURL(file)
}

const preview = isScreenshotFlag => {
  isScreenshot.value = isScreenshotFlag
  isShowPreview.value = true
  dvMainStore.setEditMode('preview')
}

const save = () => {
  localStorage.setItem('canvasData', JSON.stringify(componentData.value))
  localStorage.setItem('canvasStyle', JSON.stringify(canvasStyleData.value))
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

const backToMain = () => {
  alert('backToMain')
}

eventBus.on('preview', preview)
eventBus.on('save', save)
eventBus.on('clearCanvas', clearCanvas)
</script>

<template>
  <div>
    <div class="toolbar">
      <el-icon class="custom-el-icon" @click="backToMain()">
        <Icon class="toolbar-icon" name="icon_left_outlined" />
      </el-icon>
      <span class="name-area">{{ dvInfo.name }}</span>
      <div style="display: table-cell; float: right; width: 75%; vertical-align: middle">
        <el-divider direction="vertical" />
        <el-icon class="custom-el-icon" @click="undo()">
          <Icon class="toolbar-icon" name="icon_undo_outlined"></Icon>
        </el-icon>
        <el-icon class="custom-el-icon" @click="redo()">
          <Icon class="toolbar-icon" name="icon_redo_outlined"></Icon>
        </el-icon>

        <!--        <div class="canvas-config">-->
        <!--          <span>画布大小</span>-->
        <!--          <input v-model="canvasStyleData.width" />-->
        <!--          <span>*</span>-->
        <!--          <input v-model="canvasStyleData.height" />-->
        <!--        </div>-->
        <el-button @click="save()" style="float: right; margin-right: 12px" type="primary"
          >保存</el-button
        >
        <el-button @click="preview()" style="float: right; margin-right: 12px">预览</el-button>
      </div>
    </div>

    <!-- 预览 -->
    <Preview v-if="isShowPreview" :is-screenshot="isScreenshot" @close="handlePreviewChange" />
  </div>
</template>

<style lang="less" scoped>
.toolbar {
  height: @top-bar-height;
  padding: 5px 10px;
  line-height: 33px;
  white-space: nowrap;
  overflow-x: auto;
  background: rgba(0, 21, 41, 1);
  color: #ffffff;
  .name-area {
    margin: 0 10px;
  }
  .custom-el-icon {
    margin-left: 15px;
    color: #ffffff;
    cursor: pointer;
    vertical-align: -0.2em;
  }
  .toolbar-icon {
    width: 18px;
    height: 18px;
  }
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
