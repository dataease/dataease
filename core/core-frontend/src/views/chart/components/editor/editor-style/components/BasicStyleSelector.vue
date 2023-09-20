<script setup lang="ts">
import { onMounted, PropType, reactive, watch } from 'vue'
import { COLOR_PANEL, DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import CustomColorStyleSelect from '@/views/chart/components/editor/editor-style/components/CustomColorStyleSelect.vue'
import { ElFormItem, ElInputNumber } from 'element-plus-secondary'

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
  <div style="width: 100%">
    <template v-if="showProperty('colors')">
      <custom-color-style-select
        v-model="state"
        :themes="themes"
        @change-basic-style="changeBasicStyle"
      />
    </template>

    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('gradient')">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.gradient"
        @change="changeBasicStyle()"
      >
        {{ $t('chart.gradient') }}{{ $t('chart.color') }}
      </el-checkbox>
    </el-form-item>

    <!--map start-->
    <el-row :gutter="8">
      <el-col :span="12" v-if="showProperty('areaBorderColor')">
        <el-form-item
          :label="t('chart.area_border_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.areaBorderColor"
            is-custom
            :trigger-width="108"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeBasicStyle('areaBorderColor')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="showProperty('areaBaseColor')">
        <el-form-item
          :label="t('chart.area_base_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.areaBaseColor"
            is-custom
            :trigger-width="108"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeBasicStyle('areaBaseColor')"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('suspension')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.suspension"
        :predefine="predefineColors"
        @change="changeBasicStyle('suspension')"
      >
        {{ t('chart.suspension') }}
      </el-checkbox>
    </el-form-item>

    <!--map end-->

    <!--table start-->
    <el-row :gutter="8">
      <el-col :span="12" v-if="showProperty('tableBorderColor')">
        <el-form-item
          :label="t('chart.table_border_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.tableBorderColor"
            is-custom
            :trigger-width="108"
            color-format="hex"
            :predefine="predefineColors"
            @change="changeBasicStyle()"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="showProperty('tableScrollBarColor')">
        <el-form-item
          :label="t('chart.table_scroll_bar_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.tableScrollBarColor"
            class="color-picker-style"
            :predefine="predefineColors"
            is-custom
            :trigger-width="108"
            color-format="rgb"
            show-alpha
            @change="changeBasicStyle()"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="8">
      <el-col :span="12" v-if="showProperty('tablePageMode')">
        <el-form-item
          :label="t('chart.table_page_mode')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.basicStyleForm.tablePageMode"
            :placeholder="t('chart.table_page_mode')"
            @change="changeBasicStyle('tablePageMode', true)"
          >
            <el-option :label="t('chart.page_mode_page')" value="page" />
            <el-option :label="t('chart.page_mode_pull')" value="pull" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col
        :span="12"
        v-if="showProperty('tablePageMode') && state.basicStyleForm.tablePageMode === 'page'"
      >
        <el-form-item
          :label="t('chart.table_page_size')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
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
      </el-col>
    </el-row>
    <!--table end-->

    <div class="alpha-setting" v-if="showProperty('alpha')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.not_alpha') }}
      </label>
      <el-row style="flex: 1" gutter="8">
        <el-col :span="13">
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              v-model="state.basicStyleForm.alpha"
              @change="changeBasicStyle()"
            />
          </el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-input
              type="number"
              :effect="themes"
              v-model="state.basicStyleForm.alpha"
              :min="0"
              :max="100"
              class="alpha-input-number"
              :controls="false"
              @change="changeBasicStyle()"
            >
              <template #suffix> % </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <!--table2 start-->
    <el-form-item
      :label="t('chart.table_column_width_config')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('tableColumnMode')"
    >
      <el-radio-group v-model="state.basicStyleForm.tableColumnMode" @change="changeBasicStyle()">
        <el-radio label="adapt" :effect="themes">
          {{ t('chart.table_column_adapt') }}
        </el-radio>
        <el-radio label="custom" :effect="themes">
          {{ t('chart.table_column_custom') }}
          <el-tooltip placement="bottom" :content="t('chart.table_column_width_tip')" raw-content>
            <el-icon><InfoFilled /></el-icon>
          </el-tooltip>
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="showProperty('tableColumnMode') && state.basicStyleForm.tableColumnMode === 'custom'"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
        v-model.number="state.basicStyleForm.tableColumnWidth"
        :min="10"
        controls-position="right"
        @change="changeBasicStyle()"
      />
    </el-form-item>
    <!--table2 end-->
    <!--gauge start-->
    <el-form-item
      :label="t('chart.chart_style')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('gaugeStyle')"
    >
      <el-select
        :effect="themes"
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
    <el-form-item
      v-if="showProperty('barDefault')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.barDefault"
        @change="changeBasicStyle()"
      >
        {{ t('chart.adapt') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      v-if="showProperty('barDefault') && !state.basicStyleForm.barDefault"
      :label="t('chart.bar_gap')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
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
            @change="changeBasicStyle()"
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
            @change="changeBasicStyle()"
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
        @change="changeBasicStyle()"
      >
        {{ t('chart.line_smooth') }}
      </el-checkbox>
    </el-form-item>
    <!--line area end-->
    <!--radar begin-->
    <el-form-item
      :label="t('chart.shape')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('radarShape')"
    >
      <el-radio-group
        :effect="themes"
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
        :effect="themes"
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
        :effect="themes"
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
        :effect="themes"
        v-model="state.basicStyleForm.scatterSymbolSize"
        controls-position="right"
        :min="1"
        :max="40"
        @change="changeBasicStyle('scatterSymbolSize')"
      />
    </el-form-item>
    <!--scatter end-->

    <!--symbol map start-->
    <el-form-item
      :label="t('chart.bubble_symbol')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('mapSymbol')"
    >
      <el-select
        :effect="themes"
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
        :effect="themes"
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
        :effect="themes"
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
        :effect="themes"
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
        :effect="themes"
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
        :effect="themes"
        v-model="state.basicStyleForm.radius"
        :min="1"
        :max="100"
        @change="changeBasicStyle()"
        show-input
      />
    </el-form-item>
    <!-- pie/rose end -->
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

    &.dark {
      color: #a6a6a6;
    }
  }
  .alpha-input-number {
    :deep(input) {
      -webkit-appearance: none;
      -moz-appearance: textfield;

      &::-webkit-inner-spin-button,
      &::-webkit-outer-spin-button {
        -webkit-appearance: none;
      }
    }
  }
}
</style>
