<template>
  <div class="mobile-handler-container">
    <el-divider v-if="anyEnable" class="de-other-login-divider">{{ t('login.other_login') }}</el-divider>
    <div class="platform-icon-div">
      <div v-if="loginCategory.ldap && activeFormName === 'default'" @click="execute('ldap')" class="item">
        <el-icon>
          <Icon name="logo_ldap"><logo_ldap class="svg-icon" /></Icon>
        </el-icon>
      </div>
      <div v-if="loginCategory.ldap && activeFormName === 'ldap'" @click="execute('default')" class="item pc">
        <el-icon>
          <Icon name="icon_pc_outlined"><icon_pc_outlined class="svg-icon" /></Icon>
        </el-icon>
      </div>
    
      <div v-if="loginCategory.oidc" @click="execute('oidc')" class="item">
        <el-icon>
          <Icon name="btn_oidc"><btn_oidc class="svg-icon" /></Icon>
        </el-icon>
      </div>

      <div v-if="loginCategory.oauth2" @click="execute('oauth2')" class="item">
        <el-icon>
          <Icon name="logo_oauth"><logo_oauth class="svg-icon" /></Icon>
        </el-icon>
      </div>

      <div v-if="loginCategory.cas" @click="execute('cas')" class="item">
        <el-icon>
          <Icon name="logo_cas"><logo_cas class="svg-icon" /></Icon>
        </el-icon>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import logo_ldap from "@/assets/svg/logo_ldap.svg"
import icon_pc_outlined from "@/assets/svg/icon_pc_outlined.svg"
import logo_cas from '@/assets/svg/logo_cas.svg'
import btn_oidc from '@/assets/svg/btn_oidc.svg'
import logo_oauth from "@/assets/svg/logo_oauth.svg";
import { Icon } from '@/components/icon-custom'
import { LoginCategory } from './PlatformClient'
import request from '@/config/axios'
import { useCache } from '@/hooks/web/useCache'
import { getQueryString } from '@/utils/utils'
import { useUserStoreWithOut } from '@/store/modules/user'
import { platformLoginApi } from '@/api/login'
import { showConfirmDialog } from 'vant'

const userStore = useUserStoreWithOut()
const { t } = useI18n()
const anyEnable = ref(true)
const loginCategory = ref({} as LoginCategory)
const { wsCache } = useCache()

const activeFormName = ref('default')

const emits = defineEmits(['switchType', 'toMain', 'toMfa'])
const execute = (type) => {
  if (type === 'default' || type === 'ldap') {
    activeFormName.value = type
    emits('switchType', type)
    return
  }
  const curOrigin = window.location.origin
  let pathname = window.location.pathname
  if (pathname) {
    if (pathname.includes('oidcbi/') || pathname.includes('casbi/')) {
      pathname = pathname.replace('oidcbi/', '')
      pathname = pathname.replace('casbi/', '')
    }
    if (pathname.includes('mobile.html')) {
      pathname = pathname.replace('mobile.html', '')
    }
    pathname = pathname.substring(0, pathname.length - 1)
  }
  const nextPage = curOrigin + pathname + (type === 'oidc' ? '/oidcbi/#/' : '/casbi/#/')
  if (type === 'oauth2') {
    toOauth2LoginPage()
    return
  }
  redirect2Auth(nextPage, type)
  return
}
const redirect2Auth = (url, type) => {
  const tipsMapping = { 'oidc': 'OIDC', 'cas': 'CAS', 'oauth2': 'OAuth' }
  let residual = 3
  let timer:any = null
  showConfirmDialog({
    message: t('login.redirect_2_auth', [tipsMapping[type], residual]),
    className: 'de2-custom-dialog',
    confirmButtonText: '立即跳转',
    beforeClose: (action) => new Promise((resolve) => {
      timer && clearInterval(timer)
      if (action !== 'confirm') {
        resolve(true)
        return
      }
      window.open(url, '_self')
      resolve(true);
    }) 
  })
  timer = setInterval(() => {
    residual--
    if (residual < 0) {
      timer && clearInterval(timer)
      window.open(url, '_self')
      return
    }
    const refreshMsg = t('login.redirect_2_auth', [tipsMapping[type], residual])
    if(document.getElementsByClassName('van-dialog__message')[0]['innerText']) {
      document.getElementsByClassName('van-dialog__message')[0]['innerText'] = refreshMsg
    }
  }, 1000)
}
const toOauth2LoginPage = () => {
  const url = '/oauth2/auth'
  request.get({ url }).then(res => {
    const data = res.data
    if (data?.authEndpoint) {
      const redirectUri = encodeURIComponent(data.redirectUri)
      const result = `${data.authEndpoint}?response_type=code&client_id=${data.clientId}&scope=${data.scope}&state=${data.state}&redirect_uri=${redirectUri}`
      redirect2Auth(result, 'oauth2')
    }
  })
}
const queryCategoryStatus = () => {
  const url = `/setting/authentication/status`
  return request.get({ url })
}
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
      cb && cb()
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
const callBackType = () => {
  return getQueryString('state')
}
const oauth2Token = (cb) => {
  const code = getQueryString('code')
  const state = getQueryString('state')
  if (!code || !state) {
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

const platformLogin = origin => {  
  platformLoginApi(origin).then(res => {
    const mfa = res.data?.mfa
    if (mfa?.enabled) {
      mfa['origin'] = origin
      emits('toMfa', mfa)
      return
    }
    const token = res.data.token
    userStore.setToken(token)
    userStore.setExp(res.data.exp)
    userStore.setTime(Date.now())
    emits('toMain')
  }).catch(() => {
    userStore.setToken('')
  })
}
onMounted(() => {
  wsCache.delete('de-platform-client')
  init(async () => {
    const state = callBackType()
    if (window.location.pathname.includes('/casbi/')) {
      platformLogin(3)
    } else if (window.location.pathname.includes('/oidcbi/')) {
      platformLogin(2)
    } else if (state?.includes('oauth2')) {
      oauth2Token(() => {
        platformLogin(9)
      })
    } else {
      const domArray = document.getElementsByClassName('platform-login-mask')
      for (let index = 0; index < domArray.length; index++) {
        domArray[index].style.display = 'none'
      }
    }
  })
})
</script>

<style lang="less" scoped>
.mobile-handler-container {
  height: 68px;
  width: 100%;
  position: absolute;
  bottom: 40px;
  padding: 0 16px;
  z-index: 10;
  .de-other-login-divider {
    margin: 10px 0;
    border-color: #1F232926;
    :deep(.ed-divider__text) {
      color: #8F959E;
      font-size: 12px;
      line-height: 20px;
      padding: 0 10px;
    }
  }
  .platform-icon-div {
    margin-top: 16px;
    height: 32px;
    line-height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    column-gap: 16px;
    .item {
      width: 32px;
      cursor: pointer;
      .ed-icon {
        font-size: 32px;
        border: 1px solid #dee0e3;
        border-radius: 50%;
      }
      display: flex;
      align-items: center;
      flex-direction: column;
      justify-content: space-between;
    }
    .pc {
      :deep(svg) {
        width: 16px;
        height: 16px;
      }
    }
  }
}
</style>

<style lang="less">
.de2-custom-dialog {
  width: 303px;
  border-radius: 8px;
  .van-dialog__content {
    padding: 24px;
    text-align: center;
    height: 70px;
  }
  .van-dialog__footer {
    display: flex;
    border-top: 0.5px solid #DEE0E3;
    button {
      border: none;
    }
    .van-dialog__confirm {
      border-radius: 0;
      color: var(--ed-color-primary, #3370ff);
    }
  }
}
</style>