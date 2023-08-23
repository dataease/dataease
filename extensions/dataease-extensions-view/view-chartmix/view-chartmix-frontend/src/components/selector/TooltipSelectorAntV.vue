<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="tooltipForm" :model="tooltipForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="tooltipForm.show" @change="changeTooltipAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="tooltipForm.show">
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="tooltipForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTooltipAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="tooltipForm.textStyle.color" class="color-picker-style" :predefine="predefineColors" @change="changeTooltipAttr" />
          </el-form-item>
          <el-form-item :label="$t('chart.background')" class="form-item">
            <el-color-picker v-model="tooltipForm.backgroundColor" class="color-picker-style" :predefine="predefineColors" @change="changeTooltipAttr" />
          </el-form-item>
<!--          <el-form-item :label="$t('chart.tooltip')" class="form-item">
            <el-select v-model="values" :placeholder="$t('commons.please_select')" multiple collapse-tags @change="changeFields">
                <el-option-group
                    v-for="group in fieldOptions"
                    :key="group.label"
                    :label="group.label"
                >
                    <el-option
                        v-for="item in group.options"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                    </el-option>
                </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <span slot="label">
              <span class="span-box">
                <span>{{ $t('chart.content_formatter') }}</span>
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    {{ $t('plugin_view_chartmix.label_format_tip') }}
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </span>
            <el-input v-model="tooltipForm.tooltipTemplate" type="textarea" :placeholder="defaultPlaceholder" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeTooltipAttr" />
          </el-form-item>-->
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_TOOLTIP, getDefaultTemplate } from '@/utils/map'

export default {
  name: 'TooltipSelectorAntV',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    dimensionData: {
      type: Array,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    },
    view: {
      type: Object,
      required: true
    }
  },
  computed: {
      fieldOptions() {
        const xaxis = this.view.xaxis
        const locationIds = xaxis ? xaxis.map(item => item.id) : []
        return [
          {
            label: this.$t('chart.dimension'),
            options: this.dimensionData && this.dimensionData.filter(item => item.deType !== 5).map(item => {
              item.disabled = locationIds.includes(item.id)
              return item
            })
          },
          {
            label: this.$t('chart.quota'),
            options: this.quotaData && this.quotaData.filter(item => item.deType !== 5).map(item => {
              item.disabled = locationIds.includes(item.id)
              return item
            })
          }
        ]
      },
      tooltipFields() {
          return this.view.viewFields && this.view.viewFields.filter(field => field.busiType === this.busiType)
      },
      defaultPlaceholder() {
          return getDefaultTemplate(this.chart, 'tooltipAxis', true, true)
      }
  },
  data() {
    return {
      tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP)),
      fontSize: [],
      isSetting: false,
      predefineColors: COLOR_PANEL,
      values: null,
      busiType: 'tooltipAxis'
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
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.tooltip) {
          this.tooltipForm = customAttr.tooltip
          if (this.tooltipForm.show) {
              const tooltips = JSON.parse(JSON.stringify(this.tooltipFields))
              this.values = tooltips.map(item => item.id)
          }
        }
      }
    },
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
    changeTooltipAttr() {
      if (!this.tooltipForm.show) {
        this.isSetting = false
      }
      this.$emit('onTooltipChange', this.tooltipForm)
    },
    clearBusiTypeFields() {
        this.view.viewFields = this.view.viewFields.filter(field => field.busiType !== this.busiType)
    },
    changeFields(vals) {
        // let temp = ''
        this.clearBusiTypeFields()
        const allFields = [...JSON.parse(JSON.stringify(this.dimensionData)), ... JSON.parse(JSON.stringify(this.quotaData))]
        allFields.forEach(field => {
            if (vals.includes(field.id)) {
                const item = Object.assign(JSON.parse(JSON.stringify(field)), {busiType: this.busiType})
                item.summary = 'group_concat'
                if(item && item.groupType && item.groupType === 'q') {
                    item.summary = 'sum'
                }
                this.view.viewFields.push(item)
                // temp += (item.name +'：${properties.'+item.name+'}<br>')
            }
        })
        // this.tooltipForm.tooltipTemplate = temp
        this.$emit('onRefreshViewFields',this.view.viewFields)

    },
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
