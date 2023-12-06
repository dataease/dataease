<template>
  <component
    :is="mode"
    v-if="element.options!== null && element.options.attrs!==null && show "
    ref="deSelect"
    v-model="value"
    :class-id="'visual-' + element.id + '-' + inDraw + '-' + inScreen"
    :collapse-tags="showNumber"
    :clearable="!element.options.attrs.multiple && (inDraw || !selectFirst)"
    :multiple="element.options.attrs.multiple"
    :placeholder="$t(element.options.attrs.placeholder) + placeholderSuffix"
    :popper-append-to-body="inScreen"
    :size="size"
    :filterable="inDraw || !selectFirst"
    :filter-method="filterMethod"
    :item-disabled="!inDraw && selectFirst"
    :key-word="keyWord"
    popper-class="coustom-de-select"
    :class="{'disabled-close': !inDraw && selectFirst && element.options.attrs.multiple}"
    :list="data"
    :flag="flag"
    :is-config="isConfig"
    :custom-style="customStyle"
    @resetKeyWords="filterMethod"
    @change="changeValue"
    @focus="setOptionWidth"
    @blur="onBlur"
    @visual-change="visualChange"
    @handleShowNumber="handleShowNumber"
  >
    <el-option
      v-for="item in templateData || data"
      :key="item[element.options.attrs.key]"
      :style="{width:selectOptionWidth}"
      :label="item[element.options.attrs.label]"
      :value="item[element.options.attrs.value]"
      :disabled="!inDraw && selectFirst"
    >
      <span
        :title="item[element.options.attrs.label]"
        style="display:inline-block;width:100%;white-space:nowrap;text-overflow:ellipsis;overflow:hidden;"
      >{{ item[element.options.attrs.label] }}</span>
    </el-option>
  </component>

</template>

