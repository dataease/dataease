import CryptoJS from 'crypto-js/crypto-js'
import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { Base64 } from 'js-base64'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'

const appStore = useAppStoreWithOut()

const { wsCache } = useCache()

const rsaKey = '-pk_separator-'
const crypt = new JSEncrypt()

const aesDecrypt = (word, keyStr, ivWordArray) => {
  try {
    const keyHex = CryptoJS.enc.Utf8.parse(keyStr)
    const decrypt = CryptoJS.AES.decrypt(word, keyHex, {
      iv: ivWordArray,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    })
    return decrypt.toString(CryptoJS.enc.Utf8)
  } catch (e) {
    // 密钥对不上（如跨版本残留的旧格式缓存）时 Utf8 转换会抛 Malformed UTF-8 data
    return ''
  }
}

// RSA 公钥为 2048 位 X.509 SPKI 的 Base64（392 字符）
const isPublicKey = pk => !!pk && pk.length > 100 && /^[A-Za-z0-9+/]+={0,2}$/.test(pk)

export const rsaEncryp = word => {
  const separator = Base64.encodeURI(rsaKey) + '='
  const dekey = wsCache.get(appStore.getDekey)
  if (!dekey) {
    return false
  }
  const keyArray = dekey.split(separator)
  if (keyArray.length < 2) {
    wsCache.delete(appStore.getDekey)
    return false
  }
  const k1 = keyArray[0]
  const k2 = keyArray[1]
  // IV 由密钥派生（与后端 RsaUtils.deriveIv 一致）
  const ivWordArray = CryptoJS.SHA256(k2)
  const ivHex = CryptoJS.lib.WordArray.create(ivWordArray.words.slice(0, 4))
  const pk = aesDecrypt(k1, k2, ivHex)
  if (!isPublicKey(pk)) {
    // 解不出合法公钥：缓存已失效（跨版本残留/损坏），清除以便重新拉取
    wsCache.delete(appStore.getDekey)
    return false
  }
  crypt.setKey(pk)
  return crypt.encrypt(word)
}

export const symmetricDecrypt = data => {
  const separator = Base64.encodeURI(rsaKey) + '='
  const dekey = wsCache.get(appStore.getDekey)
  const keyArray = dekey.split(separator)
  const k2 = keyArray[1]
  const keyStr = Base64.encode(k2)
  const combined = CryptoJS.enc.Base64.parse(data)
  const combinedHex = CryptoJS.enc.Hex.parse(combined.toString(CryptoJS.enc.Hex))
  const iv = CryptoJS.lib.WordArray.create(combinedHex.words.slice(0, 4))
  const cipherHex = CryptoJS.lib.WordArray.create(combinedHex.words.slice(4))
  const key = CryptoJS.enc.Base64.parse(keyStr)
  const decrypted = CryptoJS.AES.decrypt({ ciphertext: cipherHex }, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  return decrypted.toString(CryptoJS.enc.Utf8)
}
