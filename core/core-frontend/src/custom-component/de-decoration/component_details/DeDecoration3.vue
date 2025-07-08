<template>
  <div class="dv-decoration-3" :style="border_style" :ref="refName">
    <svg
      :width="`${svgWH[0]}px`"
      :height="`${svgWH[1]}px`"
      :style="`transform:scale(${svgScale[0]},${svgScale[1]});`"
    >
      <rect
        v-for="(point, i) in points"
        :key="i"
        :fill="mergedColor[0]"
        :x="point[0] - halfPointSideLength"
        :y="point[1] - halfPointSideLength"
        :width="pointSideLength"
        :height="pointSideLength"
      >
        <animate
          v-if="Math.random() > 0.6"
          attributeName="fill"
          :values="mergedColor.join(';')"
          :dur="`${Math.random() + 1}s`"
          :begin="`${Math.random() * 2}s`"
          repeatCount="indefinite"
        />
      </rect>
    </svg>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { cloneDeep, merge } from 'lodash-es'

interface Props {
  color?: string[]
  curStyle: object
  scale: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => {
    return {
      width: '320px',
      height: '240px'
    }
  }
})

const width = computed(() => {
  return parseInt(props.curStyle.width) / props.scale
})

const height = computed(() => {
  return parseInt(props.curStyle.height) / props.scale
})

const border_style = computed(() => {
  return {
    width: `${width.value}px`,
    height: `${height.value}px`,
    zoom: props.scale
  }
})

const mergeColor = () => {
  mergedColor.value = merge(cloneDeep(defaultColor.value), props.color || []) as string[]
}

// Constants
const pointSideLength = 7
const rowNum = 2
const rowPoints = 25

// Refs
const refName = ref('decoration-3')
const svgWH = ref([300, 35])
const svgScale = ref([1, 1])
const points = ref<number[][]>([])
const defaultColor = ref(['#7acaec', 'transparent'])
const mergedColor = ref<string[]>([])

// Computed
const halfPointSideLength = computed(() => pointSideLength / 2)

// Methods
const calcPointsPosition = () => {
  const [w, h] = svgWH.value
  const horizontalGap = w / (rowPoints + 1)
  const verticalGap = h / (rowNum + 1)

  const pointsArray = Array.from({ length: rowNum }, (_, i) =>
    Array.from({ length: rowPoints }, (_, j) => [horizontalGap * (j + 1), verticalGap * (i + 1)])
  )

  points.value = pointsArray.flat()
}

const calcScale = () => {
  const [w, h] = svgWH.value
  svgScale.value = [width.value / w, height.value / h]
}

const calcSVGData = () => {
  calcPointsPosition()
  calcScale()
}

const onResize = () => {
  calcSVGData()
}

// Watchers
watch(() => props.color, mergeColor)
watch([width, height], onResize)

// Lifecycle
onMounted(() => {
  mergeColor()
  calcSVGData()
})
</script>

<style lang="less">
.dv-decoration-3 {
  width: 100%;
  height: 100%;

  svg {
    transform-origin: left top;
  }
}
</style>
