<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="axisForm" :model="axisForm" label-width="80px" size="mini">
        <el-form-item v-show="showProperty('show')" :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="axisForm.show" @change="changeXAxisStyle('show')">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="axisForm.show">
          <el-form-item v-show="showProperty('position')" :label="$t('chart.position')" class="form-item">
            <el-radio-group v-model="axisForm.position" size="mini" @change="changeXAxisStyle('position')">
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-show="showProperty('name')" :label="$t('chart.name')" class="form-item">
            <el-input v-model="axisForm.name" size="mini" @blur="changeXAxisStyle('name')" />
          </el-form-item>
          <el-form-item v-show="showProperty('nameTextStyle')" :label="$t('chart.axis_name_color')" class="form-item">
            <el-color-picker v-model="axisForm.nameTextStyle.color" class="color-picker-style" :predefine="predefineColors" @change="changeXAxisStyle('nameTextStyle')" />
          </el-form-item>
          <el-form-item v-show="showProperty('nameTextStyle')" :label="$t('chart.axis_name_fontsize')" class="form-item">
            <el-select v-model="axisForm.nameTextStyle.fontSize" :placeholder="$t('chart.axis_name_fontsize')" @change="changeXAxisStyle('nameTextStyle')">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <span v-show="showProperty('axisValue')">
            <el-divider />
            <el-form-item class="form-item">
              <span slot="label">
                <span class="span-box">
                  <span>{{ $t('chart.axis_value') }}</span>
                  <el-tooltip class="item" effect="dark" placement="bottom">
                    <div slot="content">
                      最小值、最大值、间隔均为数值类型；若不填，则该项视为自动。
                      <br>
                      请确保填写数值能正确计算，否则将无法正常显示轴值。
                    </div>
                    <i class="el-icon-info" style="cursor: pointer;" />
                  </el-tooltip>
                </span>
              </span>
              <el-checkbox v-model="axisForm.axisValue.auto" @change="changeXAxisStyle('axisValue')">{{ $t('chart.axis_auto') }}</el-checkbox>
            </el-form-item>
            <span v-show="!axisForm.axisValue.auto">
              <el-form-item :label="$t('chart.axis_value_min')" class="form-item">
                <el-input v-model="axisForm.axisValue.min" @blur="changeXAxisStyle('axisValue')" />
              </el-form-item>
              <el-form-item :label="$t('chart.axis_value_max')" class="form-item">
                <el-input v-model="axisForm.axisValue.max" @blur="changeXAxisStyle('axisValue')" />
              </el-form-item>
              <el-form-item :label="$t('chart.axis_value_split')" class="form-item">
                <span slot="label">
                  <span class="span-box">
                    <span>{{ $t('chart.axis_value_split_space') }}</span>
                    <el-tooltip class="item" effect="dark" placement="bottom">
                      <div slot="content">
                        间隔表示两个刻度之间的单位长度。
                      </div>
                      <i class="el-icon-info" style="cursor: pointer;" />
                    </el-tooltip>
                  </span>
                </span>
                <el-input v-model="axisForm.axisValue.split" @blur="changeXAxisStyle('axisValue')" />
              </el-form-item>
            </span>
          </span>
          <el-divider v-if="showProperty('splitLine')" />
          <el-form-item v-show="showProperty('splitLine')" :label="$t('chart.axis_show')" class="form-item">
            <el-checkbox v-model="axisForm.splitLine.show" @change="changeXAxisStyle('splitLine')">{{ $t('chart.axis_show') }}</el-checkbox>
          </el-form-item>
          <span v-show="showProperty('splitLine') && axisForm.splitLine.show">
            <el-form-item :label="$t('chart.axis_color')" class="form-item">
              <el-color-picker v-model="axisForm.splitLine.lineStyle.color" class="el-color-picker" :predefine="predefineColors" @change="changeXAxisStyle('splitLine')" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_width')" class="form-item form-item-slider">
              <el-slider v-model="axisForm.splitLine.lineStyle.width" :min="1" :max="10" show-input :show-input-controls="false" input-size="mini" @change="changeXAxisStyle('splitLine')" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_type')" class="form-item">
              <el-radio-group v-model="axisForm.splitLine.lineStyle.type" size="mini" @change="changeXAxisStyle('splitLine')">
                <el-radio-button label="solid">{{ $t('chart.axis_type_solid') }}</el-radio-button>
                <el-radio-button label="dashed">{{ $t('chart.axis_type_dashed') }}</el-radio-button>
                <el-radio-button label="dotted">{{ $t('chart.axis_type_dotted') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </span>
          <el-divider v-if="showProperty('axisLabel')" />
          <el-form-item v-show="showProperty('axisLabel')" :label="$t('chart.axis_label_show')" class="form-item">
            <el-checkbox v-model="axisForm.axisLabel.show" @change="changeXAxisStyle('axisLabel')">{{ $t('chart.axis_label_show') }}</el-checkbox>
          </el-form-item>
          <span v-show="showProperty('axisLabel') && axisForm.axisLabel.show">
            <el-form-item :label="$t('chart.axis_label_color')" class="form-item">
              <el-color-picker v-model="axisForm.axisLabel.color" class="el-color-picker" :predefine="predefineColors" @change="changeXAxisStyle('axisLabel')" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_label_rotate')" class="form-item form-item-slider">
              <el-slider v-model="axisForm.axisLabel.rotate" show-input :show-input-controls="false" :min="-90" :max="90" input-size="mini" @change="changeXAxisStyle('axisLabel')" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_label_fontsize')" class="form-item">
              <el-select v-model="axisForm.axisLabel.fontSize" :placeholder="$t('chart.axis_label_fontsize')" @change="changeXAxisStyle('axisLabel')">
                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
              </el-select>
            </el-form-item>

            <span v-show="chart.type && chart.type.includes('horizontal')">
              <el-form-item :label="$t('chart.value_formatter_type')" class="form-item">
                <el-select v-model="axisForm.axisLabelFormatter.type" @change="changeXAxisStyle('axisLabelFormatter')">
                  <el-option v-for="type in typeList" :key="type.value" :label="$t('chart.' + type.name)" :value="type.value" />
                </el-select>
              </el-form-item>

              <el-form-item v-show="axisForm.axisLabelFormatter.type !== 'auto'" :label="$t('chart.value_formatter_decimal_count')" class="form-item">
                <el-input-number v-model="axisForm.axisLabelFormatter.decimalCount" :precision="0" :min="0" :max="10" size="mini" @change="changeXAxisStyle('axisLabelFormatter')" />
              </el-form-item>

              <el-form-item v-show="axisForm.axisLabelFormatter.type !== 'percent'" :label="$t('chart.value_formatter_unit')" class="form-item">
                <el-select v-model="axisForm.axisLabelFormatter.unit" :placeholder="$t('chart.pls_select_field')" size="mini" @change="changeXAxisStyle('axisLabelFormatter')">
                  <el-option v-for="item in unitList" :key="item.value" :label="$t('chart.' + item.name)" :value="item.value" />
                </el-select>
              </el-form-item>

              <el-form-item :label="$t('chart.value_formatter_suffix')" class="form-item">
                <el-input v-model="axisForm.axisLabelFormatter.suffix" size="mini" clearable :placeholder="$t('commons.input_content')" @change="changeXAxisStyle('axisLabelFormatter')" />
              </el-form-item>

              <el-form-item :label="$t('chart.value_formatter_thousand_separator')" class="form-item">
                <el-checkbox v-model="axisForm.axisLabelFormatter.thousandSeparator" @change="changeXAxisStyle('axisLabelFormatter')" />
              </el-form-item>
            </span>
          </span>
          <el-divider v-if="showProperty('axisLabel')" />
          <el-form-item v-show="showProperty('axisLabel')" :label="$t('chart.content_formatter')" class="form-item">
            <el-input v-model="axisForm.axisLabel.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeXAxisStyle('axisLabel')" />
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_XAXIS_STYLE } from '../../chart/chart'
import { formatterType, unitList } from '@/views/chart/chart/formatter'

export default {
  name: 'XAxisSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      axisForm: JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE)),
      isSetting: false,
      fontSize: [],
      predefineColors: COLOR_PANEL,
      typeList: formatterType,
      unitList: unitList
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
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.xAxis) {
          this.axisForm = customStyle.xAxis
          if (!this.axisForm.splitLine) {
            this.axisForm.splitLine = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE.splitLine))
          }
          if (!this.axisForm.nameTextStyle) {
            this.axisForm.nameTextStyle = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE.nameTextStyle))
          }
          if (!this.axisForm.axisValue) {
            this.axisForm.axisValue = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE.axisValue))
          }
          if (!this.axisForm.axisLabelFormatter) {
            this.axisForm.axisLabelFormatter = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE.axisLabelFormatter))
          }
        }
      }
    },
    init() {
      const arr = []
      for (let i = 6; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeXAxisStyle(modifyName) {
      if (!this.axisForm.show) {
        this.isSetting = false
      }
      this.axisForm['modifyName'] = modifyName
      this.$emit('onChangeXAxisForm', this.axisForm)
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 10px 0
  }
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