<script>
import ElVisualSelect from '@/components/elVisualSelect'
import { linkMultFieldValues, multFieldValues } from '@/api/dataset/dataset'
import bus from '@/utils/bus'
import { isSameVueObj, mergeCustomSortOption } from '@/utils'
import { getLinkToken, getToken } from '@/utils/auth'
import customInput from '@/components/widget/deWidget/customInput'
import { textSelectWidget } from '@/components/widget/deWidget/serviceNameFn.js'
import { uuid } from 'vue-uuid'
export default {
  components: { ElVisualSelect },
  mixins: [customInput],
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
    isConfig: {
      type: Boolean,
      default: false
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
      data: [],
      onFocus: false,
      keyWord: '',
      separator: ',',
      timeMachine: null,
      changeIndex: 0,
      flag: uuid.v1()
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
    templateData() {
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
    },
    isCustomSortWidget() {
      return this.element.serviceName === 'textSelectWidget'
    },
    selectFirst() {
      return this.element.serviceName === 'textSelectWidget' && this.element.options.attrs.selectFirst
    }
  },

  watch: {
    'value': function(val, old) {
      if (!this.inDraw) {
        this.$emit('widget-value-changed', val)
      }
    },
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.fillFirstValue()
      this.setCondition()
    },
    'defaultValueStr': function(value, old) {
      if (value === old) return
      this.value = this.fillValueDerfault()
      this.changeValue(value)
    },
    'element.options.attrs.fieldId': function(value, old) {
      if (value === null || typeof value === 'undefined' || value === old) return
      this.data = []

      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(this.separator), sort: this.element.options.attrs.sort }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.data = this.optionData(res.data)

        this.clearDefault(this.data)
        this.fillFirstValue()
        bus.$emit('valid-values-change', true)
      }).catch(e => {
        bus.$emit('valid-values-change', false)
      }) || (this.element.options.value = '')
    },
    'selectFirst': function(value, old) {
      if (value === old) return
      if (value) {
        this.fillFirstValue()
      } else {
        this.value = ''
        this.firstChange(this.value)
      }
    },
    'element.options.attrs.multiple': function(value, old) {
      if (typeof old === 'undefined' || value === old) return
      if (!this.inDraw) {
        this.value = value ? [] : null
        this.element.options.value = ''
      }

      this.show = false
      this.$nextTick(() => {
        this.fillFirstValue()
        this.show = true
        this.handleCoustomStyle()
      })
    },
    'element.options.attrs.sort': function(value, old) {
      if (value === null || typeof value === 'undefined' || value === old || isSameVueObj(value, old)) return
      this.show = false

      this.data = []

      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      if (!this.element.options.attrs.fieldId) {
        this.show = true
        return
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(this.separator), sort: this.element.options.attrs.sort }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.data = this.optionData(res.data)
        this.$nextTick(() => {
          this.fillFirstValue()
          this.show = true
          this.handleCoustomStyle()
        })
        bus.$emit('valid-values-change', true)
      }).catch(e => {
        bus.$emit('valid-values-change', false)
      }).finally(() => {
        this.show = true
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
    clearDefault(optionList) {
      const emptyOption = !optionList?.length

      if (!this.inDraw && this.element.options.value) {
        if (Array.isArray(this.element.options.value)) {
          if (emptyOption) {
            this.element.options.value = []
            return
          }
          const tempValueArray = JSON.parse(JSON.stringify(this.element.options.value))
          this.element.options.value = tempValueArray.filter(item => optionList.some(option => option === item))
        } else {
          if (emptyOption) {
            this.element.options.value = ''
            return
          }
          const tempValueArray = JSON.parse(JSON.stringify(this.element.options.value.split(this.separator)))
          this.element.options.value = tempValueArray.filter(item => optionList.some(option => option === item)).join(this.separator)
        }
      }
    },
    clearHandler() {
      this.value = this.element.options.attrs.multiple ? [] : null
      this.$refs.deSelect && this.$refs.deSelect.resetSelectAll && this.$refs.deSelect.resetSelectAll()
    },

    searchWithKey(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          this.refreshOptions()
        }
        this.destroyTimeMachine()
      }, 1500)
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    filterMethod(key) {
      if (key === this.keyWord) {
        return
      }
      this.keyWord = key
      this.destroyTimeMachine()
      this.changeIndex++
      this.searchWithKey(this.changeIndex)
    },
    refreshOptions() {
      // this.data = []
      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      if (!this.element.options.attrs.fieldId) {
        return
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(this.separator), sort: this.element.options.attrs.sort, keyword: this.keyWord }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.data = this.optionData(res.data)
        this.flag = uuid.v1()
      })
    },
    onScroll() {
      if (this.onFocus) {
        this.$refs.deSelect.$refs.visualSelect.blur()
      }
    },
    resetDefaultValue(ele) {
      const id = ele.id
      const eleVal = ele.options.value.toString()
      if (this.inDraw && this.manualModify && this.element.id === id && this.value.toString() !== eleVal && this.defaultValueStr === eleVal) {
        if (this.selectFirst) {
          this.fillFirstValue()
          this.firstChange(this.value)
          return
        }
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    onBlur() {
      // this.onFocus = false
    },
    handleElTagStyle() {
      if (this.isConfig) return
      setTimeout(() => {
        this.$refs['deSelect'] && this.$refs['deSelect'].$el && textSelectWidget(this.$refs['deSelect'].$el, this.element.style)
      }, 500)
    },
    initLoad() {
      // this.value = this.fillValueDerfault()
      this.initOptions(this.fillFirstSelected)
      if (this.element.options.value && !this.selectFirst) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    fillFirstSelected() {
      if (this.selectFirst && this.data?.length) {
        this.fillFirstValue()
        this.$emit('filter-loaded', {
          componentId: this.element.id,
          val: (this.value && Array.isArray(this.value)) ? this.value.join(',') : this.value
        })
        this.element.options.loaded = true
        this.$store.commit('setComponentWithId', this.element)
      }
    },
    refreshLoad() {
      this.initOptions()
    },
    initOptions(cb) {
      this.data = []
      if (this.element.options.attrs.fieldId) {
        let method = multFieldValues
        const token = this.$store.getters.token || getToken()
        const linkToken = this.$store.getters.linkToken || getLinkToken()
        if (!token && linkToken) {
          method = linkMultFieldValues
        }
        method({
          fieldIds: this.element.options.attrs.fieldId.split(this.separator),
          sort: this.element.options.attrs.sort
        }).then(res => {
          this.data = this.optionData(res.data)
          bus.$emit('valid-values-change', true)
          cb && cb()
        }).catch(e => {
          bus.$emit('valid-values-change', false)
        })
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
          this.element.options.value = Array.isArray(value) ? value.join(this.separator) : value
        }
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
      }
      this.setCondition()
      this.handleShowNumber()
    },
    firstChange(value) {
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
        canvasId: this.canvasId,
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
      if (!this.element.options.attrs.multiple) {
        return [this.value]
      }
      return this.value.split(',')
    },
    fillFirstValue() {
      if (!this.selectFirst) {
        return
      }
      const defaultV = this.data[0].id
      if (this.element.options.attrs.multiple) {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return []
        this.value = defaultV.split(this.separator)
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
        this.value = defaultV.split(this.separator)[0]
      }
      this.firstChange(this.value)
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (this.element.options.attrs.multiple) {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return []
        return defaultV.split(this.separator)
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
        return defaultV.split(this.separator)[0]
      }
    },
    optionData(data) {
      if (!data) return null
      let tempData = data.filter(item => !!item)
      if (this.isCustomSortWidget && this.element.options.attrs?.sort?.sort === 'custom') {
        tempData = mergeCustomSortOption(this.element.options.attrs.sort.list, tempData)
      }
      return tempData.map(item => {
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

<style lang="scss" scoped>
  .disabled-close ::v-deep .el-icon-close {
    display: none !important;
  }
</style>
<style lang="scss">
.coustom-de-select {
  background-color: var(--BgSelectColor, #FFFFFF) !important;
  border-color: var(--BrSelectColor, #E4E7ED) !important;

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
