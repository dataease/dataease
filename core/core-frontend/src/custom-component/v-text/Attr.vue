<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)
</script>

<template>
  <div class="attr-list">
    <CommonAttr></CommonAttr>
    <div class="content">
      <span>内容</span>
      <el-input v-model="curComponent['propValue']" type="textarea" :rows="3" />
    </div>
  </div>
</template>

<style lang="less" scoped>
.content {
  width: 100%;
  font-size: 12px;
  padding: 10px;
}
:deep(.el-textarea__inner) {
  color: #ffffff;
  background-color: #000000;
}
</style>
