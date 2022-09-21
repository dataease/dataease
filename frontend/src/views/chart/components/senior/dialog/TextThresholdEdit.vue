<template>
  <el-col>
    <el-button icon="el-icon-plus" circle size="mini" style="margin-bottom: 10px;" @click="addThreshold" />
    <div style="max-height: 50vh;overflow-y: auto;">
      <el-row v-for="(item,index) in thresholdArr" :key="index" class="line-item">
        <el-col :span="6">
          <el-select v-model="item.term" size="mini" @change="changeThreshold">
            <el-option-group
              v-for="(group,idx) in valueOptions"
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
        <el-col :span="14" style="text-align: center;">
          <el-input v-if="item.term !== 'between'" v-model="item.value" class="value-item" :placeholder="$t('chart.drag_block_label_value')" size="mini" clearable @change="changeThreshold" />
          <span v-if="item.term === 'between'">
            <el-input v-model="item.min" class="between-item" :placeholder="$t('chart.axis_value_min')" size="mini" clearable @change="changeThreshold" />
            <span style="margin: 0 4px;">≤{{ $t('chart.drag_block_label_value') }}≤</span>
            <el-input v-model="item.max" class="between-item" :placeholder="$t('chart.axis_value_max')" size="mini" clearable @change="changeThreshold" />
          </span>
        </el-col>
        <el-col :span="2" style="text-align: center;">
          <el-color-picker v-model="item.color" show-alpha class="color-picker-style" :predefine="predefineColors" @change="changeThreshold" />
        </el-col>
        <el-col :span="2">
          <el-button type="text" icon="el-icon-delete" circle style="float: right" @click="removeThreshold(index)" />
        </el-col>
      </el-row>
    </div>
  </el-col>
</template>

<script>
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'TextThresholdEdit',
  props: {
    threshold: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      thresholdArr: [],
      thresholdObj: {
        term: 'eq',
        field: '0',
        value: '0',
        color: '#ff0000ff',
        min: '0',
        max: '1'
      },
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
      this.$emit('onLabelThresholdChange', this.thresholdArr)
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
  width: 200px !important;
}

.between-item {
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
