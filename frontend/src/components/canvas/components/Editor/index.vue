<template>
  <div
    id="editor"
    class="editor"
    :class="{ edit: isEdit }"
    :style="{
      width: changeStyleWithScale(canvasStyleData.width) + 'px',
      height: changeStyleWithScale(canvasStyleData.height) + 'px',
    }"
    @contextmenu="handleContextMenu"
    @mousedown="handleMouseDown"
  >
    <!-- 网格线 -->
    <Grid />

    <!--页面组件列表展示-->
    <Shape
      v-for="(item, index) in componentData"
      :key="item.id"
      :default-style="item.style"
      :style="getShapeStyle(item.style)"
      :active="item === curComponent"
      :element="item"
      :index="index"
      :class="{ lock: item.isLock }"
    >

      <component
        :is="item.component"
        v-if="item.type==='custom'"
        :id="'component' + item.id"
        class="component"
        :style="item.style"
        :element="item"
        @set-condition-value="setConditionValue"
      />

      <component
        :is="item.component"
        v-else
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
      />
      <!-- <component
        :is="item.component"
        v-else
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
        @input="handleInput"
      /> -->
    </Shape>
    <!-- 右击菜单 -->
    <ContextMenu />
    <!-- 标线 -->
    <MarkLine />
    <!-- 选中区域 -->
    <Area v-show="isShowArea" :start="start" :width="width" :height="height" />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import Shape from './Shape'
