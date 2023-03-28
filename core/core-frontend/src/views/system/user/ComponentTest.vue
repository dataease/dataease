<script lang="ts" setup>
import { shallowRef } from 'vue'
import { HandleMore } from '@/components/handle-more'

const menuList = shallowRef([])
menuList.value = Array(10)
  .fill(1)
  .map((_, index) => ({
    label: 'test' + index + 20,
    command: index + 'nickName'
  }))
</script>

<template>
  <div class="list-item_primary">nihao</div>
  <div class="title-form_primary">nihao1</div>
  <handle-more placement="bottom-start" :menu-list="menuList"></handle-more>
</template>

<style lang="less" scoped>
.list-item_primary {
  height: 40px;
  border-radius: 4px;
  padding: 8px 12px;
  cursor: pointer;

  &:not(.active):hover {
    background: rgba(31, 35, 41, 0.1);
  }

  &.active {
    background: rgba(51, 112, 255, 0.1);
  }
}

.title-form_primary {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 14px;
  color: #1f2329;
  position: relative;
  padding-left: 10px;

  &::before {
    content: '';
    width: 2px;
    height: 14px;
    background: #3370ff;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    left: -10px;
  }
}
</style>
