<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { DEFAULT_COLOR_CASE } from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import DeSlider from '@/components/dashboard/subject-setting/pre-subject/Slider.vue'
import OverallSetting from '@/components/dashboard/subject-setting/dashboard-style/OverallSetting.vue'
import ComponentColorSelector from '@/components/dashboard/subject-setting/dashboard-style/ComponentColorSelector.vue'
import { adaptCurThemeCommonStyleAll } from '@/utils/canvasStyle'
import ViewSimpleTitle from '@/components/dashboard/subject-setting/dashboard-style/ViewSimpleTitle.vue'
import FilterStyleSimpleSelector from '@/components/dashboard/subject-setting/dashboard-style/FilterStyleSimpleSelector.vue'
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import { deepCopy } from '@/utils/utils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { merge } from 'lodash-es'
import CanvasBackground from '@/components/visualization/component-background/CanvasBackground.vue'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { canvasStyleData, componentData, canvasViewInfo } = storeToRefs(dvMainStore)
const { t } = useI18n()
let canvasAttrInit = false
const canvasAttrActiveNames = ref(['style'])

const state = reactive({
  colorForm: JSON.parse(JSON.stringify(DEFAULT_COLOR_CASE)),
  customColor: null,
  colorIndex: 0,
  sliderShow: true,
  collapseShow: true
})

const onSubjectChange = () => {
  state.collapseShow = false
  nextTick(() => {
    dataMerge()
    state.collapseShow = true
  })
}

const dataMerge = () => {
  adaptCurThemeCommonStyleAll()
  snapshotStore.recordSnapshotCache('renderChart')
}

const onColorChange = val => {
  themeAttrChange('customAttr', 'color', val)
}
const onTextChange = val => {
  themeAttrChange('customStyle', 'text', val)
}
const themeAttrChange = (custom, property, value) => {
  if (canvasAttrInit) {
    Object.keys(canvasViewInfo.value).forEach(function (viewId) {
      const viewInfo = canvasViewInfo.value[viewId]
      try {
        if (custom === 'customAttr') {
          merge(viewInfo['customAttr'], value)
        } else {
          Object.keys(value).forEach(function (key) {
            if (viewInfo[custom][property][key] !== undefined) {
              viewInfo[custom][property][key] = value[key]
            }
          })
        }
        useEmitt().emitter.emit('renderChart-' + viewId, viewInfo)
      } catch (e) {
        console.warn('themeAttrChange-error')
      }
    })
    snapshotStore.recordSnapshotCache('renderChart')
  }
}

const themeColorChange = () => {
  //do themeColorChange
}

const componentBackgroundChange = val => {
  canvasStyleData.value.component.chartCommonStyle = val
  componentData.value.forEach(component => {
    component.commonBackground = deepCopy(val)
  })
}

onMounted(() => {
  useEmitt({
    name: 'onSubjectChange',
    callback: () => {
      onSubjectChange()
    }
  })
  nextTick(() => {
    canvasAttrInit = true
  })
})
const slider = ref()
const saveSelfSubject = () => {
  slider.value.saveSelfSubject()
}
</script>

<template>
  <div class="attr-container">
    <el-row>
      <el-collapse v-model="canvasAttrActiveNames">
        <el-collapse-item title="仪表板风格" name="style">
          <de-slider ref="slider" />
          <el-button class="button-panel__style" text size="small" @click="saveSelfSubject">
            {{ $t('commons.save') }}
          </el-button>
        </el-collapse-item>
        <el-collapse-item title="整体配置" name="overallSetting">
          <overall-setting @onThemeColorChange="themeColorChange" />
        </el-collapse-item>

        <el-collapse-item title="仪表板背景" name="background" class="content-no-padding-bottom">
          <canvas-background themes="light"></canvas-background>
        </el-collapse-item>
        <el-collapse-item
          :title="t('visualization.view_style')"
          name="componentStyle"
          class="content-no-padding-bottom"
        >
          <background-overall-common
            :common-background-pop="canvasStyleData.component.chartCommonStyle"
            component-position="'dashboard'"
            themes="light"
            @onBackgroundChange="componentBackgroundChange"
            :background-color-picker-width="197"
            :background-border-select-width="197"
          />
        </el-collapse-item>
        <el-collapse-item :title="'图表配色'" name="graphical" class="no-padding no-border-bottom">
          <component-color-selector v-if="state.collapseShow" @onColorChange="onColorChange" />
        </el-collapse-item>
        <el-collapse-item :title="t('visualization.chart_title')" name="viewTitle">
          <view-simple-title @onTextChange="onTextChange" />
        </el-collapse-item>
        <el-collapse-item
          :title="t('visualization.filter_component')"
          name="filterComponent"
          class="no-padding no-border-bottom"
        >
          <filter-style-simple-selector />
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.item-show {
  width: 100%;
  display: flex;
  text-align: center;
  padding: 16px 8px 0 8px;
}

