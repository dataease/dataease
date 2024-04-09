<script setup lang="ts">
import { toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import DragComponent from '@/custom-component/component-group/DragComponent.vue'
import { commonHandleDragEnd, commonHandleDragStart } from '@/utils/canvasUtils'

const props = defineProps({
  dvModel: {
    type: String,
    default: 'dv'
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { dvModel } = toRefs(props)
const newComponent = () => {
  eventBus.emit('handleNew', { componentName: 'DeFrame', innerType: 'DeFrame' })
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
      icon="db-more-web"
      label="网页"
      drag-info="DeFrame&DeFrame"
    ></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
  padding: 12px 8px;
}
</style>
