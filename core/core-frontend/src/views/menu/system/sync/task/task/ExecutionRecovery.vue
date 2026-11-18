<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { recheckExecutionApi, resolveExecutionApi } from '@/api/sync/syncTask'
import type {
  IExecutionRecovery,
  IExecutionRecoveryRequest,
  ITaskMaintenance
} from '@/api/sync/syncTask'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{
  taskId: string
  recovery: IExecutionRecovery
  active: boolean
  taskName?: string
  targetTable?: string
}>()
const emit = defineEmits<{ (event: 'updated', value: ITaskMaintenance): void }>()
const { t } = useI18n()
const executionOnly = computed(() => props.recovery.kind === 'EXECUTION')
const manualLabel = computed(() =>
  t(
    executionOnly.value
      ? 'sync_maintenance.recovery_execution_manual'
      : 'sync_maintenance.recovery_manual'
  )
)
const busy = ref(false)
const error = ref('')
const visible = ref(false)
const result = ref<'SUCCESS' | 'FAIL'>()
const note = ref('')
const confirmed = ref(false)
let generation = 0
watch(
  () => [props.taskId, props.recovery.logId, props.active],
  () => {
    generation++
    busy.value = false
    error.value = ''
    visible.value = false
    result.value = undefined
    note.value = ''
    confirmed.value = false
  }
)
const checkedAt = computed(() =>
  props.recovery.lastChecked
    ? new Date(props.recovery.lastChecked).toLocaleString()
    : t('sync_maintenance.recovery_never_checked')
)

/**
 * 迟到响应不能操作另一任务或已关闭的人工核实弹窗，恢复结果仍由父弹窗统一更新
 */
const execute = async (manual: boolean) => {
  if (busy.value || !props.active || !props.recovery.canResolve) return
  if (manual && (!result.value || !note.value.trim() || !confirmed.value)) return
  const version = generation
  const taskId = props.taskId
  const logId = props.recovery.logId
  const request: IExecutionRecoveryRequest = {
    taskId,
    logId,
    configFingerprint: props.recovery.configFingerprint,
    ...(manual ? { result: result.value, note: note.value.trim(), confirmed: confirmed.value } : {})
  }
  busy.value = true
  error.value = ''
  try {
    const response = await (manual ? resolveExecutionApi(request) : recheckExecutionApi(request))
    if (
      !props.active ||
      version !== generation ||
      taskId !== props.taskId ||
      logId !== props.recovery.logId
    )
      return
    visible.value = false
    emit('updated', response.data)
  } catch (cause) {
    if (version === generation && taskId === props.taskId && logId === props.recovery.logId)
      error.value = cause instanceof Error ? cause.message : String(cause)
  } finally {
    if (version === generation) busy.value = false
  }
}
onBeforeUnmount(() => generation++)
</script>

<template>
  <section class="execution-recovery">
    <p v-if="taskName || targetTable">
      {{ t('sync_maintenance.recovery_context', [taskName || '-', targetTable || '-']) }}
    </p>
    <p>{{ t('sync_maintenance.recovery_log', [recovery.logId]) }}</p>
    <el-alert
      :title="
        t(
          executionOnly
            ? recovery.manualRequired
              ? 'sync_maintenance.recovery_execution_required'
              : 'sync_maintenance.recovery_execution_pending'
            : recovery.manualRequired
            ? 'sync_maintenance.recovery_manual_required'
            : 'sync_maintenance.recovery_checking'
        )
      "
      :description="recovery.reason"
      type="warning"
      :closable="false"
      show-icon
    />
    <p>{{ t('sync_maintenance.recovery_last_checked', [checkedAt, recovery.checks]) }}</p>
    <el-alert v-if="error && !visible" :title="error" type="error" :closable="false" />
    <div class="recovery-actions">
      <el-button
        :disabled="busy || !recovery.canResolve"
        :type="recovery.manualRequired ? 'default' : 'primary'"
        @click="execute(false)"
      >
        {{ t('sync_maintenance.recovery_recheck') }}
      </el-button>
      <el-button
        :disabled="busy || !recovery.canResolve"
        :type="recovery.manualRequired ? 'primary' : 'default'"
        @click="visible = true"
      >
        {{ manualLabel }}
      </el-button>
    </div>
    <el-dialog
      v-model="visible"
      :title="manualLabel"
      width="min(620px, 94vw)"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="!busy"
      :show-close="!busy"
      class="execution-recovery-confirmation"
    >
      <div class="recovery-form">
        <p v-if="taskName || targetTable">
          {{ t('sync_maintenance.recovery_context', [taskName || '-', targetTable || '-']) }}
        </p>
        <el-alert
          :title="
            t(
              executionOnly
                ? 'sync_maintenance.recovery_execution_help'
                : 'sync_maintenance.recovery_manual_help'
            )
          "
          type="warning"
          :closable="false"
          show-icon
        />
        <el-radio-group v-model="result" :disabled="busy">
          <el-radio v-if="!executionOnly" label="SUCCESS">{{
            t('sync_maintenance.recovery_success')
          }}</el-radio>
          <el-radio label="FAIL">{{
            t(
              executionOnly
                ? 'sync_maintenance.recovery_execution_failed'
                : 'sync_maintenance.recovery_failed'
            )
          }}</el-radio>
        </el-radio-group>
        <el-input
          v-model="note"
          type="textarea"
          :rows="4"
          :maxlength="2000"
          show-word-limit
          :disabled="busy"
          :placeholder="t('sync_maintenance.recovery_note')"
        />
        <el-checkbox v-model="confirmed" :disabled="busy">{{
          t('sync_maintenance.recovery_confirm')
        }}</el-checkbox>
        <el-alert v-if="error" :title="error" type="error" :closable="false" />
      </div>
      <template #footer>
        <el-button :disabled="busy" @click="visible = false">{{ t('common.cancel') }}</el-button>
        <el-button
          type="primary"
          :disabled="busy || !result || !note.trim() || !confirmed"
          @click="execute(true)"
          >{{ t('sync_maintenance.recovery_record') }}</el-button
        >
      </template>
    </el-dialog>
  </section>
</template>

<style lang="less">
.execution-recovery {
  display: flex;
  flex-direction: column;
  gap: 12px;
  p {
    margin: 0;
    color: var(--ed-text-color-secondary);
    font-size: 12px;
  }
  .recovery-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}
.execution-recovery-confirmation {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 48px);
  .ed-dialog__header,
  .ed-dialog__footer {
    flex-shrink: 0;
  }
  .ed-dialog__body {
    min-height: 0;
    overflow-y: auto;
  }
  .recovery-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  .ed-radio-group {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  .ed-checkbox {
    height: auto;
    align-items: flex-start;
  }
  .ed-checkbox__label {
    white-space: normal;
    line-height: 22px;
  }
  .ed-alert__title,
  .ed-alert__description {
    overflow-wrap: anywhere;
  }
}
</style>
