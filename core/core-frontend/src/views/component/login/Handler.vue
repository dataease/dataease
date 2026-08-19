<template>
  <div v-if="loginCategory.qrcode" :class="{'de-qr-hidden': !qrStatus}">
    <QrTab 
      v-if="qrStatus"
      :wecom="loginCategory.wecom"
      :dingtalk="loginCategory.dingtalk"
      :lark="loginCategory.lark"
      :larksuite="loginCategory.larksuite"
    />
  </div>
  <el-divider v-if="anyEnable" class="de-other-login-divider">{{ t('login.other_login') }}</el-divider>
  <el-form-item v-if="anyEnable" class="other-login-item">
    <div class="login-list" >
      <QrcodeLdap 
        v-if="loginCategory.qrcode || loginCategory.ldap" 
        ref="qrcodeLdapHandler" 
        :qrcode="loginCategory.qrcode" 
        :ldap="loginCategory.ldap"
        @status-change="qrStatusChange"
      />
      <Oidc v-if="loginCategory.oidc" @switch-category="switcherCategory"/>
      <Oauth2 ref="oauth2Handler" v-if="loginCategory.oauth2" @switch-category="switcherCategory"/>
      <Cas v-if="loginCategory.cas" @switch-category="switcherCategory"/>
      <Saml2 ref="saml2Handler" v-if="loginCategory.saml2"  @switch-category="switcherCategory"/>
    </div>
  </el-form-item>

  <mfa-step v-if="showMfa" :mfa-data="state.mfaData" @close="showMfa = false"/>
  <platform-error v-if="platformLoginMsg" :msg="platformLoginMsg"/>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import QrcodeLdap from './QrcodeLdap.vue'
import Oidc from './Oidc.vue'
import Oauth2 from './Oauth2.vue'
import Saml2 from './Saml2.vue'
import Cas from './Cas.vue'
import QrTab from './QrTab.vue'
import request from '@/config/axios'
import { useCache } from '@/hooks/web/useCache'
import router from '@/router'
import { platformLoginApi, loginCategoryApi } from '@/api/login'
import { useUserStoreWithOut } from '@/store/modules/user'
import { getQueryString, isLarkPlatform, isPlatformClient, checkPlatform } from '@/utils/utils'
import { loadClient, LoginCategory } from './PlatformClient'
import MfaStep from './MfaStep.vue'
import { logoutHandler } from "@/utils/logout";
import { useI18n } from '@/hooks/web/useI18n'
import PlatformError from './PlatformError.vue'

const { t } = useI18n()
interface Categoryparam {
  category: string
  proxy?: string
}
const platformLoginMsg = ref('')
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const qrStatus = ref(false)
const loginCategory = ref({} as LoginCategory)
const anyEnable = ref(false)
const qrcodeLdapHandler = ref()
const oauth2Handler = ref()
const saml2Handler = ref()
const state = reactive({
  mfaData: {
    enabled: false,
    ready: false
  }
})
const emits = defineEmits(['switchTab', 'autoCallback'])
const init = (cb) => {
  queryCategoryStatus().then(res => {
    if (res['data']) {
      const list: any[] = res['data'] as any[]
      list.forEach(item => {
        loginCategory.value[item.name] = item.enable
        if (item.enable) {
          anyEnable.value = true
        }
      })
    }
    if (res?.code === 0) {
      wsCache.delete('oidc-error')
      !loadClient(loginCategory.value) && cb && cb()
    } else if (!wsCache.get('oidc-error')) {
      wsCache.set('oidc-error', 1)
      window.location.reload()
    }
  }).catch(() => {
    if (!wsCache.get('oidc-error')) {
      wsCache.set('oidc-error', 1)
      window.location.reload()
    }
  })
}

