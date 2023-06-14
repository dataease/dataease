<script setup lang="ts">
import { shallowRef, defineAsyncComponent } from 'vue'
import { propTypes } from '@/utils/propTypes'
const DashboardEditor = defineAsyncComponent(() => import('@/views/data-visualization/index.vue'))
const props = defineProps({
  componentName: propTypes.string.def('DashboardEditor')
})
const currentComponent = shallowRef()

if (props.componentName === 'DashboardEditor') {
  currentComponent.value = DashboardEditor
}
</script>
<template>
  <component :is="currentComponent"></component>
</template>
