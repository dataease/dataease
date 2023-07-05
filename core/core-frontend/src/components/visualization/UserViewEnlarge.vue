<template>
  <el-dialog ref="enlargeDialog" v-model="dialogShow" width="70vw" trigger="click">
    <div class="enlarge-outer">
      <component-wrapper
        v-if="dialogShow"
        class="enlarge-wrapper"
        :view-info="viewInfo"
        :config="config"
        show-position="viewDialog"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { ref } from 'vue'
const dialogShow = ref(false)
const viewInfo = ref(null)
const config = ref(null)
const canvasStyleData = ref(null)

const dialogInit = (canvasStyle, view, item) => {
  dialogShow.value = true
  viewInfo.value = view
  config.value = item
  canvasStyleData.value = canvasStyle
}

defineExpose({
  dialogInit
})
</script>

<style lang="less" scoped>
.enlarge-outer {
  position: relative;
  height: 65vh;
  .enlarge-wrapper {
    width: 100%;
    height: 100%;
  }
}
</style>
