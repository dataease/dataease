<script setup lang="ts">
import { toRefs, computed, watch, nextTick, onMounted } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import CanvasGroup from '@/custom-component/common/CanvasGroup.vue'
import { deepCopy } from '@/utils/utils'
import { DEFAULT_CANVAS_STYLE_DATA_DARK } from '@/views/chart/components/editor/util/dataVisualization'
import { groupSizeStyleAdaptor } from '@/utils/style'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, curComponent } = storeToRefs(dvMainStore)
const sourceCanvasStyle = deepCopy(DEFAULT_CANVAS_STYLE_DATA_DARK)

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  element: {
    type: Object,
    required: true,
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
  dvInfo: {
    type: Object,
    required: true
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  isEdit: {
    type: Boolean,
    required: false,
    default: false
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  active: {
    type: Boolean,
    default: false
  },
  canvasViewInfo: {
    type: Object,
    required: true
  }
})

const { propValue, dvInfo, element, scale, canvasViewInfo } = toRefs(props)
const customCanvasStyle = computed(() => {
  const result = sourceCanvasStyle
  result.scale = canvasStyleData.value.scale
  result.width = (element.value.style.width * 100) / result.scale
  result.height = (element.value.style.height * 100) / result.scale
  return result
})

const setCanvasActive = () => {
  element.value['canvasActive'] = true
}

watch(
  () => props.active,
  () => {
    // canvasActive失活 满足的条件 1.当前组件未激活 2.当前没有激活组件或者有激活组件时，该组件的canvasId不属于当前分组
    nextTick(() => {
      if (
        !props.active &&
        (!curComponent.value ||
          (curComponent.value && !curComponent.value.canvasId.includes(element.value.id)))
      ) {
        element.value['canvasActive'] = false
      }
    })
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => {
    groupSizeStyleAdaptor(element.value)
  })
})
</script>

<template>
  <div
    class="group"
    :class="{ 'canvas-active-custom': element['canvasActive'] }"
    @dblclick="setCanvasActive"
  >
    <canvas-group
      :component-data="propValue"
      :dv-info="dvInfo"
      :show-position="showPosition"
      :canvas-id="'Group-' + element.id"
      :canvas-style-data="customCanvasStyle"
      :canvas-view-info="canvasViewInfo"
      :is-edit="isEdit"
      :element="element"
      :scale="scale"
    >
    </canvas-group>
  </div>
</template>

<style lang="less" scoped>
.group {
  & > div {
    position: relative;
    width: 100%;
    height: 100%;

    .component {
      position: absolute;
    }
  }
}

.canvas-active-custom {
  outline: 2px solid var(--ed-color-primary) !important;
}
</style>
