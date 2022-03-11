<template>
  <el-col>
    <el-button icon="el-icon-plus" circle size="mini" style="margin-bottom: 10px;" @click="addLine" />
    <div style="max-height: 50vh;overflow-y: auto;">
      <el-row v-for="(item,index) in lineArr" :key="index" class="line-item">
        <el-col :span="6">
          <el-input v-model="item.name" class="value-item" :placeholder="$t('chart.name')" size="mini" clearable @change="changeAssistLine" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="item.field" size="mini" class="select-item" @change="changeAssistLine">
            <el-option
              v-for="opt in fieldOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input v-model="item.value" class="value-item" :placeholder="$t('chart.drag_block_label_value')" size="mini" clearable @change="changeAssistLine" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="item.lineType" size="mini" class="select-item" @change="changeAssistLine">
            <el-option
              v-for="opt in lineOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col :span="2" style="text-align: center;">
          <el-color-picker v-model="item.color" class="color-picker-style" :predefine="predefineColors" @change="changeAssistLine" />
        </el-col>
        <el-col :span="2">
          <el-button type="text" icon="el-icon-delete" circle style="float: right" @click="removeLine(index)" />
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
    }
  },
  data() {
    return {
      lineArr: [],
      lineObj: {
        name: '辅助线',
        field: '0', // 固定值
        axis: 'y', // 主轴
        value: '0',
        lineType: 'solid',
        color: '#ff0000'
      },
      fieldOptions: [
        { label: this.$t('chart.field_fixed'), value: '0' }
      ],
      lineOptions: [
        { label: this.$t('chart.line_type_solid'), value: 'solid' },
        { label: this.$t('chart.line_type_dashed'), value: 'dashed' },
        { label: this.$t('chart.line_type_dotted'), value: 'dotted' }
      ],
      predefineColors: COLOR_PANEL
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      this.lineArr = JSON.parse(JSON.stringify(this.line))
    },
    addLine() {
      this.lineArr.push(JSON.parse(JSON.stringify(this.lineObj)))
      this.changeAssistLine()
    },
    removeLine(index) {
      this.lineArr.splice(index, 1)
      this.changeAssistLine()
    },

    changeAssistLine() {
      this.$emit('onAssistLineChange', this.lineArr)
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

.form-item >>> .el-form-item__label {
  font-size: 12px;
}

span {
  font-size: 12px;
}

.value-item {
  position: relative;
  display: inline-block;
  width: 120px !important;
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

.color-picker-style >>> .el-color-picker__trigger{
  width: 28px;
  height: 28px;
}
</style>
