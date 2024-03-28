<template>
  <div style="width: 100%;">
    <el-col>
      <el-form ref="labelForm" :model="labelForm" label-width="80px" size="mini">

        <el-divider content-position="left"><span style="font-size: 12px">主轴标签</span></el-divider>

        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.show">
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini"
                       @change="changeLabelAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.color" class="color-picker-style" :predefine="predefineColors"
                             @change="changeLabelAttr"/>
          </el-form-item>
          <el-form-item
            class="form-item"
          >
            <template #label>
              {{ $t('chart.label_position') }}
              <el-tooltip
                effect="dark"
                content="仅对柱状图生效"
              >
                <i
                  class="el-icon-info"
                  style="cursor: pointer;"
                />
              </el-tooltip>
            </template>
            <el-select
              v-model="labelForm.position"
              :placeholder="$t('chart.label_position')"
              @change="changeLabelAttr"
            >
              <el-option
                v-for="option in labelPositionV"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-divider content-position="left"><span style="font-size: 12px">副轴标签</span></el-divider>

        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.subShow" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.subShow">
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.subFontSize" :placeholder="$t('chart.text_fontsize')" size="mini"
                       @change="changeLabelAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.subColor" class="color-picker-style" :predefine="predefineColors"
                             @change="changeLabelAttr"/>
          </el-form-item>
          <el-form-item
            class="form-item"
          >
            <template #label>
              {{ $t('chart.label_position') }}
              <el-tooltip
                effect="dark"
                content="仅对柱状图生效"
              >
                <i
                  class="el-icon-info"
                  style="cursor: pointer;"
                />
              </el-tooltip>
            </template>
            <el-select
              v-model="labelForm.subPosition"
              :placeholder="$t('chart.label_position')"
              @change="changeLabelAttr"
            >
              <el-option
                v-for="option in labelPositionV"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

    </el-col>
  </div>
</template>

<script>
import {COLOR_PANEL, DEFAULT_LABEL, getDefaultTemplate} from '../../utils/map'

export default {
  name: 'LabelSelector',
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
    labelFields() {
      return this.view.viewFields && this.view.viewFields.filter(field => field.busiType === this.busiType)
    },
    defaultPlaceholder() {
      return getDefaultTemplate(this.chart, 'labelAxis', false, false)
    }
  },
  data() {
    return {
      labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      fontSize: [],
      isSetting: false,
      labelPosition: [],
      labelPositionPie: [
        {name: this.$t('chart.inside'), value: 'inside'},
        {name: this.$t('chart.outside'), value: 'outside'}
      ],
      predefineColors: COLOR_PANEL,
      values: null,
      busiType: 'labelAxis',
      labelPositionV: [
        {name: this.$t('chart.text_pos_top'), value: 'top'},
        {name: this.$t('chart.center'), value: 'middle'},
        {name: this.$t('chart.text_pos_bottom'), value: 'bottom'}
      ],
    }
  },
  watch: {
    'chart': {
      handler: function () {
        this.initOptions()
        this.initData()
      }
    }
  },
  mounted() {
    this.init()
    this.initOptions()
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
        if (customAttr.label) {
          if (customAttr.label.initialized) {
            this.labelForm = customAttr.label
          }
          if (this.labelForm.show) {
            const labes = JSON.parse(JSON.stringify(this.labelFields))
            this.values = labes.map(item => item.id)
          }
          if (!this.labelForm.labelLine) {
            this.labelForm.labelLine = JSON.parse(JSON.stringify(DEFAULT_LABEL.labelLine))
          }
          if (this.labelForm.subShow === undefined) {
            this.labelForm.subShow = DEFAULT_LABEL.subShow
            this.labelForm.subPosition = DEFAULT_LABEL.subPosition
            this.labelForm.subColor = DEFAULT_LABEL.subColor
            this.labelForm.subFontSize = DEFAULT_LABEL.subFontSize
          }

          if (!customAttr.label.initialized) {
            this.changeLabelAttr();
          }
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeLabelAttr() {
      if (!this.labelForm.show) {
        this.isSetting = false
      }
      this.$emit('onLabelChange', this.labelForm)
    },

    clearBusiTypeFields() {
      this.view.viewFields = this.view.viewFields.filter(field => field.busiType !== this.busiType)
    },
    changeFields(vals) {
      this.clearBusiTypeFields()
      const allFields = [...JSON.parse(JSON.stringify(this.dimensionData)), ...JSON.parse(JSON.stringify(this.quotaData))]
      allFields.forEach(field => {
        if (vals.includes(field.id)) {
          const item = Object.assign(JSON.parse(JSON.stringify(field)), {busiType: this.busiType})
          item.summary = 'group_concat'
          if (item && item.groupType && item.groupType === 'q') {
            item.summary = 'sum'
          }
          this.view.viewFields.push(item)
        }
      })

      this.$emit('onRefreshViewFields', this.view.viewFields)

    },
    initOptions() {
      const type = this.chart.type
      if (type) {
        this.labelPosition = this.labelPositionPie
      }
    }
  }
}
</script>

<style scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.form-item ::v-deep .el-checkbox__label {
  font-size: 12px;
}

.form-item ::v-deep .el-radio__label {
  font-size: 12px;
}

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}
</style>
