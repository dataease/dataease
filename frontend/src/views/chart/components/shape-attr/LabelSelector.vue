<template>
  <div>
    <div style="width: 100%">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="labelForm" :model="labelForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.show')" class="form-item">
              <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
            </el-form-item>
            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
              <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">
                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('chart.text_color')" class="form-item">
              <colorPicker v-model="labelForm.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeLabelAttr" />
            </el-form-item>
            <el-form-item :label="$t('chart.label_position')" class="form-item">
              <el-radio-group v-model="labelForm.position" size="mini" @change="changeLabelAttr">
                <el-radio-button label="inside">{{ $t('chart.inside') }}</el-radio-button>
                <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
                <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
                <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
                <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item class="form-item">
              <span slot="label">
                <span class="span-box">
                  <span>{{ $t('chart.content_formatter') }}</span>
                  <el-tooltip class="item" effect="dark" placement="bottom">
                    <div slot="content">
                      字符串支持用 \n 换行<br>字符串模板 模板变量有：<br>{a}：系列名。<br>{b}：数据名。<br>{c}：数据值。
                    </div>
                    <i class="el-icon-info" style="cursor: pointer;" />
                  </el-tooltip>
                </span>
              </span>
              <el-input v-model="labelForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr" />
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.label') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_LABEL } from '../../chart/chart'

export default {
  name: 'LabelSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      fontSize: []
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        if (chart.customAttr) {
          const customAttr = JSON.parse(chart.customAttr)
          if (customAttr.label) {
            this.labelForm = customAttr.label
          }
        }
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      const arr = []
      for (let i = 10; i <= 20; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeLabelAttr() {
      this.$emit('onLabelChange', this.labelForm)
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
</style>
