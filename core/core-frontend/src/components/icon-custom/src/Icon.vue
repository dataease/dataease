<script setup lang="ts">
import { computed } from 'vue'
import { propTypes } from '@/utils/propTypes'
const props = defineProps({
  prefix: propTypes.string.def('icon'),
  name: propTypes.string,
  className: propTypes.string,
  staticContent: propTypes.string
})
const svgClass = computed(() => {
  if (props.className) {
    return `svg-icon ${props.className}`
  }
  return 'svg-icon'
})
</script>

<template>
  <div
    class="svg-container"
    v-if="staticContent"
    v-html="staticContent"
    :class="svgClass"
    aria-hidden="true"
  ></div>
  <slot v-else />
</template>
<style lang="less" scope>
.svg-icon {
  overflow: hidden;
  vertical-align: -0.1em; /* 因icon大小被设置为和字体大小一致，而span等标签的下边缘会和字体的基线对齐，故需设置一个往下的偏移比例，来纠正视觉上的未对齐效果 */
  fill: currentcolor; /* 定义元素的颜色，currentColor是一个变量，这个变量的值就表示当前元素的color值，如果当前元素未设置color值，则从父元素继承 */
}
.svg-container {
  width: 100%;
  height: 100%;
  svg {
    overflow: hidden;
    vertical-align: -0.1em;
    fill: currentcolor;
    width: 100%;
    height: 100%;
  }
}
</style>
