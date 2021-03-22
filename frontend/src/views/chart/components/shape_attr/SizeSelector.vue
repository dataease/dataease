<template>
  <div>
    <div style="width:100%;height: 32px;margin:0;padding:0 4px;border-radius: 4px;border: 1px solid #DCDFE6;display: flex;align-items: center;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form v-if="chart.type && chart.type.includes('bar')" ref="sizeFormBar" :model="sizeForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.adapt')" class="form-item">
              <el-checkbox v-model="sizeForm.barDefault" @change="changeBarSizeCase">{{ $t('chart.adapt') }}</el-checkbox>
            </el-form-item>
            <el-form-item :label="$t('chart.bar_width')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.barWidth" :disabled="sizeForm.barDefault" show-input :show-input-controls="false" input-size="mini" :min="1" :max="80" @change="changeBarSizeCase" />
            </el-form-item>
            <el-form-item :label="$t('chart.bar_gap')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.barGap" :disabled="sizeForm.barDefault" show-input :show-input-controls="false" input-size="mini" :min="0" :max="5" :step="0.1" @change="changeBarSizeCase" />
            </el-form-item>
          </el-form>

          <el-form v-if="chart.type && chart.type.includes('line')" ref="sizeFormLine" :model="sizeForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.line_width')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.lineWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="10" @change="changeBarSizeCase" />
            </el-form-item>
            <el-form-item :label="$t('chart.line_type')" class="form-item">
              <el-radio-group v-model="sizeForm.lineType" @change="changeBarSizeCase">
                <el-radio label="solid">{{ $t('chart.line_type_solid') }}</el-radio>
                <el-radio label="dashed">{{ $t('chart.line_type_dashed') }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('chart.line_symbol')" class="form-item">
              <el-select v-model="sizeForm.lineSymbol" :placeholder="$t('chart.line_symbol')" @change="changeBarSizeCase">
                <el-option
                  v-for="item in lineSymbolOptions"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('chart.line_symbol_size')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.lineSymbolSize" show-input :show-input-controls="false" input-size="mini" :min="0" :max="20" @change="changeBarSizeCase" />
            </el-form-item>
            <el-form-item :label="$t('chart.line_smooth')" class="form-item">
              <el-checkbox v-model="sizeForm.lineSmooth" @change="changeBarSizeCase">{{ $t('chart.line_smooth') }}</el-checkbox>
            </el-form-item>
          </el-form>

          <el-form v-if="chart.type && chart.type.includes('pie')" ref="sizeFormPie" :model="sizeForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.pie_inner_radius')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.pieInnerRadius" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
            </el-form-item>
            <el-form-item :label="$t('chart.pie_outer_radius')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.pieOuterRadius" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
            </el-form-item>
          </el-form>

          <el-form v-if="chart.type && chart.type.includes('funnel')" ref="sizeFormPie" :model="sizeForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.funnel_width')" class="form-item form-item-slider">
              <el-slider v-model="sizeForm.funnelWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.size') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_SIZE } from '../../chart/chart'
export default {
  name: 'SizeSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
      lineSymbolOptions: [
        { name: this.$t('chart.line_symbol_none'), value: 'none' },
        { name: this.$t('chart.line_symbol_emptyCircle'), value: 'emptyCircle' },
        { name: this.$t('chart.line_symbol_circle'), value: 'circle' },
        { name: this.$t('chart.line_symbol_rect'), value: 'rect' },
        { name: this.$t('chart.line_symbol_roundRect'), value: 'roundRect' },
        { name: this.$t('chart.line_symbol_triangle'), value: 'triangle' },
        { name: this.$t('chart.line_symbol_diamond'), value: 'diamond' },
        { name: this.$t('chart.line_symbol_pin'), value: 'pin' },
        { name: this.$t('chart.line_symbol_arrow'), value: 'arrow' }
      ]
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        if (chart.customAttr) {
          const customAttr = JSON.parse(chart.customAttr)
          if (customAttr.size) {
            this.sizeForm = customAttr.size
          }
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    changeBarSizeCase() {
      this.$emit('onSizeChange', this.sizeForm)
    }
  }
}
</script>

<style scoped lang="scss">
.shape-item{
  padding: 6px;
  border: none;
}
.form-item-slider>>>.el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item>>>.el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
  span{font-size: 12px}

.el-form-item{
  margin-bottom: 6px;
}
</style>
