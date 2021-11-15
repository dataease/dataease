<template>

  <el-select
    v-if="options!== null && options.attrs!==null"
    ref="deSelect"
    v-model="options.value"
    :collapse-tags="showNumber"
    :clearable="!options.attrs.multiple"
    :multiple="options.attrs.multiple"
    :placeholder="$t(options.attrs.placeholder)"
    :popper-append-to-body="inScreen"
    @change="changeValue"
    @focus="setOptionWidth"
  >
    <el-option
      v-for="item in options.attrs.datas"
      :key="item[options.attrs.key]"
      :style="{width:selectOptionWidth}"
      :label="item[options.attrs.label]"
      :value="item[options.attrs.value]"
    >
      <span :title="item[options.attrs.label]" style="display:inline-block;width:100%;white-space:nowrap;text-overflow:ellipsis;overflow:hidden;">{{ item[options.attrs.label] }}</span>
    </el-option>
  </el-select>

</template>

<script>
import { fieldValues } from '@/api/dataset/dataset'
export default {

  props: {
    element: {
      type: Object,
      default: null
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
      options: null,
      showNumber: false,
      selectOptionWidth: 0
    }
  },
  computed: {
    operator() {
      return this.options.attrs.multiple ? 'in' : 'eq'
    }
  },
  watch: {
    'options.attrs.multiple': function(value) {
      const sourceValue = this.options.value
      const sourceValid = !!sourceValue && Object.keys(sourceValue).length > 0
      if (value) {
        !sourceValid && (this.options.value = [])
        sourceValid && !Array.isArray(sourceValue) && (this.options.value = sourceValue.split(','))
        !this.inDraw && (this.options.value = [])
      } else {
        !sourceValid && (this.options.value = null)
        sourceValid && Array.isArray(sourceValue) && (this.options.value = sourceValue[0])
        !this.inDraw && (this.options.value = null)
      }
    }
  },
  created() {
    this.options = this.element.options
    if (this.options.attrs.fieldId) {
      fieldValues(this.options.attrs.fieldId).then(res => {
        this.options.attrs.datas = this.optionDatas(res.data)
      })
    }

    // this.setCondition()
  },
  mounted() {
    // this.$nextTick(() => {
    //   this.options && this.options.value && this.changeValue(this.options.value)
    // })
    this.options && this.options.value && Object.keys(this.options.value).length > 0 && this.changeValue(this.options.value)
  },
  methods: {
    changeValue(value) {
      this.setCondition()
      this.styleChange()
      this.showNumber = false
      this.$nextTick(() => {
        if (!this.$refs.deSelect.$refs.tags || !this.options.attrs.multiple) {
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
        value: Array.isArray(this.options.value) ? this.options.value : [this.options.value],
        operator: this.operator
      }
      this.inDraw && this.$store.commit('addViewFilter', param)
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
