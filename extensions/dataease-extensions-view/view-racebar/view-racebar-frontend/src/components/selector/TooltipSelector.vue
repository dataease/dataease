<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="tooltipForm" :model="tooltipForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="tooltipForm.show" @change="changeTooltipAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="tooltipForm.show">

          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="tooltipForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini"
                       @change="changeTooltipAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="tooltipForm.textStyle.color" class="color-picker-style"
                             :predefine="predefineColors" @change="changeTooltipAttr"/>
          </el-form-item>
          <el-form-item
            :label="$t('chart.background')"
            class="form-item"
          >
            <el-color-picker
              v-model="tooltipForm.backgroundColor"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeTooltipAttr"
            />
          </el-form-item>
          <!--          <el-form-item class="form-item">
                      <span slot="label">
                        <span class="span-box">
                          <span>{{ $t('chart.content_formatter') }}</span>
                          <el-tooltip class="item" effect="dark" placement="bottom">
                            <div slot="content">
                              模板变量有 {a}, {b}，{c}，{d}，分别表示系列名，数据名，数据值等。
                              <br>
                              {a}（系列名称），{b}（数据项名称），{c}（数值）, {d}（百分比）
                            </div>
                            <i class="el-icon-info" style=" cursor: pointer;" />
                          </el-tooltip>
                        </span>
                      </span>
                      <el-input v-model="tooltipForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" :placeholder="$t('chart.formatter_plc')" @blur="changeTooltipAttr" />
                    </el-form-item>-->
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import {COLOR_PANEL, DEFAULT_TOOLTIP} from '../../utils/map'

export default {
  name: 'TooltipSelector',
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
      tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP)),
      fontSize: [],
      isSetting: false,
      predefineColors: COLOR_PANEL
    }
  },
  watch: {
    'chart': {
      handler: function () {
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
        if (customAttr.tooltip) {
          this.tooltipForm = customAttr.tooltip
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 20; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeTooltipAttr() {
      if (!this.tooltipForm.show) {
        this.isSetting = false
      }
      this.$emit('onTooltipChange', this.tooltipForm)
    }
  }
}
</script>

<style scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider >>> .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item >>> .el-form-item__label {
  font-size: 12px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}
</style>
