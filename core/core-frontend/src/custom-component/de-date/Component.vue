<template>
  <div style="height: 100%">
    <time-default
      v-if="element?.formatInfo?.openMode === '0'"
      :ref="element.id"
      :element="element"
    />

    <time-elec v-if="element?.formatInfo?.openMode === '1'" :ref="element.id" :element="element" />

    <time-simple
      v-if="element?.formatInfo?.openMode === '2'"
      :ref="element.id"
      :element="element"
    />

    <time-complex
      v-if="element?.formatInfo?.openMode === '3'"
      :ref="element.id"
      :element="element"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, toRefs } from 'vue'
import { getCurrentInstance, onMounted } from 'vue/dist/vue'
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
