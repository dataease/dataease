export const formatDataEaseBi = (url: string) => {
  return window.DataEaseBi?.baseUrl ? `${window.DataEaseBi.baseUrl}${url}` : url
}
