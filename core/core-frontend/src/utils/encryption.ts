import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { queryDekey } from '@/api/login'

const { wsCache } = useCache()
const crypt = new JSEncrypt()
const cacheKey = () => useAppStoreWithOut().getDekey
const isPublicKey = (value: unknown): value is string =>
  typeof value === 'string' && /^MI[A-Za-z0-9+/=]+$/.test(value) && value.length > 300

export const ensureDekey = async (forceRefresh = false) => {
  let key = forceRefresh ? undefined : wsCache.get(cacheKey())
  if (!isPublicKey(key)) {
    // Discard the old response, which included the deployment storage AES key.
    wsCache.delete(cacheKey())
    const res = await queryDekey()
    key = res.data
    if (!isPublicKey(key)) throw new Error('Invalid public key')
    wsCache.set(cacheKey(), key)
  }
  return key
}

export const rsaEncryp = (word: string) => {
  const key = wsCache.get(cacheKey())
  if (!isPublicKey(key)) throw new Error('Invalid public key')
  crypt.setPublicKey(key)
  return crypt.encrypt(word)
}

// Configuration responses are already masked by the server; no shared secret is needed.
export const parseConfigurationJson = async <T = unknown>(data: string): Promise<T> =>
  JSON.parse(data) as T
