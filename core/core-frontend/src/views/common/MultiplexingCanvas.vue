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
    <el-button type="primary" class="confirm-button" @click="dialogShow = false">确定</el-button>
    <dashboard-preview-show
      class="multiplexing-area"
      show-position="multiplexing"
    ></dashboard-preview-show>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DashboardPreviewShow from '@/views/dashboard/DashboardPreviewShow.vue'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
const dialogShow = ref(false)
const copyStore = copyStoreWithOut()

const dialogInit = () => {
  dialogShow.value = true
}

const saveMultiplexing = () => {
  dialogShow.value = false
  copyStore.copyMultiplexingComponents()
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
