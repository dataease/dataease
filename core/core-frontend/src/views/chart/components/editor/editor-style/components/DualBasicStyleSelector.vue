<script setup lang="ts">
import { onMounted, PropType, reactive, watch, ref } from 'vue'
import { COLOR_PANEL, DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import CustomColorStyleSelect from '@/views/chart/components/editor/editor-style/components/CustomColorStyleSelect.vue'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import {
  CHART_MIX_DEFAULT_BASIC_STYLE,
  MixChartBasicStyle
} from '@/views/chart/components/js/panel/charts/others/chart-mix-common'

const dvMainStore = dvMainStoreWithOut()
const { batchOptStatus } = storeToRefs(dvMainStore)
const { t } = useI18n()
const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})

const showProperty = prop => props.propertyInner?.includes(prop)
const predefineColors = COLOR_PANEL
const state = reactive({
  basicStyleForm: JSON.parse(JSON.stringify(CHART_MIX_DEFAULT_BASIC_STYLE)) as MixChartBasicStyle,
  miscForm: JSON.parse(JSON.stringify(DEFAULT_MISC)) as ChartMiscAttr,
  customColor: null,
  colorIndex: 0,
  fieldColumnWidth: {
    fieldId: '',
    width: 0
  }
})
watch(
  [
    () => props.chart.customAttr.basicStyle,
    () => props.chart.customAttr.misc,
    () => props.chart.customAttr.tableHeader,
    () => props.chart.xAxis,
    () => props.chart.yAxis
  ],
  () => {
    init()
  },
  { deep: true }
)
const emit = defineEmits(['onBasicStyleChange', 'onMiscChange'])
const changeBasicStyle = (prop?: string, requestData = false) => {
  emit('onBasicStyleChange', { data: state.basicStyleForm, requestData }, prop)
}
const onAlphaChange = v => {
  const _v = parseInt(v)
  if (_v >= 0 && _v <= 100) {
    state.basicStyleForm.alpha = _v
  } else if (_v < 0) {
    state.basicStyleForm.alpha = 0
  } else if (_v > 100) {
    state.basicStyleForm.alpha = 100
  } else {
    const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
    const oldForm = defaultsDeep(
      basicStyle,
      cloneDeep(CHART_MIX_DEFAULT_BASIC_STYLE)
    ) as ChartBasicStyle
    state.basicStyleForm.alpha = oldForm.alpha
  }
  changeBasicStyle('alpha')
}
const onSubAlphaChange = v => {
  const _v = parseInt(v)
  if (_v >= 0 && _v <= 100) {
    state.basicStyleForm.subAlpha = _v
  } else if (_v < 0) {
    state.basicStyleForm.subAlpha = 0
  } else if (_v > 100) {
    state.basicStyleForm.subAlpha = 100
  } else {
    const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
    const oldForm = defaultsDeep(
      basicStyle,
      cloneDeep(CHART_MIX_DEFAULT_BASIC_STYLE)
    ) as MixChartBasicStyle
    state.basicStyleForm.subAlpha = oldForm.subAlpha
  }
  changeBasicStyle('alpha')
}

const init = () => {
  const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
  const miscStyle = cloneDeep(props.chart.customAttr.misc)
  configCompat(basicStyle)
  state.basicStyleForm = defaultsDeep(
    basicStyle,
    cloneDeep(CHART_MIX_DEFAULT_BASIC_STYLE)
  ) as MixChartBasicStyle
  state.miscForm = defaultsDeep(miscStyle, cloneDeep(DEFAULT_MISC)) as ChartMiscAttr
  if (!state.customColor) {
    state.customColor = state.basicStyleForm.colors[0]
    state.colorIndex = 0
  }
}
const configCompat = (basicStyle: ChartBasicStyle) => {
  // 悬浮改为图例和缩放按钮
  if (basicStyle.suspension === false && basicStyle.showZoom === undefined) {
    basicStyle.showZoom = false
  }
}
const symbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const activeName = ref<'left' | 'right'>('left')

