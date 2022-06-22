<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="overallSettingForm" :model="overallSettingForm" label-width="70px" size="mini">
        <el-form-item :label="$t('panel.theme_color')" class="form-item">
          <el-radio-group v-model="overallSettingForm.panel.themeColor" @change="themeChange('themeColor')">
            <el-radio label="light">{{ $t('panel.theme_color_light') }}</el-radio>
            <el-radio label="dark">{{ $t('panel.theme_color_dark') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('panel.component_gap')" class="form-item">
          <el-radio-group v-model="overallSettingForm.panel.gap" size="mini" @change="themeChange()">
            <el-radio label="yes">{{ $t('panel.gap') }}</el-radio>
            <el-radio label="no">{{ $t('panel.no_gap') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('panel.refresh_frequency')" class="form-item">
          <el-input v-model="overallSettingForm.refreshTime" class="el-input-refresh-time" type="number" size="mini" controls-position="right" :min="1" :max="3600" @change="themeChange" />
          <el-select v-model="overallSettingForm.refreshUnit" class="el-input-refresh-unit" @change="themeChange">
            <el-option :label="$t('panel.minute')" :value="'minute'" />
            <el-option :label="$t('panel.second')" :value="'second'" />
          </el-select>
          <el-checkbox v-model="overallSettingForm.refreshViewLoading" class="el-input-refresh-loading" @change="themeChange">提示</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('panel.panel_view_result_show')" class="form-item form-item-result">
          <div style="width: 100%;">
            <el-row>
              <el-col :span="16">
                <el-radio-group v-model="overallSettingForm.panel.resultMode" class="radio-span" size="mini" @change="themeChange">
                  <el-radio label="all"><span>{{ $t('panel.view') }}</span></el-radio>
                  <el-radio label="custom">
                    <span>{{ $t('panel.panel') }} </span>
                  </el-radio>
                </el-radio-group>
              </el-col>
              <el-col :span="8" class="slider-area">
                <el-slider
                  v-model="overallSettingForm.panel.resultCount"
                  :disabled="overallSettingForm.panel.resultMode==='all'"
                  style="margin-left: 5px"
                  show-input
                  :show-input-controls="false"
                  :show-tooltip="false"
                  input-size="mini"
                  :min="1"
                  :max="10000"
                  @change="themeChange"
                />
              </el-col>
            </el-row>
          </div>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import {
  adaptCurThemeCommonStyleAll,
  DARK_THEME_COMPONENT_BACKGROUND,
  DARK_THEME_PANEL_BACKGROUND,
  LIGHT_THEME_COMPONENT_BACKGROUND,
  LIGHT_THEME_PANEL_BACKGROUND
} from '@/components/canvas/utils/style'
import bus from '@/utils/bus'
export default {
  name: 'OverallSetting',
  data() {
    return {
      colorIndex: 0,
      overallSettingForm: {}
    }
  },
  watch: {
    // canvasStyleData: {
    //   handler(newVal, oldVla) {
    //     console.log('canvasStyleData=' + JSON.stringify(this.canvasStyleData))
    //   },
    //   deep: true
    // }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },
  created() {
    this.initForm()
  },
  methods: {
    initForm() {
      this.overallSettingForm = this.canvasStyleData
    },
    themeChange(modifyName) {
      if (modifyName === 'themeColor') {
        // 主题变更
        this.canvasStyleData.chartInfo.chartCommonStyle.backgroundColorSelect = true
        if (this.overallSettingForm.panel.themeColor === 'light') {
          this.canvasStyleData.panel.color = LIGHT_THEME_PANEL_BACKGROUND
          this.canvasStyleData.chartInfo.chartCommonStyle.color = LIGHT_THEME_COMPONENT_BACKGROUND
        } else {
          this.canvasStyleData.panel.color = DARK_THEME_PANEL_BACKGROUND
          this.canvasStyleData.chartInfo.chartCommonStyle.color = DARK_THEME_COMPONENT_BACKGROUND
        }
        adaptCurThemeCommonStyleAll()
        bus.$emit('onThemeColorChange')
      }
      this.$store.commit('recordSnapshot')
    }
  }
}
</script>

<style scoped>

.el-input-refresh-time{
  width: 55px!important;
}

.el-input-refresh-unit{
  width: 55px!important;
}

.el-input-refresh-loading{
  margin-left: 5px;
  font-size: 12px;
}
::v-deep .el-input__inner{
  padding: 0px 5px!important;
}

::v-deep .el-slider__input{
  width: 50px;
  margin-top: 0px;
}

.form-item-slider ::v-deep .el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item  ::v-deep .el-form-item__label{
  font-size: 12px;
}

.slider-area ::v-deep .el-slider__runway {
  display: none;
}

.result-count {
  width: 80px;
}
.form-item-result ::v-deep .el-radio{
  margin-right: 5px;
}

</style>
