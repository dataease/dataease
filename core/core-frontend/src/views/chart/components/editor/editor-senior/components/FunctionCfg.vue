<script lang="tsx" setup>
import { computed, onMounted, PropType, reactive, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_FUNCTION_CFG } from '@/views/chart/components/editor/util/chart'
import { equalsAny, includesAny } from '../../util/StringUtils'
import { parseJson } from '../../../js/util'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})

const emit = defineEmits(['onFunctionCfgChange'])

watch(
  () => props.chart.senior.functionCfg,
  () => {
    init()
  },
  { deep: true }
)

const state = reactive({
  functionForm: JSON.parse(JSON.stringify(DEFAULT_FUNCTION_CFG)),
  predefineColors: COLOR_PANEL,
  showEmptyStrategy: false
})

const changeFunctionCfg = () => {
  emit('onFunctionCfgChange', state.functionForm)
}

const showProperty = prop => props.propertyInner?.includes(prop)

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.senior) {
    let senior = parseJson(chart.senior)
    if (senior.functionCfg) {
      state.functionForm = senior.functionCfg
    }
    initFieldCtrl()
  }
}
const showIgnoreOption = computed(() => {
  return !equalsAny(props.chart.type, 'table-pivot', 'table-info', 'indicator')
})

const showEmptyDataFieldCtrl = computed(() => {
  return (
    showProperty('emptyDataStrategy') &&
    includesAny(props.chart.type, 'table') &&
    state.functionForm.emptyDataStrategy !== 'breakLine'
  )
})

const fieldOptions = ref([])
const initFieldCtrl = () => {
  if (showEmptyDataFieldCtrl.value) {
    fieldOptions.value = []
    let axis
    if (equalsAny(props.chart.type, 'table-normal', 'table-pivot')) {
      axis = props.chart.yAxis
    }
    if (props.chart.type === 'table-info') {
      axis = props.chart.xAxis
    }
    axis.forEach(item => {
      if (item.groupType === 'q') {
        fieldOptions.value.push({
          label: item.name,
          value: item.dataeaseName
        })
      }
    })
  }
}
onMounted(() => {
  init()
})
</script>

<template>
  <div @keydown.stop @keyup.stop style="width: 100%">
    <el-form ref="functionForm" :model="state.functionForm" label-position="top">
      <div v-if="showProperty('slider')">
        <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
          <el-checkbox
            :effect="themes"
            size="small"
            v-model="state.functionForm.sliderShow"
            @change="changeFunctionCfg"
          >
            {{ t('chart.slider') }}
          </el-checkbox>
        </el-form-item>
        <div style="padding-left: 22px">
          <el-form-item
            :label="t('chart.slider_range') + '(%)'"
            class="form-item range-slider form-item-checkbox"
            :class="'form-item-' + themes"
          >
            <el-slider
              :effect="themes"
              :disabled="!state.functionForm.sliderShow"
              v-model="state.functionForm.sliderRange"
              style="width: 90%; margin-top: -8px"
              :min="0"
              :max="100"
              input-size="small"
              range
              @change="changeFunctionCfg"
            />
          </el-form-item>
          <el-form-item
            :label="t('chart.slider_bg')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-color-picker
              :effect="themes"
              :disabled="!state.functionForm.sliderShow"
              v-model="state.functionForm.sliderBg"
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="changeFunctionCfg"
              is-custom
              :trigger-width="108"
            />
          </el-form-item>
          <el-form-item
            :label="t('chart.slider_fill_bg')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-color-picker
              :effect="themes"
              :disabled="!state.functionForm.sliderShow"
              v-model="state.functionForm.sliderFillBg"
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="changeFunctionCfg"
              is-custom
              :trigger-width="108"
            />
          </el-form-item>
          <el-form-item
            :label="t('chart.slider_text_color')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-color-picker
              :effect="themes"
              :disabled="!state.functionForm.sliderShow"
              v-model="state.functionForm.sliderTextColor"
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="changeFunctionCfg"
              is-custom
              :trigger-width="108"
            />
          </el-form-item>
        </div>
      </div>
      <el-form-item
        v-if="showProperty('emptyDataStrategy')"
        :label="t('chart.empty_data_strategy')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.functionForm.emptyDataStrategy"
          @change="changeFunctionCfg"
        >
          <el-radio :effect="themes" :label="'breakLine'">
            {{ t('chart.break_line') }}
          </el-radio>
          <el-radio :effect="themes" :label="'setZero'">{{ t('chart.set_zero') }}</el-radio>
          <el-radio v-if="showIgnoreOption" :effect="themes" :label="'ignoreData'">
            {{ t('chart.ignore_data') }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="showEmptyDataFieldCtrl"
        :label="t('chart.empty_data_field_ctrl')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.functionForm.emptyDataFieldCtrl"
          multiple
          @change="changeFunctionCfg"
        >
          <el-option
            v-for="option in fieldOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<style lang="less" scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ed-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}
.form-item :deep(.ed-radio-group) {
  display: flex;
  flex-direction: column;
  flex-wrap: wrap;
  align-items: flex-start;
  label {
    line-height: 28px;

    &:not(:last-child) {
      margin-bottom: 8px;
    }
  }
}
.range-slider {
  :deep(.ed-form-item__content) {
    padding: 0 8px;
  }
  :deep(.ed-slider__button-wrapper) {
    --ed-slider-button-wrapper-size: 36px;
    --ed-slider-button-size: 16px;
  }
}
.form-item-checkbox {
  margin-bottom: 10px !important;
}
</style>
