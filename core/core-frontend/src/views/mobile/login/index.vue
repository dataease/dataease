<script lang="ts" setup>
import { ref } from 'vue'
import VanCellGroup from 'vant/es/cell-group'
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

const onSubmit = async () => {
  if (!checkUsername(username.value) || !validatePwd(password.value)) {
    showToast('用户名或密码错误')
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
</script>

<template>
  <div class="de-mobile-login" v-loading="duringLogin">
    <div class="mobile-login-content">
      <div class="mobile-login-welcome">用户登录</div>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="用户名"
            label="用户名"
            placeholder="用户名"
            :rules="[{ required: true, message: '请填写用户名' }]"
          />
          <van-field
            v-model="password"
            type="password"
            name="密码"
            label="密码"
            placeholder="密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          />
        </van-cell-group>
        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit"> 提交 </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<style lang="less">
.de-mobile-login {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url(../../../assets/logo-bg.jpg);

  .mobile-login-content {
    background-color: #fff;
    margin-top: 20px;
    position: relative;
    opacity: 0.95;
    border-radius: 10px;
    overflow: hidden;

    :deep(.van-field) {
      padding-left: 0 !important;
    }

    .mobile-login-welcome {
      padding-left: 15px;
      font-size: x-large;
      font-weight: 500;
      letter-spacing: 2px;
    }
  }
}
</style>
