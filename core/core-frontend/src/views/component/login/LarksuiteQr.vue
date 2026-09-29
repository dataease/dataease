<template>
  <div id="de2-larksuite-qr" :class="{'de2-larksuite-qr': !isBind}"/>
</template>

<script lang="ts" setup>

import { loadScript } from "@/utils/RemoteJs"
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'
import { onUnmounted } from 'vue'
interface LarksuiteQrInfo {
  client_id?: string
  state?: string
  redirect_uri?: string
}

const props = defineProps({
  isBind: propTypes.bool.def(false),
  bindState: propTypes.string.def(''),
  bindRedirectUri: propTypes.string.def('')
})
const emit = defineEmits(['finish'])
const remoteJsUrl = 'https://lf-package-us.larksuitecdn.com/obj/lark-static-us/lark/passport/qrcode/LarkSSOSDKWebQRCode-1.0.3.js'
const jsId = 'de-larksuite-qr-id'
let disposed = false
let removeMessageListener: (() => void) | undefined
onUnmounted(() => {
  disposed = true
  removeMessageListener?.()
})
const init = () => {
  if (props.isBind && (!props.bindState || !props.bindRedirectUri)) return
  loadScript(remoteJsUrl, jsId).then(() => {
    getQrInfo().then(res => {
      if (disposed) return
      const data = formatQrResult(res.data)
      loadQr(data.client_id, data.state, data.redirect_uri)
    })
  })
}

const getQrInfo = () => {
  const url = '/larksuite/qrinfo'
  return request.get({ url })
}

const formatQrResult = (data): LarksuiteQrInfo => {
  const result = { client_id: null, state: null, redirect_uri: null } as unknown as LarksuiteQrInfo
  result.client_id = data.appId
  result.state = 'fit2cloud-larksuite-qr'
  result.redirect_uri = data.callBack
  if (props.isBind) {
    result.state = props.bindState
    result.redirect_uri = props.bindRedirectUri
  }
  return result
}

const loadQr = (CLIENT_ID, STATE, REDIRECT_URI) => {
  let url = `https://passport.larksuite.com/suite/passport/oauth/authorize?client_id=${CLIENT_ID}&response_type=code&state=${STATE}&redirect_uri=${REDIRECT_URI}`
  const QRLoginObj = window['QRLogin']({
    id: 'de2-larksuite-qr',
    goto: url,
    style: "border:none;background-color:#FFFFFF;width: 266px;height: 266px;"
  })
  const handleMessage = function (event) {
    const origin = event.origin
    if( QRLoginObj.matchOrigin(origin) && QRLoginObj.matchData(event.data)) {
      const loginTmpCode = event.data.tmp_code
      url += ("&tmp_code=" + loginTmpCode)
      window.location.href = url
    }
  }
  window.addEventListener('message', handleMessage, false)
  removeMessageListener = () => window.removeEventListener('message', handleMessage, false)
}
init()
</script>
<style lang="less" scoped>
.de2-larksuite-qr {
  margin-top: -15px;
}
</style>