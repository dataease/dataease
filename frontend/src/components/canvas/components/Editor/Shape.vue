<template>
  <div class="shape" :class="classInfo" @mouseenter="enter" @mouseleave="leave" @click="selectCurComponent" @mousedown="handleMouseDownOnShape">
    <span v-show="isActive()" class="iconfont icon-xiangyouxuanzhuan" @mousedown="handleRotate" />
    <span v-show="element.isLock" class="iconfont icon-suo" />

    <!--    <span v-show="isActive()" class="iconfont icon-more">-->
    <!--      <el-button-->
    <!--        icon="el-icon-more"-->
    <!--        type="text"-->
    <!--        size="small"-->
    <!--      />-->
    <!--    </span>-->
    <div
      v-for="item in (isActive()? pointList : [])"
      :key="item"
      class="shape-point"
      :style="getPointStyle(item)"
      @mousedown="handleMouseDownOnPoint(item, $event)"
    />
    <slot />
  </div>
</template>

<script>
import eventBus from '@/components/canvas/utils/eventBus'
import runAnimation from '@/components/canvas/utils/runAnimation'
import { mapState } from 'vuex'
import calculateComponentPositonAndSize from '@/components/canvas/utils/calculateComponentPositonAndSize'
import { mod360 } from '@/components/canvas/utils/translate'

