<script setup lang="ts">
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import { normalizeSpreadsheetFilterTextSearchClauses } from '../../utils/filter-condition-rules'

const props = defineProps<{
  condition: SpreadsheetFilterCondition
}>()

const conditionTypeOptions = [
  { label: '单条件', value: 'single' },
  { label: '与条件', value: 'and' },
  { label: '或条件', value: 'or' }
] as const

const operatorOptions = [
  { label: '精确匹配', value: 'eq' },
  { label: '模糊匹配', value: 'like' }
] as const

const handleConditionTypeChange = () => {
  props.condition.textSearchDefaultClauses = normalizeSpreadsheetFilterTextSearchClauses(
    props.condition.textSearchDefaultClauses,
    props.condition.textSearchConditionType
  )
}
</script>

<template>
  <el-form-item label="条件类型">
    <el-radio-group
      v-model="condition.textSearchConditionType"
      @change="handleConditionTypeChange"
    >
      <el-radio
        v-for="option in conditionTypeOptions"
        :key="option.value"
        :label="option.value"
      >
        {{ option.label }}
      </el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="">
    <el-checkbox v-model="condition.hideTextSearchConditionSwitch">
      隐藏条件切换
    </el-checkbox>
  </el-form-item>
  <el-form-item label="设置默认值" class="spreadsheet-filter-text-search-config__default-item">
    <div class="spreadsheet-filter-text-search-config__clauses">
      <div
        v-for="(clause, index) in condition.textSearchDefaultClauses"
        :key="index"
        :class="[
          'spreadsheet-filter-text-search-config__clause',
          condition.hideTextSearchConditionSwitch && 'is-operator-hidden'
        ]"
      >
        <span class="spreadsheet-filter-text-search-config__logic">
          {{ index > 0 ? (condition.textSearchConditionType === 'and' ? '且' : '或') : '' }}
        </span>
        <el-select
          v-if="!condition.hideTextSearchConditionSwitch"
          v-model="clause.operator"
          class="spreadsheet-filter-text-search-config__operator"
        >
          <el-option
            v-for="option in operatorOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-input
          v-model="clause.value"
          class="spreadsheet-filter-text-search-config__value"
        />
      </div>
    </div>
  </el-form-item>
</template>

<style scoped lang="less">
.spreadsheet-filter-text-search-config {
  &__default-item {
    align-items: flex-start;
  }

  &__clauses {
    width: 100%;
  }

  &__clause {
    display: grid;
    grid-template-columns: 24px 112px minmax(0, 1fr);
    align-items: center;
    column-gap: 0;
    min-height: 32px;

    & + & {
      margin-top: 8px;
    }

    &.is-operator-hidden {
      grid-template-columns: 24px minmax(0, 1fr);
    }
  }

  &__logic {
    display: flex;
    align-items: center;
    width: 24px;
    height: 32px;
    color: #646a73;
    font-size: 12px;
    line-height: 32px;
  }

  &__operator {
    width: 112px;
  }

  &__value {
    width: 100%;
    min-width: 0;
  }

  &__value :deep(.ed-input__wrapper) {
    height: 32px;
    padding: 0;
    border: 0;
    border-bottom: 1px solid rgba(31, 35, 41, 0.3);
    border-radius: 0;
    box-shadow: none !important;
    background: transparent;
  }
}
</style>
