<template>
  <div class="link-pwd-dialog-container">
    <el-dialog
      v-model="dialogVisible"
      :title="t('user.change_password')"
      width="420"
      :append-to-body="true"
      :before-close="handleClose"
    >
      <div class="link-pwd-container">
        <el-form ref="pwdForm" :model="state.form" :rules="rule" label-position="top">
          <el-form-item :label="t('system.new_password')" prop="pwd">
            <el-input v-model="state.form.pwd" :placeholder="t('commons.input_password')" />
            <div class="tips ed-form-item__error">
              {{ t('work_branch.password_hint') }}
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button secondary @click.stop="cancel">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click.stop="save">
            {{ t('common.save') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const dialogVisible = ref(false)
const pwdForm = ref()
const originPwd = ref('')
const state = reactive({
  form: reactive<any>({
    pwd: ''
  })
})
const rule = reactive<any>({
  pwd: [
    { required: true, message: t('work_branch.password_null_hint'), trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+])[A-Za-z\d!@#$%^&*()_+]{4,10}$/,
      message: t('work_branch.password_hint'),
      trigger: 'blur'
    }
  ]
})

const handleClose = done => {
  state.form.pwd = ''
  originPwd.value = ''
  pwdForm.value.resetFields()
  done()
}
const cancel = () => {
  state.form.pwd = ''
  originPwd.value = ''
  pwdForm.value.resetFields()
  dialogVisible.value = false
}
const emits = defineEmits(['pwdChange'])
const save = () => {
  const formEl = pwdForm.value
  if (!formEl) return
  formEl.validate(valid => {
    if (valid) {
      if (originPwd.value !== state.form.pwd) {
        emits('pwdChange', state.form.pwd)
      }
      cancel()
    }
  })
}
const open = (pwd: string) => {
  state.form.pwd = pwd
  originPwd.value = pwd
  dialogVisible.value = true
}

defineExpose({
  open
})
</script>

<style lang="less" scoped>
.link-pwd-container {
  width: 100%;
  :deep(.ed-form-item) {
    margin-bottom: 2px;
    height: 108px;
  }
  :deep(.ed-form-item__label) {
    line-height: 22px !important;
    height: 22px;
  }
  :deep(.ed-form-item__error:not(.tips)) {
    display: none;
  }
  .tips {
    color: #8f959e;
    line-height: 22px;
    font-size: 14px;
    font-weight: 400;
  }
  :deep(.is-error) {
    .tips {
      color: red !important;
    }
  }
}
</style>
