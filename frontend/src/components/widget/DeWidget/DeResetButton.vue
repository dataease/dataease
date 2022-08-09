<template>

  <el-button
    v-if="element.options!== null && element.options.attrs!==null"
    :type="element.options.attrs.type"
    :round="element.options.attrs.round"
    :plain="element.options.attrs.plain"
    :size="size"
    class="de-search-button"
    @click="triggerReset"
  >
    {{ element.options.value }}
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
      operator: 'eq',
      values: null
    }
  },
  created() {
  },
  methods: {
    triggerReset() {
      bus.$emit('trigger-reset-button')
    }
  }
}
</script>

<style lang="scss" scoped>
.de-search-button {
  height: 100%;
  width: 100%;
}
</style>
