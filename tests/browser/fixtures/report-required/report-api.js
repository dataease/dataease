import { reactive } from 'vue'
export const reportSaveState = reactive({ valid: true, saved: 0 })
export const reportInfoApi = async () => ({ data: {} })
export const reportCreateApi = async () => {
  reportSaveState.saved++
  return { code: 0 }
}
export const reportUpdateApi = reportCreateApi
