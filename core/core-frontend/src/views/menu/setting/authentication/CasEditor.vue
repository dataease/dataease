<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const casForm = ref<FormInstance>()
interface CasForm {
  idpUri?: string
  casCallbackDomain?: string
  logoutRedirectUrl?: string
}
const state = reactive({
  form: reactive<CasForm>({
    idpUri: '',
    casCallbackDomain: '',
    logoutRedirectUrl: ''
  })
})
const validateUrl = (rule, value, callback) => {
  const reg = new RegExp(/(http|https):\/\/([\w.]+\/?)\S*/)
  if (!reg.test(value)) {
    callback(new Error(t('system.incorrect_please_re_enter')))
  } else {
    callback()
  }
}
const rule = reactive<FormRules>({
  idpUri: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'IdpUri',
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ]
})

const edit = () => {
  showLoading()
  request
    .get({ url: '/setting/authentication/info/cas' })
    .then(res => {
      const resData = res.data
      for (const key in resData) {
        state.form[key] = resData[key]
      }
    })
    .finally(() => {
      closeLoading()
    })
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = request.post({
        url: '/setting/authentication/save/cas',
        data: param
      })
      showLoading()
      method
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
  resetForm(casForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({
    target: '.platform-info-drawer'
  })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const validate = () => {
  const url = '/setting/authentication/validate/cas'
  const data = state.form
  showLoading()
  request
    .post({ url, data })
    .then(res => {
      if (res?.data === 'true') {
        ElMessage.success(t('commons.test_connect') + t('report.last_status_success'))
      } else {
        ElMessage.error(t('commons.test_connect') + t('report.last_status_fail'))
      }
    })
    .finally(() => {
      closeLoading()
      emits('saved')
    })
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('system.cas_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="casForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="IdpUri" prop="idpUri">
        <el-input
          v-model="state.form.idpUri"
          :placeholder="t('common.please_input') + t('common.empty') + 'IdpUri'"
        />
      </el-form-item>

      <el-form-item :label="t('system.callback_domain_name')" prop="casCallbackDomain">
        <el-input
          v-model="state.form.casCallbackDomain"
          :placeholder="
            t('common.please_input') + t('common.empty') + t('system.callback_domain_name')
          "
        />
      </el-form-item>

      <el-form-item :label="t('system.logout_redirect_url')" prop="logoutRedirectUrl">
        <el-input
          v-model="state.form.logoutRedirectUrl"
          :placeholder="t('system.logout_redirect_url_placeholder')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(casForm)">{{ t('common.cancel') }}</el-button>
        <el-button secondary :disabled="!state.form.idpUri" @click="validate">
          {{ t('commons.test_connect') }}
        </el-button>
        <el-button type="primary" @click="submitForm(casForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style lang="less">
.platform-info-drawer {
  .ed-drawer__footer {
    height: 64px !important;
    padding: 16px 24px !important;
    .dialog-footer {
      height: 32px;
      line-height: 32px;
    }
  }
  .ed-form-item__label {
    line-height: 22px !important;
    height: 22px !important;
  }
}
</style>
<style lang="less" scoped>
.platform-info-drawer {
  .ed-form-item {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
  .input-with-select {
    .ed-input-group__prepend {
      width: 72px;
      background-color: #fff;
      padding: 0 20px;
      color: #1f2329;
      text-align: center;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }
  }
}
</style>
