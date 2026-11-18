import zh from '@/locales/zh-CN'
import tw from '@/locales/tw'
import en from '@/locales/en'
const lang = new URLSearchParams(location.search).get('lang') || 'zh-CN'
const messages = { 'zh-CN': zh, tw, en }[lang]
export const useI18n = () => ({
  t: (key) =>
    key.split('.').reduce((value, part) => value?.[part], messages) ?? key
})
export const importUserApi = async (data) => {
  const file = data.get('file')
  window.importedFiles = [...(window.importedFiles || []), file.name]
  return { data: { successCount: 1, errorCount: 0, dataKey: null } }
}
export const downExcelTemplateApi = () => {
  throw new Error('Template download is outside this fixture')
}
export const downErrorRecordApi = () => {
  throw new Error('Error download is outside this fixture')
}
export const clearErrorApi = () => {
  throw new Error('No real cache writes in this fixture')
}
