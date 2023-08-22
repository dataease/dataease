<template>
  <el-drawer
    direction="btt"
    size="90%"
    v-model="dialogShow"
    trigger="click"
    title="复用"
    class="custom-drawer"
  >
    <dashboard-preview-show
      v-if="dialogShow"
      ref="multiplexingPreviewShowRef"
      class="multiplexing-area"
      show-position="multiplexing"
    ></dashboard-preview-show>
    <template #footer>
      <el-row class="multiplexing-footer">
        <el-col class="adapt-count">
          <span>已选 {{ selectComponentCount }} 项</span>
        </el-col>
        <el-col class="adapt-select">
          <span class="adapt-text"> 标题样式： </span>
          <el-select
            style="width: 120px"
            v-model="multiplexingStyleAdapt"
            placeholder="Select"
            placement="top-start"
          >
            <el-option
              v-for="item in state.copyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
        <el-button class="close-button" @click="dialogShow = false">关闭</el-button>
        <el-button
          type="primary"
          :disabled="!selectComponentCount"
          class="confirm-button"
          @click="saveMultiplexing"
          >确定</el-button
        >
      </el-row>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, nextTick } from 'vue'
import DashboardPreviewShow from '@/views/dashboard/DashboardPreviewShow.vue'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const dialogShow = ref(false)
const copyStore = copyStoreWithOut()
const multiplexingPreviewShowRef = ref(null)
const { multiplexingStyleAdapt, curMultiplexingComponents } = storeToRefs(dvMainStore)
const state = reactive({
  copyOptions: [
    { label: '适应新主题', value: true },
    { label: '保持源样式', value: false }
  ]
})

const selectComponentCount = computed(() => Object.keys(curMultiplexingComponents.value).length)

const dialogInit = () => {
  dialogShow.value = true
  dvMainStore.initCurMultiplexingComponents()
}

const saveMultiplexing = () => {
  dialogShow.value = false
  const previewStateInfo = multiplexingPreviewShowRef.value.getPreviewStateInfo()
  const canvasViewInfoPreview = previewStateInfo.canvasViewInfoPreview
  nextTick(() => {
    copyStore.copyMultiplexingComponents(canvasViewInfoPreview)
  })
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
}
.multiplexing-footer {
  position: relative;
}

.adapt-count {
  position: absolute;
  top: 18px;
  left: 20px;
  color: #646a73;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
}

.adapt-select {
  position: absolute;
  top: 18px;
  right: 220px;
}
.adapt-text {
  font-size: 14px;
  font-weight: 400;
  color: #1f2329;
  line-height: 22px;
}
</style>
