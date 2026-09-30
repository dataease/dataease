<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { getFontSettings, saveFontSettings } from '@/api/font'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const ready = ref(false)
const model = reactive({ maxUploadMb: 20, maxStorageMb: 512 })
const usedBytes = ref(0)
const allowedMb = ref(0)
const usedMb = computed(() => (usedBytes.value / 1024 / 1024).toFixed(2))
const valid = computed(
  () =>
    ready.value &&
    Number.isInteger(model.maxUploadMb) &&
    Number.isInteger(model.maxStorageMb) &&
    model.maxUploadMb >= 1 &&
    model.maxUploadMb <= allowedMb.value &&
    model.maxStorageMb >= model.maxUploadMb &&
    model.maxStorageMb <= 1048576
)
const open = async () => {
  visible.value = true
  loading.value = true
  ready.value = false
  try {
    const data = await getFontSettings()
    model.maxUploadMb = data.maxUploadMb
    model.maxStorageMb = data.maxStorageMb
    usedBytes.value = data.usedBytes
    allowedMb.value = data.maxUploadAllowedMb
    ready.value = true
  } finally {
    loading.value = false
  }
}
const save = async () => {
  if (!valid.value || saving.value) return
  saving.value = true
  try {
    await saveFontSettings({ ...model })
    ElMessage.success(t('system.setting_successful'))
    visible.value = false
  } finally {
    saving.value = false
  }
}
defineExpose({ open })
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="t('system.font_limits_title')"
    width="480px"
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    :show-close="!saving"
  >
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-form v-else-if="ready" label-position="top" :disabled="saving">
      <el-form-item :label="t('system.font_file_limit')">
        <el-input-number
          v-model="model.maxUploadMb"
          :min="1"
          :max="Math.max(1, allowedMb)"
          :precision="0"
          controls-position="right"
        />
        <span class="unit">MB</span>
      </el-form-item>
      <el-form-item :label="t('system.font_storage_limit')">
        <el-input-number
          v-model="model.maxStorageMb"
          :min="1"
          :max="1048576"
          :precision="0"
          controls-position="right"
        />
        <span class="unit">MB</span>
        <div class="hint">{{ t('system.font_storage_used', { size: usedMb }) }}</div>
      </el-form-item>
      <el-alert
        v-if="ready && usedBytes > model.maxStorageMb * 1024 * 1024"
        :title="t('system.font_quota_warning')"
        type="warning"
        :closable="false"
      />
      <p class="hint">{{ t('system.font_limits_hint') }}</p>
    </el-form>
    <template #footer>
      <el-button :disabled="saving" @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="saving" :disabled="!valid || loading" @click="save">{{
        t('common.save')
      }}</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.unit {
  margin-left: 8px;
}
.hint {
  width: 100%;
  margin-top: 8px;
  color: var(--ed-text-color-secondary, #646a73);
  font-size: 12px;
  line-height: 20px;
}
</style>
