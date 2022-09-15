<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini">
        <div>
          <el-form-item :label="$t('chart.color_case')" class="form-item">
            <el-popover
              placement="bottom"
              width="400"
              trigger="click"
            >
              <div style="padding: 6px 10px;">
                <div>
                  <span class="color-label">{{ $t('chart.system_case') }}</span>
                  <el-select v-model="colorForm.value" :placeholder="$t('chart.pls_slc_color_case')" size="mini" @change="changeColorOption('value')">
                    <el-option v-for="option in colorCases" :key="option.value" :label="option.name" :value="option.value" style="display: flex;align-items: center;">
                      <div style="float: left">
                        <span v-for="(c,index) in option.colors" :key="index" :style="{width: '20px',height: '20px',float: 'left',backgroundColor: c}" />
                      </div>
                      <span style="margin-left: 4px;">{{ option.name }}</span>
                    </el-option>
                  </el-select>
                  <el-button size="mini" type="text" style="margin-left: 2px;" @click="resetCustomColor">{{ $t('commons.reset') }}</el-button>
                </div>
                <!--自定义配色方案-->
                <div>
                  <div style="display: flex;align-items: center;margin-top: 10px;">
                    <span class="color-label">{{ $t('chart.custom_case') }}</span>
                    <span>
                      <el-radio-group v-model="customColor" class="color-type">
                        <el-radio v-for="(c,index) in colorForm.colors" :key="index" :label="c" style="padding: 2px;" @change="switchColor(index)">
                          <span :style="{width: '20px',height: '20px',display:'inline-block',backgroundColor: c}" />
                        </el-radio>
                      </el-radio-group>
                    </span>
                  </div>
                  <div style="display: flex;align-items: center;margin-top: 10px;">
                    <span class="color-label" />
                    <span>
                      <el-color-picker v-model="customColor" class="color-picker-style" :predefine="predefineColors" @change="switchColorCase" />
                    </span>
                  </div>
                </div>
              </div>

              <div slot="reference" style="cursor: pointer;margin-top: 2px;width: 180px;">
                <span v-for="(c,index) in colorForm.colors" :key="index" :style="{width: '20px',height: '20px',display:'inline-block',backgroundColor: c}" />
              </div>
            </el-popover>
          </el-form-item>
          <el-form-item :label="$t('chart.not_alpha')" class="form-item form-item-slider">
            <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeColorCase('alpha')" />
          </el-form-item>
          <el-divider>{{ $t('panel.card_color_matching') }}</el-divider>
          <el-form-item :label="$t('chart.quota_color')" class="form-item">
            <el-color-picker v-model="colorForm.quotaColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('quotaColor')" />
          </el-form-item>
          <el-form-item :label="$t('chart.dimension_color')" class="form-item">
            <el-color-picker v-model="colorForm.dimensionColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('dimensionColor')" />
          </el-form-item>
        </div>
        <el-divider>{{ $t('panel.table_color_matching') }}</el-divider>
        <div>
          <el-form-item :label="$t('chart.table_header_bg')" class="form-item">
            <el-color-picker v-model="colorForm.tableHeaderBgColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('tableHeaderBgColor')" />
          </el-form-item>
          <el-form-item :label="$t('chart.table_item_bg')" class="form-item">
            <el-color-picker v-model="colorForm.tableItemBgColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('tableItemBgColor')" />
          </el-form-item>
          <el-form-item :label="$t('chart.table_header_font_color')" class="form-item">
            <el-color-picker v-model="colorForm.tableHeaderFontColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('tableHeaderFontColor')" />
          </el-form-item>
          <el-form-item :label="$t('chart.table_item_font_color')" class="form-item">
            <el-color-picker v-model="colorForm.tableFontColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('tableFontColor')" />
          </el-form-item>
          <el-form-item :label="$t('chart.table_border_color')" class="form-item">
            <el-color-picker v-model="colorForm.tableBorderColor" class="color-picker-style" :predefine="predefineColors" @change="changeColorCase('tableBorderColor')" />
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { mapState } from 'vuex'
import bus from '@/utils/bus'

