<template>
  <div class="login-background">
    <div class="login-container">
      <el-row type="flex" v-loading="loading">
        <el-col :span="12">
          <el-form :model="form" :rules="rules" ref="form" size="default">
            <div class="login-logo">
              <img src="../../assets/RackShift-black.png" alt="">
            </div>
            <div class="login-title">
              {{ $t('login.title') }}
            </div>
            <div class="login-border"></div>
            <div class="login-welcome">
              {{ $t('login.welcome') }}
            </div>
            <div class="login-form">
              <el-form-item prop="username">
                <el-input v-model="form.username" :placeholder="$t('login.username')" autofocus/>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="form.password" :placeholder="$t('login.password')"
                          show-password maxlength="30" show-word-limit
                          autocomplete="new-password"/>
              </el-form-item>
            </div>
            <div class="login-btn">
              <el-button type="primary" class="submit" @click="submit('form')" size="default">
                {{ $t('commons.button.login') }}
              </el-button>
            </div>
            <div class="login-msg">
              {{ msg }}
            </div>
          </el-form>
        </el-col>
        <el-col :span="12">
          <div class="login-image"></div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { encrypt } from '@/utils/rsaEncrypt'
export default {
  name: "Login",
  data() {
    return {
      loading: false,
      form: {
        username: 'admin',
        password: '123456'
      },
      rules: {
        username: [
          {required: true, message: this.$tm('commons.validate.input', 'login.username'), trigger: 'blur'},
        ],
        password: [
          // 先去掉方便测试
          {required: true, message: this.$tm('commons.validate.input', 'login.password'), trigger: 'blur'},
          {min: 6, max: 30, message: this.$t('commons.validate.limit', [6, 30]), trigger: 'blur'}
        ]
      },
      msg: '',
      redirect: undefined,
      otherQuery: {}
    }
  },
  watch: {
    $route: {
      handler: function (route) {
        const query = route.query
        if (query) {
          this.redirect = query.redirect
          this.otherQuery = this.getOtherQuery(query)
        }
      },
      immediate: true
    }
  },
  created: function () {
    document.addEventListener("keydown", this.watchEnter);
  },

  destroyed() {
    document.removeEventListener("keydown", this.watchEnter);
  },
  methods: {
    watchEnter(e) {
      let keyCode = e.keyCode;
      if (keyCode === 13) {
        this.submit('form');
      }
    },
    submit(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          const user = {
            username: this.form.username,
            password: this.form.password          
          }
          user.password = encrypt(user.password)
          this.loading = true;
          this.$store.dispatch('user/login', user).then(() => {
            this.$router.push({path: this.redirect || '/', query: this.otherQuery})
            this.loading = false
          }).catch(error => {
            this.msg = error.message
            this.loading = false
          })
        } else {
          return false;
        }
      });
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur]
        }
        return acc
      }, {})
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/common/variables";

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
