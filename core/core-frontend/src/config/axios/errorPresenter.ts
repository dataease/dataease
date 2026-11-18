import axios from 'axios'
import { ElMessage } from 'element-plus-secondary'
import { DataEaseError, getErrorMessage } from './error'

export const wasErrorPresented = (error: unknown) =>
  axios.isCancel(error) || (error instanceof DataEaseError && error.presented)

/** Both the interceptor and local catches use this marker; intentional cancellation stays silent. */
export const presentError = (error: unknown, message?: string) => {
  if (wasErrorPresented(error)) return
  ElMessage.error(message || getErrorMessage(error))
  if (error instanceof DataEaseError) error.presented = true
}
