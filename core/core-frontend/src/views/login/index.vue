<script lang="ts" setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { FormRules, FormInstance } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { loginApi, queryDekey } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
import { CustomPassword } from '@/components/custom-password'
import { useUserStoreWithOut } from '@/store/modules/user'
import { rsaEncryp } from '@/utils/encryption'
import router from '@/router'
import { ElMessage } from 'element-plus-secondary'
import { XpackComponent } from '@/components/plugin'
import { logoutHandler } from '@/utils/logout'
import DeImage from '@/assets/login-desc-de.png'
import elementResizeDetectorMaker from 'element-resize-detector'

const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const userStore = useUserStoreWithOut()
const { t } = useI18n()
const contentShow = ref(true)
const loading = ref(false)
const axiosFinished = ref(true)
const showFoot = ref(false)

const loginLogoUrl = ref(null)
const msg = ref(null)
const loginImageUrl = ref(null)
const footContent = ref(null)
const loginErrorMsg = ref('')
const xpackLoginHandler = ref()
const state = reactive({
  loginForm: {
    username: '',
    password: ''
  },
  uiInfo: {},
  footContent: ''
})
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

const rules = reactive<FormRules>({
  username: [{ required: true, message: t('common.required'), trigger: 'blur' }],
  password: [{ required: true, message: t('common.required'), trigger: 'blur' }]
})

const activeName = ref('simple')
const handleClick = tab => {
  const param = { methodName: 'tabSwicther', args: tab }
  xpackLoginHandler?.value.invokeMethod(param)
}

const getCurLocation = () => {
  let queryRedirectPath = '/workbranch/index'
  if (router.currentRoute.value.query.redirect) {
    queryRedirectPath = router.currentRoute.value.query.redirect as string
  }
  return queryRedirectPath
}

const formRef = ref<FormInstance | undefined>()
const duringLogin = ref(false)
const handleLogin = () => {
  if (!formRef.value) return
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      if (!checkUsername(state.loginForm.username) || !validatePwd(state.loginForm.password)) {
        ElMessage.error('用户名或密码错误')
        return
      }
      const name = state.loginForm.username.trim()
      const pwd = state.loginForm.password
      const param = { name: rsaEncryp(name), pwd: rsaEncryp(pwd) }
      duringLogin.value = true
      loginApi(param)
        .then(res => {
          const token = res.data
          userStore.setToken(token)
          const queryRedirectPath = getCurLocation()
          router.push({ path: queryRedirectPath })
        })
        .catch(() => {
          duringLogin.value = false
        })
    }
  })
}
const ldapValidate = callback => {
  if (!formRef.value) return
  formRef.value.validate((valid: boolean) => {
    if (valid && callback) {
      callback()
    }
  })
}
const activeType = ref('account')
const tablePaneList = ref([{ title: '普通登录', name: 'simple' }])
const xpackLoaded = info => {
  tablePaneList.value.push(info)
}

const loginContainer = ref()
const loginContainerWidth = ref(0)
const showLoginImage = computed<boolean>(() => {
  return !(loginContainerWidth.value < 1040)
})

onMounted(() => {
  if (localStorage.getItem('DE-GATEWAY-FLAG')) {
    loginErrorMsg.value = localStorage.getItem('DE-GATEWAY-FLAG')
    ElMessage.error(loginErrorMsg.value)
    localStorage.removeItem('DE-GATEWAY-FLAG')
    logoutHandler(true)
  }
  if (!wsCache.get(appStore.getDekey)) {
    queryDekey().then(res => {
      wsCache.set(appStore.getDekey, res.data)
    })
  }
  const erd = elementResizeDetectorMaker()
  erd.listenTo(loginContainer.value, element => {
    nextTick(() => {
      loginContainerWidth.value = loginContainer.value?.offsetWidth
    })
  })
})
</script>

<template>
  <div v-show="contentShow" class="login-background" v-loading="duringLogin">
    <div class="login-container" ref="loginContainer">
      <div class="login-image-content" v-loading="!axiosFinished" v-if="showLoginImage">
        <el-image
          v-if="!loginImageUrl && axiosFinished"
          class="login-image"
          fit="cover"
          :src="DeImage"
        />
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
          <el-form ref="formRef" :model="state.loginForm" :rules="rules" size="default">
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
              <el-tabs v-model="activeName" @tab-click="handleClick" class="default-login-tabs">
                <template v-if="activeType === 'account'">
                  <el-tab-pane
                    v-for="item in tablePaneList"
                    :key="item.name"
                    :label="item.title"
                    :name="item.name"
                  ></el-tab-pane>
                </template>
              </el-tabs>
              <XpackComponent
                class="default-login-tabs"
                :active-name="activeName"
                @validate="ldapValidate"
                jsname="L2NvbXBvbmVudC9sb2dpbi9MZGFw"
              />

              <template v-if="activeName === 'simple'">
                <div class="default-login-tabs">
                  <el-form-item class="login-form-item" prop="username">
                    <el-input
                      v-model="state.loginForm.username"
                      :placeholder="
                        t('common.account') + '/' + t('commons.email') + '/' + t('commons.phone')
                      "
                      autofocus
                    />
                  </el-form-item>
                  <el-form-item prop="password">
                    <CustomPassword
                      v-model="state.loginForm.password"
                      :placeholder="t('common.pwd')"
                      show-password
                      maxlength="30"
                      show-word-limit
                      autocomplete="new-password"
                      @keypress.enter="handleLogin"
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
                </div>
              </template>

              <XpackComponent
                ref="xpackLoginHandler"
                jsname="L2NvbXBvbmVudC9sb2dpbi9IYW5kbGVy"
                @loaded="xpackLoaded"
              />
            </div>

            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </div>
      </div>
    </div>
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
    width: 45%;
    max-width: 640px;
    min-width: 400px;
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
  position: fixed;
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
