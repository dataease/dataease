<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="axisForm" :model="axisForm" label-width="90px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="axisForm.show" @change="changeXAxisStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="axisForm.show">
          <!-- <el-form-item :label="$t('chart.position')" class="form-item">
            <el-radio-group v-model="axisForm.position" size="mini" @change="changeXAxisStyle">
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item> -->
          <el-form-item :label="$t('chart.name')" class="form-item">
            <el-input v-model="axisForm.name" size="mini" @blur="changeXAxisStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.axis_name_color')" class="form-item">
            <el-color-picker v-model="axisForm.nameTextStyle.color" class="color-picker-style" :predefine="predefineColors" @change="changeXAxisStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.axis_name_location')" class="form-item">
            <el-radio-group v-model="axisForm.align" size="mini" @change="changeXAxisStyle">
              <el-radio-button label="low">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="middle">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="high">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.dimension_font_size')" class="form-item">
            <el-select v-model="axisForm.nameTextStyle.fontSize" :placeholder="$t('chart.dimension_font_size')" @change="changeXAxisStyle">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-divider />
          <el-form-item :label="$t('chart.axis_color')" class="form-item">
            <el-color-picker v-model="axisForm.splitLine.lineStyle.color" class="el-color-picker" :predefine="predefineColors" @change="changeXAxisStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.axis_width')" class="form-item form-item-slider">
            <el-slider v-model="axisForm.splitLine.lineStyle.width" :min="0" :max="10" show-input :show-input-controls="false" input-size="mini" @change="changeXAxisStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.axis_type')" class="form-item">
            <el-radio-group v-model="axisForm.splitLine.lineStyle.type" size="mini" @change="changeXAxisStyle">
              <el-radio-button label="solid">{{ $t('chart.axis_type_solid') }}</el-radio-button>
              <el-radio-button label="shortdash">{{ $t('chart.axis_type_dashed') }}</el-radio-button>
              <el-radio-button label="dot">{{ $t('chart.axis_type_dotted') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-divider />
          <el-form-item :label="$t('chart.axis_label_show')" class="form-item">
            <el-checkbox v-model="axisForm.axisLabel.show" @change="changeXAxisStyle">{{ $t('chart.axis_label_show') }}</el-checkbox>
          </el-form-item>
          <span v-show="axisForm.axisLabel.show">
            <el-form-item :label="$t('chart.axis_label_color')" class="form-item">
              <el-color-picker v-model="axisForm.axisLabel.color" class="el-color-picker" :predefine="predefineColors" @change="changeXAxisStyle" />
            </el-form-item>
            <!-- <el-form-item :label="$t('chart.axis_label_rotate')" class="form-item form-item-slider">
              <el-slider v-model="axisForm.axisLabel.rotate" show-input :show-input-controls="false" :step="10" :min="-90" :max="90" input-size="mini" @change="changeXAxisStyle" />
            </el-form-item> -->
            <el-form-item :label="$t('chart.axis_label_fontsize')" class="form-item">
              <el-select v-model="axisForm.axisLabel.fontSize" :placeholder="$t('chart.axis_label_fontsize')" @change="changeXAxisStyle">
                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
              </el-select>
            </el-form-item>
          </span>
          <el-divider />
          <!-- <el-form-item :label="$t('chart.content_formatter')" class="form-item">
            <el-input v-model="axisForm.axisLabel.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeXAxisStyle" />
          </el-form-item> -->
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_XAXIS_STYLE_HC } from '../../chart/chart'

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
    }
  },
  data() {
    return {
      axisForm: JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE_HC)),
      isSetting: false,
      fontSize: [],
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
            this.axisForm.splitLine = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE_HC.splitLine))
          }
          if (!this.axisForm.nameTextStyle) {
            this.axisForm.nameTextStyle = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE_HC.nameTextStyle))
          }
          if (!this.axisForm.axisValue) {
            this.axisForm.axisValue = JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE_HC.axisValue))
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
    changeXAxisStyle() {
      if (!this.axisForm.show) {
        this.isSetting = false
      }
      this.$emit('onChangeXAxisForm', this.axisForm)
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
