<template>
  <div class="container" v-loading="result.loading" v-if="ready">
    <el-row type="flex">
      <el-col :span="12">

        <div class="title">
          <div class="title-img">
            <img :src="'/display/file/loginLogo'" alt="">
          </div>
          <div class="welcome">
            <span>{{ loginTitle }}</span>
          </div>
        </div>

        <div class="content">
          <div class="form">
            <el-form :model="form" :rules="rules" ref="form">
              <el-form-item v-slot:default>
                <el-radio-group v-model="form.authenticate" @change="redirectAuth(form.authenticate)">
                  <el-radio label="LDAP" size="mini" v-if="openLdap">LDAP</el-radio>
                  <el-radio label="LOCAL" size="mini" v-if="openLdap">普通登录</el-radio>
                  <el-radio :label="auth.id" size="mini" v-for="auth in authSources" :key="auth.id">{{ auth.type }} {{ auth.name }}</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item prop="username">
                <el-input v-model="form.username" :placeholder="$t('commons.login_username')" autofocus
                          autocomplete="off"/>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="form.password" :placeholder="$t('commons.password')" show-password autocomplete="off"
                          maxlength="30" show-word-limit/>
              </el-form-item>
            </el-form>
          </div>
          <div class="btn">
            <el-button type="primary" class="submit" @click="submit('form')">
              {{ $t('commons.login') }}
            </el-button>
          </div>
          <div class="msg">
            {{ msg }}
          </div>
        </div>
      </el-col>

      <div class="divider"/>

      <el-col :span="12">
        <img class="login-image" :src="'/display/file/loginImage'" alt="">
      </el-col>

    </el-row>

  </div>
</template>

<script>
import {saveLocalStorage} from '@/common/js/utils';
import {DEFAULT_LANGUAGE} from "@/common/js/constants";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const display = requireComponent.keys().length > 0 ? requireComponent("./display/Display.vue") : {};
const auth = requireComponent.keys().length > 0 ? requireComponent("./auth/Auth.vue") : {};
const license = requireComponent.keys().length > 0 ? requireComponent("./license/LicenseMessage.vue") : null;

export default {
  name: "Login",
  data() {
    return {
      result: {},
      form: {
        username: '',
        password: '',
        authenticate: 'LOCAL'
      },
      rules: {
        username: [
          {required: true, message: this.$t('commons.input_login_username'), trigger: 'blur'},
        ],
        password: [
          {required: true, message: this.$t('commons.input_password'), trigger: 'blur'},
          {min: 6, max: 30, message: this.$t('commons.input_limit', [6, 30]), trigger: 'blur'}
        ]
      },
      msg: '',
      ready: false,
      openLdap: false,
      loginTitle: this.$t("commons.welcome"),
      authSources: [],
      loginUrl: 'signin',
    }
  },
  beforeCreate() {
    this.result = this.$get("/isLogin").then(response => {

      if (display.default !== undefined) {
        display.default.showLogin(this);
      }

      if (auth.default !== undefined) {
        auth.default.getAuthSources(this);
      }

      if (!response.data.success) {
        this.ready = true;
      } else {
        let user = response.data.data;
        saveLocalStorage(response.data);
        this.getLanguage(user.language);
        window.location.href = "/";
      }
    });
    this.$get("/ldap/open", response => {
      this.openLdap = response.data;
    })
  },
  created: function () {
    // 主页添加键盘事件,注意,不能直接在焦点事件上添加回车
    document.addEventListener("keydown", this.watchEnter);
    //
    if (license.default) {
      license.default.valid(this)
    }
  },

  destroyed() {
    //移除监听回车按键
    document.removeEventListener("keydown", this.watchEnter);
  },
  methods: {
    //监听回车按钮事件
    watchEnter(e) {
      let keyNum = e.which; //获取被按下的键值
      //判断如果用户按下了回车键（keycody=13）
      if (keyNum === 13) {
        // 按下回车按钮要做的事
        this.submit('form');
      }
    },
    submit(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          switch (this.form.authenticate) {
            case "LOCAL":
              this.loginUrl = "/signin";
              this.doLogin();
              break;
            case "LDAP":
              this.loginUrl = "/ldap/signin";
              this.doLogin();
              break;
            default:
              this.loginUrl = "/sso/signin";
              this.doLogin();
          }
        } else {
          return false;
        }
      });
    },
    doLogin() {
      this.result = this.$post(this.loginUrl, this.form, response => {
        saveLocalStorage(response);
        sessionStorage.setItem('loginSuccess', 'true');
        this.getLanguage(response.data.language);
      });
    },
    getLanguage(language) {
      if (!language) {
        this.$get("language", response => {
          language = response.data;
          localStorage.setItem(DEFAULT_LANGUAGE, language);
          window.location.href = "/"
        })
      } else {
        window.location.href = "/"
      }
    },
    redirectAuth(authId) {
      if (auth.default) {
        auth.default.redirectAuth(this, authId);
      }
    }
  }
}
</script>

