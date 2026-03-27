import { ElLoading } from 'element-plus-secondary'
let loadingInstance = null

export const useLoading = (customClass = '', text = '') => {
  const open = () => {
    loadingInstance = ElLoading.service({
      fullscreen: true,
      customClass,
      text
    })
  }

  const close = () => {
    loadingInstance?.close()
  }

  return {
    open,
    close
  }
}
