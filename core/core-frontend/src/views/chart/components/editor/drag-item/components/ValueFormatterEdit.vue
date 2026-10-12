<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { nextTick, reactive, toRefs, watch } from 'vue'
import { ElForm } from 'element-plus-secondary'
import {
  isEnLocal,
  formatterType,
  getUnitTypeList,
  onChangeFormatCfgUnitLanguage,
  valueFormatter,
  initFormatCfgUnit
} from '@/views/chart/components/js/formatter'

const { t } = useI18n()
const emit = defineEmits(['change'])

const props = defineProps({
  // Existing callers keep their light popovers unless a theme is explicitly supplied.
  themes: {
    type: String,
    default: 'light'
  },
  formatTypeSelect: {
    type: Boolean,
    default: false
  },
  inline: {
    type: Boolean,
    default: false
  },
  examplePrefix: {
    type: String,
    default: ''
  },
  formatterItem: {
    type: Object,
    required: true
  },
  chart: {
    type: Object,
    required: true
  }
})

const { formatterItem } = toRefs(props)

const state = reactive({
  typeList: formatterType,
  exampleResult: '20000000'
})

function changeUnitLanguage(cfg: BaseFormatter, lang) {
  onChangeFormatCfgUnitLanguage(cfg, lang)
  changeFormatter()
}

const init = () => {
  if (!formatterItem.value.formatterCfg) {
    formatterItem.value.formatterCfg = formatterItem

    initFormatCfgUnit(formatterItem.value.formatterCfg)
  }
}
const getExampleValue = () => {
  state.exampleResult = valueFormatter(20000000, formatterItem.value.formatterCfg)
}
const changeFormatter = async () => {
  await nextTick()
  getExampleValue()
  emit('change')
}

init()
getExampleValue()
watch(() => formatterItem.value.formatterCfg, getExampleValue, { deep: true })
</script>

<template>
  <div>
    <component
      :is="inline ? 'div' : ElForm"
      ref="form"
      size="default"
      :model="formatterItem.formatterCfg"
      class="formatter-form"
      :class="{
        'formatter-form--dark': themes === 'dark',
        'formatter-form--select': formatTypeSelect,
        'formatter-form--inline': inline
      }"
      label-position="top"
    >
      <slot />
      <el-form-item :label="t('chart.value_formatter_type')">
        <el-select
          v-if="formatTypeSelect"
          :effect="themes"
          v-model="formatterItem.formatterCfg.type"
          @change="changeFormatter"
        >
          <el-option
            v-for="type in state.typeList"
            :key="type.value"
            :value="type.value"
            :label="t('chart.' + type.name)"
          />
        </el-select>
        <el-radio-group v-else v-model="formatterItem.formatterCfg.type" @change="changeFormatter">
          <el-radio
            :effect="themes"
            v-for="radio in state.typeList"
            :key="radio.value"
            :value="radio.value"
            >{{ t('chart.' + radio.name) }}</el-radio
          >
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="formatterItem.formatterCfg.type !== 'auto'"
        :label="t('chart.value_formatter_decimal_count')"
      >
        <el-input-number
          :effect="themes"
          controls-position="right"
          v-model="formatterItem.formatterCfg.decimalCount"
          :min="0"
          :max="10"
          @change="changeFormatter"
        />
      </el-form-item>

      <template v-if="formatterItem.formatterCfg.type !== 'percent'">
        <el-row :gutter="8">
          <el-col :span="12" v-if="!isEnLocal">
            <el-form-item :label="t('chart.value_formatter_unit_language')">
              <el-select
                :effect="themes"
                v-model="formatterItem.formatterCfg.unitLanguage"
                :placeholder="t('chart.pls_select_field')"
                @change="v => changeUnitLanguage(formatterItem.formatterCfg, v)"
              >
                <el-option :label="t('chart.value_formatter_unit_language_ch')" value="ch" />
                <el-option :label="t('chart.value_formatter_unit_language_en')" value="en" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="isEnLocal ? 24 : 12">
            <el-form-item :label="t('chart.value_formatter_unit')">
              <el-select
                :effect="themes"
                v-model="formatterItem.formatterCfg.unit"
                :placeholder="t('chart.pls_select_field')"
                @change="changeFormatter"
                style="width: 100%"
              >
                <el-option
                  v-for="item in getUnitTypeList(formatterItem.formatterCfg.unitLanguage)"
                  :key="item.value"
                  :label="item.name"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-form-item :label="t('chart.value_formatter_suffix')">
        <el-input
          :effect="themes"
          v-model="formatterItem.formatterCfg.suffix"
          clearable
          maxlength="30"
          :placeholder="t('commons.input_content')"
          @change="changeFormatter"
        />
      </el-form-item>

      <el-form-item>
        <el-checkbox
          :effect="themes"
          v-model="formatterItem.formatterCfg.thousandSeparator"
          @change="changeFormatter"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>

      <div style="line-height: 22px">
        <span class="formatter-example-label">{{ t('chart.value_formatter_example') }}</span>
        <span style="margin-left: 12px">{{ examplePrefix }}{{ state.exampleResult }}</span>
      </div>
    </component>
  </div>
