<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus-secondary'
import type { SpreadsheetFilterTreeField } from '../../../../types/plugin'
import type { SpreadsheetFilterAvailableField } from '../../utils/events'

const props = defineProps<{
  visible: boolean
  fields: SpreadsheetFilterAvailableField[]
  modelValue: SpreadsheetFilterTreeField[]
  datasetId?: string | number
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirm: [value: SpreadsheetFilterTreeField[]]
}>()

interface DraftLevel {
  fieldId?: string | number
}

const levels = ref<DraftLevel[]>([])
const levelNames = ['一', '二', '三', '四', '五']
const selectableFields = computed(() =>
  props.fields.filter(field => field.groupType === 'd' && Number(field.deType) === 0)
)

watch(
  () => props.visible,
  visible => {
    if (!visible) return
    levels.value = props.modelValue.length
      ? props.modelValue.slice(0, 5).map(field => ({ fieldId: field.fieldId }))
      : [{ fieldId: undefined }]
  },
  { immediate: true }
)

const isFieldDisabled = (field: SpreadsheetFilterAvailableField, index: number) =>
  !!field.desensitized || levels.value.some(
    (level, levelIndex) => levelIndex !== index && String(level.fieldId) === String(field.fieldId)
  )

const addLevel = () => {
  if (levels.value.length >= 5) return
  levels.value.push({ fieldId: undefined })
}

const removeLevel = (index: number) => {
  levels.value.splice(index, 1)
  if (!levels.value.length) levels.value.push({ fieldId: undefined })
}

const cancel = () => emit('update:visible', false)

const confirm = () => {
  if (!props.datasetId || levels.value.some(level => level.fieldId === undefined)) {
    ElMessage.warning('请选择字段')
    return
  }
  const result = levels.value.map((level, order) => {
    const field = selectableFields.value.find(
      item => String(item.fieldId) === String(level.fieldId)
    )!
    return {
      fieldId: field.fieldId,
      fieldName: field.fieldName,
      datasetId: props.datasetId!,
      deType: Number(field.deType),
      order
    }
  })
  emit('confirm', result)
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="下拉树结构设计"
    width="600px"
    append-to-body
    @close="cancel"
  >
    <div class="spreadsheet-filter-tree-field-dialog">
      <el-button text type="primary" :disabled="levels.length >= 5" @click="addLevel">
        <el-icon><Plus /></el-icon>
        添加层级
      </el-button>
      <div class="spreadsheet-filter-tree-field-dialog__header">
        <span>层级</span>
        <span>树查询字段</span>
      </div>
      <div
        v-for="(level, index) in levels"
        :key="index"
        class="spreadsheet-filter-tree-field-dialog__row"
      >
        <span>层级{{ levelNames[index] }}</span>
        <el-select v-model="level.fieldId" placeholder="请选择字段">
          <el-option
            v-for="field in selectableFields"
            :key="field.fieldId"
            :label="field.fieldName"
            :value="field.fieldId"
            :disabled="isFieldDisabled(field, index)"
            :title="field.desensitized ? '该字段已脱敏，不能作为查询条件' : ''"
          />
        </el-select>
        <el-button text @click="removeLevel(index)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
    <template #footer>
      <el-button @click="cancel">取消</el-button>
      <el-button type="primary" @click="confirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.spreadsheet-filter-tree-field-dialog {
  &__header,
  &__row {
    display: grid;
    grid-template-columns: 96px 1fr 32px;
    align-items: center;
    gap: 12px;
    min-height: 44px;
  }

  &__header {
    color: #646a73;
    font-size: 12px;
  }
}
</style>
