<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'

const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const createUserForm = ref<FormInstance>()
const state = reactive({
  roleList: [],
  form: reactive<UserForm>({
    id: null,
    account: null,
    name: null,
    email: null,
    enable: true,
    phone: null,
    phonePrefix: '+86',
    roleIds: []
  })
})

const rule = reactive<FormRules>({})

const edit = () => {
  dialogVisible.value = true
  // queryForm()
}
/* const queryForm = () => {
  showLoading()
  personInfoApi().then(res => {
    state.form = reactive<UserForm>(res.data)
    closeLoading()
  })
} */
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = null
      showLoading()
      method(param)
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))

            emits('saved')
            reset()
          }
          closeLoading()
        })
        .catch(() => {
          closeLoading()
        })
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(createUserForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.basic-info-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    title="邮件设置"
    v-model="dialogVisible"
    custom-class="basic-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="createUserForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="请求超时时间" prop="name">
        <el-input
          v-model="state.form.name"
          :placeholder="t('common.please_input') + t('user.name')"
        />
      </el-form-item>

      <el-form-item label="数据源检测时间间隔" prop="account">
        <el-input
          v-model="state.form.account"
          :placeholder="t('common.please_input') + t('common.account')"
          disabled
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(createUserForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(createUserForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style lang="less">
.basic-info-drawer {
  .editer-form-title {
    width: 100%;
    border-radius: 4px;
    background: #e1eaff;
    margin: -8px 0 16px 0;
    height: 40px;
    padding-left: 16px;

    i {
      color: var(--ed-color-primary);
      font-size: 14.666666030883789px;
    }

    .pwd {
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      text-align: left;
    }

    .pwd {
      margin: 0 8px;
      color: #1f2329;
    }
  }
  .input-with-select {
    .ed-input-group__prepend {
      width: 72px;
      background-color: #fff;
      padding: 0 20px;
      color: #1f2329;
      text-align: center;
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }
  }
}
</style>
