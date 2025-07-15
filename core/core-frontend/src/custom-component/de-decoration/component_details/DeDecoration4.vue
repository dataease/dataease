<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep } from 'lodash-es'

interface Props {
  color?: string[]
  curStyle: { width: number; height: number }
  scale: number
  reverse?: boolean
  dur?: number
  strokeWidth?: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => ({
    width: 320,
    height: 240
  }),
  reverse: false,
  dur: 6,
  strokeWidth: 3
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
const defaultColor = ref(['rgba(255, 255, 255, 0.3)', 'rgba(255, 255, 255, 0.3)'])
const mergedColor = ref<string[]>([])

// 计算属性
const containerStyle = computed(() => ({
  width: props.reverse ? `${width.value}px` : `${props.strokeWidth}px`,
  height: props.reverse ? `${props.strokeWidth}px` : `${height.value}px`,
  '--animation-duration': `${props.dur}s`,
  '--stroke-width': `${props.strokeWidth}px`,
  '--half-stroke': `${props.strokeWidth / 2}px`
}))

const svgPoints = computed(() =>
  props.reverse
    ? `0, ${props.strokeWidth / 2} ${width.value}, ${props.strokeWidth / 2}`
    : `${props.strokeWidth / 2}, 0 ${props.strokeWidth / 2}, ${height.value}`
)

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

// 生命周期和监听
onMounted(mergeColor)
watch(() => props.color, mergeColor, { immediate: true })
</script>

<template>
  <div class="dv-decoration-4" :style="border_style">
    <div :class="['container', reverse ? 'reverse' : 'normal']" :style="containerStyle">
      <svg
        :width="reverse ? width : strokeWidth"
        :height="reverse ? strokeWidth : height"
        class="decoration-svg"
      >
        <!-- 细线 -->
        <polyline :stroke="mergedColor[0]" :points="svgPoints" stroke-linecap="round" />

        <!-- 粗虚线 -->
        <polyline
          class="bold-line"
          :stroke="mergedColor[1]"
          :stroke-width="strokeWidth"
          stroke-dasharray="20, 80"
          stroke-dashoffset="-30"
          :points="svgPoints"
          stroke-linecap="round"
        />
      </svg>
    </div>
  </div>
</template>

<style lang="less">
.dv-decoration-4 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .container {
    position: absolute;
    overflow: hidden;
    will-change: transform;

    &.normal {
      left: 50%;
      margin-left: calc(-1 * var(--half-stroke));
      animation: ani-height var(--animation-duration) ease-in-out infinite;
    }

    &.reverse {
      top: 50%;
      margin-top: calc(-1 * var(--half-stroke));
      animation: ani-width var(--animation-duration) ease-in-out infinite;
    }
  }

  .decoration-svg {
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none;

    polyline {
      transition: stroke 0.3s ease;
    }
  }

  @keyframes ani-height {
    0% {
      height: 0;
      transform: translateY(100%);
    }
    70%,
    100% {
      height: 100%;
      transform: translateY(0);
    }
  }

  @keyframes ani-width {
    0% {
      width: 0;
      transform: translateX(100%);
    }
    70%,
    100% {
      width: 100%;
      transform: translateX(0);
    }
  }
}
</style>
