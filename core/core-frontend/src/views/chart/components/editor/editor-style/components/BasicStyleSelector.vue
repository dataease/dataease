<script setup lang="ts">
import { onMounted, PropType, reactive, watch } from 'vue'
import { COLOR_PANEL, DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import CustomColorStyleSelect from '@/views/chart/components/editor/editor-style/components/CustomColorStyleSelect.vue'

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
  basicStyleForm: JSON.parse(JSON.stringify(DEFAULT_BASIC_STYLE)) as ChartBasicStyle,
  customColor: null,
  colorIndex: 0
})
watch(
  () => props.chart.customAttr.basicStyle,
  () => {
    init()
  },
  { deep: true }
)
const emit = defineEmits(['onBasicStyleChange'])
const changeBasicStyle = (prop?: string, requestData = false) => {
  emit('onBasicStyleChange', { data: state.basicStyleForm, requestData })
}
const init = () => {
  const basicStyle = JSON.parse(JSON.stringify(props.chart.customAttr.basicStyle))
  state.basicStyleForm = basicStyle as ChartBasicStyle
  if (!state.customColor) {
    state.customColor = state.basicStyleForm.colors[0]
    state.colorIndex = 0
  }
}

const pageSizeOptions = [
  { name: '10' + t('chart.table_page_size_unit'), value: 10 },
  { name: '20' + t('chart.table_page_size_unit'), value: 20 },
  { name: '50' + t('chart.table_page_size_unit'), value: 50 },
  { name: '100' + t('chart.table_page_size_unit'), value: 100 }
]

const gaugeStyleOptions = [{ name: '默认', value: 'default' }]

