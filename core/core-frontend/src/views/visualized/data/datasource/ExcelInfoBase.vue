<script lang="ts" setup>
import icon_excel from '@/assets/svg/icon_excel.svg'
withDefaults(
  defineProps<{
    name?: string
    size?: number
    showDel?: boolean
  }>(),
  {
    name: '',
    size: 0
  }
)
</script>

<template>
  <div class="excel-info_base">
    <el-icon class="excel">
      <Icon name="icon_excel"><icon_excel class="svg-icon" /></Icon>
    </el-icon>
    <p :title="name" class="name ellipsis">{{ name || '-' }}</p>
    <p class="size">{{ size || '-' }}</p>
  </div>
</template>

<style lang="less" scoped>
.excel-info_base {
  display: flex;
  align-items: center;
  width: 100%;
  height: 24px;
  padding: 0 0 0 0;
  border-radius: 4px;
  font-weight: 400;
  .excel {
    font-size: 24px;
    margin-right: 4px;
  }

  .name {
    color: #1f2329;
    font-size: 14px;
    max-width: calc(100% - 112px);
    margin-right: 12px;
    line-height: 22px;
  }

  .size {
    color: #8f959e;
    font-size: 14px;
    line-height: 22px;
  }
}
</style>
