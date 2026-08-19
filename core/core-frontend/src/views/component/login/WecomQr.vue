<template>
  <div id="de2-wecom-qr" :class="isBind ? 'de2-wecom-bind-qr' : 'de2-wecom-qr'" />
</template>

<script lang="ts" setup>

import { loadScript } from "@/utils/RemoteJs"
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'
import { onUnmounted } from 'vue'
import { getLocale } from '@/utils/utils'
interface WecomInfo {
  corp_id?: string
  agent_id?: string
  state?: string
  redirect_uri?: string
}

const props = defineProps({
  isBind: propTypes.bool.def(false),
})
const emit = defineEmits(['finish'])
let wwLogin = null
const remoteJsUrl = 'https://wwcdn.weixin.qq.com/node/open/js/wecom-jssdk-2.3.3.js'
const jsId = 'de-wecom-qr-id'
const init = () => {
  loadScript(remoteJsUrl, jsId).then(() => {
    getQrInfo().then(res => {
      const data = formatQrResult(res.data)
      loadQr(data.corp_id, data.agent_id, data.state, data.redirect_uri)
    })
  })
}

const getQrInfo = () => {
  const url = '/wecom/qrinfo'
  return request.get({ url })
}

const formatQrResult = (data): WecomInfo => {
  const result = { corp_id: null, agent_id: null, state: null, redirect_uri: null } as unknown as WecomInfo
  result.corp_id = data.corpId
  result.agent_id = data.agentId
  result.state = 'fit2cloud-wecom-qr'
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

const loadQr = (CORP_ID, AGENT_ID, STATE, REDIRECT_URI) => {
  wwLogin = ww.createWWLoginPanel({
    el: '#de2-wecom-qr',
    params: {
      login_type: 'CorpApp',
      appid: CORP_ID,
      agentid: AGENT_ID,
      redirect_uri: REDIRECT_URI,
      state: STATE,
      redirect_type: 'callback',
      lang: getLocale() === 'en' ? 'en' : 'zh'
    },
    onCheckWeComLogin({ isWeComLogin }) {
      console.log(isWeComLogin)
    },
    onLoginSuccess({ code }) {
      window.location.href = REDIRECT_URI + `?code=${code}&state=${STATE}`
    },
    onLoginFail(err) {
      console.log(err)
    },
  })
}

onUnmounted(() => {
  if (wwLogin) {
    wwLogin.unmount()
  }
})
init()
</script>
<style lang="less" scoped>
.de2-wecom-qr {
  margin-top: -55px;
}
.de2-wecom-bind-qr {
  margin-top: -20px;
}
</style>