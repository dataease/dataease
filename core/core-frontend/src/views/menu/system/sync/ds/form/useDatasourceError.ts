import { h, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { errorBody, toDataEaseError } from '@/config/axios/error'
import { wasErrorPresented } from '@/config/axios/errorPresenter'
import { useI18n } from '@/hooks/web/useI18n'
import ConnectionErrorNotice from './ConnectionErrorNotice.vue'

/**
 * 同步数据库操作共用提示，兼容异常响应和连接校验的状态响应
 */
export const useDatasourceError = () => {
  const { t } = useI18n()
  let notice: ReturnType<typeof ElMessage> | undefined
  const clear = () => {
    notice?.close()
    notice = undefined
  }
  onBeforeUnmount(clear)
  const show = (message: string, diagnostic: string, requestId?: string, saveFailed = false) => {
    clear()
    const details = [
      diagnostic.startsWith(`${message}\n`) ? diagnostic.slice(message.length + 1) : diagnostic,
      requestId && !message.includes(requestId) && !diagnostic.includes(requestId)
        ? t('sync_maintenance.full_request_id', [requestId])
        : ''
    ]
      .filter(Boolean)
      .join('\n')
    notice = ElMessage({
      type: 'error',
      customClass: 'de-message-error de-message-export sync-task-error-message',
      message: details
        ? h(ConnectionErrorNotice, { message, diagnostic: details, saveFailed })
        : message,
      showClose: true,
      duration: details ? 0 : 3000
    })
  }
  const present = (error: unknown, options: { fallback?: string; saveFailed?: boolean } = {}) => {
    if (wasErrorPresented(error)) return
    const failure = toDataEaseError(error)
    const body = errorBody(failure.response?.data)
    const data = body.data
    const diagnostic =
      failure.category !== 'INTERNAL' &&
      data &&
      typeof data === 'object' &&
      'connectionDiagnostic' in data &&
      typeof data.connectionDiagnostic === 'string'
        ? data.connectionDiagnostic
        : ''
    show(
      body.msg || failure.message || options.fallback || t('sync_datasource.operation_failed'),
      diagnostic,
      failure.requestId,
      options.saveFailed
    )
    failure.presented = true
  }
  const presentStatus = (value: unknown, fallback: string) => {
    const data = value && typeof value === 'object' ? value : {}
    const diagnostic =
      'connectionDiagnostic' in data && typeof data.connectionDiagnostic === 'string'
        ? data.connectionDiagnostic
        : ''
    // 旧服务端的 statusRemark 可能是未经处理的数据库原文，只消费新诊断契约中的摘要
    const message =
      diagnostic && 'statusRemark' in data && typeof data.statusRemark === 'string'
        ? data.statusRemark
        : fallback
    const requestId =
      'requestId' in data && typeof data.requestId === 'string' ? data.requestId : undefined
    show(message, diagnostic, requestId)
  }
  return { clear, present, presentStatus }
}
