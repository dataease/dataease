<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="sizeFormLine" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.bubble_symbol')" class="form-item">
          <el-select v-model="sizeForm.scatterSymbol" :placeholder="$t('chart.line_symbol')" @change="changeBarSizeCase">
            <el-option
              v-for="item in lineSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.bubble_size')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.scatterSymbolSize" show-input :show-input-controls="false" input-size="mini" :min="1" :max="40" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.not_alpha')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.symbolOpacity" show-input :show-input-controls="false" input-size="mini" :min="1" :max="10" @change="changeBarSizeCase" />
        </el-form-item>
         <el-form-item v-if="sizeForm.scatterSymbol && sizeForm.scatterSymbol !== 'marker'" :label="$t('plugin_view_symbol_map.border')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.symbolStrokeWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="5" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_SIZE } from '@/utils/map'
export default {
  name: 'SizeSelectorAntV',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
      lineSymbolOptions: [
        { name: this.$t('plugin_view_symbol_map.marker'), value: 'marker' },
        { name: this.$t('chart.line_symbol_circle'), value: 'circle' },
        { name: this.$t('chart.line_symbol_rect'), value: 'square' },
        { name: this.$t('chart.line_symbol_triangle'), value: 'triangle' },
        { name: this.$t('plugin_view_symbol_map.pentagon'), value: 'pentagon' },
        { name: this.$t('plugin_view_symbol_map.hexagon'), value: 'hexagon' },
        { name: this.$t('plugin_view_symbol_map.octagon'), value: 'octogon' },
        { name: this.$t('plugin_view_symbol_map.hexagram'), value: 'hexagram' },
        { name: this.$t('chart.line_symbol_diamond'), value: 'rhombus' }
      ]
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.size) {
          this.sizeForm = customAttr.size
        }
      }
    },

    changeBarSizeCase() {
      this.$emit('onSizeChange', this.sizeForm)
    },
    formatTooltip(val) {
      return val / 10
    }
  }
}
</script>

<style scoped>
.shape-item{
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider ::v-deep .el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item ::v-deep .el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
span{
  font-size: 12px
}

.el-form-item{
  margin-bottom: 6px;
}
.el-divider--horizontal {
  margin: 10px 0
}
.divider-style ::v-deep .el-divider__text{
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}
</style>
