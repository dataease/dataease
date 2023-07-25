<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
defineProps({
  label: propTypes.string.def('')
})
</script>

<template>
  <div class="base-info-item">
    <p class="label">{{ label }}</p>
    <p class="value">
      <slot></slot>
    </p>
  </div>
</template>

<style lang="less" scoped>
.base-info-item {
  margin-top: 16px;
  font-family: PingFang SC;
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
  width: 100%;
  .label {
    color: #646a73;
  }
  .value {
    margin-top: 2px;
    color: #1f2329;
  }
}
</style>
