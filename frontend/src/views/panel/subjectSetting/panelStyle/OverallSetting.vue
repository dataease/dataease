<template>
  <div style="width: 100%">
    <el-col>
      <el-row class="margin-top12">
        <el-row class="custom-item-text-row"><span class="custom-item-text bl">{{ $t('panel.theme_color') }}</span>
        </el-row>
        <el-row class="custom-theme-color-button function-area">
          <color-button
            class="margin-left2"
            :color-type="'light'"
            :label="overallSettingForm.panel.themeColor"
            @onClick="colorButtonClick"
          >{{ $t('panel.theme_color_light') }}
          </color-button>
          <color-button
            class="margin-left32"
            :color-type="'dark'"
            :label="overallSettingForm.panel.themeColor"
            @onClick="colorButtonClick"
          >{{ $t('panel.theme_color_dark') }}
          </color-button>
        </el-row>
      </el-row>
      <el-row class="margin-top20">
        <el-row class="custom-item-text-row"><span class="custom-item-text bl">{{ $t('panel.component_gap') }}</span>
        </el-row>
        <el-row class="function-area">
          <el-radio-group
            v-model="overallSettingForm.panel.gap"
            size="mini"
            @change="themeChange()"
          >
            <el-radio label="yes">{{ $t('panel.gap') }}</el-radio>
            <el-radio label="no">{{ $t('panel.no_gap') }}</el-radio>
          </el-radio-group>
        </el-row>
      </el-row>
      <el-row class="margin-top20">
        <el-row class="custom-item-text-row">
          <span class="custom-item-text bl"> {{ $t('panel.refresh_frequency') }}</span>
          <span class="custom-item-text bl">
            <el-checkbox
              v-model="overallSettingForm.refreshViewEnable"
              class="el-input-refresh-loading"
              @change="themeChange"
            >{{ $t('panel.enable_refresh_view') }}</el-checkbox>
          </span>
          <span class="custom-item-text br">
            <el-checkbox
              v-model="overallSettingForm.refreshViewLoading"
              class="el-input-refresh-loading"
              @change="themeChange"
            >{{ $t('panel.enable_view_loading') }}</el-checkbox>
          </span>
        </el-row>
        <el-row class="function-area">
          <el-input
            v-model="overallSettingForm.refreshTime"
            class="el-input-refresh-time"
            type="number"
            size="mini"
            controls-position="right"
            :min="1"
            :max="3600"
            :disabled="!overallSettingForm.refreshViewEnable"
            @change="themeChange"
          />
          <el-select
            v-model="overallSettingForm.refreshUnit"
            class="el-input-refresh-unit margin-left8"
            size="mini"
            :disabled="!overallSettingForm.refreshViewEnable"
            @change="themeChange"
          >
            <el-option
              :label="$t('panel.minute')"
              :value="'minute'"
            />
            <el-option
              :label="$t('panel.second')"
              :value="'second'"
            />
          </el-select>
        </el-row>
      </el-row>
      <el-row class="margin-top20 margin-bottom20">
        <el-row class="custom-item-text-row">
          <span class="custom-item-text bl">
            {{ $t('panel.panel_view_result_show') }}
            <span>
              <el-tooltip
                class="item"
                effect="dark"
                placement="bottom"
              >
                <div slot="content">
                  {{ $t('panel.panel_view_result_tips') }}
                </div>
                <i
                  class="el-icon-info"
                  style="cursor: pointer;"
                />
              </el-tooltip>
            </span>
          </span>
        </el-row>
        <el-row class="function-area">
          <el-row>
            <el-col :span="16">
              <el-radio-group
                v-model="overallSettingForm.panel.resultMode"
                class="radio-span"
                size="mini"
                @change="themeChange"
              >
                <el-radio label="all"><span>{{ $t('panel.view') }}</span></el-radio>
                <el-radio label="custom">
                  <span>{{ $t('panel.panel') }} </span>
                </el-radio>
              </el-radio-group>
            </el-col>
            <el-col
              :span="8"
              class="slider-area"
            >
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
        </el-row>
      </el-row>
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
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_COLOR_CASE_DARK,
  DEFAULT_TAB_COLOR_CASE_DARK,
  DEFAULT_TAB_COLOR_CASE_LIGHT,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TITLE_STYLE_DARK
} from '@/views/chart/chart/chart'
import { FILTER_COMMON_STYLE, FILTER_COMMON_STYLE_DARK } from '@/views/panel/panel'
import { deepCopy } from '@/components/canvas/utils/utils'
import ColorButton from '@/components/assistButton/ColorButton'

