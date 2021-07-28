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
  >
    <el-option
      v-for="item in options.attrs.datas"
      :key="item[options.attrs.key]"
      :label="item[options.attrs.label]"
      :value="item[options.attrs.value]"
    />
  </el-select>

</template>

<script>

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
      showNumber: false
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
      this.$store.state.styleChangeTimes++
    }

  }
}
</script>

<style lang="scss" scoped>

</style>
