<template>
  <el-dialog
    v-model="dialogFormVisible"
    :show-close="false"
    :close-on-click-modal="false"
    :title="t('user.change_password')"
    @close="closeHandler"
    width="640"
  >
    <el-form
      ref="updatePwdForm"
      require-asterisk-position="right"
      :model="pwdForm"
      :rules="rule"
      class="mt16"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('system.original_password')" prop="pwd">
        <CustomPassword
          v-model="pwdForm.pwd"
          show-password
          type="password"
          :placeholder="t('system.the_original_password')"
        />
      </el-form-item>
      <el-form-item :label="t('system.new_password')" prop="newPwd">
        <CustomPassword
          v-model="pwdForm.newPwd"
          show-password
          type="password"
          :placeholder="t('system.the_new_password')"
        />
      </el-form-item>
      <el-form-item :label="t('system.confirm_password')" prop="confirm">
        <CustomPassword
          v-model="pwdForm.confirm"
          show-password
          type="password"
          :placeholder="t('system.the_confirmation_password')"
          @keypress.enter.stop="enterHandler"
        />
      </el-form-item>
      <el-button @click="save" type="primary" :class="{ 'is-mobile-btn': mobileEnv }">
        {{ t('common.save') }}
      </el-button>
    </el-form>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep } from 'lodash-es'
import request from '@/config/axios'
import { rsaEncryp } from '@/utils/encryption'
import { ElMessage } from 'element-plus-secondary'
import { logoutHandler } from '@/utils/logout'
import { CustomPassword } from '@/components/custom-password'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useCache } from '@/hooks/web/useCache'
import { isMobile } from '@/utils/utils'
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const { t } = useI18n()
const dialogFormVisible = ref(false)
const defaultForm = {
  pwd: '',
  newPwd: '',
  confirm: ''
}
const token = ref()
const exp = ref()
const userId = ref()
const pwdForm = reactive(cloneDeep(defaultForm))
const mobileEnv = computed(() => isMobile())

const validatePwd = (_: any, value: any, callback: any) => {
  if (value === pwdForm.pwd) {
    callback(new Error(t('system.be_the_same')))
  }
  const pattern =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@#$%^&*()_+\-\={}|":<>?`[\];',.\/])[a-zA-Z0-9~!@#$%^&*()_+\-\={}|":<>?`[\];',.\/]{8,20}$/
  const regep = new RegExp(pattern)
  if (!regep.test(value)) {
    const msg = t('user.pwd_pattern_error')
    callback(new Error(msg))
  } else {
    callback()
  }
}

const validateConfirmPwd = (_: any, value: any, callback: any) => {
  if (value !== pwdForm.newPwd) {
    callback(new Error(t('system.twice_are_inconsistent')))
  } else {
    callback()
  }
}

const rule = {
  pwd: [
    {
      required: true,
      message: t('system.the_original_password'),
      trigger: 'blur'
    },
    {
      min: 6,
      max: 20,
      message: t('commons.input_limit', [6, 20]),
      trigger: 'blur'
    }
  ],
  newPwd: [
    {
      required: true,
      message: t('system.the_new_password'),
      trigger: 'blur'
    },
    { validator: validatePwd, trigger: 'blur' }
  ],
  confirm: [
    {
      required: true,
      message: t('system.the_confirmation_password'),
      trigger: 'blur'
    },
    {
      min: 8,
      max: 20,
      message: t('commons.input_limit', [8, 20]),
      trigger: 'blur'
    },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}
const updatePwdForm = ref()
const enterHandler = e => {
  e.target.blur()
  e.stopPropagation()
  save
}
const save = () => {
  updatePwdForm.value.validate(val => {
    if (val) {
      const pwd = rsaEncryp(pwdForm.pwd)
      const newPwd = rsaEncryp(pwdForm.newPwd)
      userStore.setToken(token.value)
      userStore.setExp(exp.value)
      userStore.setTime(Date.now())
      request
        .post({ url: '/login/modifyInvalidPwd', data: { uid: userId.value, pwd, newPwd } })
        .then(() => {
          ElMessage.success(t('system.log_in_again'))
          dialogFormVisible.value = false
          if (mobileEnv.value) {
            emits('callBack', false)
            return
          }
          logoutHandler(true)
        })
    }
  })
}
const closeHandler = () => {
  pwdForm.pwd = ''
  pwdForm.confirm = ''
  pwdForm.newPwd = ''
  token.value = null
  exp.value = null
  dialogFormVisible.value = false
}
const init = data => {
  userId.value = ''
  wsCache.delete('pwd-validity-period')
  const invalidPwd = data.invalidPwd
  const cbParam = { ...data, ...{ status: true } }
  token.value = data.token
  exp.value = data.exp
  if (!invalidPwd) {
    userStore.setToken(token.value)
    userStore.setExp(exp.value)
    userStore.setTime(Date.now())
    emits('callBack', cbParam)
    return
  }
  userId.value = invalidPwd.uid
  if (invalidPwd.invalid) {
    userStore.clear()
    userStore.$reset()
    dialogFormVisible.value = true
    cbParam['status'] = false
    emits('callBack', cbParam)
    return
  }
  if (invalidPwd.validityPeriod === 0) {
    cbParam['status'] = false
    emits('callBack', cbParam)
    ElMessage.error(t('login.pwd_invalid_error'))
    userStore.clear()
    userStore.$reset()
    return
  }
  if (invalidPwd.validityPeriod < 7) {
    wsCache.set('pwd-validity-period', invalidPwd.validityPeriod)
  }
  userStore.setToken(token.value)
  userStore.setExp(exp.value)
  userStore.setTime(Date.now())
  cbParam['status'] = true
  emits('callBack', cbParam)
}
const emits = defineEmits(['callBack'])
defineExpose({
  init
})
</script>
<style lang="less" scoped>
.mt16 {
  margin-top: 16px;
  .ed-form-item {
    margin-bottom: 20px;
    :deep(label) {
      line-height: 22px !important;
    }
  }
}
.is-mobile-btn {
  width: 100%;
}
</style>
