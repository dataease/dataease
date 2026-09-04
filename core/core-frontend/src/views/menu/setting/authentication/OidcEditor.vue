<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const oidcForm = ref<FormInstance>()
interface OidcForm {
  clientId?: string
  clientSecret?: string
  discovery?: string
  redirectUri?: string
  realm?: string
  scope?: string
  usePkce?: boolean
  mapping?: string
}
const state = reactive({
  form: reactive<OidcForm>({
    clientId: '',
    clientSecret: '',
    discovery: '',
    redirectUri: '',
    realm: '',
    scope: '',
    usePkce: false,
    mapping: ''
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
const validateMapping = (rule, value, callback) => {
  if (!value) {
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
  clientId: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'Client ID',
      trigger: 'blur'
    },
    {
      min: 2,
      max: 50,
      message: t('commons.input_limit', [2, 50]),
      trigger: 'blur'
    }
  ],
  clientSecret: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'Client Secret',
      trigger: 'blur'
    },
    {
      min: 5,
      max: 50,
      message: t('commons.input_limit', [5, 50]),
      trigger: 'blur'
    }
  ],
  redirectUri: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'Redirect Uri',
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
  discovery: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'Discovery',
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
  realm: [
    {
      required: false,
      message: t('common.please_input') + t('common.empty') + 'Realm',
      trigger: 'blur'
    },
    {
      min: 2,
      max: 50,
      message: t('commons.input_limit', [2, 50]),
      trigger: 'blur'
    }
  ],
  scope: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'Scope',
      trigger: 'blur'
    },
    {
      min: 2,
      max: 255,
      message: t('commons.input_limit', [2, 255]),
      trigger: 'blur'
    }
  ],
  usePkce: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'Use Pkce',
      trigger: 'change'
    }
  ],
  mapping: [{ required: false, validator: validateMapping, trigger: 'blur' }]
})

const edit = () => {
  showLoading()
  request
    .get({ url: '/setting/authentication/info/oidc' })
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
        url: '/setting/authentication/save/oidc',
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
  resetForm(oidcForm.value)
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
  const url = '/setting/authentication/validate/oidc'
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
    :title="t('system.oidc_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="oidcForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="Client ID" prop="clientId">
        <el-input
          v-model="state.form.clientId"
          :placeholder="t('common.please_input') + t('common.empty') + 'Client ID'"
        />
      </el-form-item>

      <el-form-item label="Client Secret" prop="clientSecret">
        <el-input
          v-model="state.form.clientSecret"
          type="password"
          show-password
          :placeholder="t('common.please_input') + t('common.empty') + 'Client Secret'"
        />
      </el-form-item>
      <el-form-item label="Discovery" prop="discovery">
        <el-input
          v-model="state.form.discovery"
          :placeholder="t('common.please_input') + t('common.empty') + 'Discovery'"
        />
      </el-form-item>
      <el-form-item label="Realm" prop="realm">
        <el-input
          v-model="state.form.realm"
          :placeholder="t('common.please_input') + t('common.empty') + 'Realm'"
        />
      </el-form-item>
      <el-form-item label="Scope" prop="scope">
        <el-input
          v-model="state.form.scope"
          :placeholder="t('common.please_input') + t('common.empty') + 'Scope'"
        />
      </el-form-item>
      <el-form-item label="Use Pkce" prop="usePkce">
        <el-switch v-model="state.form.usePkce" />
      </el-form-item>
      <el-form-item label="Redirect Uri" prop="redirectUri">
        <el-input
          v-model="state.form.redirectUri"
          :placeholder="t('common.please_input') + t('common.empty') + 'Redirect Uri'"
        />
      </el-form-item>
      <el-form-item :label="t('system.field_mapping')" prop="mapping">
        <el-input v-model="state.form.mapping" :placeholder="t('system.oauth2name')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(oidcForm)">{{ t('common.cancel') }}</el-button>
        <el-button secondary :disabled="!state.form.clientId" @click="validate">
          {{ t('commons.test_connect') }}
        </el-button>
        <el-button type="primary" @click="submitForm(oidcForm)">
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
