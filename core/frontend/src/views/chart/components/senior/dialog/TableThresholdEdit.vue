<template>
  <el-col>
    <el-button
      icon="el-icon-plus"
      circle
      size="mini"
      style="margin-bottom: 10px;"
      @click="addThreshold"
    />
    <div style="max-height: 50vh;overflow-y: auto;">
      <div
        v-for="(fieldItem,fieldIndex) in thresholdArr"
        :key="fieldIndex"
        class="field-item"
      >
        <el-row style="margin-top: 6px;">
          <span class="color-title">{{ $t('chart.field') }}</span>
          <el-select
            v-model="fieldItem.fieldId"
            size="mini"
            @change="onFieldChange(fieldItem)"
          >
            <el-option
              v-for="fieldOption in fields"
              :key="fieldOption.id"
              :label="fieldOption.name"
              :value="fieldOption.id"
            >
              <span style="float: left">
                <svg-icon
                  v-if="fieldOption.deType === 0"
                  icon-class="field_text"
                  class="field-icon-text"
                />
                <svg-icon
                  v-if="fieldOption.deType === 1"
                  icon-class="field_time"
                  class="field-icon-time"
                />
                <svg-icon
                  v-if="fieldOption.deType === 2 || fieldOption.deType === 3"
                  icon-class="field_value"
                  class="field-icon-value"
                />
                <svg-icon
                  v-if="fieldOption.deType === 5"
                  icon-class="field_location"
                  class="field-icon-location"
                />
              </span>
              <span style="float: left; color: #8492a6; font-size: 12px">{{ fieldOption.name }}</span>
            </el-option>
          </el-select>
          <el-button
            type="text"
            icon="el-icon-plus"
            circle
            size="mini"
            style="margin-bottom: 10px;margin-left: 10px;"
            @click="addConditions(fieldItem)"
          />
          <el-button
            icon="el-icon-delete"
            circle
            size="mini"
            style="margin-bottom: 10px;float: right;"
            @click="removeThreshold(fieldIndex)"
          />
        </el-row>
        <el-row
          v-for="(item,index) in fieldItem.conditions"
          :key="index"
          class="line-item"
        >
          <el-col :span="3">
            <el-select
              v-model="item.term"
              size="mini"
              @change="changeThresholdField(item)"
            >
              <el-option-group
                v-for="(group,idx) in fieldItem.options"
                :key="idx"
                :label="group.label"
              >
                <el-option
                  v-for="opt in group.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-option-group>
            </el-select>
          </el-col>

          <el-col :span="3">
            <el-select
                v-show="!item.term.includes('null') && !item.term.includes('empty')"
                v-model="item.field"
                size="mini"
                style="margin-left: 10px;"
                @change="changeThresholdField(item)"
            >
              <el-option
                  v-for="opt in fieldTypeOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
              />
            </el-select>
          </el-col>
          <el-col
              v-if="item.field === '0'"
              :span="12"
          >
              <el-input
                v-show="!item.term.includes('null') && !item.term.includes('empty') && item.term !== 'between'"
                v-model="item.value"
                class="value-item"
                style="margin-left: 10px;"
                :placeholder="$t('chart.drag_block_label_value')"
                size="mini"
                clearable
                @change="changeThreshold"
              />
              <span v-if="item.term === 'between'">
              <el-input
                v-model="item.min"
                class="item-long-between"
                :placeholder="$t('chart.axis_value_min')"
                size="mini"
                clearable
                @change="changeThreshold"
              />
              <span style="margin: 0 4px;">≤{{ $t('chart.drag_block_label_value') }}≤</span>
              <el-input
                v-model="item.max"
                class="item-long-between"
                :placeholder="$t('chart.axis_value_max')"
                size="mini"
                clearable
                @change="changeThreshold"
              />
            </span>
          </el-col>
          <el-col
              v-if="item.field === '1'"
              :span="12"
          >
            <span v-show="!item.term.includes('null') && !item.term.includes('empty') && item.term !== 'between'">
              <el-select
                v-model="item.targetField.fieldId"
                size="mini"
                style="margin-left: 10px;"
                class="item-long select-item"
                @change="changeThresholdField(item)"
                @visible-change="$forceUpdate()"
              >
                <el-option
                  v-for="fieldOption in fieldItem.fieldOptions"
                  :key="fieldOption.id"
                  :label="fieldOption.name"
                  :value="fieldOption.id"
                >
                <span style="float: left">
                  <svg-icon
                    v-if="fieldOption.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 2 || fieldOption.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                  <span style="float: left; color: #8492a6; font-size: 12px">{{ fieldOption.name }}</span>
                </el-option>
              </el-select>
              <el-select
                  v-model="item.targetField.summary"
                  size="mini"
                  class="item-long select-item"
                  style="margin-left: 10px;"
                  :placeholder="$t('chart.aggregation')"
                  @change="changeThreshold"
                  @visible-change="$forceUpdate()"
              >
                <el-option
                        v-for="opt in getSummaryOptions(fieldItem.field.deType)"
                        :key="opt.id"
                        :value="opt.id"
                        :label="opt.name"
                />
              </el-select>
            </span>

            <span v-if="item.term === 'between'">
              <el-select
                v-model="item.minField.fieldId"
                size="mini"
                style="margin-left: 10px;"
                class="select-item item-short"
                @change="changeThresholdField(item)"
                @visible-change="$forceUpdate()"
              >
                <el-option
                  v-for="fieldOption in fieldItem.fieldOptions"
                  :key="fieldOption.id"
                  :label="fieldOption.name"
                  :value="fieldOption.id"
                >
                <span style="float: left">
                  <svg-icon
                    v-if="fieldOption.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 2 || fieldOption.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                  <span style="float: left; color: #8492a6; font-size: 12px">{{ fieldOption.name }}</span>
                </el-option>
              </el-select>
              <el-select
                v-model="item.minField.summary"
                size="mini"
                class="select-item item-short"
                style="margin-left: 10px;"
                :placeholder="$t('chart.aggregation')"
                @change="changeThreshold"
                @visible-change="$forceUpdate()"
              >
                <el-option
                        v-for="opt in getSummaryOptions(fieldItem.field.deType)"
                        :key="opt.id"
                        :value="opt.id"
                        :label="opt.name"
                />
              </el-select>
              <span style="margin: 0 4px;">≤{{ $t('chart.drag_block_label_value') }}≤</span>
              <el-select
                v-model="item.maxField.fieldId"
                size="mini"
                class="select-item item-short"
                @change="changeThresholdField(item)"
                @visible-change="$forceUpdate()"
              >
                <el-option
                  v-for="fieldOption in fieldItem.fieldOptions"
                  :key="fieldOption.id"
                  :label="fieldOption.name"
                  :value="fieldOption.id"
                >
                <span style="float: left">
                  <svg-icon
                    v-if="fieldOption.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 2 || fieldOption.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="fieldOption.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                  <span style="float: left; color: #8492a6; font-size: 12px">{{ fieldOption.name }}</span>
                </el-option>
              </el-select>
              <el-select
                v-model="item.maxField.summary"
                size="mini"
                class="select-item item-short"
                style="margin-left: 10px;"
                :placeholder="$t('chart.aggregation')"
                @change="changeThreshold"
                @visible-change="$forceUpdate()"
              >
                <el-option
                  v-for="opt in getSummaryOptions(fieldItem.field.deType)"
                  :key="opt.id"
                  :value="opt.id"
                  :label="opt.name"
                />
              </el-select>
            </span>

          </el-col>


          <el-col
            :span="3"
            style="display: flex;align-items: center;justify-content: center;"
          >
            <span class="color-title">{{ $t('chart.textColor') }}</span>
            <el-color-picker
              v-model="item.color"
              show-alpha
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeThreshold"
            />
          </el-col>
          <el-col
            :span="3"
            style="display: flex;align-items: center;justify-content: center;"
          >
            <span class="color-title">{{ $t('chart.backgroundColor') }}</span>
            <el-color-picker
              v-model="item.backgroundColor"
              show-alpha
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeThreshold"
            />
          </el-col>
          <el-col :span="1">
            <el-button
              type="text"
              icon="el-icon-delete"
              circle
              style="float: right"
              @click="removeCondition(fieldItem,index)"
            />
          </el-col>
        </el-row>
      </div>
    </div>
    <div class="tip">{{ $t('chart.table_threshold_tip') }}</div>
  </el-col>
