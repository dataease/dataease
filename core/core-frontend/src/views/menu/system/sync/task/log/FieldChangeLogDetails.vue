<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import dayjs from 'dayjs'
import { getTaskFieldChangeLogApi } from '@/api/sync/syncTaskLog'
import type { TaskFieldChangeLog } from '@/api/sync/syncTaskLog'
import { useI18n } from '@/hooks/web/useI18n'
import FieldChangeDetails from '../task/FieldChangeDetails.vue'
import FieldExecutionResults from '../task/FieldExecutionResults.vue'
import { getFieldChanges } from '../task/fieldChanges'
import { taskLogStatusKeys } from './logStatus'

const { t } = useI18n()
const emit = defineEmits<{ (event: 'closed'): void }>()
const visible = ref(false)
const loading = ref(false)
const readFailed = ref(false)
const record = ref<TaskFieldChangeLog>()
const logId = ref('')
let version = 0
let timer: ReturnType<typeof setTimeout> | undefined
const changes = computed(() =>
  record.value
    ? getFieldChanges(record.value.beforeFields, record.value.afterFields, record.value.progress)
    : []
)
const formatTime = (value: number | null) =>
  value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : t('sync_task_log.unknown')
const statusLabel = (status: string) =>
  taskLogStatusKeys[status] ? t(taskLogStatusKeys[status]) : t('sync_task_log.unknown')

/**
 * 弹窗切换或关闭后拒绝旧响应，读取失败由用户重试，不把上一轮内容留在新编号下
 */
const read = async (currentVersion = version) => {
  clearTimeout(timer)
  loading.value = true
  readFailed.value = false
  try {
    const { data } = await getTaskFieldChangeLogApi(logId.value)
    if (currentVersion !== version || !visible.value) return
    record.value = data
    if (!data.deletable) timer = setTimeout(() => read(currentVersion), 3000)
  } catch {
    if (currentVersion === version) readFailed.value = true
  } finally {
    if (currentVersion === version) loading.value = false
  }
}
const openLog = async (id: string) => {
  clearTimeout(timer)
  version++
  logId.value = id
  record.value = undefined
  visible.value = true
  await read()
}
const close = () => {
  version++
  clearTimeout(timer)
  record.value = undefined
  emit('closed')
}
onBeforeUnmount(() => {
  version++
  clearTimeout(timer)
})
defineExpose({ openLog })
</script>

<template>
  <el-drawer
    v-model="visible"
    :title="t('sync_task_log.details')"
    :close-on-click-modal="false"
    size="calc(100% - 50px)"
    direction="btt"
    @close="close"
  >
    <template #title>
      <div class="history-title">
        <span>{{ t('sync_task_log.details') }}</span>
        <el-button :loading="loading" @click="read()">{{ t('common.refresh') }}</el-button>
      </div>
    </template>
    <div v-loading="loading" class="field-change-log-details">
      <el-alert
        v-if="readFailed"
        :title="t('sync_task_log.read_failed')"
        type="error"
        :closable="false"
        show-icon
      />
      <template v-if="record">
        <div class="history-meta">
          <span
            ><label>{{ t('sync_task.task_name') }}</label
            >{{ record.jobName }}</span
          >
          <span
            ><label>{{ t('sync_task.execute_time') }}</label
            >{{ formatTime(record.executorStartTime) }}</span
          >
          <span
            ><label>{{ t('sync_task_log.operator') }}</label
            >{{ record.operatorName || t('sync_task_log.unknown') }}</span
          >
          <span
            ><label>{{ t('sync_task_log.target_table') }}</label
            >{{ record.targetTable || t('sync_task_log.unknown') }}</span
          >
          <span class="history-status" :class="record.status.toLowerCase()">{{
            statusLabel(record.status)
          }}</span>
        </div>
        <p class="history-note">{{ t('sync_task_log.planned_changes') }}</p>
        <FieldChangeDetails
          :changes="changes"
          :after-label="
            record.status === 'SUCCESS'
              ? t('sync_maintenance.after')
              : t('sync_task_log.planned_after')
          "
        />
        <div class="history-results">
          <FieldExecutionResults :steps="record.progress" :summarize-error="message => message" />
        </div>
        <el-alert v-if="record.error" :title="record.error" type="error" :closable="false" />
        <div v-if="record.initializationLogId" class="initialization-result">
          <span
            >{{ t('sync_task_log.initialization_result') }}：{{
              statusLabel(record.initializationStatus)
            }}</span
          >
          <span class="history-note"
            >{{ t('sync_task.log_id') }}：{{ record.initializationLogId }}</span
          >
          <el-alert
            v-if="record.initializationError"
            :title="record.initializationError"
            type="error"
            :closable="false"
          />
        </div>
        <div class="history-footer">
          <span>{{ t('sync_task.log_id') }}：{{ record.id }}</span>
          <el-button
            v-if="record.successorLogId"
            link
            type="primary"
            @click="openLog(record.successorLogId)"
            >{{ t('sync_task_log.successor') }}</el-button
          >
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<style scoped lang="less">
.history-title {
  display: flex;
  align-items: center;
  gap: 24px;
}
.field-change-log-details {
  min-height: 200px;
  overflow-wrap: anywhere;
}
.history-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--ed-border-color);
  label {
    margin-right: 8px;
    color: var(--ed-text-color-secondary);
  }
}
.history-status {
  margin-left: auto;
  color: var(--ed-color-warning);
  &.success {
    color: var(--ed-color-success);
  }
  &.fail {
    color: var(--ed-color-danger);
  }
  &.superseded {
    color: var(--ed-text-color-secondary);
  }
}
.history-note,
.history-footer {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
}
.history-note {
  margin: 20px 0 12px;
}
.history-results {
  margin-top: 24px;
}
.initialization-result {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
  .history-note {
    margin: 0;
  }
}
.history-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
  margin-top: 24px;
}
</style>
