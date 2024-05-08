<template>
  <div style="width: 100%;padding: 0 18px;">
    <el-col>
      <el-button
        :title="$t('chart.edit')"
        icon="el-icon-edit"
        type="text"
        size="small"
        style="width: 24px;margin-left: 4px;"
        @click="editLine"
      />
      <el-col>
        <el-row
          v-for="(item,index) in trendLine"
          :key="index"
          class="line-style"
        >
          <el-col :span="8">
            <span :title="item.name">{{ item.name }}</span>
          </el-col>
          <el-col :span="8">
            {{ item.fieldName }}
          </el-col>
          <el-col
            :span="8"
          >
            {{ $t(`chart.regression_${item.algoType}`) }}
          </el-col>
        </el-row>
      </el-col>
    </el-col>

    <el-dialog
      v-if="editLineDialog"
      v-dialogDrag
      :title="$t('chart.trend_line')"
      :visible="editLineDialog"
      :show-close="false"
      width="1000px"
      class="dialog-css"
    >
      <trend-line-edit
        :line="trendLine"
        :quota-fields="quotaData"
        @onTrendLineChange="lineChange"
      />
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="mini"
          @click="closeEditLine"
        >{{ $t('chart.cancel') }}</el-button>
        <el-button
          type="primary"
          size="mini"
          @click="changeLine"
        >{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import TrendLineEdit from '@/views/chart/components/senior/dialog/TrendLineEdit'
export default {
  name: 'TrendLine',
  components: { TrendLineEdit },
  props: {
    chart: {
      type: Object,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      trendLine: [],
      editLineDialog: false,
      lineArr: [],
      quotaFields: []
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
        if (senior.trendLine) {
          this.trendLine.splice(0, this.trendLine.length, ...senior.trendLine)
        } else {
          this.trendLine.splice(0)
        }
        this.lineArr = JSON.parse(JSON.stringify(this.trendLine))
      }
    },
    changeTrendLine() {
      this.$emit('onTrendLineChange', this.trendLine)
    },
    lineChange(val) {
      this.lineArr = val
    },

    editLine() {
      this.editLineDialog = true
    },
    closeEditLine() {
      this.editLineDialog = false
    },
    changeLine() {
      // check line config
      for (let i = 0; i < this.lineArr.length; i++) {
        const ele = this.lineArr[i]
        if (!ele.name) {
          this.$message({
            message: this.$t('chart.name_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (!ele.fieldId) {
          this.$message({
            message: this.$t('chart.field_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.trendLine = JSON.parse(JSON.stringify(this.lineArr))
      this.changeTrendLine()
      this.closeEditLine()
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

.line-style{

}
.line-style ::v-deep span{
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  padding: 0 10px;
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
</style>
