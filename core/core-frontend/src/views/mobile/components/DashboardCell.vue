<script lang="ts" setup>
defineProps({
  label: {
    type: String,
    default: ''
  },
  prefixIcon: {
    type: String,
    default: ''
  },
  nextlevel: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['click'])
const handleClick = () => {
  emits('click')
}
</script>

<template>
  <div class="dashboard-cell" @click="handleClick">
    <div class="label">
      <el-icon>
        <Icon :name="prefixIcon"></Icon>
      </el-icon>
      <span class="text ellipsis">
        {{ label }}
      </span>
    </div>
    <div class="switch" v-if="nextlevel">
      <el-icon>
        <Icon name="icon_right_outlined"></Icon>
      </el-icon>
    </div>
  </div>
</template>

<style lang="less" scoped>
.dashboard-cell {
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
    width: calc(100% - 62px);
    transform: scaleY(0.5);
    position: absolute;
    left: 46px;
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
    .ed-icon {
      font-size: 22px;
      margin-right: 8px;
    }
  }

  .switch {
    display: flex;
    align-items: center;
    color: #8f959e;
    .ed-icon {
      font-size: 16px;
    }
  }

  &:nth-child(1) {
    &::after {
      display: none;
    }
  }
}
</style>
