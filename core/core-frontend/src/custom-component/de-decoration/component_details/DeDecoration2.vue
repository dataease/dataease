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
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => ({
    width: 320,
    height: 240
  }),
  reverse: false,
  dur: 6
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
const defaultColor = ref(['#3faacb', '#fff'])
const mergedColor = ref<string[]>([])

// SVG元素位置和尺寸
const svgElements = computed(() => ({
  x: props.reverse ? width.value / 2 : 0,
  y: props.reverse ? 0 : height.value / 2,
  w: props.reverse ? 1 : width.value,
  h: props.reverse ? height.value : 1
}))

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

// 监听器
watch(() => props.color, mergeColor, { immediate: true })
watch(() => props.reverse, mergeColor)

// 生命周期
onMounted(mergeColor)
</script>

<template>
  <div class="dv-decoration-2" :style="border_style">
    <svg :width="`${width}px`" :height="`${height}px`" class="decoration-svg">
      <!-- 主矩形动画 -->
      <rect
        :x="svgElements.x"
        :y="svgElements.y"
        :width="svgElements.w"
        :height="svgElements.h"
        :fill="mergedColor[0]"
        class="main-rect"
      >
        <animate
          :attributeName="reverse ? 'height' : 'width'"
          from="0"
          :to="reverse ? height : width"
          :dur="`${dur}s`"
          calcMode="spline"
          keyTimes="0;1"
          keySplines=".42,0,.58,1"
          repeatCount="indefinite"
        />
      </rect>

      <!-- 小点动画 -->
      <rect
        :x="svgElements.x"
        :y="svgElements.y"
        width="1"
        height="1"
        :fill="mergedColor[1]"
        class="dot-rect"
      >
        <animate
          :attributeName="reverse ? 'y' : 'x'"
          from="0"
          :to="reverse ? height : width"
          :dur="`${dur}s`"
          calcMode="spline"
          keyTimes="0;1"
          keySplines="0.42,0,0.58,1"
          repeatCount="indefinite"
        />
      </rect>
    </svg>
  </div>
</template>

<style lang="less">
.dv-decoration-2 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .decoration-svg {
    position: absolute;
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none;

    .main-rect {
      opacity: 0.8;
      transition: fill 0.3s ease;
    }

    .dot-rect {
      rx: 50%; /* 圆形点 */
      transition: fill 0.3s ease;
    }
  }
}
</style>