const qrStatusChange = (activeComponent: string) => {
  qrStatus.value = activeComponent === 'qrcode'
  if (activeComponent === 'account') {
    emits('switchTab', 'simple')
  } else if (activeComponent === 'ldap') {
    switcherCategory({ category: 'ldap', proxy: '' })
  }
}
const showMfa = ref(false)
const toMfa = mfa => {
  state.mfaData = mfa
  showMfa.value = true
  if (document.getElementsByClassName('preheat-container')?.length) {
    document.getElementsByClassName('preheat-container')[0].setAttribute('style', 'display: none;')
  }
}
const ssoLogin = category => {
  const array = [
    {category: 'ldap', proxy: ''},
    {category: 'oidc', proxy: '/oidcbi/#'},
    {category: 'cas', proxy: '/casbi/#'},
    {category: 'oauth2', proxy: '/#'},
    {category: 'saml2', proxy: '/#'},
  ]
  if (category) {
    if (category === 1) {
      qrcodeLdapHandler.value?.setActive('ldap')
    }
    switcherCategory(array[category - 1])
  }
}

const switcherCategory = (param: Categoryparam) => {
  const {category, proxy} = param
  const curOrigin = window.location.origin
  const curLocation = getCurLocation()
  if (!category || category === 'simple' || category === 'ldap') {
    qrStatus.value = false
    emits('switchTab', category || 'simple')
    return
  }
  let pathname = window.location.pathname
  if (pathname) {
    if (pathname.includes('oidcbi/') || pathname.includes('casbi/')) {
      pathname = pathname.replace('oidcbi/', '')
      pathname = pathname.replace('casbi/', '')
    }
    pathname = pathname.substring(0, pathname.length - 1)
  }
  const nextPage = curOrigin + pathname + proxy + curLocation
  if (category === 'oauth2') {
    oauth2Handler?.value?.toLoginPage()
    return
  }
  if (category === 'saml2') {
    saml2Handler?.value?.toLoginPage()
    return
  }
  if (category === 'cas') {
    window.open(nextPage, '_self')
    return
  }
  window.location.href = nextPage
}

const getCurLocation = () => {
  let queryRedirectPath = '/workbranch/index'
  if (router.currentRoute.value.query.redirect) {
    queryRedirectPath = router.currentRoute.value.query.redirect as string
  }
  return queryRedirectPath
}

const platformLogin = origin => {  
  platformLoginApi(origin).then(res => {
    const mfa = res.data?.mfa
    if (mfa?.enabled) {
      mfa['origin'] = origin
      toMfa(mfa)
      return
    }
    const token = res.data.token
    if (token && isPlatformClient()) {
     wsCache.set('de-platform-client', true)
    }
    userStore.setToken(token)
    userStore.setExp(res.data.exp)
    userStore.setTime(Date.now())
    if (origin === 10 || isLarkPlatform()) {
      let locationHash = window.location.hash
      if (locationHash?.startsWith('#/login?redirect=/')) {
        locationHash = locationHash.replace('#/login?redirect=/', '#/')
      }
      window.location.href = window.location.origin + window.location.pathname + locationHash
    } else {
      const queryRedirectPath = getCurLocation()
      router.push({ path: queryRedirectPath }) 
    }
  }).catch((e: any) => {
    userStore.setToken('')
    if (isLarkPlatform()) {
      setTimeout(() => {
        window.location.href = window.location.origin + window.location.pathname + window.location.hash
      }, 2000)
    } else {
      setTimeout(() => {
        logoutHandler(true, true)
        platformLoginMsg.value = e?.message || e
      }, 1500);
    }
  })
}

const queryCategoryStatus = () => {
  const url = `/setting/authentication/status`
  return request.get({ url })
}

const wecomToken = async () => {
  const code = getQueryString('code')
  const state = getQueryString('state')
  if (!code || !state) {
    return null
  }
  const res = await request.post({url: '/wecom/token', data: {code, state}})
  userStore.setToken(res.data)
  return res.data
}

const larkToken = async () => {
  const code = getQueryString('code')
  const state = getQueryString('state')
  if (!code || !state) {
    return null
  }
  const res = await request.post({url: '/lark/token', data: {code, state}})
  userStore.setToken(res.data)
  return res.data
}

const saml2Token = (cb) => {
  const token = getQueryString('saml2Token')
  if (!token) {
    return
  }
  userStore.setToken(token)
  cb && cb()
}