export default {
  name: 'ColorSelector',
  props: {
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
        },
        {
          name: this.$t('chart.color_light'),
          value: 'light',
          colors: ['#884898', '#808080', '#82ae46', '#00a3af', '#ef8b07', '#007bbb', '#9d775f', '#fae800', '#5f9b3c']
        },
        {
          name: this.$t('chart.color_classical'),
          value: 'classical',
          colors: ['#007bbb', '#ffdb4f', '#dd4b4b', '#2ca9e1', '#ef8b07', '#4a488e', '#82ae46', '#dd4b4b', '#bb9581']
        },
        {
          name: this.$t('chart.color_fresh'),
          value: 'fresh',
          colors: ['#5f9b3c', '#75c24b', '#83d65f', '#aacf53', '#c7dc68', '#d8e698', '#e0ebaf', '#bbc8e6', '#e5e5e5']
        },
        {
          name: this.$t('chart.color_energy'),
          value: 'energy',
          colors: ['#ef8b07', '#2a83a2', '#f07474', '#c55784', '#274a78', '#7058a3', '#0095d9', '#75c24b', '#808080']
        },
        {
          name: this.$t('chart.color_red'),
          value: 'red',
          colors: ['#ff0000', '#ef8b07', '#4c6cb3', '#f8e944', '#69821b', '#9c5ec3', '#00ccdf', '#f07474', '#bb9581']
        },
        {
          name: this.$t('chart.color_fast'),
          value: 'fast',
          colors: ['#fae800', '#00c039', '#0482dc', '#bb9581', '#ff7701', '#9c5ec3', '#00ccdf', '#00c039', '#ff7701']
        },
        {
          name: this.$t('chart.color_spiritual'),
          value: 'spiritual',
          colors: ['#00a3af', '#4da798', '#57baaa', '#62d0bd', '#6ee4d0', '#86e7d6', '#aeede1', '#bde1e6', '#e5e5e5']
        }
      ],
      colorForm: {},
      customColor: null,
      colorIndex: 0,
      predefineColors: COLOR_PANEL
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),
  created() {
    this.initForm()
    bus.$on('onThemeColorChange', this.initForm)
  },
  beforeDestroy() {
    bus.$off('onThemeColorChange', this.initForm)
  },
  mounted() {
  },
  methods: {
    initForm() {
      this.colorForm = this.canvasStyleData.chartInfo.chartColor
      this.colorForm.tableHeaderFontColor = this.colorForm.tableHeaderFontColor ? this.colorForm.tableHeaderFontColor : this.colorForm.tableFontColor
    },
    changeColorOption(modifyName = 'value') {
      const that = this
      const items = this.colorCases.filter(ele => {
        return ele.value === that.colorForm.value
      })
      this.colorForm.colors = JSON.parse(JSON.stringify(items[0].colors))

      this.customColor = this.colorForm.colors[0]
      this.colorIndex = 0

      // reset custom color
      this.colorForm.seriesColors = []
      this.changeColorCase(modifyName)
    },
    changeColorCase(modifyName) {
      this.colorForm['modifyName'] = modifyName
      this.$emit('onColorChange', this.colorForm)
      this.colorForm['modifyName'] = 'colors'
      this.$emit('onColorChange', this.colorForm)
    },

    switchColor(index) {
      this.colorIndex = index
    },
    switchColorCase() {
      this.colorForm.colors[this.colorIndex] = this.customColor
      this.colorForm['modifyName'] = 'value'
      this.$emit('onColorChange', this.colorForm)
      this.colorForm['modifyName'] = 'colors'
      this.$emit('onColorChange', this.colorForm)
    },
    resetCustomColor() {
      this.changeColorOption()
    },

    switchCustomColor(index) {
      this.colorForm.seriesColors[index].isCustom = true
      this.switchColorCase()
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
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}
.color-label{
  display: inline-block;
  width: 60px;
}

.color-type ::v-deep .el-radio__input{
  display: none;
}
.el-radio{
  margin:0 2px 0 0!important;
  border: 1px solid transparent;
}
.el-radio ::v-deep .el-radio__label{
  padding-left: 0;
}

.el-radio.is-checked{
  border: 1px solid #0a7be0;
}

.span-label {
  width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  padding: 0 8px;
}

.custom-color-style {
  height: 300px;
  overflow-y: auto;
  padding: 4px 12px;
  border: 1px solid #e6e6e6;
}

.el-divider__text{
  font-size: 8px;
  font-weight: 400;
  color: rgb(144, 147, 153);
}
</style>
