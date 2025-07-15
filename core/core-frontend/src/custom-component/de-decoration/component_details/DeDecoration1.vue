<template>
  <div class="dv-decoration-1" :style="border_style" :ref="refName">
    <svg
      :width="`${svgWH[0]}px`"
      :height="`${svgWH[1]}px`"
      :style="`transform:scale(${svgScale[0]},${svgScale[1]});`"
    >
      <template v-for="(point, i) in points" :key="i">
        <rect
          v-if="Math.random() > 0.6"
          :fill="mergedColor[0]"
          :x="point[0] - halfPointSideLength"
          :y="point[1] - halfPointSideLength"
          :width="pointSideLength"
          :height="pointSideLength"
        >
          <animate
            v-if="Math.random() > 0.6"
            attributeName="fill"
            :values="`${mergedColor[0]};transparent`"
            dur="1s"
            :begin="Math.random() * 2"
            repeatCount="indefinite"
          />
        </rect>
      </template>

      <rect
        v-if="rects[0]"
        :fill="mergedColor[1]"
        :x="rects[0][0] - pointSideLength"
        :y="rects[0][1] - pointSideLength"
        :width="pointSideLength * 2"
        :height="pointSideLength * 2"
      >
        <animate
          attributeName="width"
          :values="`0;${pointSideLength * 2}`"
          dur="2s"
          repeatCount="indefinite"
        />
        <animate
          attributeName="height"
          :values="`0;${pointSideLength * 2}`"
          dur="2s"
          repeatCount="indefinite"
        />
        <animate
          attributeName="x"
          :values="`${rects[0][0]};${rects[0][0] - pointSideLength}`"
          dur="2s"
          repeatCount="indefinite"
        />
        <animate
          attributeName="y"
          :values="`${rects[0][1]};${rects[0][1] - pointSideLength}`"
          dur="2s"
          repeatCount="indefinite"
        />
      </rect>

      <rect
        v-if="rects[1]"
        :fill="mergedColor[1]"
        :x="rects[1][0] - 40"
        :y="rects[1][1] - pointSideLength"
        :width="40"
        :height="pointSideLength * 2"
      >
        <animate attributeName="width" values="0;40;0" dur="2s" repeatCount="indefinite" />
        <animate
          attributeName="x"
          :values="`${rects[1][0]};${rects[1][0] - 40};${rects[1][0]}`"
          dur="2s"
          repeatCount="indefinite"
        />
      </rect>
    </svg>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { cloneDeep } from 'lodash-es'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'

interface Props {
  color?: string[]
  curStyle: object
  scale: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => {
    return {
      width: 320,
      height: 240
    }
  }
})

const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)

const border_style = computed(() => ({
  width: `${width.value}px`,
  height: `${height.value}px`,
  transform: `scale(${props.scale})`,
  'transform-origin': '0 0',
  'will-change': 'transform' // 提示浏览器优化
}))

const pointSideLength = 2.5
const refName = ref('decoration-1')
const svgWH = ref([200, 50])
const svgScale = ref([1, 1])
const rowNum = 4
const rowPoints = 20
const halfPointSideLength = computed(() => pointSideLength / 2)
const points = ref<number[][]>([])
const rects = ref<number[][]>([])
const defaultColor = ref(['#fff', '#0de7c2'])
const mergedColor = ref<string[]>([])

const calcPointsPosition = () => {
  const [w, h] = svgWH.value
  const horizontalGap = w / (rowPoints + 1)
  const verticalGap = h / (rowNum + 1)

  let pointsArray = new Array(rowNum)
    .fill(0)
    .map((_, i) =>
      new Array(rowPoints).fill(0).map((_, j) => [horizontalGap * (j + 1), verticalGap * (i + 1)])
    )

  points.value = pointsArray.reduce((all, item) => [...all, ...item], [])
}

const calcRectsPosition = () => {
  const rect1 = points.value[rowPoints * 2 - 1]
  const rect2 = points.value[rowPoints * 2 - 3]
  rects.value = [rect1, rect2]
}

const calcScale = () => {
  const [w, h] = svgWH.value
  svgScale.value = [width.value / w, height.value / h]
}

const calcSVGData = () => {
  calcPointsPosition()
  calcRectsPosition()
  calcScale()
}

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

const onResize = () => {
  calcSVGData()
}

watch(
  () => props.color,
  () => {
    mergeColor()
  }
)

onMounted(() => {
  mergeColor()
  calcSVGData()
})

// Handle autoResize mixin
// Note: In Vue 3, consider converting autoResize to a composable
watch([width, height], () => {
  onResize()
})
</script>

<style lang="less">
.dv-decoration-1 {
  position: relative;
  width: 100%;
  height: 100%;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  contain: content; /* 限制重绘范围 */

  svg {
    position: absolute;
    transform-origin: left top;
    /* 优化SVG渲染 */
    shape-rendering: optimizeSpeed;
    pointer-events: none;
  }
}
</style>
