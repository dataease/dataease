<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="functionForm"
        :model="functionForm"
        label-width="80px"
        size="mini"
      >
        <div v-if="showSlider">
          <el-form-item
            :label="$t('chart.slider')"
            class="form-item"
          >
            <el-checkbox
              v-model="functionForm.sliderShow"
              @change="changeFunctionCfg"
            >{{ $t('chart.show') }}</el-checkbox>
          </el-form-item>
          <el-form-item
            v-show="functionForm.sliderShow"
            :label="$t('chart.slider_range')+'(%)'"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="functionForm.sliderRange"
              style="width: 90%"
              :min="0"
              :max="100"
              input-size="mini"
              range
              @change="changeFunctionCfg"
            />
          </el-form-item>
          <el-form-item
            v-show="functionForm.sliderShow"
            :label="$t('chart.slider_bg')"
            class="form-item"
          >
            <el-color-picker
              v-model="functionForm.sliderBg"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeFunctionCfg"
            />
          </el-form-item>
          <el-form-item
            v-show="functionForm.sliderShow"
            :label="$t('chart.slider_fill_bg')"
            class="form-item"
          >
            <el-color-picker
              v-model="functionForm.sliderFillBg"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeFunctionCfg"
            />
          </el-form-item>
          <el-form-item
            v-show="functionForm.sliderShow"
            :label="$t('chart.slider_text_color')"
            class="form-item"
          >
            <el-color-picker
              v-model="functionForm.sliderTextClolor"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeFunctionCfg"
            />
          </el-form-item>
        </div>
        <el-form-item
          v-show="showEmptyStrategy"
          :label="$t('chart.empty_data_strategy')"
          class="form-item"
        >
          <el-radio-group
            v-model="functionForm.emptyDataStrategy"
            @change="changeFunctionCfg"
          >
            <el-radio :label="'breakLine'">{{ $t('chart.break_line') }}</el-radio>
            <el-radio :label="'setZero'">{{ $t('chart.set_zero') }}</el-radio>
            <el-radio
              v-show="showIgnoreOption"
              :label="'ignoreData'"
            >
              {{ $t('chart.ignore_data') }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-show="showEmptyDataFieldCtrl"
          :label="$t('chart.empty_data_field_ctrl')"
          class="form-item"
        >
          <el-select
            v-model="functionForm.emptyDataFieldCtrl"
            multiple
            @change="changeFunctionCfg"
          >
            <el-option
              v-for="option in fieldOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_FUNCTION_CFG, COLOR_PANEL } from '../../chart/chart'
import { equalsAny, includesAny } from '@/utils/StringUtils'

export default {
  name: 'FunctionCfg',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      functionForm: JSON.parse(JSON.stringify(DEFAULT_FUNCTION_CFG)),
      predefineColors: COLOR_PANEL,
      fieldOptions: []
    }
  },
  computed: {
    showSlider() {
      return this.chart.type !== 'bidirectional-bar' &&
        !equalsAny(this.chart.type, 'map') &&
        !includesAny(this.chart.type, 'table')
    },
    showEmptyStrategy() {
      return (this.chart.render === 'antv' &&
        includesAny(this.chart.type, 'line', 'bar', 'area', 'table')) ||
        (this.chart.render === 'echarts' && equalsAny(this.chart.type, 'map'))
    },
    showIgnoreOption() {
      return !equalsAny(this.chart.type, 'map', 'table-pivot', 'table-info')
    },
    showEmptyDataFieldCtrl() {
      return this.showEmptyStrategy &&
        includesAny(this.chart.type, 'table') &&
        this.functionForm.emptyDataStrategy !== 'breakLine'
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
      if (chart.senior) {
        let senior = null
        if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
          senior = JSON.parse(JSON.stringify(chart.senior))
        } else {
          senior = JSON.parse(chart.senior)
        }
        if (senior.functionCfg) {
          this.functionForm = { ...DEFAULT_FUNCTION_CFG, ...senior.functionCfg }
        } else {
          this.functionForm = JSON.parse(JSON.stringify(DEFAULT_FUNCTION_CFG))
        }
        this.initFieldCtrl()
      }
    },
    initFieldCtrl() {
      if (this.showEmptyDataFieldCtrl) {
        this.fieldOptions = []
        let axis
        if (equalsAny(this.chart.type, 'table-normal', 'table-pivot')) {
          axis = this.chart.yaxis
        }
        if (this.chart.type === 'table-info') {
          axis = this.chart.xaxis
        }
        let axisArr = []
        if (Object.prototype.toString.call(axis) === '[object Array]') {
          axisArr = axisArr.concat(axis)
        } else {
          axisArr = axisArr.concat(JSON.parse(axis))
        }
        axisArr.forEach(item => {
          if (item.groupType === 'q') {
            this.fieldOptions.push({
              label: item.name,
              value: item.dataeaseName
            })
          }
        })
      }
    },
    changeFunctionCfg() {
      this.$emit('onFunctionCfgChange', this.functionForm)
    }
  }
}
</script>

<style scoped lang="scss">
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

.switch-style{
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}
.form-item ::v-deep .el-radio-group{
  display: flex;
  flex-direction: column;
  flex-wrap: wrap;
  label {
    line-height: 28px;
  }
}
</style>