</template>

<style lang="less" scoped>
.el-form-item {
  margin-bottom: 10px !important;
}
.formatter-form {
  --formatter-text: #1f2329;
  --formatter-secondary-text: #646a73;
  --formatter-font-size: 14px;
  --formatter-item-gap: 16px;
  color: var(--formatter-text);

  &--dark {
    --formatter-text: #ebebeb;
    --formatter-secondary-text: #a6a6a6;
  }

  &--inline {
    --formatter-font-size: 12px;
    --formatter-item-gap: 8px;
    font-size: 12px;
    margin-bottom: 8px;
  }

  .formatter-example-label {
    color: var(--formatter-secondary-text);
  }

  :deep(.ed-form-item) {
    margin-bottom: var(--formatter-item-gap);
  }

  :deep(.ed-form-item__label) {
    color: var(--formatter-text);
    margin-bottom: 8px !important;
    font-size: var(--formatter-font-size) !important;
    font-weight: 400 !important;
  }

  :deep(.ed-checkbox) {
    color: var(--formatter-text);
  }
  :deep(.ed-checkbox__label) {
    font-size: var(--formatter-font-size) !important;
    font-weight: 400 !important;
  }

  // Keep the same control geometry when the dialog is teleported outside the editor.
  :deep(.ed-radio) {
    height: 20px;
  }

  :deep(.ed-radio.ed-radio--small .ed-radio__inner) {
    width: 14px;
    height: 14px;
  }

  :deep(.ed-input) {
    --ed-input-height: 28px;

    .ed-input__suffix {
      height: 26px;
    }
  }

  :deep(.ed-input__inner) {
    font-size: 12px;
    font-weight: 400;
  }

  :deep(.ed-input-number) {
    width: 100%;

    .ed-input-number__decrease,
    .ed-input-number__increase {
      --ed-input-number-controls-height: 13px;
    }

    .ed-input__inner {
      text-align: start;
    }
  }

  &--select :deep(.ed-input-number) {
    // Paint the outline above the step controls so zoom rounding cannot cover its right edge.
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      border: 1px solid var(--ed-border-color);
      border-radius: var(--ed-border-radius-base);
      pointer-events: none;
      z-index: 2;
    }

    &:hover::after {
      border-color: var(--ed-border-color-hover);
    }

    &:focus-within::after {
      border-color: var(--ed-color-primary);
    }
  }

  :deep(.ed-select) {
    width: 100%;
  }

  :deep(.ed-radio__label) {
    line-height: 20px;
    font-style: normal;
    font-size: var(--formatter-font-size) !important;
    font-weight: 400 !important;
  }
}

.el-select-dropdown__item :deep(span) {
  font-size: 14px !important;
}
.exp-style {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
