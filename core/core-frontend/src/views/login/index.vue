<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElButton, ElCol, ElRow, ElInput } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { loginApi } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import router from '@/router'
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
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

const state = reactive({
  loginTypes: [],
  qrTypes: [],
  loginForm: {
    username: '',
    loginType: 0,
    password: ''
  },
  loginRules: {},
  uiInfo: {},
  radioTypes: [],
  footContent: ''
})

const showQr = () => {
  codeShow.value = !codeShow.value
}

const changeLoginType = val => {
  if (val !== 2 && val !== 7) return
}

const handleLogin = () => {
  const param = {}
  loginApi(param)
    .then(res => {
      const token = res.data
      wsCache.set(appStore.getToken, token)
      router.push({ path: '/' })
    })
    .catch(() => {
      wsCache.set(appStore.getToken, 'i am Authorization')
      router.push({ path: '/' })
    })
}

const switchCodeIndex = codeIndex => {
  codeIndex.value = codeIndex
}
</script>

<template>
  <div v-show="contentShow" class="login-background">
    <div class="login-container">
      <el-row v-loading="loading" type="flex">
        <el-col :span="12">
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
            :rules="state.loginRules"
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
                  ' DataEase')
              }}
            </div>
            <div class="login-form">
              <el-form-item v-if="state.radioTypes.length > 1">
                <el-radio-group
                  v-if="state.radioTypes.length > 1"
                  v-model="state.loginForm.loginType"
                  @change="changeLoginType"
                >
                  <el-radio :label="0" size="small">{{ $t('login.default_login') }}</el-radio>
                  <el-radio v-if="state.loginTypes.includes(1)" :label="1" size="small"
                    >LDAP</el-radio
                  >
                  <el-radio v-if="state.loginTypes.includes(2)" :label="2" size="small"
                    >OIDC</el-radio
                  >
                  <el-radio v-if="state.loginTypes.includes(7)" :label="7" size="small"
                    >Lark</el-radio
                  >
                </el-radio-group>
              </el-form-item>
              <el-form-item prop="username">
                <el-input
                  v-model="state.loginForm.username"
                  :placeholder="
                    t('common.account') + '/' + t('common.email') + '/' + t('common.phone')
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
            </div>
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
        </el-col>
        <el-col v-loading="!axiosFinished" :span="12">
          <div v-if="!loginImageUrl && axiosFinished" class="login-image" />
          <div
            v-if="loginImageUrl && axiosFinished"
            class="login-image-de"
            :style="{
              background: 'url(' + loginImageUrl + ') no-repeat',
              backgroundSize: 'contain'
            }"
          />
        </el-col>
      </el-row>
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
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-container {
  min-width: 900px;
  width: 1280px;
  height: 520px;
  background-color: var(--ContentBG, #ffffff);
  @media only screen and (max-width: 1280px) {
    width: 900px;
    height: 380px;
  }

  .login-logo {
    margin-top: 50px;
    text-align: center;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
    img {
      /*width: 240px;*/
      width: auto;
      max-height: 60px;
      @media only screen and (max-width: 1280px) {
        /*width: 200px;*/
        width: auto;
        max-height: 50px;
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
    margin-top: 20px;
    font-size: 14px;
    color: #3370ff;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
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
    margin-top: 80px;
    padding: 0 40px;

    @media only screen and (max-width: 1280px) {
      margin-top: 40px;
    }
    & :deep(.el-input) {
      margin: 6px 0;
    }
    /*  & :deep(.el-input__inner) {
      border-radius: 20px;
      border: 1px solid transparent;
      background: rgba(10, 123, 224, 0.1);
    }
    & :focus {
      border: 1px solid #3370ff;
    } */
  }

  .login-btn {
    margin-top: 22px;
    padding: 0 40px;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }

    .submit {
      width: 100%;
      border-radius: 20px;
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
    height: 520px;
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
  height: 60px;
}
</style>
