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
            <el-col :span="6">
              <span v-if="item.term === 'eq'" :title="$t('chart.filter_eq')">{{ $t('chart.filter_eq') }}</span>
              <span v-else-if="item.term === 'not_eq'" :title="$t('chart.filter_not_eq')">{{ $t('chart.filter_not_eq') }}</span>
              <span v-else-if="item.term === 'lt'" :title="$t('chart.filter_lt')">{{ $t('chart.filter_lt') }}</span>
              <span v-else-if="item.term === 'gt'" :title="$t('chart.filter_gt')">{{ $t('chart.filter_gt') }}</span>
              <span v-else-if="item.term === 'le'" :title="$t('chart.filter_le')">{{ $t('chart.filter_le') }}</span>
              <span v-else-if="item.term === 'ge'" :title="$t('chart.filter_ge')">{{ $t('chart.filter_ge') }}</span>
              <span v-else-if="item.term === 'between'" :title="$t('chart.filter_between')">{{ $t('chart.filter_between') }}</span>
            </el-col>
            <el-col :span="12">
              <span v-if="item.term !== 'between'" :title="item.value">{{ item.value }}</span>
              <span v-if="item.term === 'between'">
                {{ item.min }}&nbsp;≤{{ $t('chart.drag_block_label_value') }}≤&nbsp;{{ item.max }}
              </span>
            </el-col>
            <el-col :span="6">
              <span :style="{width:'14px', height:'14px', backgroundColor: item.color, border: 'solid 1px #e1e4e8'}" />
            </el-col>
          </el-row>
        </el-col>
      </el-col>
    </el-col>

    <!--表格-->
    <el-col v-if="chart.type && (chart.render === 'antv' && chart.type.includes('table'))">
      <el-col>
        <el-button
          :title="$t('chart.edit')"
          icon="el-icon-edit"
          type="text"
          size="small"
          style="width: 24px;margin-left: 4px;"
          @click="editTableThreshold"
        />
        <el-col style="padding: 0 18px;max-height: 500px;overflow-y: auto;">
          <el-row v-for="(fieldItem,fieldIndex) in thresholdForm.tableThreshold" :key="fieldIndex">
            <el-row class="field-style">
              <span>
                <svg-icon v-if="fieldItem.field.deType === 0" icon-class="field_text" class="field-icon-text" />
                <svg-icon v-if="fieldItem.field.deType === 1" icon-class="field_time" class="field-icon-time" />
                <svg-icon v-if="fieldItem.field.deType === 2 || fieldItem.field.deType === 3" icon-class="field_value" class="field-icon-value" />
                <svg-icon v-if="fieldItem.field.deType === 5" icon-class="field_location" class="field-icon-location" />
              </span>
              <span :title="fieldItem.field.name" class="field-text">{{ fieldItem.field.name }}</span>
            </el-row>
            <el-row v-for="(item,index) in fieldItem.conditions" :key="index" class="line-style">
              <el-col :span="6">
                <span v-if="item.term === 'eq'" :title="$t('chart.filter_eq')">{{ $t('chart.filter_eq') }}</span>
                <span v-else-if="item.term === 'not_eq'" :title="$t('chart.filter_not_eq')">{{ $t('chart.filter_not_eq') }}</span>
                <span v-else-if="item.term === 'lt'" :title="$t('chart.filter_lt')">{{ $t('chart.filter_lt') }}</span>
                <span v-else-if="item.term === 'gt'" :title="$t('chart.filter_gt')">{{ $t('chart.filter_gt') }}</span>
                <span v-else-if="item.term === 'le'" :title="$t('chart.filter_le')">{{ $t('chart.filter_le') }}</span>
                <span v-else-if="item.term === 'ge'" :title="$t('chart.filter_ge')">{{ $t('chart.filter_ge') }}</span>
                <span v-else-if="item.term === 'between'" :title="$t('chart.filter_between')">{{ $t('chart.filter_between') }}</span>
                <span v-else-if="item.term === 'like'" :title="$t('chart.filter_like')">{{ $t('chart.filter_like') }}</span>
                <span v-else-if="item.term === 'not like'" :title="$t('chart.filter_not_like')">{{ $t('chart.filter_not_like') }}</span>
                <span v-else-if="item.term === 'null'" :title="$t('chart.filter_null')">{{ $t('chart.filter_null') }}</span>
                <span v-else-if="item.term === 'not_null'" :title="$t('chart.filter_not_null')">{{ $t('chart.filter_not_null') }}</span>
                <span v-else-if="item.term === 'empty'" :title="$t('chart.filter_empty')">{{ $t('chart.filter_empty') }}</span>
                <span v-else-if="item.term === 'not_empty'" :title="$t('chart.filter_not_empty')">{{ $t('chart.filter_not_empty') }}</span>
              </el-col>
              <el-col :span="10">
                <span v-if="!item.term.includes('null') && !item.term.includes('empty') && item.term !== 'between'" :title="item.value">{{ item.value }}</span>
                <span v-else-if="!item.term.includes('null') && !item.term.includes('empty') && item.term === 'between'">
                  {{ item.min }}&nbsp;≤{{ $t('chart.drag_block_label_value') }}≤&nbsp;{{ item.max }}
                </span>
                <span v-else>&nbsp;</span>
              </el-col>
              <el-col :span="4">
                <span :title="$t('chart.textColor')" :style="{width:'14px', height:'14px', backgroundColor: item.color, border: 'solid 1px #e1e4e8'}" />
              </el-col>
              <el-col :span="4">
                <span :title="$t('chart.backgroundColor')" :style="{width:'14px', height:'14px', backgroundColor: item.backgroundColor, border: 'solid 1px #e1e4e8'}" />
              </el-col>
            </el-row>
          </el-row>
        </el-col>
      </el-col>
    </el-col>

    <!--编辑指标卡阈值-->
    <el-dialog
      v-if="editLabelThresholdDialog"
      v-dialogDrag
      :title="$t('chart.threshold')"
      :visible="editLabelThresholdDialog"
      :show-close="false"
      width="50%"
      class="dialog-css"
      append-to-body
    >
      <text-threshold-edit :threshold="thresholdForm.labelThreshold" @onLabelThresholdChange="thresholdChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeLabelThreshold">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="changeLabelThreshold">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--编辑表格阈值-->
    <el-dialog
      v-if="editTableThresholdDialog"
      v-dialogDrag
      :title="$t('chart.threshold')"
      :visible="editTableThresholdDialog"
      :show-close="false"
      width="50%"
      class="dialog-css"
      append-to-body
    >
      <table-threshold-edit :threshold="thresholdForm.tableThreshold" :chart="chart" @onTableThresholdChange="tableThresholdChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeTableThreshold">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="changeTableThreshold">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { DEFAULT_THRESHOLD } from '@/views/chart/chart/chart'
