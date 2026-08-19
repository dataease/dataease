<script lang="ts" setup>
import icon_file from '@/assets/svg/icon_file-doc_colorful.svg'
defineProps({
  label: {
    type: String,
    default: ''
  },
  subInfo: {
    type: String,
    default: ''
  }
})

const emits = defineEmits(['click'])
const handleClick = () => {
  emits('click')
}
</script>

<template>
  <div class="df-cell" @click="handleClick">
    <el-icon>
      <Icon name="icon_file"><icon_file class="svg-icon" /></Icon>
    </el-icon>
    <div class="label">
      <div class="text ellipsis">
        {{ label }}
      </div>
      <div class="sub">
        <slot name="subInfo">{{ subInfo }}</slot>
      </div>
    </div>
    <div class="btn">
      <slot name="btn"></slot>
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

  .ed-icon {
    font-size: 22px;
    margin-right: 8px;
  }

  .label {
    font-size: 16px;
    font-weight: 400;
    line-height: 22px;
    max-width: calc(100% - 54px);

    .text {
      width: 100%;
    }

    .sub {
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
      color: #8f959e;
      margin-top: 2px;
    }
  }
  .btn {
    flex: 1
  }

  //&:nth-child(1) {
  //  &::after {
  //    display: none;
  //  }
  //}
}
</style>
