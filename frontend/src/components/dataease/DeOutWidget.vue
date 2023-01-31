<template>
  <div
    ref="myContainer"
    class="my-container"
    :style="inScreen?autoStyle:''"
  >
    <div
      ref="conditionMain"
      :style="outsideStyle"
      class="condition-main"
    >
      <div
        v-if="element.options.attrs.showTitle && element.options.attrs.title"
        ref="deTitleContainer"
        :style="titleStyle"
        class="condition-title"
      >
        <div class="condition-title-absolute">
          <div class="first-title">
            <div class="span-container">
              <span ref="deTitle">{{ element.options.attrs.title }}</span>
            </div>
          </div>
        </div>
      </div>
      <div
        ref="deContentContainer"
        class="condition-content"
        :class="(element.options.attrs.showTitle && element.options.attrs.title) ? '' : 'condition-content-default'"
      >
        <div class="condition-content-container">
          <div class="first-element">
            <div
              :class="element.component === 'de-select-grid' ? 'first-element-grid-container': ''"
              :style="deSelectGridBg"
              class="first-element-container"
            >

              <component
                :is="element.component"
                v-if="element.type==='custom'"
                :id="'component' + element.id"
                ref="deOutWidget"
                :canvas-id="canvasId"
                class="component-custom"
                :out-style="element.style"
                :is-relation="isRelation"
                :element="element"
                :in-draw="inDraw"
                :in-screen="inScreen"
              />
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script>
import inputStyleMixin from '@/components/widget/deWidget/inputStyleMixin'
import { mapState } from 'vuex'

export default {
  name: 'DeOutWidget',
  mixins: [inputStyleMixin],
  props: {
    canvasId: {
      type: String,
      required: true
    },
    element: {
      type: Object,
      default: () => {
      }
    },
    inDraw: {
      type: Boolean,
      default: true
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    h: {
      type: Number,
      default: 50
    },
    editMode: {
      type: String,
      require: false,
      default: 'edit'
    },
    isRelation: {
      type: Boolean,
      default: false
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    }
  },
  data() {
    return {
      needRefreshComponents: ['de-select', 'de-select-grid', 'de-select-tree'],
      inputMaxSize: 46,
      inputLargeSize: 42,
      inputSmallSize: 38,
      inputMiniSize: 32,
      options: null,
      showNumber: false,
      mainClass: '',
      mainHeight: 75,
      duHeight: 46,
      titleStyle: null,
      outsideStyle: null
    }
  },
  computed: {
    scale() {
      return this.previewCanvasScale.scalePointHeight
    },
    autoStyle() {
      return {
        height: (100 / this.scale) + '%!important',
        width: (100 / this.scale) + '%!important',
        left: 50 * (1 - 1 / this.scale) + '%', // 放大余量 除以 2
        top: 50 * (1 - 1 / this.scale) + '%', // 放大余量 除以 2
        transform: 'scale(' + this.scale + ')'
      }
    },
    deSelectGridBg() {
      if (this.element.component !== 'de-select-grid') return null
      const { backgroundColorSelect, color } = this.element.commonBackground
      return {
        background: backgroundColorSelect ? color : '#fff',
        border: backgroundColorSelect ? 'none' : '1px solid #d7dae2'
      }
    },
    isFilterComponent() {
      return ['de-select', 'de-select-grid', 'de-date', 'de-input-search', 'de-number-range', 'de-select-tree'].includes(this.element.component)
    },
    ...mapState([
      'curComponent',
      'previewCanvasScale'
    ])
  },
  watch: {
    'element.style': {
      handler(val) {
        this.handlerPositionChange(val)
      },
      deep: true,
      immediate: true
    },
    // 监听外部计时器变化
    searchCount: function(val1) {
      // 正在操作的组件不进行刷新
      if (val1 > 0 && this.needRefreshComponents.includes(this.element.component) && (!this.curComponent || this.curComponent.id !== this.element.id)) {
        this.$refs['deOutWidget'].refreshLoad()
      }
    }
  },
  mounted() {
  },
  created() {
    const { horizontal, vertical, brColor, wordColor, innerBgColor } = this.element.style
    this.$set(this.element.style, 'horizontal', horizontal || 'left')
    this.$set(this.element.style, 'vertical', vertical || 'center')
    this.$set(this.element.style, 'brColor', brColor || '')
    this.$set(this.element.style, 'wordColor', wordColor || '')
    this.$set(this.element.style, 'innerBgColor', innerBgColor || '')
  },
  methods: {
    getComponentId() {
      return this.element.id
    },
    getCanvasId() {
      return this.canvasId
    },
    handlerPositionChange(val) {
      const { horizontal = 'left', vertical = 'center' } = val
      this.titleStyle = {
        width: '100%',
        textAlign: horizontal
      }
      this.outsideStyle = {
        flexDirection: 'column'
      }

      if (vertical !== 'top' && this.element.component !== 'de-select-grid') {
        this.titleStyle = null
        this.outsideStyle = {
          flexDirection: horizontal === 'right' ? 'row-reverse' : '',
          alignItems: 'center'
        }
      }

      if (this.element.component === 'de-select-grid') {
        this.$set(this.outsideStyle, 'flexDirection', 'column')
      }

      if (vertical !== 'top' && this.element.component === 'de-number-range') {
        if (!this.titleStyle) {
          this.titleStyle = {}
        }
        this.titleStyle.marginTop = '-20px'
      }
    },
    getCondition() {
      if (this.$refs && this.$refs['deOutWidget'] && this.$refs['deOutWidget'].getCondition) {
        return this.$refs['deOutWidget'].getCondition()
      }
      return null
    },
    clearHandler() {
      if (this.$refs && this.$refs['deOutWidget'] && this.$refs['deOutWidget'].clearHandler) {
        this.$refs['deOutWidget'].clearHandler()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.my-container {
  position: relative;
  overflow: auto !important;
  top: 0px;
  right: 0px;
  bottom: 0px;
  left: 0px;
}

.condition-main {
  position: absolute;
  overflow: hidden;
  top: 0px;
  right: 0px;
  bottom: 0px;
  left: 0px;
  display: flex;
}

.condition-title {
  height: 2em;
  cursor: -webkit-grab;
  line-height: 2em;
  white-space: nowrap;
}

.span-container {
  overflow: hidden auto;
  position: relative;
  padding: 0 5px;
}

.condition-content {
  overflow: auto hidden;
  letter-spacing: 0px !important;
  width: 100%;
}

.condition-content-container {
  position: relative;
  width: 100%;
  height: 100%;
  white-space: nowrap;
}

.first-element {
  position: relative;
  margin: 0px;
  padding: 0px;
  height: 100%;
}

.first-element-container {
  width: calc(100% - 10px);
  background: initial;
  margin: 0 4px;

  div {
    width: 100%;
  }

  display: flex;
  align-items: flex-end;
}

.first-element-container ::v-deep .el-input__inner {
  height: 40px !important;
  line-height: 40px !important;
}

.first-element-grid-container {
  background: #fff;
  border: 1px solid #d7dae2;
  top: 5px;
  height: 100%;
}

.condition-main-line {
  height: 40px !important;
}

.condition-main {
  display: flex;
  padding-top: 5px;
}

.condition-content-default {
  inset: 0px 0px 0px !important;
}
</style>
