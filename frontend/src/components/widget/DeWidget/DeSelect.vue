<template>

  <el-select
    v-if="options!== null && options.attrs!==null"
    ref="deSelect"
    v-model="options.value"
    :collapse-tags="showNumber"
    :clearable="!options.attrs.multiple"
    :multiple="options.attrs.multiple"
    :placeholder="options.attrs.placeholder"
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
    }
  },
  data() {
    return {
      options: null,
      //   operator: 'eq',
      values: null,
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
      if (value) {
        this.values = []
      } else {
        this.values = null
      }
    }
  },
  created() {
    this.options = this.element.options
    this.setCondition()
  },
  mounted() {
    this.$nextTick(() => {

    })
  },
  methods: {
    changeValue(value) {
      this.setCondition()
      // this.inDraw && this.$emit('set-condition-value', { component: this.element, value: [value], operator: this.operator })
      this.$nextTick(() => {
        const kids = this.$refs.deSelect.$refs.tags.children[0].children
        let contentWidth = 0
        kids.forEach(kid => {
          contentWidth += kid.offsetWidth
        })
        this.showNumber = contentWidth > (this.$refs.deSelect.$refs.tags.clientWidth * 0.7)
      })
    },

    setCondition() {
      const param = {
        component: this.element,
        value: Array.isArray(this.options.value) ? this.options.value : [this.options.value],
        operator: this.operator
      }
      this.inDraw && this.$store.dispatch('conditions/add', param)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
