<template>
  <div class="drag-info-main">
    <el-row style="justify-content: center">
      <Icon style="width: 120px; height: 80px" name="dv-drag-tips"></Icon>
    </el-row>
    <el-row class="tips-info"> {{ tips }} </el-row>
  </div>
</template>

<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)

const tips =
  '从顶部工具栏中选择组件，添加到这里创建' +
  (dvInfo.value.type === 'dashboard' ? '仪表板' : '数据大屏')
</script>

<style lang="less" scoped>
.drag-info-main {
  position: absolute;
  left: calc(50% - 125px);
  top: calc(40% - 90px);
  width: 250px;
  height: 180px;
}

.tips-info {
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  text-align: center;
  color: rgba(100, 106, 115, 1);
}
</style>
