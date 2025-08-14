<script lang="ts" setup>
import icon_invisible_outlined from '@/assets/svg/icon_invisible_outlined.svg'
import icon_visible_outlined from '@/assets/svg/icon_visible_outlined.svg'
import { ref } from 'vue'
import VanCellGroup from 'vant/es/cell-group'
import mobileWholeBg from '@/assets/img/bg-mobile.png'
import mobileDeTop from '@/assets/img/mobile-de-top.png'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { showToast } from 'vant'
import { loginApi, queryDekey } from '@/api/login'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useCache } from '@/hooks/web/useCache'
import { useRouter } from 'vue-router_2'
import { rsaEncryp } from '@/utils/encryption'
import VanForm from 'vant/es/form'
import VanField from 'vant/es/field'
import VanButton from 'vant/es/button'
import { XpackComponent } from '@/components/plugin'
import { useI18n } from '@/hooks/web/useI18n'
import 'vant/es/button/style'
import 'vant/es/toast/style'
import 'vant/es/field/style'
import 'vant/es/form/style'
import 'vant/es/cell-group/style'

const { t } = useI18n()
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const userStore = useUserStoreWithOut()
const router = useRouter()
const appearanceStore = useAppearanceStoreWithOut()

const username = ref('')
const password = ref('')
const duringLogin = ref(false)

const xpackLoadFail = ref(false)
const xpackInvalidPwd = ref()
const mfaRef = ref()
const showMfa = ref(false)
const mfaData = ref({ enabled: false, ready: false, uid: '', origin: 0 })
const loginType = ref('default')
const showPlatLoginMask = ref(true)
const checkUsername = value => {
  if (!value) {
    return true
  }
  const pattern = /^[a-zA-Z0-9][a-zA-Z0-9\@._-]*$/
  const reg = new RegExp(pattern)
  return reg.test(value)
}

const validatePwd = value => {
  if (!value) {
    return true
  }
  const pattern =
    /^.*(?=.{6,20})(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@#$%^&*()_+\-\={}|":<>?`[\];',.\/])[a-zA-Z0-9~!@#$%^&*()_+\-\={}|":<>?`[\];',.\/]*$/
  const regep = new RegExp(pattern)
  return regep.test(value)
}

const visible = ref(true)
const clickRightIcon = () => {
  visible.value = !visible.value
}

const inputFocus = ref('')
const handleFocus = val => {
  inputFocus.value = val
}
const mobileLogin = ref('')
const mobileLoginBg = ref('')
const loadAppearance = () => {
  if (appearanceStore.getMobileLogin) {
    mobileLogin.value = appearanceStore.getMobileLogin
  }

  if (appearanceStore.getMobileLoginBg) {
    mobileLoginBg.value = appearanceStore.getMobileLoginBg
  }
}
loadAppearance()
const handleBlur = () => {
  inputFocus.value = ''
}

const invalidPwdCb = cbParam => {
  const val = cbParam['status']
  duringLogin.value = !!val
  if (val) {
    const mfa = cbParam['mfa']
    if (mfa?.enabled) {
      for (const key in mfa) {
        mfaData.value[key] = mfa[key]
      }
      showMfa.value = true
      duringLogin.value = false
      return
    }
    router.push({ path: '/index' })
  }
}

const closeMfa = () => {
  showMfa.value = false
}
const mfaSuccess = () => {
  router.push({ path: '/index' })
}
const onSubmit = async () => {
  if (!checkUsername(username.value) || !validatePwd(password.value)) {
    showToast({
      duration: 2000,
      message: '用户名、密码不对',
      className: 'de-mobile-error'
    })
    return
  }
  const name = username.value.trim()
  const pwd = password.value
  if (!wsCache.get(appStore.getDekey)) {
    const res = await queryDekey()
    wsCache.set(appStore.getDekey, res.data)
  }
  const isLdap = loginType.value === 'ldap'
  const param = {
    name: rsaEncryp(name),
    pwd: rsaEncryp(pwd),
    origin: isLdap ? 1 : 0
  }
  duringLogin.value = true
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
      showMfa.value = false
      if (toMfa(mfa)) {
        return
      }
      userStore.setToken(token)
      userStore.setExp(exp)
      userStore.setTime(Date.now())
      router.push({ path: '/index' })
    })
    .catch(() => {
      duringLogin.value = false
    })
}

const passwordError = ref('')
const usernameError = ref('')

const passwordEndValidate = ({ status, message }) => {
  passwordError.value = status === 'passed' ? '' : message
}

const usernameEndValidate = ({ status, message }) => {
  usernameError.value = status === 'passed' ? '' : message
}
const switchType = type => {
  loginType.value = type
}
const toMain = () => {
  router.push({ path: '/index' })
}
const toMfa = (mfa: any) => {
  const isLdap = loginType.value === 'ldap'
  if (!isLdap && mfa?.enabled) {
    for (const key in mfa) {
      mfaData.value[key] = mfa[key]
    }
    showMfa.value = true
    duringLogin.value = false
    showPlatLoginMask.value = false
    return true
  }
  return false
}
const loadFail = () => {
  xpackLoadFail.value = true
  showPlatLoginMask.value = false
}
</script>

