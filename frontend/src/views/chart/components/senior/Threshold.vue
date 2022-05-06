<template>
  <div style="width: 100%">
    <!--仪表盘-->
    <el-col v-if="chart.type && chart.type === 'gauge'">
      <el-form ref="thresholdForm" :model="thresholdForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.threshold_range')+'(%)'" class="form-item">
          <span>0,</span>
          <el-input v-model="thresholdForm.gaugeThreshold" style="width: 100px;margin: 0 10px;" :placeholder="$t('chart.threshold_range')" size="mini" clearable @change="gaugeThresholdChange" />
          <span>,100</span>
          <el-tooltip class="item" effect="dark" placement="bottom">
            <div slot="content">
              阈值设置，决定仪表盘区间颜色，为空则不开启阈值，范围(0-100)，逐级递增
              <br>
              例如：输入 30,70；表示：分为3段，分别为[0,30],(30,70],(70,100]
            </div>
            <i class="el-icon-info" style="cursor: pointer;margin-left: 10px;font-size: 12px;" />
          </el-tooltip>
        </el-form-item>
      </el-form>
    </el-col>

    <!--指标卡-->
    <el-col v-if="chart.type && chart.type === 'text'">
      <el-col>
        <el-button
          :title="$t('chart.edit')"
          icon="el-icon-edit"
          type="text"
          size="small"
          style="width: 24px;margin-left: 4px;"
          @click="editLabelThreshold"
        />
        <el-col style="padding: 0 18px;">
          <el-row v-for="(item,index) in thresholdForm.labelThreshold" :key="index" class="line-style">
            <el-col :span="8">
              <span v-if="item.term === 'eq'" :title="$t('chart.filter_eq')">{{ $t('chart.filter_eq') }}</span>
              <span v-else-if="item.term === 'not_eq'" :title="$t('chart.filter_not_eq')">{{ $t('chart.filter_not_eq') }}</span>
              <span v-else-if="item.term === 'lt'" :title="$t('chart.filter_lt')">{{ $t('chart.filter_lt') }}</span>
              <span v-else-if="item.term === 'gt'" :title="$t('chart.filter_gt')">{{ $t('chart.filter_gt') }}</span>
              <span v-else-if="item.term === 'le'" :title="$t('chart.filter_le')">{{ $t('chart.filter_le') }}</span>
              <span v-else-if="item.term === 'ge'" :title="$t('chart.filter_ge')">{{ $t('chart.filter_ge') }}</span>
            </el-col>
            <el-col :span="8">
              <span :title="item.value">{{ item.value }}</span>
            </el-col>
            <el-col :span="8">
              <span :style="{width:'14px', height:'14px', backgroundColor: item.color}" />
            </el-col>
          </el-row>
        </el-col>
      </el-col>
    </el-col>

    <!--编辑阈值-->
    <el-dialog
      v-if="editLabelThresholdDialog"
      v-dialogDrag
      :title="$t('chart.threshold')"
      :visible="editLabelThresholdDialog"
      :show-close="false"
      width="50%"
      class="dialog-css"
    >
      <text-threshold-edit :threshold="thresholdForm.labelThreshold" @onLabelThresholdChange="thresholdChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeLabelThreshold">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="changeLabelThreshold">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { DEFAULT_THRESHOLD } from '@/views/chart/chart/chart'
import TextThresholdEdit from '@/views/chart/components/senior/dialog/TextThresholdEdit'

export default {
  name: 'Threshold',
  components: { TextThresholdEdit },
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      thresholdForm: JSON.parse(JSON.stringify(DEFAULT_THRESHOLD)),
      editLabelThresholdDialog: false,
      thresholdArr: []
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
          if (!this.thresholdForm.labelThreshold) {
            this.thresholdForm.labelThreshold = []
          }
        } else {
          this.thresholdForm = JSON.parse(JSON.stringify(DEFAULT_THRESHOLD))
        }
      }
    },
    changeThreshold() {
      this.$emit('onThresholdChange', this.thresholdForm)
    },
    gaugeThresholdChange() {
      // check input
      if (this.thresholdForm.gaugeThreshold) {
        const arr = this.thresholdForm.gaugeThreshold.split(',')
        for (let i = 0; i < arr.length; i++) {
          const ele = arr[i]
          if (parseFloat(ele).toString() === 'NaN' || parseFloat(ele) < 1 || parseFloat(ele) > 99) {
            this.$message({
              message: this.$t('chart.gauge_threshold_format_error'),
              type: 'error',
              showClose: true
            })
            return
          }
          if (i > 0) {
            if (parseFloat(ele) <= parseFloat(arr[i - 1])) {
              this.$message({
                message: this.$t('chart.gauge_threshold_compare_error'),
                type: 'error',
                showClose: true
              })
              return
            }
          }
        }
      }
      this.changeThreshold()
    },
    editLabelThreshold() {
      this.editLabelThresholdDialog = true
    },
    closeLabelThreshold() {
      this.editLabelThresholdDialog = false
    },
    changeLabelThreshold() {
      // check line config
      for (let i = 0; i < this.thresholdArr.length; i++) {
        const ele = this.thresholdArr[i]
        if (!ele.term || ele.term === '') {
          this.$message({
            message: this.$t('chart.exp_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (!ele.value) {
          this.$message({
            message: this.$t('chart.value_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (parseFloat(ele.value).toString() === 'NaN') {
          this.$message({
            message: this.$t('chart.value_error'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.thresholdForm.labelThreshold = JSON.parse(JSON.stringify(this.thresholdArr))
      this.changeThreshold()
      this.closeLabelThreshold()
    },
    thresholdChange(val) {
      this.thresholdArr = val
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

.line-style >>> span{
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin: 0 10px;
}

.dialog-css >>> .el-dialog__title {
  font-size: 14px;
}

.dialog-css >>> .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css >>> .el-dialog__body {
  padding: 10px 20px 20px;
}
</style>
