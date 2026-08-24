import CryptoJS from 'crypto-js/crypto-js'
import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { Base64 } from 'js-base64'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { queryDekey } from '@/api/login'

const appStore = useAppStoreWithOut()

const { wsCache } = useCache()

const rsaKey = '-pk_separator-'
const dekeySeparator = Base64.encodeURI(rsaKey) + '='
const crypt = new JSEncrypt()

const isValidDekey = (dekey: unknown): dekey is string => {
  if (typeof dekey !== 'string') {
    return false
  }
  const separatorIndex = dekey.lastIndexOf(dekeySeparator)
  return separatorIndex > 0 && separatorIndex + dekeySeparator.length < dekey.length
}

export const ensureDekey = async (forceRefresh = false) => {
  let dekey = forceRefresh ? undefined : wsCache.get(appStore.getDekey)
  if (!isValidDekey(dekey)) {
    const res = await queryDekey()
    dekey = res.data
    if (!isValidDekey(dekey)) {
      wsCache.delete(appStore.getDekey)
      throw new Error('Invalid encryption key')
    }
    wsCache.set(appStore.getDekey, dekey)
  }
  return dekey
}

const getDekeyParts = () => {
  const dekey = wsCache.get(appStore.getDekey)
  if (!isValidDekey(dekey)) {
    throw new Error('Invalid encryption key')
  }
  const separatorIndex = dekey.lastIndexOf(dekeySeparator)
  return [
    dekey.substring(0, separatorIndex),
    dekey.substring(separatorIndex + dekeySeparator.length)
  ]
}

const aesDecrypt = (word, keyStr) => {
  const keyHex = CryptoJS.enc.Utf8.parse(keyStr)
  const ivWordArray = CryptoJS.SHA256(keyStr)
  const ivHex = CryptoJS.lib.WordArray.create(ivWordArray.words.slice(0, 4))
  const decrypt = CryptoJS.AES.decrypt(word, keyHex, {
    iv: ivHex,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  return decrypt.toString(CryptoJS.enc.Utf8)
}

export const rsaEncryp = word => {
  const [k1, k2] = getDekeyParts()
  const pk = aesDecrypt(k1, k2)
  crypt.setKey(pk)
  return crypt.encrypt(word)
}

export const symmetricDecrypt = data => {
  if (typeof data !== 'string' || !data) {
    throw new Error('Invalid encrypted data')
  }
  const [, k2] = getDekeyParts()
  const keyStr = Base64.encode(k2)
  const combined = CryptoJS.enc.Base64.parse(data)
  if (combined.sigBytes <= 16) {
    throw new Error('Invalid encrypted data')
  }
  const combinedHex = CryptoJS.enc.Hex.parse(combined.toString(CryptoJS.enc.Hex))
  const iv = CryptoJS.lib.WordArray.create(combinedHex.words.slice(0, 4))
  const cipherHex = CryptoJS.lib.WordArray.create(combinedHex.words.slice(4))
  const key = CryptoJS.enc.Base64.parse(keyStr)
  const decrypted = CryptoJS.AES.decrypt({ ciphertext: cipherHex }, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  const result = decrypted.toString(CryptoJS.enc.Utf8)
  if (!result) {
    throw new Error('Failed to decrypt data')
  }
  return result
}

export const symmetricDecryptJson = async <T = unknown>(data: string): Promise<T> => {
  await ensureDekey()
  try {
    return JSON.parse(symmetricDecrypt(data)) as T
  } catch {
    await ensureDekey(true)
    return JSON.parse(symmetricDecrypt(data)) as T
  }
}
