<template>
  <div class="compose-shadow" @mousedown="handelMouseDown"></div>
</template>

<script setup lang="ts">
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
const composeStore = composeStoreWithOut()
const { areaData, isCtrlOrCmdDown } = storeToRefs(composeStore)

const props = defineProps({
  element: {
    required: true,
    type: Object
  }
})

const handelMouseDown = e => {
  // 右键返回
  if (e.buttons === 2) {
    return
  }
  const index = areaData.value.components.findIndex(component => component === props.element)
  if (index != -1) {
    areaData.value.components.splice(index, 1)
    e.stopPropagation()
  }
}
</script>

<style lang="less" scoped>
.compose-shadow {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  border: 1px solid #3370ff;
}
</style>
