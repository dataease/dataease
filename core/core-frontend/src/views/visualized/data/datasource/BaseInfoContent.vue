<script lang="ts" setup>
import { ref } from 'vue'
import { propTypes } from '@/utils/propTypes'
defineProps({
  name: propTypes.string.def('')
})
const active = ref(true)
defineExpose({
  active
})
</script>

<template>
  <div :class="[active ? 'active' : 'deactivate', 'base-info-content']">
    <p class="title" @click="active = !active">
      <el-icon style="font-size: 10px">
        <Icon name="icon_expand-right_filled"></Icon>
      </el-icon>
      <span class="name">{{ name }}</span>
    </p>
    <slot :active="active"></slot>
  </div>
</template>

<style lang="less" scoped>
.base-info-content {
  padding: 24px;
  border-radius: 4px;
  background: #fff;
  margin: 24px 24px 0 24px;
  position: relative;

  & + .base-info-content {
    margin-top: 16px;
  }

  .update-records-time {
    color: #646a73;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;
    margin-left: 8px;
  }

  .title {
    display: flex;
    align-items: center;
    cursor: pointer;
  }

  .name {
    color: #1f2329;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 16px;
    font-style: normal;
    font-weight: 500;
    line-height: 24px;
    margin-left: 8px;
  }

  &.active {
    .title {
      .ed-icon {
        transform: rotate(90deg);
        color: var(--ed-color-primary);
      }
    }
    overflow: auto;
    height: auto;
  }

  &.deactivate {
    height: 72px;
    overflow: hidden;
    .title {
      .ed-icon {
        transform: rotate(0);
        color: var(--ed-color-primary);
      }
    }
  }
}
</style>
