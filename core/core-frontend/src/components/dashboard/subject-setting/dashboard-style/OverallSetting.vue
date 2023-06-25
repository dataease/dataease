<template>
  <el-col>
    <el-row class="custom-row">
      <el-row class="custom-item-text-row"
        ><span class="custom-item-text bl">{{ t('visualization.theme_color') }}</span>
      </el-row>
      <el-row class="custom-theme-color-button function-area">
        <color-button
          class="margin-left2"
          :color-type="'light'"
          :label="state.overallSettingForm.dashboard.themeColor"
          @onClick="colorButtonClick"
          >{{ t('visualization.theme_color_light') }}
        </color-button>
        <color-button
          class="margin-left32"
          :color-type="'dark'"
          :label="state.overallSettingForm.dashboard.themeColor"
          @onClick="colorButtonClick"
          >{{ t('visualization.theme_color_dark') }}
        </color-button>
      </el-row>
    </el-row>
    <el-row class="custom-row margin-top16">
      <el-row class="custom-item-text-row"
        ><span class="custom-item-text bl">{{ t('visualization.component_gap') }}</span>
      </el-row>
      <el-row class="function-area">
        <el-radio-group v-model="state.overallSettingForm.dashboard.gap" @change="themeChange">
          <el-radio label="yes">{{ t('visualization.gap') }}</el-radio>
          <el-radio label="no">{{ t('visualization.no_gap') }}</el-radio>
        </el-radio-group>
      </el-row>
    </el-row>
    <el-row class="custom-row margin-top16">
      <el-row class="custom-item-text-row">
        <span class="custom-item-text bl">
          <el-checkbox v-model="state.overallSettingForm.refreshViewEnable" @change="themeChange">{{
            t('visualization.refresh_frequency')
          }}</el-checkbox>
        </span>
      </el-row>
      <el-row class="function-area">
        <el-input-number
          v-model="state.overallSettingForm.refreshTime"
          class="el-input-refresh-time"
          type="number"
          controls-position="right"
          :min="1"
          :max="3600"
          size="small"
          :disabled="!state.overallSettingForm.refreshViewEnable"
          @change="themeChange"
        >
        </el-input-number>
        <el-select
          v-model="state.overallSettingForm.refreshUnit"
          class="el-input-refresh-unit margin-left8"
          size="small"
          :disabled="!state.overallSettingForm.refreshViewEnable"
          @change="themeChange"
        >
          <el-option :label="t('visualization.minute')" :value="'minute'" />
          <el-option :label="t('visualization.second')" :value="'second'" />
        </el-select>
      </el-row>
    </el-row>
    <el-row class="custom-row margin-top16">
      <el-row class="custom-item-text-row">
        <span class="custom-item-text">
          <el-checkbox
            v-model="state.overallSettingForm.refreshViewLoading"
            @change="themeChange"
            >{{ t('visualization.enable_view_loading') }}</el-checkbox
          >
        </span>
      </el-row>
    </el-row>
    <el-row class="custom-row margin-top16">
      <el-row class="custom-item-text-row">
        <span class="custom-item-text bl">
          {{ t('visualization.panel_view_result_show') }}
          <span>
            <el-tooltip class="item" effect="dark" placement="bottom">
              <template #content>
                <div>
                  {{ t('visualization.panel_view_result_tips') }}
                </div>
              </template>
              <i class="el-icon-info" style="cursor: pointer" />
            </el-tooltip>
          </span>
        </span>
      </el-row>
      <el-row class="function-area custom-row">
        <el-row>
          <el-radio-group
            v-model="state.overallSettingForm.dashboard.resultMode"
            class="radio-span"
            @change="themeChange"
          >
            <el-radio label="all"
              ><span>{{ t('visualization.view') }}</span></el-radio
            >
            <el-radio label="custom">
              <span>{{ t('visualization.panel') }} </span>
            </el-radio>
          </el-radio-group>
        </el-row>
        <el-row class="margin-top8">
          <el-input-number
            v-model="state.overallSettingForm.dashboard.resultCount"
            controls-position="right"
            size="small"
            :min="1"
            :max="10000"
            @change="themeChange"
            :disabled="state.overallSettingForm.dashboard.resultMode === 'all'"
          ></el-input-number>
        </el-row>
      </el-row>
    </el-row>
  </el-col>
</template>

