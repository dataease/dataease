<template>

  <el-select
    v-if="options!== null && options.attrs!==null"
    v-model="values"
    :size="size"
    :multiple="options.attrs.multiple"
    :placeholder="options.attrs.placeholder"
    :popper-append-to-body="inScreen"
    @change="changeValue"
  >
    <el-option
      v-for="item in options.attrs.data"
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
    },
    size: String
  },
  data() {
    return {
      options: null,
      operator: 'eq',
      values: null
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
  },
  mounted() {
    this.$nextTick(() => {

    })
  },
  methods: {
    changeValue(value) {
      this.inDraw && this.$emit('set-condition-value', { component: this.element, value: [value], operator: this.operator })
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
