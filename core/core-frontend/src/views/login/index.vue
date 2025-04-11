<script lang="ts" setup>
import DataEase from '@/assets/svg/DataEase.svg'
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { FormRules, FormInstance } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { loginApi, queryDekey } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { CustomPassword } from '@/components/custom-password'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { rsaEncryp } from '@/utils/encryption'
import router from '@/router'
import { ElMessage } from 'element-plus-secondary'
import { XpackComponent } from '@/components/plugin'
import { logoutHandler } from '@/utils/logout'
import DeImage from '@/assets/login-desc-de.png'
import elementResizeDetectorMaker from 'element-resize-detector'
import { cleanPlatformFlag } from '@/utils/utils'
import xss from 'xss'
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const userStore = useUserStoreWithOut()
const appearanceStore = useAppearanceStoreWithOut()
const { t } = useI18n()
const contentShow = ref(true)
const loading = ref(false)
const axiosFinished = ref(true)
const showFoot = ref(false)

const loginLogoUrl = ref(null)
const msg = ref(null)
const loginImageUrl = ref(null)
const slogan = ref(null)
const footContent = ref(null)
const loginErrorMsg = ref('')
const xpackLoginHandler = ref()
const showDempTips = ref(false)
const xpackInvalidPwd = ref()
const demoTips = computed(() => {
  if (!showDempTips.value) {
    return ''
  }
  return appearanceStore.getDemoTipsContent || ''
})
const state = reactive({
  loginForm: {
    username: '',
    password: ''
  },
  footContent: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: t('common.required'), trigger: 'blur' }],
  password: [{ required: true, message: t('common.required'), trigger: 'blur' }]
})

const activeName = ref('simple')

const getCurLocation = () => {
  let queryRedirectPath = '/workbranch/index'
  if (router.currentRoute.value.query.redirect) {
    queryRedirectPath = router.currentRoute.value.query.redirect as string
  }
  return queryRedirectPath
}
const enterHandler = e => {
  e.target.blur()
  e.stopPropagation()
  handleLogin()
}
const formRef = ref<FormInstance | undefined>()
const duringLogin = ref(true)
const handleLogin = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      const name = state.loginForm.username.trim()
      const pwd = state.loginForm.password
      if (!wsCache.get(appStore.getDekey)) {
        const res = await queryDekey()
        wsCache.set(appStore.getDekey, res.data)
      }
      const param = { name: rsaEncryp(name), pwd: rsaEncryp(pwd) }
      const isLdap = activeName.value === 'ldap'
      if (isLdap) {
        param['origin'] = 1
      }
      duringLogin.value = true
      cleanPlatformFlag()
      loginApi(param)
        .then(res => {
          const { token, exp, mfa } = res.data
          if (!isLdap && !xpackLoadFail.value && xpackInvalidPwd.value?.invokeMethod) {
            const param = {
              methodName: 'init',
              args: res.data
            }
            xpackInvalidPwd?.value.invokeMethod(param)
            return
          }
          if (!isLdap && mfa?.enabled) {
            xpackLoginHandler.value?.invokeMethod({ methodName: 'toMfa', args: mfa })
            duringLogin.value = false
            return
          }
          userStore.setToken(token)
          userStore.setExp(exp)
          userStore.setTime(Date.now())
          const queryRedirectPath = getCurLocation()
          router.push({ path: queryRedirectPath })
        })
        .catch(() => {
          duringLogin.value = false
        })
    }
  })
}
const invalidPwdCb = cbParam => {
  const val = cbParam['status']
  duringLogin.value = !!val
  if (val) {
    const mfa = cbParam['mfa']
    if (mfa?.enabled) {
      xpackLoginHandler.value?.invokeMethod({ methodName: 'toMfa', args: mfa })
      duringLogin.value = false
      return
    }
    const queryRedirectPath = getCurLocation()
    router.push({ path: queryRedirectPath })
  }
}
const xpackLoadFail = ref(false)
const loadingText = ref('加载中...')
const loginContainer = ref()
const loginContainerWidth = ref(0)
const showLoginImage = computed<boolean>(() => {
  return !(loginContainerWidth.value < 889)
})

