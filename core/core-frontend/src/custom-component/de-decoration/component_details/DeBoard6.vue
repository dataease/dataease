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
  <div class="dv-border-box-6" :style="border_style" ref="dv-border-box-6">
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <polygon
        :fill="backgroundColor"
        :points="`
        9, 7 ${width - 9}, 7 ${width - 9}, ${height - 7} 9, ${height - 7}
      `"
      />

      <!-- 优化圆点渲染 -->
      <g class="corner-dots">
        <circle :fill="mergedColor[1]" cx="5" cy="5" r="2" />
        <circle :fill="mergedColor[1]" :cx="width - 5" cy="5" r="2" />
        <circle :fill="mergedColor[1]" :cx="width - 5" :cy="height - 5" r="2" />
        <circle :fill="mergedColor[1]" cx="5" :cy="height - 5" r="2" />
      </g>

      <!-- 水平线 -->
      <polyline
        class="horizontal-line"
        :stroke="mergedColor[0]"
        :points="`10, 4 ${width - 10}, 4`"
      />
      <polyline
        class="horizontal-line"
        :stroke="mergedColor[0]"
        :points="`10, ${height - 4} ${width - 10}, ${height - 4}`"
      />

      <!-- 垂直线 -->
      <polyline
        class="vertical-line"
        :stroke="mergedColor[0]"
        :points="`5, 70 5, ${height - 70}`"
      />
      <polyline
        class="vertical-line"
        :stroke="mergedColor[0]"
        :points="`${width - 5}, 70 ${width - 5}, ${height - 70}`"
      />

      <!-- 装饰短线 -->
      <g class="decoration-lines">
        <polyline :stroke="mergedColor[0]" :points="`3, 10, 3, 50`" />
        <polyline :stroke="mergedColor[0]" :points="`7, 30 7, 80`" />
        <polyline :stroke="mergedColor[0]" :points="`${width - 3}, 10 ${width - 3}, 50`" />
        <polyline :stroke="mergedColor[0]" :points="`${width - 7}, 30 ${width - 7}, 80`" />
        <polyline :stroke="mergedColor[0]" :points="`3, ${height - 10} 3, ${height - 50}`" />
        <polyline :stroke="mergedColor[0]" :points="`7, ${height - 30} 7, ${height - 80}`" />
        <polyline
          :stroke="mergedColor[0]"
          :points="`${width - 3}, ${height - 10} ${width - 3}, ${height - 50}`"
        />
        <polyline
          :stroke="mergedColor[0]"
          :points="`${width - 7}, ${height - 30} ${width - 7}, ${height - 80}`"
        />
      </g>
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-6 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .dv-border-svg-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none; /* 禁用鼠标事件 */

    /* 公共线条样式 */
    & > polyline {
      fill: none;
      stroke-width: 1;
      vector-effect: non-scaling-stroke; /* 保持线条宽度不受缩放影响 */
    }

    /* 角点样式 */
    .corner-dots > circle {
      animation: dot-pulse 2s ease-in-out infinite;
    }

    /* 水平线样式 */
    .horizontal-line {
      stroke-dasharray: none;
      stroke-linecap: round;
    }

    /* 垂直线样式 */
    .vertical-line {
      stroke-dasharray: 5 2;
      stroke-linecap: round;
    }

    /* 装饰短线样式 */
    .decoration-lines > polyline {
      stroke-dasharray: 3 3;
      animation: line-flicker 1.5s ease-in-out infinite alternate;
    }
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    isolation: isolate; /* 创建新的层叠上下文 */
  }
}

/* 动画定义 */
@keyframes dot-pulse {
  0%,
  100% {
    r: 2;
    opacity: 0.8;
  }
  50% {
    r: 3;
    opacity: 1;
  }
}

@keyframes line-flicker {
  0% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}
</style>
