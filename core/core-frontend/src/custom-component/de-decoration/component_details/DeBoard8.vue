<template>
  <div class="dv-border-box-8" :style="border_style" ref="dv-border-box-8">
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <defs>
        <path :id="path" :d="pathD" fill="transparent" />
        <radialGradient :id="gradient" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#fff" stop-opacity="1" />
          <stop offset="100%" stop-color="#fff" stop-opacity="0" />
        </radialGradient>

        <mask :id="mask">
          <circle cx="0" cy="0" r="150" :fill="`url(#${gradient})`">
            <animateMotion :dur="`${dur}s`" :path="pathD" rotate="auto" repeatCount="indefinite" />
          </circle>
        </mask>
      </defs>

      <polygon
        :fill="backgroundColor"
        :points="`5, 5 ${width - 5}, 5 ${width - 5} ${height - 5} 5, ${height - 5}`"
      />

      <use :stroke="mergedColor[0]" stroke-width="1" :xlink:href="`#${path}`" />

      <use
        :stroke="mergedColor[1]"
        stroke-width="3"
        :xlink:href="`#${path}`"
        :mask="`url(#${mask})`"
      >
        <animate
          attributeName="stroke-dasharray"
          :from="`0, ${length}`"
          :to="`${length}, 0`"
          :dur="`${dur}s`"
          repeatCount="indefinite"
        />
      </use>
    </svg>

    <div class="border-box-content">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="tsx" setup>
import { ref, watch, onMounted, computed } from 'vue'
interface Props {
  color?: string[]
  backgroundColor?: string
  curStyle: object
  scale: number
}
const props = withDefaults(defineProps<Props>(), {
  color: () => [],
  backgroundColor: 'transparent',
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
const defaultColor = ref(['#2862b7', '#2862b7'])
const mergedColor = ref<string[]>([])
import { cloneDeep, merge } from 'lodash-es'
import { uuid } from 'vue-uuid'

const id = uuid
const path = `border-box-8-path-${id}`
const gradient = `border-box-8-gradient-${id}`
const mask = `border-box-8-mask-${id}`
const mergeColor = () => {
  mergedColor.value = merge(cloneDeep(defaultColor.value), props.color || []) as string[]
}

const length = computed(() => {
  return (width.value + height.value - 5) * 2
})

const reverse = computed(() => false)

const pathD = computed(() => {
  if (reverse.value)
    return `M 2.5, 2.5 L 2.5, ${height.value - 2.5} L ${width.value - 2.5}, ${
      height.value - 2.5
    } L ${width.value - 2.5}, 2.5 L 2.5, 2.5`

  return `M2.5, 2.5 L${width.value - 2.5}, 2.5 L${width.value - 2.5}, ${height.value - 2.5} L2.5, ${
    height.value - 2.5
  } L2.5, 2.5`
})

const border_style = computed(() => {
  return {
    width: `${width.value}px`,
    height: `${height.value}px`,
    zoom: props.scale
  }
})

watch(
  () => props.color,
  () => {
    mergeColor()
  }
)

onMounted(() => {
  mergeColor()
})
</script>

<style lang="less">
.dv-border-box-8 {
  position: relative;
  width: 100%;
  height: 100%;

  .dv-border-svg-container {
    position: absolute;
    width: 100%;
    height: 100%;
    left: 0px;
    top: 0px;
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
  }
}
</style>
