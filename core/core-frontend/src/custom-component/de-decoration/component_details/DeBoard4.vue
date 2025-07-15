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

const defaultColor = ref(['#805151', '#2862b7'])
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

watch(() => props.color, mergeColor, { immediate: true })
onMounted(mergeColor)
</script>

<template>
  <div class="dv-border-box-4" :style="border_style" :ref="ref">
    <svg
      :class="`dv-border-svg-container ${reverse && 'dv-reverse'}`"
      :width="width"
      :height="height"
    >
      <polygon
        :fill="backgroundColor"
        :points="`
        ${width - 15}, 22 170, 22 150, 7 40, 7 28, 21 32, 24
        16, 42 16, ${height - 32} 41, ${height - 7} ${width - 15}, ${height - 7}
      `"
      />

      <polyline
        class="dv-bb4-line-1"
        :stroke="mergedColor[0]"
        :points="`145, ${height - 5} 40, ${height - 5} 10, ${height - 35}
          10, 40 40, 5 150, 5 170, 20 ${width - 15}, 20`"
      />
      <polyline
        :stroke="mergedColor[1]"
        class="dv-bb4-line-2"
        :points="`245, ${height - 1} 36, ${height - 1} 14, ${height - 23}
          14, ${height - 100}`"
      />

      <polyline
        class="dv-bb4-line-3"
        :stroke="mergedColor[0]"
        :points="`7, ${height - 40} 7, ${height - 75}`"
      />
      <polyline class="dv-bb4-line-4" :stroke="mergedColor[0]" :points="`28, 24 13, 41 13, 64`" />
      <polyline class="dv-bb4-line-5" :stroke="mergedColor[0]" :points="`5, 45 5, 140`" />
      <polyline class="dv-bb4-line-6" :stroke="mergedColor[1]" :points="`14, 75 14, 180`" />
      <polyline
        class="dv-bb4-line-7"
        :stroke="mergedColor[1]"
        :points="`55, 11 147, 11 167, 26 250, 26`"
      />
      <polyline class="dv-bb4-line-8" :stroke="mergedColor[1]" :points="`158, 5 173, 16`" />
      <polyline
        class="dv-bb4-line-9"
        :stroke="mergedColor[0]"
        :points="`200, 17 ${width - 10}, 17`"
      />
      <polyline
        class="dv-bb4-line-10"
        :stroke="mergedColor[1]"
        :points="`385, 17 ${width - 10}, 17`"
      />
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-4 {
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
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none; /* 禁用鼠标事件 */

    & > polyline {
      fill: none;
      vector-effect: non-scaling-stroke; /* 保持线条宽度不受缩放影响 */
    }
  }

  .sw1 {
    stroke-width: 1;
    stroke-linecap: butt;
  }

  .sw3 {
    stroke-width: 3px;
    stroke-linecap: round;
  }

  .dv-bb4-line-1 {
    .sw1;
    stroke-dasharray: none;
  }

  .dv-bb4-line-2 {
    .sw1;
    stroke-dasharray: none;
  }

  .dv-bb4-line-3 {
    .sw3;
    stroke-dasharray: none;
  }

  .dv-bb4-line-4 {
    .sw3;
    stroke-dasharray: none;
  }

  .dv-bb4-line-5 {
    .sw1;
    stroke-dasharray: 2 2; /* 添加虚线效果 */
  }

  .dv-bb4-line-6 {
    .sw1;
    stroke-dasharray: 2 2; /* 添加虚线效果 */
  }

  .dv-bb4-line-7 {
    .sw1;
    stroke-dasharray: none;
  }

  .dv-bb4-line-8 {
    .sw3;
    stroke-dasharray: none;
  }

  .dv-bb4-line-9 {
    .sw3;
    stroke-dasharray: 10 5; /* 优化虚线样式 */
    animation: dash-animation 3s linear infinite;
  }

  .dv-bb4-line-10 {
    .sw1;
    stroke-dasharray: 8 6; /* 优化虚线样式 */
    animation: dash-animation 3s linear infinite reverse;
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    isolation: isolate; /* 创建新的层叠上下文 */
  }
}

@keyframes dash-animation {
  to {
    stroke-dashoffset: 15;
  }
}
</style>
