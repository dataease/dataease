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

const handleDragStart = e => {
  e.dataTransfer.setData('id', e.target.dataset.id)
}

const newComponent = componentName => {
  eventBus.emit('handleNew', { componentName: componentName, innerType: componentName })
}
</script>

<template>
  <div class="group" @dragstart="handleDragStart" v-on:click="newComponent('VQuery')">
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
