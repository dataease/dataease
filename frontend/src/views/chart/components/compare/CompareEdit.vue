<template>
  <div>
    <el-form ref="form" :model="compareItem.compareCalc" label-width="80px" size="mini" class="compare-form">
      <el-form-item :label="$t('chart.compare_date')">
        <el-select v-model="compareItem.compareCalc.field" :placeholder="$t('chart.pls_select_field')" size="mini" @change="initCompareType">
          <el-option v-for="field in fieldList" :key="field.id" :label="field.name + '(' + $t('chart.' + field.dateStyle) + ')'" :value="field.id" />
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('chart.compare_type')">
        <el-radio-group v-model="compareItem.compareCalc.type">
          <el-radio v-for="radio in compareList" :key="radio.value" :label="radio.value">{{ $t('chart.' + radio.value) }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('chart.compare_data')">
        <el-radio-group v-model="compareItem.compareCalc.resultData">
          <el-radio label="sub">{{ $t('chart.data_sub') }}</el-radio>
          <el-radio label="percent">{{ $t('chart.data_percent') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('chart.compare_calc_expression')">
        <span v-if="compareItem.compareCalc.resultData === 'sub'" class="exp-style">本期数据 - 上期数据</span>
        <span v-else-if="compareItem.compareCalc.resultData === 'percent'" class="exp-style">(本期数据 / 上期数据 - 1) * 100%</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { compareDayList, compareMonthList, compareYearList } from '@/views/chart/chart/compare'

export default {
  name: 'CompareEdit',
  props: {
    compareItem: {
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
      fieldList: [],
      compareList: []
    }
  },
  watch: {
    'chart': function() {
      this.initFieldList()
      this.initCompareType()
    }
  },
  mounted() {
    this.initFieldList()
    this.initCompareType()
  },
  methods: {
    // 过滤xaxis，extStack所有日期字段
    initFieldList() {
      let xAxis = null
      if (Object.prototype.toString.call(this.chart.xaxis) === '[object Array]') {
        xAxis = JSON.parse(JSON.stringify(this.chart.xaxis))
      } else {
        xAxis = JSON.parse(this.chart.xaxis)
      }
      const t1 = xAxis.filter(ele => { return ele.deType === 1 })
      this.fieldList = t1
      // 如果没有选中字段，则默认选中第一个
      if ((!this.compareItem.compareCalc.field || this.compareItem.compareCalc.field === '') && this.fieldList.length > 0) {
        this.compareItem.compareCalc.field = this.fieldList[0].id
      }
    },
    // 获得不同字段格式对应能计算的同环比列表
    initCompareType() {
      const checkedField = this.fieldList.filter(ele => ele.id === this.compareItem.compareCalc.field)
      if (checkedField && checkedField.length > 0) {
        switch (checkedField[0].dateStyle) {
          case 'y':
            this.compareList = compareYearList
            break
          case 'y_M':
            this.compareList = compareMonthList
            break
          case 'y_M_d':
            this.compareList = compareDayList
            break
          default:
            break
        }
      } else {
        this.compareList = []
      }
      // 如果没有选中一个同环比类型，则默认选中第一个
      if ((!this.compareItem.compareCalc.type || this.compareItem.compareCalc.type === '' || this.compareItem.compareCalc.type === 'none') && this.compareList.length > 0) {
        this.compareItem.compareCalc.type = this.compareList[0].value
      }
    }
  }
}
</script>

<style scoped>
.el-form-item{
  margin-bottom: 10px!important;
}
.compare-form ::v-deep .el-form-item__label{
  font-size: 12px!important;
  font-weight: 400!important;
}
.compare-form ::v-deep .el-radio__label{
  font-size: 12px!important;
  font-weight: 400!important;
}
.el-select-dropdown__item ::v-deep span{
  font-size: 12px!important;
}
.exp-style{
  color: #C0C4CC;
  font-size: 12px;
}
</style>
