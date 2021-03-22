<template>
  <div>
    <div style="width:100%;height: 32px;margin:0;padding:0 4px;border-radius: 4px;border: 1px solid #DCDFE6;display: flex;align-items: center;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="legendForm" :model="legendForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.show')" class="form-item">
              <el-checkbox v-model="legendForm.show" @change="changeLegendStyle">{{ $t('chart.show') }}</el-checkbox>
            </el-form-item>
            <el-form-item :label="$t('chart.orient')" class="form-item">
              <el-radio-group v-model="legendForm.orient" size="mini" @change="changeLegendStyle">
                <el-radio-button label="horizontal">{{ $t('chart.horizontal') }}</el-radio-button>
                <el-radio-button label="vertical">{{ $t('chart.vertical') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('chart.text_h_position')" class="form-item">
              <el-radio-group v-model="legendForm.hPosition" size="mini" @change="changeLegendStyle">
                <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
                <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
                <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('chart.text_v_position')" class="form-item">
              <el-radio-group v-model="legendForm.vPosition" size="mini" @change="changeLegendStyle">
                <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
                <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
                <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.legend') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_LEGEND_STYLE } from '../../chart/chart'

export default {
  name: 'LegendSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      legendForm: JSON.parse(JSON.stringify(DEFAULT_LEGEND_STYLE))
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        if (chart.customStyle) {
          const customStyle = JSON.parse(chart.customStyle)
          if (customStyle.legend) {
            this.legendForm = customStyle.legend
          }
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    changeLegendStyle() {
      this.$emit('onLegendChange', this.legendForm)
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
