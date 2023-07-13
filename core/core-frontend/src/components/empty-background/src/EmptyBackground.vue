<script lang="ts" setup>
import { PropType } from 'vue'
import nothingInput from '@/assets/img/nothing-input.png'
import nothingSelect from '@/assets/img/nothing-select.png'
import nothingTable from '@/assets/img/nothing-table.png'
import none from '@/assets/img/none.png'
import nothingNone from '@/assets/img/nothing-none.png'
defineProps({
  imgType: {
    type: String as PropType<'input' | 'select' | 'table' | 'none' | 'noneWhite'>,
    default: 'table'
  },
  imageSize: {
    type: Number,
    default: 125
  },
  description: {
    type: String,
    default: ''
  }
})
const getAssetsFile = {
  input: nothingInput,
  select: nothingSelect,
  table: nothingTable,
  noneWhite: nothingNone,
  none
}
</script>

<template>
  <el-empty
    class="empty-info"
    :imageSize="imageSize"
    :description="description"
    :image="getAssetsFile[imgType]"
  >
    <slot></slot>
  </el-empty>
</template>

<style lang="less" scoped>
.empty-info {
  height: 100%;
}
</style>