const preheat = ref(true)
const showLoginErrorMsg = () => {
  if (!loginErrorMsg.value) {
    return
  }
  if (loginErrorMsg.value.includes('token is empty')) {
    ElMessage.error('token为空！')
    return
  }
  if (loginErrorMsg.value.includes('token is Expired')) {
    ElMessage.error('登录信息已过期，请重新登录！')
    return
  }
  if (loginErrorMsg.value.includes('token is destroyed')) {
    ElMessage.error('登录信息已销毁，请重新登录！')
    return
  }
  if (loginErrorMsg.value.startsWith('user_disable')) {
    ElMessage.error('用户已被禁用，无法登录！')
    return
  }
  if (loginErrorMsg.value.startsWith('permission has been changed')) {
    ElMessage.error('默认组织已发生变更，请重新登录！')
    return
  }
  ElMessage.error(loginErrorMsg.value)
}

const loadArrearance = () => {
  showDempTips.value = appearanceStore.getShowDemoTips
  if (appearanceStore.getBg) {
    loginImageUrl.value = appearanceStore.getBg
  }
  if (appearanceStore.getLogin) {
    loginLogoUrl.value = appearanceStore.getLogin
  }
  if (appearanceStore.getSlogan) {
    slogan.value = appearanceStore.getSlogan
  }
  if (appearanceStore.getFoot) {
    showFoot.value = appearanceStore.getFoot === 'true'
    if (showFoot.value) {
      const content = appearanceStore.getFootContent
      const myXss = new xss['FilterXSS']({
        css: {
          whiteList: {
            'background-color': true,
            'text-align': true,
            color: true,
            'margin-top': true,
            'margin-bottom': true,
            'line-height': true,
            'box-sizing': true,
            'padding-top': true,
            'padding-bottom': true,
            'font-size': true
          }
        },
        whiteList: {
          ...xss['whiteList'],
          p: ['style'],
          span: ['style']
        }
      })
      footContent.value = myXss.process(content)
    }
  }
}
const switchTab = (name: string) => {
  activeName.value = name || 'simple'
}
const autoCallback = (param: any) => {
  console.log(param)
  activeName.value = param.activeName || 'simple'
  preheat.value = param.preheat
  if (param.loadingText) {
    loadingText.value = param.loadingText
  }
}
const handlerFail = () => {
  const param = {
    activeName: 'simple',
    preheat: false
  }
  autoCallback(param)
}
onMounted(async () => {
  loadArrearance()
  duringLogin.value = false
  if (localStorage.getItem('DE-GATEWAY-FLAG')) {
    const msg = localStorage.getItem('DE-GATEWAY-FLAG')
    loginErrorMsg.value = decodeURIComponent(msg)
    showLoginErrorMsg()
    localStorage.removeItem('DE-GATEWAY-FLAG')
    logoutHandler(true)
  }
  if (!wsCache.get(appStore.getDekey)) {
    queryDekey().then(res => {
      wsCache.set(appStore.getDekey, res.data)
    })
  }
  const erd = elementResizeDetectorMaker()
  erd.listenTo(loginContainer.value, () => {
    nextTick(() => {
      loginContainerWidth.value = loginContainer.value?.offsetWidth
    })
  })
})
</script>

