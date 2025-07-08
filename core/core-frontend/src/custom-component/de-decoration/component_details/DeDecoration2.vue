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
import { cloneDeep, merge } from 'lodash-es'

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
      width: '320px',
      height: '240px'
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
  mergedColor.value = merge(cloneDeep(defaultColor.value), props.color || []) as string[]
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
