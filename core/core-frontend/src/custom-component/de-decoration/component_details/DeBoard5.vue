<script lang="tsx" setup>
import { ref, watch, onMounted, computed } from 'vue'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep } from 'lodash-es'

interface Props {
  color?: string[]
  backgroundColor?: string
  curStyle: { width: number; height: number }
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
const reverse = computed(() => false)

const defaultColor = ref(['#2862b7', '#2862b7'])
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
    'will-change': 'transform' // 提示浏览器优化变换
  }
})

// 使用立即执行的watch
watch(() => props.color, mergeColor, { immediate: true })
onMounted(mergeColor)
</script>

<template>
  <div class="dv-border-box-5" :style="border_style" ref="dv-border-box-5">
    <svg
      :class="`dv-border-svg-container ${reverse && 'dv-reverse'}`"
      :width="width"
      :height="height"
    >
      <polygon
        :fill="backgroundColor"
        :points="`
        10, 22 ${width - 22}, 22 ${width - 22}, ${height - 86} ${width - 84}, ${height - 24} 10, ${
          height - 24
        }
      `"
      />

      <polyline
        class="dv-bb5-line-1"
        :stroke="mergedColor[0]"
        :points="`8, 5 ${width - 5}, 5 ${width - 5}, ${height - 100}
          ${width - 100}, ${height - 5} 8, ${height - 5} 8, 5`"
      />
      <polyline
        class="dv-bb5-line-2"
        :stroke="mergedColor[1]"
        :points="`3, 5 ${width - 20}, 5 ${width - 20}, ${height - 60}
          ${width - 74}, ${height - 5} 3, ${height - 5} 3, 5`"
      />
      <polyline
        class="dv-bb5-line-3"
        :stroke="mergedColor[1]"
        :points="`50, 13 ${width - 35}, 13`"
      />
      <polyline
        class="dv-bb5-line-4"
        :stroke="mergedColor[1]"
        :points="`15, 20 ${width - 35}, 20`"
      />
      <polyline
        class="dv-bb5-line-5"
        :stroke="mergedColor[1]"
        :points="`15, ${height - 20} ${width - 110}, ${height - 20}`"
      />
      <polyline
        class="dv-bb5-line-6"
        :stroke="mergedColor[1]"
        :points="`15, ${height - 13} ${width - 110}, ${height - 13}`"
      />
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-5 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .dv-reverse {
    transform: rotate(180deg);
    transform-origin: center;
  }

  .dv-border-svg-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none; /* 禁用鼠标事件 */

    & > polyline {
      fill: none;
      vector-effect: non-scaling-stroke; /* 保持线条宽度不受缩放影响 */
    }
  }

  /* 线条样式优化 */
  .dv-bb5-line-1 {
    stroke-width: 1;
    stroke-linecap: round;
    stroke-dasharray: none;
  }

  .dv-bb5-line-2 {
    stroke-width: 1;
    stroke-linecap: round;
    stroke-dasharray: 5 2; /* 添加虚线效果 */
  }

  .dv-bb5-line-3 {
    stroke-width: 5;
    stroke-linecap: round;
    animation: line-pulse 2s ease-in-out infinite;
  }

  .dv-bb5-line-4 {
    stroke-width: 2;
    stroke-linecap: square;
  }

  .dv-bb5-line-5 {
    stroke-width: 2;
    stroke-linecap: square;
    stroke-dasharray: 10 5; /* 添加虚线效果 */
  }

  .dv-bb5-line-6 {
    stroke-width: 5;
    stroke-linecap: round;
    animation: line-pulse 2s ease-in-out infinite reverse;
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    isolation: isolate; /* 创建新的层叠上下文 */
  }
}

@keyframes line-pulse {
  0%,
  100% {
    opacity: 0.8;
    stroke-width: 5;
  }
  50% {
    opacity: 1;
    stroke-width: 6;
  }
}
</style>
