<script lang="ts" setup>
import { reactive, watch } from 'vue'
import type { FieldItemData } from '../../types/plugin'

const props = defineProps<{
  modelValue: boolean
  field: FieldItemData | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [field: FieldItemData]
}>()

const form = reactive({
  chartShowName: '',
  desc: ''
})

watch(
  () => [props.modelValue, props.field] as const,
  ([visible, field]) => {
    if (!visible) {
      return
    }

    form.chartShowName = field?.chartShowName || field?.name || ''
    form.desc = field?.desc || ''
  },
  { immediate: true }
)

const closeDialog = () => {
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  if (!props.field) {
    closeDialog()
    return
  }

  const chartShowName = form.chartShowName.trim()

  emit('confirm', {
    ...props.field,
    chartShowName: chartShowName && chartShowName !== props.field.name ? chartShowName : undefined,
    desc: form.desc
  })
  closeDialog()
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑显示名称"
    width="480px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="true"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-width="110px" class="field-display-name-form">
      <el-form-item label="字段原始名称">
        <div class="field-original-name">{{ field?.name || '' }}</div>
      </el-form-item>
      <el-form-item label="字段显示名称">
        <el-input v-model="form.chartShowName" />
      </el-form-item>
      <el-form-item label="字段描述">
        <el-input v-model="form.desc" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.field-display-name-form {
  .field-original-name {
    color: #1f2329;
    word-break: break-all;
  }
}
</style>
