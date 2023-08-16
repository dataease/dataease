<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElButton, ElRow, ElInput, FormRules } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { loginApi, queryDekey } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useUserStoreWithOut } from '@/store/modules/user'
import { rsaEncryp } from '@/utils/encryption'
import router from '@/router'
import { toPlatformPage, setLoginForm, callback } from './Platform'
import { ElMessage } from 'element-plus-secondary'

const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const userStore = useUserStoreWithOut()
const { t } = useI18n()
const contentShow = ref(true)
const loading = ref(false)
const codeShow = ref(false)
const imgAppShow = ref(false)
const axiosFinished = ref(true)
const showFoot = ref(false)

const loginLogoUrl = ref(null)
const msg = ref(null)
const loginImageUrl = ref(null)
const footContent = ref(null)
const codeIndex = ref(0)
const loginErrorMsg = ref('')
const state = reactive({
  loginTypes: [2, 3],
  qrTypes: [],
  loginForm: {
    username: '',
    loginType: 0,
    password: ''
  },
  uiInfo: {},
  radioTypes: [0, 2, 3],
  footContent: ''
})

const checkUsername = (_rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error(t('commons.required')))
  }
  setTimeout(() => {
    const reg = /^[a-zA-Z][a-zA-Z0-9]{2,9}/
    if (!reg.test(value)) {
      callback(new Error(t('login.username_format')))
    } else {
      callback()
    }
  }, 1000)
}

const rules = reactive<FormRules>({
  username: [{ validator: checkUsername, trigger: 'blur' }],
  password: [
    { required: true, message: t('commons.required'), trigger: 'blur' },
    { min: 5, max: 15, message: t('login.pwd_format'), trigger: 'blur' }
  ]
})

const showQr = () => {
  codeShow.value = !codeShow.value
}

const activeName = ref('simple')
const handleClick = tab => {
  console.log('tab', tab)
}

const changeLoginType = val => {
  toPlatformPage(val)
}
const getCurLocation = () => {
  let queryRedirectPath = '/workbranch/index'
  if (router.currentRoute.value.query.redirect) {
    queryRedirectPath = router.currentRoute.value.query.redirect as string
  }
  return queryRedirectPath
}
const handleLogin = () => {
  const name = state.loginForm.username
  const pwd = state.loginForm.password
  const param = { name: rsaEncryp(name), pwd: rsaEncryp(pwd) }
  loginApi(param).then(res => {
    const token = res.data
    userStore.setToken(token)
    const queryRedirectPath = getCurLocation()
    router.push({ path: queryRedirectPath })
  })
}

const handleTypeClick = ele => {
  state.loginForm.loginType = ele.type
  changeLoginType(ele.type)
  switch (ele.value) {
    case 'qrcode':
      activeName.value = 'dingding'
      break
    default:
      break
  }
  if (['account', 'qrcode']) {
    activeType.value = ele.value
  }
}

const iconMap = {
  OIDC: 'btn_oidc',
  OAUTH: 'logo_oauth',
  CAS: 'logo_cas',
  Lark: 'logo_lark',
  qrcode: 'icon_qr_outlined',
  account: 'icon_member_filled'
}

const activeType = ref('account')

const loginList = ref([
  {
    label: '扫码登录',
    value: 'qrcode',
    type: 0
  },
  {
    label: 'OIDC',
    value: 'OIDC',
    type: 2
  },
  {
    label: 'OAUTH 2',
    value: 'OAUTH',
    type: 0
  },
  {
    label: 'CAS',
    value: 'CAS',
    type: 3
  },
  {
    label: '国际飞书',
    value: 'Lark',
    type: 7
  }
])

const switchCodeIndex = codeIndex => {
  codeIndex.value = codeIndex
}

onMounted(() => {
  if (localStorage.getItem('DE-GATEWAY-FLAG')) {
    loginErrorMsg.value = localStorage.getItem('DE-GATEWAY-FLAG')
    ElMessage.error(loginErrorMsg.value)
    localStorage.removeItem('DE-GATEWAY-FLAG')
  }
  if (!wsCache.get(appStore.getDekey)) {
    queryDekey().then(res => {
      wsCache.set(appStore.getDekey, res.data)
    })
  }
  setLoginForm(state)
  callback()
})
</script>

