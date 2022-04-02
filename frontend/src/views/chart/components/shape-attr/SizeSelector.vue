<template>
  <div style="width: 100%">
    <el-col>
      <el-form v-show="chart.type && chart.type.includes('bar')" ref="sizeFormBar" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.adapt')" class="form-item">
          <el-checkbox v-model="sizeForm.barDefault" @change="changeBarSizeCase">{{ $t('chart.adapt') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.bar_width')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.barWidth" :disabled="sizeForm.barDefault" show-input :show-input-controls="false" input-size="mini" :min="1" :max="80" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item v-show="chart.type && chart.type !== 'bar-stack'" :label="$t('chart.bar_gap')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.barGap" :disabled="sizeForm.barDefault" show-input :show-input-controls="false" input-size="mini" :min="0" :max="5" :step="0.1" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('line')" ref="sizeFormLine" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.line_width')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.lineWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="10" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.line_type')" class="form-item">
          <el-radio-group v-model="sizeForm.lineType" @change="changeBarSizeCase">
            <el-radio-button label="solid">{{ $t('chart.line_type_solid') }}</el-radio-button>
            <el-radio-button label="dashed">{{ $t('chart.line_type_dashed') }}</el-radio-button>
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
        <!--        <el-form-item :label="$t('chart.line_area')" class="form-item">-->
        <!--          <el-checkbox v-model="sizeForm.lineArea" @change="changeBarSizeCase">{{ $t('chart.show') }}</el-checkbox>-->
        <!--        </el-form-item>-->
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('pie')" ref="sizeFormPie" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.pie_inner_radius')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.pieInnerRadius" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pie_outer_radius')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.pieOuterRadius" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
        </el-form-item>

        <span v-show="chart.type && chart.type.includes('pie-rose')">
          <el-form-item :label="$t('chart.rose_type')" class="form-item">
            <el-radio-group v-model="sizeForm.pieRoseType" size="mini" @change="changeBarSizeCase">
              <el-radio-button label="radius">{{ $t('chart.radius_mode') }}</el-radio-button>
              <el-radio-button label="area">{{ $t('chart.area_mode') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.rose_radius')" class="form-item form-item-slider">
            <el-slider v-model="sizeForm.pieRoseRadius" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
          </el-form-item>
        </span>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('funnel')" ref="sizeFormPie" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.funnel_width')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.funnelWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('radar')" ref="sizeFormPie" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.shape')" class="form-item">
          <el-radio-group v-model="sizeForm.radarShape" size="mini" @change="changeBarSizeCase">
            <el-radio-button label="polygon">{{ $t('chart.polygon') }}</el-radio-button>
            <el-radio-button label="circle">{{ $t('chart.circle') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('chart.radar_size')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.radarSize" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('table')" ref="sizeFormPie" :model="sizeForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.table_title_fontsize')" class="form-item">
          <el-select v-model="sizeForm.tableTitleFontSize" :placeholder="$t('chart.table_title_fontsize')" @change="changeBarSizeCase">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.table_item_fontsize')" class="form-item">
          <el-select v-model="sizeForm.tableItemFontSize" :placeholder="$t('chart.table_item_fontsize')" @change="changeBarSizeCase">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.table_title_height')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.tableTitleHeight" :min="36" :max="100" show-input :show-input-controls="false" input-size="mini" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.table_item_height')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.tableItemHeight" :min="36" :max="100" show-input :show-input-controls="false" input-size="mini" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item v-show="chart.type && chart.type === 'table-info'" :label="$t('chart.table_page_size')" class="form-item">
          <el-select v-model="sizeForm.tablePageSize" :placeholder="$t('chart.table_page_size')" @change="changeBarSizeCase">
            <el-option
              v-for="item in pageSizeOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('gauge')" ref="sizeFormGauge" :model="sizeForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.min')" class="form-item form-item-slider">
          <el-input-number v-model="sizeForm.gaugeMin" size="mini" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.max')" class="form-item form-item-slider">
          <el-input-number v-model="sizeForm.gaugeMax" size="mini" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.start_angle')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.gaugeStartAngle" show-input :show-input-controls="false" input-size="mini" :min="-360" :max="360" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.end_angle')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.gaugeEndAngle" show-input :show-input-controls="false" input-size="mini" :min="-360" :max="360" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && (chart.type.includes('text') || chart.type === 'label')" ref="sizeFormPie" :model="sizeForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.quota_font_size')" class="form-item">
          <el-select v-model="sizeForm.quotaFontSize" :placeholder="$t('chart.quota_font_size')" @change="changeBarSizeCase">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="chart.type" :label="$t('chart.dimension_show')" class="form-item">
          <el-checkbox v-model="sizeForm.dimensionShow" @change="changeBarSizeCase">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item v-show="chart.type" :label="$t('chart.dimension_font_size')" class="form-item">
          <el-select v-model="sizeForm.dimensionFontSize" :placeholder="$t('chart.dimension_font_size')" @change="changeBarSizeCase">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="chart.type" :label="$t('chart.space_split')" class="form-item">
          <el-input-number v-model="sizeForm.spaceSplit" :min="0" size="mini" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('scatter')" ref="sizeFormLine" :model="sizeForm" label-width="80px" size="mini">
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
      </el-form>

      <el-form v-show="chart.type && chart.type === 'treemap'" ref="sizeFormLine" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.width')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.treemapWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.height')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.treemapHeight" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>

      <el-form v-show="chart.type && chart.type === 'chart-mix'" ref="sizeFormBar" :model="sizeForm" label-width="80px" size="mini">
        <el-divider content-position="center" class="divider-style">{{ $t('chart.chart_bar') }}</el-divider>
        <el-form-item :label="$t('chart.adapt')" class="form-item">
          <el-checkbox v-model="sizeForm.barDefault" @change="changeBarSizeCase">{{ $t('chart.adapt') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.bar_width')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.barWidth" :disabled="sizeForm.barDefault" show-input :show-input-controls="false" input-size="mini" :min="1" :max="80" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.bar_gap')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.barGap" :disabled="sizeForm.barDefault" show-input :show-input-controls="false" input-size="mini" :min="0" :max="5" :step="0.1" @change="changeBarSizeCase" />
        </el-form-item>
        <el-divider content-position="center" class="divider-style">{{ $t('chart.chart_line') }}</el-divider>
        <el-form-item :label="$t('chart.line_width')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.lineWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :max="10" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.line_type')" class="form-item">
          <el-radio-group v-model="sizeForm.lineType" @change="changeBarSizeCase">
            <el-radio-button label="solid">{{ $t('chart.line_type_solid') }}</el-radio-button>
            <el-radio-button label="dashed">{{ $t('chart.line_type_dashed') }}</el-radio-button>
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
        <el-form-item :label="$t('chart.line_area')" class="form-item">
          <el-checkbox v-model="sizeForm.lineArea" @change="changeBarSizeCase">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-divider content-position="center" class="divider-style">{{ $t('chart.chart_scatter') }}</el-divider>
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
      </el-form>

      <el-form v-show="chart.type && chart.type === 'liquid'" ref="sizeFormLine" :model="sizeForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.liquid_shape')" class="form-item">
          <el-select v-model="sizeForm.liquidShape" :placeholder="$t('chart.liquid_shape')" @change="changeBarSizeCase">
            <el-option
              v-for="item in liquidShapeOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.liquid_max')" class="form-item form-item-slider">
          <el-input-number v-model="sizeForm.liquidMax" :min="1" size="mini" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.radar_size')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.liquidSize" show-input :show-input-controls="false" input-size="mini" :min="1" :max="100" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.liquid_outline_border')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.liquidOutlineBorder" show-input :show-input-controls="false" input-size="mini" :min="1" :max="20" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.liquid_outline_distance')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.liquidOutlineDistance" show-input :show-input-controls="false" input-size="mini" :min="0" :max="20" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.liquid_wave_length')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.liquidWaveLength" show-input :show-input-controls="false" input-size="mini" :min="10" :max="500" @change="changeBarSizeCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.liquid_wave_count')" class="form-item form-item-slider">
          <el-slider v-model="sizeForm.liquidWaveCount" show-input :show-input-controls="false" input-size="mini" :min="2" :max="10" @change="changeBarSizeCase" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_SIZE } from '../../chart/chart'
