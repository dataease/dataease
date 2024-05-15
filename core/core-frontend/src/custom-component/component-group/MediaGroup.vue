<script setup lang="ts">
import { toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import DragComponent from '@/custom-component/component-group/DragComponent.vue'
import { commonHandleDragEnd, commonHandleDragStart } from '@/utils/canvasUtils'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  dvModel: {
    type: String,
    default: 'dv'
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { dvModel } = toRefs(props)
const newComponent = params => {
  eventBus.emit('handleNew', { componentName: params, innerType: params })
}

const handleDragStart = e => {
  commonHandleDragStart(e, dvModel.value)
}

const handleDragEnd = e => {
  commonHandleDragEnd(e, dvModel.value)
}
</script>

<template>
  <div class="group" @dragstart="handleDragStart" @dragend="handleDragEnd">
    <drag-component
      :themes="themes"
      icon="dv-picture-show"
      label="图片"
      drag-info="Picture&Picture"
      v-on:click="newComponent('Picture')"
    ></drag-component>
    <drag-component
      :themes="themes"
      icon="icon-video"
      label="视频"
      drag-info="DeVideo&DeVideo"
      v-on:click="newComponent('DeVideo')"
    ></drag-component>
    <drag-component
      :themes="themes"
      icon="icon-stream"
      label="流媒体"
      drag-info="DeStreamMedia&DeStreamMedia"
      v-on:click="newComponent('DeStreamMedia')"
    ></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
  padding: 12px 8px;
  display: inline-flex;
}
</style>
