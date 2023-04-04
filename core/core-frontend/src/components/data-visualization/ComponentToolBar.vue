<script setup lang="ts">
import { ElIcon } from 'element-plus-secondary'
</script>
<template>
  <div class="custom-main">
    <el-icon><Histogram>图表</Histogram></el-icon>
    <el-icon><Filter>过滤组件</Filter></el-icon>
    <el-icon><Document>富文本</Document></el-icon>
    <el-icon><VideoCameraFilled>视频</VideoCameraFilled></el-icon>
    <el-icon><PictureFilled>图片</PictureFilled></el-icon>
  </div>
</template>

<style lang="less">
.custom-main {
  width: 100%;
  display: flex;
}
</style>
