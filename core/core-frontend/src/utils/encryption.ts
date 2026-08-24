import CryptoJS from 'crypto-js/crypto-js'
import JSEncrypt from 'jsencrypt/bin/jsencrypt.min'
import { Base64 } from 'js-base64'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { queryDekey } from '@/api/login'

const appStore = useAppStoreWithOut()

const { wsCache } = useCache()

const rsaKey = '-pk_separator-'
const crypt = new JSEncrypt()

export const ensureDekey = async () => {
  let dekey = wsCache.get(appStore.getDekey)
  if (!dekey) {
    const res = await queryDekey()
    dekey = res.data
    wsCache.set(appStore.getDekey, dekey)
  }
  return dekey
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
  const separator = Base64.encodeURI(rsaKey) + '='
  const dekey = wsCache.get(appStore.getDekey)
  const keyArray = dekey.split(separator)
  const k1 = keyArray[0]
  const k2 = keyArray[1]
  const pk = aesDecrypt(k1, k2)
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
