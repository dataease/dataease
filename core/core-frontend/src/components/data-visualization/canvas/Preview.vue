<script setup lang="ts">
import { getStyle, getCanvasStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { toPng } from 'html-to-image'
import { deepCopy } from '@/utils/utils'
import { getCurrentInstance } from 'vue'

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
import { ElButton } from 'element-plus-secondary'

const props = defineProps({
  isScreenshot: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['close'])
const currentInstance = getCurrentInstance()

const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, componentData } = storeToRefs(dvMainStore)
const copyData = ref(deepCopy(componentData))
const container = ref(null)

const close = () => {
  emit('close')
}

const htmlToImage = () => {
  toPng(container.value.querySelector('.canvas'))
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', 'screenshot')
      a.href = dataUrl
      a.click()
      this.close
    })
    .catch(error => {
      console.error('oops, something went wrong!', error)
      this.close
    })
}
</script>

<template>
  <div ref="container" class="bg">
    <el-button v-if="!isScreenshot" class="close" @click="close()">关闭</el-button>
    <el-button v-else class="close" @click="htmlToImage()">确定</el-button>
    <div class="canvas-container">
      <div
        class="canvas"
        :style="{
          ...getCanvasStyle(canvasStyleData),
          width: changeStyleWithScale(canvasStyleData.width) + 'px',
          height: changeStyleWithScale(canvasStyleData.height) + 'px'
        }"
      >
        <ComponentWrapper v-for="(item, index) in copyData" :key="index" :config="item" />
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.bg {
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  position: fixed;
  background: rgb(0, 0, 0, 0.5);
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  padding: 20px;

  .canvas-container {
    width: calc(100% - 40px);
    height: calc(100% - 120px);
    overflow: auto;

    .canvas {
      background: #fff;
      position: relative;
      margin: auto;
    }
  }

  .close {
    position: absolute;
    right: 20px;
    top: 20px;
  }
}
</style>