onMounted(() => {
  init()
})
</script>
<template>
  <div style="width: 100%">
    <el-tabs v-model="activeName" id="axis-tabs" stretch>
      <el-tab-pane :label="t('chart.yAxisLeft')" name="left">
        <template v-if="showProperty('colors')">
          <custom-color-style-select
            v-model="state"
            :chart="chart"
            :themes="themes"
            :property-inner="propertyInner"
            @change-basic-style="prop => changeBasicStyle(prop)"
          />
        </template>

        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('gradient')"
        >
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="state.basicStyleForm.gradient"
            @change="changeBasicStyle('gradient')"
          >
            {{ $t('chart.gradient') }}{{ $t('chart.color') }}
          </el-checkbox>
        </el-form-item>

        <div class="alpha-setting" v-if="showProperty('alpha')">
          <label class="alpha-label" :class="{ dark: 'dark' === themes }">
            {{ t('chart.not_alpha') }}
          </label>
          <el-row style="flex: 1" :gutter="8">
            <el-col :span="13">
              <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
                <el-slider
                  :effect="themes"
                  v-model="state.basicStyleForm.alpha"
                  @change="changeBasicStyle('alpha')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="11" style="padding-top: 2px">
              <el-form-item class="form-item" :class="'form-item-' + themes">
                <el-input
                  type="number"
                  :effect="themes"
                  v-model="state.basicStyleForm.alpha"
                  :min="0"
                  :max="100"
                  class="basic-input-number"
                  :controls="false"
                  @change="onAlphaChange"
                >
                  <template #suffix> % </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-form-item
          class="form-item"
          v-if="showProperty('radiusColumnBar')"
          :label="t('chart.radiusColumnBar')"
          :class="'form-item-' + themes"
        >
          <el-radio-group
            size="small"
            :effect="themes"
            v-model="state.basicStyleForm.radiusColumnBar"
            @change="changeBasicStyle('radiusColumnBar')"
          >
            <el-radio label="rightAngle" :effect="themes">{{ t('chart.rightAngle') }}</el-radio>
            <el-radio label="roundAngle" :effect="themes">{{ t('chart.roundAngle') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-tab-pane>
      <el-tab-pane :label="t('chart.yAxisRight')" name="right">
        <template v-if="showProperty('colors')">
          <custom-color-style-select
            sub
            v-model="state"
            :chart="chart"
            :themes="themes"
            :property-inner="propertyInner"
            @change-basic-style="prop => changeBasicStyle(prop)"
          />
        </template>

        <div class="alpha-setting" v-if="showProperty('alpha')">
          <label class="alpha-label" :class="{ dark: 'dark' === themes }">
            {{ t('chart.not_alpha') }}
          </label>
          <el-row style="flex: 1" :gutter="8">
            <el-col :span="13">
              <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
                <el-slider
                  :effect="themes"
                  v-model="state.basicStyleForm.subAlpha"
                  @change="changeBasicStyle('alpha')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="11" style="padding-top: 2px">
              <el-form-item class="form-item" :class="'form-item-' + themes">
                <el-input
                  type="number"
                  :effect="themes"
                  v-model="state.basicStyleForm.subAlpha"
                  :min="0"
                  :max="100"
                  class="basic-input-number"
                  :controls="false"
                  @change="onSubAlphaChange"
                >
                  <template #suffix> % </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item
              :label="t('chart.line_width')"
              class="form-item form-item-slider"
              :class="'form-item-' + themes"
              v-if="showProperty('lineWidth')"
            >
              <el-input-number
                :effect="themes"
                v-model="state.basicStyleForm.lineWidth"
                :min="0"
                :max="10"
                controls-position="right"
                @change="changeBasicStyle('lineWidth')"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item
              :label="t('chart.line_symbol')"
              class="form-item"
              :class="'form-item-' + themes"
              v-if="showProperty('lineSymbol')"
            >
              <el-select
                :effect="themes"
                v-model="state.basicStyleForm.lineSymbol"
                :placeholder="t('chart.line_symbol')"
                @change="changeBasicStyle('lineSymbol')"
              >
                <el-option
                  v-for="item in symbolOptions"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="t('chart.line_symbol_size')"
              class="form-item form-item-slider"
              :class="'form-item-' + themes"
              v-if="showProperty('lineSymbolSize')"
            >
              <el-input-number
                :effect="themes"
                v-model="state.basicStyleForm.lineSymbolSize"
                :min="0"
                :max="20"
                controls-position="right"
                @change="changeBasicStyle('lineSymbolSize')"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('lineSmooth')"
        >
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="state.basicStyleForm.lineSmooth"
            @change="changeBasicStyle('lineSmooth')"
          >
            {{ t('chart.line_smooth') }}
          </el-checkbox>
        </el-form-item>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped lang="less">
.form-item {
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.alpha-setting {
  display: flex;
  width: 100%;

  .alpha-slider {
    padding: 0 8px;
    :deep(.ed-slider__button-wrapper) {
      --ed-slider-button-wrapper-size: 36px;
      --ed-slider-button-size: 16px;
    }
  }

  .alpha-label {
    padding-right: 8px;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    height: 32px;
    line-height: 32px;
    display: inline-flex;
    align-items: flex-start;

    min-width: 56px;

    &.dark {
      color: #a6a6a6;
    }
  }
}
.table-field-width-config {
  .ed-select {
    width: 100px !important;
    :deep(.ed-input__wrapper) {
      border-radius: 4px 0 0 4px !important;
    }
  }
  .ed-input-group {
    width: 120px;
    :deep(.ed-input__wrapper) {
      border-radius: 0 !important;
    }
    :deep(.ed-input-group__append) {
      padding: 0 8px;
    }
  }
}
.table-column-mode {
  :deep(.ed-radio) {
    margin-right: 10px !important;
  }
}
.basic-input-number {
  :deep(input) {
    -webkit-appearance: none;
    -moz-appearance: textfield;

    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      -webkit-appearance: none;
    }
  }
}
.top-n-setting {
  .ed-input-number {
    width: 80px !important;
    margin: 0 2px;
  }
  :deep(span) {
    font-size: 12px;
  }
}
#axis-tabs {
  margin-top: -16px;
  --ed-tabs-header-height: 34px;

  :deep(.ed-tabs__header) {
    border-top: none !important;
  }
}
</style>