<template>
  <div
    v-if="showPlatLoginMask"
    class="platform-login-mask"
    v-loading="true"
    :element-loading-text="t('auth.loading')"
    element-loading-background="#F5F6F7"
  />
  <div class="de-mobile-login" v-loading="duringLogin">
    <img class="mobile-login_bg" :src="mobileLoginBg ? mobileLoginBg : mobileWholeBg" alt="" />
    <div class="mobile-login-content">
      <img width="120" height="31" :src="mobileLogin ? mobileLogin : mobileDeTop" alt="" />
      <div class="mobile-login-welcome">
        {{ loginType === 'ldap' ? t('login.ldap_login') : t('login.account_login') }}
      </div>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="用户名"
            :style="{ borderColor: !!usernameError ? '#F54A45' : '#bbbfc4' }"
            :placeholder="t('login.input_account')"
            @blur="handleBlur"
            :class="inputFocus === 'username' && 'input-focus-primary'"
            @end-validate="usernameEndValidate"
            @focus="handleFocus('username')"
            :rules="[{ required: true, message: '请填写用户名' }]"
          />
          <div v-if="!!usernameError" class="van-ed-error">
            {{ usernameError }}
          </div>
          <van-field
            v-model="password"
            :type="visible ? 'password' : 'text'"
            :class="inputFocus === 'password' && 'input-focus-primary'"
            @click-right-icon="clickRightIcon"
            :style="{ borderColor: !!passwordError ? '#F54A45' : '#bbbfc4' }"
            @focus="handleFocus('password')"
            @blur="handleBlur"
            name="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
            @end-validate="passwordEndValidate"
          >
            <template #right-icon>
              <el-icon>
                <Icon v-if="visible" name="icon_invisible_outlined"
                  ><icon_invisible_outlined class="svg-icon"
                /></Icon>
                <Icon v-else name="icon_visible_outlined"
                  ><icon_visible_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </van-field>
          <div v-if="!!passwordError" class="van-ed-error">
            {{ passwordError }}
          </div>
        </van-cell-group>
        <van-button block type="primary" native-type="submit"> 登录 </van-button>
      </van-form>
    </div>
    <XpackComponent
      jsname="L2NvbXBvbmVudC9sb2dpbi9Nb2JpbGVIYW5kbGVy"
      @switch-type="switchType"
      @to-mfa="toMfa"
      @to-main="toMain"
    />
  </div>
  <XpackComponent
    ref="xpackInvalidPwd"
    jsname="L2NvbXBvbmVudC9sb2dpbi9JbnZhbGlkUHdk"
    @load-fail="loadFail"
    @call-back="invalidPwdCb"
  />
  <XpackComponent
    ref="mfaRef"
    v-if="showMfa"
    :mfa-data="mfaData"
    jsname="L2NvbXBvbmVudC9sb2dpbi9NZmFTdGVw"
    @close="closeMfa"
    @success="mfaSuccess"
  />
</template>

<style lang="less">
.platform-login-mask {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  position: absolute;
  z-index: 100;
}
.de-mobile-login {
  overflow: hidden;
  height: 100vh;
  width: 100vw;
  position: relative;
  background-size: contain;
  background-repeat: no-repeat;

  .mobile-login_bg {
    width: 100%;
    height: 100%;
    position: relative;
    z-index: 1;
  }

  .mobile-login-content {
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, #ffffff 58.86%);
    position: absolute;
    bottom: 0;
    left: 0;
    border-top-right-radius: 20px;
    border-top-left-radius: 20px;
    overflow: hidden;
    width: 100%;
    height: 70%;
    padding: 24px 16px;
    z-index: 10;
    --van-cell-group-inset-padding: 0;
    --van-cell-group-inset-radius: 0;
    --van-cell-group-background: transparent;
    --van-cell-background: transparent;
    --van-cell-vertical-padding: 12px;
    --van-button-default-height: 48px;
    --van-field-placeholder-text-color: #8f959e;

    .input-focus-primary {
      border-color: var(--ed-color-primary) !important;
    }

    .van-ed-error {
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
      margin-top: -12px;
      color: #f54a45;
    }

    .van-field__right-icon {
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: -2px;
      font-size: 20px;
    }

    .van-field {
      border: 1px solid #bbbfc4;
      border-radius: 4px;
      margin: 16px 0;
    }

    .van-cell:after {
      display: none;
    }

    .mobile-login-welcome {
      font-size: 22px;
      font-weight: 500;
      line-height: 30px;
      margin-top: 10px;
    }

    .van-button--normal {
      font-size: 17px;
      font-weight: 400;
      line-height: 24px;
    }

    .van-field__control {
      font-size: 16px;
      font-weight: 400;
      line-height: 22px;
    }

    .van-cell {
      .van-field__error-message {
        display: none;
      }
    }
  }
}
.de-mobile-error {
  background: var(--van-toast-background) !important;
}
</style>
