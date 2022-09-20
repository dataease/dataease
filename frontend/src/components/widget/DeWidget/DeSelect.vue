<template>
  <component
    :is="mode"
    v-if="element.options!== null && element.options.attrs!==null && show "
    ref="deSelect"
    v-model="value"
    :class-id="'visual-' + element.id + '-' + inDraw + '-' + inScreen"
    :collapse-tags="showNumber"
    :clearable="!element.options.attrs.multiple"
    :multiple="element.options.attrs.multiple"
    :placeholder="$t(element.options.attrs.placeholder) + placeholderSuffix"
    :popper-append-to-body="inScreen"
    :size="size"
    :filterable="true"
    :filter-method="filterMethod"
    :key-word="keyWord"
    popper-class="coustom-de-select"
    :list="datas"
    :custom-style="customStyle"
    @change="changeValue"
    @focus="setOptionWidth"
    @blur="onBlur"
    @visual-change="visualChange"
    @handleShowNumber="handleShowNumber"
  >
    <el-option
      v-for="item in templateDatas || datas"
      :key="item[element.options.attrs.key]"
      :style="{width:selectOptionWidth}"
      :label="item[element.options.attrs.label]"
      :value="item[element.options.attrs.value]"
    >
      <span :title="item[element.options.attrs.label]" style="display:inline-block;width:100%;white-space:nowrap;text-overflow:ellipsis;overflow:hidden;">{{ item[element.options.attrs.label] }}</span>
    </el-option>
  </component>

</template>

<script>
import ElVisualSelect from '@/components/ElVisualSelect'
import { multFieldValues, linkMultFieldValues } from '@/api/dataset/dataset'
import bus from '@/utils/bus'
import { isSameVueObj } from '@/utils'
import { getLinkToken, getToken } from '@/utils/auth'
import customInput from '@/components/widget/DeWidget/customInput'
import { textSelectWidget } from '@/components/widget/DeWidget/serviceNameFn.js'

