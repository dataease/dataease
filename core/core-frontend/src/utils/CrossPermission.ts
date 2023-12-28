import { ElMessage, ElMessageBox } from 'element-plus-secondary'

export const check = (data, id?: string, weight?: number) => {
  if (!weight) {
    weight = 1
  }
  if (!id) {
    ElMessage.error('资源ID不能为空')
    return false
  }
  const node = getNode(data, id)
  if (!node || node < weight) {
    showMsg('无权访问当前资源，是否离开当前页面？系统将不保存您所做的更改', id)
    return false
  }
  if (window['cross-panel-' + id]) {
    ElMessageBox.close()
    window['cross-panel-' + id] = null
  }
  return true
}

const getNode = (data, id: string) => {
  if (!id) {
    return null
  }
  return data[id]
}

const showMsg = (msg: string, id: string) => {
  if (window['cross-panel-' + id]) {
    return
  }
  window['cross-panel-' + id] = ElMessageBox.confirm(msg, {
    confirmButtonType: 'primary',
    type: 'warning',
    confirmButtonText: '关闭页面',
    cancelButtonText: '取消',
    autofocus: false,
    showClose: false
  }).then(() => {
    window.close()
  })
}

export const compareStorage = (oldVal?: string, newVal?: string) => {
  if (oldVal === newVal) {
    return true
  }
  /* unfinished please do not delete
  let oldObj = null
  let newObj = null
  let oldText = null
  let newText = null
  if (oldVal) {
    oldObj = JSON.parse(oldVal)
    oldText = oldObj['v']
  }
  if (newVal) {
    newObj = JSON.parse(newVal)
    newText = newObj['v']
  }
  return oldText === newText */
}
