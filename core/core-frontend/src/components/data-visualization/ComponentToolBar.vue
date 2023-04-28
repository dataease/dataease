<script setup lang="ts">
import ComponentGroup from '../visualization/ComponentGroup.vue'
</script>
<template>
  <template class="custom-main">
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

    <!--    <el-icon><Histogram>图表</Histogram></el-icon>-->
    <!--    <el-icon><Filter>过滤组件</Filter></el-icon>-->
    <!--    <el-icon><Document>富文本</Document></el-icon>-->
    <!--    <el-icon><VideoCameraFilled>视频</VideoCameraFilled></el-icon>-->
    <!--    <el-icon><PictureFilled>图片</PictureFilled></el-icon>-->
  </template>
</template>

<style lang="less">
.custom-main {
  width: calc(100% - 200px);
  display: flex;
  position: absolute;
  z-index: 10;
  height: @component-toolbar-height;
  background-color: rgba(37, 45, 54, 1);
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  color: #fff;
}
</style>