export default {
  components: { ElVisualSelect },
  mixins: [customInput],
  props: {
    element: {
      type: Object,
      default: () => {}
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
    size: String,
    isRelation: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showNumber: false,
      selectOptionWidth: 0,
      show: true,
      value: null,
      datas: [],
      onFocus: false,
      keyWord: ''
    }
  },
  computed: {
    mode() {
      let result = 'el-select'
      if (this.element.options && this.element.options.attrs && this.element.options.attrs.visual) {
        result = 'el-visual-select'
      }
      return result
    },
    templateDatas() {
      return this.mode === 'el-visual-select' ? [] : null
    },
    operator() {
      return this.element.options.attrs.multiple ? 'in' : 'eq'
    },
    defaultValueStr() {
      if (!this.element || !this.element.options || !this.element.options.value) return ''
      return this.element.options.value.toString()
    },
    viewIds() {
      if (!this.element || !this.element.options || !this.element.options.attrs.viewIds) return ''
      return this.element.options.attrs.viewIds.toString()
    },
    manualModify() {
      return !!this.element.options.manualModify
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    customStyle() {
      const { brColor, wordColor, innerBgColor } = this.element.style
      return { brColor, wordColor, innerBgColor }
    },
    placeholderSuffix() {
      const i18nKey = this.element.options.attrs.multiple ? 'panel.multiple_choice' : 'panel.single_choice'
      const i18nValue = this.$t(i18nKey)
      return '(' + i18nValue + ')'
    }
  },

  watch: {
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    'defaultValueStr': function(value, old) {
      if (value === old) return
      this.value = this.fillValueDerfault()
      this.changeValue(value)
    },
    'element.options.attrs.fieldId': function(value, old) {
      if (value === null || typeof value === 'undefined' || value === old) return
      this.datas = []

      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.datas = this.optionDatas(res.data)
        bus.$emit('valid-values-change', true)
      }).catch(e => {
        bus.$emit('valid-values-change', false)
      }) || (this.element.options.value = '')
    },
    'element.options.attrs.multiple': function(value, old) {
      if (typeof old === 'undefined' || value === old) return
      if (!this.inDraw) {
        this.value = value ? [] : null
        this.element.options.value = ''
      }

      this.show = false
      this.$nextTick(() => {
        this.show = true
        this.handleCoustomStyle()
      })
    },
    'element.options.attrs.sort': function(value, old) {
      if (value === null || typeof value === 'undefined' || value === old || isSameVueObj(value, old)) return
      this.show = false

      this.datas = []

      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.datas = this.optionDatas(res.data)
        this.$nextTick(() => {
          this.show = true
          this.handleCoustomStyle()
        })
        bus.$emit('valid-values-change', true)
      }).catch(e => {
        bus.$emit('valid-values-change', false)
      }) || (this.element.options.value = '')
    }

  },
  created() {
    if (this.element && this.element.options && this.element.options.attrs) {
      this.element.options.attrs.visual = true
    }

    if (!this.element.options.attrs.sort) {
      this.element.options.attrs.sort = {}
    }
    this.initLoad()
  },

  mounted() {
    bus.$on('onScroll', this.onScroll)
    if (this.inDraw) {
      bus.$on('reset-default-value', this.resetDefaultValue)
    }
  },
  beforeDestroy() {
    bus.$off('onScroll', this.onScroll)
    bus.$off('reset-default-value', this.resetDefaultValue)
  },
  methods: {
    clearHandler() {
      this.value = this.element.options.attrs.multiple ? [] : null
      this.$refs.deSelect && this.$refs.deSelect.resetSelectAll && this.$refs.deSelect.resetSelectAll()
    },
    filterMethod(key) {
      this.keyWord = key
    },
    onScroll() {
      if (this.onFocus) {
        this.$refs.deSelect.$refs.visualSelect.blur()
      }
    },
    resetDefaultValue(id) {
      if (this.inDraw && this.manualModify && this.element.id === id) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    onBlur() {
      // this.onFocus = false
    },
    handleElTagStyle() {
      setTimeout(() => {
        this.$refs['deSelect'] && this.$refs['deSelect'].$el && textSelectWidget(this.$refs['deSelect'].$el, this.element.style)
      }, 50)
    },
    initLoad() {
      this.value = this.fillValueDerfault()
      this.datas = []
      if (this.element.options.attrs.fieldId) {
        let method = multFieldValues
        const token = this.$store.getters.token || getToken()
        const linkToken = this.$store.getters.linkToken || getLinkToken()
        if (!token && linkToken) {
          method = linkMultFieldValues
        }
        method({ fieldIds: this.element.options.attrs.fieldId.split(','), sort: this.element.options.attrs.sort }).then(res => {
          this.datas = this.optionDatas(res.data)
          bus.$emit('valid-values-change', true)
        }).catch(e => {
          bus.$emit('valid-values-change', false)
        })
      }
      if (this.element.options.value) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    visualChange(value) {
      this.value = value
      this.$nextTick(() => {
        if (!this.element.options.attrs.multiple) {
          return
        }
        this.handleElTagStyle()
      })
    },
    changeValue(value) {
      if (!this.inDraw) {
        if (value === null) {
          this.element.options.value = ''
        } else {
          this.element.options.value = Array.isArray(value) ? value.join() : value
        }
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
      }
      this.setCondition()
      this.handleShowNumber()
    },
    handleShowNumber() {
      this.showNumber = false

      this.$nextTick(() => {
        if (!this.$refs.deSelect || !this.$refs.deSelect.$refs.visualSelect || !this.$refs.deSelect.$refs.visualSelect.$refs.tags) return
        const tags = this.$refs.deSelect.$refs.visualSelect.$refs.tags
        if (!this.element.options.attrs.multiple || !this.$refs.deSelect || !tags) {
          return
        }
        const kids = tags.children[0].children
        let contentWidth = 0
        kids.forEach(kid => {
          contentWidth += kid.offsetWidth
        })
        this.showNumber = contentWidth > ((tags.clientWidth - 30) * 0.9)
        this.handleElTagStyle()
      })
    },
    getCondition() {
      const param = {
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      return param
    },
    setCondition() {
      const param = this.getCondition()
      !this.isRelation && this.inDraw && this.$store.commit('addViewFilter', param)
    },
    formatFilterValue() {
      if (this.value === null) return []
      if (Array.isArray(this.value)) return this.value
      return this.value.split(',')
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (this.element.options.attrs.multiple) {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return []
        return defaultV.split(',')
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
        return defaultV.split(',')[0]
      }
    },
    optionDatas(datas) {
      if (!datas) return null
      return datas.filter(item => !!item).map(item => {
        return {
          id: item,
          text: item
        }
      })
    },
    setOptionWidth(event) {
      this.onFocus = true
      // 下拉框弹出时，设置弹框的宽度
      this.$nextTick(() => {
        // this.selectOptionWidth = event.srcElement.offsetWidth + 'px'
        this.selectOptionWidth = event.srcElement.parentElement.parentElement.offsetWidth + 'px'
        this.handleCoustomStyle()
      })
    }

  }

}
</script>

<style lang="scss">
.coustom-de-select {
  background-color: var(--BgSelectColor, #FFFFFF) !important;
  border-color: var(--BrSelectColor, #E4E7ED) !important;
  // .popper__arrow::after{
  //   border-bottom-color: var(--BgSelectColor, #FFFFFF) !important;
  // }

  .popper__arrow,
  .popper__arrow::after {
    display: none !important;
  }

  .el-select-dropdown__item {
    color: var(--SelectColor, #606266);
  }

  .el-select-dropdown__item.selected {
    color: #409eff;
    background-color: rgb(245, 247, 250, .5);
  }

  .el-select-dropdown__item.hover {
    background-color: rgb(245, 247, 250, .5);
  }
}

.coustom-de-select.is-multiple {
  .el-select-dropdown__item.selected {
    background-color: rgb(245, 247, 250, .5) !important;
  }
  .el-select-dropdown__item.hover {
    background-color: rgb(245, 247, 250, .5) !important;
  }
}
</style>
