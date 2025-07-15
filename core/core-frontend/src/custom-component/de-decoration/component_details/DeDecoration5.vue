<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep } from 'lodash-es'

interface Props {
  color?: string[]
  curStyle: { width: number; height: number }
  scale: number
  dur?: number
  line1Width?: number
  line2Width?: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => ({
    width: 320,
    height: 240
  }),
  dur: 3,
  line1Width: 3,
  line2Width: 2
})

// 尺寸计算
const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)

// 样式计算
const border_style = computed(() => ({
  width: `${width.value}px`,
  height: `${height.value}px`,
  transform: `scale(${props.scale})`,
  'transform-origin': '0 0',
  'will-change': 'transform' // 提示浏览器优化
}))

// 颜色配置
const defaultColor = ref(['#3f96a5', '#3f96a5'])
const mergedColor = ref<string[]>([])

// 路径数据
const svgPaths = computed(() => {
  const line1Points = [
    [0, height.value * 0.2],
    [width.value * 0.18, height.value * 0.2],
    [width.value * 0.2, height.value * 0.4],
    [width.value * 0.25, height.value * 0.4],
    [width.value * 0.27, height.value * 0.6],
    [width.value * 0.72, height.value * 0.6],
    [width.value * 0.75, height.value * 0.4],
    [width.value * 0.8, height.value * 0.4],
    [width.value * 0.82, height.value * 0.2],
    [width.value, height.value * 0.2]
  ]

  const line2Points = [
    [width.value * 0.3, height.value * 0.8],
    [width.value * 0.7, height.value * 0.8]
  ]

  // 计算路径长度（简化版，实际项目中可以使用更精确的计算）
  const calculateLength = (points: number[][]) => {
    let length = 0
    for (let i = 1; i < points.length; i++) {
      const [x1, y1] = points[i - 1]
      const [x2, y2] = points[i]
      length += Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
    }
    return length
  }

  return {
    line1: {
      points: line1Points.map(p => p.join(',')).join(' '),
      length: calculateLength(line1Points),
      halfLength: calculateLength(line1Points) / 2
    },
    line2: {
      points: line2Points.map(p => p.join(',')).join(' '),
      length: calculateLength(line2Points),
      halfLength: calculateLength(line2Points) / 2
    }
  }
})

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

// 生命周期和监听
onMounted(mergeColor)
watch(() => props.color, mergeColor, { immediate: true })
</script>

<template>
  <div class="dv-decoration-5" :style="border_style">
    <svg :width="width" :height="height" class="decoration-svg">
      <!-- 主线条动画 -->
      <polyline
        fill="transparent"
        :stroke="mergedColor[0]"
        :stroke-width="line1Width"
        stroke-linecap="round"
        :points="svgPaths.line1.points"
      >
        <animate
          attributeName="stroke-dasharray"
          :values="`0,${svgPaths.line1.halfLength},0,${svgPaths.line1.halfLength};0,0,${svgPaths.line1.length},0`"
          :dur="`${dur}s`"
          calcMode="spline"
          keyTimes="0;1"
          keySplines="0.4,1,0.49,0.98"
          repeatCount="indefinite"
        />
      </polyline>

      <!-- 副线条动画 -->
      <polyline
        fill="transparent"
        :stroke="mergedColor[1]"
        :stroke-width="line2Width"
        stroke-linecap="round"
        :points="svgPaths.line2.points"
      >
        <animate
          attributeName="stroke-dasharray"
          :values="`0,${svgPaths.line2.halfLength},0,${svgPaths.line2.halfLength};0,0,${svgPaths.line2.length},0`"
          :dur="`${dur}s`"
          calcMode="spline"
          keyTimes="0;1"
          keySplines="0.4,1,0.49,0.98"
          repeatCount="indefinite"
        />
      </polyline>
    </svg>
  </div>
</template>

<style lang="less">
.dv-decoration-5 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .decoration-svg {
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none;

    polyline {
      transition: stroke 0.3s ease;
    }
  }
}
</style>
