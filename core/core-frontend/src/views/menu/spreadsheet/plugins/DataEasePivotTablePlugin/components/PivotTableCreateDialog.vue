<script lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const _placement = ref<'new' | 'existing'>('new')
const _existingRange = ref('')
const _selectedRange = ref()
const _rangeSelecting = ref(false)
const _resultLimit = ref<string | number>(1000)

function _resetState() {
  _placement.value = 'new'
  _existingRange.value = ''
  _selectedRange.value = undefined
  _rangeSelecting.value = false
  _resultLimit.value = 1000
}
</script>

<script setup lang="ts">
import { ICommandService, type Injector } from '@univerjs/core'
import { IDialogService } from '@univerjs/ui'
import type { IRangeSelectResult } from '../../RangeSelectPlugin/type'
import type { ITableCreateDialogParams, ITableCreateResult } from '../../../types/editor'
import {
  clampSpreadsheetResultLimit,
  DEFAULT_SPREADSHEET_QUERY_LIMIT,
  getSpreadsheetQueryLimit
} from '../../../utils/query-limit'

const props = defineProps<{
  injector: Injector
  params?: ITableCreateDialogParams
}>()

const placement = _placement
const existingRange = _existingRange
const selectedRange = _selectedRange
const rangeSelecting = _rangeSelecting
const resultLimit = _resultLimit
const resultLimitMax = ref(DEFAULT_SPREADSHEET_QUERY_LIMIT)

const commandService = props.injector.get(ICommandService)
const dialogService = props.injector.get(IDialogService)

if (props.params) {
  props.params.canClose = () => !rangeSelecting.value
}

const normalizedResultLimit = computed(() => {
  return clampSpreadsheetResultLimit(resultLimit.value, resultLimitMax.value)
})
const confirmDisabled = computed(() => {
  // 区域选择弹窗打开期间，插入位置弹窗不能提前结束当前流程。
  if (rangeSelecting.value) {
    return true
  }
  return normalizedResultLimit.value <= 0 || (placement.value === 'existing' && !selectedRange.value)
})

const handleResultLimitChange = (value: string | number) => {
  resultLimit.value = clampSpreadsheetResultLimit(value, resultLimitMax.value)
}

onMounted(async () => {
  resultLimitMax.value = await getSpreadsheetQueryLimit()
  resultLimit.value = clampSpreadsheetResultLimit(resultLimit.value, resultLimitMax.value)
})

const applySelectedRange = (result: IRangeSelectResult, silent = false): boolean => {
  const message = props.params?.validateRange?.(result, normalizedResultLimit.value, silent)
  if (message) {
    selectedRange.value = undefined
    existingRange.value = ''
    return false
  }

  selectedRange.value = result
  existingRange.value = result.fullRange
  return true
}

const handleRangeClick = async () => {
  if (rangeSelecting.value) {
    return
  }

  rangeSelecting.value = true
  try {
    const opened = await commandService.executeCommand('dataease.operation.open-range-select-dialog', {
      callback: (result: IRangeSelectResult) => {
        applySelectedRange(result)
      },
      onClose: () => {
        rangeSelecting.value = false
      }
    })

    if (!opened) {
      rangeSelecting.value = false
    }
  } catch (e) {
    rangeSelecting.value = false
    throw e
  }
}

const handleConfirm = () => {
  if (rangeSelecting.value) {
    return
  }

  const result: ITableCreateResult = {
    pluginType: 'pivot',
    placement: placement.value,
    range: placement.value === 'existing' ? selectedRange.value : undefined,
    resultLimit: normalizedResultLimit.value
  }

  _resetState()
  dialogService.close('PivotTableCreateDialog')

  props.params?.callback(result)
}

const handleCancel = () => {
  _resetState()
  props.params?.onClose?.()
  dialogService.close('PivotTableCreateDialog')
  dialogService.close('RangeSelectDialog')
}

const handlePlacementChange = () => {
  if (placement.value === 'new') {
    dialogService.close('RangeSelectDialog')
    return
  }

  if (!selectedRange.value && props.params?.initialRange) {
    applySelectedRange(props.params.initialRange, true)
  }
}

onBeforeUnmount(_resetState)
</script>

<template>
  <div class="table-create-dialog">
    <div class="section-title">请选择透视表放置的位置：</div>

    <el-radio-group v-model="placement" class="placement-group" @change="handlePlacementChange">
      <el-radio label="new">新工作表</el-radio>
      <el-radio label="existing">现有工作表</el-radio>
    </el-radio-group>

    <div v-if="placement === 'existing'" class="range-selection-section">
      <div class="label">点击选择现有工作表中的区域：</div>
      <div class="range-input-wrapper" @click="handleRangeClick">
        <el-input
          v-model="existingRange"
          placeholder="点击选择区域"
          readonly
          class="range-input"
        >
          <template #suffix>
            <el-icon><i class="el-icon-location"></i></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <div class="result-limit-section">
      <div class="label">结果展示：</div>
      <el-input-number
        v-model="resultLimit"
        :min="1"
        :max="resultLimitMax"
        :precision="0"
        :controls="false"
        class="result-limit-input"
        @change="handleResultLimitChange"
      />
    </div>

    <div class="dialog-footer">
      <el-button :disabled="rangeSelecting" @click="handleCancel">取消</el-button>
      <el-button type="primary" :disabled="confirmDisabled" @click="handleConfirm">确定</el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
.table-create-dialog {
  padding: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #303133;
}

.placement-group {
  margin-bottom: 24px;
  display: flex;
  gap: 20px;
}

.range-selection-section {
  margin-top: 10px;
}

.range-selection-section .label,
.result-limit-section .label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.result-limit-section {
  margin-top: 16px;
  .result-limit-input {
    width: 100%;
  }
}

.range-input-wrapper,
.range-input-wrapper :deep(.el-input__wrapper),
.range-input :deep(.el-input__inner) {
  cursor: pointer;
}

.dialog-footer {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer :deep(.el-button--primary) {
  background-color: #3370ff;
  border-color: #3370ff;
  color: #fff;
}

.dialog-footer :deep(.el-button--primary:hover) {
  background-color: #5b8bff;
  border-color: #5b8bff;
  color: #fff;
}

.dialog-footer :deep(.el-button--primary:active) {
  background-color: #2860df;
  border-color: #2860df;
}
</style>
