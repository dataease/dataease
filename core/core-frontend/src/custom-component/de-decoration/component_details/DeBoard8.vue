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
  duration: 3
})

const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)
const dur = computed(() => props.duration)

const defaultColor = ref(['#2862b7', '#2862b7'])
const mergedColor = ref<string[]>([])

// 生成唯一ID
const generateId = () => Math.random().toString(36).substring(2, 11)
const pathId = `path-${generateId()}`
const gradientId = `gradient-${generateId()}`
const maskId = `mask-${generateId()}`

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

const pathD = computed(() => {
  return `M2.5,2.5 L${width.value - 2.5},2.5 L${width.value - 2.5},${height.value - 2.5} L2.5,${
    height.value - 2.5
  } L2.5,2.5`
})

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
  <div class="dv-border-box-8" :style="border_style" ref="dv-border-box-8">
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <defs>
        <!-- 优化路径定义 -->
        <path :id="pathId" :d="pathD" fill="none" />

        <!-- 优化渐变定义 -->
        <radialGradient :id="gradientId" cx="50%" cy="50%" r="50%">
          <stop offset="0%" :stop-color="mergedColor[1]" stop-opacity="0.8" />
          <stop offset="100%" :stop-color="mergedColor[1]" stop-opacity="0" />
        </radialGradient>

        <!-- 优化遮罩定义 -->
        <mask :id="maskId">
          <circle cx="0" cy="0" r="120" :fill="`url(#${gradientId})`">
            <animateMotion
              :dur="`${dur}s`"
              :path="pathD"
              rotate="auto"
              repeatCount="indefinite"
              calcMode="linear"
            />
          </circle>
        </mask>
      </defs>

      <!-- 背景多边形 -->
      <polygon
        :fill="backgroundColor"
        :points="`5,5 ${width - 5},5 ${width - 5} ${height - 5} 5,${height - 5}`"
      />

      <!-- 静态边框 -->
      <use :stroke="mergedColor[0]" stroke-width="1" :href="`#${pathId}`" stroke-linecap="round" />

      <!-- 动态发光边框 -->
      <use
        :stroke="mergedColor[1]"
        stroke-width="3"
        :href="`#${pathId}`"
        :mask="`url(#${maskId})`"
        stroke-linecap="round"
      >
        <animate
          attributeName="stroke-dasharray"
          :values="`0,${length};${length},0`"
          :dur="`${dur}s`"
          repeatCount="indefinite"
          calcMode="linear"
        />
      </use>
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-8 {
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

    & > polygon {
      vector-effect: non-scaling-stroke;
    }
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    isolation: isolate; /* 创建新的层叠上下文 */
    z-index: 1;
  }
}
</style>
