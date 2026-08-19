<script setup lang="ts">
import type { Options } from '@popperjs/core'
import { ref, watch } from 'vue'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterTextSearchClause,
  SpreadsheetFilterTextSearchValue
} from '../../../../types/plugin'
import { normalizeSpreadsheetFilterTextSearchClauses } from '../../utils/filter-condition-rules'

const props = defineProps<{
  modelValue: unknown
  condition: SpreadsheetFilterCondition
  placeholder?: string
  isConfig?: boolean
  disabled?: boolean
  popperAppendTo?: string
  popperOptions?: Partial<Options>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: SpreadsheetFilterTextSearchValue]
  commit: []
}>()

const operatorOptions = [
  { label: '精确匹配', value: 'eq' },
  { label: '模糊匹配', value: 'like' }
] as const

const clauses = ref<SpreadsheetFilterTextSearchClause[]>([])

const readValue = () => {
  const value = props.modelValue as Partial<SpreadsheetFilterTextSearchValue> | undefined
  const source = Array.isArray(value?.clauses)
    ? value.clauses
    : typeof props.modelValue === 'string'
      ? [{ operator: 'like' as const, value: props.modelValue }]
      : []
  clauses.value = normalizeSpreadsheetFilterTextSearchClauses(
    source,
    props.condition.textSearchConditionType
  )
}

const emitValue = () => {
  emit('update:modelValue', {
    conditionType: props.condition.textSearchConditionType,
    clauses: clauses.value.map(clause => ({ ...clause }))
  })
}

const updateOperator = (index: number, operator: 'eq' | 'like') => {
  clauses.value[index].operator = operator
  emitValue()
  emit('commit')
}

const updateClauseValue = (index: number, value: string) => {
  clauses.value[index].value = value
  emitValue()
}

const commitValue = () => {
  emit('commit')
}

const handleKeyEnter = (event: KeyboardEvent) => {
  if (event.isComposing) return
  commitValue()
}

watch(
  () => [props.modelValue, props.condition.textSearchConditionType],
  readValue,
  { immediate: true, deep: true }
)
</script>

<template>
  <div class="spreadsheet-filter-text-search">
    <template
      v-for="(clause, index) in clauses"
      :key="index"
    >
      <span v-if="index > 0" class="spreadsheet-filter-text-search__logic">
        {{ condition.textSearchConditionType === 'and' ? '且' : '或' }}
      </span>
      <div class="spreadsheet-filter-text-search__clause">
        <el-select
          v-if="!condition.hideTextSearchConditionSwitch"
          :model-value="clause.operator"
          :disabled="disabled"
          :append-to="popperAppendTo"
          popper-class="spreadsheet-filter-runtime-popper"
          :popper-options="popperOptions"
          class="spreadsheet-filter-text-search__operator"
          @update:model-value="value => updateOperator(index, value)"
        >
          <el-option
            v-for="option in operatorOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-input
          :model-value="clause.value"
          :disabled="disabled"
          :placeholder="placeholder || '请输入'"
          clearable
          class="spreadsheet-filter-text-search__input"
          @update:model-value="value => updateClauseValue(index, value)"
          @blur="commitValue"
          @keydown.enter.exact.prevent="handleKeyEnter"
        />
      </div>
    </template>
  </div>
</template>

<style scoped lang="less">
.spreadsheet-filter-text-search {
  display: flex;
  align-items: center;
  width: 100%;

  &__clause {
    display: flex;
    align-items: center;
    min-width: 0;
    flex: 1 1 0;
  }

  &__logic {
    flex: 0 0 auto;
    margin: 0 8px;
    color: #646a73;
    font-size: 12px;
  }

  &__operator {
    width: 104px;
    flex: 0 0 104px;
    margin-right: 8px;
  }

  &__input {
    min-width: 0;
    flex: 1;
  }
}
</style>
