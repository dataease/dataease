<template>

  <el-button
    v-if="options!== null && options.attrs!==null"
    :type="options.attrs.type"
    :round="options.attrs.round"
    :plain="options.attrs.plain"
    :size="size"
    @click="triggerSearch"
  >
    {{ options.value }}
  </el-button>
</template>

<script>
import bus from '@/utils/bus'
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
      options: null,
      operator: 'eq',
      values: null
    }
  },
  created() {
    this.options = this.element.options
  },
  methods: {
    triggerSearch() {
      bus.$emit('trigger-search-button')
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
