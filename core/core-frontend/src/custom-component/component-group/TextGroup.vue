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
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { propValue, element, dvModel, themes } = toRefs(props)
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
    v-on:click="newComponent('VText')"
  >
    <drag-component
      :themes="themes"
      name="Text"
      label="文本"
      drag-info="VText&VText"
    ></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
  padding-top: 5px;
}
.custom_img {
  width: 100px;
  height: 70px;
  cursor: pointer;
}
</style>
