import { ElMessage } from 'element-plus-secondary'

export default function toast(message = '') {
  ElMessage.error(message)
}
