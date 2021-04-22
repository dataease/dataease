<template>
  <div class="login-background">
    <div class="login-container">
      <el-row v-loading="loading" type="flex">
        <el-col :span="12">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" size="default">
            <div class="login-logo">
              <img src="@/assets/DataEase-black.png" alt="">
            </div>
            <div class="login-title">
              {{ $t('login.title') }}
            </div>
            <div class="login-border" />
            <div class="login-welcome">
              {{ $t('login.welcome') }}
            </div>
            <div class="login-form">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" :placeholder="$t('login.username')" autofocus />
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
                {{ $t('commons.button.login') }}
              </el-button>
            </div>
            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </el-col>
        <el-col :span="12">
          <div class="login-image" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>

import { encrypt } from '@/utils/rsaEncrypt'
import { validateUserName } from '@/api/user'
export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      const userName = value.trim()
      validateUserName({ userName: userName }).then(res => {
        if (res.data) {
          callback()
        } else {
          callback(new Error('Please enter the correct user name'))
        }
      }).catch(() => {
        callback(new Error('Please enter the correct user name'))
      })
    //   if (!validUsername(value)) {
    //     callback(new Error('Please enter the correct user name'))
    //   } else {
    //     callback()
    //   }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('The password can not be less than 6 digits'))
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
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined
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
          console.log('error submit!!')
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
  height: 100%;
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
    margin-top: 30px;
    margin-left: 30px;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }

    img {
      height: 45px;
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
      margin: 10px auto 10px;
    }
  }

  .login-welcome {
    margin-top: 50px;
    font-size: 14px;
    color: #999999;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }
  }

  .login-form {
    margin-top: 30px;
    padding: 0 40px;

    @media only screen and (max-width: 1280px) {
      margin-top: 10px;
    }

    & ::v-deep .el-input__inner {
      border-radius: 0;
    }
  }

  .login-btn {
    margin-top: 40px;
    padding: 0 40px;
    @media only screen and (max-width: 1280px) {
      margin-top: 20px;
    }

    .submit {
      width: 100%;
      border-radius: 0;
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
}
</style>
