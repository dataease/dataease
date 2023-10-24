<script setup lang="ts">
import ContextMenuDetails from '@/components/data-visualization/canvas/ContextMenuDetails.vue'

const emit = defineEmits(['close'])
defineProps({
  element: {
    type: Object
  },
  index: {
    type: Number
  }
})

const close = param => {
  emit('close', param)
}
</script>

<template>
  <context-menu-details active-position="aside" @close="close"></context-menu-details>
</template>
