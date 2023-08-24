<template>
  <view v-if="axiosFinished" :class="mobileBG ? 'content-de' : 'content'"
    :style="mobileBG ? {background:'url(' + mobileBG + ') no-repeat', 'backgroundSize':'cover'} : ''">

    <view class="input-group">
      <view class="input-row">
        <text class="welcome">{{$t('login.title')}}</text>
      </view>
      <view class="input-row border">
        <text class="title">{{$t('login.account')}}</text>
        <m-input class="m-input" type="text" clearable focus v-model="username"
          :placeholder="$t('login.accountPlaceholder')"></m-input>
      </view>
      <view class="input-row border">
        <text class="title">{{$t('login.password')}}</text>
        <m-input type="password" displayable v-model="password" :placeholder="$t('login.passwordPlaceholder')">
        </m-input>
      </view>

    </view>
    <view class="btn-row">
      <button type="primary" class="primary" :loading="loginBtnLoading"
        @tap="bindLogin">{{$t('login.loginbtn')}}</button>
    </view>

  </view>
</template>

<script>
  import {
    mapState,
    mapMutations
  } from 'vuex'
  import mInput from '../../components/m-input.vue'
  import {
    login,
    getInfo,
    getPublicKey,
    getUIinfo
  } from '@/api/auth'
  import {
    encrypt,
    setLocalPK,
    getLocalPK,
    setToken,
    getToken,
    setUserInfo,
    getUserInfo,
    getUrlParams
  } from '@/common/utils'

  export default {
    components: {
      mInput
    },
    data() {
      return {
        username: '',
        loginBtnLoading: false,
        password: '',
        mobileBG: null,
        axiosFinished: false
      }
    },
    computed: mapState(['forcedLogin', 'hasLogin', 'univerifyErrorMsg', 'hideUniverify']),

    onLoad() {
      uni.showLoading({
        title: this.$t('commons.loading')
      });
      this.loadUiInfo()
      this.loadPublicKey()     
      if (!this.autoLogin() && getToken() && getUserInfo()) {
        this.toMain()
      }
    },

    methods: {
      ...mapMutations(['login']),

      async loginByPwd() {
        if (this.username.length < 1) {
          uni.showToast({
            icon: 'none',
            title: this.$t('login.accFmtError')
          });
          return;
        }
        if (this.password.length < 1) {
          uni.showToast({
            icon: 'none',
            title: this.$t('login.passwordPlaceholder')
          });
          return;
        }
        const data = {
          username: encrypt(this.username),
          password: encrypt(this.password)
        };
        this.loginBtnLoading = true
        login(data).then(res => {
          if (res.success) {
            setToken(res.data.token)
            this.toMain()
          }

        }).catch(error => {
          this.loginBtnLoading = false
          let msg = error.response.data.message
          if (msg?.startsWith('MultiLoginError')) {
            msg = this.$t('login.multiLogin')
          }
          uni.showToast({
            icon: 'error',
            title: msg,
          });
        })
      },

      bindLogin() {
        this.loginByPwd()
      },

      toMain(userName) {
        getInfo().then(res => {
          setUserInfo(res.data)
          uni.reLaunch({
            url: '../tabBar/home/index',
          });
        })
      },

      loadUiInfo() {
        getUIinfo().then(res => {
          const list = res.data
          list.forEach(item => {
            if (item.paramKey === 'ui.mobileBG' && item.paramValue) {
              this.mobileBG = '/system/ui/image/' + item.paramValue
              return false
            }
          })
          setTimeout(() => {
            uni.hideLoading()
            this.axiosFinished = true
          }, 1500)

        })
      },

      loadPublicKey() {
        if (!getLocalPK()) {
          getPublicKey().then(res => {
            setLocalPK(res.data)
          })
        }
      },
      autoLogin() {
        const url = window.location.href
        const param = getUrlParams(url)
        if (param?.detoken) {
          if(param.detoken.endsWith('#/'))
          param.detoken = param.detoken.substr(0, param.detoken.length - 2)
          setToken(param.detoken)
          getInfo().then(res => {
            setUserInfo(res.data)
            const redirect = window.location.href.split('?')[0]
           
            window.location.href = redirect
          })
          return true
        }
        return false
      }

    },
    onReady() {

    }
  }
