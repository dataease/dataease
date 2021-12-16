<template>

  <el-select
    v-if="element.options!== null && element.options.attrs!==null && show"
    ref="deSelect"
    v-model="value"
    :collapse-tags="showNumber"
    :clearable="!element.options.attrs.multiple"
    :multiple="element.options.attrs.multiple"
    :placeholder="$t(element.options.attrs.placeholder)"
    :popper-append-to-body="inScreen"
    @change="changeValue"
    @focus="setOptionWidth"
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
import { multFieldValues } from '@/api/dataset/dataset'
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
    }
  },
  data() {
    return {
      showNumber: false,
      selectOptionWidth: 0,
      show: true,
      value: null,
      datas: []
    }
  },
  computed: {
    operator() {
      return this.element.options.attrs.multiple ? 'in' : 'eq'
    }
  },

  watch: {
    'element.options.value': function(value, old) {
      if (value === old) return
      this.value = this.fillValueDerfault()
      this.changeValue(value)
    },
    'element.options.attrs.fieldId': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.datas = []
      this.element.options.attrs.fieldId &&
      this.element.options.attrs.fieldId.length > 0 &&
      multFieldValues(this.element.options.attrs.fieldId.split(',')).then(res => {
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

  methods: {
    initLoad() {
      this.value = this.fillValueDerfault()
      this.datas = []
      if (this.element.options.attrs.fieldId) {
        multFieldValues(this.element.options.attrs.fieldId.split(',')).then(res => {
          this.datas = this.optionDatas(res.data)
        })
      }
      if (this.element.options.value) {
        this.value = this.fillValueDerfault()
        this.changeValue(this.value)
      }
    },
    changeValue(value) {
      if (!this.inDraw) {
        if (value === null) {
          this.element.options.value = ''
        } else {
          this.element.options.value = Array.isArray(value) ? value.join() : value
        }
      }
      this.setCondition()
      this.styleChange()
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
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '') return []
        return defaultV.split(',')
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '') return null
        return defaultV.split(',')[0]
      }
    },
    styleChange() {
      this.$store.commit('recordStyleChange')
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
    // 下拉框弹出时，设置弹框的宽度
      this.$nextTick(() => {
        this.selectOptionWidth = event.srcElement.offsetWidth + 'px'
      })
    }

  }

}
</script>

<style lang="scss" scoped>

</style>
