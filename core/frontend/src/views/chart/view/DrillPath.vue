<template>
  <div v-if="drillFilters && drillFilters.length > 0">
    <el-breadcrumb
      separator-class="el-icon-arrow-right"
      class="drill-style"
    >
      <el-breadcrumb-item
        class="drill-item"
        @click.native="drillJump(0)"
      >
        <span :style="{'color': textColor}">{{ $t('commons.all') }}</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item
        v-for="(filter,index) in drillFilters"
        :key="index"
        class="drill-item"
        @click.native="drillJump(index + 1)"
      >
        <span :style="{'color': textColor}">{{ filter.value[0] }}</span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script>
import { reverseColor } from '@/views/chart/chart/common/common'
export default {
  name: 'DrillPath',
  props: {
    drillFilters: {
      type: Array,
      default: () => []
    },
    themeStyle: {
      type: Object,
      required: false,
      default: null
    }
  },
  data() {
    return {
      textColor: null
    }
  },
  watch: {
    'themeStyle.backgroundColorSelect'() {
      this.loadThemeStyle()
    },
    'themeStyle.color'() {
      this.loadThemeStyle()
    }
  },
  mounted() {
  },
  created() {
    this.loadThemeStyle()
  },
  methods: {
    drillJump(index) {
      if (index < this.drillFilters.length) {
        this.$emit('onDrillJump', index)
      }
    },
    loadThemeStyle() {
      let themeStyle = null
      if (this.themeStyle) {
        themeStyle = JSON.parse(JSON.stringify(this.themeStyle))
        if (themeStyle && themeStyle.commonBackground) {
          const viewBGColor = themeStyle.commonBackground.color
          if (viewBGColor !== '#FFFFFF') {
            const reverseValue = reverseColor(viewBGColor)
            this.textColor = reverseValue
          } else {
            this.textColor = null
          }
        }
        if (themeStyle && themeStyle.backgroundColorSelect) {
          const panelColor = themeStyle.color
          if (panelColor !== '#FFFFFF') {
            const reverseValue = reverseColor(panelColor)
            this.textColor = reverseValue
          } else {
            this.textColor = null
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.drill-style {
  font-size: 12px;
}
.drill-style ::v-deep .el-breadcrumb__separator{
  margin: 0!important;
}
.drill-item{
  cursor: pointer;
}
</style>