<template>
  <div v-show="contentShow" class="login-background">
    <div class="login-container">
      <div class="login-image-content" v-loading="!axiosFinished">
        <div v-if="!loginImageUrl && axiosFinished" class="login-image" />
        <div
          v-if="loginImageUrl && axiosFinished"
          class="login-image-de"
          :style="{
            background: 'url(' + loginImageUrl + ') no-repeat',
            backgroundSize: 'contain'
          }"
        />
      </div>
      <div class="login-form-content" v-loading="loading">
        <div class="login-form-center">
          <div
            v-show="state.qrTypes.length"
            :class="codeShow ? 'trans-pc' : 'trans'"
            @click="showQr"
          >
            <div v-show="imgAppShow" class="imgApp" />
          </div>
          <el-form
            v-show="!codeShow"
            ref="loginForm"
            :model="state.loginForm"
            :rules="rules"
            size="default"
          >
            <div class="login-logo">
              <Icon
                v-if="!loginLogoUrl && axiosFinished"
                className="login-logo-icon"
                name="DataEase"
              ></Icon>
              <img v-if="loginLogoUrl && axiosFinished" :src="loginLogoUrl" alt="" />
            </div>
            <div
              v-if="
                state.uiInfo &&
                state.uiInfo['ui.loginTitle'] &&
                state.uiInfo['ui.loginTitle'].paramValue
              "
              class="login-welcome"
            >
              {{ state.uiInfo['ui.loginTitle'].paramValue }}
            </div>
            <div v-else class="login-welcome">
              {{
                t('login.welcome') +
                ((state.uiInfo &&
                  state.uiInfo['ui.title'] &&
                  state.uiInfo['ui.title'].paramValue) ||
                  ' DataEase 数据可视化分析平台')
              }}
            </div>
            <div class="login-form">
              <el-tabs v-model="activeName" @tab-click="handleClick">
                <template v-if="activeType === 'account'">
                  <el-tab-pane label="普通登录" name="simple"></el-tab-pane>
                  <el-tab-pane label="LDAP" name="ldap"></el-tab-pane>
                </template>
                <template v-else>
                  <el-tab-pane label="企业微信" name="wx"></el-tab-pane>
                  <el-tab-pane label="钉钉" name="dingding"></el-tab-pane>
                  <el-tab-pane label="飞书" name="fly"></el-tab-pane>
                </template>
              </el-tabs>
              <div class="login-qrcode" v-if="activeName === 'dingding'">
                <div class="title">
                  <el-icon>
                    <Icon name="logo_dingtalk"></Icon>
                  </el-icon>
                  钉钉登录
                </div>
                <div class="qrcode">钉钉登录</div>
              </div>
              <template v-else>
                <el-form-item class="login-form-item" prop="username">
                  <el-input
                    v-model="state.loginForm.username"
                    :placeholder="
                      t('common.account') + '/' + t('commons.email') + '/' + t('commons.phone')
                    "
                    autofocus
                    :disabled="state.loginTypes.includes(2) && state.loginForm.loginType === 2"
                  />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input
                    v-model="state.loginForm.password"
                    :placeholder="t('common.pwd')"
                    show-password
                    maxlength="30"
                    show-word-limit
                    autocomplete="new-password"
                    :disabled="state.loginTypes.includes(2) && state.loginForm.loginType === 2"
                    @keypress.enter="handleLogin"
                  />
                </el-form-item>
                <div class="login-btn">
                  <el-button
                    type="primary"
                    class="submit"
                    size="default"
                    :disabled="state.loginTypes.includes(2) && state.loginForm.loginType === 2"
                    @click="handleLogin"
                  >
                    {{ t('login.btn') }}
                  </el-button>
                  <div
                    v-if="
                      state.uiInfo &&
                      state.uiInfo['ui.demo.tips'] &&
                      state.uiInfo['ui.demo.tips'].paramValue
                    "
                    class="demo-tips"
                  >
                    {{ state.uiInfo['ui.demo.tips'].paramValue }}
                  </div>
                </div>
              </template>
              <el-divider> 其他登录方式 </el-divider>
              <el-form-item v-if="state.radioTypes.length > 1">
                <div class="login-list" v-if="state.radioTypes.length > 1">
                  <div
                    @click="handleTypeClick(ele)"
                    v-for="ele in loginList"
                    :key="ele.value"
                    class="item"
                    :class="ele.value"
                  >
                    <el-icon>
                      <Icon :name="iconMap[ele.value]"></Icon>
                    </el-icon>
                    <span class="name">
                      {{ ele.label }}
                    </span>
                  </div>
                </div>
              </el-form-item>
            </div>

            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
          <div v-show="codeShow" class="code">
            <el-row class="code-contaniner">
              <plugin-com
                v-if="codeShow && state.loginTypes.includes(4) && codeIndex === 4"
                ref="WecomQr"
                component-name="WecomQr"
              />
              <plugin-com
                v-if="codeShow && state.loginTypes.includes(5) && codeIndex === 5"
                ref="DingtalkQr"
                component-name="DingtalkQr"
              />
              <plugin-com
                v-if="codeShow && state.loginTypes.includes(6) && codeIndex === 6"
                ref="LarkQr"
                component-name="LarkQr"
              />
            </el-row>

            <div v-if="state.qrTypes.length > 1" class="login-third-items">
              <span
                v-if="state.qrTypes.includes(4)"
                class="login-third-item login-third-wecom"
                @click="switchCodeIndex(4)"
              />
              <span
                v-if="state.qrTypes.includes(5)"
                class="login-third-item login-third-dingtalk"
                @click="switchCodeIndex(5)"
              />
              <span
                v-if="state.qrTypes.includes(6)"
                class="login-third-item login-third-lark"
                @click="switchCodeIndex(6)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    <plugin-com
      v-if="state.loginTypes.includes(2) && state.loginForm.loginType === 2"
      ref="SSOComponent"
      component-name="SSOComponent"
    />

    <plugin-com
      v-if="state.loginTypes.includes(7) && state.loginForm.loginType === 7"
      ref="LarksuiteQr"
      component-name="LarksuiteQr"
    />
  </div>
  <div v-if="showFoot" class="dynamic-login-foot" v-html="footContent" />
