<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
import { useI18n } from '@/hooks/web/useI18n'
import { revealSensitive, type SecretType } from '@/api/sensitive'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ resourceId: string | number; secretType: SecretType }>()
const { t } = useI18n()
const { toClipboard } = useClipboard()
const resultArea = ref<HTMLElement>()
const visible = ref(false)
const password = ref('')
const value = ref<string | null>(null)
const busy = ref(false)
const error = ref('')
const remainingSeconds = ref(30)
let generation = 0
let timer: ReturnType<typeof setTimeout> | undefined
let countdownTimer: ReturnType<typeof setInterval> | undefined
const business = computed(() =>
  props.secretType.startsWith('ENGINE_')
    ? 'engine'
    : props.secretType.startsWith('SYNC_')
    ? 'sync'
    : props.secretType === 'DATASOURCE_REMOTE_PASSWORD'
    ? 'remote'
    : 'datasource'
)
const clear = () => {
  generation++
  clearTimeout(timer)
  clearInterval(countdownTimer)
  remainingSeconds.value = 30
  password.value = ''
  value.value = null
  error.value = ''
  busy.value = false
}
watch(visible, open => {
  if (!open) clear()
})
watch(
  () => [props.resourceId, props.secretType],
  () => {
    visible.value = false
    clear()
  }
)
onBeforeUnmount(clear)
const reveal = async () => {
  if (!password.value || busy.value) return
  busy.value = true
  error.value = ''
  const current = ++generation
  try {
    const plain = await revealSensitive(props.resourceId, props.secretType, password.value)
    if (current !== generation || !visible.value) return
    value.value = plain
    remainingSeconds.value = 30
    const expiresAt = performance.now() + 30000
    countdownTimer = setInterval(() => {
      remainingSeconds.value = Math.max(0, Math.ceil((expiresAt - performance.now()) / 1000))
      if (remainingSeconds.value === 0) {
        visible.value = false
        clear()
      }
    }, 1000)
    timer = setTimeout(() => {
      visible.value = false
      clear()
    }, 30000)
  } catch {
    if (current === generation) error.value = t('sensitive_reveal.failed')
  } finally {
    if (current === generation) {
      password.value = ''
      busy.value = false
    }
  }
}
const copyValue = async () => {
  if (value.value === null || !resultArea.value) return
  const current = generation
  try {
    // Keep the temporary clipboard element inside the dialog's focus trap.
    await toClipboard(value.value, resultArea.value)
    if (current === generation && visible.value) ElMessage.success(t('common.copy_success'))
  } catch {
    if (current === generation && visible.value) ElMessage.warning(t('operate_log.copy_failed'))
  }
}
// The existing input eye icon opens this dialog; no additional trigger is rendered.
defineExpose({
  open: () => {
    clear()
    visible.value = true
  }
})
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="t(`sensitive_reveal.${business}_title`)"
    width="480px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
  >
    <template #header="{ titleId, titleClass }">
      <div :id="titleId" :class="[titleClass, 'sensitive-reveal-title']">
        <el-icon :size="20" color="var(--ed-color-warning)" aria-hidden="true">
          <WarningFilled />
        </el-icon>
        <span>{{ t(`sensitive_reveal.${business}_title`) }}</span>
      </div>
    </template>
    <template v-if="value === null">
      <p class="sensitive-reveal-hint">{{ t(`sensitive_reveal.${business}_hint`) }}</p>
      <el-form @submit.prevent="reveal">
        <el-form-item :label="t('sensitive_reveal.current_password')" :error="error">
          <el-input
            v-model="password"
            type="password"
            show-password
            autocomplete="current-password"
            :aria-label="t('sensitive_reveal.current_password')"
            @keyup.enter="reveal"
          />
        </el-form-item>
      </el-form>
    </template>
    <template v-else>
      <p class="sensitive-reveal-hint">
        {{ t('sensitive_reveal.auto_hide', { seconds: remainingSeconds }) }}
      </p>
      <pre
        ref="resultArea"
        class="sensitive-reveal-value"
        role="region"
        tabindex="0"
        :aria-label="t('sensitive_reveal.result')"
        v-text="value"
      />
    </template>
    <template #footer>
      <el-button @click="visible = false">{{ t('sensitive_reveal.close') }}</el-button>
      <el-button
        v-if="value === null"
        type="primary"
        :loading="busy"
        :disabled="!password"
        @click="reveal"
      >
        {{ t('sensitive_reveal.verify') }}
      </el-button>
      <el-button v-else type="primary" @click="copyValue">{{ t('common.copy') }}</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.sensitive-reveal-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sensitive-reveal-hint {
  margin: 0 0 16px;
}

.sensitive-reveal-value {
  max-height: 240px;
  min-height: 64px;
  padding: 12px;
  margin: 0;
  overflow: auto;
  font-family: monospace;
  line-height: 1.6;
  color: var(--ed-text-color-primary);
  overflow-wrap: anywhere;
  white-space: pre-wrap;
  background: var(--ed-fill-color-light);
  border-radius: 4px;
  user-select: text;
}
</style>
