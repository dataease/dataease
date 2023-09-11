<template>
  <div class="shadow-main" :style="shadowStyle">
    <div class="shadow-background"></div>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import { computed, toRefs } from 'vue'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()

const { curComponent } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const emit = defineEmits(['onStartResize', 'onStartMove', 'onDragging', 'onResizing', 'onMouseUp'])

const props = defineProps({
  baseWidth: {
    required: true,
    type: Number
  },
  baseHeight: {
    required: true,
    type: Number
  },
  curGap: {
    required: true,
    type: Number
  },
  element: {
    required: true,
    type: Object,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null
      }
    }
  }
})

const { element, baseWidth, baseHeight, curGap } = toRefs(props)

const shadowStyle = computed(() => {
  const { x, y, sizeX, sizeY } = element.value
  return {
    padding: curGap.value + 'px',
    width: sizeX * baseWidth.value + 'px',
    height: sizeY * baseHeight.value + 'px',
    left: (x - 1) * baseWidth.value + 'px',
    top: (y - 1) * baseHeight.value + 'px'
  }
})
</script>

<style lang="less" scoped>
.shadow-main {
  position: absolute;
  transition: all 0.1s;
}
.shadow-background {
  width: 100%;
  height: 100%;
  background-color: #b8d3f9;
}
</style>
