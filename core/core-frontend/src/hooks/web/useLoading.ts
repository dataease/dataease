import { ElLoading } from 'element-plus-secondary'
let loadingInstance = null

export const useLoading = () => {
  const open = () => {
    loadingInstance = ElLoading.service({ fullscreen: true })
  }

  const close = () => {
    loadingInstance?.close()
  }

  return {
    open,
    close
  }
}
