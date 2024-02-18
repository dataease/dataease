import { useEmbedded } from '@/store/modules/embedded'
const embeddedStore = useEmbedded()
export const formatDataEaseBi = (url: string) => {
  return embeddedStore.baseUrl ? `${embeddedStore.baseUrl}${url}` : url
}
