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
  const [primaryColor, secondaryColor] = mergedColor.value
  return {
    width: `${width.value}px`,
    height: `${height.value}px`,
    transform: `scale(${props.scale})`,
    'transform-origin': '0 0',
    'will-change': 'transform', // 提示浏览器优化
    'box-shadow': `inset 0 0 25px ${primaryColor}33`, // 添加透明度
    border: `1px solid ${primaryColor}`,
    'background-color': props.backgroundColor
  }
})

// 使用立即执行的watch并添加防抖
let debounceTimer: number
watch(
  () => props.color,
  () => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(mergeColor, 50)
  },
  { immediate: true }
)

onMounted(mergeColor)
</script>

<template>
  <div class="dv-border-box-7" :style="border_style" ref="dv-border-box-7">
    <!-- 使用分组减少DOM数量 -->
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <!-- 外层边框 -->
      <g class="outer-border">
        <polyline :stroke="mergedColor[0]" :points="`0, 25 0, 0 25, 0`" />
        <polyline :stroke="mergedColor[0]" :points="`${width - 25}, 0 ${width}, 0 ${width}, 25`" />
        <polyline
          :stroke="mergedColor[0]"
          :points="`${width - 25}, ${height} ${width}, ${height} ${width}, ${height - 25}`"
        />
        <polyline
          :stroke="mergedColor[0]"
          :points="`0, ${height - 25} 0, ${height} 25, ${height}`"
        />
      </g>

      <!-- 内层边框 -->
      <g class="inner-border">
        <polyline :stroke="mergedColor[1]" :points="`0, 10 0, 0 10, 0`" />
        <polyline :stroke="mergedColor[1]" :points="`${width - 10}, 0 ${width}, 0 ${width}, 10`" />
        <polyline
          :stroke="mergedColor[1]"
          :points="`${width - 10}, ${height} ${width}, ${height} ${width}, ${height - 10}`"
        />
        <polyline
          :stroke="mergedColor[1]"
          :points="`0, ${height - 10} 0, ${height} 10, ${height}`"
        />
      </g>
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less">
.dv-border-box-7 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */
  overflow: hidden; /* 防止内容溢出 */

  .dv-border-svg-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* 优化SVG渲染 */
    shape-rendering: crispEdges;
    pointer-events: none; /* 禁用鼠标事件 */

    & > g > polyline {
      fill: none;
      stroke-linecap: round;
      vector-effect: non-scaling-stroke; /* 保持线条宽度不受缩放影响 */
      transition: stroke 0.3s ease; /* 颜色变化动画 */
    }

    .outer-border > polyline {
      stroke-width: 2;
      animation: outer-glow 2s ease-in-out infinite alternate;
    }

    .inner-border > polyline {
      stroke-width: 5;
      animation: inner-glow 1.5s ease-in-out infinite alternate;
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

/* 动画定义 */
@keyframes outer-glow {
  0% {
    stroke-opacity: 0.7;
  }
  100% {
    stroke-opacity: 1;
  }
}

@keyframes inner-glow {
  0% {
    stroke-width: 4;
  }
  100% {
    stroke-width: 6;
  }
}
</style>
