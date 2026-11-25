import { reactive } from 'vue'
import { useCache } from '../../../../core/core-frontend/src/hooks/web/useCache'
export { useCache }
export const user = reactive({ getToken: 'login-a', getUid: '1', getOid: '1' })
export const embedded = reactive({
  token: '',
  baseUrl: 'http://bi.test/tenant'
})
export const link = reactive({ getLinkToken: '' })
export const useUserStoreWithOut = () => user
export const useEmbedded = () => embedded
export const useLinkStoreWithOut = () => link

export const fetchResourceImage = async (path, signal) => {
  const response = await fetch('/de2api' + path, {
    signal,
    headers: link.getLinkToken
      ? { 'X-DE-LINK-TOKEN': link.getLinkToken }
      : embedded.token
      ? { 'X-EMBEDDED-TOKEN': embedded.token }
      : { 'X-DE-TOKEN': user.getToken }
  })
  if (!response.ok) throw new Error('Image denied')
  return response.blob()
}

export const guid = () => 'test-id'
export const ElMessage = { error: () => {} }
export default {
  get: async ({ url, signal }) => ({
    data: await fetchResourceImage(url, signal)
  })
}
