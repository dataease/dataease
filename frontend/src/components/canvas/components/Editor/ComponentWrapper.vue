<template>
  <div
    :style="getOutStyleDefault(config.style)"
    class="component"
    @click="handleClick"
    @mousedown="elementMouseDown"
  >
    <!--视图右上角组件编辑器-->
    <edit-bar v-if="curComponent && config === curComponent" :element="config" @showViewDetails="showViewDetails" />
    <de-out-widget
      v-if="config.type==='custom'"
      :id="'component' + config.id"
      class="component-custom"
      :style="getComponentStyleDefault(config.style)"
      :out-style="config.style"
      :element="config"
      :in-screen="inScreen"
    />
    <component
      :is="config.component"
      v-else
      ref="wrapperChild"
      :out-style="config.style"
      :style="getComponentStyleDefault(config.style)"
      :prop-value="config.propValue"
      :is-edit="false"
      :element="config"
      :search-count="searchCount"
      :h="config.style.height"
    />
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import runAnimation from '@/components/canvas/utils/runAnimation'
import { mixins } from '@/components/canvas/utils/events'
import { mapState } from 'vuex'
import DeOutWidget from '@/components/dataease/DeOutWidget'
import EditBar from '@/components/canvas/components/Editor/EditBar'
import MobileCheckBar from '@/components/canvas/components/Editor/MobileCheckBar'

export default {
  components: { MobileCheckBar, DeOutWidget, EditBar },
  mixins: [mixins],
  props: {
    config: {
      type: Object,
      require: true,
      default: null
    },
    filter: {
      type: Object,
      require: false,
      default: null
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  computed: {
    curGap() {
      return this.canvasStyleData.panel.gap === 'yes' && this.config.auxiliaryMatrix ? this.componentGap : 0
    },
    ...mapState([
      'mobileLayoutStatus',
      'canvasStyleData',
      'curComponent',
      'componentGap'
    ])
  },
  mounted() {
    runAnimation(this.$el, this.config.animations)
  },
  methods: {
    getStyle,
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
        const top = this.format(style['top'], this.scaleHeight)
        return top
      }
    },
    format(value, scale) {
      // 自适应画布区域 返回原值
      return value * scale / 100
    },
    getOutStyleDefault(style) {
      const result = {
        padding: this.curGap + 'px'
      }
      // 移动端编辑状态 且 未被移动端选中的组件 放满容器
      if (this.mobileLayoutStatus && !this.config.mobileSelected) {
        result.width = '100%'
        result.height = '100%'
      } else {
        ['width', 'left'].forEach(attr => {
          result[attr] = style[attr] + 'px'
        });
        ['height', 'top'].forEach(attr => {
          result[attr] = style[attr] + 'px'
        })
        result['rotate'] = style['rotate']
      }
      return result
    },

    getComponentStyleDefault(style) {
      // 移动端编辑状态 且 未被移动端选中的组件 放满容器
      if (this.mobileLayoutStatus && !this.config.mobileSelected) {
        return {
          width: '100%',
          height: '100%'
        }
      } else {
        return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
      }
    },

    handleClick() {
      const events = this.config.events
      Object.keys(events).forEach(event => {
        this[event](events[event])
      })
    },
    elementMouseDown(e) {
      // private 设置当前组件数据及状态
      this.$store.commit('setClickComponentStatus', true)
      if (this.config.component !== 'v-text' && this.config.component !== 'rect-shape' && this.config.component !== 'de-input-search' && this.config.component !== 'de-select-grid' && this.config.component !== 'de-number-range' && this.config.component !== 'de-date') {
        e.preventDefault()
      }
      // 阻止冒泡事件
      e.stopPropagation()
      this.$store.commit('setCurComponent', { component: this.config, index: this.index })
    },
    showViewDetails() {
      this.$refs.wrapperChild.openChartDetailsDialog()
    }
  }
}
</script>

<style lang="scss" scoped>
.component {
    position: absolute;
}

.component:hover {
  box-shadow:0px 0px 7px #0a7be0;
}
.gap_class{
  padding:5px;
}
.component-custom {
  outline: none;
  width: 100% !important;
  height: 100%;
}
</style>
