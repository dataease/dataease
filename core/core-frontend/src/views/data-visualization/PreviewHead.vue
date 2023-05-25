<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const emit = defineEmits(['reload', 'download'])

const preview = () => {
  const url = '#/preview/?dvId=' + dvInfo.value.id
  window.open(url, '_blank')
}

const reload = () => {
  emit('reload', dvInfo.value.id)
}

const download = () => {
  emit('download')
}

const dvEdit = () => {
  const url = '#/dvCanvas/?dvId=' + dvInfo.value.id
  window.open(url, '_blank')
}
</script>

<template>
  <div class="preview-head">
    <div class="canvas-name">{{ dvInfo.name }}</div>
    <div class="canvas-opt-icon">
      <el-icon class="custom-icon"><Star /></el-icon>
      <el-icon class="custom-icon"><Share /></el-icon>
      <el-icon class="custom-icon" @click="reload()"><Refresh /></el-icon>
    </div>
    <div class="canvas-opt-button">
      <el-button type="primary" @click="download()">导出</el-button>
      <el-button type="primary" @click="preview()">预览</el-button>
      <el-button @click="dvEdit()">编辑</el-button>
    </div>
  </div>
</template>

<style lang="less">
.preview-head {
  width: 100%;
  min-width: 300px;
  height: 45px;
  display: flex;
  padding: 0 10px 0 10px;
  border-bottom: 1px solid #d7d7d7;
  .canvas-name {
    display: flex;
    align-items: center;
    width: 100px;
  }
  .canvas-opt-icon {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 60px;
    .custom-icon {
      color: #3a8ee6;
      &:hover {
        background-color: #ecf5ff;
        cursor: pointer;
      }
    }
  }
  .canvas-opt-button {
    display: flex;
    justify-content: right;
    align-items: center;
    flex: 1;
  }
}
</style>
