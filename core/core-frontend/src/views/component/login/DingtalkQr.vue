<template>
  <div id="de2-dingtalk-qr" :class="{'de2-dingtalk-qr': !isBind}"/>
</template>
  
<script lang="ts" setup>
import { loadScript } from '@/utils/RemoteJs'
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'
interface DingtalkQrInfo {
  client_id?: string
  state?: string
  redirect_uri?: string
}
  
const props = defineProps({
  isBind: propTypes.bool.def(false),
})
const emit = defineEmits(['finish'])
const remoteJsUrl = 'https://g.alicdn.com/dingding/dinglogin/0.0.5/ddLogin.js'
const jsId = 'de-dingtalk-qr-id'
const init = () => {
  loadScript(remoteJsUrl, jsId).then(() => {
    getQrInfo().then(res => {
      const data = formatQrResult(res.data)
      loadQr(data.client_id, data.state, data.redirect_uri)
    })
  })
}

const getQrInfo = () => {
  const url = '/dingtalk/qrinfo'
  return request.get({ url })
}

const formatQrResult = (data): DingtalkQrInfo => {
  const result = { client_id: null, state: null, redirect_uri: null } as unknown as DingtalkQrInfo
  result.client_id = data.appKey
  result.state = 'fit2cloud-dingtalk-qr'
  result.redirect_uri = data.callBack
  if (props.isBind) {
    result.state += '_de_bind'
    const pathname = window.location.pathname
    if (pathname.includes('oidcbi/')) {
      result.state += '_path_oidcbi'
    } else if (pathname.includes('casbi/')) {
      result.state += '_path_casbi'
    }
  }
  return result
}

const loadQr = (APPID, STATE, REDIRECT_URI) => {
  const redUrl = encodeURIComponent(REDIRECT_URI)
  let url = `https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=${APPID}&response_type=code&scope=snsapi_login&state=${STATE}&redirect_uri=${redUrl}`
  const obj = DDLogin({
    id: 'de2-dingtalk-qr',
    goto: encodeURIComponent(url),
    style: 'border:none;background-color:#FFFFFF;',
    width: '280',
    height: '300'
  })
  const handleMessage = function (event) {
    const origin = event.origin
    if (origin == 'https://login.dingtalk.com') {
      const loginTmpCode = event.data
      url += '&loginTmpCode=' + loginTmpCode
      window.location.href = url
    }
  }
  if (typeof window.addEventListener != 'undefined') {
    window.addEventListener('message', handleMessage, false)
  } else if (typeof window.attachEvent != 'undefined') {
    window.attachEvent('onmessage', handleMessage)
  }
}
init()
</script>
<style lang="less" scoped>
.de2-dingtalk-qr {
  margin-top: -36px;
}
</style>
  