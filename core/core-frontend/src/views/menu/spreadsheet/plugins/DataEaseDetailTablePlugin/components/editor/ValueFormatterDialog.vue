<script setup lang="ts">
import { ref, watch } from 'vue'
import { cloneDeep } from 'lodash-es'
import type { FieldFormatterConfig } from '../../../../types/plugin'
import {
  createDefaultFormatterConfig,
  getUnitLabel,
  normalizeFormatterConfig
} from '../../utils/field-format'

const props = defineProps<{
  modelValue: boolean
  fieldName: string
  formatterCfg?: FieldFormatterConfig
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [formatterCfg: FieldFormatterConfig]
}>()

const formatterItem = ref<{ formatterCfg: FieldFormatterConfig }>({
  formatterCfg: createDefaultFormatterConfig()
})
const exampleResult = ref('')
const formatterTypes: Array<{
  label: string
  value: FieldFormatterConfig['type']
}> = [
  { label: '自动', value: 'auto' },
  { label: '数值', value: 'value' },
  { label: '百分比', value: 'percent' }
]
const unitOptions = {
  ch: [
    { label: '无', value: 1 },
    { label: '千', value: 1000 },
    { label: '万', value: 10000 },
    { label: '百万', value: 1000000 },
    { label: '亿', value: 100000000 }
  ],
  en: [
    { label: 'None', value: 1 },
    { label: 'Thousand (K)', value: 1000 },
    { label: 'Million (M)', value: 1000000 },
    { label: 'Billion (B)', value: 1000000000 }
  ]
}

watch(
  () => props.modelValue,
  visible => {
    if (visible) {
      formatterItem.value = {
        formatterCfg: cloneDeep(normalizeFormatterConfig(props.formatterCfg))
      }
      updateExample()
    }
  },
  { immediate: true }
)

function updateExample() {
  const config = formatterItem.value.formatterCfg
  const sourceValue = config.type === 'percent' ? 20000000 * 100 : 20000000 / config.unit
  const value =
    config.type === 'auto'
      ? sourceValue
      : Number(sourceValue.toFixed(config.decimalCount))
  const result = config.thousandSeparator ? value.toLocaleString('en-US') : String(value)
  const unitLabel = config.type === 'percent'
    ? ''
    : getUnitLabel(config.unitLanguage, config.unit)
  exampleResult.value = `${result}${config.type === 'percent' ? '%' : ''}${unitLabel}${config.suffix}`
}

const changeUnitLanguage = (language: string) => {
  const config = formatterItem.value.formatterCfg
  const normalizedLanguage = language === 'en' ? 'en' : 'ch'
  config.unitLanguage = normalizedLanguage
  if (!unitOptions[normalizedLanguage].some(option => option.value === config.unit)) {
    config.unit = 1
  }
  updateExample()
}

const close = () => emit('update:modelValue', false)

const confirm = () => {
  emit('confirm', cloneDeep(formatterItem.value.formatterCfg))
  close()
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="`数值格式 - ${fieldName}`"
    width="480px"
    append-to-body
    destroy-on-close
    @close="close"
  >
    <el-form :model="formatterItem.formatterCfg" label-position="top">
      <el-form-item label="格式化方式">
        <el-radio-group v-model="formatterItem.formatterCfg.type" @change="updateExample">
          <el-radio v-for="item in formatterTypes" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="formatterItem.formatterCfg.type !== 'auto'"
        label="小数位数"
      >
        <el-input-number
          v-model="formatterItem.formatterCfg.decimalCount"
          controls-position="right"
          :min="0"
          :max="10"
          @change="updateExample"
        />
      </el-form-item>

      <el-row v-if="formatterItem.formatterCfg.type !== 'percent'" :gutter="8">
        <el-col :span="12">
          <el-form-item label="单位制">
            <el-select
              v-model="formatterItem.formatterCfg.unitLanguage"
              @change="changeUnitLanguage"
            >
              <el-option label="中文" value="ch" />
              <el-option label="英文" value="en" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单位">
            <el-select
              v-model="formatterItem.formatterCfg.unit"
              style="width: 100%"
              @change="updateExample"
            >
              <el-option
                v-for="item in unitOptions[formatterItem.formatterCfg.unitLanguage]"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="后缀">
        <el-input
          v-model="formatterItem.formatterCfg.suffix"
          clearable
          maxlength="30"
          placeholder="请输入内容"
          @change="updateExample"
        />
      </el-form-item>

      <el-form-item>
        <el-checkbox
          v-model="formatterItem.formatterCfg.thousandSeparator"
          label="使用千分位分隔符"
          @change="updateExample"
        />
      </el-form-item>

      <div class="formatter-example">
        <span>示例</span>
        <span>{{ exampleResult }}</span>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="confirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.formatter-example {
  display: flex;
  gap: 12px;
  line-height: 22px;
  color: #646a73;
}
</style>
