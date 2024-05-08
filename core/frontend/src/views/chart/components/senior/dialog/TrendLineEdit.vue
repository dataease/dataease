<template>
  <el-col>
    <el-button
      icon="el-icon-plus"
      circle
      size="mini"
      style="margin-bottom: 10px;"
      @click="addLine"
    />
    <div style="max-height: 50vh;overflow-y: auto;">
      <el-row
        v-for="(item,index) in lineArr"
        :key="index"
        class="line-item"
      >
        <el-col :span="4">
          <el-input
            v-model="item.name"
            class="value-item"
            style="width: 90% !important;"
            :placeholder="$t('chart.name')"
            size="mini"
            clearable
            @change="changeTrendLine"
          />
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="item.fieldId"
            size="mini"
            class="select-item"
            :placeholder="$t('chart.field')"
            @change="changeTrendLineField(item)"
          >
            <el-option
              v-for="quota in quotaData"
              :key="quota.id"
              :label="quota.name"
              :value="quota.id"
            >
              <span style="float: left">
                <svg-icon
                  v-if="quota.deType === 0"
                  icon-class="field_text"
                  class="field-icon-text"
                />
                <svg-icon
                  v-if="quota.deType === 1"
                  icon-class="field_time"
                  class="field-icon-time"
                />
                <svg-icon
                  v-if="quota.deType === 2 || quota.deType === 3"
                  icon-class="field_value"
                  class="field-icon-value"
                />
                <svg-icon
                  v-if="quota.deType === 5"
                  icon-class="field_location"
                  class="field-icon-location"
                />
              </span>
              <span style="float: left; color: #8492a6; font-size: 12px">{{ quota.name }}</span>
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select
            v-model="item.algoType"
            size="mini"
            class="select-item"
            style="margin-left: 10px;"
            :placeholder="$t('chart.regression_algo')"
            @change="changeTrendLine"
          >
            <el-option
              v-for="option in algoOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select
            v-model="item.fontSize"
            size="mini"
            class="select-item"
            style="margin-left: 10px;"
            :placeholder="$t('chart.text_fontsize')"
            @change="changeTrendLine"
          >
            <el-option
              v-for="option in fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select
            v-model="item.lineType"
            size="mini"
            class="select-item"
            @change="changeTrendLine"
          >
            <el-option
              v-for="opt in lineOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col
          :span="1"
          style="text-align: center;"
        >
          <el-color-picker
            v-model="item.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeTrendLine"
          />
        </el-col>
        <el-col :span="1">
          <el-button
            type="text"
            icon="el-icon-delete"
            circle
            style="float: right"
            @click="removeLine(index)"
          />
        </el-col>
      </el-row>
    </div>
  </el-col>
</template>

<script>
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'TrendLineEdit',
  props: {
    line: {
      type: Array,
      required: true
    },
    quotaFields: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      lineArr: [],
      lineObj: {
        name: '趋势线',
        algoType: 'poly',
        fieldId: '',
        fieldName: '',
        lineType: 'solid',
        color: '#ff0000',
        fontSize: 10
      },
      algoOptions: [
        { label: this.$t('chart.regression_poly'), value: 'poly' },
        { label: this.$t('chart.regression_linear'), value: 'linear' },
        { label: this.$t('chart.regression_exp'), value: 'exp' },
        { label: this.$t('chart.regression_log'), value: 'log' },
        { label: this.$t('chart.regression_quad'), value: 'quad' },
        { label: this.$t('chart.regression_pow'), value: 'pow' },
        { label: this.$t('chart.regression_loess'), value: 'loess' }
      ],
      lineOptions: [
        { label: this.$t('chart.line_type_solid'), value: 'solid' },
        { label: this.$t('chart.line_type_dashed'), value: 'dashed' },
        { label: this.$t('chart.line_type_dotted'), value: 'dotted' }
      ],
      predefineColors: COLOR_PANEL,
      quotaData: [],
      fontSize: []
    }
  },
  watch: {
    'quotaFields': function() {
      this.initField()
    }
  },
  mounted() {
    this.initField()
    this.init()
  },
  methods: {
    initField() {
      this.quotaData = this.quotaFields.filter(ele => !ele.chartId && ele.id !== 'count')
    },
    init() {
      this.lineArr = JSON.parse(JSON.stringify(this.line))
      for (let i = 10; i <= 60; i = i + 2) {
        this.fontSize.push({
          name: i + '',
          value: i
        })
      }
    },
    addLine() {
      const obj = {
        ...this.lineObj,
        fieldId: this.quotaData ? this.quotaData[0]?.id : null,
        fieldName: this.quotaData ? this.quotaData[0]?.name : null
      }
      this.lineArr.push(JSON.parse(JSON.stringify(obj)))
      this.changeTrendLine()
    },
    removeLine(index) {
      this.lineArr.splice(index, 1)
      this.changeTrendLine()
    },

    changeTrendLine() {
      this.$emit('onTrendLineChange', this.lineArr)
    },
    changeTrendLineField(item) {
      const curField = this.getQuotaField(item.fieldId)
      item.fieldName = curField.name
      this.changeTrendLine()
    },
    getQuotaField(id) {
      if (!id) {
        return {}
      }
      const fields = this.quotaData.filter(ele => {
        return ele.id === id
      })
      if (fields.length === 0) {
        return {}
      } else {
        return fields[0]
      }
    }
  }
}
</script>

<style scoped>
.line-item {
  width: 100%;
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  padding: 4px 14px;
  margin-bottom: 10px;
  display: flex;
  justify-content: left;
  align-items: center;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

span {
  font-size: 12px;
}

.value-item {
  position: relative;
  display: inline-block;
  width: 100px !important;
}

.select-item {
  position: relative;
  display: inline-block;
  width: 100px !important;
}

.el-select-dropdown__item {
  padding: 0 20px;
  font-size: 12px;
}

.color-picker-style{
  cursor: pointer;
  z-index: 1003;
  width: 28px;
  height: 28px;
  margin-top: 6px;
}

.color-picker-style ::v-deep .el-color-picker__trigger{
  width: 28px;
  height: 28px;
}
</style>
