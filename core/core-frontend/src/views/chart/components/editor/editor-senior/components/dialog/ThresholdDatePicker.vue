<script lang="ts" setup>
import { computed, PropType } from 'vue'
import {
  transDateFormat,
  transDatePickerType
} from '@/views/chart/components/editor/util/DateFormatUtil'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  field: {
    type: Object as PropType<ChartViewField & { dateStyle?: string; datePattern?: string }>,
    required: true
  },
  placeholder: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['update:modelValue', 'change'])

// 各类条件样式共用字段的日期粒度和分隔符，显示格式与保存格式保持一致。
const format = computed(() => transDateFormat(props.field.dateStyle, props.field.datePattern))
const pickerType = computed(() => transDatePickerType(props.field.dateStyle))
const value = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val ?? '')
})
const change = val => {
  // 清空日期时统一为空字符串，沿用条件样式的必填校验。
  emit('change', val ?? '')
}
</script>

<template>
  <el-time-picker
    v-if="field.dateStyle === 'H_m_s'"
    v-model="value"
    :format="format"
    :value-format="format"
    :placeholder="placeholder"
    size="default"
    style="width: 100%"
    @change="change"
  />
  <el-date-picker
    v-else
    v-model="value"
    :type="pickerType"
    :format="format"
    :value-format="format"
    :placeholder="placeholder"
    size="default"
    style="width: 100%"
    @change="change"
  />
</template>
