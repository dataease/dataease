<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
defineProps({
  name: propTypes.string.def(''),
  size: propTypes.number.def(0),
  showDel: propTypes.bool.def(false)
})

const emits = defineEmits(['del'])
const del = () => {
  emits('del')
}
</script>

<template>
  <div class="excel-info">
    <el-icon class="excel">
      <Icon name="icon_excel"></Icon>
    </el-icon>
    <div class="info">
      <p class="name">{{ name || '-' }}</p>
      <p class="size">{{ size || '-' }}</p>
    </div>
    <el-icon v-if="showDel" @click="del" class="delete">
      <Icon name="icon_delete-trash_outlined"></Icon>
    </el-icon>
  </div>
</template>

<style lang="less" scoped>
.excel-info {
  display: flex;
  align-items: center;
  width: 100%;
  height: 58px;
  padding: 0 16px 0 12px;
  border-radius: 4px;
  border: 1px solid #dee0e3;
  .excel {
    font-size: 32px;
    margin-right: 14.67px;
  }

  .info {
    font-family: PingFang SC;
    font-style: normal;
    font-weight: 400;
    .name {
      color: #1f2329;
      font-size: 14px;
      line-height: 22px;
    }

    .size {
      color: #8f959e;
      font-size: 12px;
      line-height: 20px;
    }
  }

  .delete {
    cursor: pointer;
    font-size: 16px;
    margin-left: auto;
  }
}
</style>
