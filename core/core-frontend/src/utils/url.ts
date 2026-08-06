import { useEmbedded } from '@/store/modules/embedded'
const embeddedStore = useEmbedded()
export const formatDataEaseBi = (url: string) => {
  return embeddedStore.baseUrl ? `${embeddedStore.baseUrl}${url}` : url
}

// 从已加载的入口 bundle 反推部署根路径（含结尾 /），
// 代理子路径 / 访问地址缺失结尾斜杠都能正确解析静态资源
export const getResourceBaseUrl = () => {
  const el = document.querySelector(
    'script[type="module"][src*="/js/"]'
  ) as HTMLScriptElement | null
  if (el?.src) {
    return el.src.replace(/js\/[^/]+\.js.*$/, '')
  }
  return new URL('./', document.baseURI).href
}
