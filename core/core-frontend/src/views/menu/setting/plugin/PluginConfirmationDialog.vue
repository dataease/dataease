<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { ElDialog, ElIcon, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus-secondary'

const props = defineProps<{
  t: (key: string) => string
  operation: 'install' | 'update' | 'uninstall' | 'confirm'
  verify: (password: string) => Promise<string>
}>()
const emit = defineEmits<{ (event: 'confirmed', ticket: string): void; (event: 'cancel'): void }>()
const visible = ref(true)
const password = ref('')
const busy = ref(false)
const error = ref('')
let generation = 0
const clear = () => {
  generation++
  password.value = ''
  busy.value = false
}
onBeforeUnmount(clear)
const close = () => {
  visible.value = false
  clear()
  emit('cancel')
}
const confirm = async () => {
  if (!password.value || busy.value) return
  busy.value = true
  error.value = ''
  const current = ++generation
  try {
    const ticket = await props.verify(password.value)
    if (current === generation && visible.value) emit('confirmed', ticket)
  } catch (failure) {
    const key =
      failure && typeof failure === 'object' && 'messageKey' in failure
        ? failure.messageKey
        : undefined
    const messages = {
      i18n_plugin_password_incorrect: `plugin.password_incorrect_${props.operation}`,
      i18n_plugin_password_locked: 'plugin.password_locked',
      i18n_sensitive_password_unavailable: 'plugin.password_unavailable',
      i18n_error_forbidden: 'plugin.operation_forbidden'
    }
    if (current === generation) {
      error.value = props.t(
        typeof key === 'string' && Object.prototype.hasOwnProperty.call(messages, key)
          ? messages[key]
          : 'plugin.verification_failed'
      )
    }
  } finally {
    if (current === generation) {
      password.value = ''
      busy.value = false
    }
  }
}
</script>

<template>
  <ElDialog
    :model-value="visible"
    :title="t('plugin.confirm_title')"
    width="480px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    @update:model-value="close"
  >
    <template #header="{ titleId, titleClass }">
      <div :id="titleId" :class="[titleClass, 'plugin-confirmation-title']">
        <ElIcon :size="20" color="var(--ed-color-warning)" aria-hidden="true">
          <WarningFilled />
        </ElIcon>
        <span>{{ t('plugin.confirm_title') }}</span>
      </div>
    </template>
    <p class="plugin-confirmation-hint">{{ t('plugin.confirm_warning') }}</p>
    <ElForm @submit.prevent="confirm">
      <ElFormItem :label="t('plugin.current_password')" :error="error">
        <ElInput
          v-model="password"
          type="password"
          show-password
          autocomplete="current-password"
          :aria-label="t('plugin.current_password')"
          @keyup.enter="confirm"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="close">{{ t('plugin.close') }}</ElButton>
      <ElButton type="primary" :loading="busy" :disabled="!password" @click="confirm">
        {{ t('plugin.verify_continue') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
.plugin-confirmation-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.plugin-confirmation-hint {
  margin: 0 0 16px;
}
</style>
