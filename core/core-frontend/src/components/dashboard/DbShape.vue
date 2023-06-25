<script setup lang="ts">
import { computed, nextTick, reactive, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import findComponent from '@/utils/components'
import { getStyle } from '@/utils/style'
import eventBus from '@/utils/eventBus'
import { isPreventDrop } from '@/utils/utils'
import ComponentBar from '@/components/visualization/ComponentBar.vue'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const composeStore = composeStoreWithOut()
const svgFilterAttrs = ['width', 'height', 'top', 'left', 'rotate']

const { curComponent } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)

const props = defineProps({
  canvasViewInfo: Object,
  active: {
    type: Boolean,
    default: false
  },
  item: {
    required: true,
    type: Object,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null
      }
    }
  },
  index: {
    required: true,
    type: [Number, String],
    default: 0
  }
})

const { active, item, index, canvasViewInfo } = toRefs(props)

const state = reactive({
  seriesIdMap: {
    id: ''
  }
})

const componentActiveFlag = computed(() => {
  return active.value
})

const getTextareaHeight = (element, text) => {
  let { lineHeight, fontSize, height } = element.style
  if (lineHeight === '') {
    lineHeight = 1.5
  }

  const newHeight = (text.split('<br>').length - 1) * lineHeight * fontSize
  return height > newHeight ? height : newHeight
}

const handleInput = (element, value) => {
  // 根据文本组件高度调整 shape 高度
  dvMainStore.setShapeStyle({
    top: null,
    left: null,
    width: null,
    height: getTextareaHeight(element, value),
    rotate: null
  })
}
const getComponentStyle = style => {
  return getStyle(style, svgFilterAttrs)
}

const selectCurComponent = e => {
  // 阻止向父组件冒泡
  e.stopPropagation()
  e.preventDefault()
}

const handleMouseDownOnShape = e => {
  // 将当前点击组件的事件传播出去
  nextTick(() => eventBus.emit('componentClick'))
  dvMainStore.setInEditorStatus(true)
  dvMainStore.setClickComponentStatus(true)
  if (isPreventDrop(item.value.component)) {
    e.preventDefault()
  }
  e.stopPropagation()
  dvMainStore.setCurComponent({ component: item.value, index: index.value })
}
const showViewDetails = params => {
  // to open details
}
</script>

<template>
  <div
    class="shape"
    :class="{ active }"
    @click="selectCurComponent"
    @mousedown="handleMouseDownOnShape"
  >
    <component-bar
      v-if="componentActiveFlag"
      :source-element="item"
      :terminal="'pc'"
      :element="item"
      :canvas-id="'canvas-main'"
      :show-position="'edit'"
      :series-id-map="state.seriesIdMap"
      @showViewDetails="showViewDetails"
    />
    <!--如果是视图 则动态获取预存的chart-view数据-->
    <component
      :is="findComponent(item.component)"
      v-if="item.component === 'UserView'"
      :id="'component' + item.id"
      class="component"
      :style="getComponentStyle(item.style)"
      :prop-value="item.propValue"
      :view="canvasViewInfo[item.id]"
      :element="item"
      :request="item.request"
      @input="handleInput"
    />

    <component
      :is="findComponent(item.component)"
      v-else-if="item.component != 'VText'"
      :id="'component' + item.id"
      class="component"
      :style="getComponentStyle(item.style)"
      :prop-value="item.propValue"
      :element="item"
      :request="item.request"
    />

    <component
      :is="findComponent(item.component)"
      v-else
      :id="'component' + item.id"
      class="component"
      :style="getComponentStyle(item.style)"
      :prop-value="item.propValue"
      :element="item"
      :request="item.request"
      @input="handleInput"
    />
  </div>
</template>

<style lang="less" scoped>
.active {
  outline: 1px solid #70c0ff;
  user-select: none;
}

.shape {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
  &:hover {
    outline: 1px dashed #70c0ff;
  }
}
</style>
