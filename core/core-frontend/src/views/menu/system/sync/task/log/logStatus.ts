import type { TaskEventLog } from '@/api/sync/syncTaskLog'

export const taskLogStatusKeys: Record<string, string> = {
  FAIL_RETRY: 'sync_task.status_failed',
  NO_PROCESS: 'sync_task.status_failed',
  FAIL: 'sync_task.status_failed',
  RUNNING: 'sync_task.status_running',
  SUCCESS: 'sync_task.status_success',
  CONNECTION_LOST: 'sync_task.status_connection_lost',
  TERMINATION: 'sync_task.status_terminated',
  PARTIAL: 'sync_task_log.partial',
  WAITING_INITIALIZATION: 'sync_task_log.pending_initialization',
  RECOVERY_REQUIRED: 'sync_task_log.recovery_required',
  SUPERSEDED: 'sync_task_log.superseded'
}

/**
 * 待初始化或待恢复的字段变更可能由其他页面推进，列表继续更新其状态
 */
export const isTaskEventLogActive = (row: TaskEventLog) =>
  row.taskType === 'FIELD_CHANGE' ? !row.deletable : row.status === 'RUNNING'
