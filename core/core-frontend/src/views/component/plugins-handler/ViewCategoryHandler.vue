<template>
  <div class="view-catelog-handler-container" />
</template>

<script lang="ts" setup>
import { loadViewCategory } from './viewCategoryApi'

const emits = defineEmits(['loadPluginCategory'])

loadViewCategory().then(res => {
  emits('loadPluginCategory', res)
})

</script>

<style lang="less" scoped>
.view-catelog-handler-container {
  display: none;
}
</style>