import TextThresholdEdit from '@/views/chart/components/senior/dialog/TextThresholdEdit'
import TableThresholdEdit from '@/views/chart/components/senior/dialog/TableThresholdEdit'

export default {
  name: 'Threshold',
  components: { TableThresholdEdit, TextThresholdEdit },
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
      thresholdArr: [],
      editTableThresholdDialog: false,
      tableThresholdArr: []
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
          if (!this.thresholdForm.tableThreshold) {
            this.thresholdForm.tableThreshold = []
          }
        } else {
          this.thresholdForm = JSON.parse(JSON.stringify(DEFAULT_THRESHOLD))
        }
        this.thresholdArr = JSON.parse(JSON.stringify(this.thresholdForm.labelThreshold))
        this.tableThresholdArr = JSON.parse(JSON.stringify(this.thresholdForm.tableThreshold))
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
        if (ele.term === 'between') {
          if (!ele.min || !ele.max) {
            this.$message({
              message: this.$t('chart.value_can_not_empty'),
              type: 'error',
              showClose: true
            })
            return
          }
          if (parseFloat(ele.min).toString() === 'NaN' || parseFloat(ele.max).toString() === 'NaN') {
            this.$message({
              message: this.$t('chart.value_error'),
              type: 'error',
              showClose: true
            })
            return
          }
        } else {
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
      }
      this.thresholdForm.labelThreshold = JSON.parse(JSON.stringify(this.thresholdArr))
      this.changeThreshold()
      this.closeLabelThreshold()
    },
    thresholdChange(val) {
      this.thresholdArr = val
    },
    tableThresholdChange(val) {
      this.tableThresholdArr = val
    },

    editTableThreshold() {
      this.editTableThresholdDialog = true
    },
    closeTableThreshold() {
      this.editTableThresholdDialog = false
    },
    changeTableThreshold() {
      // check line config
      for (let i = 0; i < this.tableThresholdArr.length; i++) {
        const field = this.tableThresholdArr[i]
        if (!field.fieldId) {
          this.$message({
            message: this.$t('chart.field_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (!field.conditions || field.conditions.length === 0) {
          this.$message({
            message: this.$t('chart.conditions_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        for (let j = 0; j < field.conditions.length; j++) {
          const ele = field.conditions[j]
          if (!ele.term || ele.term === '') {
            this.$message({
              message: this.$t('chart.exp_can_not_empty'),
              type: 'error',
              showClose: true
            })
            return
          }
          if (ele.term === 'between') {
            if (!ele.term.includes('null') && !ele.term.includes('empty') && (!ele.min || !ele.max)) {
              this.$message({
                message: this.$t('chart.value_can_not_empty'),
                type: 'error',
                showClose: true
              })
              return
            }
            if ((field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) && (parseFloat(ele.min).toString() === 'NaN' || parseFloat(ele.max).toString() === 'NaN')) {
              this.$message({
                message: this.$t('chart.value_error'),
                type: 'error',
                showClose: true
              })
              return
            }
          } else {
            if (!ele.term.includes('null') && !ele.term.includes('empty') && !ele.value) {
              this.$message({
                message: this.$t('chart.value_can_not_empty'),
                type: 'error',
                showClose: true
              })
              return
            }
            if ((field.field.deType === 2 || field.field.deType === 3 || field.field.deType === 4) && parseFloat(ele.value).toString() === 'NaN') {
              this.$message({
                message: this.$t('chart.value_error'),
                type: 'error',
                showClose: true
              })
              return
            }
          }
        }
      }
      this.thresholdForm.tableThreshold = JSON.parse(JSON.stringify(this.tableThresholdArr))
      this.changeThreshold()
      this.closeTableThreshold()
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

.line-style ::v-deep span{
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin: 0 10px;
}

.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

.field-style{
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.field-text{
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #8492a6;
  font-size: 12px;
}
</style>
