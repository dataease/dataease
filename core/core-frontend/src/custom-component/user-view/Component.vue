<script setup lang="ts">
import { computed, toRefs, PropType, CSSProperties } from 'vue'
import Chart from '@/views/chart/components/views/index.vue'
import { isISOMobile } from '@/utils/utils'

const props = defineProps({
  // 公共参数集
  commonParams: {
    type: Object,
    required: false
  },
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
  },
  suffixId: {
    type: String,
    required: false,
    default: 'common'
  },
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  },
  optType: {
    type: String,
    required: false
  }
})

const { element, view, active, searchCount, scale } = toRefs(props)
const autoStyle = computed(() => {
  if (element.value.innerType === 'rich-text') {
    if (isISOMobile()) {
      return {
        position: 'absolute',
        height: 100 / scale.value + '%!important',
        width: 100 / scale.value + '%!important',
        left: 50 * (1 - 1 / scale.value) + '%', // 放大余量 除以 2
        top: 50 * (1 - 1 / scale.value) + '%', // 放大余量 除以 2
        transform: 'scale(' + scale.value + ') translateZ(0)'
      } as CSSProperties
    } else {
      return { zoom: scale.value }
    }
  } else {
    return {}
  }
})
const emits = defineEmits(['onPointClick', 'onComponentEvent'])

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
      :suffixId="suffixId"
      :font-family="fontFamily"
      :common-params="commonParams"
      @onPointClick="onPointClick"
      @onComponentEvent="() => emits('onComponentEvent')"
      :opt-type="optType"
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
