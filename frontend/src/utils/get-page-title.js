import defaultSettings from '@/settings'
import { getSysUI } from '@/utils/auth'

let title = defaultSettings.title || 'Vue Admin Template'

export default function getPageTitle(pageTitle) {
  const uiInfo = getSysUI()
  if (uiInfo && uiInfo['ui.title'] && uiInfo['ui.title'].paramValue) {
    title = uiInfo['ui.title'].paramValue
  }
  if (pageTitle) {
    return `${pageTitle} - ${title}`
  }
  return `${title}`
}
