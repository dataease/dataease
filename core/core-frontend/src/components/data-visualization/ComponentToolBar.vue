<script setup lang="ts">
import ComponentGroup from '../visualization/ComponentGroup.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
import changeComponentsSizeWithScale from '../../utils/changeComponentsSizeWithScale'
import UserViewGroup from '../../custom-component/component-group/UserViewGroup.vue'
import TextGroup from '@/custom-component/component-group/TextGroup.vue'
import MediaGroup from '@/custom-component/component-group/MediaGroup.vue'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
let scale = ref(canvasStyleData.value.scale)
let timer = null

const handleScaleChange = () => {
  // 画布比例设一个最小值，不能为 0
  scale.value = ~~scale.value || 10
  changeComponentsSizeWithScale(scale.value)
}
</script>
<template>
  <el-row class="custom-main">
    <div style="display: flex; padding-top: 10px; margin-left: 50px">
      <el-slider
        style="width: 300px"
        v-model="scale"
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
