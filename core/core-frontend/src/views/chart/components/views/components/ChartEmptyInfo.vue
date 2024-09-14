<script lang="tsx" setup>
import { iconChartDarkMap } from '@/components/icon-group/chart-dark-list'
import { iconChartMap } from '@/components/icon-group/chart-list'
import { toRefs } from 'vue'
const props = defineProps({
  viewIcon: {
    type: String,
    required: true,
    default: ''
  },
  themes: {
    type: String,
    required: true,
    default: ''
  }
})

const { viewIcon, themes } = toRefs(props)
</script>

<template>
  <div class="canvas-content error-info">
    <Icon class-name="item-icon"
      ><component
        class="svg-icon item-icon"
        :is="themes === 'dark' ? iconChartDarkMap[`${viewIcon}-dark`] : iconChartMap[viewIcon]"
      ></component
    ></Icon>
  </div>
</template>

<style lang="less" scoped>
.error-info {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  color: #9ea6b2;
  width: 100%;
  height: 100%;
  flex-direction: column;
}

.item-icon {
  width: 50%;
  height: 50%;
  opacity: 0.3;
}
</style>
