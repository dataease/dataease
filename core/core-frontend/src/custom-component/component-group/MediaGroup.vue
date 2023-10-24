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
const newComponent = () => {
  eventBus.emit('handleNew', { componentName: 'Picture', innerType: 'Picture' })
}

const handleDragStart = e => {
  commonHandleDragStart(e, dvModel.value)
}

const handleDragEnd = e => {
  commonHandleDragEnd(e, dvModel.value)
}
</script>

<template>
  <div
    class="group"
    @dragstart="handleDragStart"
    @dragend="handleDragEnd"
    v-on:click="newComponent"
  >
    <drag-component
      :themes="themes"
      icon="dv-picture-show"
      label="图片"
      drag-info="Picture&Picture"
    ></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
  padding: 12px 8px;
}
</style>
