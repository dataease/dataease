<template>
  <div v-show="contentShow" class="login-background">
    <div class="login-container">
      <el-row v-loading="loading" type="flex">
        <el-col :span="12">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" size="default">
            <div class="login-logo">
              <svg-icon v-if="!loginLogoUrl && axiosFinished" icon-class="DataEase" custom-class="login-logo-icon" />
              <img v-if="loginLogoUrl && axiosFinished" :src="loginLogoUrl" alt="">
            </div>
            <div v-if="uiInfo && uiInfo['ui.loginTitle'] && uiInfo['ui.loginTitle'].paramValue" class="login-welcome">
              {{ uiInfo['ui.loginTitle'].paramValue }}
            </div>
            <div v-else class="login-welcome">
              {{ $t('login.welcome') + (uiInfo && uiInfo['ui.title'] && uiInfo['ui.title'].paramValue || ' DataEase') }}
            </div>
            <div class="login-form">
              <el-form-item v-if="loginTypes.length > 1">
                <el-radio-group v-if="loginTypes.length > 1" v-model="loginForm.loginType" @change="changeLoginType">
                  <el-radio :label="0" size="mini">{{ $t('login.default_login') }}</el-radio>
                  <el-radio v-if="loginTypes.includes(1)" :label="1" size="mini">LDAP</el-radio>
                  <el-radio v-if="loginTypes.includes(2)" :label="2" size="mini">OIDC</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="ID" autofocus :disabled="loginTypes.includes(2) && loginForm.loginType === 2" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  :placeholder="$t('login.password')"
                  show-password
                  maxlength="30"
                  show-word-limit
                  autocomplete="new-password"
                  :disabled="loginTypes.includes(2) && loginForm.loginType === 2"
                  @keypress.enter.native="handleLogin"
                />
              </el-form-item>
            </div>
            <div class="login-btn">
              <el-button type="primary" class="submit" size="default" :disabled="loginTypes.includes(2) && loginForm.loginType === 2" @click.native.prevent="handleLogin">
                {{ $t('commons.login') }}
              </el-button>
              <div v-if="uiInfo && uiInfo['ui.demo.tips'] && uiInfo['ui.demo.tips'].paramValue" class="demo-tips">
                {{ uiInfo['ui.demo.tips'].paramValue }}
              </div>
            </div>
            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </el-col>
        <el-col v-loading="!axiosFinished" :span="12">
          <div v-if="!loginImageUrl && axiosFinished" class="login-image" />
          <div v-if="loginImageUrl && axiosFinished" class="login-image-de" :style="{background:'url(' + loginImageUrl + ') no-repeat', 'backgroundSize':'contain'}" />
        </el-col>
      </el-row>
    </div>
    <plugin-com v-if="loginTypes.includes(2) && loginForm.loginType === 2" ref="SSOComponent" component-name="SSOComponent" />

  </div>
</template>

<script>

import { encrypt } from '@/utils/rsaEncrypt'
import { ldapStatus, oidcStatus, getPublicKey, pluginLoaded } from '@/api/user'
import { getSysUI } from '@/utils/auth'
import { initTheme } from '@/utils/ThemeUtil'
import PluginCom from '@/views/system/plugin/PluginCom'
import Cookies from 'js-cookie'
export default {
  name: 'Login',
  components: { PluginCom },
  data() {
    return {
      loginForm: {
        loginType: 0,
        username: '',
        password: ''
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', message: this.$t('commons.input_id') }],
        password: [{ required: true, trigger: 'blur', message: this.$t('commons.input_pwd') }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined,
      uiInfo: null,
      loginImageUrl: null,
      loginLogoUrl: null,
      axiosFinished: false,
      loginTypes: [0],
      isPluginLoaded: false,
      contentShow: false,
      clearLocalStorage: [
        'panel-main-tree',
        'panel-default-tree',
        'chart-tree',
        'dataset-tree'
      ]
    }
  },
  computed: {
    msg() {
      return this.$store.state.user.loginMsg
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  beforeCreate() {
    pluginLoaded().then(res => {
      this.isPluginLoaded = res.success && res.data
      this.isPluginLoaded && initTheme()
      this.contentShow = true
    }).catch(() => {
      this.contentShow = true
    })

    ldapStatus().then(res => {
      if (res.success && res.data) {
        this.loginTypes.push(1)
      }
    })

    oidcStatus().then(res => {
      if (res.success && res.data) {
        this.loginTypes.push(2)
      }
    })
    getPublicKey().then(res => {
      if (res.success && res.data) {
        // 保存公钥
        localStorage.setItem('publicKey', res.data)
      }
    })
  },

  mounted() {
    // this.loading = false
  },

  created() {
    this.$store.dispatch('user/getUI').then(() => {
      // const uiLists = this.$store.state.user.uiInfo
      // this.uiInfo = format(uiLists)
      this.axiosFinished = true
      this.showLoginImage()
    }).catch(err => {
      console.error(err)
    })
    let msg = Cookies.get('OidcError')
    if (msg) {
      msg = msg.replace('+', '')
      this.$error(msg)
    }
    this.clearOidcMsg()
  },

  methods: {
    clearOidcMsg() {
      Cookies.remove('OidcError')
      Cookies.remove('IdToken')
    },
    showLoginImage() {
      this.uiInfo = getSysUI()
      if (this.uiInfo['ui.loginImage'] && this.uiInfo['ui.loginImage'].paramValue) {
        this.loginImageUrl = '/system/ui/image/' + this.uiInfo['ui.loginImage'].paramValue
      }
      if (this.uiInfo['ui.loginLogo'] && this.uiInfo['ui.loginLogo'].paramValue) {
        this.loginLogoUrl = '/system/ui/image/' + this.uiInfo['ui.loginLogo'].paramValue
      }
      /* if (this.uiInfo['ui.themeStr'] && this.uiInfo['ui.themeStr'].paramValue) {
        if (this.uiInfo['ui.themeStr'].paramValue === 'dark') {
          document.body.className = 'blackTheme'
        } else if (this.uiInfo['ui.themeStr'].paramValue === 'light') {
          document.body.className = ''
        }
      } */
    },
    initCache() {
      this.clearLocalStorage.forEach(item => {
        localStorage.removeItem(item)
      })
    },

    handleLogin() {
      this.initCache()
      this.clearOidcMsg()
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          const user = {
            username: encrypt(this.loginForm.username),
            password: encrypt(this.loginForm.password),
            loginType: this.loginForm.loginType
          }
          const publicKey = localStorage.getItem('publicKey')
          this.$store.dispatch('user/login', user).then(() => {
            this.$router.push({ path: this.redirect || '/' })
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          return false
        }
      })
    },
    changeLoginType(val) {
      if (val !== 2) return
      this.clearOidcMsg()
      this.$nextTick(() => {

      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/variables";

@mixin login-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-background {
  background-color: var(--MainBG, $--background-color-base);
  height: 100vh;
  @include login-center;
}

.login-container {
  min-width: 900px;
  width: 1280px;
  height: 520px;
  background-color: var(--ContentBG, #FFFFFF);
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
    img{
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
    background: $--color-primary;
    @media only screen and (max-width: 1280px) {
      margin: 20px auto 20px;
    }
  }

  .login-welcome {
    margin-top: 20px;
    font-size: 14px;
    color: $--color-primary;
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
    color: $--color-danger;
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

    & ::v-deep .el-input__inner {
      border-radius: 20px;
      border: 1px solid transparent;
      background: $colorBg;
    }
    & :focus {
      border: 1px solid $--color-primary;
    }
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
    color: $--color-danger;
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
</style>
