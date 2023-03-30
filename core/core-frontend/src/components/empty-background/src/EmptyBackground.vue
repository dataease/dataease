<script lang="ts" setup>
import { PropType } from 'vue'

defineProps({
  imgType: {
    type: String as PropType<'input' | 'select' | 'table'>,
    default: 'table'
  },
  description: {
    type: String,
    default: ''
  }
})
const getAssetsFile = (url: string) => {
  const path = `../../../assets/img/nothing-${url}.png`
  return new URL(path, import.meta.url).href
}
</script>

<template>
  <el-empty :description="description" :image="getAssetsFile(imgType)">
    <slot></slot>
  </el-empty>
</template>

<style lang="less" scoped></style>
