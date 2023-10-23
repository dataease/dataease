<script lang="tsx" setup>
import Editor from '@/views/chart/components/editor/index.vue'
import Chart from '@/views/chart/components/views/index.vue'
</script>
<!--调试测试页面-->
<template>
  <div>
    <editor :style="{ position: 'absolute', height: '100%', right: 0 }"></editor>
    <chart :style="{ width: '500px', height: '500px' }" />
  </div>
</template>

<style lang="less" scoped></style>
