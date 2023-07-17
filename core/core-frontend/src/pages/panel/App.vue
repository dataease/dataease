<script setup lang="ts">
import { shallowRef, defineAsyncComponent } from 'vue'
import { propTypes } from '@/utils/propTypes'
const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/index.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))
const props = defineProps({
  componentName: propTypes.string.def('DashboardEditor')
})
const currentComponent = shallowRef()
console.log('props.componentName', props.componentName)

if (props.componentName === 'DashboardEditor') {
  currentComponent.value = DashboardEditor
}

if (props.componentName === 'VisualizationEditor') {
  currentComponent.value = VisualizationEditor
}
</script>
<template>
  <component :is="currentComponent"></component>
</template>
