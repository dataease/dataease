<script lang="ts" setup>
import DynamicDefaultPreview from './DynamicDefaultPreview.vue'
defineProps({ config: { type: Object, required: true } })
</script>

<template>
  <DynamicDefaultPreview :config="config" display-type="1" />
</template>
