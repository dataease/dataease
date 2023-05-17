<script lang="tsx" setup>
import Editor from '@/views/chart/components/editor/index.vue'
import ChartComponentG2Plot from '@/views/chart/components/views/ChartComponentG2Plot.vue'
</script>
<!--调试测试页面-->
<template>
  <div>
    <div>视图编辑调试测试页面</div>
    <editor :style="{ position: 'absolute', height: '100%', right: 0 }"></editor>
    <chart-component-g2-plot :style="{ width: '500px', height: '500px' }" />
  </div>
</template>

<style lang="less" scoped></style>
