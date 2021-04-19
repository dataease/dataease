<template>

  <el-select v-if="options!== null && options.attrs!==null" v-model="options.value" :multiple="options.attrs.multiple" :style="element.style" :placeholder="options.attrs.placeholder" @change="changeValue">
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
      operator: 'eq'
    }
  },
  watch: {
    'element.style': function(value) {
      // console.log(value)
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
