<script lang="ts" setup>

defineProps<{
  index: number
}>()

const emits = defineEmits(['click'])
const handleClick = () => {
  emits('click')
}
</script>

<template>
  <div class="df-cell" @click="handleClick">
    <div class="index">
      {{index}}
    </div>
    <div class="label">
      <slot></slot>
    </div>
    <div class="icons">
      <slot name="icon"></slot>
    </div>
  </div>
</template>

<style lang="less" scoped>
.df-cell {
  width: 100%;
  min-height: 68px;
  position: relative;
  display: flex;
  padding: 12px 16px;
  background: #fff;
  &::after {
    content: '';
    background: #e4e5e7;
    height: 1px;
    width: calc(100% - 62px);
    transform: scaleY(0.5);
    position: absolute;
    left: 46px;
    bottom: 0;
  }

  .index {
    width: 22px;
    margin-right: 10px;
    display: flex;
    align-items: center;
    font-weight: 600;
    color: #a6a6a6;
  }

  .label {
    font-size: 16px;
    font-weight: 400;
    line-height: 22px;
    width: 0;
    flex: 1;
    text-overflow: ellipsis;
    word-break: break-all;
    white-space: nowrap;
    overflow: hidden;
  }

  .icons {
    padding-left: 12px;
    padding-right: 12px;
    display: flex;
    align-items: center;
  }


  //&:nth-child(1) {
  //  &::after {
  //    display: none;
  //  }
  //}
}
</style>
