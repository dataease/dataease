import CryptoJS from 'crypto-js'

const loadHmacInfo = async (baseUrl: string) => {
  if (window['de_secret_key']) {
    if (window['de_secret_key'] === 1) {
      return null
    }
    return window['de_secret_key']
  }
  let res = null as any
  const url = baseUrl + '/perSetting/hmac/info'
  const xhr = new XMLHttpRequest()
  xhr.onreadystatechange = () => {
    if (xhr.readyState === 4 && xhr.status === 200) {
      if (xhr.responseText) {
        try {
          const response = JSON.parse(xhr.responseText)
          if (response.code === 0) {
            res = response
          } else {
            throw new Error(response.msg)
          }
        } catch (e) {
          throw e
        }
      } else {
        throw new Error('网络异常，请联系网管')
      }
    }
  }

  xhr.open('get', url, false)
  xhr.send()

  if (!res?.data) {
    window['de_secret_key'] = 1
    return null
  }
  const secretInfoText = res.data
  const prefix = secretInfoText.substring(0, 16)
  const suffix = secretInfoText.substring(secretInfoText.length - 16)
  const originSecretInfo = secretInfoText.substring(16, secretInfoText.length - 16)
  const infoJson = aesDecryptWithIv(originSecretInfo, prefix + suffix)
  const info = JSON.parse(infoJson)
  if (!info?.enable) {
    window['de_secret_key'] = 1
    return null
  }
  window['de_secret_key'] = info.secretKey
  return info.secretKey
}

const aesDecryptWithIv = (encryptedBase64: string, secretKey: string): string => {
  try {
    const combined = CryptoJS.enc.Base64.parse(encryptedBase64)
    const combinedBytes = CryptoJS.enc.Hex.parse(combined.toString(CryptoJS.enc.Hex))

    const ivBytes = CryptoJS.lib.WordArray.create(combinedBytes.words.slice(0, 4))
    const ciphertextBytes = CryptoJS.lib.WordArray.create(combinedBytes.words.slice(4))

    const key = CryptoJS.enc.Utf8.parse(secretKey)

    const decrypted = CryptoJS.AES.decrypt({ ciphertext: ciphertextBytes }, key, {
      iv: ivBytes,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    })

    const result = decrypted.toString(CryptoJS.enc.Utf8)

    if (!result) {
      throw new Error('解密失败，请检查密钥是否正确')
    }

    return result
  } catch (error: any) {
    throw new Error(`AES解密失败: ${error.message}`)
  }
}

const hmac_white_list = [
  /* '/xpackModel',
  '/DEXPackTs.umd.js',
  '/sysParameter/requestTimeOut',
  '/appearance/image/',
  '/sysParameter/i18nOptions',
  '/perSetting/hmac/info' */
]
const securityConfig = async (config: any, uri: string) => {
  if (hmac_white_list.some(item => uri.includes(item))) {
    return
  }
  // console.log(33333)
  const keyId = 'dataease-key'
  const gmtTime = new Date().toUTCString()
  const requestMethod = config.method.toLocaleUpperCase()
  let requestPath = uri
  if (requestPath.startsWith('/api')) {
    requestPath = '/de2api' + requestPath.substring(4)
  }
  if (requestPath.startsWith('./de2api')) {
    requestPath = location.pathname.replace('mobile.html', '') + requestPath.substring(2)
  }
  if (requestPath.includes('/casbi')) {
    requestPath = requestPath.replace('/casbi', '')
  }
  if (requestPath.includes('/oidcbi')) {
    requestPath = requestPath.replace('/oidcbi', '')
  }
  const signingString = `${keyId}\n${requestMethod} ${requestPath}\nX-Date: ${gmtTime}\n`
  try {
    const secretKey = await loadHmacInfo(config.baseURL as string)
    if (!secretKey || secretKey === 1) {
      return
    }
    const signature = CryptoJS.HmacSHA256(signingString, secretKey)
    const signatureBase64 = CryptoJS.enc.Base64.stringify(signature)
    const hmacHeaders = {
      'X-Date': gmtTime.toString(),
      Authorization: `Signature keyId="${keyId}",algorithm="hmac-sha256",headers="@request-target X-Date",signature="${signatureBase64}"`
    }
    config.headers = { ...config.headers, ...hmacHeaders }
  } catch (e: any) {
    return
  }
}

export { securityConfig }
