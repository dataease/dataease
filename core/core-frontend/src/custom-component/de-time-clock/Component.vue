<template>
  <div style="height: 100%">
    <time-default :ref="element.id" :element="element" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, toRefs, getCurrentInstance, onMounted } from 'vue'
import TimeDefault from '@/custom-component/de-time-clock/TimeDefault.vue'
const props = defineProps({
  element: {
    type: Object
  }
})
const { element } = toRefs(props)

let currentInstance
const timeMargin = computed(() => {
  return element.value.style.time_margin
})

onMounted(() => {
  currentInstance = getCurrentInstance()
})

const chartResize = () => {
  const dataRefs = currentInstance.proxy
  nextTick(() => {
    dataRefs.$refs[element.value.id] &&
      dataRefs.$refs[element.value.id].resize &&
      dataRefs.$refs[element.value.id].resize()
  })
}
</script>
