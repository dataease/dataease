<template>
  <div class="dv-border-box-1" :style="border_style" :ref="refName">
    <svg class="border" :width="width" :height="height">
      <polygon
        :fill="backgroundColor"
        :points="`10, 27 10, ${height - 27} 13, ${height - 24} 13, ${height - 21} 24, ${height - 11}
      38, ${height - 11} 41, ${height - 8} 73, ${height - 8} 75, ${height - 10} 81, ${height - 10}
      85, ${height - 6} ${width - 85}, ${height - 6} ${width - 81}, ${height - 10} ${width - 75}, ${
          height - 10
        }
      ${width - 73}, ${height - 8} ${width - 41}, ${height - 8} ${width - 38}, ${height - 11}
      ${width - 24}, ${height - 11} ${width - 13}, ${height - 21} ${width - 13}, ${height - 24}
      ${width - 10}, ${height - 27} ${width - 10}, 27 ${width - 13}, 25 ${width - 13}, 21
      ${width - 24}, 11 ${width - 38}, 11 ${width - 41}, 8 ${width - 73}, 8 ${width - 75}, 10
      ${width - 81}, 10 ${
          width - 85
        }, 6 85, 6 81, 10 75, 10 73, 8 41, 8 38, 11 24, 11 13, 21 13, 24`"
      />
    </svg>

    <svg width="150px" height="150px" :key="item" v-for="item in border" :class="`${item} border`">
      <polygon
        :fill="mergedColor[0]"
        points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63"
      >
        <animate
          attributeName="fill"
          :values="`${mergedColor[0]};${mergedColor[1]};${mergedColor[0]}`"
          dur="0.5s"
          begin="0s"
          repeatCount="indefinite"
        />
      </polygon>
      <polygon
        :fill="mergedColor[1]"
        points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"
      >
        <animate
          attributeName="fill"
          :values="`${mergedColor[1]};${mergedColor[0]};${mergedColor[1]}`"
          dur="0.5s"
          begin="0s"
          repeatCount="indefinite"
        />
      </polygon>
      <polygon
        :fill="mergedColor[0]"
        points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54"
      >
        <animate
          attributeName="fill"
          :values="`${mergedColor[0]};${mergedColor[1]};transparent`"
          dur="1s"
          begin="0s"
          repeatCount="indefinite"
        />
      </polygon>
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
      width: 320,
      height: 240
    }
  }
})

const width = computed(() => props.curStyle.width)
const height = computed(() => props.curStyle.height)

const refName = ref('border-box-1')
const border = ref(['left-top', 'right-top', 'left-bottom', 'right-bottom'])
const defaultColor = ref(['#4fd2dd', '#235fa7'])
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
    transform: `scale(${props.scale})`,
    'transform-origin': '0 0'
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
.dv-border-box-1 {
  position: relative;
  /* 启用硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  will-change: transform; // 提示浏览器准备变换

  .border {
    position: absolute;
    display: block;
    /* 优化SVG渲染 */
    shape-rendering: optimizeSpeed;
    /* 禁用鼠标事件提高性能 */
    pointer-events: none;
  }

  /* 保持原有角标定位不变 */
  .right-top {
    right: 0px;
    transform: rotateY(180deg) translateZ(0); // 添加硬件加速
  }

  .left-bottom {
    bottom: 0px;
    transform: rotateX(180deg) translateZ(0); // 添加硬件加速
  }

  .right-bottom {
    right: 0px;
    bottom: 0px;
    transform: rotateX(180deg) rotateY(180deg) translateZ(0); // 添加硬件加速
  }

  .border-box-content {
    position: relative;
    width: 100%;
    height: 100%;
    /* 创建新的层叠上下文 */
    isolation: isolate;
  }
}
</style>
