<template>
  <div
    :style="style"
    :class="[
      {
        [classNameDragging]: dragging,
        [classNameResizing]: resizing,
        [classNameDraggable]: draggable,
        [classNameResizable]: resizable,
        [classNameRotating]: rotating,
        [classNameRotatable]: rotatable,
        [classNameActive]: enabled ,
        ['linkageSetting']:linkageActive,
        ['positionChange']:!(dragging || resizing||rotating)
      },
      className
    ]"
    @mousedown="elementMouseDown"
    @touchstart="elementTouchDown"
    @mouseenter="enter"
    @mouseleave="leave"
  >
    <div
      :class="[
        {
          ['de-drag-active-inner']:enabled,
          [classNameMouseOn]: mouseOn || active
        },
        className
      ]"
      :style="mainSlotStyle"
    >
      <edit-bar v-if="editBarShow" style="transform: translateZ(10px)" :active-model="'edit'" :element="element" @showViewDetails="showViewDetails" @amRemoveItem="amRemoveItem" @amAddItem="amAddItem" @resizeView="resizeView" @linkJumpSet="linkJumpSet" @boardSet="boardSet" />
      <mobile-check-bar v-if="mobileCheckBarShow" :element="element" @amRemoveItem="amRemoveItem" />
      <div v-if="resizing" style="transform: translateZ(11px);position: absolute; z-index: 3" :style="resizeShadowStyle" />
      <div
        v-for="(handlei, indexi) in actualHandles"
        :key="indexi"
        :class="[classNameHandle, classNameHandle + '-' + handlei]"
        :style="handleStyle(handlei, indexi)"
        @mousedown.stop.prevent="handleDown(handlei, $event)"
        @touchstart.stop.prevent="handleTouchDown(handlei, $event)"
      >
        <slot :name="handlei" />
      </div>
      <div :style="mainSlotStyleInner" class="main-background">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
import { matchesSelectorToParentElements, getComputedSize, addEvent, removeEvent } from '../../utils/dom'
import { computeWidth, computeHeight, restrictToBounds, snapToGrid, rotatedPoint, getAngle } from '../../utils/fns'
import { events, userSelectNone, userSelectAuto } from './option.js'
let eventsFor = events.mouse

// private
import eventBus from '@/components/canvas/utils/eventBus'
import { mapState } from 'vuex'
import EditBar from '@/components/canvas/components/Editor/EditBar'
import MobileCheckBar from '@/components/canvas/components/Editor/MobileCheckBar'
import { hexColorToRGBA } from '@/views/chart/chart/util'

