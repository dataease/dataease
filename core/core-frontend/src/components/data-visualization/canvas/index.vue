<script setup lang="ts">
import Shape from './Shape.vue'
import {
  getStyle,
  getComponentRotatedStyle,
  getShapeStyle,
  getSVGStyle,
  getCanvasStyle
} from '@/utils/style'
import { $, isPreventDrop } from '@/utils/utils'
import ContextMenu from './ContextMenu.vue'
import MarkLine from './MarkLine.vue'
import Area from './Area.vue'
import eventBus from '@/utils/eventBus'
import Grid from './Grid.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { ref, onMounted, toRef, computed } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { storeToRefs } from 'pinia'
import findComponent from '@/utils/components'

const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()

const { componentData, curComponent, canvasStyleData, canvasViewInfo } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const props = defineProps({
  isEdit: {
    type: Boolean,
    default: true
  }
})
const isEdit = toRef(props, 'isEdit')
const editorX = ref(0)
const editorY = ref(0)
const start = ref({
  // 选中区域的起点
  x: 0,
  y: 0
})
const width = ref(0)
const height = ref(0)
const isShowArea = ref(false)
const svgFilterAttrs = ['width', 'height', 'top', 'left', 'rotate']

const handleMouseDown = e => {
  // 右键返回
  if (e.buttons === 2) {
    return
  }
  // 如果没有选中组件 在画布上点击时需要调用 e.preventDefault() 防止触发 drop 事件
  if (!curComponent.value || isPreventDrop(curComponent.value.component)) {
    e.preventDefault()
  }
  hideArea()
  // 获取编辑器的位移信息，每次点击时都需要获取一次。主要是为了方便开发时调试用。
  const rectInfo = editor.value.getBoundingClientRect()
  editorX.value = rectInfo.x
  editorY.value = rectInfo.y

  const startX = e.clientX
  const startY = e.clientY
  start.value.x = startX - editorX.value
  start.value.y = startY - editorY.value
  // 展示选中区域
  isShowArea.value = true

  const move = moveEvent => {
    width.value = Math.abs(moveEvent.clientX - startX)
    height.value = Math.abs(moveEvent.clientY - startY)
    if (moveEvent.clientX < startX) {
      start.value.x = moveEvent.clientX - editorX.value
    }

    if (moveEvent.clientY < startY) {
      start.value.y = moveEvent.clientY - editorY.value
    }
  }

  const up = e => {
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)

    if (e.clientX == startX && e.clientY == startY) {
      hideArea()
      return
    }

    createGroup()
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

const hideArea = () => {
  isShowArea.value = false
  width.value = 0
  height.value = 0
  composeStore.setAreaData({
    style: {
      left: 0,
      top: 0,
      width: 0,
      height: 0
    },
    components: []
  })
}

const createGroup = () => {
  // 获取选中区域的组件数据
  const areaData = getSelectArea()
  if (areaData.length <= 1) {
    hideArea()
    return
  }

  // 根据选中区域和区域中每个组件的位移信息来创建 Group 组件
  // 要遍历选择区域的每个组件，获取它们的 left top right bottom 信息来进行比较
  let top = Infinity,
    left = Infinity
  let right = -Infinity,
    bottom = -Infinity
  areaData.forEach(component => {
    let style = { left: 0, top: 0, right: 0, bottom: 0 }
    if (component.component == 'Group') {
      component.propValue.forEach(item => {
        const rectInfo = $(`#component${item.id}`).getBoundingClientRect()
        style.left = rectInfo.left - editorX.value
        style.top = rectInfo.top - editorY.value
        style.right = rectInfo.right - editorX.value
        style.bottom = rectInfo.bottom - editorY.value

        if (style.left < left) left = style.left
        if (style.top < top) top = style.top
        if (style.right > right) right = style.right
        if (style.bottom > bottom) bottom = style.bottom
      })
    } else {
      style = getComponentRotatedStyle(component.style)
    }

    if (style.left < left) left = style.left
    if (style.top < top) top = style.top
    if (style.right > right) right = style.right
    if (style.bottom > bottom) bottom = style.bottom
  })

  start.value.x = left
  start.value.y = top
  width.value = right - left
  height.value = bottom - top

  // 设置选中区域位移大小信息和区域内的组件数据
  composeStore.setAreaData({
    style: {
      left,
      top,
      width: width.value,
      height: height.value
    },
    components: areaData
  })
  // 如果有组件被group选中 取消当前画布选中的组件
  if (areaData.length > 0) {
    dvMainStore.setCurComponent({ component: null, index: null })
  }
}

const getSelectArea = () => {
  const result = []
  // 区域起点坐标
  const { x, y } = start.value
  // 计算所有的组件数据，判断是否在选中区域内
  componentData.value.forEach(component => {
    if (component.isLock) return

    const styleInfo = getComponentRotatedStyle(component.style)
    if (
      x <= styleInfo.left &&
      y <= styleInfo.top &&
      styleInfo.left + styleInfo.width <= x + width.value &&
      styleInfo.top + styleInfo.height <= y + height.value
    ) {
      result.push(component)
    }
  })

  // 返回在选中区域内的所有组件
  return result
}

const handleContextMenu = e => {
  e.stopPropagation()
  e.preventDefault()

  // 计算菜单相对于编辑器的位移
  let target = e.target
  let top = e.offsetY
  let left = e.offsetX
  while (target instanceof SVGElement) {
    target = target.parentNode
  }

  while (!target.className.includes('editor')) {
    left += target.offsetLeft
    top += target.offsetTop
    target = target.parentNode
  }

  contextmenuStore.showContextMenu({ top, left })
}

const getComponentStyle = style => {
  return getStyle(style, svgFilterAttrs)
}

const getSVGStyleInner = style => {
  return getSVGStyle(style, svgFilterAttrs)
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

const getTextareaHeight = (element, text) => {
  let { lineHeight, fontSize, height } = element.style
  if (lineHeight === '') {
    lineHeight = 1.5
  }

  const newHeight =
    (text.split('<br>').length - 1) * lineHeight * (fontSize || canvasStyleData.value.fontSize)
  return height > newHeight ? height : newHeight
}

const editStyle = computed(() => {
  return {
    ...getCanvasStyle(canvasStyleData.value),
    width: changeStyleWithScale(canvasStyleData.value['width']) + 'px',
    height: changeStyleWithScale(canvasStyleData.value['height']) + 'px'
  }
})
onMounted(() => {
  // 获取编辑器元素
  composeStore.getEditor()
  eventBus.on('hideArea', () => {
    hideArea()
  })
})
</script>

<template>
  <div
    id="editor"
    class="editor"
    :class="{ edit: isEdit }"
    :style="editStyle"
    @contextmenu="handleContextMenu"
    @mousedown="handleMouseDown"
  >
    <!-- 网格线 -->
    <!--    <Grid />-->

    <!--页面组件列表展示-->
    <Shape
      v-for="(item, index) in componentData.filter(component => component.isShow)"
      :key="item.id"
      :default-style="item.style"
      :style="getShapeStyle(item.style)"
      :active="item.id === (curComponent || {})['id']"
      :element="item"
      :index="index"
      :class="{ lock: item.isLock }"
    >
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
    </Shape>
    <!-- 右击菜单 -->
    <ContextMenu />
    <!-- 标线 -->
    <MarkLine />
    <!-- 选中区域 -->
    <Area v-show="isShowArea" :start="start" :width="width" :height="height" />
  </div>
</template>

<style lang="less" scoped>
.editor {
  position: relative;
  margin: auto;
  background-size: 100% 100% !important;
  .lock {
    opacity: 0.5;
    &:hover {
      cursor: not-allowed;
    }
  }
}

.edit {
  .component {
    outline: none;
    width: 100%;
    height: 100%;
  }
}
</style>
