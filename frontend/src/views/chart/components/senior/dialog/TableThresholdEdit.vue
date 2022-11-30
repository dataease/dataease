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
            @change="addField(fieldItem)"
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
          <el-col :span="4">
            <el-select
              v-model="item.term"
              size="mini"
              @change="changeThreshold"
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
          <el-col
            :span="10"
            style="text-align: center;"
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
                class="between-item"
                :placeholder="$t('chart.axis_value_min')"
                size="mini"
                clearable
                @change="changeThreshold"
              />
              <span style="margin: 0 4px;">≤{{ $t('chart.drag_block_label_value') }}≤</span>
              <el-input
                v-model="item.max"
                class="between-item"
                :placeholder="$t('chart.axis_value_max')"
                size="mini"
                clearable
                @change="changeThreshold"
              />
            </span>
          </el-col>
          <el-col
            :span="4"
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
            :span="4"
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
          <el-col :span="2">
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
        max: '1'
      },
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
        item.conditions && item.conditions.forEach(ele => {
          ele.term = ''
        })
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
      // 暂不支持时间
      // this.fields = this.fields.filter(ele => ele.deType !== 1)
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

    addConditions(item) {
      item.conditions.push(JSON.parse(JSON.stringify(this.thresholdCondition)))
      this.changeThreshold()
    },
    removeCondition(item, index) {
      item.conditions.splice(index, 1)
      this.changeThreshold()
    },
    addField(item) {
      // get field
      if (this.fields && this.fields.length > 0) {
        this.fields.forEach(ele => {
          if (item.fieldId === ele.id) {
            item.field = JSON.parse(JSON.stringify(ele))
            this.initOptions(item)
          }
        })
      }
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

.between-item {
  position: relative;
  display: inline-block;
  width: 90px !important;
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
