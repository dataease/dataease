// 通过控制css变量控制过滤组件弹框样式 de-select-grid除外
import { attrsMap, styleAttrs } from '@/components/widget/DeWidget/serviceNameFn.js'

export default {
  data() {
    return {
      attrsMap,
      styleAttrs,
      // 过滤组件名css变量映射
      refComNameMap: {
        'de-date': ['--BgDateColor', '--DateColor', '--BrDateColor'],
        'de-select': ['--BgSelectColor', '--SelectColor', '--BrSelectColor'],
        'de-select-tree': ['--BgSelectTreeColor', '--SelectTreeColor', '--BrSelectTreeColor'],
        'de-input-search': ['--BgSearchColor', '--SearchColor', '--BrSearchColor'],
        'de-number-range': ['--BgRangeColor', '--RangeColor', '--BrRangeColor']
      }
    }
  },
  watch: {
    cssArr: {
      handler() {
        if (['de-select', 'de-select-tree'].includes(this.element.component)) {
          if (!this.element.options.attrs.multiple) {
            return
          }
          this.handleElTagStyle()
        }
      },
      deep: true
    },
  },
  computed: {
    cssArr() {
      const { brColor, wordColor, innerBgColor } = this.element.style
      return { brColor, wordColor, innerBgColor }
    }
  },
  mounted() {
    this.handleCoustomStyle()
  },
  methods: {
    typeTransform() {
      const refNode = this.refComNameMap[this.element.component]
      if (!refNode) return []
      return refNode
    },
    handleCoustomStyle() {
      // 判断组件是否是在仪表板内部 否则css样式取默认值
      const isPanelDe = this.$parent.handlerInputStyle
      const { brColor, wordColor, innerBgColor } = this.element.style
      const newValue = { brColor, wordColor, innerBgColor }
      const cssVar = this.typeTransform()
      this.styleAttrs.forEach((ele, index) => {
        if (newValue[ele]) {
          document.documentElement.style.setProperty(cssVar[index], !isPanelDe ? '' : newValue[ele])
        } else {
          document.documentElement.style.removeProperty(cssVar[index])
        }
      })
    },
  }
}