</template>

<script>
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'TableThresholdEdit',
  props: {
    threshold: {
      type: Array,
      required: true
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      thresholdArr: [],
      fields: [],
      fieldsByType: {
        text: [],
        value: [],
        date: []
      },
      thresholdObj: {
        fieldId: '',
        field: {},
        conditions: []
      },
      thresholdCondition: {
        term: 'eq',
        field: '0',
        value: '',
        color: '#ff0000ff',
        backgroundColor: '#ffffff00',
        min: '0',
        max: '1',
        targetField:{},
        minField:{},
        maxField:{}
      },
      summaryOptions: [{
          id: 'value',
          name: this.$t('chart.value')
      }, {
          id: 'avg',
          name: this.$t('chart.avg')
      }, {
          id: 'max',
          name: this.$t('chart.max')
      }, {
          id: 'min',
          name: this.$t('chart.min')
      }],
      textOptions: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'like',
            label: this.$t('chart.filter_like')
          }, {
            value: 'not like',
            label: this.$t('chart.filter_not_like')
          }]
        },
        {
          label: '',
          options: [{
            value: 'null',
            label: this.$t('chart.filter_null')
          }, {
            value: 'not_null',
            label: this.$t('chart.filter_not_null')
          }]
        }
      ],
      dateOptions: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'lt',
            label: this.$t('chart.filter_lt')
          }, {
            value: 'gt',
            label: this.$t('chart.filter_gt')
          }]
        },
        {
          label: '',
          options: [{
            value: 'le',
            label: this.$t('chart.filter_le')
          }, {
            value: 'ge',
            label: this.$t('chart.filter_ge')
          }]
        }
      ],
      valueOptions: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'lt',
            label: this.$t('chart.filter_lt')
          }, {
            value: 'gt',
            label: this.$t('chart.filter_gt')
          }]
        },
        {
          label: '',
          options: [{
            value: 'le',
            label: this.$t('chart.filter_le')
          }, {
            value: 'ge',
            label: this.$t('chart.filter_ge')
          }]
        },
        {
          label: '',
          options: [{
            value: 'between',
            label: this.$t('chart.filter_between')
          }]
        }
      ],
      fieldTypeOptions: [
        { label: this.$t('chart.field_fixed'), value: '0' },
        { label: this.$t('chart.field_dynamic'), value: '1' }
      ],
      predefineColors: COLOR_PANEL
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      this.thresholdArr = JSON.parse(JSON.stringify(this.threshold))
      this.initFields()
      this.thresholdArr && this.thresholdArr.forEach(ele => {
        this.initOptions(ele)
        if (ele.conditions) {
          for (const item of ele.conditions) {
            this.initConditionField(item)
          }
        }
      })
    },
    initConditionField(item) {
      // 兼容旧数据
      if (!item.targetField) {
        item.targetField = {};
      }
      if (!item.minField) {
        item.minField = {};
      }
      if (!item.maxField) {
        item.maxField = {};
      }
    },
    initOptions(item) {
      if (item.field) {
        if (item.field.deType === 0 || item.field.deType === 5) {
          item.options = JSON.parse(JSON.stringify(this.textOptions))
        } else if (item.field.deType === 1) {
          item.options = JSON.parse(JSON.stringify(this.dateOptions))
        } else {
          item.options = JSON.parse(JSON.stringify(this.valueOptions))
        }
        this.initFieldOptions(item)
      }
    },
    initFieldOptions(item) {
      if (item.field) {
        if (item.field.deType === 0 || item.field.deType === 5) {
          item.fieldOptions = this.fieldsByType.text
        } else if (item.field.deType === 1) {
          item.fieldOptions = this.fieldsByType.date
        } else {
          item.fieldOptions = this.fieldsByType.value
        }
      }
    },
    initFields() {
      // 暂时支持指标
      if (this.chart.type === 'table-info') {
        if (Object.prototype.toString.call(this.chart.xaxis) === '[object Array]') {
          this.fields = JSON.parse(JSON.stringify(this.chart.xaxis))
        } else {
          this.fields = JSON.parse(this.chart.xaxis)
        }
      } else if (this.chart.type === 'table-pivot') {
        if (Object.prototype.toString.call(this.chart.yaxis) === '[object Array]') {
          this.fields = JSON.parse(JSON.stringify(this.chart.yaxis))
        } else {
          this.fields = JSON.parse(this.chart.yaxis)
        }
      } else {
        if (Object.prototype.toString.call(this.chart.xaxis) === '[object Array]') {
          this.fields = this.fields.concat(JSON.parse(JSON.stringify(this.chart.xaxis)))
        } else {
          this.fields = this.fields.concat(JSON.parse(this.chart.xaxis))
        }
        if (Object.prototype.toString.call(this.chart.yaxis) === '[object Array]') {
          this.fields = this.fields.concat(JSON.parse(JSON.stringify(this.chart.yaxis)))
        } else {
          this.fields = this.fields.concat(JSON.parse(this.chart.yaxis))
        }
      }

      // 区分文本、数值、日期字段
      this.fields.forEach(ele => {
        // 视图字段和计数字段不可用
        if (ele.chartId || ele.id === 'count') {
          return;
        }
        if (ele.deType === 0 || ele.deType === 5) {
          this.fieldsByType.text.push(ele)
        } else if (ele.deType === 1) {
          this.fieldsByType.date.push(ele)
        } else {
          this.fieldsByType.value.push(ele)
        }
      })
    },
    getSummaryOptions(deType) {
      if (deType === 1) {
          // 时间
          return this.summaryOptions.filter(ele => {
              return ele.id !== 'avg'
          })
      } else if (deType === 0 || deType === 5) {
          // 文本、地理位置
          return this.summaryOptions.filter(ele => {
              return ele.id === 'value'
          })
      } else {
          return this.summaryOptions
      }
    },
    addThreshold() {
      this.thresholdArr.push(JSON.parse(JSON.stringify(this.thresholdObj)))
      this.changeThreshold()
    },
    removeThreshold(index) {
      this.thresholdArr.splice(index, 1)
      this.changeThreshold()
    },
    changeThreshold() {
      this.$emit('onTableThresholdChange', this.thresholdArr)
    },
    changeThresholdField(item) {
      if (item.field === '1') {
        if (item.term === 'between') {
          item.minField.curField = this.getQuotaField(item.minField.fieldId)
          item.maxField.curField = this.getQuotaField(item.maxField.fieldId)
          item.targetField = {};
        } else {
          item.targetField.curField = this.getQuotaField(item.targetField.fieldId)
          item.minField = {};
          item.maxField = {};
        }
      } else {
          item.targetField = {};
          item.minField = {};
          item.maxField = {};
      }
      this.changeThreshold()
    },
    getQuotaField(id) {
        if (!id) {
            return {}
        }
        const fields = this.fields.filter(ele => {
            return ele.id === id
        })
        if (fields.length === 0) {
            return {}
        } else {
            return fields[0]
        }
    },
    addConditions(item) {
      item.conditions.push(JSON.parse(JSON.stringify(this.thresholdCondition)))
      this.changeThreshold()
    },
    removeCondition(item, index) {
      item.conditions.splice(index, 1)
      this.changeThreshold()
    },
    onFieldChange(item) {
      // get field
      if (this.fields && this.fields.length > 0) {
        this.fields.forEach(ele => {
          if (item.fieldId === ele.id) {
            item.field = JSON.parse(JSON.stringify(ele))
            this.initOptions(item)
          }
        })
      }
      // 清空 term
      item.conditions && item.conditions.forEach(ele => {
        ele.term = ''
      })
      this.changeThreshold()
    }
  }
}
</script>

<style scoped>
.field-item {
  width: 100%;
  border-radius: 4px;
  border: 1px solid #DCDFE6;
  padding: 4px 14px;
  margin-bottom: 10px;
}

.line-item {
  width: 100%;
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
}

.select-item {
  position: relative;
  display: inline-block;
  width: 100px !important;
}

.item-long {
  position: relative;
  display: inline-block;
  width: 220px !important;
}

.item-long-between {
  position: relative;
  display: inline-block;
  width: 200px !important;
}

.item-long:first-child,.item-short:first-child,.item-long-between:first-child {
  margin-left: 10px;
}

.item-short {
  position: relative;
  display: inline-block;
  width: 95px !important;
}

.el-select-dropdown__item {
  padding: 0 20px;
  font-size: 12px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
  width: 28px;
  height: 28px;
}

.color-picker-style ::v-deep .el-color-picker__trigger {
  width: 28px;
  height: 28px;
}

.color-title {
  margin-right: 6px;
  color: #909399;
}

.tip {
  color: #F56C6C;
  font-size: 12px;
}
</style>
