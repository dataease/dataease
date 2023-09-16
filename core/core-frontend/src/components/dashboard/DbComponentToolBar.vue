<script setup lang="ts">
import ComponentGroup from '../visualization/ComponentGroup.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import UserViewGroup from '../../custom-component/component-group/UserViewGroup.vue'
import TextGroup from '@/custom-component/component-group/TextGroup.vue'
import MediaGroup from '@/custom-component/component-group/MediaGroup.vue'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
let timer = null
</script>
<template>
  <el-row class="custom-main">
    <component-group :base-width="300" icon-name="chart_pie" title="图表">
      <user-view-group></user-view-group>
    </component-group>
    <component-group :base-width="148" icon-name="other_text" title="文本">
      <text-group></text-group>
    </component-group>
    <component-group icon-name="other_media" title="媒体">
      <media-group></media-group>
    </component-group>
    <component-group icon-name="other_material" title="素材">
      <div>this is material test</div>
    </component-group>
    <component-group icon-name="other_setting" title="参数">
      <div>setting</div>
    </component-group>
  </el-row>
</template>

<style lang="less">
.custom-main {
  display: flex;
  width: 100%;
  height: @component-toolbar-height;
  background-color: rgba(37, 45, 54, 1);
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  color: #fff;
}
</style>
