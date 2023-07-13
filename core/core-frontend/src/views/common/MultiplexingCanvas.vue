<template>
  <el-dialog
    append-to-body
    fullscreen
    :show-close="false"
    v-model="dialogShow"
    trigger="click"
    title="复用"
  >
    <el-button class="close-button" @click="dialogShow = false">关闭</el-button>
    <el-button type="primary" class="confirm-button" @click="saveMultiplexing">确定</el-button>
    <dashboard-preview-show
      v-if="dialogShow"
      ref="multiplexingPreviewShowRef"
      class="multiplexing-area"
      show-position="multiplexing"
    ></dashboard-preview-show>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DashboardPreviewShow from '@/views/dashboard/DashboardPreviewShow.vue'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()
const dialogShow = ref(false)
const copyStore = copyStoreWithOut()
const multiplexingPreviewShowRef = ref(null)

const dialogInit = () => {
  dialogShow.value = true
  dvMainStore.initCurMultiplexingComponents()
}

const saveMultiplexing = () => {
  dialogShow.value = false
  const previewStateInfo = multiplexingPreviewShowRef.value.getPreviewStateInfo()
  copyStore.copyMultiplexingComponents(previewStateInfo.canvasViewInfoPreview)
}

defineExpose({
  dialogInit
})
</script>

<style lang="less" scoped>
.close-button {
  position: absolute;
  top: 18px;
  right: 120px;
}
.confirm-button {
  position: absolute;
  top: 18px;
  right: 20px;
}
.multiplexing-area {
  width: 100%;
  height: 100%;
  border: 1px solid @side-outline-border-color-light;
}
</style>
