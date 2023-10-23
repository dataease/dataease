import CryptoJS from 'crypto-js/crypto-js'
import JSEncrypt from 'jsencrypt'
import { Base64 } from 'js-base64'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'

const appStore = useAppStoreWithOut()

const { wsCache } = useCache()

const rsaKey = '-pk_separator-'
const crypt = new JSEncrypt()

/* export const aesEncrypt = word => {
  const srcs = CryptoJS.enc.Utf8.parse(word)
  const encrypted = CryptoJS.AES.encrypt(srcs, aesKey, {
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  })
  return CryptoJS.enc.Hex.stringify(encrypted.ciphertext)
} */
const aesDecrypt = (word, keyStr) => {
  const keyHex = CryptoJS.enc.Utf8.parse(keyStr) //
  const ivHex = CryptoJS.enc.Utf8.parse('0000000000000000')
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
