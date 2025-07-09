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

const reverse = computed(() => false)

const defaultColor = ref(['#2862b7', '#2862b7'])
const mergedColor = ref<string[]>([])
import { cloneDeep } from 'lodash-es'
import { customMergeColor } from '@/custom-component/de-decoration/component_details/config'

const mergeColor = () => {
  mergedColor.value = customMergeColor(cloneDeep(defaultColor.value), props.color)
}

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
.dv-border-box-4 {
  position: relative;
  width: 100%;
  height: 100%;

  .dv-reverse {
    transform: rotate(180deg);
  }

  .dv-border-svg-container {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0px;
    left: 0px;

    & > polyline {
      fill: none;
    }
  }

  .sw1 {
    stroke-width: 1;
  }

  .sw3 {
    stroke-width: 3px;
    stroke-linecap: round;
  }

  .dv-bb4-line-1 {
    .sw1;
  }

  .dv-bb4-line-2 {
    .sw1;
  }

  .dv-bb4-line-3 {
    .sw3;
  }

  .dv-bb4-line-4 {
    .sw3;
  }

  .dv-bb4-line-5 {
    .sw1;
  }

  .dv-bb4-line-6 {
    .sw1;
  }

  .dv-bb4-line-7 {
    .sw1;
  }

  .dv-bb4-line-8 {
    .sw3;
  }

  .dv-bb4-line-9 {
    .sw3;
    stroke-dasharray: 100 250;
  }

  .dv-bb4-line-10 {
    .sw1;
    stroke-dasharray: 80 270;
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
  }
}
</style>
