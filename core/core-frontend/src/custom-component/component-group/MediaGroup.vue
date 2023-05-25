<script setup lang="ts">
import { ref, toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import DragComponent from '@/custom-component/component-group/DragComponent.vue'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
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

const { propValue, element } = toRefs(props)
const currentPane = ref('common')
const newComponent = innerType => {
  eventBus.emit('handleNew', { componentName: 'UserView', innerType: 'bar' })
}

const handleDragStart = e => {
  e.dataTransfer.setData('id', e.target.dataset.id)
}
</script>

<template>
  <div class="group" @dragstart="handleDragStart">
    <drag-component icon="other_media" label="图片" drag-info="Picture&Picture"></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
}
</style>
