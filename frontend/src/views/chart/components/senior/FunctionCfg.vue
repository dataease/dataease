<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="functionForm" :model="functionForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.slider')" class="form-item">
          <el-checkbox v-model="functionForm.sliderShow" @change="changeFunctionCfg">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item v-show="functionForm.sliderShow" :label="$t('chart.slider_range')+'(%)'" class="form-item form-item-slider">
          <el-slider v-model="functionForm.sliderRange" style="width: 90%" :min="0" :max="100" input-size="mini" range @change="changeFunctionCfg" />
        </el-form-item>
        <el-form-item v-show="functionForm.sliderShow" :label="$t('chart.slider_bg')" class="form-item">
          <el-color-picker v-model="functionForm.sliderBg" class="color-picker-style" :predefine="predefineColors" @change="changeFunctionCfg" />
        </el-form-item>
        <el-form-item v-show="functionForm.sliderShow" :label="$t('chart.slider_fill_bg')" class="form-item">
          <el-color-picker v-model="functionForm.sliderFillBg" class="color-picker-style" :predefine="predefineColors" @change="changeFunctionCfg" />
        </el-form-item>
        <el-form-item v-show="functionForm.sliderShow" :label="$t('chart.slider_text_color')" class="form-item">
          <el-color-picker v-model="functionForm.sliderTextClolor" class="color-picker-style" :predefine="predefineColors" @change="changeFunctionCfg" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_FUNCTION_CFG, COLOR_PANEL } from '../../chart/chart'

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
      predefineColors: COLOR_PANEL
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
          this.functionForm = senior.functionCfg
        } else {
          this.functionForm = JSON.parse(JSON.stringify(DEFAULT_FUNCTION_CFG))
        }
      }
    },
    changeFunctionCfg() {
      this.$emit('onFunctionCfgChange', this.functionForm)
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

.switch-style{
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}
</style>
