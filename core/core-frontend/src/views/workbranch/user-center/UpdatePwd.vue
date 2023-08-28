<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep } from 'lodash-es'

const { t } = useI18n()

const defaultForm = {
  original: '',
  new: '',
  confirm: ''
}
const pwdForm = reactive(cloneDeep(defaultForm))
const validatePwd = (_: any, value: any, callback: any) => {
  if (value === '' || value.length < 8 || value.length > 31) {
    callback(new Error('有效密码：8-30位，英文大小写字母+数字+特殊字符（可选）'))
  } else {
    callback()
  }
}

const validateConfirmPwd = (_: any, value: any, callback: any) => {
  if (value !== pwdForm.original) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rule = {
  original: [{ required: true, message: '请输入原始密码', trigger: 'blur' }],
  new: [
    { validator: validatePwd, trigger: 'blur' },
    { required: true, message: '请输入', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请输入', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}
const updatePwdForm = ref()

const save = () => {
  updatePwdForm.value.validate(val => {
    if (val) {
      console.log('save')
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
    <el-form-item label="原始密码" prop="original">
      <el-input v-model="pwdForm.original" placeholder="请输入原始密码" />
    </el-form-item>
    <el-form-item label="新密码" prop="new">
      <el-input v-model="pwdForm.new" placeholder="请输入新密码" />
    </el-form-item>
    <el-form-item label="确认密码" prop="confirm">
      <el-input v-model="pwdForm.confirm" placeholder="请输入确认密码" />
    </el-form-item>
    <el-button style="margin-top: 12px" @click="save" type="primary">
      {{ t('common.save') }}
    </el-button>
  </el-form>
</template>

<style lang="less" scoped>
.mt16 {
  margin-top: 16px;
}
</style>
