<script setup lang="ts">
import { isRequestError, toDataEaseError } from '@/config/axios/error'
import { computed, onBeforeUnmount, ref } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { fullRefreshTaskApi, getTaskMaintenanceApi } from '@/api/sync/syncTask'
import type { ITaskMaintenance } from '@/api/sync/syncTask'
import { useI18n } from '@/hooks/web/useI18n'
import generateID from '@/utils/generateID'
import ExecutionRecovery from './ExecutionRecovery.vue'

const { t } = useI18n()
const emit = defineEmits<{ (event: 'submitted', taskId: string): void }>()
const visible = ref(false)
const loading = ref(false)
const error = ref<{ message: string; requestId?: string; isSubmission: boolean }>()
let viewVersion = 0
/**
 * 每次打开和关闭都更新会话编号，迟到的读取或提交结果不能操作另一任务的弹窗
 */
const isCurrentView = (version: number) => visible.value && version === viewVersion
const close = () => {
  viewVersion++
  visible.value = false
  error.value = undefined
}
const showError = (cause: unknown, isSubmission = false) => {
  error.value = {
    message:
      isRequestError(cause) && cause.response?.status === 404
        ? t('sync_maintenance.backend_unavailable')
        : typeof cause === 'string'
        ? cause
        : cause instanceof Error
        ? cause.message
        : t('sync_maintenance.read_failed'),
    requestId: isRequestError(cause) ? toDataEaseError(cause).requestId : undefined,
    isSubmission
  }
}
const exclusive = ref(false)
const data = ref<ITaskMaintenance>()
const requestId = ref('')
const supported = computed(() => data.value?.structure?.fullRefresh && !data.value?.blockedReason)
const blockedReason = computed(() =>
  data.value?.blockedCode === 'UNSUPPORTED_TARGET' ||
  (data.value?.structure && !data.value.structure.fullRefresh && !data.value.blockedReason)
    ? t('sync_maintenance.full_unsupported', [data.value.targetName, data.value.targetType])
    : data.value?.blockedReason
)
/**
 * 每次打开读取当前生效配置与能力，为本次确认生成请求编号，不复用上次确认勾选
 */
const open = async (taskId: string) => {
  const version = ++viewVersion
  data.value = undefined
  error.value = undefined
  exclusive.value = false
  requestId.value = generateID()
  visible.value = true
  loading.value = true
  try {
    const response = await getTaskMaintenanceApi(taskId, true)
    if (isCurrentView(version)) data.value = response.data
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 一次全量只提交当前配置指纹和独占写入确认，不提交字段草稿或修改原定时设置
 */
const submit = async () => {
  if (!data.value || !exclusive.value || !supported.value || loading.value) return
  const version = viewVersion
  const taskId = data.value.taskId
  error.value = undefined
  loading.value = true
  try {
    await fullRefreshTaskApi({
      taskId,
      operationId: requestId.value,
      configFingerprint: data.value.configFingerprint,
      changes: [],
      confirmed: true,
      exclusiveTargetConfirmed: true
    })
    // 原任务确已提交时仍通知列表刷新，但只有原会话可以显示成功并关闭弹窗
    emit('submitted', taskId)
    if (!isCurrentView(version)) return
    ElMessage.success(t('sync_maintenance.full_submitted'))
    close()
  } catch (cause) {
    if (isCurrentView(version)) showError(cause, true)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
onBeforeUnmount(() => viewVersion++)
defineExpose({ open })
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="t('sync_maintenance.full_once')"
    width="min(560px, 94vw)"
    class="full-refresh-dialog"
    align-center
    :close-on-click-modal="false"
    :before-close="close"
  >
    <div v-loading="loading" class="full-refresh-content">
      <ExecutionRecovery
        v-if="data?.executionRecovery"
        :key="data.taskId + data.executionRecovery.logId"
        :task-id="data.taskId"
        :task-name="data.taskName"
        :target-table="data.targetTable"
        :active="visible"
        :recovery="data.executionRecovery"
        @updated="open(data.taskId)"
      />
      <el-alert
        v-if="!data?.executionRecovery"
        :title="t('sync_maintenance.full_warning')"
        type="warning"
        :closable="false"
        show-icon
      />
      <p v-if="!data?.executionRecovery">{{ t('sync_maintenance.full_mode') }}</p>
      <el-alert
        v-if="error || (blockedReason && !data?.executionRecovery)"
        :title="error?.message || blockedReason"
        type="error"
        :closable="false"
      >
        <template v-if="error" #default>
          <p v-if="error.isSubmission" class="full-refresh-error-help">
            {{ t('sync_maintenance.full_submit_error_help') }}
          </p>
          <p v-if="error.requestId" class="full-refresh-error-help">
            {{ t('sync_maintenance.full_request_id', [error.requestId]) }}
          </p>
        </template>
      </el-alert>
      <el-checkbox v-if="!data?.executionRecovery" v-model="exclusive" :disabled="!supported">{{
        t('sync_maintenance.full_exclusive')
      }}</el-checkbox>
    </div>
    <template #footer>
      <el-button @click="close">{{ t('common.cancel') }}</el-button>
      <el-button
        v-if="!data?.executionRecovery"
        type="primary"
        :disabled="!exclusive || !supported || loading"
        @click="submit"
      >
        {{ t('sync_maintenance.full_once') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less">
.full-refresh-dialog {
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
  .ed-dialog__footer {
    padding-top: 16px;
  }
  .full-refresh-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    line-height: 22px;
  }
  .ed-alert__title {
    overflow-wrap: anywhere;
    line-height: 22px;
  }
  .full-refresh-error-help {
    margin: 4px 0 0;
    overflow-wrap: anywhere;
    line-height: 22px;
  }
  .ed-checkbox {
    align-items: flex-start;
    height: auto;
    margin: 0;
    .ed-checkbox__input {
      margin-top: 4px;
    }
    .ed-checkbox__label {
      white-space: normal;
      line-height: 22px;
    }
  }
}
</style>
