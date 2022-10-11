<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="splitForm"
        :model="splitForm"
        label-width="80px"
        size="mini"
      >
        <el-form-item
          v-show="showProperty('name')"
          :label="$t('chart.name')"
          class="form-item"
        >
          <el-checkbox
            v-model="splitForm.name.show"
            @change="changeSplitStyle('name')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item
          v-show="showProperty('name')"
          :label="$t('chart.color')"
          class="form-item"
        >
          <el-color-picker
            v-model="splitForm.name.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeSplitStyle('name')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('name')"
          :label="$t('chart.text_fontsize')"
          class="form-item form-item-slider"
        >
          <el-select
            v-model="splitForm.name.fontSize"
            :placeholder="$t('chart.text_fontsize')"
            @change="changeSplitStyle('name')"
          >
            <el-option
              v-for="option in fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('axisLine')"
          :label="$t('chart.axis_line')"
          class="form-item"
        >
          <el-checkbox
            v-model="splitForm.axisLine.show"
            @change="changeSplitStyle('axisLine')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item
          v-show="showProperty('axisLine')"
          :label="$t('chart.axis_color')"
          class="form-item"
        >
          <el-color-picker
            v-model="splitForm.axisLine.lineStyle.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeSplitStyle('axisLine')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('axisLabel')"
          :label="$t('chart.axis_label')"
          class="form-item"
        >
          <el-checkbox
            v-model="splitForm.axisLabel.show"
            @change="changeSplitStyle('axisLabel')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item
          v-show="showProperty('axisLabel')"
          :label="$t('chart.axis_label_color')"
          class="form-item"
        >
          <el-color-picker
            v-model="splitForm.axisLabel.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeSplitStyle('axisLabel')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('axisLabel')"
          :label="$t('chart.label_fontsize')"
          class="form-item form-item-slider"
        >
          <el-select
            v-model="splitForm.axisLabel.fontSize"
            :placeholder="$t('chart.label_fontsize')"
            @change="changeSplitStyle('axisLabel')"
          >
            <el-option
              v-for="option in fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-show="showProperty('splitLine')"
          :label="$t('chart.split_line')"
          class="form-item"
        >
          <el-checkbox
            v-model="splitForm.splitLine.show"
            @change="changeSplitStyle('splitLine')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item
          v-show="showProperty('splitLine')"
          :label="$t('chart.split_color')"
          class="form-item"
        >
          <el-color-picker
            v-model="splitForm.splitLine.lineStyle.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeSplitStyle('splitLine')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('splitArea')"
          :label="$t('chart.shadow')"
          class="form-item"
        >
          <el-checkbox
            v-model="splitForm.splitArea.show"
            @change="changeSplitStyle('splitArea')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_SPLIT } from '../../chart/chart'

export default {
  name: 'SplitSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      splitForm: JSON.parse(JSON.stringify(DEFAULT_SPLIT)),
      isSetting: false,
      fontSize: [],
      predefineColors: COLOR_PANEL
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
    this.init()
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.split) {
          this.splitForm = customStyle.split
        } else {
          this.splitForm = JSON.parse(JSON.stringify(DEFAULT_SPLIT))
        }
      }
    },
    init() {
      const arr = []
      for (let i = 6; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeSplitStyle(modifyName) {
      if (!this.splitForm.show) {
        this.isSetting = false
      }
      this.splitForm['modifyName'] = modifyName
      this.$emit('onChangeSplitForm', this.splitForm)
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 10px 0
  }
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
</style>