<style scoped>
.container {
  width: 1440px;
  height: 810px;
  margin: calc((100vh - 810px) / 2) auto 0;
  background-color: #FFFFFF;
}

.el-col:nth-child(3) {
  align-items: center;
  display: flex;
}

.title img {
  width: 293px;
  max-height: 60px;
  margin-top: 165px;
}

.title-img {
  letter-spacing: 0;
  text-align: center;
}

.login-image {
  height: 365px;
  width: 567px;
  margin: auto;
  display: block;
}

.welcome {
  margin-top: 12px;
  margin-bottom: 75px;
  font-size: 14px;
  color: #843697;
  line-height: 14px;
  text-align: center;
}

.form, .btn {
  padding: 0;
  width: 443px;
  margin: auto;
}

.btn > .submit {
  border-radius: 70px;
  border-color: #8B479B;
  background-color: #8B479B;
}

.btn > .submit:hover {
  border-color: rgba(139, 71, 155, 0.9);
  background-color: rgba(139, 71, 155, 0.9);
}

.btn > .submit:active {
  border-color: rgba(139, 71, 155, 0.8);
  background-color: rgba(139, 71, 155, 0.8);
}

.el-form-item:first-child {
  margin-top: 60px;
}

/deep/ .el-radio__input.is-checked .el-radio__inner {
  background-color: #783887;
  background: #783887;
  border-color: #783887;
}

/deep/ .el-radio__input.is-checked + .el-radio__label {
  color: #783887;
}

/deep/ .el-input__inner {
  border-radius: 70px !important;
  background: #f6f3f8 !important;
  border-color: #f6f3f8 !important;
  /*谷歌浏览器默认填充的颜色无法替换，使用下列样式填充*/
  box-shadow: inset 0 0 0 1000px #f6f3f8 !important;
}

.el-input, .el-button {
  width: 443px;
}

/deep/ .el-input__inner:focus {
  border: 1px solid #783887 !important;
}

.divider {
  border: 1px solid #f6f3f8;
  height: 480px;
  margin: 165px 0px;
}

</style>

<style>
body {
  font-family: -apple-system, BlinkMacSystemFont, "Neue Haas Grotesk Text Pro", "Arial Nova", "Segoe UI", "Helvetica Neue", ".PingFang SC", "PingFang SC", "Source Han Sans SC", "Noto Sans CJK SC", "Source Han Sans CN", "Noto Sans SC", "Source Han Sans TC", "Noto Sans CJK TC", "Hiragino Sans GB", sans-serif;
  font-size: 14px;
  /*background-color: #F5F5F5;*/
  line-height: 26px;
  color: #2B415C;
  -webkit-font-smoothing: antialiased;
  margin: 0;
}

.form .el-input > .el-input__inner {
  border-radius: 0;
}
</style>

