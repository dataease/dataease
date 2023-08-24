// 通过控制 js 控制过滤组件输入框样式 如需额外处理 声明组件serviceName同名函数处理
import {
  attrsMap,
  styleAttrs,
  textInputWidget,
  timeDateRangeWidget
} from '@/components/widget/deWidget/serviceNameFn.js'

export default {
  data() {
    return {
      attrsMap,
      styleAttrs,
      // 过滤组件名ref映射
      refComNameMap: {
        'de-date': 'dateRef',
        'de-select-grid': 'de-select-grid',
        'de-select': 'deSelect',
        'de-select-tree': 'deSelectTree',
        'de-input-search': 'de-input-search',
        'de-number-range': ['de-number-range-min', 'de-number-range-max']
      }
    }
  },
  watch: {
    cssArr: {
      handler(newValue) {
        if (!this.isFilterComponent) return
        this.typeTransform().forEach(ele => {
          this.handlerInputStyle(ele, newValue)
        })
      },
      deep: true
    },
    multiple: {
      handler() {
        if (!['de-select-tree', 'de-select'].includes(this.element.component)) return
        const time = setTimeout(() => {
          clearTimeout(time)
          this.typeTransform().forEach(ele => {
            this.handlerInputStyle(ele, this.cssArr)
          })
        }, 100)
      },
      deep: true
    }
  },
  computed: {
    cssArr() {
      const { brColor, wordColor, innerBgColor } = this.element.style
      return { brColor, wordColor, innerBgColor }
    },
    multiple() {
      const { multiple = false } = this.element.options.attrs
      return multiple
    }
  },
  mounted() {
    if (!this.isFilterComponent) return
    this.typeTransform().forEach(item => {
      const nodeCache = this.$refs.deOutWidget?.$refs[item].$el.querySelector('.el-input__inner') || this.$refs.deOutWidget?.$refs[item].$el
      this.styleAttrs.forEach(ele => {
        nodeCache.style[this.attrsMap[ele]] = this.element.style[ele]
        this[this.element.serviceName] && this[this.element.serviceName](this.selectRange(item), ele, this.element.style[ele])
      })
    })
  },
  methods: {
    handlerInputStyle(type, newValue) {
      let nodeCache = ''
      this.styleAttrs.forEach(ele => {
        if (!nodeCache) {
          nodeCache = this.$refs.deOutWidget?.$refs[type].$el.querySelector('.el-input__inner') || this.$refs.deOutWidget?.$refs[type].$el
        }
        if (nodeCache) {
          nodeCache.style[this.attrsMap[ele]] = newValue[ele]
        }
        this[this.element.serviceName] && this[this.element.serviceName](this.selectRange(type), ele, newValue[ele])
      })
    },
    selectRange(item) {
      if (this.element.component === 'de-select-grid') {
        return this.$refs.deOutWidget.$el
      }
      return this.$refs.deOutWidget.$refs[item].$el
    },
    timeDateRangeWidget: timeDateRangeWidget,
    textInputWidget: textInputWidget,
    typeTransform() {
      let refNode = this.refComNameMap[this.element.component]
      if (!refNode) return []
      if (!Array.isArray(refNode)) {
        refNode = [refNode]
      }
      return refNode
    }
  }
}