const oauth2Token = (cb) => {
  const localCodeKey = localStorage.getItem('DE_OAUTH2_CODE_KEY') || 'code'
  const code = getQueryString(localCodeKey)
  const state = getQueryString('state')
  if (!code || !state) {
    throw Error('no code or state')
    return null
  }
  request.post({url: '/oauth2/token', data: {code, state}}).then(res => {
    userStore.setToken(res.data.token)
    cb && cb()
  }).catch(() => {
    setTimeout(() => {
      window.location.href = window.location.origin + window.location.pathname + window.location.hash
    }, 2000)
  })
  
}

const larksuiteToken = async () => {
  const code = getQueryString('code')
  const state = getQueryString('state')
  if (!code || !state) {
    return null
  }
  const res = await request.post({url: '/larksuite/token', data: {code, state}})
  userStore.setToken(res.data)
  return res.data
}

const dingtalkToken = async () => {
  const code = getQueryString('code')
  const state = getQueryString('state')
  if (!code || !state) {
    return null
  }
  const res = await request.post({url: '/dingtalk/token', data: {code, state}})
  userStore.setToken(res.data)
  return res.data
}

const callBackType = () => {
  return getQueryString('state')
}

const auto2Platform = async () => {
  const resultParam = {
    preheat: true,
    loadingText: '加载中...',
    activeName: 'simple'
  }
  if (!checkPlatform()) {
    const res = await loginCategoryApi()
    const adminLogin = router.currentRoute?.value?.name === 'admin-login'
    if (adminLogin && (!res.data || res.data === 1)) {
      emits('autoCallback', resultParam)
      router.push('/401')
      return
    }
    if (res.data && !adminLogin) {
      if (res.data === 1) {
        resultParam.activeName = 'ldap'
        resultParam.preheat = false
      } else {
        resultParam.loadingText = '加载中...'
        document.getElementsByClassName('ed-loading-text')?.length &&
          (document.getElementsByClassName('ed-loading-text')[0]['innerText'] = resultParam.loadingText)
      }
      nextTick(() => {
        ssoLogin(res.data)
      })
    } else {
      resultParam.preheat = false
    }
  } else if (getQueryString('state')?.includes('fit2clouddeoauth2')) {
    resultParam.preheat = true
  }
  emits('autoCallback', resultParam)
}

onMounted(() => {
  wsCache.delete('de-platform-client')
  init(async () => {
    const state = callBackType()
    if (state?.includes('_de_bind')) {
      const sep = '_path_'
      const sepIdx = state.indexOf(sep)
      if (sepIdx > -1) {
        const prefix = state.substring(sepIdx + sep.length)
        const cleanState = state.substring(0, sepIdx)
        const params = new URLSearchParams(window.location.search)
        params.set('state', cleanState)
        window.location.href = window.location.origin + '/' + prefix + '/?' + params.toString() + window.location.hash
        return
      }
      return
    } else if (window.location.pathname.includes('/casbi/')) {
      platformLogin(3)
    } else if (window.location.pathname.includes('/oidcbi/')) {
      platformLogin(2)
    } else if(state?.includes('dingtalk')) {
      await dingtalkToken()
      platformLogin(5)
    } else if (state?.includes('larksuite')) {
      await larksuiteToken()
      platformLogin(7)
    } else if (state?.includes('wecom')) {
      await wecomToken()
      platformLogin(6)
    } else if (state?.includes('lark')) {
      await larkToken()
      platformLogin(4)
    } else if (state?.includes('oauth2')) {
      oauth2Token(() => {
        platformLogin(9)
      })
    } else if (state?.includes('saml2')) {
      saml2Token(() => {
        platformLogin(10)
      })
    } else {
      auto2Platform()
    }
  })
})

defineExpose({
  ssoLogin, toMfa
})

</script>

<style lang="less" scoped>
.login-list {
  margin-top: 2px;
  width: 100%;
  display: flex;
  justify-content: center;
  column-gap: 16px;
}
.de-qr-hidden {
  display: none;
}
.de-other-login-divider {
  border-top: 1px solid #1F232926;
}
.other-login-item {
  margin-bottom: 0;
}
</style>