export default {
  replace: true,
  name: 'Dedrag',
  components: { EditBar, MobileCheckBar },
  props: {
    className: {
      type: String,
      default: 'vdr'
    },
    classNameDraggable: {
      type: String,
      default: 'draggable'
    },
    classNameResizable: {
      type: String,
      default: 'resizable'
    },
    // 新增开启旋转时的自定义类名
    classNameRotatable: {
      type: String,
      default: 'rotatable'
    },
    classNameDragging: {
      type: String,
      default: 'dragging'
    },
    classNameResizing: {
      type: String,
      default: 'resizing'
    },
    // 新增组件处于旋转时的自定义类名
    classNameRotating: {
      type: String,
      default: 'rotating'
    },
    classNameActive: {
      type: String,
      default: 'active'
    },
    classNameHandle: {
      type: String,
      default: 'handle'
    },
    disableUserSelect: {
      type: Boolean,
      default: true
    },
    enableNativeDrag: {
      type: Boolean,
      default: false
    },
    preventDeactivation: {
      type: Boolean,
      default: false
    },
    active: {
      type: Boolean,
      default: false
    },
    draggable: {
      type: Boolean,
      default: true
    },
    resizable: {
      type: Boolean,
      default: true
    },
    // 新增 旋转 默认为false 不开启
    rotatable: {
      type: Boolean,
      default: false
    },
    // 锁定宽高比
    lockAspectRatio: {
      type: Boolean,
      default: false
    },
    // 新增 外部传入纵横比 w/h
    outsideAspectRatio: {
      type: [Number, String],
      default: 0
    },
    w: {
      type: [Number, String],
      default: 200,
      validator: val => {
        if (typeof val === 'number') {
          return val > 0
        }
        return val === 'auto'
      }
    },
    h: {
      type: [Number, String],
      default: 200,
      validator: val => {
        if (typeof val === 'number') {
          return val > 0
        }
        return val === 'auto'
      }
    },
    minWidth: {
      type: Number,
      default: 0,
      validator: val => val >= 0
    },
    minHeight: {
      type: Number,
      default: 0,
      validator: val => val >= 0
    },
    maxWidth: {
      type: Number,
      default: null,
      validator: val => val >= 0
    },
    maxHeight: {
      type: Number,
      default: null,
      validator: val => val >= 0
    },
    x: {
      type: [String, Number],
      default: 0
    },
    y: {
      type: [String, Number],
      default: 0
    },
    z: {
      type: [String, Number],
      default: 'auto',
      validator: val => (typeof val === 'string' ? val === 'auto' : val >= 0)
    },
    // 新增 初始旋转角度
    r: {
      type: [String, Number],
      default: 0
    },
    // 新增 旋转手柄 rot
    handles: {
      type: Array,
      default: () => ['tl', 'tm', 'tr', 'mr', 'br', 'bm', 'bl', 'ml', 'rot'],
      validator: val => {
        const s = new Set(['tl', 'tm', 'tr', 'mr', 'br', 'bm', 'bl', 'ml', 'rot'])
        return new Set(val.filter(h => s.has(h))).size === val.length
      }
    },
    dragHandle: {
      type: String,
      default: null
    },
    dragCancel: {
      type: String,
      default: null
    },
    axis: {
      type: String,
      default: 'both',
      validator: val => ['x', 'y', 'both'].includes(val)
    },
    grid: {
      type: Array,
      default: () => [1, 1]
    },
    parent: {
      type: [Boolean, String],
      default: false
    },
    onDragStart: {
      type: Function,
      default: () => true
    },
    onDrag: {
      type: Function,
      default: () => true
    },
    onResizeStart: {
      type: Function,
      default: () => true
    },
    onResize: {
      type: Function,
      default: () => true
    },
    // 新增 回调事件
    onRotateStart: {
      type: Function,
      default: () => true
    },
    onRotate: {
      type: Function,
      default: () => true
    },
    // 冲突检测
    isConflictCheck: {
      type: Boolean,
      default: false
    },
    // 元素对齐
    snap: {
      type: Boolean,
      default: false
    },
    // 新增 是否对齐容器边界
    snapBorder: {
      type: Boolean,
      default: false
    },
    // 当调用对齐时，用来设置组件与组件之间的对齐距离，以像素为单位
    snapTolerance: {
      type: Number,
      default: 5,
      validator: function(val) {
        return typeof val === 'number'
      }
    },
    // 缩放比例
    scaleRatio: {
      type: Number,
      default: 1,
      validator: val => typeof val === 'number'
    },
    // handle是否缩放
    handleInfo: {
      type: Object,
      default: () => {
        return {
          size: 8,
          offset: -4,
          switch: true
        }
      }
    },
    // private
    classNameMouseOn: {
      type: String,
      default: 'mouseOn'
    },
    // eslint-disable-next-line vue/require-default-prop
    element: {
      require: true,
      type: Object
    },
    // eslint-disable-next-line vue/require-default-prop
    defaultStyle: {
      require: true,
      type: Object
    },
    // eslint-disable-next-line vue/require-default-prop
    index: {
      require: true,
      type: [Number, String]
    },
    // 水平设计（相对于悬浮设计） 开启水平设计后 会自动冲突检查 吸附 组件矩阵等
    horizontal: {
      type: Boolean,
      default: true
    },
    // eslint-disable-next-line vue/require-default-prop
    changeStyle: {
      require: true,
      type: Object
    },
    // 联动设置
    linkageActive: {
      type: Boolean,
      default: false
    }
  },
  data: function() {
    return {
      left: this.x,
      top: this.y,
      right: null,
      bottom: null,
      // 新增旋转角度
      rotate: this.r,
      width: null,
      height: null,
      widthTouched: false,
      heightTouched: false,
      // 纵横比变量
      aspectFactor: null,
      // 容器的大小
      parentWidth: null,
      parentHeight: null,
      // 设置最小和最大尺寸
      minW: this.minWidth,
      minH: this.minHeight,
      maxW: this.maxWidth,
      maxH: this.maxHeight,
      // 定义控制手柄
      handle: null,
      enabled: this.active,
      resizing: false,
      dragging: false,
      // 新增 表明组件是否正处于旋转状态
      rotating: false,
      zIndex: this.z,
      // 新增 保存中心点位置，用于计算旋转的方向矢量
      lastCenterX: 0,
      lastCenterY: 0,
      parentX: 0,
      parentY: 0,
      // private
      // 鼠标移入事件
      mouseOn: false,
      // 是否移动 （如果没有移动 不需要记录snapshot）
      hasMove: false,
      // 上次的鼠标指针纵向位置，用来判断指针是上移还是下移
      latestMoveY: 0
    }
  },
  computed: {
    // 编辑组件显示
    editBarShow() {
      // 编辑组件显示条件：1.当前组件存在 2.组件是激活状态或者当前在联动设置撞他 3.当前不在移动端画布编辑状态
      return this.curComponent && (this.active || this.linkageSettingStatus) && !this.mobileLayoutStatus
    },
    // 移动端编辑组件选择按钮显示
    mobileCheckBarShow() {
      // 显示条件：1.当前是移动端画布编辑状态 2.当前组件在激活状态或者鼠标移入状态
      return this.mobileLayoutStatus && (this.active || this.mouseOn)
    },
    handleStyle() {
      return (stick, index) => {
        if (!this.handleInfo.switch) return { display: this.enabled ? 'block' : 'none' }
        // 新增 当没有开启旋转的时候，旋转手柄不显示
        if (stick === 'rot' && !this.rotatable) return { display: 'none' }
        const size = (this.handleInfo.size / this.scaleRatio).toFixed(2)
        const offset = (this.handleInfo.offset / this.scaleRatio).toFixed(2)
        const center = (size / 2).toFixed(2)
        const styleMap = {
          tl: {
            top: `${offset}px`,
            left: `${offset}px`
          },
          tm: {
            top: `${offset}px`,
            left: `calc(50% - ${center}px)`
          },
          tr: {
            top: `${offset}px`,
            right: `${offset}px`
          },
          mr: {
            top: `calc(50% - ${center}px)`,
            right: `${offset}px`
          },
          br: {
            bottom: `${offset}px`,
            right: `${offset}px`
          },
          bm: {
            bottom: `${offset}px`,
            right: `calc(50% - ${center}px)`
          },
          bl: {
            bottom: `${offset}px`,
            left: `${offset}px`
          },
          ml: {
            top: `calc(50% - ${center}px)`,
            left: `${offset}px`
          },
          rot: {
            top: `-${size * 3}px`,
            left: `50%`
          }
        }
        const stickStyle = {
          width: styleMap[stick].width || `${size}px`,
          height: styleMap[stick].height || `${size}px`,
          top: styleMap[stick].top,
          left: styleMap[stick].left,
          right: styleMap[stick].right,
          bottom: styleMap[stick].bottom
        }
        // 新增 让控制手柄的鼠标样式跟随旋转角度变化
        if (stick !== 'rot') {
          const cursorStyleArray = ['nw-resize', 'n-resize', 'ne-resize', 'e-resize', 'se-resize', 's-resize', 'sw-resize', 'w-resize']
          const STEP = 45
          const rotate = this.rotate + STEP / 2
          const deltaIndex = Math.floor(rotate / STEP)
          index = (index + deltaIndex) % 8
          stickStyle.cursor = cursorStyleArray[index]
        }
        stickStyle.display = this.enabled ? 'block' : 'none'
        return stickStyle
      }
    },
    style() {
      return {
        padding: this.curGap + 'px!important',
        transform: `translate(${this.left}px, ${this.top}px) rotate(${this.rotate}deg)`,
        width: this.computedWidth,
        height: this.computedHeight,
        zIndex: this.zIndex,
        fontSize: this.handleInfo.size * 2 + 'px',
        ...(this.dragging && this.disableUserSelect ? userSelectNone : userSelectAuto)
      }
    },
    resizeShadowStyle() {
      return {
        width: this.computedWidth,
        height: this.computedHeight,
        opacity: 0.2,
        background: 'gray'
      }
    },
    // 控制柄显示与否
    actualHandles() {
      if (!this.resizable) return []
      return this.handles
    },
    //  根据left right 算出元素的宽度
    computedWidth() {
      if (this.w === 'auto') {
        if (!this.widthTouched) {
          return 'auto'
        }
      }
      return this.width + 'px'
    },
    // 根据top bottom 算出元素的宽度
    computedHeight() {
      if (this.h === 'auto') {
        if (!this.heightTouched) {
          return 'auto'
        }
      }
      return this.height + 'px'
    },

    //  根据left right 算出元素的宽度
    computedMainSlotWidth() {
      if (this.w === 'auto') {
        if (!this.widthTouched) {
          return 'auto'
        }
      }
      if (this.element.auxiliaryMatrix) {
        const width = Math.round(this.width / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
        return (width - this.curGap * 2) + 'px'
      } else {
        return (this.width - this.curGap * 2) + 'px'
      }
    },
    // 根据top bottom 算出元素的宽度
    computedMainSlotHeight() {
      if (this.h === 'auto') {
        if (!this.heightTouched) {
          return 'auto'
        }
      }
      if (this.element.auxiliaryMatrix) {
        const height = Math.round(this.height / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
        return (height - this.curGap * 2) + 'px'
      } else {
        return (this.height - this.curGap * 2) + 'px'
      }
    },

    // private
    mainSlotStyle() {
      const style = {
        width: this.computedMainSlotWidth,
        height: this.computedMainSlotHeight
      }
      return style
    },
    mainSlotStyleInner() {
      const style = {}
      if (this.element.commonBackground) {
        style['padding'] = (this.element.commonBackground.innerPadding || 0) + 'px'
        style['border-radius'] = (this.element.commonBackground.borderRadius || 0) + 'px'
        if (this.element.commonBackground.enable) {
          if (this.element.commonBackground.backgroundType === 'innerImage') {
            style['background'] = `url(${this.element.commonBackground.innerImage}) no-repeat`
          } else if (this.element.commonBackground.backgroundType === 'outerImage') {
            style['background'] = `url(${this.element.commonBackground.outerImage}) no-repeat`
          } else if (this.element.commonBackground.backgroundType === 'color') {
            style['background-color'] = hexColorToRGBA(this.element.commonBackground.color, this.element.commonBackground.alpha)
          }
        }
      }
      return style
    },
    curComponent() {
      return this.$store.state.curComponent
    },
    curGap() {
      return (this.canvasStyleData.panel.gap === 'yes' && this.element.auxiliaryMatrix) ? this.componentGap : 0
    },
    miniWidth() {
      return this.element.auxiliaryMatrix ? this.curCanvasScale.matrixStyleWidth * (this.element.miniSizex || 1) : 0
    },
    miniHeight() {
      if (this.element.auxiliaryMatrix) {
        if (this.element.component === 'de-number-range') {
          return this.element.auxiliaryMatrix ? this.curCanvasScale.matrixStyleHeight * (this.element.miniSizey || 2) : 0
        } else {
          return this.element.auxiliaryMatrix ? this.curCanvasScale.matrixStyleHeight * (this.element.miniSizey || 1) : 0
        }
      } else {
        return 0
      }
    },
    ...mapState([
      'editor',
      'curCanvasScale',
      'canvasStyleData',
      'linkageSettingStatus',
      'mobileLayoutStatus',
      'componentGap',
      'scrollAutoMove'
    ])
  },
  watch: {
    active(val) {
      this.enabled = val
      if (val) {
        this.$emit('activated')
      } else {
        this.$emit('deactivated')
      }
    },
    z(val) {
      if (val >= 0 || val === 'auto') {
        this.zIndex = val
      }
    },
    x(val) {
      if (this.resizing || this.dragging) {
        return
      }
      if (this.parent) {
        this.bounds = this.calcDragLimits()
      }
      this.moveHorizontally(val)
    },
    y(val) {
      if (this.resizing || this.dragging) {
        return
      }
      if (this.parent) {
        this.bounds = this.calcDragLimits()
      }
      this.moveVertically(val)
    },
    // 新增 监听外部传入参数  旋转角度
    r(val) {
      if (val >= 0) {
        this.rotate = val % 360
      }
    },
    lockAspectRatio(val) {
      if (val) {
        if (this.outsideAspectRatio) {
          this.aspectFactor = this.outsideAspectRatio
        } else {
          this.aspectFactor = this.width / this.height
        }
      } else {
        this.aspectFactor = undefined
      }
    },
    outsideAspectRatio(val) {
      if (val) {
        this.aspectFactor = val
      }
    },
    minWidth(val) {
      if (val > 0 && val <= this.width) {
        this.minW = val
      }
    },
    minHeight(val) {
      if (val > 0 && val <= this.height) {
        this.minH = val
      }
    },
    maxWidth(val) {
      this.maxW = val
    },
    maxHeight(val) {
      this.maxH = val
    },
    w(val) {
      // console.log('changeWidthCK：' + this.resizing)

      if (this.resizing || this.dragging) {
        return
      }
      if (this.parent) {
        this.bounds = this.calcResizeLimits()
      }
      // console.log('changeWidth：' + val)
      this.changeWidth(val)
    },
    h(val) {
      if (this.resizing || this.dragging) {
        return
      }
      if (this.parent) {
        this.bounds = this.calcResizeLimits()
      }
      this.changeHeight(val)
    },
    changeStyle(val) {
      this.beforeDestroyFunction()
      this.createdFunction()
      this.mountedFunction()
    },
    // private 监控dragging  resizing
    dragging(val) {
      if (this.enabled) {
        this.curComponent.optStatus.dragging = val
        this.$store.commit('setScrollAutoMove', 0)
      }
    },
    // private 监控dragging  resizing
    resizing(val) {
      if (this.enabled) {
        this.curComponent.optStatus.resizing = val
        this.$store.commit('setScrollAutoMove', 0)
      }
    }
  },
  created: function() {
    this.createdFunction()
  },
  mounted: function() {
    this.mountedFunction()
  },
  beforeDestroy: function() {
    this.beforeDestroyFunction()
  },
  methods: {
    // 重置边界和鼠标状态
    resetBoundsAndMouseState() {
      this.mouseClickPosition = { mouseX: 0, mouseY: 0, x: 0, y: 0, w: 0, h: 0 }
      this.bounds = {
        minLeft: null,
        maxLeft: null,
        minRight: null,
        maxRight: null,
        minTop: null,
        maxTop: null,
        minBottom: null,
        maxBottom: null
      }
    },
    // 检查父元素大小
    checkParentSize() {
      if (this.parent) {
        const [newParentWidth, newParentHeight] = this.getParentSize()
        // 修复父元素改变大小后，组件resizing时活动异常
        this.right = newParentWidth - this.width - this.left
        this.bottom = newParentHeight - this.height - this.top
        this.parentWidth = newParentWidth
        this.parentHeight = newParentHeight
      }
    },
    // 获取父元素大小
    getParentSize() {
      if (this.parent === true) {
        const style = window.getComputedStyle(this.$el.parentNode, null)
        const rect = this.$el.parentNode.getBoundingClientRect()
        this.parentX = rect.x
        this.parentY = rect.y
        return [Math.round(parseFloat(style.getPropertyValue('width'), 10)) + 6, 100000]
      }
      if (typeof this.parent === 'string') {
        const parentNode = document.querySelector(this.parent)
        if (!(parentNode instanceof HTMLElement)) {
          throw new Error(`The selector ${this.parent} does not match any element`)
        }
        return [parentNode.offsetWidth, parentNode.offsetHeight]
      }
      return [null, null]
    },
    // 元素触摸按下
    elementTouchDown(e) {
      eventsFor = events.touch
      this.elementDown(e)
    },
    elementMouseDown(e) {
      // private 设置当前组件数据及状态
      this.$store.commit('setClickComponentStatus', true)
      if (this.element.component !== 'v-text' && this.element.component !== 'rect-shape' && this.element.component !== 'de-input-search' && this.element.component !== 'de-select-grid' && this.element.component !== 'de-number-range' && this.element.component !== 'de-date') {
        e.preventDefault()
      }
      // 阻止冒泡事件
      e.stopPropagation()
      // 此处阻止冒泡 但是外层需要获取pageX pageY
      this.element.auxiliaryMatrix && this.$emit('elementMouseDown', e)
      this.$store.commit('setCurComponent', { component: this.element, index: this.index })
      // 移动端组件点击自动置顶
      this.mobileLayoutStatus && this.$store.commit('topComponent')
      eventsFor = events.mouse
      this.elementDown(e)
    },
    // 元素按下
    elementDown(e) {
      if (e instanceof MouseEvent && e.which !== 1) {
        return
      }
      const target = e.target || e.srcElement
      if (this.$el.contains(target)) {
        // 挤压式画布设计 drag start 通知
        this.element.auxiliaryMatrix && this.$emit('onDragStart', e, this.element, this.index)
        if (this.onDragStart(e) === false) {
          return
        }
        if (
          (this.dragHandle && !matchesSelectorToParentElements(target, this.dragHandle, this.$el)) ||
          (this.dragCancel && matchesSelectorToParentElements(target, this.dragCancel, this.$el))
        ) {
          this.dragging = false
          return
        }
        if (!this.enabled) {
          this.enabled = true
          this.$emit('activated')
          this.$emit('update:active', true)
        }
        if (this.draggable) {
          this.dragging = true
        }
        //  按下鼠标表示保存当前状态
        this.mouseClickPosition.mouseX = e.touches ? e.touches[0].pageX : e.pageX
        this.mouseClickPosition.mouseY = e.touches ? e.touches[0].pageY : e.pageY
        this.mouseClickPosition.left = this.left
        this.mouseClickPosition.right = this.right
        this.mouseClickPosition.top = this.top
        this.mouseClickPosition.bottom = this.bottom
        this.mouseClickPosition.width = this.width
        this.mouseClickPosition.height = this.height
        // 鼠标按下 重置上次鼠标指针位置
        this.latestMoveY = this.mouseClickPosition.mouseY
        if (this.parent) {
          this.bounds = this.calcDragLimits()
        }
        addEvent(document.documentElement, eventsFor.move, this.move)
        addEvent(document.documentElement, eventsFor.stop, this.handleUp)
      }
    },
    // 计算移动范围
    calcDragLimits() {
      // 开启旋转时，不在进行边界限制
      if (this.rotatable) {
        return {
          // minLeft: -9999,
          // maxLeft: 9999,
          // minRight: -9999,
          // maxRight: 9999,
          // minTop: -9999,
          // maxTop: 9999,
          // minBottom: -9999,
          // maxBottom: 9999,
          minLeft: -this.width / 2,
          maxLeft: this.parentWidth - this.width / 2,
          minRight: this.width / 2,
          maxRight: this.parentWidth + this.width / 2,
          minTop: -this.height / 2,
          maxTop: this.parentHeight - this.height / 2,
          minBottom: this.height / 2,
          maxBottom: this.parentHeight + this.height / 2
        }
      } else {
        return {
          minLeft: this.left % this.grid[0],
          maxLeft: Math.floor((this.parentWidth - this.width - this.left) / this.grid[0]) * this.grid[0] + this.left,
          minRight: this.right % this.grid[0],
          maxRight: Math.floor((this.parentWidth - this.width - this.right) / this.grid[0]) * this.grid[0] + this.right,
          minTop: this.top % this.grid[1],
          maxTop: Math.floor((this.parentHeight - this.height - this.top) / this.grid[1]) * this.grid[1] + this.top,
          minBottom: this.bottom % this.grid[1],
          maxBottom: Math.floor((this.parentHeight - this.height - this.bottom) / this.grid[1]) * this.grid[1] + this.bottom
        }
      }
    },
    // 取消
    deselect(e) {
      const target = e.target || e.srcElement
      const regex = new RegExp(this.className + '-([trmbl]{2})', '')
      if (!this.$el.contains(target) && !regex.test(target.className)) {
        if (this.enabled && !this.preventDeactivation) {
          this.enabled = false
          this.$emit('deactivated')
          this.$emit('update:active', false)
        }
        removeEvent(document.documentElement, eventsFor.move, this.move)
      }
      this.resetBoundsAndMouseState()
    },
    // 控制柄触摸按下
    handleTouchDown(handle, e) {
      eventsFor = events.touch
      this.handleDown(handle, e)
    },
    // 控制柄按下
    handleDown(handle, e) {
      if (e instanceof MouseEvent && e.which !== 1) {
        return false
      }
      this.element.auxiliaryMatrix && this.$emit('onResizeStart', e, this.element, this.index)
      if (this.onResizeStart(handle, e) === false) {
        return false
      }
      if (e.stopPropagation) e.stopPropagation()
      // 锁定纵横比时，将顶点转换为中点 - 不在需要因而将其注释
      // if (this.lockAspectRatio && !handle.includes('m')) {
      //   this.handle = 'm' + handle.substring(1)
      // } else {
      // this.handle = handle;
      // }
      this.handle = handle
      // 新增
      if (this.handle === 'rot') {
        this.rotating = true
      } else {
        this.resizing = true
      }
      // 新增保存矩形信息
      // 获取父元素的位置大小信息
      const { top, left, width, height } = this.$el.getBoundingClientRect()
      // 保存旋转中心的绝对坐标
      this.lastCenterX = window.pageXOffset + left + width / 2
      this.lastCenterY = window.pageYOffset + top + height / 2
      // 保存四个顶点的坐标
      const oleft = this.left
      const otop = this.top
      const owidth = this.width
      const oheight = this.height
      const centerX = oleft + owidth / 2
      const centerY = otop + oheight / 2
      const rotate = this.rotate
      this.TL = rotatedPoint(centerX, centerY, oleft, otop, rotate)
      this.TR = rotatedPoint(centerX, centerY, oleft + owidth, otop, rotate)
      this.BL = rotatedPoint(centerX, centerY, oleft, otop + oheight, rotate)
      this.BR = rotatedPoint(centerX, centerY, oleft + owidth, otop + oheight, rotate)
      //  保存鼠标按下时的当前状态
      this.mouseClickPosition.mouseX = e.touches ? e.touches[0].pageX : e.pageX
      this.mouseClickPosition.mouseY = e.touches ? e.touches[0].pageY : e.pageY
      this.mouseClickPosition.left = this.left
      this.mouseClickPosition.right = this.right
      this.mouseClickPosition.top = this.top
      this.mouseClickPosition.bottom = this.bottom
      this.mouseClickPosition.width = this.width
      this.mouseClickPosition.height = this.height
      // 计算边界
      this.bounds = this.calcResizeLimits()
      // 添加事件
      addEvent(document.documentElement, eventsFor.move, this.move)
      addEvent(document.documentElement, eventsFor.stop, this.handleUp)
    },
    // 计算调整大小范围
    calcResizeLimits() {
      const minW = this.minW
      const minH = this.minH
      let maxW = this.maxW
      let maxH = this.maxH
      const [gridX, gridY] = this.grid
      // 获取矩形信息
      const width = this.width
      const height = this.height
      const left = this.left
      const top = this.top
      const right = this.right
      const bottom = this.bottom
      // 对齐网格
      maxW = maxW - (maxW % gridX)
      maxH = maxH - (maxH % gridY)
      const limits = {
        minLeft: null,
        maxLeft: null,
        minTop: null,
        maxTop: null,
        minRight: null,
        maxRight: null,
        minBottom: null,
        maxBottom: null
      }
      // 边界限制
      if (this.parent) {
        limits.minLeft = left
        limits.maxLeft = left + Math.floor((width - minW) / gridX)
        limits.minTop = top
        limits.maxTop = top + Math.floor((height - minH) / gridY)
        limits.minRight = right
        limits.maxRight = right + Math.floor((width - minW) / gridX)
        limits.minBottom = bottom
        limits.maxBottom = bottom + Math.floor((height - minH) / gridY)
        if (maxW) {
          limits.minLeft = Math.max(limits.minLeft, this.parentWidth - right - maxW)
          limits.minRight = Math.max(limits.minRight, this.parentWidth - left - maxW)
        }
        if (maxH) {
          limits.minTop = Math.max(limits.minTop, this.parentHeight - bottom - maxH)
          limits.minBottom = Math.max(limits.minBottom, this.parentHeight - top - maxH)
        }
      } else {
        limits.minLeft = null
        limits.maxLeft = left + Math.floor(width - minW)
        limits.minTop = null
        limits.maxTop = top + Math.floor(height - minH)
        limits.minRight = null
        limits.maxRight = right + Math.floor(width - minW)
        limits.minBottom = null
        limits.maxBottom = bottom + Math.floor(height - minH)
        if (maxW) {
          limits.minLeft = -(right + maxW)
          limits.minRight = -(left + maxW)
        }
        if (maxH) {
          limits.minTop = -(bottom + maxH)
          limits.minBottom = -(top + maxH)
        }
        if (this.lockAspectRatio && (maxW && maxH)) {
          limits.minLeft = Math.min(limits.minLeft, -(right + maxW))
          limits.minTop = Math.min(limits.minTop, -(maxH + bottom))
          limits.minRight = Math.min(limits.minRight, -left - maxW)
          limits.minBottom = Math.min(limits.minBottom, -top - maxH)
        }
      }
      return limits
    },
    // 移动
    move(e) {
      if (this.resizing) {
        this.handleResize(e)
      } else if (this.dragging) {
        this.handleDrag(e)
      } else if (this.rotating) {
        this.handleRotate(e)
      }
    },
    // 获取鼠标或者触摸点的坐标
    getMouseCoordinate(e) {
      if (e.type.indexOf('touch') !== -1) {
        return {
          x: e.changedTouches[0].clientX,
          y: e.changedTouches[0].clientY
        }
      } else {
        return {
          x: e.pageX || e.clientX + document.documentElement.scrollLeft,
          y: e.pageY || e.clientY + document.documentElement.scrollTop
        }
      }
    },
    handleRotate(e) {
      // 获取方向向量，得到旋转角度
      const { x: mouseX, y: mouseY } = this.getMouseCoordinate(e)
      const x = mouseX - this.lastCenterX
      const y = mouseY - this.lastCenterY
      this.rotate = (getAngle(x, y) + 90) % 360
      this.$emit('rotating', this.rotate)
      // 元素移动

      // private 记录当前样式
      this.recordCurStyle()
    },

    // 元素移动
    async handleDrag(e) {
      const axis = this.axis
      const grid = this.grid
      const bounds = this.bounds
      const mouseClickPosition = this.mouseClickPosition
      // 水平移动
      const tmpDeltaX = axis && axis !== 'y' ? mouseClickPosition.mouseX - (e.touches ? e.touches[0].pageX : e.pageX) : 0
      // 垂直移动
      const mY = e.touches ? e.touches[0].pageY : e.pageY
      const tmpDeltaY = axis && axis !== 'x' ? mouseClickPosition.mouseY - mY : 0
      // mY 鼠标指针移动的点 mY - this.latestMoveY 是计算向下移动还是向上移动
      const offsetY = mY - this.latestMoveY
      // console.log('mY:' + mY + ';latestMoveY=' + this.latestMoveY + ';offsetY=' + offsetY)
      this.$emit('canvasDragging', mY, offsetY)
      this.latestMoveY = mY
      const [deltaX, deltaY] = snapToGrid(grid, tmpDeltaX, tmpDeltaY, this.scaleRatio)
      const left = restrictToBounds(mouseClickPosition.left - deltaX, bounds.minLeft, bounds.maxLeft)
      const top = restrictToBounds(mouseClickPosition.top - deltaY, bounds.minTop, bounds.maxTop)
      if (this.onDrag(left, top) === false) {
        return
      }
      const right = restrictToBounds(mouseClickPosition.right + deltaX, bounds.minRight, bounds.maxRight)
      const bottom = restrictToBounds(mouseClickPosition.bottom + deltaY, bounds.minBottom, bounds.maxBottom)
      this.left = left
      this.top = top + this.scrollAutoMove
      this.right = right
      this.bottom = bottom
      await this.snapCheck()
      this.$emit('dragging', this.left, this.top)
      // 如果当前视图遵循矩阵设计则 进行位置挤压检查
      if (this.element.auxiliaryMatrix) {
        this.$emit('onDragging', e, this.element)
      }

      // private 记录当前样式
      this.recordCurStyle()
    },
    // 外部传参改动x
    moveHorizontally(val) {
      // eslint-disable-next-line no-unused-vars
      const [deltaX, _] = snapToGrid(this.grid, val, this.top, this.scale)
      const left = restrictToBounds(deltaX, this.bounds.minLeft, this.bounds.maxLeft)
      this.left = left
      this.right = this.parentWidth - this.width - left
    },
    // 外部传参改动y
    moveVertically(val) {
      // eslint-disable-next-line no-unused-vars
      const [_, deltaY] = snapToGrid(this.grid, this.left, val, this.scale)
      const top = restrictToBounds(deltaY, this.bounds.minTop, this.bounds.maxTop)
      this.top = top
      this.bottom = this.parentHeight - this.height - top
    },
    // 控制柄移动
    handleResize(e) {
      const handle = this.handle
      // eslint-disable-next-line no-unused-vars
      const scaleRatio = this.scaleRatio
      const { TL, TR, BL, BR } = this
      let { x: mouseX, y: mouseY } = this.getMouseCoordinate(e)
      // 在非旋转且有父容器限制的时候，直接限制mouse参与计算的坐标值
      if (!this.rotatable && this.parent) {
        mouseX = restrictToBounds(mouseX, this.parentX, this.parentX + this.parentWidth)
        mouseY = restrictToBounds(mouseY, this.parentY, this.parentY + this.parentHeight)
      }
      // 获取鼠标移动的坐标差
      let deltaX = mouseX - this.mouseClickPosition.mouseX
      let deltaY = mouseY - this.mouseClickPosition.mouseY
      // 考虑放缩
      deltaX = deltaX / this.scaleRatio
      deltaY = deltaY / this.scaleRatio
      let diffX, diffY, scale, scaleB, scaleC, newX, newY, newW, newH
      let Fixed = {} // 固定点
      let BX = {} // 高度边选点
      let CX = {} //  宽度边选点
      let Va = {} // 固定点到鼠标 向量
      let Vb = {} // 固定点到投影边  向量
      let Vc = {} // 另一边投影
      let Vw = {} // 宽度向量
      let Vh = {} // 高度向量
      // 拖动中点
      if (handle.includes('m')) {
        switch (handle) {
          case 'tm':
            diffX = deltaX + (TL.x + TR.x) / 2
            diffY = deltaY + (TL.y + TR.y) / 2
            Fixed = BL
            BX = TL
            CX = BR
            Va = { x: diffX - Fixed.x, y: diffY - Fixed.y }
            Vb = { x: BX.x - Fixed.x, y: BX.y - Fixed.y }
            scale = (Va.x * Vb.x + Va.y * Vb.y) / (Math.pow(Vb.x, 2) + Math.pow(Vb.y, 2))
            Vw = { x: CX.x - Fixed.x, y: CX.y - Fixed.y }
            Vh = { x: Vb.x * scale, y: Vb.y * scale }
            break
          case 'bm':
            diffX = deltaX + (BL.x + BR.x) / 2
            diffY = deltaY + (BL.y + BR.y) / 2
            Fixed = TL
            BX = BL
            CX = TR
            Va = { x: diffX - Fixed.x, y: diffY - Fixed.y }
            Vb = { x: BX.x - Fixed.x, y: BX.y - Fixed.y }
            scale = (Va.x * Vb.x + Va.y * Vb.y) / (Math.pow(Vb.x, 2) + Math.pow(Vb.y, 2))
            Vw = { x: CX.x - Fixed.x, y: CX.y - Fixed.y }
            Vh = { x: Vb.x * scale, y: Vb.y * scale }
            break
          case 'ml':
            diffX = deltaX + (TL.x + BL.x) / 2
            diffY = deltaY + (TL.y + BL.y) / 2
            Fixed = BR
            BX = BL
            CX = TR
            Va = { x: diffX - Fixed.x, y: diffY - Fixed.y }
            Vb = { x: BX.x - Fixed.x, y: BX.y - Fixed.y }
            scale = (Va.x * Vb.x + Va.y * Vb.y) / (Math.pow(Vb.x, 2) + Math.pow(Vb.y, 2))
            Vh = { x: CX.x - Fixed.x, y: CX.y - Fixed.y }
            Vw = { x: Vb.x * scale, y: Vb.y * scale }
            break
          case 'mr':
            diffX = deltaX + (TR.x + TR.x) / 2
            diffY = deltaY + (TR.y + TR.y) / 2
            Fixed = BL
            BX = BR
            CX = TL
            Va = { x: diffX - Fixed.x, y: diffY - Fixed.y }
            Vb = { x: BX.x - Fixed.x, y: BX.y - Fixed.y }
            scale = (Va.x * Vb.x + Va.y * Vb.y) / (Math.pow(Vb.x, 2) + Math.pow(Vb.y, 2))
            Vh = { x: CX.x - Fixed.x, y: CX.y - Fixed.y }
            Vw = { x: Vb.x * scale, y: Vb.y * scale }
            break
          default:
            break
        }
        newX = Fixed.x + (Vw.x + Vh.x) / 2
        newY = Fixed.y + (Vw.y + Vh.y) / 2
        newW = Math.sqrt(Math.pow(Vw.x, 2) + Math.pow(Vw.y, 2))
        newH = Math.sqrt(Math.pow(Vh.x, 2) + Math.pow(Vh.y, 2))
      } else {
        // 拖动顶点
        switch (handle) {
          case 'tl':
            diffX = deltaX + TL.x
            diffY = deltaY + TL.y
            Fixed = BR
            BX = BL // 高度 TL BL
            CX = TR // 宽度 TL TR
            break
          case 'tr':
            diffX = deltaX + TR.x
            diffY = deltaY + TR.y
            Fixed = BL
            BX = BR
            CX = TL
            break
          case 'bl':
            diffX = deltaX + BL.x
            diffY = deltaY + BL.y
            Fixed = TR
            BX = TL
            CX = BR
            break
          case 'br':
            diffX = deltaX + BR.x
            diffY = deltaY + BR.y
            Fixed = TL
            BX = TR
            CX = BL
            break
          default:
            break
        }
        Va = { x: diffX - Fixed.x, y: diffY - Fixed.y }
        Vb = { x: BX.x - Fixed.x, y: BX.y - Fixed.y }
        Vc = { x: CX.x - Fixed.x, y: CX.y - Fixed.y }
        scaleB = (Va.x * Vb.x + Va.y * Vb.y) / (Math.pow(Vb.x, 2) + Math.pow(Vb.y, 2))
        scaleC = (Va.x * Vc.x + Va.y * Vc.y) / (Math.pow(Vc.x, 2) + Math.pow(Vc.y, 2))
        Vw = { x: Vb.x * scaleB, y: Vb.y * scaleB }
        Vh = { x: Vc.x * scaleC, y: Vc.y * scaleC }
        newX = Fixed.x + (Vw.x + Vh.x) / 2
        newY = Fixed.y + (Vw.y + Vh.y) / 2
        newW = Math.sqrt(Math.pow(Vw.x, 2) + Math.pow(Vw.y, 2))
        newH = Math.sqrt(Math.pow(Vh.x, 2) + Math.pow(Vh.y, 2))
      }
      this.left = newX - newW / 2
      this.top = newY - newH / 2
      // 存在父容器，内部元素大小不允许超过父容器
      if (this.parent) {
        newW = restrictToBounds(newW, 0, this.parentWidth)
        newH = restrictToBounds(newH, 0, this.parentHeight)
      }
      // 外部传参限制大小
      // newW = restrictToBounds(newW, this.minW || 0, this.maxW)
      // newH = restrictToBounds(newH, this.minH || 0, this.maxH)
      newW = restrictToBounds(newW, this.miniWidth || 0, this.maxW)
      newH = restrictToBounds(newH, this.miniHeight || 0, this.maxH)
      // 纵横比
      if (this.lockAspectRatio) {
        // console.log(this.lockAspectRatio, this.aspectFactor)
        if (newW / newH > this.aspectFactor) {
          newW = newH * this.aspectFactor
        } else {
          newH = newW / this.aspectFactor
        }
      }
      this.width = newW
      // console.log('width2:' + this.width)
      this.height = newH

      // this.$emit('resizing', this.left, this.top, this.width, this.height)
      this.element.auxiliaryMatrix && this.$emit('onResizing', e, this.element)

      // private 记录当前组件样式
      this.recordCurStyle()
      this.element.propValue && this.element.propValue.viewId && eventBus.$emit('resizing', this.element.propValue.viewId)
    },
    changeWidth(val) {
      // console.log('parentWidth', this.parentWidth)
      // console.log('parentHeight', this.parentHeight)
      // eslint-disable-next-line no-unused-vars
      const [newWidth, _] = snapToGrid(this.grid, val, 0, this.scale)
      // const right = restrictToBounds(this.parentWidth - newWidth - this.left, this.bounds.minRight, this.bounds.maxRight)
      // private 将 this.bounds.minRight 设置为0
      const right = restrictToBounds(this.parentWidth - newWidth - this.left, 0, this.bounds.maxRight)
      let bottom = this.bottom
      if (this.lockAspectRatio) {
        bottom = this.bottom - (this.right - right) / this.aspectFactor
      }
      const width = computeWidth(this.parentWidth, this.left, right)
      const height = computeHeight(this.parentHeight, this.top, bottom)
      this.right = right
      this.bottom = bottom
      this.width = width
      // console.log('width3:' + this.width)
      this.height = height
    },
    changeHeight(val) {
      // eslint-disable-next-line no-unused-vars
      const [_, newHeight] = snapToGrid(this.grid, 0, val, this.scale)
      // const bottom = restrictToBounds(this.parentHeight - newHeight - this.top, this.bounds.minBottom, this.bounds.maxBottom)
      // private 将 this.bounds.minBottom parentHeight理论不设上限 所以这里不再检验bottom底部距离
      // const bottom = restrictToBounds(this.parentHeight - newHeight - this.top, 0, this.bounds.maxBottom)
      const bottom = this.parentHeight - newHeight - this.top
      let right = this.right
      if (this.lockAspectRatio) {
        right = this.right - (this.bottom - bottom) * this.aspectFactor
      }
      const width = computeWidth(this.parentWidth, this.left, right)
      const height = computeHeight(this.parentHeight, this.top, bottom)
      this.right = right
      this.bottom = bottom
      this.width = width
      // console.log('width4:' + this.width)
      this.height = height
    },
    // 从控制柄松开
    handleUp(e) {
      this.handle = null
      // 初始化辅助线数据
      const temArr = new Array(3).fill({ display: false, position: '', origin: '', lineLength: '' })
      const refLine = { vLine: [], hLine: [] }
      for (const i in refLine) {
        refLine[i] = JSON.parse(JSON.stringify(temArr))
      }
      // 保存 鼠标松开的坐标
      const { x: mouseX, y: mouseY } = this.getMouseCoordinate(e)
      this.lastMouseX = mouseX
      this.lastMouseY = mouseY
      if (this.resizing) {
        this.resizing = false
        // console.log('resizing2:' + this.resizing)
        this.conflictCheck()
        this.$emit('refLineParams', refLine)
        // this.$emit('resizestop', this.left, this.top, this.width, this.height)
        // private
        // this.$emit('resizestop')
      }
      if (this.dragging) {
        this.dragging = false
        this.conflictCheck()
        this.$emit('refLineParams', refLine)
        this.$emit('dragstop', this.left, this.top)
      }
      if (this.rotating) {
        this.rotating = false
        this.$emit('rotatestop', this.rotate)
      }
      this.resetBoundsAndMouseState()
      // private 记录snapshot

      // 如果辅助设计 需要最后调整矩阵
      if (this.element.auxiliaryMatrix) {
        // this.recordMatrixCurStyle()
        setTimeout(() => {
          this.recordMatrixCurShadowStyle()
          this.hasMove && this.$store.commit('recordSnapshot', 'handleUp')
          // 记录snapshot后 移动已记录设置为false
          this.hasMove = false
        }, 100)
      } else {
        this.hasMove && this.$store.commit('recordSnapshot', 'handleUp')
        // 记录snapshot后 移动已记录设置为false
        this.hasMove = false
      }

      removeEvent(document.documentElement, eventsFor.move, this.move)

      // private 删除handle Up事件 防止重复recordSnapshot
      removeEvent(document.documentElement, eventsFor.stop, this.handleUp)

      // 挤占式画布设计 handleUp
      this.element.auxiliaryMatrix && this.$emit('onHandleUp', e)
    },
    // 新增方法 ↓↓↓
    // 设置属性
    settingAttribute() {
      // 设置冲突检测
      this.$el.setAttribute('data-is-check', `${this.isConflictCheck}`)
      // 设置对齐元素
      this.$el.setAttribute('data-is-snap', `${this.snap}`)
    },
    // 冲突检测
    conflictCheck() {
      const top = this.top
      const left = this.left
      const width = this.width
      const height = this.height
      if (this.isConflictCheck) {
        const nodes = this.$el.parentNode.childNodes // 获取当前父节点下所有子节点
        for (const item of nodes) {
          if (
            item.className !== undefined &&
            !item.className.split(' ').includes(this.classNameActive) &&
            item.getAttribute('data-is-check') !== null &&
            item.getAttribute('data-is-check') !== 'false'
          ) {
            const tw = item.offsetWidth
            const th = item.offsetHeight
            // 正则获取left与right
            const [tl, tt] = this.formatTransformVal(item.style.transform)
            // 左上角与右下角重叠
            const tfAndBr = (top >= tt && left >= tl && tt + th > top && tl + tw > left) || (top <= tt && left < tl && top + height > tt && left + width > tl)
            // 右上角与左下角重叠
            const brAndTf = (left <= tl && top >= tt && left + width > tl && top < tt + th) || (top < tt && left > tl && top + height > tt && left < tl + tw)
            // 下边与上边重叠
            const bAndT = (top <= tt && left >= tl && top + height > tt && left < tl + tw) || (top >= tt && left <= tl && top < tt + th && left > tl + tw)
            // 上边与下边重叠（宽度不一样）
            const tAndB = (top <= tt && left >= tl && top + height > tt && left < tl + tw) || (top >= tt && left <= tl && top < tt + th && left > tl + tw)
            // 左边与右边重叠
            const lAndR = (left >= tl && top >= tt && left < tl + tw && top < tt + th) || (top > tt && left <= tl && left + width > tl && top < tt + th)
            // 左边与右边重叠（高度不一样）
            const rAndL = (top <= tt && left >= tl && top + height > tt && left < tl + tw) || (top >= tt && left <= tl && top < tt + th && left + width > tl)
            // 如果冲突，就将回退到移动前的位置
            if (tfAndBr || brAndTf || bAndT || tAndB || lAndR || rAndL) {
              this.top = this.mouseClickPosition.top
              this.left = this.mouseClickPosition.left
              this.width = this.mouseClickPosition.width
              // console.log('width5:' + this.width)
              this.height = this.mouseClickPosition.height
            }
          }
        }
      }
    },
    // 检测对齐元素
    async snapCheck() {
      if (this.snap) {
        // 保存当前元素的四个属性
        let width = this.width
        let height = this.height
        let activeLeft = this.left
        let activeRight = this.left + width
        let activeTop = this.top
        let activeBottom = this.top + height
        // 初始化辅助线数据
        const temArr = new Array(3).fill({ display: false, position: '', origin: '', lineLength: '' })
        const refLine = { vLine: [], hLine: [] }
        for (const i in refLine) {
          refLine[i] = JSON.parse(JSON.stringify(temArr))
        }
        const tem = {
          value: { x: [[], [], []], y: [[], [], []] },
          display: [],
          position: []
        }
        // 获取当前父节点下所有子节点
        const nodes = this.$el.parentNode.childNodes
        //  当允许多个同时激活时，获取总体的属性
        const { groupWidth, groupHeight, groupLeft, groupTop, bln } = await this.getActiveAll(nodes)
        if (!bln) {
          width = groupWidth
          height = groupHeight
          activeLeft = groupLeft
          activeRight = groupLeft + groupWidth
          activeTop = groupTop
          activeBottom = groupTop + groupHeight
        }
        // 遍历获取其他元素的属性
        for (const item of nodes) {
          if (
            // private
            item.tagName !== 'svg' &&
            item.className !== undefined &&
            !item.className.split(' ').includes(this.classNameActive) &&
            item.getAttribute('data-is-snap') !== null &&
            item.getAttribute('data-is-snap') !== 'false'
          ) {
            // 获取位置，角度
            const [l, t, rotate] = this.formatTransformVal(item.style.transform)
            if ((rotate - this.rotate) % 90 === 0) {
              // 获取宽高
              const w = item.offsetWidth
              const h = item.offsetHeight
              // 计算得到right和bottom
              const r = l + w // 对齐目标right
              const b = t + h // 对齐目标的bottom
              const hc = Math.abs(activeTop + height / 2 - (t + h / 2)) <= this.snapTolerance // 水平中线
              const vc = Math.abs(activeLeft + width / 2 - (l + w / 2)) <= this.snapTolerance // 垂直中线
              const ts = Math.abs(t - activeBottom) <= this.snapTolerance // 从上到下
              const TS = Math.abs(b - activeBottom) <= this.snapTolerance // 从上到下
              const bs = Math.abs(t - activeTop) <= this.snapTolerance // 从下到上 上边共线
              const BS = Math.abs(b - activeTop) <= this.snapTolerance // 从下到上
              const ls = Math.abs(l - activeRight) <= this.snapTolerance // 外左
              const LS = Math.abs(r - activeRight) <= this.snapTolerance // 外左
              const rs = Math.abs(l - activeLeft) <= this.snapTolerance // 外右
              const RS = Math.abs(r - activeLeft) <= this.snapTolerance // 外右
              tem.display = [ts, TS, bs, BS, hc, hc, ls, LS, rs, RS, vc, vc]
              tem.position = [t, b, t, b, t + h / 2, t + h / 2, l, r, l, r, l + w / 2, l + w / 2]
              // 单个可激活元素与其他元素对齐
              if (bln) {
                if (ts) {
                  this.top = t - height
                  this.bottom = this.parentHeight - this.top - height
                  tem.value.y[0].push(l, r, activeLeft, activeRight)
                }
                if (bs) {
                  this.top = t
                  this.bottom = this.parentHeight - this.top - height
                  tem.value.y[0].push(l, r, activeLeft, activeRight)
                }
                if (TS) {
                  this.top = b - height
                  this.bottom = this.parentHeight - this.top - height
                  tem.value.y[1].push(l, r, activeLeft, activeRight)
                }
                if (BS) {
                  this.top = b
                  this.bottom = this.parentHeight - this.top - height
                  tem.value.y[1].push(l, r, activeLeft, activeRight)
                }
                if (ls) {
                  this.left = l - width
                  this.right = this.parentWidth - this.left - width
                  tem.value.x[0].push(t, b, activeTop, activeBottom)
                }
                if (rs) {
                  this.left = l
                  this.right = this.parentWidth - this.left - width
                  tem.value.x[0].push(t, b, activeTop, activeBottom)
                }
                if (LS) {
                  this.left = r - width
                  this.right = this.parentWidth - this.left - width
                  tem.value.x[1].push(t, b, activeTop, activeBottom)
                }
                if (RS) {
                  this.left = r
                  this.right = this.parentWidth - this.left - width
                  tem.value.x[1].push(t, b, activeTop, activeBottom)
                }
                if (hc) {
                  this.top = t + h / 2 - height / 2
                  this.bottom = this.parentHeight - this.top - height
                  tem.value.y[2].push(l, r, activeLeft, activeRight)
                }
                if (vc) {
                  this.left = l + w / 2 - width / 2
                  this.right = this.parentWidth - this.left - width
                  tem.value.x[2].push(t, b, activeTop, activeBottom)
                }
                // 和容器贴边
                if (this.snapBorder) {
                  if (Math.abs(this.left - 0) <= this.snapTolerance) {
                    this.left = 0
                    this.right = this.parentWidth - this.left - width
                  }
                  if (Math.abs(this.right - 0) <= this.snapTolerance) {
                    this.right = 0
                    this.left = this.parentWidth - this.width - this.right
                  }
                  if (Math.abs(this.top - 0) <= this.snapTolerance) {
                    this.top = 0
                    this.bottom = this.parentHeight - this.top - height
                  }
                  if (Math.abs(this.bottom - 0) <= this.snapTolerance) {
                    this.bottom = 0
                    this.top = this.parentHeight - this.bottom - height
                  }
                }
              }
              // 再次进行边界处理
              const bounds = this.bounds
              this.left = restrictToBounds(this.left, bounds.minLeft, bounds.maxLeft)
              this.top = restrictToBounds(this.top, bounds.minTop, bounds.maxTop)
              this.right = restrictToBounds(this.right, bounds.minRight, bounds.maxRight)
              this.bottom = restrictToBounds(this.bottom, bounds.minBottom, bounds.maxBottom)
              // 辅助线坐标与是否显示(display)对应的数组,易于循环遍历
              const arrTem = [0, 1, 0, 1, 2, 2, 0, 1, 0, 1, 2, 2]
              for (let i = 0; i <= arrTem.length; i++) {
                // 前6为Y辅助线,后6为X辅助线
                const xory = i < 6 ? 'y' : 'x'
                const horv = i < 6 ? 'hLine' : 'vLine'
                if (tem.display[i]) {
                  const { origin, length } = this.calcLineValues(tem.value[xory][arrTem[i]])
                  refLine[horv][arrTem[i]].display = tem.display[i]
                  refLine[horv][arrTem[i]].position = tem.position[i] + 'px'
                  refLine[horv][arrTem[i]].origin = origin
                  refLine[horv][arrTem[i]].lineLength = length
                }
              }
            }
          }
        }
        this.$emit('refLineParams', refLine)
      }
    },
    // 计算参考线
    calcLineValues(arr) {
      const length = Math.max(...arr) - Math.min(...arr) + 'px'
      const origin = Math.min(...arr) + 'px'
      return { length, origin }
    },
    async getActiveAll(nodes) {
      const activeAll = []
      const XArray = []
      const YArray = []
      let groupWidth = 0
      let groupHeight = 0
      let groupLeft = 0
      let groupTop = 0
      for (const item of nodes) {
        // console.log('===' + typeof item.tagName)
        // 修复判断条件
        // if (item.className !== undefined && item.className.split(' ').includes(this.classNameActive)) {
        if (item.tagName !== 'svg' && item.className !== undefined && item.className.split(' ').includes(this.classNameActive)) {
          activeAll.push(item)
        }
      }
      const AllLength = activeAll.length
      if (AllLength > 1) {
        for (const i of activeAll) {
          const l = i.offsetLeft
          const r = l + i.offsetWidth
          const t = i.offsetTop
          const b = t + i.offsetHeight
          XArray.push(l, r)
          YArray.push(t, b)
        }
        groupWidth = Math.max(...XArray) - Math.min(...XArray)
        groupHeight = Math.max(...YArray) - Math.min(...YArray)
        groupLeft = Math.min(...XArray)
        groupTop = Math.min(...YArray)
      }
      const bln = AllLength === 1
      return { groupWidth, groupHeight, groupLeft, groupTop, bln }
    },
    // 修复 正则获取left与top
    formatTransformVal(string) {
      // eslint-disable-next-line prefer-const
      let [left, top, rotate = 0] = string.match(/[\d|\.]+/g)
      if (top === undefined) top = 0
      return [Number(left), Number(top), rotate]
    },
    // private
    // 鼠标移入事件
    enter() {
      this.mouseOn = true
    },
    // 鼠标移出事件
    leave() {
      this.mouseOn = false
    },
    // 记录当前样式
    recordCurStyle() {
      const style = {
        ...this.defaultStyle
      }
      style.left = this.left
      style.top = this.top
      style.width = this.width
      style.height = this.height
      style.rotate = this.rotate
      this.hasMove = true
      this.$store.commit('setShapeStyle', style)
    },

    // 记录当前样式 矩阵处理
    recordMatrixCurStyle() {
      const left = Math.round(this.left / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
      const top = Math.round(this.top / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
      const width = Math.round(this.width / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
      const height = Math.round(this.height / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
      const style = {
        ...this.defaultStyle
      }
      style.left = left
      style.top = top
      style.width = width
      style.height = height
      style.rotate = this.rotate
      // this.hasMove = true
      this.$store.commit('setShapeStyle', style)

      // resize
      self.$emit('resizeView')
      // const self = this
      // setTimeout(function() {
      //   self.$emit('resizeView')
      // }, 200)
    },
    // 记录当前样式 跟随阴影位置 矩阵处理
    recordMatrixCurShadowStyle() {
      const left = (this.element.x - 1) * this.curCanvasScale.matrixStyleWidth
      const top = (this.element.y - 1) * this.curCanvasScale.matrixStyleHeight
      const width = this.element.sizex * this.curCanvasScale.matrixStyleWidth
      const height = this.element.sizey * this.curCanvasScale.matrixStyleHeight
      // const t1 = Math.round(this.width / this.curCanvasScale.matrixStyleWidth)
      // const left = Math.round(this.left / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
      // const top = Math.round(this.top / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
      // const width = t1 * this.curCanvasScale.matrixStyleWidth
      // const height = Math.round(this.height / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight

      const style = {
        ...this.defaultStyle
      }
      style.left = left
      style.top = top
      style.width = width
      style.height = height
      style.rotate = this.rotate
      // this.hasMove = true
      // console.log('recordMatrixCurShadowStyle:t1:' + JSON.stringify(style))

      this.$store.commit('setShapeStyle', style)

      // resize
      const self = this
      self.$emit('resizeView')
      // setTimeout(function() {
      //   self.$emit('resizeView')
      // }, 200)
    },
    mountedFunction() {
      // private 冲突检测 和水平设计值保持一致
      // this.isConflictCheck = this.horizontal
      // this.snap = this.horizontal
      // this.snapTolerance = 5
      // this.grid = [10, 10]

      if (!this.enableNativeDrag) {
        this.$el.ondragstart = () => false
      }
      const [parentWidth, parentHeight] = this.getParentSize()
      this.parentWidth = parentWidth
      this.parentHeight = parentHeight
      const [width, height] = getComputedSize(this.$el)
      this.aspectFactor = (this.w !== 'auto' ? this.w : width) / (this.h !== 'auto' ? this.h : height)
      if (this.outsideAspectRatio) {
        this.aspectFactor = this.outsideAspectRatio
      }
      this.width = this.w !== 'auto' ? this.w : width
      // console.log('width1:' + this.width)
      this.height = this.h !== 'auto' ? this.h : height
      this.right = this.parentWidth - this.width - this.left
      this.bottom = this.parentHeight - this.height - this.top

      // 绑定data-*属性
      this.settingAttribute()
      // 监听取消操作
      addEvent(document.documentElement, 'mousedown', this.deselect)
      addEvent(document.documentElement, 'touchend touchcancel', this.deselect)
      //  窗口变化时，检查容器大小
      addEvent(window, 'resize', this.checkParentSize)
    },
    createdFunction() {
      // minWidth不能大于maxWidth
      if (this.maxWidth && this.minWidth > this.maxWidth) console.warn('[Vdr warn]: Invalid prop: minWidth cannot be greater than maxWidth')
      // minHeight不能大于maxHeight
      if (this.maxWidth && this.minHeight > this.maxHeight) console.warn('[Vdr warn]: Invalid prop: minHeight cannot be greater than maxHeight')
      this.elmX = 0
      this.elmY = 0
      this.elmW = 0
      this.elmH = 0
      this.lastCenterX = 0
      this.lastCenterY = 0
      this.fixedXName = ''
      this.fixedYName = ''
      this.fixedX = 0
      this.fixedY = 0
      this.TL = {}
      this.TR = {}
      this.BL = {}
      this.BR = {}
      this.resetBoundsAndMouseState()
    },
    beforeDestroyFunction() {
      removeEvent(document.documentElement, 'mousedown', this.deselect)
      removeEvent(document.documentElement, 'touchstart', this.handleUp)
      removeEvent(document.documentElement, 'mousemove', this.move)
      removeEvent(document.documentElement, 'touchmove', this.move)
      removeEvent(document.documentElement, 'mouseup', this.handleUp)
      removeEvent(document.documentElement, 'touchend touchcancel', this.deselect)
      removeEvent(window, 'resize', this.checkParentSize)
    },
    showViewDetails() {
      this.$emit('showViewDetails')
    },
    amAddItem() {
      this.$emit('amAddItem')
    },
    amRemoveItem() {
      this.$emit('amRemoveItem')
    },
    resizeView() {
      this.$emit('resizeView')
    },
    // 跳转设置
    linkJumpSet() {
      this.$emit('linkJumpSet')
    },
    // 跳转设置
    boardSet() {
      this.$emit('boardSet')
    }
  }

}
</script>
<style scoped>
.vdr {
  touch-action: none;
  position: absolute;
  transform-style:preserve-3d;
  border: 1px
}

.handle {
  box-sizing: border-box;
  position: absolute;
  background: #ffffff;
  border: 1px solid #70c0ff;
  border-radius: 50%;
  z-index: 2;
}
.handle-tl {
  cursor: nw-resize;
}
.handle-tm {
  cursor: n-resize;
}
.handle-tr {
  cursor: ne-resize;
}
.handle-ml {
  cursor: w-resize;
}
.handle-mr {
  cursor: e-resize;
}
.handle-bl {
  cursor: sw-resize;
}
.handle-bm {
  cursor: s-resize;
}
.handle-br {
  cursor: se-resize;
}
/* 新增 旋转控制柄 */

.handle-rot {
  position: relative;
  transform: translateX(-50%);
  cursor: grab;
  display: inline-block;
  box-sizing: border-box;
  border: none;
  text-indent: -9999px;
  vertical-align: middle;
}
.handle-rot:before,
.handle-rot:after {
  content: "";
  box-sizing: inherit;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
.handle-rot:before {
  /* display: block; */
  width: 1em;
  height: 1em;
  border: 2px solid #333;
  border-right-color: transparent;
  border-radius: 50%;
}
.handle-rot:after {
  width: 0px;
  height: 0px;
  border: 0.25em solid #333;
  border-left-color: transparent;
  border-top-color: transparent;
  left: 100%;
  top: 10%;
}

.mouseOn {
  outline: 1px dashed #70c0ff;
  user-select: none;
}

.linkageSetting{
  opacity: 0.5;
}

.positionChange{
  transition: 0.2s
}

.de-drag-active{
  user-select: none;
}

.de-drag-active-inner{
  outline: 1px solid #70c0ff;
}
  .main-background{
    overflow: hidden;
    width: 100%;
    height: 100%;
    background-size: 100% 100% !important;
  }
</style>
