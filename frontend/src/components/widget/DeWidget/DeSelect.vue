<template>

  <el-select
    v-if="element.options!== null && element.options.attrs!==null && show"
    ref="deSelect"
    class="deSelect"
    v-model="value"
    :collapse-tags="showNumber"
    :clearable="!element.options.attrs.multiple"
    :multiple="element.options.attrs.multiple"
    :placeholder="$t(element.options.attrs.placeholder)"
    :popper-append-to-body="inScreen"
    :size="size"
    @change="changeValue"
    @focus="setOptionWidth"
    @blur="onBlur"
  >
    <el-option
      v-for="item in datas"
      :key="item[element.options.attrs.key]"
      :style="{width:selectOptionWidth}"
      :label="item[element.options.attrs.label]"
      :value="item[element.options.attrs.value]"
    >
      <span :title="item[element.options.attrs.label]" style="display:inline-block;width:100%;white-space:nowrap;text-overflow:ellipsis;overflow:hidden;">{{ item[element.options.attrs.label] }}</span>
    </el-option>
  </el-select>

</template>

<script>
import { multFieldValues, linkMultFieldValues } from '@/api/dataset/dataset'
import bus from '@/utils/bus'
import { getLinkToken, getToken } from '@/utils/auth'
export default {

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
    size: String
  },
  data() {
    return {
      showNumber: false,
      selectOptionWidth: 0,
      show: true,
      value: null,
      datas: [],
      onFocus: false
    }
  },
  computed: {
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
      if (typeof value === 'undefined' || value === old) return
      this.datas = []

      let method = multFieldValues
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = linkMultFieldValues
      }
      const param = { fieldIds: this.element.options.attrs.fieldId.split(',') }
      if (this.panelInfo.proxy) {
        param.userId = this.panelInfo.proxy
      }
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      method(param).then(res => {
        this.datas = this.optionDatas(res.data)
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
      })
    }

  },
  created() {
    this.initLoad()
  },
  mounted() {
    bus.$on('onScroll', () => {
      if (this.onFocus) {
        this.$refs.deSelect.blur()
      }
    })
    bus.$on('reset-default-value', id => {
      if (this.inDraw && this.manualModify && this.element.id === id) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    })
  },

  methods: {
    onBlur() {
      this.onFocus = false
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
        method({ fieldIds: this.element.options.attrs.fieldId.split(',') }).then(res => {
          this.datas = this.optionDatas(res.data)
        })
      }
      if (this.element.options.value) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    changeValue(value) {
      console.log('下拉框的值', value)
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
      this.showNumber = false

      this.$nextTick(() => {
        if (!this.element.options.attrs.multiple || !this.$refs.deSelect || !this.$refs.deSelect.$refs.tags) {
          return
        }
        const kids = this.$refs.deSelect.$refs.tags.children[0].children
        let contentWidth = 0
        kids.forEach(kid => {
          contentWidth += kid.offsetWidth
        })
        this.showNumber = contentWidth > ((this.$refs.deSelect.$refs.tags.clientWidth - 30) * 0.9)
      })
    },

    setCondition() {
      const param = {
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      console.log('param触发---', param)
      this.inDraw && this.$store.commit('addViewFilter', param)
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
        this.selectOptionWidth = event.srcElement.offsetWidth + 'px'
      })
    }

  }

}
</script>

<style lang="scss" scoped>
// .deSelect ::v-deep .el-input__inner {
//   background-color: transparent;
// }
</style>
