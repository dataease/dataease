<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref, onMounted } from 'vue'
import changeComponentsSizeWithScale from '../../utils/changeComponentsSizeWithScale'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()

let timer = null

const handleScaleChange = () => {
  snapshotStore.recordSnapshotCache()
  // 画布比例设一个最小值，不能为 0
  canvasStyleData.value.scale = ~~canvasStyleData.value.scale || 10
  changeComponentsSizeWithScale(canvasStyleData.value.scale)
}

onMounted(() => {
  handleScaleChange()
})
</script>
<template>
  <el-row class="custom-main">
    <div style="display: flex; padding-top: 10px; margin-left: 50px">
      <el-slider
        style="width: 300px"
        v-model="canvasStyleData.scale"
        @change="handleScaleChange()"
        show-input
        size="small"
      />
      <span style="margin-left: 5px">%</span>
    </div>
  </el-row>
</template>

<style lang="less">
.custom-main {
  display: flex;
  width: 100%;
  justify-content: right;
  height: @component-toolbar-height;
  background-color: @side-area-background;
  border-top: 1px solid @side-outline-border-color;
  color: #fff;
  transition: 0.5s;
}
</style>
