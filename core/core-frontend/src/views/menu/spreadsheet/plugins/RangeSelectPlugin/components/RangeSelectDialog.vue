<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { IUniverInstanceService, UniverInstanceType } from '@univerjs/core'
import { SheetsSelectionsService } from '@univerjs/sheets'
import { serializeRangeWithSheet, serializeRange } from '@univerjs/engine-formula'
import { IDialogService } from '@univerjs/ui'
import type { IRangeSelectDialogParams, IRangeSelectResult } from '../type'

const props = defineProps({
  injector: {
    type: Object,
    required: true
  },
  params: {
    type: Object as () => IRangeSelectDialogParams,
    required: false
  }
})

const rangeText = ref('')
let subscription: any

// 从 injector 获取必要的服务
const univerInstanceService = props.injector.get(IUniverInstanceService)
const selectionManagerService = props.injector.get(SheetsSelectionsService)
const dialogService = props.injector.get(IDialogService)

const getCurrentWorkbook = () => {
  return univerInstanceService.getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
}

const updateRangeText = () => {
  const workbook = getCurrentWorkbook()
  if (!workbook) return

  const worksheet = workbook.getActiveSheet()
  if (!worksheet) return

  const selections = selectionManagerService.getCurrentSelections()
  if (selections && selections.length > 0) {
    const range = selections[0].range
    const sheetName = worksheet.getName()
    // 使用 engine-formula 的工具函数转换为 A1 样式: Sheet1!A1:B10
    rangeText.value = serializeRangeWithSheet(sheetName, range)
  }
}

onMounted(() => {
  // 初始化显示当前选区
  updateRangeText()

  // 通过 SheetsSelectionsService 监听选区移动结束事件
  if (selectionManagerService) {
    subscription = selectionManagerService.selectionMoveEnd$.subscribe(() => {
      updateRangeText()
    })
  }
})

onUnmounted(() => {
  if (subscription) {
    subscription.unsubscribe()
  }
})

const handleConfirm = () => {
  if (props.params?.callback) {
    const workbook = getCurrentWorkbook()
    const worksheet = workbook?.getActiveSheet()
    const selections = selectionManagerService.getCurrentSelections()
    if (worksheet && selections && selections.length > 0) {
      const range = selections[0].range
      const sheetName = worksheet.getName()
      const result: IRangeSelectResult = {
        startRowNumber: range.startRow,
        startColumnNumber: range.startColumn,
        endRowNumber: range.endRow,
        endColumnNumber: range.endColumn,
        sheetId: worksheet.getSheetId(),
        sheetName: sheetName,
        range: serializeRange(range),
        fullRange: serializeRangeWithSheet(sheetName, range)
      }
      props.params.callback(result)
    }
  }
  props.params?.onClose?.()
  dialogService.close('RangeSelectDialog')
}

const handleCancel = () => {
  props.params?.onClose?.()
  dialogService.close('RangeSelectDialog')
}
</script>

<template>
  <div class="range-select-dialog-container">
    <div class="range-input-section">
      <div class="label">当前选择范围：</div>
      <el-input
        v-model="rangeText"
        placeholder="请在表格中选择区域"
        readonly
      />
    </div>
    <div class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>
  </div>
</template>

<style scoped>
.range-select-dialog-container {
  padding: 16px;
}

.range-input-section {
  margin-bottom: 24px;
}

.range-input-section .label {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-input__wrapper) {
  width: 100%;
}
</style>