.attr-container {
  color: #fff;
  z-index: 20;
  height: 100%;
  width: 100%;
  position: relative;
  .button-panel__style {
    position: absolute;
    top: 4px;
    right: 4px;
    font-size: 12px;
  }
}

:deep(.ed-collapse) {
  width: 100%;
}

.disabled :deep(.el-upload--picture-card) {
  display: none;
}

:deep(.ed-radio__label) {
  color: #1f2329;
  font-size: 12px !important;
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
}

:deep(.ed-radio__input.is-checked + .ed-radio__label) {
  color: #1f2329 !important;
}

:deep(.ed-radio__input.is-disabled + span.ed-radio__label) {
  color: var(--ed-text-color-placeholder) !important;
}

:deep(.ed-collapse-item.ed-collapse--light .ed-collapse-item__content) {
  border: none;
}

.no-padding {
  :deep(.ed-collapse-item__content) {
    padding: 0;
  }
}
.no-border-bottom {
  :deep(.ed-collapse-item__wrap) {
    border-bottom: none;
  }
}
.content-no-padding-bottom {
  :deep(.ed-collapse-item__content) {
    padding-bottom: 0;
  }
}

.avatar-uploader {
  :deep(.ed-upload) {
    width: 80px;
    height: 80px;
    line-height: 90px;
  }

  :deep(.ed-upload-list li) {
    width: 80px !important;
    height: 80px !important;
  }

  :deep(.ed-upload--picture-card) {
    background: #eff0f1;
    border: 1px dashed #dee0e3;
    border-radius: 4px;

    .ed-icon {
      color: #1f2329;
    }

    &:hover {
      .ed-icon {
        color: #3370ff;
      }
    }
  }
}
.img-area {
  width: 120px;
  height: 80px;
  margin-top: 8px;
  overflow: hidden;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.color-label {
  display: inline-block;
  width: 60px;
}

.color-type :deep(.ed-radio__input) {
  display: none;
}

.ed-radio {
  color: #757575;
}

.custom-color-style {
}

.custom-color-style :deep(.ed-radio) {
  margin: 0 2px 0 0 !important;
  border: 1px solid transparent;
}

.custom-color-style :deep(.ed-radio__label) {
  padding-left: 0;
}

.custom-color-style :deep(.ed-radio.is-checked) {
  border: 1px solid #0a7be0;
}

.image-hint {
  color: #8f959e;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
  margin-top: 2px;
}

.re-update-span {
  cursor: pointer;
  color: #3370ff;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

:deep(.ed-collapse-item__header) {
  height: 36px !important;
  line-height: 36px !important;
  font-size: 12px !important;
  padding: 0 !important;
  font-weight: 500 !important;
}

:deep(.ed-checkbox__label) {
  font-size: 12px !important;
}

.ed-radio {
  font-weight: 400;
  height: 20px;
}
.ed-checkbox {
  font-weight: 400;
  height: 20px;
}
.margin-top8 {
  margin-top: 8px !important;
}

:deep(.ed-collapse-item__arrow) {
  margin: 0 6px 0 8px;
}

:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
}

:deep(.ed-form-item) {
  .ed-radio.ed-radio--small .ed-radio__inner {
    width: 14px;
    height: 14px;
  }
  .ed-input__inner {
    font-size: 12px;
    font-weight: 400;
  }
  .ed-input {
    --ed-input-height: 28px;

    .ed-input__suffix {
      height: 26px;
    }
  }
  .ed-input-number {
    width: 100%;

    .ed-input-number__decrease {
      --ed-input-number-controls-height: 13px;
    }
    .ed-input-number__increase {
      --ed-input-number-controls-height: 13px;
    }

    .ed-input__inner {
      text-align: start;
    }
  }
  .ed-select {
    width: 100%;
    .ed-input__inner {
      height: 26px !important;
    }
  }
  .ed-checkbox {
    .ed-checkbox__label {
      font-size: 12px;
    }
  }
  .ed-color-picker {
    .ed-color-picker__mask {
      height: 26px;
      width: calc(100% - 2px) !important;
    }
  }
  .ed-radio {
    height: 20px;
    .ed-radio__label {
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
    }
  }
}
:deep(.ed-checkbox__label) {
  color: #1f2329;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
}
</style>
