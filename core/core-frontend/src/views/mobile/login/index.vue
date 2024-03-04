<script lang="ts" setup>
import { ref } from 'vue'
import VanCellGroup from 'vant/es/cell-group'
import mobileDeTop from '@/assets/img/mobile-de-top.png'
import { showToast } from 'vant'
import { loginApi, queryDekey } from '@/api/login'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useCache } from '@/hooks/web/useCache'
import { useRouter } from 'vue-router'
import { rsaEncryp } from '@/utils/encryption'
import VanForm from 'vant/es/form'
import VanField from 'vant/es/field'
import VanButton from 'vant/es/button'
import 'vant/es/button/style'
import 'vant/es/toast/style'
import 'vant/es/field/style'
import 'vant/es/form/style'
import 'vant/es/cell-group/style'

const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const userStore = useUserStoreWithOut()
const router = useRouter()

const username = ref('')
const password = ref('')
const duringLogin = ref(false)

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

const handleBlur = () => {
  inputFocus.value = ''
}
const onSubmit = async () => {
  if (!checkUsername(username.value) || !validatePwd(password.value)) {
    showToast({
      duration: 2000,
      message: '用户名、密码不对'
    })
    return
  }
  const name = username.value.trim()
  const pwd = password.value
  if (!wsCache.get(appStore.getDekey)) {
    const res = await queryDekey()
    wsCache.set(appStore.getDekey, res.data)
  }
  const param = { name: rsaEncryp(name), pwd: rsaEncryp(pwd) }
  duringLogin.value = true
  loginApi(param)
    .then(res => {
      const { token, exp } = res.data
      userStore.setToken(token)
      userStore.setExp(exp)
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
</script>

<template>
  <div class="de-mobile-login" v-loading="duringLogin">
    <div class="mobile-login-content">
      <img width="120" height="31" :src="mobileDeTop" alt="" />
      <div class="mobile-login-welcome">用户登录</div>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="用户名"
            :style="{ borderColor: !!usernameError ? '#F54A45' : '#bbbfc4' }"
            placeholder="请输入用户名"
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
                <Icon v-if="visible" name="icon_invisible_outlined"></Icon>
                <Icon v-else name="icon_visible_outlined"></Icon>
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
  </div>
</template>

<style lang="less">
.de-mobile-login {
  height: 100vh;
  width: 100vw;
  position: relative;
  background-size: contain;
  background-repeat: no-repeat;
  background-image: url(../../../assets/img/bg-mobile.png);

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
</style>
