<script setup lang="ts">
import { reactive } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import DeCanvas from '@/views/canvas/DeCanvas.vue'
const dvMainStore = dvMainStoreWithOut()

const { componentData, canvasStyleData, canvasViewInfo } = storeToRefs(dvMainStore)

const state = reactive({
  canvasId: 'canvas-main'
})
</script>

<template>
  <div class="dv-common-layout-mobile">
    <de-canvas
      style="overflow-x: hidden"
      :canvas-id="state.canvasId"
      :component-data="componentData"
      :canvas-style-data="canvasStyleData"
      :canvas-view-info="canvasViewInfo"
    ></de-canvas>
  </div>
</template>

<style lang="less">
.dv-common-layout-mobile {
  height: 100vh;
  width: 100vw;
  overflow-y: auto;
}
</style>
