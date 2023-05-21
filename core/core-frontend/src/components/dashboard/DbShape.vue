<template>
  <div class="shape" :class="{ active }">
    <component
      :is="findComponent(item.component)"
      v-if="item.component != 'VText'"
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

<script setup lang="ts">
import { toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import findComponent from '@/utils/components'
import { getStyle } from '@/utils/style'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const composeStore = composeStoreWithOut()
const svgFilterAttrs = ['width', 'height', 'top', 'left', 'rotate']

const { curComponent } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)

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

const props = defineProps({
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

const { active, item, index } = toRefs(props)
</script>

<style lang="less" scoped>
.shape {
  width: 100%;
  height: 100%;
  position: relative;
  &:hover {
    outline: 1px dashed #70c0ff;
  }
}

.active {
  outline: 1px solid #70c0ff;
  user-select: none;
}
</style>
