<template>
  <div class="dv-border-box-10" :style="border_style" ref="dv-border-box-10">
    <svg class="dv-border-svg-container" :width="width" :height="height">
      <polygon
        :fill="backgroundColor"
        :points="`
        4, 0 ${width - 4}, 0 ${width}, 4 ${width}, ${height - 4} ${width - 4}, ${height}
        4, ${height} 0, ${height - 4} 0, 4
      `"
      />
    </svg>

    <svg
      width="150px"
      height="150px"
      :key="item"
      v-for="item in border"
      :class="`${item} dv-border-svg-container`"
    >
      <polygon :fill="mergedColor[1]" points="40, 0 5, 0 0, 5 0, 16 3, 19 3, 7 7, 3 35, 3" />
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
const border = ['left-top', 'right-top', 'left-bottom', 'right-bottom']

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
    zoom: props.scale,
    'box-shadow': `inset 0 0 25px 3px ${mergedColor.value[0]}`
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
.dv-border-box-10 {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 6px;

  .dv-border-svg-container {
    position: absolute;
    display: block;
  }

  .right-top {
    right: 0px;
    transform: rotateY(180deg);
  }

  .left-bottom {
    bottom: 0px;
    transform: rotateX(180deg);
  }

  .right-bottom {
    right: 0px;
    bottom: 0px;
    transform: rotateX(180deg) rotateY(180deg);
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
  }
}
</style>
