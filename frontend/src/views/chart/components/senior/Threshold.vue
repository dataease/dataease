<template>
  <div style="width: 100%">
    <el-col v-if="chart.type && chart.type === 'gauge'">
      <el-form ref="thresholdForm" :model="thresholdForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.threshold_range')+'(%)'" class="form-item">
          <span>0,</span>
          <el-input v-model="thresholdForm.gaugeThreshold" style="width: 100px;margin: 0 10px;" :placeholder="$t('chart.threshold_range')" size="mini" clearable @change="changeThreshold" />
          <span>,100</span>
          <el-tooltip class="item" effect="dark" placement="bottom">
            <div slot="content">
              阈值设置，决定仪表盘区间颜色，为空则不开启阈值，范围(0-100)，仅限整数，且逐级递增
              <br>
              例如：输入 30,70；表示：分为3段，分别为[0,30],(30,70],(70,100]
            </div>
            <i class="el-icon-info" style="cursor: pointer;margin-left: 10px;font-size: 12px;" />
          </el-tooltip>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_THRESHOLD } from '@/views/chart/chart/chart'

export default {
  name: 'Threshold',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      thresholdForm: JSON.parse(JSON.stringify(DEFAULT_THRESHOLD))
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
        if (senior.threshold) {
          this.thresholdForm = senior.threshold
        } else {
          this.thresholdForm = JSON.parse(JSON.stringify(DEFAULT_THRESHOLD))
        }
      }
    },
    changeThreshold() {
      // check input
      if (this.thresholdForm.gaugeThreshold) {
        const arr = this.thresholdForm.gaugeThreshold.split(',')
        for (let i = 0; i < arr.length; i++) {
          const ele = arr[i]
          if (ele.indexOf('.') > -1 || parseInt(ele).toString() === 'NaN' || parseInt(ele) < 1 || parseInt(ele) > 99) {
            this.$message({
              message: this.$t('chart.gauge_threshold_format_error'),
              type: 'error',
              showClose: true
            })
            return
          }
        }
      }
      this.$emit('onThresholdChange', this.thresholdForm)
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
