<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_FUNCTION_CFG } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
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
    let senior = null
    if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
      senior = JSON.parse(JSON.stringify(chart.senior))
    } else {
      senior = JSON.parse(chart.senior)
    }
    if (senior.functionCfg) {
      state.functionForm = senior.functionCfg
    }
  }
}

init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="functionForm" :model="state.functionForm" label-width="80px" size="small">
        <div v-show="showProperty('slider')">
          <el-form-item :label="t('chart.slider')" class="form-item">
            <el-checkbox v-model="state.functionForm.sliderShow" @change="changeFunctionCfg">{{
              t('chart.show')
            }}</el-checkbox>
          </el-form-item>
          <div v-show="state.functionForm.sliderShow">
            <el-form-item
              :label="t('chart.slider_range') + '(%)'"
              class="form-item form-item-slider"
            >
              <el-slider
                v-model="state.functionForm.sliderRange"
                style="width: 90%"
                :min="0"
                :max="100"
                input-size="small"
                range
                @change="changeFunctionCfg"
              />
            </el-form-item>
            <el-form-item :label="t('chart.slider_bg')" class="form-item">
              <el-color-picker
                v-model="state.functionForm.sliderBg"
                class="color-picker-style"
                :predefine="state.predefineColors"
                @change="changeFunctionCfg"
              />
            </el-form-item>
            <el-form-item :label="t('chart.slider_fill_bg')" class="form-item">
              <el-color-picker
                v-model="state.functionForm.sliderFillBg"
                class="color-picker-style"
                :predefine="state.predefineColors"
                @change="changeFunctionCfg"
              />
            </el-form-item>
            <el-form-item :label="t('chart.slider_text_color')" class="form-item">
              <el-color-picker
                v-model="state.functionForm.sliderTextColor"
                class="color-picker-style"
                :predefine="state.predefineColors"
                @change="changeFunctionCfg"
              />
            </el-form-item>
          </div>
        </div>
        <el-form-item
          v-show="showProperty('emptyDataStrategy')"
          :label="t('chart.empty_data_strategy')"
          class="form-item"
        >
          <el-radio-group
            v-model="state.functionForm.emptyDataStrategy"
            @change="changeFunctionCfg"
          >
            <el-radio :effect="props.themes" :label="'breakLine'">{{
              t('chart.break_line')
            }}</el-radio>
            <el-radio :effect="props.themes" :label="'setZero'">{{ t('chart.set_zero') }}</el-radio>
            <el-radio :effect="props.themes" :label="'ignoreData'">{{
              t('chart.ignore_data')
            }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-col>
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
.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}
.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}
.ed-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}
.ed-form-item {
  margin-bottom: 6px;
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
  }
}
</style>
