<script lang="tsx" setup>
import { ref, watch, onMounted, computed } from 'vue'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep } from 'lodash-es'

interface Props {
  color?: string[]
  backgroundColor?: string
  curStyle: object
  scale: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  backgroundColor: 'transparent',
  curStyle: () => ({
    width: 320,
    height: 240
  })
})

const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)

const defaultColor = ref(['#4fd2dd', '#235fa7'])
const mergedColor = ref<string[]>([])

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

const border_style = computed(() => {
  return {
    width: `${width.value}px`,
    height: `${height.value}px`,
    transform: `scale(${props.scale})`,
    'transform-origin': '0 0',
    'will-change': 'transform' // 提示浏览器准备变换
  }
})

watch(() => props.color, mergeColor)
onMounted(mergeColor)
</script>

<template>
  <!-- 保持原有模板结构不变 -->
  <div class="dv-border-box-2" :style="border_style" :ref="ref">
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <polygon
        :fill="backgroundColor"
        :points="`
        7, 7 ${width - 7}, 7 ${width - 7}, ${height - 7} 7, ${height - 7}
      `"
      />
      <polyline
        :stroke="mergedColor[0]"
        :points="`2, 2 ${width - 2} ,2 ${width - 2}, ${height - 2} 2, ${height - 2} 2, 2`"
      />
      <polyline
        :stroke="mergedColor[1]"
        :points="`6, 6 ${width - 6}, 6 ${width - 6}, ${height - 6} 6, ${height - 6} 6, 6`"
      />
      <circle :fill="mergedColor[0]" cx="11" cy="11" r="1" />
      <circle :fill="mergedColor[0]" :cx="width - 11" cy="11" r="1" />
      <circle :fill="mergedColor[0]" :cx="width - 11" :cy="height - 11" r="1" />
      <circle :fill="mergedColor[0]" cx="11" :cy="height - 11" r="1" />
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-2 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .dv-border-svg-container {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    /* 优化SVG渲染 */
    shape-rendering: optimizeSpeed;
    pointer-events: none; /* 禁用鼠标事件 */

    & > polyline {
      fill: none;
      stroke-width: 1;
      vector-effect: non-scaling-stroke; /* 保持线条宽度不受缩放影响 */
    }

    & > circle {
      will-change: transform; /* 优化小圆点渲染 */
    }
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    isolation: isolate; /* 创建新的层叠上下文 */

    /* 如果内容不需要交互也可以添加 */
    /* pointer-events: none; */
  }
}
</style>
