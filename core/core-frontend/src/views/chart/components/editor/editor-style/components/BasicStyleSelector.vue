<script setup lang="ts">
import { PropType, reactive, watch } from 'vue'
import {
  ElCheckbox,
  ElForm,
  ElFormItem,
  ElInputNumber,
  ElOption,
  ElSelect,
  ElSlider
} from 'element-plus-secondary'
import {
  COLOR_CASES,
  COLOR_PANEL,
  DEFAULT_BASIC_STYLE
} from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const props = defineProps({
  chart: {
    type: Object as PropType<Omit<Chart, 'customAttr'> & { customAttr: DeepPartial<ChartAttr> }>,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})
const colorCases = COLOR_CASES

const predefineColors = COLOR_PANEL
const state = reactive({
  basicStyleForm: JSON.parse(JSON.stringify(DEFAULT_BASIC_STYLE)) as ChartBasicStyle,
  customColor: null,
  colorIndex: 0
})
watch(
  props.chart.customAttr.basicStyle,
  () => {
    init()
  },
  { deep: true }
)
const emit = defineEmits(['onBasicStyleChange'])
const changeBasicStyle = (prop?: string) => emit('onBasicStyleChange', state.basicStyleForm)
const init = () => {
  const basicStyle = JSON.parse(JSON.stringify(props.chart.customAttr.basicStyle))
  state.basicStyleForm = basicStyle as ChartBasicStyle
  if (!state.customColor) {
    state.customColor = state.basicStyleForm.colors[0]
    state.colorIndex = 0
  }
}
const changeColorOption = () => {
  const items = colorCases.filter(ele => {
    return ele.value === state.basicStyleForm.colorScheme
  })
  state.basicStyleForm.colors = [...items[0].colors]

  state.customColor = state.basicStyleForm.colors[0]
  state.colorIndex = 0

  changeBasicStyle()
}
const resetCustomColor = () => {
  changeColorOption()
}
const switchColor = index => {
  state.colorIndex = index
}
const switchColorCase = () => {
  state.basicStyleForm.colors[state.colorIndex] = state.customColor
  changeBasicStyle()
}

const pageSizeOptions = [
  { name: '10' + t('chart.table_page_size_unit'), value: '10' },
  { name: '20' + t('chart.table_page_size_unit'), value: '20' },
  { name: '50' + t('chart.table_page_size_unit'), value: '50' },
  { name: '100' + t('chart.table_page_size_unit'), value: '100' }
]

const gaugeStyleOptions = [{ name: '默认', value: 'default' }]

const lineSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]
const mapStyleOptions = [
  { name: t('chart.map_style_normal'), value: 'normal' },
  { name: t('chart.map_style_darkblue'), value: 'darkblue' },
  { name: t('chart.map_style_light'), value: 'light' },
  { name: t('chart.map_style_dark'), value: 'dark' },
  { name: t('chart.map_style_whitesmoke'), value: 'whitesmoke' },
  { name: t('chart.map_style_fresh'), value: 'fresh' },
  { name: t('chart.map_style_grey'), value: 'grey' },
  { name: t('chart.map_style_graffiti'), value: 'graffiti' },
  { name: t('chart.map_style_macaron'), value: 'macaron' },
  { name: t('chart.map_style_blue'), value: 'blue' },
  { name: t('chart.map_style_wine'), value: 'wine' }
]
init()
</script>
<template>
  <el-form :model="state.basicStyleForm" label-width="80px" size="small">
    <el-form-item :label="t('chart.color_case')" class="form-item">
      <el-popover placement="bottom" width="400" trigger="click">
        <template #reference>
          <div :style="{ cursor: 'pointer', marginTop: '2px', width: '180px' }">
            <span
              v-for="(c, index) in state.basicStyleForm.colors"
              :key="index"
              :style="{
                width: '20px',
                height: '20px',
                display: 'inline-block',
                backgroundColor: c
              }"
            />
          </div>
        </template>

        <div style="padding: 6px 10px">
          <div>
            <span class="color-label">{{ t('chart.system_case') }}</span>
            <el-select
              v-model="state.basicStyleForm.colorScheme"
              :placeholder="t('chart.pls_slc_color_case')"
              size="small"
              @change="changeColorOption()"
            >
              <el-option
                v-for="option in colorCases"
                :key="option.value"
                :label="option.name"
                :value="option.value"
                style="display: flex; align-items: center"
              >
                <div style="float: left">
                  <span
                    v-for="(c, index) in option.colors"
                    :key="index"
                    :style="{
                      width: '20px',
                      height: '20px',
                      float: 'left',
                      backgroundColor: c
                    }"
                  />
                </div>
                <span style="margin-left: 4px">{{ option.name }}</span>
              </el-option>
            </el-select>
            <el-button size="small" type="text" style="margin-left: 2px" @click="resetCustomColor"
              >{{ t('chart.reset') }}
            </el-button>
          </div>
          <!--自定义配色方案-->
          <div>
            <div style="display: flex; align-items: center; margin-top: 10px">
              <span class="color-label">{{ t('chart.custom_case') }}</span>
              <span>
                <el-radio-group v-model="state.customColor" class="color-type">
                  <el-radio
                    v-for="(c, index) in state.basicStyleForm.colors"
                    :key="index"
                    :label="c"
                    style="padding: 2px"
                    @click="switchColor(index)"
                  >
                    <span
                      :style="{
                        width: '20px',
                        height: '20px',
                        display: 'inline-block',
                        backgroundColor: c
                      }"
                    />
                  </el-radio>
                </el-radio-group>
              </span>
            </div>
            <div style="display: flex; align-items: center; margin-top: 10px">
              <span class="color-label" />
              <span>
                <el-color-picker
                  v-model="state.customColor"
                  class="color-picker-style"
                  :predefine="predefineColors"
                  @change="switchColorCase"
                />
              </span>
            </div>
          </div>
          <!--自定义系列或维度枚举值颜色-->
          <!--                <div-->
          <!--                  v-show="showProperty('colorPanel')"-->
          <!--                  style="display: flex; align-items: center; margin-top: 10px"-->
          <!--                >-->
          <!--                  <span class="color-label" />-->
          <!--                  <span>-->
          <!--                    <span v-for="(c, index) in colorForm.colors" :key="index" style="padding: 2px">-->
          <!--                      <span-->
          <!--                        :style="{-->
          <!--                          width: '20px',-->
          <!--                          height: '20px',-->
          <!--                          display: 'inline-block',-->
          <!--                          backgroundColor: c-->
          <!--                        }"-->
          <!--                      />-->
          <!--                    </span>-->
          <!--                  </span>-->
          <!--                </div>-->
        </div>

        <!--              <div class="custom-color-style">-->
        <!--                <div-->
        <!--                  v-for="(item, index) in state.colorForm.seriesColors"-->
        <!--                  :key="index"-->
        <!--                  style="display: flex; align-items: center; margin: 2px 0"-->
        <!--                >-->
        <!--                  <el-color-picker-->
        <!--                    v-model="item.color"-->
        <!--                    class="color-picker-style"-->
        <!--                    :predefine="predefineColors"-->
        <!--                    @change="switchCustomColor(index)"-->
        <!--                  />-->
        <!--                  <span class="span-label" :title="item.name">{{ item.name }}</span>-->
        <!--                </div>-->
        <!--              </div>-->
      </el-popover>
    </el-form-item>
    <el-form-item :label="t('chart.not_alpha')" class="form-item form-item-slider">
      <el-input-number
        :effect="props.themes"
        v-model="state.basicStyleForm.alpha"
        :min="0"
        :max="100"
        size="small"
        controls-position="right"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <!--table start-->
    <el-form-item :label="t('chart.table_column_width_config')" class="form-item">
      <el-radio-group v-model="state.basicStyleForm.tableColumnMode" @change="changeBasicStyle()">
        <el-radio label="adopt" :effect="props.themes">
          {{ t('chart.table_column_adapt') }}
        </el-radio>
        <el-radio label="custom" :effect="props.themes">
          {{ t('chart.table_column_custom') }}
        </el-radio>
      </el-radio-group>
      <el-tooltip placement="bottom" :content="t('chart.table_column_width_tip')" raw-content>
        <el-icon><InfoFilled /></el-icon>
      </el-tooltip>
    </el-form-item>
    <el-form-item
      v-show="state.basicStyleForm.tableColumnMode === 'custom'"
      class="form-item form-item-slider"
    >
      <el-input-number
        :effect="props.themes"
        v-model.number="state.basicStyleForm.tableColumnWidth"
        :min="10"
        :max="500"
        controls-position="right"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-form-item :label="t('chart.table_border_color')" class="form-item">
      <el-color-picker
        v-model="state.basicStyleForm.tableBorderColor"
        :predefine="predefineColors"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-form-item :label="t('chart.table_scroll_bar_color')" class="form-item">
      <el-color-picker
        v-model="state.basicStyleForm.tableScrollBarColor"
        class="color-picker-style"
        :predefine="predefineColors"
        color-format="rgb"
        show-alpha
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-form-item :label="t('chart.table_page_mode')" class="form-item">
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.tablePageMode"
        :placeholder="t('chart.table_page_mode')"
        @change="changeBasicStyle()"
      >
        <el-option :label="t('chart.page_mode_page')" value="page" />
        <el-option :label="t('chart.page_mode_pull')" value="pull" />
      </el-select>
    </el-form-item>
    <el-form-item
      v-show="state.basicStyleForm.tablePageMode === 'page'"
      :label="t('chart.table_page_size')"
      class="form-item"
    >
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.tablePageSize"
        :placeholder="t('chart.table_page_size')"
        @change="changeBasicStyle()"
      >
        <el-option
          v-for="item in pageSizeOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <!--table end-->
    <!--gauge start-->
    <el-form-item :label="t('chart.chart_style')" class="form-item">
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.gaugeStyle"
        @change="changeBasicStyle()"
      >
        <el-option
          v-for="item in gaugeStyleOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <!--gauge end-->
    <!--bar start-->
    <el-checkbox v-model="state.basicStyleForm.barDefault" @change="changeBasicStyle()">
      {{ t('chart.adapt') }}
    </el-checkbox>
    <el-form-item
      v-show="!state.basicStyleForm.barDefault"
      :label="t('chart.bar_gap')"
      class="form-item form-item-slider"
    >
      <el-input-number
        :effect="props.themes"
        v-model="state.basicStyleForm.barGap"
        controls-position="right"
        :show-input-controls="false"
        :min="0"
        :max="5"
        :step="0.1"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <!--bar end-->
    <!--line area start-->
    <el-form-item :label="t('chart.line_width')" class="form-item form-item-slider">
      <el-input-number
        :effect="props.themes"
        v-model="state.basicStyleForm.lineWidth"
        :min="0"
        :max="10"
        size="small"
        controls-position="right"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-form-item :label="t('chart.line_symbol')" class="form-item">
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.lineSymbol"
        :placeholder="t('chart.line_symbol')"
        @change="changeBasicStyle()"
      >
        <el-option
          v-for="item in lineSymbolOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('chart.line_symbol_size')" class="form-item form-item-slider">
      <el-input-number
        :effect="props.themes"
        v-model="state.basicStyleForm.lineSymbolSize"
        :min="0"
        :max="20"
        size="small"
        controls-position="right"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-checkbox v-model="state.basicStyleForm.lineSmooth" @change="changeBasicStyle()">
      {{ t('chart.line_smooth') }}
    </el-checkbox>
    <!--line area end-->
    <!--radar begin-->
    <el-form-item :label="t('chart.shape')" class="form-item">
      <el-radio-group
        v-model="state.basicStyleForm.radarShape"
        @change="changeBasicStyle('radarShape')"
      >
        <el-radio :effect="props.themes" label="polygon">{{ t('chart.polygon') }}</el-radio>
        <el-radio :effect="props.themes" label="circle">{{ t('chart.circle') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <!--radar end-->
    <!--flow map begin-->
    <el-form-item :label="t('chart.map_style')" class="form-item">
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.mapStyle"
        @change="changeBasicStyle('mapStyle')"
      >
        <el-option
          v-for="item in mapStyleOptions"
          :key="item.name"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <!--flow map end-->
    <!--scatter start-->
    <el-form-item :label="t('chart.bubble_symbol')" class="form-item">
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.scatterSymbol"
        :placeholder="t('chart.line_symbol')"
        @change="changeBasicStyle('scatterSymbol')"
      >
        <el-option
          v-for="item in lineSymbolOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('chart.bubble_size')" class="form-item form-item-slider">
      <el-input-number
        :effect="props.themes"
        v-model="state.basicStyleForm.scatterSymbolSize"
        controls-position="right"
        :min="1"
        :max="40"
        @change="changeBasicStyle('scatterSymbolSize')"
      />
    </el-form-item>
    <!--scatter end-->
    <!--map start-->
    <el-form-item :label="t('chart.area_border_color')" class="form-item">
      <el-color-picker
        v-model="state.basicStyleForm.areaBorderColor"
        class="color-picker-style"
        :predefine="predefineColors"
        @change="changeBasicStyle('areaBorderColor')"
      />
    </el-form-item>
    <el-form-item :label="t('chart.area_base_color')" class="form-item">
      <el-color-picker
        v-model="state.basicStyleForm.areaBaseColor"
        class="color-picker-style"
        :predefine="predefineColors"
        @change="changeBasicStyle('areaBaseColor')"
      />
    </el-form-item>
    <el-form-item :label="t('chart.suspension')" class="form-item">
      <el-checkbox
        v-model="state.basicStyleForm.suspension"
        :predefine="predefineColors"
        @change="changeBasicStyle('suspension')"
      />
    </el-form-item>
    <!--map end-->
    <!--symbol map start-->
    <el-form-item :label="t('chart.bubble_symbol')" class="form-item">
      <el-select
        :effect="props.themes"
        v-model="state.basicStyleForm.scatterSymbol"
        :placeholder="t('chart.line_symbol')"
        @change="changeBasicStyle"
      >
        <el-option
          v-for="item in lineSymbolOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('chart.bubble_size')" class="form-item form-item-slider">
      <el-input-number
        :effect="props.themes"
        controls-position="right"
        v-model="state.basicStyleForm.scatterSymbolSize"
        :min="1"
        :max="40"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-form-item :label="t('chart.not_alpha')" class="form-item form-item-slider">
      <el-input-number
        :effect="props.themes"
        controls-position="right"
        v-model="state.basicStyleForm.symbolOpacity"
        :min="1"
        :max="100"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <el-form-item
      v-if="state.basicStyleForm.scatterSymbol !== 'marker'"
      :label="t('visualization.border_color')"
      class="form-item form-item-slider"
    >
      <el-input-number
        :effect="props.themes"
        controls-position="right"
        v-model="state.basicStyleForm.symbolStrokeWidth"
        :min="0"
        :max="5"
        @change="changeBasicStyle()"
      />
    </el-form-item>
  </el-form>
</template>
<style scoped lang="less">
.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.color-label {
  display: inline-block;
  width: 60px;
}

.color-type {
  &:deep(.ed-radio__input) {
    display: none;
  }
  .ed-radio {
    margin: 0 2px 0 0 !important;
    border: 1px solid transparent;
  }
}

.ed-radio :deep(.ed-radio__label) {
  padding-left: 0;
}

.ed-radio.is-checked {
  border: 1px solid #0a7be0;
}

.custom-color-style {
  height: 300px;
  overflow-y: auto;
  padding: 4px 12px;
  border: 1px solid #e6e6e6;
}
</style>
