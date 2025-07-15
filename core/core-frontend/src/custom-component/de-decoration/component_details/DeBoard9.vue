<script lang="tsx" setup>
import { ref, watch, onMounted, computed } from 'vue'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep } from 'lodash-es'

interface Props {
  color?: string[]
  backgroundColor?: string
  curStyle: { width: number; height: number }
  scale: number
  duration?: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  backgroundColor: 'transparent',
  curStyle: () => ({
    width: 320,
    height: 240
  }),
  duration: 10
})

const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)

// 生成更简洁的ID
const generateId = () => Math.random().toString(36).substring(2, 8)
const gradientId = `grad-${generateId()}`
const maskId = `mask-${generateId()}`

const defaultColor = ref(['#11eefd', '#0078d2'])
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
    'will-change': 'transform' // 提示浏览器优化
  }
})

// 计算关键点坐标
const points = computed(() => ({
  // 背景多边形点
  bgPoints: `
    15,9 ${width.value * 0.1 + 1},9 ${width.value * 0.1 + 4},6 ${width.value * 0.52 + 2},6
    ${width.value * 0.52 + 6},10 ${width.value * 0.58 - 7},10 ${width.value * 0.58 - 2},6
    ${width.value * 0.9 + 2},6 ${width.value * 0.9 + 6},10 ${width.value - 10},10 ${
    width.value - 10
  },${height.value * 0.1 - 6}
    ${width.value - 6},${height.value * 0.1 - 1} ${width.value - 6},${height.value * 0.8 + 1} ${
    width.value - 10
  },${height.value * 0.8 + 6}
    ${width.value - 10},${height.value - 10} ${width.value * 0.92 + 7},${height.value - 10} ${
    width.value * 0.92 + 2
  },${height.value - 6}
    11,${height.value - 6} 11,${height.value * 0.15 - 2} 15,${height.value * 0.15 - 7}
  `,
  // 遮罩多边形点
  maskPoints1: `8,${height.value * 0.4} 8,3 ${width.value * 0.4 + 7},3`,
  maskPoints2: `8,${height.value * 0.15} 8,3 ${width.value * 0.1 + 7},3 ${
    width.value * 0.1
  },8 14,8 14,${height.value * 0.15 - 7}`,
  maskPoints3: `${width.value * 0.5},3 ${width.value - 3},3 ${width.value - 3},${
    height.value * 0.25
  }`,
  maskPoints4: `${width.value * 0.52},3 ${width.value * 0.58},3 ${width.value * 0.58 - 7},9 ${
    width.value * 0.52 + 7
  },9`,
  maskPoints5: `${width.value * 0.9},3 ${width.value - 3},3 ${width.value - 3},${
    height.value * 0.1
  } ${width.value - 9},${height.value * 0.1 - 7} ${width.value - 9},9 ${width.value * 0.9 + 7},9`,
  maskPoints6: `8,${height.value * 0.5} 8,${height.value - 3} ${width.value * 0.3 + 7},${
    height.value - 3
  }`,
  maskPoints7: `8,${height.value * 0.55} 8,${height.value * 0.7} 2,${height.value * 0.7 - 7} 2,${
    height.value * 0.55 + 7
  }`,
  maskPoints8: `${width.value * 0.35},${height.value - 3} ${width.value - 3},${height.value - 3} ${
    width.value - 3
  },${height.value * 0.35}`,
  maskPoints9: `${width.value * 0.92},${height.value - 3} ${width.value - 3},${height.value - 3} ${
    width.value - 3
  },${height.value * 0.8} ${width.value - 9},${height.value * 0.8 + 7} ${width.value - 9},${
    height.value - 9
  } ${width.value * 0.92 + 7},${height.value - 9}`
}))

// 使用立即执行的watch
watch(() => props.color, mergeColor, { immediate: true })
onMounted(mergeColor)
</script>

<template>
  <div class="dv-border-box-9" :style="border_style" ref="dv-border-box-9">
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <defs>
        <!-- 优化渐变定义 -->
        <linearGradient :id="gradientId" x1="0%" y1="0%" x2="100%" y2="100%">
          <animate
            attributeName="x1"
            values="0%;100%;0%"
            :dur="`${props.duration}s`"
            calcMode="linear"
            repeatCount="indefinite"
          />
          <animate
            attributeName="x2"
            values="100%;0%;100%"
            :dur="`${props.duration}s`"
            calcMode="linear"
            repeatCount="indefinite"
          />
          <stop offset="0%" :stop-color="mergedColor[0]">
            <animate
              attributeName="stop-color"
              :values="`${mergedColor[0]};${mergedColor[1]};${mergedColor[0]}`"
              :dur="`${props.duration}s`"
              calcMode="linear"
              repeatCount="indefinite"
            />
          </stop>
          <stop offset="100%" :stop-color="mergedColor[1]">
            <animate
              attributeName="stop-color"
              :values="`${mergedColor[1]};${mergedColor[0]};${mergedColor[1]}`"
              :dur="`${props.duration}s`"
              calcMode="linear"
              repeatCount="indefinite"
            />
          </stop>
        </linearGradient>

        <!-- 优化遮罩定义 -->
        <mask :id="maskId">
          <polyline
            stroke="#fff"
            stroke-width="3"
            fill="transparent"
            :points="points.maskPoints1"
          />
          <polyline fill="#fff" :points="points.maskPoints2" />
          <polyline
            stroke="#fff"
            stroke-width="3"
            fill="transparent"
            :points="points.maskPoints3"
          />
          <polyline fill="#fff" :points="points.maskPoints4" />
          <polyline fill="#fff" :points="points.maskPoints5" />
          <polyline
            stroke="#fff"
            stroke-width="3"
            fill="transparent"
            :points="points.maskPoints6"
          />
          <polyline fill="#fff" :points="points.maskPoints7" />
          <polyline
            stroke="#fff"
            stroke-width="3"
            fill="transparent"
            :points="points.maskPoints8"
          />
          <polyline fill="#fff" :points="points.maskPoints9" />
        </mask>
      </defs>

      <!-- 背景多边形 -->
      <polygon :fill="backgroundColor" :points="points.bgPoints" />

      <!-- 渐变矩形 -->
      <rect
        x="0"
        y="0"
        :width="width"
        :height="height"
        :fill="`url(#${gradientId})`"
        :mask="`url(#${maskId})`"
      />
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-9 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */
  overflow: hidden;

  .dv-border-svg-container {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    /* 优化SVG渲染 */
    shape-rendering: optimizeSpeed;
    pointer-events: none; /* 禁用鼠标事件 */

    & > polygon,
    & > rect {
      vector-effect: non-scaling-stroke;
    }
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    isolation: isolate; /* 创建新的层叠上下文 */
    z-index: 1; /* 确保内容在边框上方 */
  }
}
</style>