<template>
  <div
    v-if="preheat"
    ref="loginContainer"
    class="preheat-container"
    v-loading="true"
    :element-loading-text="loadingText"
    element-loading-background="#F5F6F7"
  />
  <div v-show="contentShow" class="login-background" v-loading="duringLogin">
    <div class="login-container" ref="loginContainer">
      <div class="login-image-content" v-loading="!axiosFinished" v-if="showLoginImage">
        <el-image
          v-if="axiosFinished"
          class="login-image"
          fit="cover"
          :src="loginImageUrl || DeImage"
        />
      </div>
      <div class="login-form-content" v-loading="loading">
        <div class="login-form-center">
          <el-form
            ref="formRef"
            :model="state.loginForm"
            :rules="rules"
            size="default"
            :disabled="preheat"
          >
            <div class="login-logo">
              <Icon
                v-if="!loginLogoUrl && axiosFinished"
                className="login-logo-icon"
                name="DataEase"
              >
                <DataEase class="login-logo-icon" />
              </Icon>
              <img v-if="loginLogoUrl && axiosFinished" :src="loginLogoUrl" alt="" />
            </div>
            <div class="login-welcome">
              {{ slogan || t('system.available_to_everyone') }}
            </div>
            <div class="login-form">
              <div
                class="default-login-tabs"
                v-if="activeName === 'simple' || activeName === 'ldap'"
              >
                <div class="login-form-title">
                  <span>{{
                    activeName === 'ldap' ? t('login.ldap_login') : t('login.account_login')
                  }}</span>
                </div>
                <el-form-item class="login-form-item login-input-module" prop="username">
                  <el-input
                    v-model="state.loginForm.username"
                    :placeholder="`${t('common.account')}${
                      activeName === 'simple' ? '/' + t('commons.email') : ''
                    }`"
                    autofocus
                  />
                </el-form-item>
                <el-form-item class="login-input-module" prop="password">
                  <CustomPassword
                    v-model="state.loginForm.password"
                    :placeholder="t('common.pwd')"
                    show-password
                    maxlength="30"
                    show-word-limit
                    autocomplete="new-password"
                    @keypress.enter.stop="enterHandler"
                  />
                </el-form-item>
                <div class="login-btn">
                  <el-button
                    type="primary"
                    class="submit"
                    size="default"
                    :disabled="duringLogin"
                    @click="handleLogin"
                  >
                    {{ t('login.btn') }}
                  </el-button>
                  <div v-if="showDempTips" class="demo-tips">
                    <span>{{ demoTips }}</span>
                  </div>
                </div>
              </div>

              <XpackComponent
                ref="xpackLoginHandler"
                jsname="L2NvbXBvbmVudC9sb2dpbi9IYW5kbGVy"
                @switch-tab="switchTab"
                @auto-callback="autoCallback"
                @load-fail="handlerFail"
              />
              <XpackComponent
                ref="xpackInvalidPwd"
                jsname="L2NvbXBvbmVudC9sb2dpbi9JbnZhbGlkUHdk"
                @load-fail="() => (xpackLoadFail = true)"
                @call-back="invalidPwdCb"
              />
            </div>

            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </div>
        <div v-if="showFoot" class="dynamic-login-foot" v-html="footContent" />
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.preheat-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  position: absolute;
  z-index: 100;
}
.login-background {
  background-color: #f5f7fa;
  height: 100vh;
  width: 100vw;
}

.login-container {
  width: 100%;
  height: 100%;
  background-color: var(--ContentBG, #ffffff);
  display: flex;
  .login-image-content {
    overflow: hidden;
    height: 100%;
    width: 40%;
    min-width: 400px;
  }

  .login-form-content {
    position: relative;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    .login-form-center {
      width: 480px;
    }
  }
  .login-logo {
    text-align: center;
    img {
      width: auto;
      max-height: 52px;
      @media only screen and (max-width: 1280px) {
        width: auto;
        max-height: 52px;
      }
    }
  }

  .login-title {
    margin-top: 50px;
    font-size: 32px;
    letter-spacing: 0;
    text-align: center;
    color: #999999;

    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
  }

  .login-border {
    height: 2px;
    margin: 20px auto 20px;
    position: relative;
    width: 80px;
    background: var(--ed-color-primary);
    @media only screen and (max-width: 1280px) {
      margin: 20px auto 20px;
    }
  }

  .login-welcome {
    text-align: center;
    margin-top: 8px;
    color: #646a73;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
    word-wrap: break-word;
  }

  .demo-tips {
    position: absolute;
    font-size: 18px;
    color: #f56c6c;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
    top: 120px;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
  }

  .login-form {
    margin-top: 40px;
    padding: 40px;
    padding-top: 20px;
    box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
    border: 1px solid #dee0e3;
    border-radius: 4px;

    .login-input-module {
      width: 100%;
      :deep(.ed-input) {
        height: 40px;
        line-height: 40px;
      }
    }

    .login-form-item {
      margin-top: 24px;
    }

    .ed-form-item--default {
      margin-bottom: 24px;
    }
    .login-form-title {
      margin-top: 20px;
      color: #1f2329;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 20px;
      font-weight: 500;
      line-height: 28px;
      text-align: left;
    }
  }

  :deep(.ed-divider__text) {
    color: #8f959e;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
    padding: 0 8px;
  }

  .login-btn {
    position: relative;
    margin-bottom: 120px;
    .submit {
      width: 100%;
      height: 40px;
      line-height: 40px;
    }
  }

  .login-msg {
    margin-top: 10px;
    padding: 0 40px;
    color: #f56c6c;
    text-align: center;
  }

  .login-image {
    //object-fit: cover;
    //background: url(../../assets/login-desc-de.png);
    background-size: 100% 100%;
    width: 100%;
    height: 100%;
  }
  .login-image-de {
    background-size: cover;
    width: 100%;
    height: 520px;
    @media only screen and (max-width: 1280px) {
      height: 380px;
    }
  }
}
.dynamic-login-foot {
  visibility: visible;
  width: 100%;
  position: absolute;
  z-index: 302;
  bottom: 0;
  left: 0;
  height: auto;
  padding-top: 1px;
  zoom: 1;
  margin: 0;
}

.login-logo-icon {
  width: auto;
  height: 52px;
}
</style>
