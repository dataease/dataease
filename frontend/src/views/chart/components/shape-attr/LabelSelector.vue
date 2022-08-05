<template>
  <div style="width: 100%;">
    <el-col>
      <el-form ref="labelForm" :model="labelForm" label-width="80px" size="mini">
        <el-form-item v-show="showProperty('show')" :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr('show')">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.show">
          <el-form-item v-show="showProperty('labelLine')" :label="$t('chart.pie_label_line_show')" class="form-item">
            <el-checkbox v-model="labelForm.labelLine.show" @change="changeLabelAttr('labelLine')">{{ $t('chart.pie_label_line_show') }}</el-checkbox>
          </el-form-item>
          <el-form-item v-show="showProperty('fontSize')" :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr('fontSize')">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-show="showProperty('color')" :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeLabelAttr('color')" />
          </el-form-item>
          <el-form-item v-show="showProperty('position-pie') " :label="$t('chart.label_position')" class="form-item">
            <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr('position')">
              <el-option v-for="option in labelPositionPie" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-show="showProperty('position-h') " :label="$t('chart.label_position')" class="form-item">
            <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr('position')">
              <el-option v-for="option in labelPositionH" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-show="showProperty('position-v') " :label="$t('chart.label_position')" class="form-item">
            <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr('position')">
              <el-option v-for="option in labelPositionV" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-show="showProperty('label-bg') " :label="$t('chart.label_bg')" class="form-item">
            <el-color-picker v-model="labelForm.bgColor" class="color-picker-style" :predefine="predefineColors" @change="changeLabelAttr('bgColor')" />
          </el-form-item>
          <el-form-item v-show="showProperty('label-show-shadow')" :label="$t('chart.label_shadow')" class="form-item">
            <el-checkbox v-model="labelForm.showShadow" @change="changeLabelAttr('showShadow')">{{ $t('chart.show') }}</el-checkbox>
          </el-form-item>
          <el-form-item v-show="showProperty('label-shadow-color') && labelForm.showShadow" :label="$t('chart.label_shadow_color')" class="form-item">
            <el-color-picker v-model="labelForm.shadowColor" class="color-picker-style" :predefine="predefineColors" @change="changeLabelAttr('shadowColor')" />
          </el-form-item>

          <el-form-item v-show="showProperty('formatter')" class="form-item">
            <span slot="label">
              <span class="span-box">
                <span>{{ $t('chart.content_formatter') }}</span>
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    字符串模板 模板变量有：<br>{a}：系列名。<br>{b}：数据名。<br>{c}：数据值。<br>{d}：百分比（用于饼图等）。
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </span>
            <el-input v-model="labelForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr('formatter')" />
          </el-form-item>
        </div>
        <el-form-item v-show="showProperty('gaugeFormatter')" class="form-item">
          <span slot="label">
            <span class="span-box">
              <span>{{ $t('chart.content_formatter') }}</span>
            </span>
          </span>
          <el-input v-model="labelForm.gaugeFormatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr('gaugeFormatter')" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_LABEL } from '../../chart/chart'

export default {
  name: 'LabelSelector',
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
      labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      fontSize: [],
      isSetting: false,
      labelPosition: [],
      labelPositionPie: [
        { name: this.$t('chart.inside'), value: 'inside' },
        { name: this.$t('chart.outside'), value: 'outside' }
      ],
      labelPositionH: [
        { name: this.$t('chart.text_pos_left'), value: 'left' },
        { name: this.$t('chart.center'), value: 'inside' },
        { name: this.$t('chart.text_pos_right'), value: 'right' }
      ],
      labelPositionV: [
        { name: this.$t('chart.text_pos_top'), value: 'top' },
        { name: this.$t('chart.center'), value: 'inside' },
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
    if (this.showProperty('position-pie')) {
      this.labelForm.position = 'outside'
    }
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
    changeLabelAttr(modifyName) {
      if (!this.labelForm.show) {
        this.isSetting = false
      }
      this.labelForm['modifyName'] = modifyName
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
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
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
