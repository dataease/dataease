<template>
  <div style="width: 100%;">
    <el-col>
      <el-form v-show="chart.type && !chart.type.includes('gauge')" ref="labelForm" :model="labelForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.show">
          <!--          <el-form-item v-show="chart.type && chart.type.includes('pie')" :label="$t('chart.pie_label_line_show')" class="form-item">-->
          <!--            <el-checkbox v-model="labelForm.labelLine.show" @change="changeLabelAttr">{{ $t('chart.pie_label_line_show') }}</el-checkbox>-->
          <!--          </el-form-item>-->
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeLabelAttr" />
          </el-form-item>
          <el-form-item v-show="chart.type && chart.type !== 'liquid' && chart.type !== 'pie-rose' && !chart.type.includes('line') && chart.type !== 'treemap' && !chart.type.includes('scatter')" :label="$t('chart.label_position')" class="form-item">
            <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr">
              <el-option v-for="option in labelPosition" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('gauge')" ref="labelForm" :model="labelForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
          <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.text_color')" class="form-item">
          <el-color-picker v-model="labelForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeLabelAttr" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_LABEL } from '../../chart/chart'

export default {
  name: 'LabelSelectorAntV',
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
      labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      fontSize: [],
      isSetting: false,
      labelPosition: [],
      labelPositionPie: [
        { name: this.$t('chart.inside'), value: 'inner' },
        { name: this.$t('chart.outside'), value: 'outer' }
      ],
      labelPositionH: [
        { name: this.$t('chart.text_pos_left'), value: 'left' },
        { name: this.$t('chart.center'), value: 'middle' },
        { name: this.$t('chart.text_pos_right'), value: 'right' }
      ],
      labelPositionV: [
        { name: this.$t('chart.text_pos_top'), value: 'top' },
        { name: this.$t('chart.center'), value: 'middle' },
        { name: this.$t('chart.text_pos_bottom'), value: 'bottom' }
      ],
      predefineColors: COLOR_PANEL
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initOptions()
        this.initData()
      }
    }
  },
  mounted() {
    this.init()
    this.initOptions()
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
        if (customAttr.label) {
          this.labelForm = customAttr.label
          if (!this.labelForm.labelLine) {
            this.labelForm.labelLine = JSON.parse(JSON.stringify(DEFAULT_LABEL.labelLine))
          }
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeLabelAttr() {
      if (!this.labelForm.show) {
        this.isSetting = false
      }
      this.$emit('onLabelChange', this.labelForm)
    },
    initOptions() {
      const type = this.chart.type
      if (type) {
        if (type.includes('horizontal') || type === 'funnel') {
          this.labelPosition = this.labelPositionH
        } else if (type.includes('pie')) {
          this.labelPosition = this.labelPositionPie
        } else {
          this.labelPosition = this.labelPositionV
        }
      }
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
