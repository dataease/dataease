<template>
  <div class="dv-decoration-4" :style="border_style" :ref="refName">
    <div :class="['container', reverse ? 'reverse' : 'normal']" :style="containerStyle">
      <svg :width="reverse ? width : 5" :height="reverse ? 5 : height">
        <polyline
          :stroke="mergedColor[0]"
          :points="reverse ? `0, 2.5 ${width}, 2.5` : `2.5, 0 2.5, ${height}`"
        />
        <polyline
          class="bold-line"
          :stroke="mergedColor[1]"
          stroke-width="3"
          stroke-dasharray="20, 80"
          stroke-dashoffset="-30"
          :points="reverse ? `0, 2.5 ${width}, 2.5` : `2.5, 0 2.5, ${height}`"
        />
      </svg>
    </div>
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

const refName = ref('decoration-4')
const defaultColor = ref(['rgba(255, 255, 255, 0.3)', 'rgba(255, 255, 255, 0.3)'])
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

const mergeColor = () => {
  mergedColor.value = merge(cloneDeep(defaultColor.value), props.color || []) as string[]
}

// Computed properties
const containerStyle = computed(() => ({
  width: props.reverse ? `${width.value}px` : '5px',
  height: props.reverse ? '5px' : `${height.value}px`,
  animationDuration: `${props.dur}s`
}))

// Watchers
watch(() => props.color, mergeColor)

// Lifecycle
onMounted(mergeColor)
</script>

<style lang="less">
.dv-decoration-4 {
  position: relative;
  width: 100%;
  height: 100%;

  .container {
    display: flex;
    overflow: hidden;
    position: absolute;
    flex: 1;

    &.normal {
      animation: ani-height ease-in-out infinite;
      left: 50%;
      margin-left: -2px;
    }

    &.reverse {
      animation: ani-width ease-in-out infinite;
      top: 50%;
      margin-top: -2px;
    }
  }

  @keyframes ani-height {
    0% {
      height: 0%;
    }

    70% {
      height: 100%;
    }

    100% {
      height: 100%;
    }
  }

  @keyframes ani-width {
    0% {
      width: 0%;
    }

    70% {
      width: 100%;
    }

    100% {
      width: 100%;
    }
  }
}
</style>
