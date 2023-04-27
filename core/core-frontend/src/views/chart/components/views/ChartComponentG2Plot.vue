<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { baseBarOption } from '../js/panel/bar/bar'

onMounted(() => {
  baseBarOption('container')
})
</script>

<template>
  <div>
    <div id="container"></div>
  </div>
</template>

<style lang="less" scoped></style>
