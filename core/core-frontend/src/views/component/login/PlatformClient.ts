import { loadScript } from '@/utils/RemoteJs'
import { getQueryString } from '@/utils/utils'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import request from '@/config/axios'
import { useI18n } from '@/hooks/web/useI18n'
import * as dd from 'dingtalk-jsapi'
export interface LoginCategory {
  oidc?: boolean
  cas?: boolean
  ldap?: boolean
  oauth2?: boolean
  saml2?: boolean
  qrcode?: boolean
  lark?: boolean
  dingtalk?: boolean
  wecom?: boolean
  larksuite?: boolean
}
const { t } = useI18n()
const flagArray = ['dingtalk', 'lark', 'larksuite']
const urlArray = [
  'https://g.alicdn.com/dingding/dingtalk-jsapi/3.1.0/dingtalk.open.js',
  'https://lf1-cdn-tos.bytegoofy.com/goofy/lark/op/h5-js-sdk-1.5.26.js',
  'https://lf1-cdn-tos.bytegoofy.com/goofy/lark/op/h5-js-sdk-1.5.16.js'
]
export const loadClient = (category: LoginCategory) => {
  const type = getQueryString('client')
  const corpid = getQueryString('corpid')
  if (type && !category[type]) {
    ElMessageBox.confirm(t('login.platform_disable', [t(`threshold.${type}`)]), {
      confirmButtonType: "danger",
      type: "warning",
      showCancelButton: false,
      confirmButtonText: t("commons.refresh"),
      cancelButtonText: t("dataset.cancel"),
      autofocus: false,
      showClose: false,
    }).then(() => {
      window.location.reload()
    }).catch(() => {});
    return false
  }
  if (!type || !flagArray.includes(type) || !category[type] || (type === 'dingtalk' && !corpid)) {
    return false
  }
  const index = flagArray.indexOf(type)
  const jsId = `fit2cloud-dataease-v2-platform-client-${type}`
  let awaitMethod: Promise<void> = null as any
  if (index === 0) {
    console.log('load dingtalk client')
    awaitMethod = new Promise<void>((resolve, reject) => {
      dd.ready(() => {
        resolve()
      })
      dd.error((err) => {
        reject(err)
      })
    })
  } else {
    awaitMethod = loadScript(urlArray[index], jsId)
  }
  awaitMethod
    .then(() => {
      if (index === 0 && corpid) {
        dingtalkClientRequest(corpid)
      }
      if (index === 1) {
        larkClientRequest()
      }
      if (index === 2) {
        larksuiteClientRequest()
      }
    })
    .catch(() => {
      ElMessage.error('加载失败')
    })
  return true
}

const dingtalkClientRequest = (id?: string) => {
  if (id && dd?.runtime?.permission?.requestAuthCode) {
    dd.runtime.permission.requestAuthCode({
      corpId: id,
      onSuccess: function (result) {
        const code = result.code
        const state = `fit2cloud-dingtalk-client`
        let targetUrl = `?code=${code}&state=${state}`
        /* if (location.hash?.startsWith('#/preview') || location.hash?.startsWith('#/login?redirect=/preview')) {
          targetUrl += location.hash.replace('#/login?redirect=/preview', '#/preview')
        } */
        if (location.hash?.startsWith('#/login?redirect=')) {
          targetUrl += location.hash.replace('#/login?redirect=', '#')
        }
        toUrl(targetUrl)
      },
      onFail: function (err) {
        ElMessage.error(err)
        console.error(err)
      }
    })
  } else {
    ElMessage.error('not success')
  }
}

const larkClientRequest = async () => {
  if (!window['tt']) {
    ElMessage.error('load remote lark js error')
    return
  }
  const res = await queryAppid('lark')
  if (!res?.data?.appId) {
    ElMessage.error('get appId error')
    return
  }
  const appId = res.data.appId
  const callRequestAuthCode = () => {
    window['tt'].requestAuthCode({
      appId: appId,
      success: res => {
        const { code } = res
        const state = `fit2cloud-lark-client`
        let targetUrl = `?code=${code}&state=${state}`
        if (location.hash?.startsWith('#/login?redirect=')) {
          targetUrl += location.hash.replace('#/login?redirect=', '#')
        }
        toUrl(targetUrl)
      },
      fail: error => {
        const { errno, errString } = error
        ElMessage.error(`error code: ${errno}, error msg: ${errString}`)
      }
    })
  }
  if (window['tt'].requestAccess) {
    window['tt'].requestAccess({
      appID: appId,
      scopeList: [],
      success: res => {
        const { code } = res
        const state = `fit2cloud-lark-client`
        // toUrl(`?code=${code}&state=${state}`)
        let targetUrl = `?code=${code}&state=${state}`
        if (location.hash?.startsWith('#/login?redirect=')) {
          targetUrl += location.hash.replace('#/login?redirect=', '#')
        }
        toUrl(targetUrl)
      },
      fail: error => {
        const { errno, errString } = error
        if (errno === 103) {
          callRequestAuthCode()
        } else {
          ElMessage.error(`error code: ${errno}, error msg: ${errString}`)
        }
      }
    })
  } else {
    callRequestAuthCode()
  }
}

const larksuiteClientRequest = async () => {
  if (!window['tt'] || !window['h5sdk']) {
    ElMessage.error('load remote lark js error')
    return
  }
  const res = await queryAppid('larksuite')
  if (!res?.data?.appId) {
    ElMessage.error('get appId error')
    return
  }
  const appId = res.data.appId
  window['h5sdk'].ready(() => {
    window['tt'].requestAuthCode({
      appId: appId,
      success(res) {
        const code = res?.code || res
        const state = `fit2cloud-larksuite-client`
        // toUrl(`?code=${code}&state=${state}`)
        let targetUrl = `?code=${code}&state=${state}`
        if (location.hash?.startsWith('#/login?redirect=')) {
          targetUrl += location.hash.replace('#/login?redirect=', '#')
        }
        toUrl(targetUrl)
      },
      fail(error) {
        const { errno, errString } = error
        ElMessage.error(`error code: ${errno}, error msg: ${errString}`)
      }
    })
  })
}

const queryAppid = (type: string) => {
  const url = `/${type}/qrinfo`
  return request.get({ url })
}

const toUrl = (url) => {
  const { origin, pathname } = window.location
  window.location.href = origin + pathname + url
}
