<script setup lang="ts">
import { shallowRef, defineAsyncComponent } from 'vue'
import { propTypes } from '@/utils/propTypes'
const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/index.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('./DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('./ViewWrapper.vue'))
const props = defineProps({
  componentName: propTypes.string.def('DashboardEditor')
})
const currentComponent = shallowRef()
console.log('props.componentName', props.componentName)

const componentMap = {
  DashboardEditor,
  VisualizationEditor,
  ViewWrapper,
  Dashboard
}

currentComponent.value = componentMap[props.componentName]
</script>
<template>
  <component :is="currentComponent"></component>
</template>
