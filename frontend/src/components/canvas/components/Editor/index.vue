<template>
  <div
    v-if="showDrag"
    id="editor"
    class="editor"
    :class="{ edit: isEdit }"
    :style="customStyle"
    @contextmenu="handleContextMenu"
    @mousedown="handleMouseDown"
  >
    <!-- 网格线 -->
    <Grid v-if="canvasStyleData.auxiliaryMatrix" :matrix-style="matrixStyle" />
    <!--页面组件列表展示-->
    <de-drag
      v-for="(item, index) in componentData"
      :key="item.id"
      :index="index"
      :x="getShapeStyleIntDeDrag(item.style,'left')"
      :y="getShapeStyleIntDeDrag(item.style,'top')"
      :w="getShapeStyleIntDeDrag(item.style,'width')"
      :h="getShapeStyleIntDeDrag(item.style,'height')"
      :r="item.style.rotate"
      :parent="true"
      :rotatable="rotatable"
      :default-style="getShapeStyleInt(item.style)"
      :active="item === curComponent"
      :element="item"
      class-name-active="de-drag-active"
      :class="{'gap_class':canvasStyleData.panel.gap==='yes'}"
      :snap="true"
      :snap-tolerance="5"
      @refLineParams="getRefLineParams"
    >
      <component
        :is="item.component"
        v-if="item.type==='v-text'"
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyleDefault(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :out-style="getShapeStyleInt(item.style)"
        @input="handleInput"
      />
      <component
        :is="item.component"
        v-else-if="item.type==='other'"
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :out-style="getShapeStyleInt(item.style)"
      />
      <component
        :is="item.component"
        v-else
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyleDefault(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :out-style="getShapeStyleInt(item.style)"
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
    </de-drag>
    <!-- 右击菜单 -->
    <ContextMenu />
    <!-- 标线 (临时去掉标线 吸附等功能)-->
    <!--    <MarkLine />-->
    <!-- 选中区域 -->
    <!--    <Area v-show="isShowArea" :start="start" :width="width" :height="height" />-->

    <span
      v-for="(item, index) in vLine"
      v-show="item.display"
      :key="'v_'+index"
      class="ref-line v-line"
      :style="{
        left: item.position,
        top: item.origin,
        height: item.lineLength,
      }"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import Shape from './Shape'
import DeDrag from '@/components/DeDrag'

// eslint-disable-next-line no-unused-vars
import { getStyle, getComponentRotatedStyle } from '@/components/canvas/utils/style'
import { $ } from '@/components/canvas/utils/utils'
import ContextMenu from './ContextMenu'
import MarkLine from './MarkLine'
import Area from './Area'
import eventBus from '@/components/canvas/utils/eventBus'
import Grid from './Grid'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'

export default {
  components: { Shape, ContextMenu, MarkLine, Area, Grid, DeDrag },
  props: {
    isEdit: {
      type: Boolean,
      default: true
    },

    outStyle: {
      type: Object,
      require: false,
      default: null
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
      conditions: [],
      scaleWidth: 100,
      scaleHeight: 100,
      timer: null,
      needToChangeHeight: [
        'top',
        'height',
        'fontSize',
        'borderWidth'
      ],
      needToChangeWidth: [
        'left',
        'width'
      ],
      // private 是否可旋转
      rotatable: false,
      // 矩阵大小
      matrixStyle: {
        width: 80,
        height: 20
      },
      // 矩阵数量 默认 12 * 24
      matrixCount: {
        x: 12,
        y: 24
      },
      customStyleHistory: null,
      showDrag: true,
      vLine: [],
      hLine: []
    }
  },
  computed: {
    customStyle() {
      let style = {
        width: this.format(this.canvasStyleData.width, this.scaleWidth) + 'px',
        height: this.format(this.canvasStyleData.height, this.scaleHeight) + 'px'
      }
      // console.log('customStyle=>' + JSON.stringify(style))

      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${this.canvasStyleData.panel.imageUrl}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      return style
    },
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
  watch: {
    outStyle: {
      handler(newVal, oldVla) {
        this.changeScale()
      },
      deep: true
    },
    canvasStyleData: {
      handler(newVal, oldVla) {
        this.changeScale()
      },
      deep: true
    },
    componentData: {
      handler(newVal, oldVla) {
        console.log('11111')
      },
      deep: true
    }
  },

  mounted() {
    // 获取编辑器元素
    this.$store.commit('getEditor')

    eventBus.$on('hideArea', () => {
      this.hideArea()
    })
    // bus.$on('delete-condition', condition => {
    //   this.deleteCondition(condition)
    // })
  },
  created() {
    // this.$store.dispatch('conditions/clear')
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
      let top = Infinity
      let left = Infinity
      let right = -Infinity
      let bottom = -Infinity
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
      let target = e.target
      while (target instanceof SVGElement) {
        target = target.parentNode
      }
      let top = 0
      let left = 0
      // 如果档期有计划的组件 坐标取当前组件的加上偏移量
      if (this.curComponent && !target.className.includes('editor')) {
        top = this.curComponent.style.top * this.scaleHeight / 100 + e.offsetY
        left = this.curComponent.style.left * this.scaleWidth / 100 + e.offsetX
      } else {
        // 计算菜单相对于编辑器的位移
        top = e.offsetY
        left = e.offsetX

        while (!target.className.includes('editor')) {
          left += target.offsetLeft
          top += target.offsetTop
          target = target.parentNode
        }
      }

      this.$store.commit('showContextMenu', { top, left })
    },

    getShapeStyle(style) {
      const result = {};
      ['width', 'left'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleWidth) + 'px'
      });
      ['height', 'top'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleHeight) + 'px'
      })
      result.transform = 'rotate(' + style['rotate'] + 'deg)'

      return result
    },

    getShapeStyleInt(style) {
      const result = {};
      ['width', 'left'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleWidth)
      });
      ['height', 'top'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleHeight)
      })
      result['rotate'] = style['rotate']
      result['borderWidth'] = style['borderWidth']
      result['opacity'] = style['opacity']

      return result
    },

    getComponentStyleDefault(style) {
      return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
      // return style
    },

    getComponentStyle(style) {
      //   return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
      return style
    },

    handleInput(element, value) {
      // 根据文本组件高度调整 shape 高度
      // remain -自适应画布会导致一些问题 暂时不修改
      // this.$store.commit('setShapeStyle', { height: this.getTextareaHeight(element, value) })
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

    format(value, scale) {
      // 自适应画布区域 返回原值
      if (this.canvasStyleData.selfAdaption) {
        return parseInt(value * parseInt(scale) / 100)
      } else {
        return parseInt(value)
      }
    },
    changeScale() {
      // 获取当前宽高（宽高改变后重新渲染画布）
      const style = {
        width: this.format(this.canvasStyleData.width, this.scaleWidth) + 'px',
        height: this.format(this.canvasStyleData.height, this.scaleHeight) + 'px'
      }
      if (this.customStyleHistory && this.customStyleHistory !== style) {
        this.showDrag = false
        this.$nextTick(() => (this.showDrag = true))
      }
      this.customStyleHistory = style

      if (this.canvasStyleData.matrixCount) {
        this.matrixCount = this.canvasStyleData.matrixCount
      }
      if (this.outStyle.width && this.outStyle.height) {
        // 矩阵计算
        if (!this.canvasStyleData.selfAdaption) {
          this.matrixStyle.width = this.canvasStyleData.width / this.matrixCount.x
          this.matrixStyle.height = this.canvasStyleData.height / this.matrixCount.y
        } else {
          this.matrixStyle.width = this.outStyle.width / this.matrixCount.x
          this.matrixStyle.height = this.outStyle.height / this.matrixCount.y
        }
        this.scaleWidth = parseInt(this.outStyle.width * 100 / this.canvasStyleData.width)
        this.scaleHeight = parseInt(this.outStyle.height * 100 / this.canvasStyleData.height)
        this.$store.commit('setCurCanvasScale',
          {
            scaleWidth: this.scaleWidth,
            scaleHeight: this.scaleHeight,
            matrixStyleWidth: this.matrixStyle.width,
            matrixStyleHeight: this.matrixStyle.height
          })
      }
    },
    getShapeStyleIntDeDrag(style, prop) {
      if (prop === 'rotate') {
        return style['rotate']
      }
      if (prop === 'width') {
        return this.format(style['width'], this.scaleWidth)
      }
      if (prop === 'left') {
        return this.format(style['left'], this.scaleWidth)
      }
      if (prop === 'height') {
        return this.format(style['height'], this.scaleHeight)
      }
      if (prop === 'top') {
        return this.format(style['top'], this.scaleHeight)
      }
    },
    getRefLineParams(params) {
      console.log(params)
      const { vLine, hLine } = params
      this.vLine = vLine
      this.hLine = hLine
    }
  }
}
</script>

<style lang="scss" scoped>
.editor {
    position: relative;
    /*background: #fff;*/
    margin: auto;
    background-size:100% 100% !important;

    .lock {
        opacity: .5;
    }
}
.edit {
    outline: 1px solid gainsboro;
    .component {
        outline: none;
        width: 100%;
        height: 100%;
    }
}

.gap_class{
   padding:3px;
}

// 拖拽组件样式

.de-drag-active{
  outline: 1px solid #70c0ff;
  user-select: none;
}

.ref-line {
  position: absolute;
  background-color: #70c0ff;;
  z-index: 9999;
}
.v-line {
  width: 1px;
}
.h-line {
  height: 1px;
}

</style>
