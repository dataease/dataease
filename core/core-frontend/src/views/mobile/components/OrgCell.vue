<script lang="ts" setup>
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
const props = defineProps({
  label: {
    type: String,
    default: ''
  },
  prefixIcon: {
    type: String,
    default: ''
  },
  tips: {
    type: String,
    default: ''
  },
  active: {
    type: Boolean,
    default: false
  },
  nextlevel: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['click'])
const handleLeftClick = () => {
  emits('click', !props.nextlevel ? 'all' : 'left')
}

const handleRightClick = () => {
  emits('click', !props.nextlevel ? 'all' : 'right')
}
</script>

<template>
  <div class="org-cell">
    <div class="label" :class="active && 'active'">
      <el-icon v-if="!!prefixIcon">
        <Icon><component class="svg-icon" :is="prefixIcon"></component></Icon>
      </el-icon>
      <span class="text ellipsis">
        {{ label }}
      </span>
    </div>
    <div class="switch" v-if="nextlevel">
      <div class="tips">
        {{ tips }}
      </div>
      <el-icon>
        <Icon name="icon_right_outlined"><icon_right_outlined class="svg-icon" /></Icon>
      </el-icon>
    </div>
    <div class="left-area" @click="handleLeftClick"></div>
    <div class="right-area" @click="handleRightClick"></div>
  </div>
</template>

<style lang="less" scoped>
.org-cell {
  width: 100%;
  height: 48px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  &::after {
    content: '';
    background: #e4e5e7;
    height: 1px;
    width: calc(100% - 32px);
    transform: scaleY(0.5);
    position: absolute;
    left: 16px;
    bottom: 0;
  }

  .label {
    font-size: 16px;
    font-weight: 400;
    line-height: 22px;
    display: flex;
    max-width: calc(100% - 32px);
    align-items: center;

    .text {
      width: 100%;
    }
    &.active {
      color: var(--ed-color-primary);
    }

    .ed-icon {
      font-size: 20px;
      margin-right: 12px;
    }
  }

  .switch {
    display: flex;
    align-items: center;
    color: #8f959e;
    .tips {
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      margin-right: 8px;
    }

    .ed-icon {
      font-size: 16px;
    }
  }

  .left-area {
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    width: calc(100% - 48px);
  }

  .right-area {
    position: absolute;
    top: 0;
    right: 0;
    height: 100%;
    width: 48px;
  }

  &:nth-child(1) {
    &::after {
      display: none;
    }
  }
}
</style>
