<template>
  <div class="pwd-body" v-loading="loading">
    <div class="pwd-wrapper">
      <div class="pwd-content">
        <div class="span-header">
          <div class="bi-text">
            <span style="text-align: center">{{ t('pblink.key_pwd') }} </span>
          </div>
        </div>

        <div class="input-layout">
          <div class="input-main">
            <div class="div-input">
              <el-form ref="pwdForm" :model="form" :rules="rule" size="small" @submit.stop.prevent>
                <el-form-item label="" prop="password">
                  <CustomPassword
                    v-model="form.password"
                    maxlength="10"
                    minlength="4"
                    show-password
                    class="real-input"
                    :placeholder="t('pblink.input_placeholder')"
                  />
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div class="abs-input">
            <div class="input-text">{{ msg }}</div>
          </div>
        </div>

        <div class="auth-root-class">
          <el-button size="small" type="primary" @click="refresh(pwdForm)">{{
            t('pblink.sure_bt')
          }}</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import { useAppStoreWithOut } from '@/store/modules/app'
import { rsaEncryp } from '@/utils/encryption'
import { useCache } from '@/hooks/web/useCache'
import { queryDekey } from '@/api/login'
import { CustomPassword } from '@/components/custom-password'
import { useRoute } from 'vue-router'
const route = useRoute()
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()

const { t } = useI18n()
const msg = ref('')
const loading = ref(true)
const pwdForm = ref<FormInstance>()
const form = ref({
  password: ''
})
const rule = reactive<FormRules>({
  password: [
    { required: true, message: t('pblink.key_pwd'), trigger: 'blur' },
    {
      required: true,
      pattern: /^[A-Za-z\d!@#$%^&*()_+]{4,10}$/,
      message: t('pblink.pwd_format_error'),
      trigger: 'blur'
    }
  ]
})

const refresh = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      const uuid = route.params.uuid
      const pwd = form.value.password
      const text = `${uuid},${pwd}`
      const ciphertext = rsaEncryp(text)
      request.post({ url: '/share/validate', data: { ciphertext } }).then(res => {
        if (res.data) {
          wsCache.set(`link-${uuid}`, ciphertext)
          window.location.reload()
        } else {
          msg.value = '密码错误'
        }
      })
    } else {
      console.error('error submit!', fields)
    }
  })
}
onMounted(() => {
  if (!wsCache.get(appStore.getDekey)) {
    queryDekey()
      .then(res => {
        wsCache.set(appStore.getDekey, res.data)
      })
      .finally(() => {
        loading.value = false
      })
  }
  loading.value = false
})
</script>

<style lang="less" scoped>
.pwd-body {
  position: absolute;
  width: 100%;
  margin: 0;
  padding: 0;
  top: 0;
  left: 0;
  background-repeat: repeat;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  -o-user-select: none;
  user-select: none;
  color: #3d4d66;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-decoration: none;
  -kthml-user-focus: normal;
  -moz-user-focus: normal;
  -moz-outline: 0 none;
  outline: 0 none;
  height: 100%;
  display: block;
}
.pwd-wrapper {
  background-color: #f7f8fa;
  height: 100%;
  justify-content: center !important;
  align-items: center !important;
  min-height: 25px;
  display: flex;
  -moz-flex-direction: row;
  -o-flex-direction: row;
  flex-direction: row;
  -moz-justify-content: flex-start;
  -ms-justify-content: flex-start;
  -o-justify-content: flex-start;
  justify-content: flex-start;
  -moz-align-items: flex-start;
  -ms-align-items: flex-start;
  -o-align-items: flex-start;
  align-items: flex-start;
  -o-flex-wrap: nowrap;
  flex-wrap: nowrap;
}
.pwd-content {
  width: 450px;
  height: 250px;
  position: relative;
  flex-shrink: 0;
  background-color: #ffffff;
  display: block;
}
.span-header {
  position: relative;
  margin: 57px auto 0px;
  justify-content: center !important;
  align-items: center !important;
}
.bi-text {
  max-width: 100%;
  text-align: center;
  white-space: pre;
  text-overflow: ellipsis;
  position: relative;
  flex-shrink: 0;
  box-sizing: border-box;
  overflow: hidden;
  overflow-x: hidden;
  overflow-y: hidden;
  word-break: break-all;
  display: block;
}
.input-layout {
  width: 200px;
  position: relative;
  margin: 0px auto;
  padding: 0;
  display: block;
}
.input-main {
  width: 192px;
  height: 35px;
  position: relative;
  margin-top: 30px;
  // border: 1px solid #e8eaed;
  display: block;
}
.abs-input {
  height: 20px;
  position: relative;
  margin-top: 5px;
  display: block;
}
.input-text {
  height: 20px;
  line-height: 20px;
  text-align: center;
  white-space: pre;
  text-overflow: ellipsis;
  left: 0px;
  top: 0px;
  bottom: 0px;
  position: absolute;
  color: #e65251;
  box-sizing: border-box;
}
.auth-root-class {
  margin: 15px 0px 5px;
  text-align: center;
}
</style>
