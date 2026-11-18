import { reactive } from 'vue'
export const reportSaveState = reactive({
  valid: true,
  saved: 0,
  stepValid: true,
  stepThrows: false,
  delay: 0,
})
export const reportInfoApi = async () => ({ data: {} })
export const reportCreateApi = async () => {
  reportSaveState.saved++
  if (reportSaveState.delay)
    await new Promise((resolve) => setTimeout(resolve, reportSaveState.delay))
  return { code: 0 }
}
export const reportUpdateApi = reportCreateApi
