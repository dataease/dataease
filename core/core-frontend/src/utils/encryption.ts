import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { queryDekey } from '@/api/login'

const { wsCache } = useCache()
const cacheKey = () => useAppStoreWithOut().getDekey
const encryptionError = (reason: 'invalid_key' | 'encryption_failed') => {
  const { t } = useI18n()
  const message = t(`rsa.${reason}`)
  ElMessage.error(message)
  return new Error(message)
}

export const isPublicKey = (value: unknown): value is string => {
  if (typeof value !== 'string' || !/^MI[A-Za-z0-9+/=]+$/.test(value)) return false
  try {
    const crypt = new JSEncrypt()
    crypt.setPublicKey(value)
    const parsed = crypt.getKey()
    return !!parsed.n && parsed.n.bitLength() >= 1024 && parsed.e > 0
  } catch {
    return false
  }
}

export const ensureDekey = async (forceRefresh = false) => {
  let key = forceRefresh ? undefined : wsCache.get(cacheKey())
  if (!isPublicKey(key)) {
    // Discard the old response, which included the deployment storage AES key.
    wsCache.delete(cacheKey())
    const res = await queryDekey()
    key = res.data
    if (!isPublicKey(key)) throw encryptionError('invalid_key')
    wsCache.set(cacheKey(), key)
  }
  return key
}

export const rsaEncryp = (word: string) => {
  const key = wsCache.get(cacheKey())
  if (!isPublicKey(key)) throw encryptionError('invalid_key')
  const crypt = new JSEncrypt()
  crypt.setPublicKey(key)
  const capacity = Math.ceil(crypt.getKey().n.bitLength() / 8) - 11
  if (new TextEncoder().encode(word).length > capacity) {
    throw encryptionError('encryption_failed')
  }
  const encrypted = crypt.encrypt(word)
  if (!encrypted) throw encryptionError('encryption_failed')
  return encrypted
}

// Configuration responses are already masked by the server; no shared secret is needed.
export const parseConfigurationJson = async <T = unknown>(data: string): Promise<T> =>
  JSON.parse(data) as T