<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
import {
  DEFAULT_CANVAS_STYLE_DATA,
  dvMainStoreWithOut
} from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()
import {
  adaptCurThemeCommonStyleAll,
  DARK_THEME_COMPONENT_BACKGROUND,
  DARK_THEME_PANEL_BACKGROUND,
  LIGHT_THEME_COMPONENT_BACKGROUND,
  LIGHT_THEME_PANEL_BACKGROUND
} from '@/utils/canvasStyle'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_COLOR_CASE_DARK,
  DEFAULT_TAB_COLOR_CASE_DARK,
  DEFAULT_TAB_COLOR_CASE_LIGHT,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TITLE_STYLE_DARK,
  FILTER_COMMON_STYLE_DARK,
  FILTER_COMMON_STYLE
} from '@/views/chart/components/editor/util/chart'
import ColorButton from '@/components/assist-button/ColorButton.vue'
import { reactive } from 'vue'
import { deepCopy } from '@/utils/utils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const emits = defineEmits(['onThemeColorChange'])
const snapshotStore = snapshotStoreWithOut()

const state = reactive({
  colorIndex: 0,
  overallSettingForm: deepCopy(DEFAULT_CANVAS_STYLE_DATA)
})

const initForm = () => {
  state.overallSettingForm = dvMainStore.canvasStyleData.value
}
const themeChange = modifyName => {
  if (modifyName === 'themeColor') {
    // 主题变更
    dvMainStore.canvasStyleData.component.chartCommonStyle.backgroundColorSelect = true
    dvMainStore.canvasStyleData.dashboard.backgroundType = 'color'
    if (state.overallSettingForm.dashboard.themeColor === 'light') {
      dvMainStore.canvasStyleData.dashboard.color = deepCopy(LIGHT_THEME_PANEL_BACKGROUND)
      dvMainStore.canvasStyleData.component.chartCommonStyle.color = deepCopy(
        LIGHT_THEME_COMPONENT_BACKGROUND
      )
      dvMainStore.canvasStyleData.component.chartTitle = deepCopy(DEFAULT_TITLE_STYLE)
      dvMainStore.canvasStyleData.component.chartColor = deepCopy(DEFAULT_COLOR_CASE)
      dvMainStore.canvasStyleData.component.filterStyle = deepCopy(FILTER_COMMON_STYLE)
      dvMainStore.canvasStyleData.component.tabStyle = deepCopy(DEFAULT_TAB_COLOR_CASE_LIGHT)
    } else {
      dvMainStore.canvasStyleData.dashboard.color = deepCopy(DARK_THEME_PANEL_BACKGROUND)
      dvMainStore.canvasStyleData.component.chartCommonStyle.color = deepCopy(
        DARK_THEME_COMPONENT_BACKGROUND
      )
      dvMainStore.canvasStyleData.component.chartTitle = deepCopy(DEFAULT_TITLE_STYLE_DARK)
      dvMainStore.canvasStyleData.component.chartColor = deepCopy(DEFAULT_COLOR_CASE_DARK)
      dvMainStore.canvasStyleData.component.filterStyle = deepCopy(FILTER_COMMON_STYLE_DARK)
      dvMainStore.canvasStyleData.component.tabStyle = deepCopy(DEFAULT_TAB_COLOR_CASE_DARK)
    }
    adaptCurThemeCommonStyleAll()
    emits('onThemeColorChange')
  }
  snapshotStore.recordSnapshot()
}
const colorButtonClick = val => {
  if (val !== state.overallSettingForm.dashboard.themeColor) {
    state.overallSettingForm.dashboard.themeColor = val
    themeChange('themeColor')
  } else {
    state.overallSettingForm.dashboard.themeColor = val
  }
}
</script>

<style scoped lang="less">
.el-input-refresh-time {
  width: calc(100% - 90px) !important;
}

.el-input-refresh-unit {
  width: 80px !important;
}

.margin-left4 {
  margin-left: 4px;
}

.margin-left8 {
  margin-left: 8px;
}

.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

.slider-area :deep(.ed-slider__runway) {
  display: none;
}

.result-count {
  width: 80px;
}

.custom-item-text-row {
  display: flex;
}

.custom-item-text {
  font-size: 12px !important;
  font-weight: 400 !important;
  line-height: 20px;
  color: #646a73 !important;
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

.margin-top16 {
  margin-top: 16px !important;
}

.margin-top8 {
  margin-top: 8px !important;
}
.ed-radio {
  font-weight: 400;
  height: 20px;
}
.ed-checkbox {
  font-weight: 400;
  height: 20px;
}

:deep(.ed-input-number) {
  width: 100%;
}

:deep(.ed-input__inner) {
  text-align: left;
}
</style>