</script>

<style>
  @import "@/components/m-icon/m-icon.css";

  /*每个页面公共css */
  page {
    min-height: 100%;
    display: flex;
    font-size: 14px;
  }

  input,
  textarea,
  button {
    font-size: 14px;
  }

  /* #ifdef MP-BAIDU */
  page {
    width: 100%;
    height: 100%;
    display: block;
  }

  swan-template {
    width: 100%;
    min-height: 100%;
    display: flex;
  }

  /* 原生组件模式下需要注意组件外部样式 */
  custom-component {
    width: 100%;
    min-height: 100%;
    display: flex;
  }

  /* #endif */

  /* #ifdef MP-ALIPAY */
  page {
    min-height: 100vh;
  }

  /* #endif */

  /* 原生组件模式下需要注意组件外部样式 */
  m-input {
    width: 100%;
    /* min-height: 100%; */
    display: flex;
    flex: 1;
  }

  .content {
    display: flex;
    flex: 1;
    flex-direction: column;
    background-color: #000000;
    background-image: url(../../static/logo-bg.jpg);
    padding: 10px;
    justify-content: center;
    background-repeat: no-repeat;
    background-size: 100% 100%;
    opacity: 0.75;
  }

  .content-de {
    display: flex;
    flex: 1;
    flex-direction: column;
    background-color: #000000;
    padding: 10px;
    justify-content: center;
    background-repeat: no-repeat;
    background-size: 100% 100%;
    opacity: 0.75;
  }

  .input-group {
    background-color: #ffffff;
    margin-top: 20px;
    position: relative;
    opacity: 0.95;
    border-radius: 10px;
    overflow: hidden;
  }

  .input-group::before {
    position: absolute;
    right: 0;
    top: 0;
    left: 0;
    height: 1px;
    content: '';
    -webkit-transform: scaleY(.5);
    transform: scaleY(.5);
    background-color: #c8c7cc;
  }

  .input-group::after {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    height: 1px;
    content: '';
    -webkit-transform: scaleY(.5);
    transform: scaleY(.5);
    background-color: #c8c7cc;
  }

  .input-row {
    display: flex;
    flex-direction: row;
    position: relative;
    /* font-size: 18px; */
    height: 50px;
    line-height: 50px;
  }

  .input-row .title {
    width: 70px;
    padding-left: 15px;
  }

  .input-row.border::after {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 8px;
    height: 1px;
    content: '';
    -webkit-transform: scaleY(.5);
    transform: scaleY(.5);
    background-color: #c8c7cc;
  }

  .btn-row {
    margin-top: 10px;
    padding: 10px 0;
  }

  button.primary {
    background-color: #0faeff;
    line-height: 40px;
    border-radius: 5px;
    overflow: hidden;
  }

  .login-type {
    display: flex;
    justify-content: center;
  }

  .login-type-btn {
    line-height: 30px;
    margin: 0px 15px;
  }

  .login-type-btn.act {
    color: #0FAEFF;
    border-bottom: solid 1px #0FAEFF;
  }

  .send-code-btn {
    width: 120px;
    text-align: center;
    background-color: #0FAEFF;
    color: #FFFFFF;
  }

  .action-row {
    display: flex;
    flex-direction: row;
    justify-content: center;
  }

  .action-row navigator {
    color: #007aff;
    padding: 0 10px;
  }

  .oauth-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-around;
    flex-wrap: wrap;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
  }

  .oauth-image {
    position: relative;
    width: 50px;
    height: 50px;
    border: 1px solid #dddddd;
    border-radius: 50px;
    background-color: #ffffff;
  }

  .oauth-image image {
    width: 30px;
    height: 30px;
    margin: 10px;
  }

  .oauth-image button {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
  }

  .captcha-view {
    line-height: 0;
    justify-content: center;
    align-items: center;
    display: flex;
    position: relative;
    background-color: #f3f3f3;
  }

  .welcome {
    padding-left: 15px;
    font-size: x-large;
    font-weight: 500;
    letter-spacing: 2px;
  }
</style>