export default {
  name: 'OverallSetting',
  components: { ColorButton },
  data() {
    return {
      colorIndex: 0,
      overallSettingForm: {}
    }
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
        this.canvasStyleData.panel.backgroundType = 'color'
        if (this.overallSettingForm.panel.themeColor === 'light') {
          this.canvasStyleData.panel.color = deepCopy(LIGHT_THEME_PANEL_BACKGROUND)
          this.canvasStyleData.chartInfo.chartCommonStyle.color = deepCopy(LIGHT_THEME_COMPONENT_BACKGROUND)
          this.canvasStyleData.chartInfo.chartTitle = deepCopy(DEFAULT_TITLE_STYLE)
          this.canvasStyleData.chartInfo.chartColor = deepCopy(DEFAULT_COLOR_CASE)
          this.canvasStyleData.chartInfo.filterStyle = deepCopy(FILTER_COMMON_STYLE)
          this.canvasStyleData.chartInfo.tabStyle = deepCopy(DEFAULT_TAB_COLOR_CASE_LIGHT)
        } else {
          this.canvasStyleData.panel.color = deepCopy(DARK_THEME_PANEL_BACKGROUND)
          this.canvasStyleData.chartInfo.chartCommonStyle.color = deepCopy(DARK_THEME_COMPONENT_BACKGROUND)
          this.canvasStyleData.chartInfo.chartTitle = deepCopy(DEFAULT_TITLE_STYLE_DARK)
          this.canvasStyleData.chartInfo.chartColor = deepCopy(DEFAULT_COLOR_CASE_DARK)
          this.canvasStyleData.chartInfo.filterStyle = deepCopy(FILTER_COMMON_STYLE_DARK)
          this.canvasStyleData.chartInfo.tabStyle = deepCopy(DEFAULT_TAB_COLOR_CASE_DARK)
        }
        adaptCurThemeCommonStyleAll()
        bus.$emit('onThemeColorChange')
      }
      this.$store.commit('recordSnapshot')
    },
    colorButtonClick(val) {
      if (val !== this.overallSettingForm.panel.themeColor) {
        this.overallSettingForm.panel.themeColor = val
        this.themeChange('themeColor')
      } else {
        this.overallSettingForm.panel.themeColor = val
      }
    }
  }
}
</script>

<style scoped>

.el-input-refresh-time {
  width: calc(50% - 4px) !important;
}

.el-input-refresh-unit {
  width: calc(50% - 4px) !important;
}

.el-input-refresh-loading {
  margin-left: 4px;
  font-size: 12px;
}

.margin-left4 {
  margin-left: 4px;
}

.margin-left8 {
  margin-left: 8px;
}

::v-deep .el-input__inner {
  padding: 0px 5px !important;
}

::v-deep .el-slider__input {
  width: 100px;
  margin-top: 0px;
}

.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.slider-area ::v-deep .el-slider__runway {
  display: none;
}

.result-count {
  width: 80px;
}

.form-item-result ::v-deep .el-radio {
  margin-right: 5px;
}

.custom-item-text-row {
  display: flex;
}

.custom-item-text {
  font-weight: 400 !important;
  font-size: 14px !important;
  color: var(--TextPrimary, #1F2329) !important;
  line-height: 22px;
}

.custom-theme-color-button {
  float: left;
}

.margin-left2 {
  margin-left: 2px;
}

.margin-left32 {
  margin-left: 32px;
}

.bl {
  justify-content: flex-start;
  display: flex;
}

.br {
  flex: 1;
  justify-content: flex-end;
  display: flex;
}

.function-area {
  margin-top: 8px;
}

.margin-top12 {
  margin-top: 12px !important;
}

.margin-top20 {
  margin-top: 20px !important;
}

.margin-bottom20 {
  margin-bottom: 20px !important;

}
</style>
