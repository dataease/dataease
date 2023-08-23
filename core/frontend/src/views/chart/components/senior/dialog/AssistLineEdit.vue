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
            @change="changeAssistLine"
          />
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="item.field"
            size="mini"
            class="select-item"
            @change="changeAssistLine"
          >
            <el-option
              v-for="opt in fieldOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col
          v-if="item.field === '0'"
          :span="6"
        >
          <el-input
            v-model="item.value"
            class="value-item"
            :placeholder="$t('chart.drag_block_label_value')"
            size="mini"
            clearable
            @change="changeAssistLine"
          />
        </el-col>
        <el-col
          v-if="item.field === '1'"
          :span="6"
        >
          <el-select
            v-model="item.fieldId"
            size="mini"
            class="select-item"
            :placeholder="$t('chart.field')"
            @change="changeAssistLineField(item)"
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
          <el-select
            v-model="item.summary"
            size="mini"
            class="select-item"
            style="margin-left: 10px;"
            :placeholder="$t('chart.aggregation')"
            @change="changeAssistLine"
          >
            <el-option
              key="avg"
              value="avg"
              :label="$t('chart.avg')"
            />
            <el-option
              key="max"
              value="max"
              :label="$t('chart.max')"
            />
            <el-option
              key="min"
              value="min"
              :label="$t('chart.min')"
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
            @change="changeAssistLine"
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
            @change="changeAssistLine"
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
            @change="changeAssistLine"
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
  name: 'AssistLineEdit',
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
        name: '辅助线',
        field: '0', // 固定值
        fieldId: '',
        summary: 'avg',
        axis: 'y', // 主轴
        value: '0',
        lineType: 'solid',
        color: '#ff0000',
        curField: {},
        fontSize: '10'
      },
      fieldOptions: [
        { label: this.$t('chart.field_fixed'), value: '0' },
        { label: this.$t('chart.field_dynamic'), value: '1' }
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

      const arr = []
      for (let i = 10; i <= 60; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    addLine() {
      const obj = { ...this.lineObj,
        curField: this.quotaData ? this.quotaData[0] : null,
        fieldId: this.quotaData ? this.quotaData[0]?.id : null
      }
      this.lineArr.push(JSON.parse(JSON.stringify(obj)))
      this.changeAssistLine()
    },
    removeLine(index) {
      this.lineArr.splice(index, 1)
      this.changeAssistLine()
    },

    changeAssistLine() {
      this.$emit('onAssistLineChange', this.lineArr)
    },
    changeAssistLineField(item) {
      item.curField = this.getQuotaField(item.fieldId)
      this.changeAssistLine()
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
