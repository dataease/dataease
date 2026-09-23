<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const oauth2Form = ref<FormInstance>()
interface Oauth2Form {
  authEndpoint?: string
  tokenEndpoint?: string
  userInfoEndpoint?: string
  scope?: string
  clientId?: string
  clientSecret?: string
  redirectUri?: string
  mapping?: string
  authMethod?: string
}
const state = reactive({
  form: reactive<Oauth2Form>({
    tokenEndpoint: '',
    userInfoEndpoint: '',
    scope: '',
    clientId: '',
    clientSecret: '',
    redirectUri: '',
    mapping: '',
    authMethod: '0'
  })
})
const validateUrl = (rule, value, callback) => {
  const reg = new RegExp(/(http|https):\/\/([\w.]+\/?)\S*/)
  if (!reg.test(value)) {
    callback(new Error(t('system.incorrect_please_re_enter_de')))
  } else {
    callback()
  }
}
const validateMapping = (rule, value, callback) => {
  if (value === null || value === '') {
    callback()
  }
  try {
    JSON.parse(value)
  } catch (e) {
    callback(new Error(t('system.in_json_format')))
  }
  callback()
}
const rule = reactive<FormRules>({
  authEndpoint: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.authorization_end_address'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],
  tokenEndpoint: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.token_end_address'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],
  userInfoEndpoint: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.information_end_address'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],

  scope: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.connection_range'),
      trigger: 'blur'
    },
    {
      min: 2,
      max: 50,
      message: t('commons.input_limit', [2, 50]),
      trigger: 'blur'
    }
  ],
  clientId: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.client_id'),
      trigger: 'blur'
    },
    {
      min: 2,
      max: 255,
      message: t('commons.input_limit', [2, 255]),
      trigger: 'blur'
    }
  ],
  clientSecret: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.client_key'),
      trigger: 'blur'
    },
    {
      min: 5,
      max: 255,
      message: t('commons.input_limit', [5, 255]),
      trigger: 'blur'
    }
  ],
  redirectUri: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.callback_address'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],
  mapping: [{ required: false, validator: validateMapping, trigger: 'blur' }]
})

const edit = () => {
  showLoading()
  request
    .get({ url: '/setting/authentication/info/oauth2' })
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
        url: '/setting/authentication/save/oauth2',
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
  resetForm(oauth2Form.value)
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
  const url = '/setting/authentication/validate/oauth2'
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
    :title="t('system.oauth2_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="oauth2Form"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('system.authorization_end_address')" prop="authEndpoint">
        <el-input
          v-model="state.form.authEndpoint"
          :placeholder="
            t('common.please_input') + t('common.empty') + t('system.authorization_end_address')
          "
        />
      </el-form-item>
      <el-form-item :label="t('system.token_end_address')" prop="tokenEndpoint">
        <el-input
          v-model="state.form.tokenEndpoint"
          :placeholder="
            t('common.please_input') + t('common.empty') + t('system.token_end_address')
          "
        />
      </el-form-item>
      <el-form-item :label="t('system.information_end_address')" prop="userInfoEndpoint">
        <el-input
          v-model="state.form.userInfoEndpoint"
          :placeholder="
            t('common.please_input') + t('common.empty') + t('system.information_end_address')
          "
        />
      </el-form-item>

      <el-form-item :label="t('system.connection_range')" prop="scope">
        <el-input
          v-model="state.form.scope"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.connection_range')"
        />
      </el-form-item>

      <el-form-item :label="t('system.client_id')" prop="clientId">
        <el-input
          v-model="state.form.clientId"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.client_id')"
        />
      </el-form-item>

      <el-form-item :label="t('system.client_key')" prop="clientSecret">
        <el-input
          v-model="state.form.clientSecret"
          type="password"
          show-password
          :placeholder="t('common.please_input') + t('common.empty') + t('system.client_key')"
        />
      </el-form-item>

      <el-form-item :label="t('system.callback_address')" prop="redirectUri">
        <el-input
          v-model="state.form.redirectUri"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.callback_address')"
        />
      </el-form-item>

      <el-form-item :label="t('system.field_mapping')" prop="mapping">
        <el-input v-model="state.form.mapping" :placeholder="t('system.oauth2name')" />
      </el-form-item>

      <el-form-item :label="t('datasource.auth_method')" prop="authMethod">
        <el-radio-group v-model="state.form.authMethod">
          <el-radio label="0">Authorization Code</el-radio>
          <el-radio label="1">Client Secret Jwt</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(oauth2Form)">{{ t('common.cancel') }}</el-button>
        <el-button secondary :disabled="!state.form.clientId" @click="validate">
          {{ t('commons.test_connect') }}
        </el-button>
        <el-button type="primary" @click="submitForm(oauth2Form)">
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
