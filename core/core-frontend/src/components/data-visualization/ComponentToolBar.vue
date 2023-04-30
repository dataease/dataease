<script setup lang="ts">
import ComponentGroup from '../visualization/ComponentGroup.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
import changeComponentsSizeWithScale from '../../utils/changeComponentsSizeWithScale'
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
    <component-group icon-name="chart_pie" title="图表">
      <div>this is chart_pie test</div>
    </component-group>
    <component-group icon-name="other_text" title="文本">
      <div>this is other_text test</div>
    </component-group>
    <component-group icon-name="other_media" title="媒体">
      <div>this is media test</div>
    </component-group>
    <component-group icon-name="other_material" title="素材">
      <div>this is material test</div>
    </component-group>
    <component-group icon-name="other_setting" title="参数">
      <div>setting</div>
    </component-group>

    <div style="display: flex; padding-top: 10px; margin-left: 150px">
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
  width: calc(100% - 200px);
  display: flex;
  position: absolute;
  z-index: 10;
  height: 45px;
  background-color: rgba(37, 45, 54, 1);
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  border-top: 1px solid rgba(85, 85, 85, 1);
  color: #fff;
}
</style>
