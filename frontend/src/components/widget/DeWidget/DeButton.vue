<template>

  <el-button
    v-if="element.options!== null && element.options.attrs!==null"
    :type="element.options.attrs.type"
    :round="element.options.attrs.round"
    :plain="element.options.attrs.plain"
    :size="size"
    :class="'de-search-button' + (useDarkClass ? ' dark-search-button' : '')"
    @click="triggerSearch"
  >
    {{ element.options.value }}
  </el-button>
</template>

<script>
import bus from '@/utils/bus'
import { mapState } from 'vuex'
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
  computed: {
    useDarkClass() {
      if (this.canvasStyleData.openCommonStyle && this.canvasStyleData.panel.backgroundType === 'color') {
        const themeColor = this.canvasStyleData.panel.themeColor
        if (themeColor !== 'light') {
          return true
        }
      }
      return false
    },
    ...mapState([
      'canvasStyleData'
    ])

  },
  created() {
  },
  methods: {
    triggerSearch() {
      bus.$emit('trigger-search-button')
    }
  }
}
</script>

<style lang="scss" scoped>
.de-search-button {
  height: 100%;
  width: 100%;
}
.dark-search-button {
  color: #21333B !important;
  background-color: #2681FF !important;
  border-color: #2681FF !important;
  &:hover {
    background: rgb(81, 154, 255) !important;
    border-color: rgb(81, 154, 255) !important;
    color: #21333B !important;
  }
}
</style>
