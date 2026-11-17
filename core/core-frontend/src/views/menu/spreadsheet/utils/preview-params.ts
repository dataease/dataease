import { useEmbedded } from '@/store/modules/embedded'

export const getSpreadsheetPreviewParams = (spreadsheetId: string | number): URLSearchParams => {
  const [path, search] = window.location.hash.split('?')
  const embeddedStore = useEmbedded()
  // 嵌入当前窗口只切组件，地址仍属于来源看板，不能继续读取来源的跳转和外参。
  if (
    path !== '#/spreadsheet-preview' &&
    embeddedStore.getType === 'Spreadsheet' &&
    String(embeddedStore.resourceId) === String(spreadsheetId) &&
    embeddedStore.jumpInfoParam
  ) {
    return new URLSearchParams({ jumpInfoParam: embeddedStore.jumpInfoParam })
  }
  return new URLSearchParams(search || window.location.search)
}
