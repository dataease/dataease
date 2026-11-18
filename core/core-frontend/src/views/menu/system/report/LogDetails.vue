<script setup lang="ts">
import { defineAsyncComponent, ref, watch } from 'vue'
import type { Component } from 'vue'

interface DetailsHandle {
  jobLogDetailVisible: boolean
  startInterval(instanceId?: string, taskId?: string): void
}
const Details = defineAsyncComponent<Component>(async () => {
  try {
    return (await import('./ReportDeliveryDetails.vue')).default
  } catch {
    return (await import('./LogDetailsLegacy.vue')).default
  }
})
const child = ref<DetailsHandle>()
const jobLogDetailVisible = ref(false)
let pending: [string?, string?] | undefined
const startInterval = (instanceId?: string, taskId?: string) => {
  pending = [instanceId, taskId]
  if (child.value) {
    child.value.startInterval(...pending)
    pending = undefined
  }
}
watch([jobLogDetailVisible, child], () => {
  if (!child.value) return
  child.value.jobLogDetailVisible = jobLogDetailVisible.value
  if (pending && jobLogDetailVisible.value) {
    child.value.startInterval(...pending)
    pending = undefined
  }
})
const emit = defineEmits(['jobLogDetailVisibleClose'])
watch(
  () => child.value?.jobLogDetailVisible,
  visible => {
    if (visible === false && jobLogDetailVisible.value) {
      jobLogDetailVisible.value = false
      emit('jobLogDetailVisibleClose')
    }
  }
)
defineExpose({ jobLogDetailVisible, startInterval })
</script>

<template>
  <Details ref="child" />
</template>
