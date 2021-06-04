<template>
  <div class="login-background">
    <div class="login-container">
      <el-row v-loading="loading" type="flex">
        <el-col :span="12">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" size="default">
            <div class="login-logo">
              <img v-if="!loginLogoUrl" src="@/assets/DataEase-color.png" alt="">
              <img v-else :src="loginLogoUrl" alt="">
            </div>
            <div class="login-welcome">
              {{ $t('login.welcome') + (uiInfo && uiInfo['ui.title'] && uiInfo['ui.title'].paramValue || 'DATAEASE') }}
            </div>
            <div class="login-form">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="ID" autofocus />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  :placeholder="$t('login.password')"
                  show-password
                  maxlength="30"
                  show-word-limit
                  autocomplete="new-password"
                  @keyup.enter.native="handleLogin"
                />
              </el-form-item>
            </div>
            <div class="login-btn">
              <el-button type="primary" class="submit" size="default" @click.native.prevent="handleLogin">
                {{ $t('commons.login') }}
              </el-button>
            </div>
            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </el-col>
        <el-col :span="12">
          <div v-if="!loginImageUrl" class="login-image" />
          <div v-else class="login-image-de" :style="{background:'url(' + loginImageUrl + ') no-repeat', 'backgroundSize':'cover'}" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>

import { encrypt } from '@/utils/rsaEncrypt'
import { validateUserName } from '@/api/user'
import { getSysUI } from '@/utils/auth'
export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      const userName = value.trim()
      validateUserName({ userName: userName }).then(res => {
        if (res.data) {
          callback()
        } else {
          callback(this.$t('login.username_error'))
        }
      }).catch(() => {
        callback(this.$t('login.username_error'))
      })
    //   if (!validUsername(value)) {
    //     callback(new Error('Please enter the correct user name'))
    //   } else {
    //     callback()
    //   }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 8) {
        callback(this.$t('login.password_error'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
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
      loginLogoUrl: null
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
  created() {
    this.$store.dispatch('user/getUI').then(() => {
      // const uiLists = this.$store.state.user.uiInfo
      // this.uiInfo = format(uiLists)
      this.uiInfo = getSysUI()
      if (this.uiInfo['ui.loginImage'] && this.uiInfo['ui.loginImage'].paramValue) {
        this.loginImageUrl = '/system/ui/image/' + this.uiInfo['ui.loginImage'].paramValue
      }
      if (this.uiInfo['ui.loginLogo'] && this.uiInfo['ui.loginLogo'].paramValue) {
        this.loginLogoUrl = '/system/ui/image/' + this.uiInfo['ui.loginLogo'].paramValue
      }
    }).catch(err => {
      console.error(err)
    })
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          const user = {
            username: this.loginForm.username,
            password: this.loginForm.password
          }
          user.password = encrypt(user.password)
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
  background-color: $--background-color-base;
  height: 100vh;
  @include login-center;
}

.login-container {
  min-width: 900px;
  width: 1280px;
  height: 520px;
  background-color: #FFFFFF;
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