</template>

<style lang="less" scoped>
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
    height: 100%;
    width: 800px;
  }

  .login-form-content {
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
      /*width: 240px;*/
      width: auto;
      max-height: 52px;
      @media only screen and (max-width: 1280px) {
        /*width: 200px;*/
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
    background: #3370ff;
    @media only screen and (max-width: 1280px) {
      margin: 20px auto 20px;
    }
  }

  .login-welcome {
    text-align: center;
    margin-top: 8px;
    color: #646a73;
    font-family: PingFang SC;
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
  }

  .demo-tips {
    margin-top: 20px;
    font-size: 18px;
    color: #f56c6c;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
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

    .login-form-item {
      margin-top: 24px;
    }

    .login-qrcode {
      height: 300px;
      display: flex;
      align-items: center;
      flex-direction: column;
      .qrcode {
        display: flex;
        width: 200px;
        height: 200px;
        padding: 8px 12px;
        justify-content: center;
        align-items: center;
        border-radius: 8px;
        border: 1px solid #bbbfc4;
        background: #fff;
      }

      .title {
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 24px 0 12px 0;
        overflow: hidden;
        font-family: PingFang SC;
        font-size: 18px;
        font-style: normal;
        font-weight: 500;
        line-height: 26px;
        .ed-icon {
          margin-right: 8px;
          font-size: 24px;
        }
      }
    }

    .login-list {
      margin-top: -8px;
      width: 100%;
      display: flex;
      .item {
        width: 64px;
        cursor: pointer;
        margin-right: 16px;
        &:last-child {
          margin-right: 8px;
        }

        &.qrcode,
        &.account {
          .ed-icon {
            padding: 5px;
          }
        }

        .ed-icon {
          font-size: 32px;
          border: 1px solid #dee0e3;
          border-radius: 50%;
        }
        display: flex;
        align-items: center;
        flex-direction: column;
        justify-content: space-between;

        .name {
          margin-top: 8px;
          color: #000;
          text-align: center;
          font-family: PingFang SC;
          font-size: 12px;
          font-style: normal;
          font-weight: 400;
          line-height: 20px; /* 166.667% */
        }
      }
    }

    .ed-form-item--default {
      margin-bottom: 24px;
    }
  }

  :deep(.ed-divider__text) {
    color: #8f959e;
    font-family: PingFang SC;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
    padding: 0 8px;
  }

  .login-btn {
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
    background: url(../../assets/login-desc.png) no-repeat;
    background-size: cover;
    width: 100%;
    height: 100%;
    @media only screen and (max-width: 1280px) {
      height: 380px;
    }
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
  position: fixed;
  z-index: 302;
  bottom: 0;
  left: 0;
  height: auto;
  padding-top: 1px;
  zoom: 1;
  margin: 0;
}

.trans {
  position: absolute;
  left: calc(50% - 64px);
  top: 0;
  width: 64px;
  height: 64px;
  background-image: url(../../assets/qrcode.png);
  cursor: pointer;
}
.trans-pc {
  margin-left: calc(100% - 64px);
  top: 0;
  width: 64px;
  height: 64px;
  background: var(--primary, #3370ff) url(../../assets/xianshiqi-2.png) no-repeat top right/40px;
  cursor: pointer;
}
.imgApp {
  width: 64px;
  height: 64px;
  background: linear-gradient(225deg, transparent 45px, #fff 0);
}
.code {
  width: 100%;
  height: 100%; //将登录框挤出显示区域
  text-align: center;
  img {
    width: 150px;
    height: 150px;
    padding: 10px;
  }
  .code-contaniner {
    height: 410px;
  }
}
.login-third-item {
  display: inline-block;
  width: 32px;
  height: 32px;
  border-radius: 50% 50%;
  background: #ccc;
  cursor: pointer;
  margin: 0 10px;
}
.login-third-wecom {
  background: url(../../assets/wecom.png) no-repeat 50% / cover;
}
.login-third-dingtalk {
  background: url(../../assets/dingding01.png) no-repeat 50% / cover;
}
.login-third-lark {
  background: url(../../assets/lark.png) no-repeat 50% / cover;
}

.login-logo-icon {
  width: auto;
  height: 52px;
}
</style>
