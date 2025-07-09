<template>
  <div class="dv-decoration-5" :style="border_style" :ref="refName">
    <svg :width="width" :height="height">
      <polyline fill="transparent" :stroke="mergedColor[0]" stroke-width="3" :points="line1Points">
        <animate
          attributeName="stroke-dasharray"
          attributeType="XML"
          :from="`0, ${line1Length / 2}, 0, ${line1Length / 2}`"
          :to="`0, 0, ${line1Length}, 0`"
          :dur="`${dur}s`"
          begin="0s"
          calcMode="spline"
          keyTimes="0;1"
          keySplines="0.4,1,0.49,0.98"
          repeatCount="indefinite"
        />
      </polyline>
      <polyline fill="transparent" :stroke="mergedColor[1]" stroke-width="2" :points="line2Points">
        <animate
          attributeName="stroke-dasharray"
          attributeType="XML"
          :from="`0, ${line2Length / 2}, 0, ${line2Length / 2}`"
          :to="`0, 0, ${line2Length}, 0`"
          :dur="`${dur}s`"
          begin="0s"
          calcMode="spline"
          keyTimes="0;1"
          keySplines=".4,1,.49,.98"
          repeatCount="indefinite"
        />
      </polyline>
    </svg>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import {
  customMergeColor,
  getPointDistances
} from '@/custom-component/de-decoration/component_details/config'
import { cloneDeep, sum } from 'lodash-es'

interface Props {
  color?: string[]
  curStyle: object
  scale: number
  dur?: number
}

const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  curStyle: () => {
    return {
      width: 320,
      height: 240
    }
  },
  dur: 3
})

const refName = ref('decoration-5')
const line1Points = ref('')
const line2Points = ref('')
const line1Length = ref(0)
const line2Length = ref(0)
const defaultColor = ref(['#3f96a5', '#3f96a5'])
const mergedColor = ref<string[]>([])

const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)

const border_style = computed(() => {
  return {
    width: `${width.value}px`,
    height: `${height.value}px`,
    zoom: props.scale
  }
})

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

const calcSVGData = () => {
  const line1PointsArray = [
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

  const line2PointsArray = [
    [width.value * 0.3, height.value * 0.8],
    [width.value * 0.7, height.value * 0.8]
  ]

  line1Length.value = sum(getPointDistances(line1PointsArray))
  line2Length.value = sum(getPointDistances(line2PointsArray))

  line1Points.value = line1PointsArray.map(point => point.join(',')).join(' ')
  line2Points.value = line2PointsArray.map(point => point.join(',')).join(' ')
}

// Watchers
watch(() => props.color, mergeColor)
watch([width, height], calcSVGData)

// Lifecycle
onMounted(() => {
  mergeColor()
  calcSVGData()
})
</script>

<style lang="less">
.dv-decoration-5 {
  width: 100%;
  height: 100%;
}
</style>
