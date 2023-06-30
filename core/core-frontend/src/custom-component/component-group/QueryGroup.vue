<script setup lang="ts">
import { ref, toRefs } from 'vue'
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
  }
})

const { propValue, element, dvModel } = toRefs(props)
const currentPane = ref('common')

const handleDragStart = e => {
  commonHandleDragStart(e, dvModel.value)
}

const handleDragEnd = e => {
  commonHandleDragEnd(e, dvModel.value)
}

const newComponent = componentName => {
  eventBus.emit('handleNew', { componentName: componentName, innerType: componentName })
}
</script>

<template>
  <div
    class="group"
    @dragstart="handleDragStart"
    @dragend="handleDragEnd"
    v-on:click="newComponent('VQuery')"
  >
    <drag-component name="Query" label="查询组件" drag-info="VQuery&VQuery"></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
}
.custom_img {
  width: 100px;
  height: 70px;
  cursor: pointer;
}
</style>
