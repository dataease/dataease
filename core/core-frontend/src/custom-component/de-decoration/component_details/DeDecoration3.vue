<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep } from 'lodash-es'

interface Props {
  color?: string[]
  curStyle: { width: number; height: number }
  scale: number
  animationRatio?: number // 新增：控制动画元素比例
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => ({
    width: 320,
    height: 240
  }),
  animationRatio: 0.6 // 默认60%的元素有动画
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

// 点阵配置
const pointSideLength = 7
const svgWH = ref([300, 35])
const rowNum = 2
const rowPoints = 25

// 颜色配置
const defaultColor = ref(['#7acaec', 'transparent'])
const mergedColor = ref<string[]>([])

// 计算属性
const halfPointSideLength = computed(() => pointSideLength / 2)
const svgScale = computed(() => [width.value / svgWH.value[0], height.value / svgWH.value[1]])

// 点阵位置
const points = ref<number[][]>([])

// 预生成动画配置（优化随机性能）
const animationConfigs = computed(() => {
  return points.value.map((_, i) => ({
    shouldAnimate: Math.random() <= props.animationRatio,
    duration: 1 + Math.random(), // 1-2秒
    delay: Math.random() * 2 // 0-2秒
  }))
})

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

// 计算点阵位置
const calcPointsPosition = () => {
  const [w, h] = svgWH.value
  const horizontalGap = w / (rowPoints + 1)
  const verticalGap = h / (rowNum + 1)

  points.value = Array.from({ length: rowNum }, (_, i) =>
    Array.from({ length: rowPoints }, (_, j) => [horizontalGap * (j + 1), verticalGap * (i + 1)])
  ).flat()
}

// 响应式更新
const updateLayout = () => {
  calcPointsPosition()
}

// 生命周期和监听
onMounted(() => {
  mergeColor()
  updateLayout()
})

watch(() => props.color, mergeColor, { immediate: true })
watch([width, height], updateLayout)
</script>

<template>
  <div class="dv-decoration-3" :style="border_style">
    <svg
      :width="`${svgWH[0]}px`"
      :height="`${svgWH[1]}px`"
      :style="`transform:scale(${svgScale[0]},${svgScale[1]});`"
      class="decoration-svg"
    >
      <rect
        v-for="(point, i) in points"
        :key="i"
        :fill="mergedColor[0]"
        :x="point[0] - halfPointSideLength"
        :y="point[1] - halfPointSideLength"
        :width="pointSideLength"
        :height="pointSideLength"
        rx="1"
        opacity="0.8"
      >
        <animate
          v-if="animationConfigs[i].shouldAnimate"
          attributeName="fill"
          :values="mergedColor.join(';')"
          :dur="`${animationConfigs[i].duration}s`"
          :begin="`${animationConfigs[i].delay}s`"
          repeatCount="indefinite"
          calcMode="linear"
        />
      </rect>
    </svg>
  </div>
</template>

<style lang="less">
.dv-decoration-3 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  .decoration-svg {
    position: absolute;
    transform-origin: left top;
    /* 优化SVG渲染 */
    shape-rendering: optimizeSpeed;
    pointer-events: none;

    rect {
      transition: fill 0.3s ease; /* 颜色变化过渡 */
    }
  }
}
</style>
