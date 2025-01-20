<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep } from 'lodash-es'
import request from '@/config/axios'
import { rsaEncryp } from '@/utils/encryption'
import { ElMessage } from 'element-plus-secondary'
import { logoutHandler } from '@/utils/logout'
import { CustomPassword } from '@/components/custom-password'

const { t } = useI18n()

const defaultForm = {
  pwd: '',
  newPwd: '',
  confirm: ''
}
const pwdForm = reactive(cloneDeep(defaultForm))

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
      message: t('common.require'),
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
      message: t('common.require'),
      trigger: 'blur'
    },
    { validator: validatePwd, trigger: 'blur' }
  ],
  confirm: [
    {
      required: true,
      message: t('common.require'),
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

const save = () => {
  updatePwdForm.value.validate(val => {
    if (val) {
      const pwd = rsaEncryp(pwdForm.pwd)
      const newPwd = rsaEncryp(pwdForm.newPwd)
      request.post({ url: '/user/modifyPwd', data: { pwd, newPwd } }).then(() => {
        ElMessage.success(t('system.log_in_again'))
        logoutHandler()
      })
    }
  })
}
</script>

<template>
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
      />
    </el-form-item>
    <el-button @click="save" type="primary">
      {{ t('common.save') }}
    </el-button>
  </el-form>
</template>

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
</style>
