<template>
  <div>
    <div style="width: 100%">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="tooltipForm" :model="tooltipForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.show')" class="form-item">
              <el-checkbox v-model="tooltipForm.show" @change="changeTooltipAttr">{{ $t('chart.show') }}</el-checkbox>
            </el-form-item>
            <el-form-item :label="$t('chart.trigger_position')" class="form-item">
              <el-radio-group v-model="tooltipForm.trigger" size="mini" @change="changeTooltipAttr">
                <el-radio-button label="item">{{ $t('chart.tooltip_item') }}</el-radio-button>
                <el-radio-button label="axis">{{ $t('chart.tooltip_axis') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
              <el-select v-model="tooltipForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTooltipAttr">
                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('chart.text_color')" class="form-item">
              <colorPicker v-model="tooltipForm.textStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeTooltipAttr" />
            </el-form-item>
            <el-form-item :label="$t('chart.content_formatter')" class="form-item">
              <el-input v-model="tooltipForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" :placeholder="$t('chart.formatter_plc')" @blur="changeTooltipAttr" />
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.tooltip') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_TOOLTIP } from '../../chart/chart'

export default {
  name: 'TooltipSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP)),
      fontSize: []
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        if (chart.customAttr) {
          const customAttr = JSON.parse(chart.customAttr)
          if (customAttr.tooltip) {
            this.tooltipForm = customAttr.tooltip
          }
        }
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
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
      this.$emit('onTooltipChange', this.tooltipForm)
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
</style>
