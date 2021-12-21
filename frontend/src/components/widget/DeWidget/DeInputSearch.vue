<template>

  <el-input
    v-if="element.options!== null && element.options.attrs!==null"
    v-model="value"
    resize="vertical"
    :placeholder="$t(element.options.attrs.placeholder)"
    :size="size"
    @input="valueChange"
    @keypress.enter.native="search"
    @dblclick="setEdit"
  >

    <el-button slot="append" icon="el-icon-search" @click="search" />
  </el-input>

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
    size: String
  },
  data() {
    return {
      operator: 'like',
      value: null,
      canEdit: false
    }
  },
  computed: {
    defaultValueStr() {
      if (!this.element || !this.element.options || !this.element.options.value) return ''
      return this.element.options.value.toString()
    },
    viewIds() {
      if (!this.element || !this.element.options || !this.element.options.attrs.viewIds) return ''
      return this.element.options.attrs.viewIds.toString()
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
      this.search()
    }
  },
  created() {
    if (this.element.options.value) {
      this.value = this.element.options.value
      this.search()
    }
  },
  methods: {
    search() {
      if (!this.inDraw) {
        this.element.options.value = this.value
      }
      this.setCondition()
    },
    setCondition() {
      const param = {
        component: this.element,
        value: !this.value ? [] : [this.value],
        operator: this.operator
      }
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    setEdit() {
      this.canEdit = true
    },
    valueChange(val) {
      if (!this.inDraw) {
        this.element.options.value = val
      }
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
      return defaultV.split(',')[0]
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
