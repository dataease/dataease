<script setup lang="ts">
import { shallowRef } from 'vue'
import { propTypes } from '@/utils/propTypes'
import Dashboard from '@/views/dashboard/index.vue'
const props = defineProps({
  componentName: propTypes.string.def('Dashboard')
})
const currentComponent = shallowRef()

if (props.componentName === 'Dashboard') {
  currentComponent.value = Dashboard
}
</script>
<template>
  <component :is="currentComponent"></component>
</template>
