import { xpackModelApi } from '@/api/plugin'
import { useCache } from '@/hooks/web/useCache'
import { isNull } from '@/utils/utils'
import { useEmbedded } from '@/store/modules/embedded'
const { wsCache } = useCache()
const embeddedStore = useEmbedded()
const basePath = import.meta.env.VITE_API_BASEPATH
const embeddedBasePath =
  basePath.startsWith('./') && basePath.length > 2 ? basePath.substring(2) : basePath
export const PATH_URL = embeddedStore.baseUrl ? embeddedStore?.baseUrl + embeddedBasePath : basePath

const remoteTsUrl = PATH_URL + '/DEXPackTs.umd.js'
const loadXpackTs = (url: string) => {
  return new Promise<void>(function (resolve, reject) {
    const scriptId = 'de-fit2cloud-script-xpack-ts-id'
    let dom = document.getElementById(scriptId)
    if (dom) {
      dom.parentElement?.removeChild(dom)
      dom = null
    }
    const script = document.createElement('script')

    script.id = scriptId
    script.onload = function () {
      return resolve()
    }
    script.onerror = function () {
      return reject(new Error('Load script from '.concat(url, ' failed')))
    }
    script.src = url
    const head = document.head || document.getElementsByTagName('head')[0]
    ;(document.body || head).appendChild(script)
  })
}

export const importXpackTool = async (methodName: string) => {
  const jsname = 'L3Rvb2xzL0htYWNUb29s'
  const key = 'xpack-model-distributed'
  let distributed = false
  if (wsCache.get(key) === null) {
    const res = await xpackModelApi()
    const resData = isNull(res.data) ? 'null' : res.data
    wsCache.set('xpack-model-distributed', resData)
    distributed = res.data
  } else {
    distributed = wsCache.get(key)
  }
  distributed = true
  if (isNull(distributed) || !distributed) {
    return null
  }
  if (window['DEXPackTs']) {
    const xpack = await window['DEXPackTs'].mapping[jsname]
    return xpack[methodName]
  } else {
    if (!window['de_hmac_promise']) {
      window['de_hmac_promise'] = null
    }
    if (!window['de_hmac_promise']) {
      window['de_hmac_promise'] = loadXpackTs(remoteTsUrl)
        .then(() => {
          return window['DEXPackTs']
        })
        .catch((error: any) => {
          window['__de_xpack_loading__'] = null
          throw error
        })
    }
    await window['de_hmac_promise']
    const xpack = await window['DEXPackTs'].mapping[jsname]
    return xpack[methodName]
  }
}
