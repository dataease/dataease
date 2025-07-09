<template>
  <div class="dv-decoration-2" :style="border_style" :ref="refName">
    <svg :width="`${width}px`" :height="`${height}px`">
      <rect :x="x" :y="y" :width="w" :height="h" :fill="mergedColor[0]">
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

      <rect :x="x" :y="y" width="1" height="1" :fill="mergedColor[1]">
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

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'
import { cloneDeep } from 'lodash-es'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'

interface Props {
  color?: string[]
  curStyle: object
  scale: number
  reverse?: boolean
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
  reverse: false,
  dur: 6
})

const refName = ref('decoration-2')
const x = ref(0)
const y = ref(0)
const w = ref(0)
const h = ref(0)
const defaultColor = ref(['#3faacb', '#fff'])
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

const calcSVGData = () => {
  if (props.reverse) {
    w.value = 1
    h.value = height.value
    x.value = width.value / 2
    y.value = 0
  } else {
    w.value = width.value
    h.value = 1
    x.value = 0
    y.value = height.value / 2
  }
}

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

// Watchers
watch(
  () => props.color,
  () => {
    mergeColor()
  }
)

watch(
  () => props.reverse,
  () => {
    calcSVGData()
  }
)

watch([width, height], () => {
  calcSVGData()
})

// Lifecycle hooks
onMounted(() => {
  mergeColor()
  calcSVGData()
})
</script>

<style lang="less">
.dv-decoration-2 {
  display: flex;
  width: 100%;
  height: 100%;
  justify-content: center;
  align-items: center;
}
</style>