export default {
  name: 'SizeSelector',
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
        { name: this.$t('chart.line_symbol_none'), value: 'none' },
        { name: this.$t('chart.line_symbol_emptyCircle'), value: 'emptyCircle' },
        { name: this.$t('chart.line_symbol_circle'), value: 'circle' },
        { name: this.$t('chart.line_symbol_rect'), value: 'rect' },
        { name: this.$t('chart.line_symbol_roundRect'), value: 'roundRect' },
        { name: this.$t('chart.line_symbol_triangle'), value: 'triangle' },
        { name: this.$t('chart.line_symbol_diamond'), value: 'diamond' },
        { name: this.$t('chart.line_symbol_pin'), value: 'pin' },
        { name: this.$t('chart.line_symbol_arrow'), value: 'arrow' }
      ],
      liquidShapeOptions: [
        { name: this.$t('chart.liquid_shape_circle'), value: 'circle' },
        { name: this.$t('chart.liquid_shape_diamond'), value: 'diamond' },
        { name: this.$t('chart.liquid_shape_triangle'), value: 'triangle' },
        { name: this.$t('chart.liquid_shape_pin'), value: 'pin' },
        { name: this.$t('chart.liquid_shape_rect'), value: 'rect' }
      ],
      pageSizeOptions: [
        { name: '10' + this.$t('chart.table_page_size_unit'), value: '10' },
        { name: '20' + this.$t('chart.table_page_size_unit'), value: '20' },
        { name: '50' + this.$t('chart.table_page_size_unit'), value: '50' },
        { name: '100' + this.$t('chart.table_page_size_unit'), value: '100' }
      ],
      fontSize: []
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
    this.init()
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
          this.sizeForm.treemapWidth = this.sizeForm.treemapWidth ? this.sizeForm.treemapWidth : 80
          this.sizeForm.treemapHeight = this.sizeForm.treemapHeight ? this.sizeForm.treemapHeight : 80
          this.sizeForm.radarSize = this.sizeForm.radarSize ? this.sizeForm.radarSize : 80

          this.sizeForm.liquidShape = this.sizeForm.liquidShape ? this.sizeForm.liquidShape : DEFAULT_SIZE.liquidShape
          this.sizeForm.liquidMax = this.sizeForm.liquidMax ? this.sizeForm.liquidMax : DEFAULT_SIZE.liquidMax
          this.sizeForm.liquidSize = this.sizeForm.liquidSize ? this.sizeForm.liquidSize : DEFAULT_SIZE.liquidSize
          this.sizeForm.liquidOutlineBorder = this.sizeForm.liquidOutlineBorder ? this.sizeForm.liquidOutlineBorder : DEFAULT_SIZE.liquidOutlineBorder
          this.sizeForm.liquidOutlineDistance = (this.sizeForm.liquidOutlineDistance || this.sizeForm.liquidOutlineDistance === 0) ? this.sizeForm.liquidOutlineDistance : DEFAULT_SIZE.liquidOutlineDistance
          this.sizeForm.liquidWaveLength = this.sizeForm.liquidWaveLength ? this.sizeForm.liquidWaveLength : DEFAULT_SIZE.liquidWaveLength
          this.sizeForm.liquidWaveCount = this.sizeForm.liquidWaveCount ? this.sizeForm.liquidWaveCount : DEFAULT_SIZE.liquidWaveCount

          this.sizeForm.tablePageSize = this.sizeForm.tablePageSize ? this.sizeForm.tablePageSize : DEFAULT_SIZE.tablePageSize
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 60; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeBarSizeCase() {
      this.$emit('onSizeChange', this.sizeForm)
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
.el-divider--horizontal {
  margin: 10px 0
}
.divider-style>>>.el-divider__text{
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}
</style>
