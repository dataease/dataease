<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const props = defineProps<{ modelValue: number[]; allowed: number }>()
const emit = defineEmits<{ (event: 'update:modelValue', value: number[]): void }>()
const { t } = useI18n()
const choices = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
</script>
<template>
  <div class="visitor-permissions">
    <div>{{ t('share_visitor.title') }}</div>
    <p class="visitor-permissions-hint">{{ t('share_visitor.hint') }}</p>
    <el-checkbox-group v-model="choices">
      <el-checkbox
        :label="1"
        :disabled="!(allowed & 1)"
        :title="!(allowed & 1) ? t('share_visitor.no_permission') : ''"
        >{{ t('share_visitor.details') }}</el-checkbox
      >
      <el-checkbox
        :label="2"
        :disabled="!(allowed & 2)"
        :title="!(allowed & 2) ? t('share_visitor.no_permission') : ''"
        >{{ t('share_visitor.data') }}</el-checkbox
      >
      <el-checkbox
        :label="4"
        :disabled="!(allowed & 4)"
        :title="!(allowed & 4) ? t('share_visitor.no_permission') : ''"
        >{{ t('share_visitor.image') }}</el-checkbox
      >
    </el-checkbox-group>
    <p class="visitor-permissions-hint">{{ t('share_visitor.buttons_hint') }}</p>
  </div>
</template>
<style scoped lang="less">
.visitor-permissions {
  border-top: 1px solid var(--ed-border-color);
  padding: 16px;
  margin-top: 16px;
}
.visitor-permissions :deep(.ed-checkbox-group) {
  display: flex;
  flex-direction: column;
}
.visitor-permissions-hint {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
}
</style>
