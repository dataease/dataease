<template>

  <el-button
    v-if="element.options!== null && element.options.attrs!==null"
    :type="element.options.attrs.type"
    :round="element.options.attrs.round"
    :plain="element.options.attrs.plain"
    :size="size"
    :class="'de-search-button' + (useDarkClass ? ' dark-reset-button' : '')"
    @click="triggerReset"
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
.dark-reset-button {
  color: #f2f6fc !important;
  background-color: #21333b !important;
  border-color: #bbbfc4 !important;
  border: 1px solid;
  &:hover {
    background: #bec2c7 !important;
    border-color: #bbbfc4 !important;
  }
}
</style>
