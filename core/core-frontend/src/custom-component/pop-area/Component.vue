<template>
  <!--此区域暂时放到类似视图和分组平行的组件作为后续扩展使用，但当前版本只作为类似MarkLine 和canvasCore平行组件 仅做区域显示使用-->
  <div
    class="pop-area"
    :style="popCanvasStyle"
    :class="{ 'preview-pop': showPosition === 'preview' }"
    @mousedown.stop
    @mousedup.stop
  >
    <div style="width: 100%; height: 100%">
      <div v-if="popComponentData && popComponentData.length > 0" class="pop-content">
        <!--使用ComponentWrapper 保留扩展能力-->
        <ComponentWrapper
          v-for="(item, index) in popComponentData"
          :id="'component-pop-' + item.id"
          :view-info="canvasViewInfo[item.id]"
          :key="index"
          :config="item"
          :index="index"
          :dv-info="dvInfo"
          :pop-active="curActive(item)"
          :show-position="showPosition"
          :style="customPopStyle"
          :scale="innerScale"
        />
      </div>
      <div
        v-else
        class="pop-area-main"
        :class="{ 'pop-area-active': areaActive }"
        :style="baseStyle"
        @drop="handleDrop"
        @dragover="handleDragOver"
        @dragleave="handleDragLeave"
      >
        <span>可点击或拖拽查询组件到此位置，点击预览可查看弹窗区</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, toRefs } from 'vue'
import { findDragComponent } from '@/utils/canvasUtils'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { changeComponentSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { ElMessage } from 'element-plus-secondary'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const areaActive = ref(false)
const props = defineProps({
  dvInfo: {
    type: Object,
    required: true
  },
  canvasStyleData: {
    type: Object,
    required: true
  },
  popComponentData: {
    type: Array,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  showPosition: {
    type: String,
    required: false,
    default: 'preview'
  },
  canvasState: {
    type: Object,
    required: true
  }
})

const { canvasStyleData, popComponentData, canvasViewInfo, scale, canvasState } = toRefs(props)
const { curComponent } = storeToRefs(dvMainStore)
const baseStyle = computed(() => {
  return {
    fontSize: 30 * props.scale + 'px',
    height: canvasStyleData.value.height * props.scale * 0.15 + 'px'
  }
})

const innerScale = computed(() =>
  props.showPosition === 'preview' ? props.scale : props.scale * 100
)

const curActive = item => {
  return curComponent.value?.id === item.id && props.showPosition === 'popEdit'
}

const handleDragOver = e => {
  areaActive.value = true
  e.preventDefault()
  e.dataTransfer.dropEffect = 'copy'
}

const handleDragLeave = e => {
  areaActive.value = false
}

const handleDrop = e => {
  areaActive.value = false
  // 判断当前区域师傅已经有隐藏组件
  if (!popComponentData.value || popComponentData.value.length === 0) {
    e.preventDefault()
    e.stopPropagation()
    const componentInfo = e.dataTransfer.getData('id')
    if (componentInfo) {
      const component = findDragComponent(componentInfo)
      if (component.component === 'VQuery') {
        component.style.top = 0
        component.style.left = 0
        component.id = guid()
        component.category = 'hidden'
        component.commonBackground.backgroundColor = 'rgba(41, 41, 41, 1)'
        changeComponentSizeWithScale(component)
        dvMainStore.addComponent({ component: component, index: undefined })
        adaptCurThemeCommonStyle(component)
        snapshotStore.recordSnapshotCache('renderChart', component.id)
      } else {
        ElMessage.error('及支持添加查询组件')
      }
    }
  }
}

const handleDragEnd = () => {
  areaActive.value = false
}

const customPopStyle = computed(() => {
  return {
    width: '100%',
    height: '100%'
  }
})

const popCanvasStyle = computed(() => {
  if (canvasState.value.curPointArea === 'hidden') {
    let queryCount = 0
    popComponentData.value.forEach(popItem => {
      queryCount = 0 + popItem.propValue.length
    })
    return {
      height: queryCount < 8 ? '15%' : (queryCount * 45 * scale.value) / 4 + 'px'
    }
  } else {
    return { height: '0px!important', overflow: 'hidden', border: '0!important' }
  }
})

onMounted(() => {
  eventBus.on('handleDragEnd-canvas-main', handleDragEnd)
})

onBeforeUnmount(() => {
  eventBus.off('handleDragEnd-canvas-main', handleDragEnd)
})
</script>

<style lang="less" scoped>
.pop-area {
  position: absolute;
  width: 100%;
  max-height: 50%;
  top: 0;
  left: 0;
  border: 1px dashed rgba(67, 67, 67, 1);
  background: rgba(26, 26, 26, 1);
  transition: height 0.2s ease;
  z-index: 1;
}

.preview-pop {
  border: 1px rgba(67, 67, 67, 1) !important;
}
.pop-area-main {
  display: flex;
  width: 100%;
  justify-content: center;
  align-items: center;
  color: rgba(166, 166, 166, 1);
  top: 0;
  left: 0;
}
.pop-area-active {
  border: 1px dashed rgba(51, 112, 255, 1) !important;
  background: #1d2331 !important;
}
.pop-content {
  position: static !important;
  width: 100%;
  height: 100%;
  :deep(.no-list-label .container .ed-button) {
    font-size: 32px;
  }
  :deep(.no-list-label .container) {
    font-size: 32px;
  }
}
</style>
