<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="splitForm" :model="splitForm" label-width="80px" size="mini" :disabled="!hasDataPermission('manage',param.privileges)">
        <el-form-item :label="$t('chart.name')" class="form-item">
          <el-checkbox v-model="splitForm.name.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.color')" class="form-item">
          <el-color-picker v-model="splitForm.name.color" class="color-picker-style" @change="changeSplitStyle" />
        </el-form-item>
        <el-form-item :label="$t('chart.text_fontsize')" class="form-item form-item-slider">
          <el-select v-model="splitForm.name.fontSize" :placeholder="$t('chart.text_fontsize')" @change="changeSplitStyle">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.axis_line')" class="form-item">
          <el-checkbox v-model="splitForm.axisLine.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.axis_color')" class="form-item">
          <el-color-picker v-model="splitForm.axisLine.lineStyle.color" class="color-picker-style" @change="changeSplitStyle" />
        </el-form-item>
        <el-form-item :label="$t('chart.axis_label')" class="form-item">
          <el-checkbox v-model="splitForm.axisLabel.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.axis_label_color')" class="form-item">
          <el-color-picker v-model="splitForm.axisLabel.color" class="color-picker-style" @change="changeSplitStyle" />
        </el-form-item>
        <el-form-item :label="$t('chart.label_fontsize')" class="form-item form-item-slider">
          <el-select v-model="splitForm.axisLabel.fontSize" :placeholder="$t('chart.label_fontsize')" @change="changeSplitStyle">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.split_line')" class="form-item">
          <el-checkbox v-model="splitForm.splitLine.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.split_color')" class="form-item">
          <el-color-picker v-model="splitForm.splitLine.lineStyle.color" class="color-picker-style" @change="changeSplitStyle" />
        </el-form-item>
        <el-form-item :label="$t('chart.shadow')" class="form-item">
          <el-checkbox v-model="splitForm.splitArea.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
      </el-form>
    </el-col>
    <!--    <div style="width: 100%">-->
    <!--      <el-popover-->
    <!--        placement="right"-->
    <!--        width="400"-->
    <!--        trigger="click"-->
    <!--      >-->
    <!--        <el-col>-->
    <!--          <el-form ref="splitForm" :model="splitForm" label-width="80px" size="mini">-->
    <!--            <el-form-item :label="$t('chart.name')" class="form-item">-->
    <!--              <el-checkbox v-model="splitForm.name.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.color')" class="form-item">-->
    <!--              <colorPicker v-model="splitForm.name.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeSplitStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_fontsize')" class="form-item form-item-slider">-->
    <!--              <el-select v-model="splitForm.name.fontSize" :placeholder="$t('chart.text_fontsize')" @change="changeSplitStyle">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.axis_line')" class="form-item">-->
    <!--              <el-checkbox v-model="splitForm.axisLine.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.axis_color')" class="form-item">-->
    <!--              <colorPicker v-model="splitForm.axisLine.lineStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeSplitStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.axis_label')" class="form-item">-->
    <!--              <el-checkbox v-model="splitForm.axisLabel.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.axis_label_color')" class="form-item">-->
    <!--              <colorPicker v-model="splitForm.axisLabel.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeSplitStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.label_fontsize')" class="form-item form-item-slider">-->
    <!--              <el-select v-model="splitForm.axisLabel.fontSize" :placeholder="$t('chart.label_fontsize')" @change="changeSplitStyle">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.split_line')" class="form-item">-->
    <!--              <el-checkbox v-model="splitForm.splitLine.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.split_color')" class="form-item">-->
    <!--              <colorPicker v-model="splitForm.splitLine.lineStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeSplitStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.shadow')" class="form-item">-->
    <!--              <el-checkbox v-model="splitForm.splitArea.show" @change="changeSplitStyle">{{ $t('chart.show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" :disabled="!hasDataPermission('manage',param.privileges)" size="mini" class="shape-item">-->
    <!--          {{ $t('chart.split') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
    <!--        </el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_SPLIT } from '../../chart/chart'

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
    }
  },
  data() {
    return {
      splitForm: JSON.parse(JSON.stringify(DEFAULT_SPLIT)),
      isSetting: false,
      fontSize: []
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
    changeSplitStyle() {
      if (!this.splitForm.show) {
        this.isSetting = false
      }
      this.$emit('onChangeSplitForm', this.splitForm)
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
.form-item-slider>>>.el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item>>>.el-form-item__label{
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
