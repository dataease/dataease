<template>
  <div>
    <div style="width:100%;height: 32px;margin:0;padding:0 4px;border-radius: 4px;border: 1px solid #DCDFE6;display: flex;align-items: center;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="axisForm" :model="axisForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.show')" class="form-item">
              <el-checkbox v-model="axisForm.show" @change="changeYAxisStyle">{{ $t('chart.show') }}</el-checkbox>
            </el-form-item>
            <el-form-item :label="$t('chart.position')" class="form-item">
              <el-radio-group v-model="axisForm.position" size="mini" @change="changeYAxisStyle">
                <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
                <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('chart.name')" class="form-item">
              <el-input v-model="axisForm.name" size="mini" @blur="changeYAxisStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.rotate')" class="form-item form-item-slider">
              <el-slider v-model="axisForm.axisLabel.rotate" show-input :show-input-controls="false" :min="-90" :max="90" input-size="mini" @change="changeYAxisStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.content_formatter')" class="form-item">
              <el-input v-model="axisForm.axisLabel.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeYAxisStyle"/>
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.yAxis') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_YAXIS_STYLE } from '../../chart/chart'

export default {
  name: 'YAxisSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      axisForm: JSON.parse(JSON.stringify(DEFAULT_YAXIS_STYLE))
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        if (chart.customStyle) {
          const customStyle = JSON.parse(chart.customStyle)
          if (customStyle.yAxis) {
            this.axisForm = customStyle.yAxis
          }
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    changeYAxisStyle() {
      this.$emit('onChangeYAxisForm', this.axisForm)
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
  span{
    font-size: 12px
  }
  .el-form-item{
    margin-bottom: 6px;
  }
</style>