// eslint-disable-next-line no-unused-vars
import { getStyle, getComponentRotatedStyle } from '@/components/canvas/utils/style'
import { $ } from '@/components/canvas/utils/utils'
import ContextMenu from './ContextMenu'
import MarkLine from './MarkLine'
import Area from './Area'
import eventBus from '@/components/canvas/utils/eventBus'
import Grid from './Grid'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'
import { Condition } from '@/components/widget/bean/Condition'
import bus from '@/utils/bus'
export default {
  components: { Shape, ContextMenu, MarkLine, Area, Grid },
  props: {
    isEdit: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      editorX: 0,
      editorY: 0,
      start: { // 选中区域的起点
        x: 0,
        y: 0
      },
      width: 0,
      height: 0,
      isShowArea: false,
      conditions: []
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'componentData',
      'curComponent',
      'canvasStyleData',
      'editor'
    ])
  },

  mounted() {
    // 获取编辑器元素
    this.$store.commit('getEditor')

    eventBus.$on('hideArea', () => {
      this.hideArea()
    })
    bus.$on('delete-condition', condition => {
      this.deleteCondition(condition)
    })
  },
  methods: {
    changeStyleWithScale,

    handleMouseDown(e) {
      // 如果没有选中组件 在画布上点击时需要调用 e.preventDefault() 防止触发 drop 事件
      if (!this.curComponent || (this.curComponent.component !== 'v-text' && this.curComponent.component !== 'rect-shape')) {
        e.preventDefault()
      }

      this.hideArea()

      // 获取编辑器的位移信息，每次点击时都需要获取一次。主要是为了方便开发时调试用。
      const rectInfo = this.editor.getBoundingClientRect()
      this.editorX = rectInfo.x
      this.editorY = rectInfo.y

      const startX = e.clientX
      const startY = e.clientY
      this.start.x = startX - this.editorX
      this.start.y = startY - this.editorY
      // 展示选中区域
      this.isShowArea = true

      const move = (moveEvent) => {
        this.width = Math.abs(moveEvent.clientX - startX)
        this.height = Math.abs(moveEvent.clientY - startY)
        if (moveEvent.clientX < startX) {
          this.start.x = moveEvent.clientX - this.editorX
        }

        if (moveEvent.clientY < startY) {
          this.start.y = moveEvent.clientY - this.editorY
        }
      }

      const up = (e) => {
        document.removeEventListener('mousemove', move)
        document.removeEventListener('mouseup', up)

        if (e.clientX === startX && e.clientY === startY) {
          this.hideArea()
          return
        }

        this.createGroup()
      }

      document.addEventListener('mousemove', move)
      document.addEventListener('mouseup', up)
    },

    hideArea() {
      this.isShowArea = 0
      this.width = 0
      this.height = 0
    },

    createGroup() {
      // 获取选中区域的组件数据
      const areaData = this.getSelectArea()
      if (areaData.length <= 1) {
        this.hideArea()
        return
      }

      // 根据选中区域和区域中每个组件的位移信息来创建 Group 组件
      // 要遍历选择区域的每个组件，获取它们的 left top right bottom 信息来进行比较
      let top = Infinity; let left = Infinity
      let right = -Infinity; let bottom = -Infinity
      areaData.forEach(component => {
        let style = {}
        if (component.component === 'Group') {
          component.propValue.forEach(item => {
            const rectInfo = $(`#component${item.id}`).getBoundingClientRect()
            style.left = rectInfo.left - this.editorX
            style.top = rectInfo.top - this.editorY
            style.right = rectInfo.right - this.editorX
            style.bottom = rectInfo.bottom - this.editorY

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

      this.start.x = left
      this.start.y = top
      this.width = right - left
      this.height = bottom - top

      // 设置选中区域位移大小信息和区域内的组件数据
      this.$store.commit('setAreaData', {
        style: {
          left,
          top,
          width: this.width,
          height: this.height
        },
        components: areaData
      })
    },

    getSelectArea() {
      const result = []
      // 区域起点坐标
      const { x, y } = this.start
      // 计算所有的组件数据，判断是否在选中区域内
      this.componentData.forEach(component => {
        if (component.isLock) return

        const { left, top, width, height } = component.style
        if (x <= left && y <= top && (left + width <= x + this.width) && (top + height <= y + this.height)) {
          result.push(component)
        }
      })

      // 返回在选中区域内的所有组件
      return result
    },

    handleContextMenu(e) {
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

      this.$store.commit('showContextMenu', { top, left })
    },

    getShapeStyle(style) {
      const result = {};
      ['width', 'height', 'top', 'left', 'rotate'].forEach(attr => {
        if (attr !== 'rotate') {
          result[attr] = style[attr] + 'px'
        } else {
          result.transform = 'rotate(' + style[attr] + 'deg)'
        }
      })

      return result
    },

    getComponentStyle(style) {
    //   return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
      return style
    },

    handleInput(element, value) {
      // 根据文本组件高度调整 shape 高度
      this.$store.commit('setShapeStyle', { height: this.getTextareaHeight(element, value) })
    },

    getTextareaHeight(element, text) {
      // eslint-disable-next-line prefer-const
      let { lineHeight, fontSize, height } = element.style
      if (lineHeight === '') {
        lineHeight = 1.5
      }

      const newHeight = (text.split('<br>').length - 1) * lineHeight * fontSize
      return height > newHeight ? height : newHeight
    },

    filterValueChange(value) {
      console.log('emit:' + value)
    },

    setConditionValue(obj) {
      const { component, value, operator } = obj
      const fieldId = component.options.attrs.fieldId
      const viewIds = component.options.attrs.viewIds
      const condition = new Condition(component.id, fieldId, operator, value, viewIds)
      this.addCondition(condition)
    },
    addCondition(condition) {
      let conditionExist = false
      for (let index = 0; index < this.conditions.length; index++) {
        const element = this.conditions[index]
        if (condition.componentId === element.componentId) {
          this.conditions[index] = condition
          conditionExist = true
        }
      }
      !conditionExist && this.conditions.push(condition)
      this.executeSearch()
    },
    deleteCondition(condition) {
      this.conditions = this.conditions.filter(item => {
        const componentIdSuitable = !condition.componentId || (item.componentId === condition.componentId)
        const fieldIdSuitable = !condition.fieldId || (item.fieldId === condition.fieldId)
        return !(componentIdSuitable && fieldIdSuitable)
      })
      this.executeSearch()
    },
    executeSearch() {
      console.log('当前查询条件是: ' + JSON.stringify(this.conditions))
    }
  }
}
</script>

<style lang="scss" scoped>
.editor {
    position: relative;
    background: #fff;
    margin: auto;

    .lock {
        opacity: .5;
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
