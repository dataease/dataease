<script setup lang="ts">
import { CSSProperties, computed, toRefs, PropType } from 'vue'
import Chart from '@/views/chart/components/views/index.vue'

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  propValue: {
    type: String,
    required: true,
    default: ''
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  view: {
    type: Object as PropType<ChartObj>,
    default() {
      return {
        propValue: null
      }
    }
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  dvType: {
    type: String,
    required: false,
    default: 'dashboard'
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false
  }
})

const { element, view, active, searchCount, scale } = toRefs(props)
const autoStyle = computed(() => {
  if (element.value.innerType === 'rich-text') {
    return {
      position: 'absolute',
      height: 100 / scale.value + '%!important',
      width: 100 / scale.value + '%!important',
      left: 50 * (1 - 1 / scale.value) + '%', // 放大余量 除以 2
      top: 50 * (1 - 1 / scale.value) + '%', // 放大余量 除以 2
      transform: 'scale(' + scale.value + ')'
    } as CSSProperties
  } else {
    return {}
  }
})
const emits = defineEmits(['onPointClick'])

const onPointClick = param => {
  emits('onPointClick', param)
}
</script>

<template>
  <div class="bash-shape" :style="autoStyle">
    <chart
      :scale="scale"
      :active="active"
      :view="view"
      :element="element"
      :show-position="showPosition"
      :search-count="searchCount"
      :disabled="disabled"
      @onPointClick="onPointClick"
    ></chart>
  </div>
</template>

<style lang="less" scoped>
.bash-shape {
  width: 100%;
  height: 100%;
  transform: translate(0);
}
</style>