export default {
  props: {
    active: {
      type: Boolean,
      default: false
    },
    element: {
      require: true,
      type: Object,
      default: null
    },
    defaultStyle: {
      require: true,
      type: Object,
      default: null
    },
    index: {
      require: true,
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      mouseOn: false,
      pointList: ['lt', 't', 'rt', 'r', 'rb', 'b', 'lb', 'l'], // 八个方向
      initialAngle: { // 每个点对应的初始角度
        lt: 0,
        t: 45,
        rt: 90,
        r: 135,
        rb: 180,
        b: 225,
        lb: 270,
        l: 315
      },
      angleToCursor: [ // 每个范围的角度对应的光标
        { start: 338, end: 23, cursor: 'nw' },
        { start: 23, end: 68, cursor: 'n' },
        { start: 68, end: 113, cursor: 'ne' },
        { start: 113, end: 158, cursor: 'e' },
        { start: 158, end: 203, cursor: 'se' },
        { start: 203, end: 248, cursor: 's' },
        { start: 248, end: 293, cursor: 'sw' },
        { start: 293, end: 338, cursor: 'w' }
      ],
      cursors: {}
    }
  },

  computed: {
    classInfo() {
      return this.active ? 'active' : (this.mouseOn ? 'mouseOn' : '')
    },
    ...mapState([
      'curComponent',
      'editor'
    ])
  },
  mounted() {
    // 用于 Group 组件
    if (this.curComponent) {
      this.cursors = this.getCursor() // 根据旋转角度获取光标位置
    }
    this.element.type === 'custom' && (this.pointList = ['l', 'r'])

    eventBus.$on('runAnimation', () => {
      if (this.element === this.curComponent) {
        runAnimation(this.$el, this.curComponent.animations)
      }
    })
  },
  methods: {
    // 鼠标移入事件
    enter() {
      this.mouseOn = true
    },
    // 鼠标移出事件
    leave() {
      this.mouseOn = false
    },
    isActive() {
      return this.active && !this.element.isLock
    },
    // 处理旋转
    handleRotate(e) {
      this.$store.commit('setClickComponentStatus', true)
      e.preventDefault()
      e.stopPropagation()
      // 初始坐标和初始角度
      const pos = { ...this.defaultStyle }
      const startY = e.clientY
      const startX = e.clientX
      const startRotate = pos.rotate

      // 获取元素中心点位置
      const rect = this.$el.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2

      // 旋转前的角度
      const rotateDegreeBefore = Math.atan2(startY - centerY, startX - centerX) / (Math.PI / 180)

      // 如果元素没有移动，则不保存快照
      let hasMove = false
      const move = (moveEvent) => {
        hasMove = true
        const curX = moveEvent.clientX
        const curY = moveEvent.clientY
        // 旋转后的角度
        const rotateDegreeAfter = Math.atan2(curY - centerY, curX - centerX) / (Math.PI / 180)
        // 获取旋转的角度值
        pos.rotate = startRotate + rotateDegreeAfter - rotateDegreeBefore
        // 修改当前组件样式
        this.$store.commit('setShapeStyle', pos)
      }

      const up = () => {
        hasMove && this.$store.commit('recordSnapshot')
        document.removeEventListener('mousemove', move)
        document.removeEventListener('mouseup', up)
        this.cursors = this.getCursor() // 根据旋转角度获取光标位置
      }

      document.addEventListener('mousemove', move)
      document.addEventListener('mouseup', up)
    },

    getPointStyle(point) {
      const { width, height } = this.defaultStyle
      const hasT = /t/.test(point)
      const hasB = /b/.test(point)
      const hasL = /l/.test(point)
      const hasR = /r/.test(point)
      let newLeft = 0
      let newTop = 0

      // 四个角的点
      if (point.length === 2) {
        newLeft = hasL ? 0 : width
        newTop = hasT ? 0 : height
      } else {
        // 上下两点的点，宽度居中
        if (hasT || hasB) {
          newLeft = width / 2
          newTop = hasT ? 0 : height
        }

        // 左右两边的点，高度居中
        if (hasL || hasR) {
          newLeft = hasL ? 0 : width
          newTop = Math.floor(height / 2)
        }
      }

      const style = {
        marginLeft: hasR ? '-4px' : '-4px',
        marginTop: '-4px',
        left: `${newLeft}px`,
        top: `${newTop}px`,
        cursor: this.cursors[point]
      }

      return style
    },

    getCursor() {
      const { angleToCursor, initialAngle, pointList, curComponent } = this
      const rotate = mod360(curComponent.style.rotate) // 取余 360
      const result = {}
      let lastMatchIndex = -1 // 从上一个命中的角度的索引开始匹配下一个，降低时间复杂度

      pointList.forEach(point => {
        const angle = mod360(initialAngle[point] + rotate)
        const len = angleToCursor.length
        // eslint-disable-next-line no-constant-condition
        while (true) {
          lastMatchIndex = (lastMatchIndex + 1) % len
          const angleLimit = angleToCursor[lastMatchIndex]
          if (angle < 23 || angle >= 338) {
            result[point] = 'nw-resize'

            return
          }

          if (angleLimit.start <= angle && angle < angleLimit.end) {
            result[point] = angleLimit.cursor + '-resize'

            return
          }
        }
      })

      return result
    },

    handleMouseDownOnShape(e) {
      this.$store.commit('setClickComponentStatus', true)
      if (this.element.component !== 'v-text' && this.element.component !== 'rect-shape' && this.element.component !== 'de-input-search' && this.element.component !== 'de-number-range') {
        e.preventDefault()
      }

      e.stopPropagation()
      this.$store.commit('setCurComponent', { component: this.element, index: this.index })
      if (this.element.isLock) return

      this.cursors = this.getCursor() // 根据旋转角度获取光标位置

      const pos = { ...this.defaultStyle }
      const startY = e.clientY
      const startX = e.clientX
      // 如果直接修改属性，值的类型会变为字符串，所以要转为数值型
      const startTop = Number(pos.top)
      const startLeft = Number(pos.left)

      // 如果元素没有移动，则不保存快照
      let hasMove = false
      const move = (moveEvent) => {
        hasMove = true
        const curX = moveEvent.clientX
        const curY = moveEvent.clientY
        pos.top = curY - startY + startTop
        pos.left = curX - startX + startLeft

        // 修改当前组件样式
        this.$store.commit('setShapeStyle', pos)
        // 等更新完当前组件的样式并绘制到屏幕后再判断是否需要吸附
        // 如果不使用 $nextTick，吸附后将无法移动
        this.$nextTick(() => {
          // 触发元素移动事件，用于显示标线、吸附功能
          // 后面两个参数代表鼠标移动方向
          // curY - startY > 0 true 表示向下移动 false 表示向上移动
          // curX - startX > 0 true 表示向右移动 false 表示向左移动
          eventBus.$emit('move', curY - startY > 0, curX - startX > 0)
        })
      }

      const up = () => {
        hasMove && this.$store.commit('recordSnapshot')
        // 触发元素停止移动事件，用于隐藏标线
        eventBus.$emit('unmove')
        document.removeEventListener('mousemove', move)
        document.removeEventListener('mouseup', up)
      }

      document.addEventListener('mousemove', move)
      document.addEventListener('mouseup', up)
    },

    selectCurComponent(e) {
      // 阻止向父组件冒泡
      e.stopPropagation()
      e.preventDefault()
      this.$store.commit('hideContextMenu')
    },

    handleMouseDownOnPoint(point, e) {
      this.$store.commit('setClickComponentStatus', true)
      e.stopPropagation()
      e.preventDefault()

      const style = { ...this.defaultStyle }

      // 组件宽高比
      const proportion = style.width / style.height

      // 组件中心点
      const center = {
        x: style.left + style.width / 2,
        y: style.top + style.height / 2
      }

      // 获取画布位移信息
      const editorRectInfo = this.editor.getBoundingClientRect()

      // 当前点击坐标
      const curPoint = {
        x: e.clientX - editorRectInfo.left,
        y: e.clientY - editorRectInfo.top
      }

      // 获取对称点的坐标
      const symmetricPoint = {
        x: center.x - (curPoint.x - center.x),
        y: center.y - (curPoint.y - center.y)
      }

      // 是否需要保存快照
      let needSave = false
      let isFirst = true

      const needLockProportion = this.isNeedLockProportion()
      const move = (moveEvent) => {
        // 第一次点击时也会触发 move，所以会有“刚点击组件但未移动，组件的大小却改变了”的情况发生
        // 因此第一次点击时不触发 move 事件
        if (isFirst) {
          isFirst = false
          return
        }

        needSave = true
        const curPositon = {
          x: moveEvent.clientX - editorRectInfo.left,
          y: moveEvent.clientY - editorRectInfo.top
        }

        calculateComponentPositonAndSize(point, style, curPositon, proportion, needLockProportion, {
          center,
          curPoint,
          symmetricPoint
        })
        // console.log('this is test:' + JSON.stringify(this.element.propValue.viewId))
        this.$store.commit('setShapeStyle', style)
        this.element.propValue && this.element.propValue.viewId && eventBus.$emit('resizing', this.element.propValue.viewId)
      }

      const up = () => {
        document.removeEventListener('mousemove', move)
        document.removeEventListener('mouseup', up)
        needSave && this.$store.commit('recordSnapshot')
      }

      document.addEventListener('mousemove', move)
      document.addEventListener('mouseup', up)
    },

    isNeedLockProportion() {
      if (this.element.component !== 'Group') return false
      const ratates = [0, 90, 180, 360]
      for (const component of this.element.propValue) {
        if (!ratates.includes(mod360(parseInt(component.style.rotate)))) {
          return true
        }
      }

      return false
    }
  }
}
</script>

<style lang="scss" scoped>
.shape {
    position: absolute;

    &:hover {
        cursor: move;
    }
}
.active {
    outline: 1px solid #70c0ff;
    user-select: none;
}

.mouseOn {
  outline: 1px dashed #70c0ff;
  user-select: none;
}
.shape-point {
    position: absolute;
    background: #fff;
    border: 1px solid #59c7f9;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    z-index: 1;
}
.icon-xiangyouxuanzhuan {
    position: absolute;
    top: -34px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 16px;
    font-weight: 600;
    cursor: grab;
    color: #59c7f9;
    font-size: 20px;
    font-weight: 600;

    &:active {
        cursor: grabbing;
    }
}
.icon-suo {
    position: absolute;
    top: 0;
    right: 0;
}

.edit-side {
  overflow: hidden auto;
  min-height: 24px;
  padding-top: 0px;
  padding-bottom: 0px;
}

.icon-more {
  position: absolute;
  top: 0;
  right: -10px;
}
</style>
