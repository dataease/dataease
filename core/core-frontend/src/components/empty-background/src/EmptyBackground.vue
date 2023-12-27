<script lang="ts" setup>
import { PropType } from 'vue'
import nothingInput from '@/assets/img/nothing-input.png'
import nothingSelect from '@/assets/img/nothing-select.png'
import nothingTable from '@/assets/img/nothing-table.png'
import none from '@/assets/img/none.png'
import error from '@/assets/img/error.png'
import nothingTree from '@/assets/img/nothing-tree.png'
import nothingNone from '@/assets/img/nothing-none.png'
defineProps({
  imgType: {
    type: String as PropType<
      'input' | 'select' | 'table' | 'none' | 'noneWhite' | 'tree' | 'error'
    >,
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
  tree: nothingTree,
  error,
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
:deep(.ed-empty__description) {
  margin-top: 8px;

  color: var(--N600, #646a73);
  text-align: center;
  /* 正文-字号 14，行高 22，字重适中（Regular），用于列表Feed会话 */
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
}
</style>
