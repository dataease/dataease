<template>
  <div>
    <div style="width: 100%">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini">
            <div v-if="sourceType==='view' || sourceType==='panelEchart'">
              <el-form-item v-show="chart.type && !chart.type.includes('table') && !chart.type.includes('text')" :label="$t('chart.color_case')" class="form-item">
                <el-select v-model="colorForm.value" :placeholder="$t('chart.pls_slc_color_case')" size="mini" @change="changeColorCase">
                  <el-option v-for="option in colorCases" :key="option.value" :label="option.name" :value="option.value" style="display: flex;align-items: center;">
                    <div style="float: left">
                      <span v-for="(c,index) in option.colors" :key="index" :style="{width: '20px',height: '20px',float: 'left',backgroundColor: c}" />
                    </div>
                    <span style="margin-left: 4px;">{{ option.name }}</span>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item v-show="(chart.type && chart.type.includes('text')) || sourceType==='panelTable'" :label="$t('chart.dimension_color')" class="form-item">
                <colorPicker v-model="colorForm.dimensionColor" style="margin-top: 6px;cursor: pointer;z-index: 1003;border: solid 1px black" @change="changeColorCase" />
              </el-form-item>
              <el-form-item v-show="(chart.type && chart.type.includes('text')) || sourceType==='panelTable'" :label="$t('chart.quota_color')" class="form-item">
                <colorPicker v-model="colorForm.quotaColor" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeColorCase" />
              </el-form-item>
            </div>
            <div v-if="sourceType==='view' || sourceType==='panelTable'">
              <el-form-item v-show="(chart.type && chart.type.includes('table')) || sourceType==='panelTable'" :label="$t('chart.table_header_bg')" class="form-item">
                <colorPicker v-model="colorForm.tableHeaderBgColor" style="margin-top: 6px;cursor: pointer;z-index: 1002;border: solid 1px black" @change="changeColorCase" />
              </el-form-item>
              <el-form-item v-show="(chart.type && chart.type.includes('table')) || sourceType==='panelTable'" :label="$t('chart.table_item_bg')" class="form-item">
                <colorPicker v-model="colorForm.tableItemBgColor" style="margin-top: 6px;cursor: pointer;z-index: 1003;border: solid 1px black" @change="changeColorCase" />
              </el-form-item>
              <el-form-item v-show="(chart.type && chart.type.includes('table')) || sourceType==='panelTable'" :label="$t('chart.table_item_font_color')" class="form-item">
                <colorPicker v-model="colorForm.tableFontColor" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeColorCase" />
              </el-form-item>
              <!--              暂时不支持该功能-->
              <!--              <el-form-item v-show="(chart.type && chart.type.includes('table')) || sourceType==='panelTable'" :label="$t('chart.stripe')" class="form-item">-->
              <!--                <el-checkbox v-model="colorForm.tableStripe" @change="changeColorCase">{{ $t('chart.stripe') }}</el-checkbox>-->
              <!--              </el-form-item>-->
            </div>

            <el-form-item v-show="chart.type && !chart.type.includes('text')" :label="$t('chart.not_alpha')" class="form-item form-item-slider">
              <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeColorCase" />
            </el-form-item>
          </el-form>
        </el-col>

        <el-button slot="reference" :disabled="param && !hasDataPermission('manage',param.privileges)" size="mini" class="shape-item">{{ $t('chart.color') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_COLOR_CASE } from '../../chart/chart'

export default {
  name: 'ColorSelector',
  props: {
    param: {
      type: Object,
      required: false
    },
    chart: {
      type: Object,
      required: true
    },
    sourceType: {
      type: String,
      default: 'view',
      required: false
    }
  },
  data() {
    return {
      colorCases: [
        {
          name: this.$t('chart.color_default'),
          value: 'default',
          colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
        },
        {
          name: this.$t('chart.color_retro'),
          value: 'retro',
          colors: ['#0780cf', '#765005', '#fa6d1d', '#0e2c82', '#b6b51f', '#da1f18', '#701866', '#f47a75', '#009db2']
        },
        {
          name: this.$t('chart.color_elegant'),
          value: 'elegant',
          colors: ['#95a2ff', '#fa8080', '#ffc076', '#fae768', '#87e885', '#3cb9fc', '#73abf5', '#cb9bff', '#434348']
        },
        {
          name: this.$t('chart.color_future'),
          value: 'future',
          colors: ['#63b2ee', '#76da91', '#f8cb7f', '#f89588', '#7cd6cf', '#9192ab', '#7898e1', '#efa666', '#eddd86']
        },
        {
          name: this.$t('chart.color_gradual'),
          value: 'gradual',
          colors: ['#71ae46', '#96b744', '#c4cc38', '#ebe12a', '#eab026', '#e3852b', '#d85d2a', '#ce2626', '#ac2026']
        },
        {
          name: this.$t('chart.color_simple'),
          value: 'simple',
          colors: ['#929fff', '#9de0ff', '#ffa897', '#af87fe', '#7dc3fe', '#bb60b2', '#433e7c', '#f47a75', '#009db2']
        },
        {
          name: this.$t('chart.color_business'),
          value: 'business',
          colors: ['#194f97', '#555555', '#bd6b08', '#00686b', '#c82d31', '#625ba1', '#898989', '#9c9800', '#007f54']
        },
        {
          name: this.$t('chart.color_gentle'),
          value: 'gentle',
          colors: ['#5b9bd5', '#ed7d31', '#70ad47', '#ffc000', '#4472c4', '#91d024', '#b235e6', '#02ae75', '#5b9bd5']
        },
        {
          name: this.$t('chart.color_technology'),
          value: 'technology',
          colors: ['#05f8d6', '#0082fc', '#fdd845', '#22ed7c', '#09b0d3', '#1d27c9', '#f9e264', '#f47a75', '#009db2']
        }
      ],
      colorForm: JSON.parse(JSON.stringify(DEFAULT_COLOR_CASE))
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.init()
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    changeColorCase() {
      const that = this
      const items = this.colorCases.filter(ele => {
        return ele.value === that.colorForm.value
      })
      const val = JSON.parse(JSON.stringify(this.colorForm))
      val.value = items[0].value
      val.colors = items[0].colors
      this.$emit('onColorChange', val)
    },
    init() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.color) {
          this.colorForm = customAttr.color
        }
      }
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
  align-items: center
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