const symbolOptions = [
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

onMounted(() => {
  init()
})
</script>
<template>
  <template v-if="showProperty('colors')">
    <custom-color-style-select
      v-model="state"
      :themes="themes"
      @change-basic-style="changeBasicStyle"
    />
  </template>

  <el-form-item
    :label="t('chart.gradient')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('gradient')"
  >
    <el-checkbox v-model="state.basicStyleForm.gradient" @change="changeBasicStyle()" />
  </el-form-item>
  <el-form-item
    :label="t('chart.not_alpha')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('alpha')"
  >
    <el-input-number
      :effect="props.themes"
      v-model="state.basicStyleForm.alpha"
      :min="0"
      :max="100"
      controls-position="right"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <!--table start-->
  <el-form-item
    :label="t('chart.table_column_width_config')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('tableColumnMode')"
  >
    <el-radio-group v-model="state.basicStyleForm.tableColumnMode" @change="changeBasicStyle()">
      <el-radio label="adapt" :effect="props.themes">
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
    v-if="showProperty('tableColumnMode') && state.basicStyleForm.tableColumnMode === 'custom'"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
  >
    <el-input-number
      :effect="props.themes"
      v-model.number="state.basicStyleForm.tableColumnWidth"
      :min="10"
      controls-position="right"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.table_border_color')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('tableBorderColor')"
  >
    <el-color-picker
      :persistent="false"
      v-model="state.basicStyleForm.tableBorderColor"
      color-format="hex"
      :predefine="predefineColors"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.table_scroll_bar_color')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('tableScrollBarColor')"
  >
    <el-color-picker
      :persistent="false"
      v-model="state.basicStyleForm.tableScrollBarColor"
      class="color-picker-style"
      :predefine="predefineColors"
      color-format="rgb"
      show-alpha
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.table_page_mode')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('tablePageMode')"
  >
    <el-select
      :effect="props.themes"
      v-model="state.basicStyleForm.tablePageMode"
      :placeholder="t('chart.table_page_mode')"
      @change="changeBasicStyle('tablePageMode', true)"
    >
      <el-option :label="t('chart.page_mode_page')" value="page" />
      <el-option :label="t('chart.page_mode_pull')" value="pull" />
    </el-select>
  </el-form-item>
  <el-form-item
    v-if="showProperty('tablePageMode') && state.basicStyleForm.tablePageMode === 'page'"
    :label="t('chart.table_page_size')"
    class="form-item"
    :class="'form-item-' + themes"
  >
    <el-select
      :effect="props.themes"
      v-model="state.basicStyleForm.tablePageSize"
      :placeholder="t('chart.table_page_size')"
      @change="changeBasicStyle('tablePageSize', true)"
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
  <el-form-item
    :label="t('chart.chart_style')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('gaugeStyle')"
  >
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
  <el-checkbox
    v-model="state.basicStyleForm.barDefault"
    @change="changeBasicStyle()"
    v-if="showProperty('barDefault')"
  >
    {{ t('chart.adapt') }}
  </el-checkbox>
  <el-form-item
    v-if="showProperty('barDefault') && !state.basicStyleForm.barDefault"
    :label="t('chart.bar_gap')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
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
  <el-form-item
    :label="t('chart.line_width')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('lineWidth')"
  >
    <el-input-number
      :effect="props.themes"
      v-model="state.basicStyleForm.lineWidth"
      :min="0"
      :max="10"
      controls-position="right"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.line_symbol')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('lineSymbol')"
  >
    <el-select
      :effect="props.themes"
      v-model="state.basicStyleForm.lineSymbol"
      :placeholder="t('chart.line_symbol')"
      @change="changeBasicStyle()"
    >
      <el-option
        v-for="item in symbolOptions"
        :key="item.value"
        :label="item.name"
        :value="item.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item
    :label="t('chart.line_symbol_size')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('lineSymbolSize')"
  >
    <el-input-number
      :effect="props.themes"
      v-model="state.basicStyleForm.lineSymbolSize"
      :min="0"
      :max="20"
      controls-position="right"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-checkbox
    v-model="state.basicStyleForm.lineSmooth"
    @change="changeBasicStyle()"
    v-if="showProperty('lineSmooth')"
  >
    {{ t('chart.line_smooth') }}
  </el-checkbox>
  <!--line area end-->
  <!--radar begin-->
  <el-form-item
    :label="t('chart.shape')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('radarShape')"
  >
    <el-radio-group
      v-model="state.basicStyleForm.radarShape"
      @change="changeBasicStyle('radarShape')"
    >
      <el-radio :effect="themes" label="polygon">{{ t('chart.polygon') }}</el-radio>
      <el-radio :effect="themes" label="circle">{{ t('chart.circle') }}</el-radio>
    </el-radio-group>
  </el-form-item>
  <!--radar end-->
  <!--flow map begin-->
  <el-form-item
    :label="t('chart.map_style')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('mapStyle')"
  >
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
  <el-form-item
    :label="t('chart.bubble_symbol')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('scatterSymbol')"
  >
    <el-select
      :effect="props.themes"
      v-model="state.basicStyleForm.scatterSymbol"
      :placeholder="t('chart.line_symbol')"
      @change="changeBasicStyle('scatterSymbol')"
    >
      <el-option
        v-for="item in symbolOptions"
        :key="item.value"
        :label="item.name"
        :value="item.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item
    :label="t('chart.bubble_size')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('scatterSymbolSize')"
  >
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
  <el-form-item
    :label="t('chart.area_border_color')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('areaBorderColor')"
  >
    <el-color-picker
      :persistent="false"
      v-model="state.basicStyleForm.areaBorderColor"
      class="color-picker-style"
      :predefine="predefineColors"
      @change="changeBasicStyle('areaBorderColor')"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.area_base_color')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('areaBaseColor')"
  >
    <el-color-picker
      :persistent="false"
      v-model="state.basicStyleForm.areaBaseColor"
      class="color-picker-style"
      :predefine="predefineColors"
      @change="changeBasicStyle('areaBaseColor')"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.suspension')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('suspension')"
  >
    <el-checkbox
      v-model="state.basicStyleForm.suspension"
      :predefine="predefineColors"
      @change="changeBasicStyle('suspension')"
    />
  </el-form-item>
  <!--map end-->
  <!--symbol map start-->
  <el-form-item
    :label="t('chart.bubble_symbol')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('mapSymbol')"
  >
    <el-select
      :effect="props.themes"
      v-model="state.basicStyleForm.mapSymbol"
      :placeholder="t('chart.line_symbol')"
      @change="changeBasicStyle"
    >
      <el-option
        v-for="item in symbolOptions"
        :key="item.value"
        :label="item.name"
        :value="item.value"
      />
    </el-select>
  </el-form-item>
  <el-form-item
    :label="t('chart.bubble_size')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('mapSymbolSize')"
  >
    <el-input-number
      :effect="props.themes"
      controls-position="right"
      v-model="state.basicStyleForm.mapSymbolSize"
      :min="1"
      :max="40"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.not_alpha')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('mapSymbolOpacity')"
  >
    <el-input-number
      :effect="props.themes"
      controls-position="right"
      v-model="state.basicStyleForm.mapSymbolOpacity"
      :min="1"
      :max="100"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <el-form-item
    v-if="showProperty('mapSymbol') && state.basicStyleForm.mapSymbol !== 'marker'"
    :label="t('visualization.border_color')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
  >
    <el-input-number
      :effect="props.themes"
      controls-position="right"
      v-model="state.basicStyleForm.mapSymbolStrokeWidth"
      :min="0"
      :max="5"
      @change="changeBasicStyle()"
    />
  </el-form-item>
  <!-- pie/rose start -->
  <el-form-item
    :label="t('chart.pie_inner_radius_percent')"
    class="form-item"
    :class="'form-item-' + themes"
    v-if="showProperty('innerRadius')"
  >
    <el-slider
      v-model="state.basicStyleForm.innerRadius"
      :min="1"
      :max="100"
      @change="changeBasicStyle()"
      show-input
    />
  </el-form-item>
  <el-form-item
    :label="t('chart.pie_outer_radius')"
    class="form-item form-item-slider"
    :class="'form-item-' + themes"
    v-if="showProperty('radius')"
  >
    <el-slider
      v-model="state.basicStyleForm.radius"
      :min="1"
      :max="100"
      @change="changeBasicStyle()"
      show-input
    />
  </el-form-item>
  <!-- pie/rose end -->
</template>
<style scoped lang="less">
.form-item {
  :deep(.ed-input) {
    --ed-input-height: 28px;
  }
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
    border-radius: 5px;
  }

  .ed-radio.ed-radio--small {
    height: 26px;
  }

  .ed-radio :deep(.ed-radio__label) {
    padding-left: 0;
    height: 20px;
  }

  .ed-radio.is-checked {
    border: 1px solid #0a7be0;
  }
}

.custom-color-style {
  height: 300px;
  overflow-y: auto;
  padding: 4px 12px;
  border: 1px solid #e6e6e6;
}
</style>
