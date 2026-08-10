<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, toRefs } from 'vue'
import {
  isEnLocal,
  formatterType,
  getUnitTypeList,
  onChangeFormatCfgUnitLanguage,
  valueFormatter,
  initFormatCfgUnit
} from '@/views/chart/components/js/formatter'
import { ElFormItem } from 'element-plus-secondary'

const { t } = useI18n()

const emit = defineEmits(['onFormatterItemChange'])

const props = defineProps({
  formatterCfg: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    default: 'light'
  }
})

const { formatterCfg } = toRefs(props)

const state = reactive({
  typeList: formatterType,
  exampleResult: '20000000'
})

function changeUnitLanguage(cfg: BaseFormatter, lang) {
  onChangeFormatCfgUnitLanguage(cfg, lang)
  getExampleValue()
}

const init = () => {
  if (!formatterCfg.value) {
    formatterCfg.value = formatterCfg

    initFormatCfgUnit(formatterCfg.value)
  }
}

const onFormatChange = () => {
  getExampleValue()
  emit('onFormatterItemChange', formatterCfg.value)
}
const getExampleValue = () => {
  state.exampleResult = valueFormatter(20000000, formatterCfg.value)
}

init()
getExampleValue()
</script>

<template>
  <div>
    <el-form
      ref="form"
      :effect="themes"
      :model="formatterCfg"
      class="formatter-form"
      label-position="top"
    >
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        :label="t('chart.value_formatter_type')"
      >
        <el-radio-group
          class="radio-span"
          :effect="themes"
          v-model="formatterCfg.type"
          @change="onFormatChange"
        >
          <el-radio
            :effect="themes"
            v-for="radio in state.typeList"
            :key="radio.value"
            :label="radio.value"
            >{{ t('chart.' + radio.name) }}</el-radio
          >
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="formatterCfg.type !== 'auto'"
        class="form-item"
        :class="'form-item-' + themes"
        :label="t('chart.value_formatter_decimal_count')"
      >
        <el-input-number
          controls-position="right"
          v-model="formatterCfg.decimalCount"
          :effect="themes"
          size="small"
          :min="0"
          :max="10"
          @change="onFormatChange"
        />
      </el-form-item>

      <template v-if="formatterCfg.type !== 'percent'">
        <el-row :gutter="8">
          <el-col :span="12" v-if="!isEnLocal">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.value_formatter_unit_language')"
            >
              <el-select
                v-model="formatterCfg.unitLanguage"
                size="small"
                :placeholder="t('chart.pls_select_field')"
                :effect="themes"
                @change="v => changeUnitLanguage(formatterCfg, v)"
              >
                <el-option
                  :effect="themes"
                  size="small"
                  :label="t('chart.value_formatter_unit_language_ch')"
                  value="ch"
                />
                <el-option
                  size="small"
                  :effect="themes"
                  :label="t('chart.value_formatter_unit_language_en')"
                  value="en"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="isEnLocal ? 24 : 12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.value_formatter_unit')"
            >
              <el-select
                :effect="themes"
                size="small"
                v-model="formatterCfg.unit"
                :placeholder="t('chart.pls_select_field')"
                @change="onFormatChange"
                style="width: 100%"
              >
                <el-option
                  :effect="themes"
                  size="small"
                  v-for="item in getUnitTypeList(formatterCfg.unitLanguage)"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-form-item
        :effect="themes"
        class="form-item"
        :class="'form-item-' + themes"
        :label="t('chart.value_formatter_suffix')"
      >
        <el-input
          v-model="formatterCfg.suffix"
          :effect="themes"
          size="small"
          clearable
          :placeholder="t('commons.input_content')"
          @change="onFormatChange"
        />
      </el-form-item>

      <el-form-item class="form-item" :class="'form-item-' + themes" :effect="themes">
        <el-checkbox
          :effect="themes"
          size="small"
          v-model="formatterCfg.thousandSeparator"
          @change="onFormatChange"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>

      <div style="line-height: 22px">
        <span style="color: #646a73">{{ t('chart.value_formatter_example') }}</span>
        <span style="margin-left: 12px">{{ state.exampleResult }}</span>
      </div>
    </el-form>
  </div>
</template>

<style lang="less" scoped>
.el-form-item {
  margin-bottom: 10px !important;
}
.formatter-form {
  margin-bottom: 16px;
  :deep(.ed-form-item) {
    margin-bottom: 16px;
  }

  :deep(.ed-form-item__label) {
    color: #1f2329;
    margin-bottom: 8px !important;
    font-size: 14px !important;
    font-weight: 400 !important;
  }

  :deep(.ed-checkbox) {
    color: #1f2329;
  }
}

.el-select-dropdown__item :deep(span) {
  font-size: 14px !important;
}
.exp-style {
  color: #c0c4cc;
  font-size: 12px;
}

.form-item-dark {
  :deep(.ed-form-item__label) {
    color: #6a6a6a;
    font-size: 12px !important;
    font-weight: 400;
    line-height: 20px;
  }
}
</style>
